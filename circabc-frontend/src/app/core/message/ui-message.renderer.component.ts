import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { UiMessage } from 'app/core/message/ui-message';
import { UiMessageService } from 'app/core/message/ui-message.service';

@Component({
  selector: 'cbc-ui-message-rendered',
  templateUrl: './ui-message.renderer.component.html',
  styleUrls: ['./ui-message.renderer.component.scss'],
  preserveWhitespaces: true,
})
export class UiMessageRendererComponent implements OnChanges {
  @Input()
  public message!: UiMessage;

  public constructor(private uiMessageService: UiMessageService) {}

  public ngOnChanges(changes: SimpleChanges): void {
    if (changes.message) {
      if (changes.message.currentValue.autoclose) {
        let timeOutIntrevalInMiliSeconds = 3000;

        if (changes.message.currentValue.displayTime !== undefined) {
          timeOutIntrevalInMiliSeconds =
            changes.message.currentValue.displayTime * 1000;
        }

        setTimeout(() => {
          this.uiMessageService.removeMessage(this.message);
        }, timeOutIntrevalInMiliSeconds);
      }
    }
  }

  public closeMessage(): void {
    this.message.active = false;
    this.uiMessageService.removeMessage(this.message);
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
