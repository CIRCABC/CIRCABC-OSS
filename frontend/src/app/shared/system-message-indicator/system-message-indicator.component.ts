import {
  ChangeDetectionStrategy,
  Component,
  effect,
  inject,
  input,
  NgZone,
  OnDestroy,
  resource,
} from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';
import { AppMessage, AppMessageService } from 'app/core/generated/circabc';
import { UiMessageService } from 'app/core/message/ui-message.service';

/**
 * Indicator component that surfaces platform-wide system/app messages to the
 * user.
 *
 * It renders a clickable indicator (an icon whose style depends on
 * {@link SystemMessageIndicatorComponent.useBlueIcon}) that is shown only when
 * there are currently active messages. The component periodically polls the
 * backend ({@link AppMessageService.getEnabledAppMessages}) every five minutes
 * to keep its list of enabled messages fresh, and displays the messages as
 * UI toasts through the {@link UiMessageService}.
 *
 * On first load the messages are shown automatically (once per browser, tracked
 * via the `systemMessageAlreadyShown` flag in `localStorage`); afterwards they
 * can be re-displayed on demand via
 * {@link SystemMessageIndicatorComponent.forceDisplayMessages}.
 *
 * Key collaborators:
 * - {@link AppMessageService} — fetches the enabled app messages from the API.
 * - {@link UiMessageService} — renders the messages as info/error/warning/success toasts.
 * - {@link NgZone} — used to run the polling interval outside Angular to avoid
 *   unnecessary change-detection cycles.
 */
@Component({
  selector: 'cbc-system-message-indicator',
  templateUrl: './system-message-indicator.component.html',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [TranslocoModule],
})
export class SystemMessageIndicatorComponent implements OnDestroy {
  /** Angular zone used to run the polling interval outside change detection. */
  private readonly zone = inject(NgZone);
  /** API service that provides the list of enabled application messages. */
  private readonly appMessageService = inject(AppMessageService);
  /** UI service used to display the messages as toast notifications. */
  private readonly uiMessageService = inject(UiMessageService);

  /**
   * Signal input controlling the indicator's appearance. When `true` the
   * indicator renders using the blue icon variant; otherwise the default icon
   * is used. Defaults to `false`.
   */
  readonly useBlueIcon = input(false);

  /**
   * Resource fetching the currently enabled application messages from the
   * backend. Reloaded every five minutes via the polling {@link interval}.
   */
  private readonly messagesResource = resource({
    loader: async () => {
      try {
        return await this.appMessageService.getEnabledAppMessagesAsync();
      } catch (e) {
        console.error(e);
        return [];
      }
    },
    defaultValue: [] as AppMessage[],
  });

  /** The list of application messages currently fetched from the backend. */
  public readonly listOfMessages = this.messagesResource.value;

  /**
   * Handle for the recurring polling timer that refreshes the messages.
   * Typed as `any` because the return type differs between browser and Node.
   */
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  private interval: any;

  /**
   * Starts a recurring timer, running outside the Angular zone, that refreshes
   * the list of messages every five minutes. Also displays the initial set of
   * messages automatically the first time they are loaded.
   */
  constructor() {
    this.zone.runOutsideAngular(() => {
      const fiveMinutesInMiliSeconds = 300000;
      this.interval = globalThis.setInterval(() => {
        this.messagesResource.reload();
      }, fiveMinutesInMiliSeconds);
    });

    let firstLoadHandled = false;
    effect(() => {
      if (this.messagesResource.isLoading() || firstLoadHandled) return;
      firstLoadHandled = true;
      this.displayMessages(true);
    });
  }

  /**
   * Lifecycle hook. Clears the recurring polling timer to avoid leaks.
   */
  ngOnDestroy(): void {
    clearInterval(this.interval);
  }

  /**
   * Determines whether there is at least one active message that should be
   * indicated to the user (enabled and whose auto-close date has not passed).
   *
   * @returns `true` if there is at least one active message, otherwise `false`.
   */
  public hasMessages(): boolean {
    const messages = this.listOfMessages();
    if (messages !== undefined && messages.length > 0) {
      for (const message of messages) {
        if (message.enabled && !this.isAutoCloseDatePassed(message)) {
          return true;
        }
      }
    }

    return false;
  }

