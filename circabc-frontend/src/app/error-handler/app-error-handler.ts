import { ErrorHandler, Injectable, Injector } from '@angular/core';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { AnalyticsService } from 'app/core//analytics.service';
import { environment } from 'environments/environment';

@Injectable()
export class AppErrorHandler extends ErrorHandler {
  constructor(private injector: Injector) {
    super();
  }

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
