import { ChangeDetectionStrategy, Component, input } from '@angular/core';

/**
 * Presentational component that renders a single notification/alert message.
 *
 * Displayed via the `cbc-notification-message` selector, it shows the provided
 * message text styled according to a severity level and optionally indicates
 * whether the notification is available to guest (unauthenticated) users.
 *
 * The component is purely display-oriented: all data is supplied through its
 * signal-based inputs and it exposes no outputs.
 */
@Component({
  selector: 'cbc-notification-message',
  templateUrl: './notification-message.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  styleUrl: './notification-message.component.scss',
})
export class NotificationMessageComponent {
  /**
   * Required input holding the text content of the notification to render.
   */
  public readonly message = input.required<string>();
  /**
   * Input describing the severity level of the notification, used to drive its
   * visual styling. Defaults to `0`.
   */
  public readonly severity = input(0);
  /**
   * Input flag indicating whether the notification should be shown to guest
   * (unauthenticated) users. Defaults to `false`; may be `undefined`.
   */
  readonly guestAllowed = input<boolean | undefined>(false);

  /**
   * Resolves the {@link guestAllowed} input to a strict boolean, treating any
   * falsy or `undefined` value as `false`.
   *
   * @returns `true` when the notification is allowed for guest users, otherwise `false`.
   */
  public isGuestAllowed(): boolean {
    const guestAllowed = this.guestAllowed();
    if (guestAllowed) {
      return guestAllowed;
    }
    return false;
  }
}
