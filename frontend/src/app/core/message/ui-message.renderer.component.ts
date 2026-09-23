import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  OnChanges,
  SimpleChanges,
} from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { UiMessage } from 'app/core/message/ui-message';
import { UiMessageService } from 'app/core/message/ui-message.service';

/**
 * Renders a single {@link UiMessage} notification banner (info, warning,
 * error or success) in the UI.
 *
 * The component displays the message body together with a level-specific
 * icon and a close control. When the bound message is configured to
 * auto-close, it schedules its own removal from the {@link UiMessageService}
 * after the configured display time. Manual dismissal is delegated to the
 * same service so the message is removed from the shared message store.
 *
 * Uses OnPush change detection; all template-bound state derives from the
 * reactive `message` input, and integrates with Transloco for translated
 * content and RouterLink for in-message navigation.
 */
@Component({
  selector: 'cbc-ui-message-rendered',
  templateUrl: './ui-message.renderer.component.html',
  styleUrl: './ui-message.renderer.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, TranslocoModule],
})
export class UiMessageRendererComponent implements OnChanges {
  /**
   * Service that owns the shared collection of active UI messages. Used to
   * remove this message when it is dismissed manually or auto-closes.
   */
  private readonly uiMessageService = inject(UiMessageService);

  /**
   * Required input carrying the message to render, including its level,
   * body text and auto-close configuration.
   */
  public readonly message = input.required<UiMessage>();

  /**
   * Angular lifecycle hook reacting to changes of the {@link message} input.
   *
   * When a new message that has `autoclose` enabled is bound, schedules the
   * message for automatic removal from the {@link UiMessageService}. The
   * delay defaults to 3000ms and, when the message defines a `displayTime`
   * (in seconds), uses that value converted to milliseconds instead.
   *
   * @param changes - The set of input changes provided by Angular; only the
   *   `message` change is handled.
   * @returns Nothing.
   */
  public ngOnChanges(changes: SimpleChanges): void {
    if (changes.message) {
      if (changes.message.currentValue.autoclose) {
        let timeOutIntrevalInMiliSeconds = 3000;

        if (changes.message.currentValue.displayTime !== undefined) {
          timeOutIntrevalInMiliSeconds =
            changes.message.currentValue.displayTime * 1000;
        }

        setTimeout(() => {
          this.uiMessageService.removeMessage(this.message());
        }, timeOutIntrevalInMiliSeconds);
      }
    }
  }

  /**
   * Dismisses the message manually. Marks it inactive and removes it from
   * the {@link UiMessageService}.
   *
   * @returns Nothing.
   */
  public closeMessage(): void {
    const message = this.message();
    message.active = false;
    this.uiMessageService.removeMessage(message);
  }

  /**
   * Relative asset path of the icon used for informational messages.
   *
   * @returns The image URL for the info icon.
   */
  get imageInfoLink(): string {
    return 'img/info-signs.png';
  }

  /**
   * Relative asset path of the icon used for warning messages.
   *
   * @returns The image URL for the exclamation icon.
   */
  get imageExclamationLink(): string {
    return 'img/exclamation.png';
  }

  /**
   * Relative asset path of the icon used for error messages.
   *
   * @returns The image URL for the error icon.
   */
  get imageErrorLink(): string {
    return 'img/error-sign.png';
  }

  /**
   * Relative asset path of the icon used for success messages.
   *
   * @returns The image URL for the check-mark icon.
   */
  get imageCheckMarkLink(): string {
    return 'img/check-mark.png';
  }
}
