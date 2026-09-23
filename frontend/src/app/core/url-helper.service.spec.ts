import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { UrlHelperService } from 'app/core/url-helper.service';
import { vi } from 'vitest';

describe('UrlHelperService', () => {
  let service: UrlHelperService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(UrlHelperService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should be created', () => {
    expect(service).toBeDefined();
  });

  it('should fetch a blob and return an object URL', () => {
    const fakeObjectUrl = 'blob:http://localhost/fake';
    vi.spyOn(URL, 'createObjectURL').mockReturnValue(fakeObjectUrl);

    let result: string | undefined;
    service.get('/api/image').subscribe((url) => {
      result = url;
    });

    const req = httpTesting.expectOne('/api/image');
    expect(req.request.responseType).toBe('blob');
    req.flush(new Blob(['data']));

    expect(result).toBe(fakeObjectUrl);
  });

  it('should return cached URL on subsequent calls', () => {
    const fakeObjectUrl = 'blob:http://localhost/cached';
    vi.spyOn(URL, 'createObjectURL').mockReturnValue(fakeObjectUrl);

    // First call — populates cache
    service.get('/api/image').subscribe();
    httpTesting.expectOne('/api/image').flush(new Blob(['data']));

    // Second call — should use cache, no HTTP request
    let result: string | undefined;
    service.get('/api/image').subscribe((url) => {
      result = url;
    });

    httpTesting.expectNone('/api/image');
    expect(result).toBe(fakeObjectUrl);
  });

  it('should return teardown that revokes URL when cache exceeds 64 entries', () => {
    const revokespy = vi
      .spyOn(URL, 'revokeObjectURL')
      .mockImplementation(() => {});
    vi.spyOn(URL, 'createObjectURL').mockReturnValue('blob:fake');

    // Fill cache with 65 entries so size > 64 when next observable is created
    for (let i = 0; i < 65; i++) {
      service.get(`/api/img/${i}`).subscribe();
      httpTesting.expectOne(`/api/img/${i}`).flush(new Blob());
    }

    // This call sees cache size > 64, so teardown is returned
    const sub = service.get('/api/img/65').subscribe();
    httpTesting.expectOne('/api/img/65').flush(new Blob());

    sub.unsubscribe();
    expect(revokespy).toHaveBeenCalledWith('blob:fake');
  });
});
