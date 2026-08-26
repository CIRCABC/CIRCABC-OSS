import {
  Component,
  OnChanges,
  SimpleChanges,
  input,
  signal,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { UiMessage } from 'app/core/message/ui-message';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { MessageSuppressionService } from 'app/core/message/message-suppression.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'cbc-ui-message-rendered',
  templateUrl: './ui-message.renderer.component.html',
  styleUrl: './ui-message.renderer.component.scss',
  preserveWhitespaces: true,
  imports: [RouterLink, TranslocoModule, FormsModule, CommonModule],
})
export class UiMessageRendererComponent implements OnChanges {
  public readonly message = input.required<UiMessage>();
  public readonly doNotShowAgain = signal<boolean>(false);
  public readonly shouldDisplay = signal<boolean>(true);

  public constructor(
    private uiMessageService: UiMessageService,
    private suppressionService: MessageSuppressionService
  ) {}

  public ngOnChanges(changes: SimpleChanges): void {
    if (changes.message) {
      const currentMessage = changes.message.currentValue;

      // Check suppression before any rendering
      if (
        currentMessage.id &&
        this.suppressionService.isSuppressed(
          currentMessage.id,
          currentMessage.body
        )
      ) {
        this.shouldDisplay.set(false);
        return;
      }

      this.shouldDisplay.set(true);

      // Existing autoclose logic
      if (currentMessage.autoclose) {
        let timeOutIntrevalInMiliSeconds = 3000;

        if (currentMessage.displayTime !== undefined) {
          timeOutIntrevalInMiliSeconds = currentMessage.displayTime * 1000;
        }

        setTimeout(() => {
          this.uiMessageService.removeMessage(this.message());
        }, timeOutIntrevalInMiliSeconds);
      }
    }
  }

  public closeMessage(): void {
    const message = this.message();

    // Handle suppression if checkbox was selected
    if (message.id && this.doNotShowAgain()) {
      this.suppressionService.suppress(message.id, message.body);
    }

    message.active = false;
    this.uiMessageService.removeMessage(message);
  }

  get showCheckbox(): boolean {
    return this.message().id !== undefined;
  }

  get imageInfoLink(): string {
    return 'img/info-signs.png';
  }

  get imageExclamationLink(): string {
    return 'img/exclamation.png';
  }

  get imageErrorLink(): string {
    return 'img/error-sign.png';
  }

  get imageCheckMarkLink(): string {
    return 'img/check-mark.png';
  }
}
