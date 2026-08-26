import { Component, input } from '@angular/core';

@Component({
  selector: 'cbc-notification-message',
  templateUrl: './notification-message.component.html',
  styleUrl: './notification-message.component.scss',
})
export class NotificationMessageComponent {
  public readonly message = input.required<string>();
  public readonly severity = input(0);
  readonly guestAllowed = input<boolean | undefined>(false);

  public isGuestAllowed(): boolean {
    const guestAllowed = this.guestAllowed();
    if (guestAllowed) {
      return guestAllowed;
    }
    return false;
  }
}
