import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { DownloadService } from 'app/core/download.service';
import { DownloadUtilService } from 'app/shared/services/download-util.service';
import { vi } from 'vitest';

describe('DownloadService', () => {
  let service: DownloadService;
  let httpTesting: HttpTestingController;
  const mockDownloadUtil = {
    getDownloadUrl: vi.fn(),
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: DownloadUtilService, useValue: mockDownloadUtil },
      ],
    });
    service = TestBed.inject(DownloadService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should be created', () => {
    expect(service).toBeDefined();
  });

  it('should call getDownloadUrl and return blob content', async () => {
    const nodeId = 'abc-123';
    const fakeUrl = '/api/download/abc-123';
    const fakeBlob = new Blob(['file content'], {
      type: 'application/octet-stream',
    });

    mockDownloadUtil.getDownloadUrl.mockReturnValue(fakeUrl);

    const promise = service.getNodeContent(nodeId);

    const req = httpTesting.expectOne(fakeUrl);
    expect(req.request.method).toBe('GET');
    expect(req.request.responseType).toBe('blob');
    req.flush(fakeBlob);

    const result = await promise;
    expect(result).toBeInstanceOf(Blob);
    expect(mockDownloadUtil.getDownloadUrl).toHaveBeenCalledWith(nodeId);
  });

  it('should reject when the HTTP request fails', async () => {
    const nodeId = 'fail-node';
    const fakeUrl = '/api/download/fail-node';

    mockDownloadUtil.getDownloadUrl.mockReturnValue(fakeUrl);

    const promise = service.getNodeContent(nodeId);

    const req = httpTesting.expectOne(fakeUrl);
    req.error(new ProgressEvent('error'), {
      status: 404,
      statusText: 'Not Found',
    });

    await expect(promise).rejects.toBeDefined();
  });
});
