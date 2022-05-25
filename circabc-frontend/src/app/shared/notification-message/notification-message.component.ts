import { Component, Input } from '@angular/core';

@Component({
  selector: 'cbc-notification-message',
  templateUrl: './notification-message.component.html',
  styleUrls: ['./notification-message.component.scss'],
})
export class NotificationMessageComponent {
  @Input() public message!: string;
  @Input() public severity = 0;
  @Input() guestAllowed: boolean | undefined = false;

  public isGuestAllowed(): boolean {
    if (this.guestAllowed) {
      return this.guestAllowed;
    }
    return false;
  }
}
