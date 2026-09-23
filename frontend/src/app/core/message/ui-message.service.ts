import { Service } from '@angular/core';
import { UiMessage } from 'app/core/message/ui-message';
import { UiMessageLevel } from 'app/core/message/ui-message-level';
import { Observable, Subject } from 'rxjs';

/**
 * Application-wide service for broadcasting transient UI messages
 * (notifications/toasts) to interested subscribers.
 *
 * Registered as a root-level singleton (`providedIn: 'root'`), this service
 * acts as the central hub of the messaging system: callers push messages via
 * the typed `add*Message` helpers, while UI components (e.g. a notification
 * container) subscribe to {@link UiMessageService.messageAnnounced$} to render
 * them and to {@link UiMessageService.messageDestroyed$} to dismiss them.
 *
 * Messages are represented by {@link UiMessage} and classified by
 * {@link UiMessageLevel}. To avoid flooding the UI, identical messages emitted
 * within a short time window are de-duplicated (see the private
 * `isSameAsLastMessage` guard).
 */
@Service()
export class UiMessageService {
  /** Subject used internally to emit newly announced messages. */
  private readonly messageSource: Subject<UiMessage> = new Subject<UiMessage>();
  /** Subject used internally to emit messages that should be dismissed. */
  private readonly messageDestroySource: Subject<UiMessage> =
    new Subject<UiMessage>();
  /**
   * Stream of messages announced to the application. UI components subscribe to
   * this observable to display each emitted {@link UiMessage}.
   */
  public messageAnnounced$: Observable<UiMessage> =
    this.messageSource.asObservable();
  /**
   * Stream of messages requested to be removed/dismissed. UI components
   * subscribe to this observable to tear down the corresponding notification.
   */
  public messageDestroyed$: Observable<UiMessage> =
    this.messageDestroySource.asObservable();

  /**
   * Tracks the most recently announced message body and the time it was
   * emitted, enabling short-window de-duplication of identical messages.
   */
  private readonly lastMessage = { message: '', time: Date.now() };

  /**
   * Announces an informational message ({@link UiMessageLevel.INFO}).
   *
   * @param message The text to display to the user.
   * @param autoclose When `true`, the notification dismisses itself automatically. Defaults to `false`.
   * @param displayTime Optional duration, in seconds, the message stays visible before auto-closing.
   * @returns Nothing.
   */
  public addInfoMessage(
    message: string,
    autoclose = false,
    displayTime?: number
  ): void {
    this.addMessage(message, UiMessageLevel.INFO, autoclose, displayTime);
  }

  /**
   * Announces an error message ({@link UiMessageLevel.ERROR}).
   *
   * @param message The text to display to the user.
   * @param autoclose When `true`, the notification dismisses itself automatically. Defaults to `false`.
   * @param displayTime Optional duration, in seconds, the message stays visible before auto-closing.
   * @returns Nothing.
   */
  public addErrorMessage(
    message: string,
    autoclose = false,
    displayTime?: number
  ): void {
    this.addMessage(message, UiMessageLevel.ERROR, autoclose, displayTime);
  }

  /**
   * Announces a warning message ({@link UiMessageLevel.WARNING}).
   *
   * @param message The text to display to the user.
   * @param autoclose When `true`, the notification dismisses itself automatically. Defaults to `false`.
   * @param displayTime Optional duration, in seconds, the message stays visible before auto-closing.
   * @returns Nothing.
   */
  public addWarningMessage(
    message: string,
    autoclose = false,
    displayTime?: number
  ): void {
    this.addMessage(message, UiMessageLevel.WARNING, autoclose, displayTime);
  }

  /**
   * Announces a success message ({@link UiMessageLevel.SUCCESS}).
   *
   * @param message The text to display to the user.
   * @param autoclose When `true`, the notification dismisses itself automatically. Defaults to `false`.
   * @param displayTime Optional duration, in seconds, the message stays visible before auto-closing.
   * @returns Nothing.
   */
  public addSuccessMessage(
    message: string,
    autoclose = false,
    displayTime?: number
  ): void {
    this.addMessage(message, UiMessageLevel.SUCCESS, autoclose, displayTime);
  }

  /**
   * Requests dismissal of a previously announced message by emitting it on the
   * {@link UiMessageService.messageDestroyed$} stream.
   *
   * @param message The {@link UiMessage} instance to remove from the UI.
   * @returns Nothing.
   */
  public removeMessage(message: UiMessage): void {
    this.messageDestroySource.next(message);
  }

  /**
   * Determines whether the given message body is a duplicate of the most
   * recently announced one within a 5-second window, and records it as the new
   * "last message" regardless of the outcome.
   *
   * @param message The message body to compare against the last announced message.
   * @returns `true` if the same message was announced less than 5 seconds ago; otherwise `false`.
   */
  private isSameAsLastMessage(message: string): boolean {
    let result = false;
    const time = Date.now();
    if (
      message === this.lastMessage.message &&
      time - this.lastMessage.time < 5000
    ) {
      result = true;
    }
    this.lastMessage.message = message;
    this.lastMessage.time = time;
    return result;
  }

  /**
   * Builds a {@link UiMessage} of the given level and emits it on the
   * {@link UiMessageService.messageAnnounced$} stream, unless it is detected as
   * a duplicate of the most recent message (see `isSameAsLastMessage`).
   *
   * @param message The text to display to the user.
   * @param level The severity {@link UiMessageLevel} classifying the message.
   * @param autoclose When `true`, the notification dismisses itself automatically. Defaults to `false`.
   * @param displayTime Optional duration, in seconds, the message stays visible before auto-closing.
   * @returns Nothing.
   */
  private addMessage(
    message: string,
    level: UiMessageLevel,
    autoclose = false,
    displayTime?: number
  ): void {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    let finalDisplayTime: any;
    if (displayTime !== undefined) {
      finalDisplayTime = displayTime;
    }

    if (!this.isSameAsLastMessage(message)) {
      const uiMessage = new UiMessage(
        level,
        message,
        autoclose,
        finalDisplayTime
      );
      this.messageSource.next(uiMessage);
    }
  }
}
