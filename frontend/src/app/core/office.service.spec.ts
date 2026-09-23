import { TestBed } from '@angular/core/testing';
import {
  Client,
  OneDriveLargeFileUploadTask,
} from '@microsoft/microsoft-graph-client';
import { vi } from 'vitest';
import { OfficeService } from './office.service';

function createMockGraphClient() {
  const client = {
    api: vi.fn().mockReturnThis(),
    headers: vi.fn().mockReturnThis(),
    get: vi.fn(),
    post: vi.fn(),
    delete: vi.fn(),
  };
  client.api.mockReturnValue(client);
  return client as unknown as Client & typeof client;
}

describe('OfficeService', () => {
  let service: OfficeService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(OfficeService);
  });

  it('should be created', () => {
    expect(service).toBeDefined();
  });

  it('should have rootFolder set to CIRCABC', () => {
    expect(OfficeService.rootFolder).toBe('CIRCABC');
  });

  describe('rootFolderExists', () => {
    it('should return true when folder exists', async () => {
      const client = createMockGraphClient();
      client.get.mockResolvedValue({});

      const result = await service.rootFolderExists(client, 'TestFolder');

      expect(client.api).toHaveBeenCalledWith('/me/drive/root:/TestFolder');
      expect(result).toBe(true);
    });

    it('should return false when folder does not exist', async () => {
      const client = createMockGraphClient();
      client.get.mockRejectedValue(new Error('Not found'));

      const result = await service.rootFolderExists(client, 'Missing');

      expect(result).toBe(false);
    });
  });

  describe('createFolder', () => {
    it('should create a folder and return the result value', async () => {
      const client = createMockGraphClient();
      const driveItem = { id: '123', name: 'NewFolder' };
      client.post.mockResolvedValue({ value: driveItem });

      const result = await service.createFolder(client, 'NewFolder');

      expect(client.api).toHaveBeenCalledWith('/me/drive/root/children');
      expect(client.post).toHaveBeenCalledWith({
        name: 'NewFolder',
        folder: {},
        '@microsoft.graph.conflictBehavior': 'fail',
      });
      expect(result).toEqual(driveItem);
    });
  });

  describe('getFiles', () => {
    it('should return files in a folder', async () => {
      const client = createMockGraphClient();
      const files = [{ id: '1', name: 'file.txt' }];
      client.get.mockResolvedValue({ value: files });

      const result = await service.getFiles(client, 'MyFolder');

      expect(client.api).toHaveBeenCalledWith(
        '/me/drive/root:/MyFolder:/children'
      );
      expect(result).toEqual(files);
    });
  });

  describe('getFile', () => {
    it('should return a single file', async () => {
      const client = createMockGraphClient();
      const file = { id: '1', name: 'doc.pdf' };
      client.get.mockResolvedValue(file);

      const result = await service.getFile(client, 'Folder', 'doc.pdf');

      expect(client.api).toHaveBeenCalledWith('/me/drive/root:/Folder/doc.pdf');
      expect(result).toEqual(file);
    });
  });

  describe('deleteFile', () => {
    it('should delete a file and return the result', async () => {
      const client = createMockGraphClient();
      client.delete.mockResolvedValue(undefined);

      const result = await service.deleteFile(client, 'Folder', 'old.txt');

      expect(client.api).toHaveBeenCalledWith('/me/drive/root:/Folder/old.txt');
      expect(client.delete).toHaveBeenCalled();
      expect(result).toBeUndefined();
    });
  });

  describe('getFileContent', () => {
    it('should fetch file content as a blob', async () => {
      const client = createMockGraphClient();
      const mockBlob = new Blob(['content']);
      const fileData = {
        id: '1',
        '@microsoft.graph.downloadUrl': 'https://example.com/download',
      };
      client.get.mockResolvedValue(fileData);

      const mockFetch = vi.spyOn(globalThis, 'fetch').mockResolvedValue({
        blob: () => Promise.resolve(mockBlob),
      } as Response);

      const result = await service.getFileContent(client, 'Folder', 'file.txt');

      expect(mockFetch).toHaveBeenCalledWith('https://example.com/download');
      expect(result).toBe(mockBlob);

      mockFetch.mockRestore();
    });
  });

  describe('largeFileUpload', () => {
    it('should upload a large file', async () => {
      const client = createMockGraphClient();
      const mockFile = new Blob(['data']);
      const uploadResponse = { responseBody: { id: 'uploaded-id' } };
      const mockUploadTask = {
        upload: vi.fn().mockResolvedValue(uploadResponse),
      };

      const createSpy = vi
        .spyOn(OneDriveLargeFileUploadTask, 'create')
        .mockResolvedValue(mockUploadTask as never);

      const result = await service.largeFileUpload(client, mockFile, 'big.zip');

      expect(createSpy).toHaveBeenCalledWith(client, mockFile, {
        path: '/CIRCABC',
        fileName: 'big.zip',
        rangeSize: 1024 * 1024,
      });
      expect(result).toEqual(uploadResponse);

      createSpy.mockRestore();
    });
  });

  describe('inviteUser', () => {
    it('should invite a user with write permissions', async () => {
      const client = createMockGraphClient();
      const permissionResult = { id: 'perm-1', roles: ['write'] };
      client.post.mockResolvedValue({ value: permissionResult });

      const result = await service.inviteUser(
        client,
        'item-123',
        'user@example.com'
      );

      expect(client.api).toHaveBeenCalledWith(
        '/me/drive/items/item-123/invite'
      );
      expect(client.post).toHaveBeenCalledWith({
        recipients: [{ email: 'user@example.com' }],
        message: "Here's the file that we're collaborating on.",
        requireSignIn: true,
        sendInvitation: true,
        roles: ['write'],
      });
      expect(result).toEqual(permissionResult);
    });
  });
});
