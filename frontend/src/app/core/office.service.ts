import { Service } from '@angular/core';
import {
  Client,
  OneDriveLargeFileUploadTask,
} from '@microsoft/microsoft-graph-client';
import { DriveItem, Permission } from '@microsoft/microsoft-graph-types';

/**
 * Root-provided Angular service that wraps Microsoft Graph OneDrive
 * operations used for the Office online-editing integration.
 *
 * It provides thin helpers to inspect, create, read, upload, delete and
 * share files stored in the signed-in user's OneDrive, all scoped around a
 * dedicated CIRCABC root folder. Every method receives an already
 * authenticated Microsoft Graph {@link Client} instance as its first
 * argument, so this service holds no authentication state of its own.
 *
 * Key collaborators:
 * - `@microsoft/microsoft-graph-client` {@link Client} for issuing Graph API requests.
 * - `OneDriveLargeFileUploadTask` for chunked uploads of large files.
 * - `@microsoft/microsoft-graph-types` {@link DriveItem} / {@link Permission} models.
 */
@Service()
export class OfficeService {
  /** Name of the dedicated OneDrive folder that holds CIRCABC files. */
  public static readonly rootFolder = 'CIRCABC';

  /**
   * Checks whether a folder exists at the root of the user's OneDrive.
   *
   * @param graphClient - Authenticated Microsoft Graph client.
   * @param folderName - Name of the folder to look up under the drive root.
   * @returns A promise resolving to `true` if the folder exists, `false`
   * otherwise (any request error is treated as "does not exist").
   */
  async rootFolderExists(
    graphClient: Client,
    folderName: string
  ): Promise<boolean> {
    try {
      const _result = await graphClient
        .api(`/me/drive/root:/${folderName}`)
        .get();
      return true;
    } catch (error) {
      console.error(error);
      return false;
    }
  }
  /**
   * Creates a new folder at the root of the user's OneDrive.
   *
   * The Graph request uses a `fail` conflict behavior, so the operation
   * rejects if a folder with the same name already exists.
   *
   * @param graphClient - Authenticated Microsoft Graph client.
   * @param folder - Name of the folder to create.
   * @returns A promise resolving to the created {@link DriveItem}.
   * @throws Rejects with the Graph API error if creation fails (for
   * example when a conflicting item already exists).
   */
  async createFolder(graphClient: Client, folder: string): Promise<DriveItem> {
    const driveItem = {
      name: folder,
      folder: {},
      '@microsoft.graph.conflictBehavior': 'fail',
    };
    const result = await graphClient
      .api('/me/drive/root/children')
      .post(driveItem);
    return result.value;
  }
  /**
   * Lists the child items contained in the given OneDrive folder.
   *
   * @param graphClient - Authenticated Microsoft Graph client.
   * @param folder - Path of the folder (relative to the drive root) whose
   * children should be listed.
   * @returns A promise resolving to the array of {@link DriveItem} children.
   */
  async getFiles(graphClient: Client, folder: string): Promise<DriveItem[]> {
    const path = `/me/drive/root:/${folder}:/children`;
    const result = await graphClient.api(path).get();
    return result.value;
  }

  /**
   * Retrieves the metadata of a single file within a OneDrive folder.
   *
   * @param graphClient - Authenticated Microsoft Graph client.
   * @param folder - Path of the folder (relative to the drive root)
   * containing the file.
   * @param file - Name of the file to retrieve.
   * @returns A promise resolving to the {@link DriveItem} describing the file.
   */
  async getFile(
    graphClient: Client,
    folder: string,
    file: string
  ): Promise<DriveItem> {
    const path = `/me/drive/root:/${folder}/${file}`;
    const result = await graphClient.api(path).get();
    return result;
  }

  /**
   * Deletes a single file from a OneDrive folder.
   *
   * @param graphClient - Authenticated Microsoft Graph client.
   * @param folder - Path of the folder (relative to the drive root)
   * containing the file.
   * @param file - Name of the file to delete.
   * @returns A promise resolving to the Graph API delete response.
   */
  async deleteFile(
    graphClient: Client,
    folder: string,
    file: string
  ): Promise<DriveItem> {
    const path = `/me/drive/root:/${folder}/${file}`;
    const result = await graphClient.api(path).delete();
    return result;
  }

  /**
   * Downloads the binary content of a file stored in OneDrive.
   *
   * It first fetches the file metadata to obtain its temporary
   * `@microsoft.graph.downloadUrl`, then performs a `fetch` against that
   * URL to retrieve the raw bytes.
   *
   * @param graphClient - Authenticated Microsoft Graph client.
   * @param folder - Path of the folder (relative to the drive root)
   * containing the file.
   * @param file - Name of the file whose content should be downloaded.
   * @returns A promise resolving to a {@link Blob} with the file content.
   */
  async getFileContent(
    graphClient: Client,
    folder: string,
    file: string
  ): Promise<Blob> {
    const fileData = await this.getFile(graphClient, folder, file);

    const response = await fetch(
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      (fileData as any)['@microsoft.graph.downloadUrl'] as string
    );
    const blob = await response.blob();
    return blob;
  }

  /**
   * Uploads a file to the CIRCABC root folder using a chunked
   * (resumable) upload session, suitable for large files.
   *
   * Uploads are performed in 1 MiB ranges via
   * {@link OneDriveLargeFileUploadTask}.
   *
   * @param graphClient - Authenticated Microsoft Graph client.
   * @param file - Binary content to upload.
   * @param fileName - Name to give the uploaded file.
   * @returns A promise resolving to the upload task response describing the
   * uploaded item.
   */
  async largeFileUpload(
    graphClient: Client,
    file: Blob,
    fileName: string
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
  ): Promise<any> {
    const options = {
      path: `/${OfficeService.rootFolder}`,
      fileName: fileName,
      rangeSize: 1024 * 1024,
    };
    const uploadTask = await OneDriveLargeFileUploadTask.create(
      graphClient,
      file,
      options
    );

    const response = await uploadTask.upload();
    return response;
  }

  /**
   * Shares a OneDrive item with another user by sending them a write-access
   * invitation that requires sign-in.
   *
   * @param graphClient - Authenticated Microsoft Graph client.
   * @param itemId - Identifier of the drive item to share.
   * @param email - Email address of the recipient to invite.
   * @returns A promise resolving to the created {@link Permission}.
   */
  async inviteUser(
    graphClient: Client,
    itemId: string,
    email: string
  ): Promise<Permission> {
    const permission = {
      recipients: [
        {
          email: email,
        },
      ],
      message: "Here's the file that we're collaborating on.",
      requireSignIn: true,
      sendInvitation: true,
      roles: ['write'],
    };

    const result = await graphClient
      .api(`/me/drive/items/${itemId}/invite`)
      .post(permission);
    return result.value;
  }
}
