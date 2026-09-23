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
import { TranslocoService } from '@jsverse/transloco';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { environment } from 'environments/environment';
import { vi } from 'vitest';

import { messageInterceptor } from './message.interceptor';

// A URL that matches a real action pattern (POST to category logos, show: true)
const MATCHING_POST_URL =
  '/categories/12345678-1234-1234-1234-123456789abc/logos';
// A URL that matches a real action pattern (DELETE group logos, show: true)
const MATCHING_DELETE_URL =
  '/groups/12345678-1234-1234-1234-123456789abc/logos';
// A URL that matches a real action pattern (PUT group members, show: true)
const MATCHING_PUT_URL = '/groups/12345678-1234-1234-1234-123456789abc/members';

describe('MessageInterceptor', () => {
  let httpClient: HttpClient;
  let httpMock: HttpTestingController;
  let mockTransloco: { translate: ReturnType<typeof vi.fn> };
  let mockUiMessage: {
    addSuccessMessage: ReturnType<typeof vi.fn>;
    addErrorMessage: ReturnType<typeof vi.fn>;
  };

  beforeEach(() => {
    mockTransloco = { translate: vi.fn((key: string) => key) };
    mockUiMessage = {
      addSuccessMessage: vi.fn(),
      addErrorMessage: vi.fn(),
    };

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([messageInterceptor])),
        provideHttpClientTesting(),
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

  it('should pass through GET requests without showing messages', () => {
    httpClient.get('/api/data').subscribe();

    const req = httpMock.expectOne('/api/data');
    req.flush({});

    expect(mockUiMessage.addSuccessMessage).not.toHaveBeenCalled();
    expect(mockUiMessage.addErrorMessage).not.toHaveBeenCalled();
  });

  it('should pass through POST requests with no matching action type', () => {
    httpClient.post('/api/no-match', {}).subscribe();

    const req = httpMock.expectOne('/api/no-match');
    req.flush({});

    expect(mockUiMessage.addSuccessMessage).not.toHaveBeenCalled();
  });

  it('should show success message on successful POST with matching action type', () => {
    mockTransloco.translate.mockReturnValue('Success!');

    httpClient.post(MATCHING_POST_URL, {}).subscribe();

    const req = httpMock.expectOne(MATCHING_POST_URL);
    req.flush({});

    expect(mockUiMessage.addSuccessMessage).toHaveBeenCalledWith(
      'Success!',
      true
    );
  });

  it('should show default error message when no status-specific translation exists', () => {
    mockTransloco.translate.mockImplementation((key: string) => key);

    httpClient.post(MATCHING_POST_URL, {}).subscribe({ error: () => {} });

    const req = httpMock.expectOne(MATCHING_POST_URL);
    req.flush('error', { status: 500, statusText: 'Server Error' });

    // When translate returns the key unchanged, it falls back to the default error label
    expect(mockUiMessage.addErrorMessage).toHaveBeenCalledWith(
      'category.admin.logo.upload.failed',
      true
    );
  });

  it('should show status-specific error message when translation exists', () => {
    mockTransloco.translate.mockImplementation((key: string) => {
      if (key === 'category.admin.logo.upload.failed.403')
        return 'Forbidden error';
      return key;
    });

    httpClient.post(MATCHING_POST_URL, {}).subscribe({ error: () => {} });

    const req = httpMock.expectOne(MATCHING_POST_URL);
    req.flush('error', { status: 403, statusText: 'Forbidden' });

    expect(mockUiMessage.addErrorMessage).toHaveBeenCalledWith(
      'Forbidden error',
      true
    );
  });

  it('should intercept PUT requests with matching action type', () => {
    mockTransloco.translate.mockReturnValue('Updated!');

    httpClient.put(MATCHING_PUT_URL, {}).subscribe();

    const req = httpMock.expectOne(MATCHING_PUT_URL);
    req.flush({});

    expect(mockUiMessage.addSuccessMessage).toHaveBeenCalledWith(
      'Updated!',
      true
    );
  });

  it('should intercept DELETE requests with matching action type', () => {
    mockTransloco.translate.mockReturnValue('Deleted!');

    httpClient.delete(MATCHING_DELETE_URL).subscribe();

    const req = httpMock.expectOne(MATCHING_DELETE_URL);
    req.flush({});

    expect(mockUiMessage.addSuccessMessage).toHaveBeenCalledWith(
      'Deleted!',
      true
    );
  });

  describe('ARES Bridge requests', () => {
    const origEnabled = environment.aresBridgeEnabled;
    const origServer = environment.aresBridgeServer;

    beforeEach(() => {
      (environment as { aresBridgeEnabled: boolean }).aresBridgeEnabled = true;
      (environment as { aresBridgeServer: string }).aresBridgeServer =
        'https://ares.test.eu';
    });

    afterEach(() => {
      (environment as { aresBridgeEnabled: boolean }).aresBridgeEnabled =
        origEnabled;
      (environment as { aresBridgeServer: string }).aresBridgeServer =
        origServer;
    });

    it('should show 504 error message for ARES bridge timeout', () => {
      mockTransloco.translate.mockReturnValue('ARES not accessible');

      httpClient
        .get('https://ares.test.eu/api/doc')
        .subscribe({ error: () => {} });

      const req = httpMock.expectOne('https://ares.test.eu/api/doc');
      req.flush('', { status: 504, statusText: 'Gateway Timeout' });

      expect(mockTransloco.translate).toHaveBeenCalledWith(
        'error.ares.server.not.accessible'
      );
      expect(mockUiMessage.addErrorMessage).toHaveBeenCalledWith(
        'ARES not accessible',
        false
      );
    });

    it('should show original error for ARES bridge with error id and message', () => {
      mockTransloco.translate.mockReturnValue('ARES error');

      httpClient
        .get('https://ares.test.eu/api/doc')
        .subscribe({ error: () => {} });

      const req = httpMock.expectOne('https://ares.test.eu/api/doc');
      req.flush(
        { id: '123', message: 'Bad request' },
        { status: 400, statusText: 'Bad Request' }
      );

      expect(mockTransloco.translate).toHaveBeenCalledWith(
        'error.ares.original.error',
        { errorId: '123', errorMessage: 'Bad request' }
      );
      expect(mockUiMessage.addErrorMessage).toHaveBeenCalledWith(
        'ARES error',
        false
      );
    });

    it('should not show message for ARES errors without id and message when not 504', () => {
      httpClient
        .get('https://ares.test.eu/api/doc')
        .subscribe({ error: () => {} });

      const req = httpMock.expectOne('https://ares.test.eu/api/doc');
      req.flush('generic error', { status: 400, statusText: 'Bad Request' });

      expect(mockUiMessage.addErrorMessage).not.toHaveBeenCalled();
    });
  });
});
