import { TestBed } from '@angular/core/testing';
import { LoginService } from 'app/core/login.service';
import { UploadService } from 'app/core/upload.service';
import { CBC_BASE_PATH, SERVER_URL } from 'app/core/variables';
import { vi } from 'vitest';

describe('UploadService', () => {
  let service: UploadService;
  let mockLoginService: { getTicket: ReturnType<typeof vi.fn> };
  let xhrInstances: MockXHR[];

  class MockXHR {
    open = vi.fn();
    setRequestHeader = vi.fn();
    send = vi.fn();
    onreadystatechange: (() => void) | null = null;
    readyState = 0;
    status = 0;
    statusText = '';
    response = '';

    constructor() {
      xhrInstances.push(this);
    }

    triggerSuccess(responseBody: object) {
      this.readyState = 4;
      this.status = 200;
      this.response = JSON.stringify(responseBody);
      this.onreadystatechange?.();
    }

    triggerError(statusText: string) {
      this.readyState = 4;
      this.status = 500;
      this.statusText = statusText;
      this.onreadystatechange?.();
    }
  }

  beforeEach(() => {
    xhrInstances = [];
    mockLoginService = { getTicket: vi.fn().mockReturnValue('test-ticket') };

    vi.stubGlobal('XMLHttpRequest', MockXHR);

    TestBed.configureTestingModule({
      providers: [
        UploadService,
        { provide: LoginService, useValue: mockLoginService },
        { provide: CBC_BASE_PATH, useValue: 'http://circabc-api' },
        { provide: SERVER_URL, useValue: 'http://server/' },
      ],
    });

    service = TestBed.inject(UploadService);
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('should be created', () => {
    expect(service).toBeDefined();
  });

  describe('uploadNewFile', () => {
    it('should POST to the correct URL and return the node ID', async () => {
      const file = new File(['content'], 'test.txt');
      const promise = service.uploadNewFile(file, 'folder-id');

      const xhr = xhrInstances[0];
      xhr.triggerSuccess({ nodeRef: 'workspace://SpacesStore/abc-123' });

      const result = await promise;
      expect(xhr.open).toHaveBeenCalledWith(
        'POST',
        'http://server/rest/upload/folder-id'
      );
      expect(result).toBe('abc-123');
    });

    it('should reject when the server returns an error', async () => {
      const file = new File(['content'], 'test.txt');
      const promise = service.uploadNewFile(file, 'folder-id');

      const xhr = xhrInstances[0];
      xhr.triggerError('Internal Server Error');

      await expect(promise).rejects.toThrow('Internal Server Error');
    });
  });

  describe('updateExistingFileContent', () => {
    it('should POST to the update URL with notify and generateNewFileName params', async () => {
      const file = new File(['content'], 'test.txt');
      const promise = service.updateExistingFileContent(file, 'node-456', true);

      const xhr = xhrInstances[0];
      xhr.triggerSuccess({ nodeRef: 'workspace://SpacesStore/node-456' });

      const result = await promise;
      expect(xhr.open).toHaveBeenCalledWith(
        'POST',
        'http://server/rest/update/node-456?notify=true&generateNewFileName=true'
      );
      expect(result).toBe('node-456');
    });
  });

  describe('updateCheckedOutFileContent', () => {
    it('should POST to the circabc content update URL', async () => {
      const file = new File(['content'], 'test.txt');
      const promise = service.updateCheckedOutFileContent(
        file,
        'node-789',
        false
      );

      const xhr = xhrInstances[0];
      xhr.triggerSuccess({});

      await promise;
      expect(xhr.open).toHaveBeenCalledWith(
        'POST',
        'http://circabc-api/content/node-789/update?notify=false'
      );
    });
  });

  describe('updateAvatar', () => {
    it('should POST to the avatar URL', async () => {
      const file = new File(['img'], 'avatar.png');
      const promise = service.updateAvatar('user-1', file);

      const xhr = xhrInstances[0];
      xhr.triggerSuccess({});

      await promise;
      expect(xhr.open).toHaveBeenCalledWith(
        'POST',
        'http://circabc-api/users/user-1/avatar'
      );
    });
  });

  describe('sendRequest', () => {
    it('should set Authorization header with Base64-encoded ticket', async () => {
      const file = new File(['content'], 'test.txt');
      const promise = service.uploadNewFile(file, 'folder-id');

      const xhr = xhrInstances[0];
      xhr.triggerSuccess({ nodeRef: 'workspace://SpacesStore/x' });

      await promise;
      expect(xhr.setRequestHeader).toHaveBeenCalledWith(
        'Authorization',
        `Basic ${btoa('test-ticket')}`
      );
      expect(xhr.setRequestHeader).toHaveBeenCalledWith(
        'X-Requested-With',
        'XMLHttpRequest'
      );
    });

    it('should append file to FormData and send it', async () => {
      const file = new File(['content'], 'test.txt');
      const promise = service.uploadNewFile(file, 'folder-id');

      const xhr = xhrInstances[0];
      xhr.triggerSuccess({ nodeRef: 'workspace://SpacesStore/x' });

      await promise;
      expect(xhr.send).toHaveBeenCalled();
      const formData = xhr.send.mock.calls[0][0] as FormData;
      expect(formData.get('filedata')).toBe(file);
    });
  });
});
