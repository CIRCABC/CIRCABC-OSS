import { inject, Service } from '@angular/core';
import { LoginService } from 'app/core/login.service';
import { CBC_BASE_PATH, SERVER_URL } from 'app/core/variables';
import { NodeIdPipe } from 'app/shared/pipes/nodeid.pipe';

/**
 * Application-wide service responsible for uploading binary file content to the
 * CIRCABC backend.
 *
 * Unlike the generated API clients (which use Angular's `HttpClient`), this
 * service performs raw `multipart/form-data` uploads via `XMLHttpRequest`. It
 * covers creating new documents, updating existing document content (including
 * checked-out documents) and replacing user avatars.
 *
 * Key collaborators:
 * - {@link LoginService}: supplies the authentication ticket sent as a Basic
 *   `Authorization` header on every request.
 * - {@link NodeIdPipe}: normalises the raw `nodeRef` returned by the backend
 *   into a plain node identifier.
 * - `CBC_BASE_PATH` / `SERVER_URL` injection tokens: provide the base URLs used
 *   to build the upload/update endpoints.
 *
 * Provided in the root injector, so a single instance is shared across the app.
 */
@Service()
export class UploadService {
  /** Service used to retrieve the current authentication ticket. */
  private readonly loginService = inject(LoginService);

  /** Base URL of the CIRCABC REST API (from the `CBC_BASE_PATH` token). */
  private readonly circabcURL!: string;
  /** Base URL of the server hosting the legacy upload/update endpoints (from the `SERVER_URL` token). */
  private readonly serverURL!: string;
  /** Pipe instance used to convert a backend `nodeRef` into a plain node id. */
  private readonly nodeIdPipe: NodeIdPipe;

  /**
   * Initialises the service by resolving the base URLs from the injected
   * configuration tokens and instantiating the {@link NodeIdPipe} used to
   * transform returned node references.
   */
  public constructor() {
    const circabcBasePath = inject(CBC_BASE_PATH);
    const serverURL = inject(SERVER_URL);

    if (circabcBasePath) {
      this.circabcURL = circabcBasePath;
    }

    if (serverURL) {
      this.serverURL = serverURL;
    }

    this.nodeIdPipe = new NodeIdPipe();
  }

  /**
   * Uploads a new file into the given folder, creating a new document node.
   *
   * @param file - The file to upload.
   * @param folderID - Identifier of the destination folder node.
   * @returns A promise resolving to the plain node id of the newly created
   * document.
   * @throws Error if the upload request fails or the server returns a non-OK
   * status.
   */
  public async uploadNewFile(file: File, folderID: string): Promise<string> {
    const result: { nodeRef?: string } = (await this.httpUploadFile(
      file,
      folderID
    )) as { nodeRef?: string };
    return this.nodeIdPipe.transform(result.nodeRef as string);
  }

  /**
   * Replaces the content of an existing document with a new file version.
   *
   * @param file - The new file content.
   * @param nodeId - Identifier of the document node to update.
   * @param notify - Whether subscribers should be notified about the update.
   * @param generateNewFileName - Whether the backend should generate a new file
   * name for the uploaded content. Defaults to `true`.
   * @returns A promise resolving to the plain node id of the updated document.
   * @throws Error if the update request fails or the server returns a non-OK
   * status.
   */
  public async updateExistingFileContent(
    file: File,
    nodeId: string,
    notify: boolean,
    generateNewFileName = true
  ) {
    const result: { nodeRef?: string } = (await this.httpUpdateFile(
      file,
      nodeId,
      notify,
      generateNewFileName
    )) as { nodeRef?: string };
    return this.nodeIdPipe.transform(result.nodeRef as string);
  }

  /**
   * Uploads new content for a document that is currently checked out.
   *
   * @param file - The new file content.
   * @param nodeId - Identifier of the checked-out document node.
   * @param notify - Whether subscribers should be notified about the update.
   * @returns A promise that resolves once the update completes.
   * @throws Error if the update request fails or the server returns a non-OK
   * status.
   */
  public async updateCheckedOutFileContent(
    file: File,
    nodeId: string,
    notify: boolean
  ) {
    await this.httpUpdate(file, nodeId, notify);
  }

  /**
   * Uploads a new avatar image for the given user.
   *
   * @param userId - Identifier of the user whose avatar is being replaced.
   * @param file - The image file to use as the new avatar.
   * @returns A promise that resolves once the avatar has been updated.
   * @throws Error if the request fails or the server returns a non-OK status.
   */
  public async updateAvatar(userId: string, file: File) {
    await this.httpUpdateAvatar(userId, file);
  }