  /**
   * Checks whether a message's auto-close (closure) date is in the past.
   *
   * @param message The message whose closure date is evaluated.
   * @returns `true` if the message has a closure date that has already passed,
   * otherwise `false`.
   */
  private isAutoCloseDatePassed(message: AppMessage): boolean {
    if (message?.dateClosure) {
      const today = new Date();
      const dateClosure = new Date(message.dateClosure);
      if (today > dateClosure) {
        return true;
      }
    }

    return false;
  }

  /**
   * Displays the messages unless they have already been shown in this browser
   * (tracked via the `systemMessageAlreadyShown` flag in `localStorage`). On the
   * first-time display the flag is persisted so messages are not auto-shown again.
   *
   * @param firstTime Whether this is the automatic first-time display (on load).
   */
  private displayMessages(firstTime: boolean) {
    const systemMessageAlreadyShown = localStorage.getItem(
      'systemMessageAlreadyShown'
    );
    if (systemMessageAlreadyShown !== '1') {
      if (firstTime) {
        localStorage.setItem('systemMessageAlreadyShown', '1');
      }

      this.displayMessagesInternal(firstTime);
    }
  }

  /**
   * Forces the currently active messages to be displayed again, bypassing the
   * first-time `localStorage` guard. Typically triggered by user interaction
   * with the indicator.
   */
  public forceDisplayMessages() {
    this.displayMessagesInternal(false);
  }

  /**
   * Iterates over {@link listOfMessages} and displays each active message
   * (enabled and not past its auto-close date) as a UI toast.
   *
   * @param firstTime Whether this is the automatic first-time display, which
   * affects the display time and whether the toast is persisted.
   */
  private displayMessagesInternal(firstTime: boolean) {
    const messages = this.listOfMessages();
    if (!messages?.length) return;

    for (const message of messages) {
      if (message.enabled && !this.isAutoCloseDatePassed(message)) {
        const displayTime = this.getDisplayTime(message.displayTime, firstTime);
        this.displayMessage(message, firstTime, displayTime);
      }
    }
  }

  /**
   * Computes the display duration (in seconds) for a toast.
   *
   * On first-time display a fixed duration of 30 seconds is used; otherwise the
   * message's own display time is used, clamped to a minimum of 15 seconds and
   * defaulting to 15 when unspecified.
   *
   * @param messageDisplayTime The message's configured display time in seconds, if any.
   * @param firstTime Whether this is the automatic first-time display.
   * @returns The display duration in seconds.
   */
  private getDisplayTime(
    messageDisplayTime: number | undefined,
    firstTime: boolean
  ): number {
    if (firstTime) return 30;
    if (messageDisplayTime !== undefined && messageDisplayTime < 15) return 15;
    return messageDisplayTime ?? 15;
  }

  /**
   * Displays a single message as a UI toast, choosing the toast type from the
   * message's `level` (`info`, `error`, `warning`, or success as the fallback).
   * Messages are persisted (non-auto-dismissing) unless shown for the first time.
   *
   * @param message The message to display.
   * @param firstTime Whether this is the automatic first-time display; controls persistence.
   * @param displayTime The display duration in seconds.
   */
  private displayMessage(
    message: AppMessage,
    firstTime: boolean,
    displayTime: number
  ) {
    const persist = !firstTime;
    const messageMap: {
      [key: string]: (content: string, persist: boolean, time: number) => void;
    } = {
      info: (c, p, t) => this.uiMessageService.addInfoMessage(c, p, t),
      error: (c, p, t) => this.uiMessageService.addErrorMessage(c, p, t),
      warning: (c, p, t) => this.uiMessageService.addWarningMessage(c, p, t),
    };

    const addMessage =
      messageMap[message.level ?? ''] ||
      ((c, p, t) => this.uiMessageService.addSuccessMessage(c, p, t));

    addMessage(message.content ?? '', persist, displayTime);
  }
}
