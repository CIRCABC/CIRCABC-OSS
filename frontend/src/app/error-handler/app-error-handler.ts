import { ErrorHandler, Injector, inject, Service } from '@angular/core';
import { AnalyticsService } from 'app/core/analytics.service';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { environment } from 'environments/environment';

/**
 * Global error handler for the CIRCABC application.
 *
 * Registered as Angular's {@link ErrorHandler}, this service intercepts every
 * uncaught error in the application. In addition to the default framework
 * behavior (logging to the console via `super.handleError`), it:
 * - surfaces the error stack trace to the user via the {@link UiMessageService}
 *   when running outside of production and acceptance environments, to aid
 *   local debugging;
 * - reports the error to the {@link AnalyticsService} so failures can be
 *   tracked centrally.
 *
 * Collaborators ({@link UiMessageService}, {@link AnalyticsService}) are
 * resolved lazily through the {@link Injector} to avoid circular dependency
 * issues that can arise when an `ErrorHandler` is constructed early in the
 * Angular bootstrap lifecycle.
 */
@Service({ autoProvided: false })
export class AppErrorHandler extends ErrorHandler {
  /**
   * Angular {@link Injector} used to lazily resolve collaborating services
   * ({@link UiMessageService} and {@link AnalyticsService}) at the moment an
   * error is handled, rather than at construction time.
   */
  private readonly injector = inject(Injector);

  /**
   * Handles an uncaught application error.
   *
   * Delegates to the base {@link ErrorHandler} first (which logs the error to
   * the console), then optionally displays the stack trace to the user in
   * non-production/non-acceptance environments, and finally forwards the error
   * to the analytics tracking service when a stack trace is available.
   *
   * @param error - The uncaught error to handle.
   */
  override handleError(error: Error) {
    super.handleError(error);

    if (
      environment.environmentType !== 'prod' &&
      environment.environmentType !== 'acc'
    ) {
      const uiMessageService: UiMessageService =
        this.injector.get<UiMessageService>(UiMessageService);
      if (error.stack) {
        uiMessageService.addErrorMessage(error.stack, false);
      }
    }

    if (error.stack) {
      const analyticsService: AnalyticsService =
        this.injector.get<AnalyticsService>(AnalyticsService);
      analyticsService.trackError(error);
    }
  }
}
