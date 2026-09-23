import {
  HttpClient,
  HttpResponse,
  provideHttpClient,
  withInterceptors,
} from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { cacheInterceptor } from 'app/core/interceptors/cache.interceptor';
import { RequestCache } from 'app/core/interceptors/request-cache.service';
import { vi } from 'vitest';

describe('CacheInterceptor', () => {
  let httpClient: HttpClient;
  let httpMock: HttpTestingController;
  let mockCache: {
    get: ReturnType<typeof vi.fn>;
    put: ReturnType<typeof vi.fn>;
  };

  beforeEach(() => {
    mockCache = { get: vi.fn(), put: vi.fn() };

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([cacheInterceptor])),
        provideHttpClientTesting(),
        { provide: RequestCache, useValue: mockCache },
      ],
    });

    httpClient = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should pass through non-cachable requests without checking cache', () => {
    mockCache.get.mockReturnValue(undefined);

    httpClient.get('/api/other').subscribe();

    const req = httpMock.expectOne('/api/other');
    expect(mockCache.get).not.toHaveBeenCalled();
    req.flush({});
  });

  it('should not cache POST requests even if URL matches pattern', () => {
    httpClient.post('/service/circabc/users/abc1234', {}).subscribe();

    const req = httpMock.expectOne('/service/circabc/users/abc1234');
    expect(mockCache.get).not.toHaveBeenCalled();
    req.flush({});
  });

  it('should return cached response for cachable GET request', () => {
    const cachedBody = { name: 'cached-user' };
    mockCache.get.mockReturnValue(
      new HttpResponse({ status: 200, body: cachedBody })
    );

    let result: unknown;
    httpClient.get('/service/circabc/users/abc1234').subscribe((r) => {
      result = r;
    });

    // No request should be made since cache returned a value
    httpMock.expectNone('/service/circabc/users/abc1234');
    expect(result).toEqual(cachedBody);
  });

  it('should fetch from server and cache response when no cache exists', () => {
    mockCache.get.mockReturnValue(undefined);

    httpClient.get('/service/circabc/users/abc1234').subscribe();

    const req = httpMock.expectOne('/service/circabc/users/abc1234');
    req.flush({ name: 'fresh-user' });

    expect(mockCache.put).toHaveBeenCalled();
  });

  it('should refresh when x-refresh header is set and no cache exists', () => {
    mockCache.get.mockReturnValue(undefined);

    httpClient
      .get('/service/circabc/users/abc1234', {
        headers: { 'x-refresh': 'true' },
      })
      .subscribe();

    const req = httpMock.expectOne('/service/circabc/users/abc1234');
    expect(req.request.headers.has('x-refresh')).toBe(false);
    req.flush({ name: 'refreshed' });

    expect(mockCache.put).toHaveBeenCalled();
  });
});
