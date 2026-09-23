import {
  ChangeDetectionStrategy,
  Component,
  input,
  output,
} from '@angular/core';

import { TranslocoModule } from '@jsverse/transloco';
import { type User } from 'app/core/generated/circabc';
import { DownloadPipe } from 'app/shared/pipes/download.pipe';
import { SecurePipe } from 'app/shared/pipes/secure.pipe';

/**
 * Presentational component that renders a single group leader as a card,
 * typically used within the "request group" flow of the explorer.
 *
 * The card displays the leader's identifying details (using the secured
 * avatar/resource pipes) and, when deletion is enabled, exposes a control
 * that lets the parent remove this leader from the current selection.
 *
 * The component holds no state of its own: it receives the {@link User} to
 * display via input and delegates removal handling back to its parent
 * through the {@link removeClicked} output.
 */
@Component({
  selector: 'cbc-leader-card',
  templateUrl: './leader-card.component.html',
  styleUrl: './leader-card.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [DownloadPipe, SecurePipe, TranslocoModule],
})
export class LeaderCardComponent {
  /**
   * The user to render as a leader in this card. Required signal input.
   */
  readonly user = input.required<User>();

  /**
   * Whether the delete/remove control should be shown for this card.
   * Defaults to `true`; set to `false` to render the card in a read-only
   * fashion without the removal affordance.
   */
  readonly showDelete = input(true);

  /**
   * Emitted when the user activates the remove control on the card,
   * signalling to the parent that this leader should be removed.
   */
  readonly removeClicked = output();

  /**
   * Handles activation of the card's remove control by emitting the
   * {@link removeClicked} event so the parent component can react.
   *
   * @returns void
   */
  onRemoveClick() {
    this.removeClicked.emit();
  }
}
