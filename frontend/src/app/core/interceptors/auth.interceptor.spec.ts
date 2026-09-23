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
import { authInterceptor } from 'app/core/interceptors/auth.interceptor';
import { LoginService } from 'app/core/login.service';
import { environment } from 'environments/environment';
import { vi } from 'vitest';

describe('AuthInterceptor', () => {
  let httpClient: HttpClient;
  let httpMock: HttpTestingController;
  let mockLoginService: {
    isGuest: ReturnType<typeof vi.fn>;
    getTicket: ReturnType<typeof vi.fn>;
  };

  beforeEach(() => {
    mockLoginService = {
      isGuest: vi.fn(),
      getTicket: vi.fn(),
    };

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        { provide: LoginService, useValue: mockLoginService },
      ],
    });

    httpClient = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should add Authorization header when user is not guest and not a translation request', () => {
    mockLoginService.isGuest.mockReturnValue(false);
    mockLoginService.getTicket.mockReturnValue('TICKET_abc123');

    httpClient.get('/api/data').subscribe();

    const req = httpMock.expectOne('/api/data');
    expect(req.request.headers.get('Authorization')).toBe(
      `Basic ${btoa('TICKET_abc123')}`
    );
    req.flush({});
  });

  it('should add guest param when user is guest', () => {
    mockLoginService.isGuest.mockReturnValue(true);
    mockLoginService.getTicket.mockReturnValue('');

    httpClient.get('/api/data').subscribe();

    const req = httpMock.expectOne((r) => r.url === '/api/data');
    expect(req.request.params.get('guest')).toBe('true');
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush({});
  });

  it('should pass through translation requests without auth', () => {
    mockLoginService.isGuest.mockReturnValue(false);
    mockLoginService.getTicket.mockReturnValue('TICKET_abc123');

    httpClient.get('/assets/i18n/en.json').subscribe();

    const req = httpMock.expectOne('/assets/i18n/en.json');
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush({});
  });

  it('should pass through ARES bridge requests without auth', () => {
    const origEnabled = environment.aresBridgeEnabled;
    const origServer = environment.aresBridgeServer;
    (environment as { aresBridgeEnabled: boolean }).aresBridgeEnabled = true;
    (environment as { aresBridgeServer: string }).aresBridgeServer =
      'https://ares.test.eu';

    mockLoginService.isGuest.mockReturnValue(false);
    mockLoginService.getTicket.mockReturnValue('TICKET_abc123');

    httpClient.get('https://ares.test.eu/something').subscribe();

    const req = httpMock.expectOne('https://ares.test.eu/something');
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush({});

    (environment as { aresBridgeEnabled: boolean }).aresBridgeEnabled =
      origEnabled;
    (environment as { aresBridgeServer: string }).aresBridgeServer = origServer;
  });

  it('should pass through captcha requests without auth', () => {
    mockLoginService.isGuest.mockReturnValue(false);
    mockLoginService.getTicket.mockReturnValue('TICKET_abc123');

    httpClient.get('/api/captchaImg/123').subscribe();

    const req = httpMock.expectOne('/api/captchaImg/123');
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush({});
  });
});