  /**
   * Builds and sends the raw upload request that creates a new document node
   * under the given destination folder.
   *
   * @param file - The file to upload.
   * @param destination - Identifier of the destination folder node.
   * @returns A promise resolving to the parsed server response (expected to
   * contain a `nodeRef`).
   */
  private async httpUploadFile(file: File, destination: string) {
    return new Promise((resolve, reject) => {
      const ticket = this.loginService.getTicket();
      const url = `${this.serverURL}rest/upload/${destination}`;
      this.sendRequest(url, ticket, file, resolve, reject);
    });
  }

  /**
   * Builds and sends the raw update request that replaces the content of an
   * existing document via the legacy `rest/update` endpoint.
   *
   * @param file - The new file content.
   * @param destination - Identifier of the document node to update.
   * @param notify - Whether subscribers should be notified about the update.
   * @param generateNewFileName - Whether the backend should generate a new file
   * name for the uploaded content.
   * @returns A promise resolving to the parsed server response (expected to
   * contain a `nodeRef`).
   */
  private async httpUpdateFile(
    file: File,
    destination: string,
    notify: boolean,
    generateNewFileName: boolean
  ) {
    return new Promise((resolve, reject) => {
      const ticket = this.loginService.getTicket();
      const url = `${this.serverURL}rest/update/${destination}?notify=${notify}&generateNewFileName=${generateNewFileName}`;
      this.sendRequest(url, ticket, file, resolve, reject);
    });
  }

  /**
   * Builds and sends the raw update request for a checked-out document via the
   * CIRCABC `content/{nodeId}/update` endpoint.
   *
   * @param file - The new file content.
   * @param nodeId - Identifier of the checked-out document node.
   * @param notify - Whether subscribers should be notified about the update.
   * @returns A promise resolving to the parsed server response.
   */
  private async httpUpdate(file: File, nodeId: string, notify: boolean) {
    return new Promise((resolve, reject) => {
      const ticket = this.loginService.getTicket();
      const url = `${this.circabcURL}/content/${nodeId}/update?notify=${notify}`;
      this.sendRequest(url, ticket, file, resolve, reject);
    });
  }

  /**
   * Builds and sends the raw request that uploads a new avatar image for the
   * given user via the CIRCABC `users/{userId}/avatar` endpoint.
   *
   * @param userId - Identifier of the user whose avatar is being replaced.
   * @param file - The image file to upload.
   * @returns A promise resolving to the parsed server response.
   */
  private async httpUpdateAvatar(userId: string, file: File) {
    return new Promise((resolve, reject) => {
      const ticket = this.loginService.getTicket();
      const url = `${this.circabcURL}/users/${userId}/avatar`;
      this.sendRequest(url, ticket, file, resolve, reject);
    });
  }

  /**
   * Performs the actual `multipart/form-data` POST upload using
   * `XMLHttpRequest`, wiring the result into the supplied promise callbacks.
   *
   * The authentication ticket is sent as a Basic `Authorization` header, and
   * the file is attached to the form under the `filedata` field. On completion
   * with an HTTP 200 status the parsed JSON response is passed to `resolve`;
   * any other status rejects with an `Error` carrying the status text.
   *
   * @param url - Fully-qualified endpoint the file is posted to.
   * @param ticket - Authentication ticket used to build the Basic auth header.
   * @param file - The file to upload; must be defined.
   * @param resolve - Callback invoked with the parsed response on success.
   * @param reject - Callback invoked with an `Error` on failure.
   * @throws Error if `file` is not provided.
   */
  private sendRequest(
    url: string,
    ticket: string,
    file: File,
    resolve: (value?: {} | PromiseLike<{}> | undefined) => void,
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    reject: (reason?: any) => void
  ) {
    const request = new XMLHttpRequest();
    request.open('POST', url);
    request.setRequestHeader('Authorization', `Basic ${btoa(ticket)}`);
    request.setRequestHeader('X-Requested-With', 'XMLHttpRequest');

    if (globalThis.location.origin) {
      request.setRequestHeader('X-Origin', globalThis.location.origin);
    }

    const formData = new FormData();
    if (file) {
      formData.append('filedata', file);
    } else {
      throw new Error('Invalid parameters please specify');
    }
    const doneState = 4;
    const OK = 200;

    request.onreadystatechange = function () {
      if (request.readyState === doneState) {
        if (request.status === OK) {
          resolve(JSON.parse(request.response) as Record<string, unknown>);
        } else {
          reject(new Error(this.statusText));
        }
      }
    };
    request.send(formData);
  }
}
