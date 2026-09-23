import {
  ChangeDetectionStrategy,
  Component,
  inject,
  OnDestroy,
  signal,
} from '@angular/core';
import { UiMessage } from 'app/core/message/ui-message';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { Subscription } from 'rxjs';
import { UiMessageRendererComponent } from './ui-message.renderer.component';

/**
 * Host component for the application-wide UI message (notification/toast) system.
 *
 * Rendered once near the root of the application, it acts as the container that
 * displays every {@link UiMessage} currently active. It subscribes to the
 * {@link UiMessageService} announce/destroy streams and keeps its local
 * {@link UiMessageSystemComponent.messages} list in sync, delegating the actual
 * rendering of each message to {@link UiMessageRendererComponent}.
 *
 * @remarks
 * Uses OnPush change detection; the signal-backed
 * {@link UiMessageSystemComponent.messages} list marks the view dirty when a
 * message is announced or destroyed so the container re-renders immediately.
 * Subscriptions are cleaned up in
 * {@link UiMessageSystemComponent.ngOnDestroy} to avoid memory leaks.
 */
@Component({
  selector: 'cbc-ui-message-system',
  templateUrl: './ui-message.system.component.html',
  styleUrl: './ui-message.system.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [UiMessageRendererComponent],
})
export class UiMessageSystemComponent implements OnDestroy {
  /** Service that emits UI messages to announce or destroy; the source of truth for notifications. */
  private readonly uiMessageService = inject(UiMessageService);

  /** The list of currently active messages rendered in the template. */
  public readonly messages = signal<UiMessage[]>([]);
  /** Subscription to the service's message-announced stream; unsubscribed on destroy. */
  private readonly messageAnnouncedSubscription$: Subscription;
  /** Subscription to the service's message-destroyed stream; unsubscribed on destroy. */
  private readonly messageDestroyedSubscription$: Subscription;

  /**
   * Wires up subscriptions to the {@link UiMessageService} streams.
   *
   * On each announced message the message is appended to
   * {@link UiMessageSystemComponent.messages}; on each destroyed message the
   * matching entry is removed from the list.
   */
  public constructor() {
    this.messageAnnouncedSubscription$ =
      this.uiMessageService.messageAnnounced$.subscribe(
        (message: UiMessage) => {
          this.messages.set([...this.messages(), message]);
        }
      );

    this.messageDestroyedSubscription$ =
      this.uiMessageService.messageDestroyed$.subscribe(
        (message: UiMessage) => {
          this.messages.set(this.messages().filter((m) => m !== message));
        }
      );
  }
  /**
   * Cleans up both service subscriptions when the component is destroyed to
   * prevent memory leaks.
   */
  public ngOnDestroy(): void {
    this.messageAnnouncedSubscription$.unsubscribe();
    this.messageDestroyedSubscription$.unsubscribe();
  }
}
