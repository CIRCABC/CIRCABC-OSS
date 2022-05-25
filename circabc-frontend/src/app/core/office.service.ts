import { Injectable } from '@angular/core';
import {
  Client,
  OneDriveLargeFileUploadTask,
} from '@microsoft/microsoft-graph-client';
import { DriveItem, Permission } from '@microsoft/microsoft-graph-types';

@Injectable({
  providedIn: 'root',
})
export class OfficeService {
  public static readonly rootFolder = 'CIRCABC';
  async rootFolderExists(
    graphClient: Client,
    folderName: string
  ): Promise<boolean> {
    try {
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      const result = await graphClient
        .api(`/me/drive/root:/${folderName}`)
        .get();
      return true;
    } catch (error) {
      return false;
    }
  }
  async createFolder(graphClient: Client, folder: string): Promise<DriveItem> {
    const driveItem = {
      name: folder,
      folder: {},
      '@microsoft.graph.conflictBehavior': 'fail',
    };
    const result = await graphClient
      .api(`/me/drive/root/children`)
      .post(driveItem);
    return result.value;
  }
  async getFiles(graphClient: Client, folder: string): Promise<DriveItem[]> {
    const path = `/me/drive/root:/${folder}:/children`;
    const result = await graphClient.api(path).get();
    return result.value;
  }

  async getFile(
    graphClient: Client,
    folder: string,
    file: string
  ): Promise<DriveItem> {
    const path = `/me/drive/root:/${folder}/${file}`;
    const result = await graphClient.api(path).get();
    return result;
  }

  async deleteFile(
    graphClient: Client,
    folder: string,
    file: string
  ): Promise<DriveItem> {
    const path = `/me/drive/root:/${folder}/${file}`;
    const result = await graphClient.api(path).delete();
    return result;
  }

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
