import { Inject, Injectable, Optional } from '@angular/core';
import { LoginService } from 'app/core/login.service';
import { CBC_BASE_PATH, SERVER_URL } from 'app/core/variables';
import { NodeIdPipe } from 'app/shared/pipes/nodeid.pipe';

@Injectable({
  providedIn: 'root',
})
export class UploadService {
  private circabcURL!: string;
  private serverURL!: string;
  private nodeIdPipe: NodeIdPipe;

  public constructor(
    private loginService: LoginService,
    @Optional()
    @Inject(CBC_BASE_PATH)
    circabcBasePath: string,
    @Optional()
    @Inject(SERVER_URL)
    serverURL: string
  ) {
    if (circabcBasePath) {
      this.circabcURL = circabcBasePath;
    }

    if (serverURL) {
      this.serverURL = serverURL;
    }

    this.nodeIdPipe = new NodeIdPipe();
  }

  public async uploadNewFile(file: File, folderID: string): Promise<string> {
    const result: { nodeRef?: string } = (await this.httpUploadFile(
      file,
      folderID
    )) as { nodeRef?: string };
    return this.nodeIdPipe.transform(result.nodeRef as string);
  }

  public async updateExistingFileContent(
    file: File,
    nodeId: string,
    generateNewFileName = true
  ) {
    const result: { nodeRef?: string } = (await this.httpUpdateFile(
      file,
      nodeId,
      generateNewFileName
    )) as { nodeRef?: string };
    return this.nodeIdPipe.transform(result.nodeRef as string);
  }

  public async updateCheckedOutFileContent(file: File, nodeId: string) {
    await this.httpUpdate(file, nodeId);
  }

  public async updateAvatar(userId: string, file: File) {
    await this.httpUpdateAvatar(userId, file);
  }

  private async httpUploadFile(file: File, destination: string) {
    return new Promise((resolve, reject) => {
      const ticket = this.loginService.getTicket();
      const url = `${this.serverURL}rest/upload/${destination}`;
      this.sendRequest(url, ticket, file, resolve, reject);
    });
  }

  private async httpUpdateFile(
    file: File,
    destination: string,
    generateNewFileName: boolean
  ) {
    return new Promise((resolve, reject) => {
      const ticket = this.loginService.getTicket();
      const url = `${this.serverURL}rest/update/${destination}?generateNewFileName=${generateNewFileName}`;
      this.sendRequest(url, ticket, file, resolve, reject);
    });
  }

  private async httpUpdate(file: File, nodeId: string) {
    return new Promise((resolve, reject) => {
      const ticket = this.loginService.getTicket();
      const url = `${this.circabcURL}/content/${nodeId}/update`;
      this.sendRequest(url, ticket, file, resolve, reject);
    });
  }

  private async httpUpdateAvatar(userId: string, file: File) {
    return new Promise((resolve, reject) => {
      const ticket = this.loginService.getTicket();
      const url = `${this.circabcURL}/users/${userId}/avatar`;
      this.sendRequest(url, ticket, file, resolve, reject);
    });
  }

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
          resolve(JSON.parse(request.response));
        } else {
          reject(new Error(this.statusText));
        }
      }
    };
    request.send(formData);
  }
}
