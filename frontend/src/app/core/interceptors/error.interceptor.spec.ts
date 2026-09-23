import {
  HttpClient,
  provideHttpClient,
  withInterceptors,
} from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { AnalyticsService } from 'app/core/analytics.service';

import { errorInterceptor } from 'app/core/interceptors/error.interceptor';
import { vi } from 'vitest';

describe('ErrorInterceptor', () => {
  let httpClient: HttpClient;
  let httpMock: HttpTestingController;
  let mockAnalyticsService: { trackHTTPError: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    mockAnalyticsService = { trackHTTPError: vi.fn() };

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([errorInterceptor])),
        provideHttpClientTesting(),
        { provide: AnalyticsService, useValue: mockAnalyticsService },
      ],
    });

    httpClient = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should pass through successful requests without tracking', () => {
    httpClient.get('/api/data').subscribe();

    const req = httpMock.expectOne('/api/data');
    req.flush({ success: true });

    expect(mockAnalyticsService.trackHTTPError).not.toHaveBeenCalled();
  });

  it('should call trackHTTPError on server error', () => {
    httpClient.get('/api/data').subscribe({ error: () => {} });

    const req = httpMock.expectOne('/api/data');
    req.flush('error', { status: 500, statusText: 'Server Error' });

    expect(mockAnalyticsService.trackHTTPError).toHaveBeenCalledWith(
      expect.stringContaining('/api/data'),
      'GET',
      500,
      expect.stringContaining('Server Side Error')
    );
  });

  it('should not track errors for /ticket/ URLs', () => {
    httpClient.get('/api/ticket/abc').subscribe({ error: () => {} });

    const req = httpMock.expectOne('/api/ticket/abc');
    req.flush('error', { status: 401, statusText: 'Unauthorized' });

    expect(mockAnalyticsService.trackHTTPError).not.toHaveBeenCalled();
  });

  it('should not track 401 errors for /users/ URLs', () => {
    httpClient.get('/api/users/123').subscribe({ error: () => {} });

    const req = httpMock.expectOne('/api/users/123');
    req.flush('error', { status: 401, statusText: 'Unauthorized' });

    expect(mockAnalyticsService.trackHTTPError).not.toHaveBeenCalled();
  });

  it('should track non-401 errors for /users/ URLs', () => {
    httpClient.get('/api/users/123').subscribe({ error: () => {} });

    const req = httpMock.expectOne('/api/users/123');
    req.flush('error', { status: 500, statusText: 'Server Error' });

    expect(mockAnalyticsService.trackHTTPError).toHaveBeenCalledWith(
      expect.stringContaining('/api/users/123'),
      'GET',
      500,
      expect.stringContaining('Server Side Error')
    );
  });

  it('should re-throw the error to the subscriber', () => {
    let errorReceived = false;

    httpClient.get('/api/data').subscribe({
      error: () => {
        errorReceived = true;
      },
    });

    const req = httpMock.expectOne('/api/data');
    req.flush('error', { status: 404, statusText: 'Not Found' });

    expect(errorReceived).toBe(true);
  });
});
