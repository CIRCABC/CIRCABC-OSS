import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  Input,
  NgZone,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { AppMessage, AppMessageService } from 'app/core/generated/circabc';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-system-message-indicator',
  templateUrl: './system-message-indicator.component.html',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SystemMessageIndicatorComponent implements OnInit, OnDestroy {
  @Input()
  useBlueIcon = false;
  public listOfMessages: AppMessage[] = [];
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  private interval: any;

  constructor(
    private zone: NgZone,
    private ref: ChangeDetectorRef,
    private appMessageService: AppMessageService,
    private uiMessageService: UiMessageService
  ) {
    this.zone.runOutsideAngular(() => {
      const fiveMinutesInMiliSeconds = 300000;
      this.interval = window.setInterval(async () => {
        await this.getMessages();
        // eslint-disable-next-line @typescript-eslint/indent
      }, fiveMinutesInMiliSeconds);
    });
  }

  async ngOnInit() {
    await this.getMessages();
    this.displayMessages(true);
  }

  ngOnDestroy(): void {
    clearInterval(this.interval);
  }

  private async getMessages() {
    this.listOfMessages = await firstValueFrom(
      this.appMessageService.getEnabledAppMessages()
    );
    this.ref.markForCheck();
  }

  public hasMessages(): boolean {
    if (this.listOfMessages !== undefined && this.listOfMessages.length > 0) {
      for (const message of this.listOfMessages) {
        if (message.enabled && !this.isAutoCloseDatePassed(message)) {
          return true;
        }
      }
    }

    return false;
  }

  private isAutoCloseDatePassed(message: AppMessage): boolean {
    if (message && message.dateClosure) {
      const today = new Date();
      const dateClosure = new Date(message.dateClosure);
      if (today > dateClosure) {
        return true;
      }
    }

    return false;
  }

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

  public forceDisplayMessages() {
    this.displayMessagesInternal(false);
  }

  private displayMessagesInternal(firstTime: boolean) {
    if (this.listOfMessages !== undefined && this.listOfMessages.length > 0) {
      for (const message of this.listOfMessages) {
        let displayTime = message.displayTime;
        if (firstTime) {
          displayTime = 30;
        } else if (displayTime !== undefined && displayTime < 15) {
          displayTime = 15;
        }
        if (message.enabled && !this.isAutoCloseDatePassed(message)) {
          if (message.level === 'info') {
            this.uiMessageService.addInfoMessage(
              message.content,
              !firstTime,
              displayTime
            );
          } else if (message.level === 'error') {
            this.uiMessageService.addErrorMessage(
              message.content,
              !firstTime,
              displayTime
            );
          } else if (message.level === 'warning') {
            this.uiMessageService.addWarningMessage(
              message.content,
              !firstTime,
              displayTime
            );
          } else {
            this.uiMessageService.addSuccessMessage(
              message.content,
              !firstTime,
              displayTime
            );
          }
        }
      }
    }
  }
}
