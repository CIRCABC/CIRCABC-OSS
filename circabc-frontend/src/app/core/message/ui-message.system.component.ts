import { Component, OnDestroy } from '@angular/core';
import { UiMessage } from 'app/core/message/ui-message';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'cbc-ui-message-system',
  templateUrl: './ui-message.system.component.html',
  styleUrls: ['./ui-message.system.component.scss'],
  preserveWhitespaces: true,
})
export class UiMessageSystemComponent implements OnDestroy {
  public messages: UiMessage[] = [];
  private messageAnnouncedSubscription$: Subscription;
  private messageDestroyedSubscription$: Subscription;

  public constructor(private uiMessageService: UiMessageService) {
    this.messageAnnouncedSubscription$ =
      this.uiMessageService.messageAnnounced$.subscribe(
        (message: UiMessage) => {
          this.messages.push(message);
        }
      );

    this.messageDestroyedSubscription$ =
      this.uiMessageService.messageDestroyed$.subscribe(
        (message: UiMessage) => {
          const i = this.messages.indexOf(message);
          this.messages.splice(i, 1);
        }
      );
  }
  public ngOnDestroy(): void {
    this.messageAnnouncedSubscription$.unsubscribe();
    this.messageDestroyedSubscription$.unsubscribe();
  }
}
