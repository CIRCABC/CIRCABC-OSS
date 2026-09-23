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
import { Router } from '@angular/router';
import { TranslocoService } from '@jsverse/transloco';
import { unauthInterceptor } from 'app/core/interceptors/unauth.interceptor';
import { LoginService } from 'app/core/login.service';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { environment } from 'environments/environment';
import { vi } from 'vitest';

describe('UnauthInterceptor', () => {
  let httpClient: HttpClient;
  let httpMock: HttpTestingController;
  let mockLoginService: {
    isGuest: ReturnType<typeof vi.fn>;
    cleanAuthentication: ReturnType<typeof vi.fn>;
  };
  let mockRouter: { navigate: ReturnType<typeof vi.fn> };
  let mockTransloco: { translate: ReturnType<typeof vi.fn> };
  let mockUiMessage: { addErrorMessage: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    mockLoginService = { isGuest: vi.fn(), cleanAuthentication: vi.fn() };
    mockRouter = { navigate: vi.fn() };
    mockTransloco = { translate: vi.fn().mockReturnValue('Session expired') };
    mockUiMessage = { addErrorMessage: vi.fn() };

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([unauthInterceptor])),
        provideHttpClientTesting(),
        { provide: LoginService, useValue: mockLoginService },
        { provide: Router, useValue: mockRouter },
        { provide: TranslocoService, useValue: mockTransloco },
        { provide: UiMessageService, useValue: mockUiMessage },
      ],
    });

    httpClient = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should pass through successful requests', () => {
    httpClient.get('/api/test').subscribe();

    const req = httpMock.expectOne('/api/test');
    req.flush({ data: 'ok' });

    expect(mockLoginService.cleanAuthentication).not.toHaveBeenCalled();
  });

  it('should skip interception for ARES bridge requests', () => {
    const origEnabled = environment.aresBridgeEnabled;
    const origServer = environment.aresBridgeServer;
    (environment as { aresBridgeEnabled: boolean }).aresBridgeEnabled = true;
    (environment as { aresBridgeServer: string }).aresBridgeServer =
      'https://ares.test.eu';

    httpClient.get('https://ares.test.eu/some/path').subscribe({
      error: () => {
        /* expected */
      },
    });

    const req = httpMock.expectOne('https://ares.test.eu/some/path');
    req.flush('', { status: 401, statusText: 'Unauthorized' });

    expect(mockLoginService.cleanAuthentication).not.toHaveBeenCalled();

    (environment as { aresBridgeEnabled: boolean }).aresBridgeEnabled =
      origEnabled;
    (environment as { aresBridgeServer: string }).aresBridgeServer = origServer;
  });

  it('should handle 401 error for non-guest user', () => {
    mockLoginService.isGuest.mockReturnValue(false);

    httpClient.get('/api/test').subscribe({
      error: () => {
        /* expected */
      },
    });

    const req = httpMock.expectOne('/api/test');
    req.flush('', { status: 401, statusText: 'Unauthorized' });

    expect(mockLoginService.cleanAuthentication).toHaveBeenCalled();
    expect(mockRouter.navigate).toHaveBeenCalledWith(['welcome']);
    expect(mockTransloco.translate).toHaveBeenCalledWith(
      'error.session.expired'
    );
    expect(mockUiMessage.addErrorMessage).toHaveBeenCalledWith(
      'Session expired',
      true
    );
  });

  it('should not handle 401 error for guest user', () => {
    mockLoginService.isGuest.mockReturnValue(true);

    httpClient.get('/api/test').subscribe({
      error: () => {
        /* expected */
      },
    });

    const req = httpMock.expectOne('/api/test');
    req.flush('', { status: 401, statusText: 'Unauthorized' });

    expect(mockLoginService.cleanAuthentication).not.toHaveBeenCalled();
    expect(mockRouter.navigate).not.toHaveBeenCalled();
  });

  it('should not handle non-401 errors', () => {
    httpClient.get('/api/test').subscribe({
      error: () => {
        /* expected */
      },
    });

    const req = httpMock.expectOne('/api/test');
    req.flush('', { status: 500, statusText: 'Server Error' });

    expect(mockLoginService.cleanAuthentication).not.toHaveBeenCalled();
    expect(mockRouter.navigate).not.toHaveBeenCalled();
  });
});
