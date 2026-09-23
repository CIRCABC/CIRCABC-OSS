import { TestBed } from '@angular/core/testing';
import { AnalyticsService } from 'app/core/analytics.service';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { vi } from 'vitest';

import { AppErrorHandler } from './app-error-handler';

vi.mock('environments/environment', () => ({
  environment: {
    environmentType: 'local',
  },
}));

import { environment } from 'environments/environment';

describe('AppErrorHandler', () => {
  let handler: AppErrorHandler;
  let mockUiMessageService: { addErrorMessage: ReturnType<typeof vi.fn> };
  let mockAnalyticsService: { trackError: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    mockUiMessageService = { addErrorMessage: vi.fn() };
    mockAnalyticsService = { trackError: vi.fn() };

    TestBed.configureTestingModule({
      providers: [
        AppErrorHandler,
        { provide: UiMessageService, useValue: mockUiMessageService },
        { provide: AnalyticsService, useValue: mockAnalyticsService },
      ],
    });

    handler = TestBed.inject(AppErrorHandler);
  });

  afterEach(() => {
    (environment as { environmentType: string }).environmentType = 'local';
  });

  it('should call super.handleError', () => {
    const spy = vi.spyOn(
      Object.getPrototypeOf(Object.getPrototypeOf(handler)),
      'handleError'
    );
    const error = new Error('test');
    handler.handleError(error);
    expect(spy).toHaveBeenCalledWith(error);
    spy.mockRestore();
  });

  it('should show error message in non-prod/acc environments', () => {
    const error = new Error('fail');
    error.stack = 'stack trace';
    handler.handleError(error);

    expect(mockUiMessageService.addErrorMessage).toHaveBeenCalledWith(
      'stack trace',
      false
    );
  });

  it('should not show error message in prod environment', () => {
    (environment as { environmentType: string }).environmentType = 'prod';

    const error = new Error('fail');
    error.stack = 'stack trace';
    handler.handleError(error);

    expect(mockUiMessageService.addErrorMessage).not.toHaveBeenCalled();
  });

  it('should not show error message in acc environment', () => {
    (environment as { environmentType: string }).environmentType = 'acc';

    const error = new Error('fail');
    error.stack = 'stack trace';
    handler.handleError(error);

    expect(mockUiMessageService.addErrorMessage).not.toHaveBeenCalled();
  });

  it('should track error via analytics when stack exists', () => {
    const error = new Error('fail');
    error.stack = 'stack trace';
    handler.handleError(error);

    expect(mockAnalyticsService.trackError).toHaveBeenCalledWith(error);
  });

  it('should not track error via analytics when stack is undefined', () => {
    const error = new Error('fail');
    error.stack = undefined;
    handler.handleError(error);

    expect(mockAnalyticsService.trackError).not.toHaveBeenCalled();
  });

  it('should not show error message when stack is undefined in non-prod', () => {
    const error = new Error('fail');
    error.stack = undefined;
    handler.handleError(error);

    expect(mockUiMessageService.addErrorMessage).not.toHaveBeenCalled();
  });
});
