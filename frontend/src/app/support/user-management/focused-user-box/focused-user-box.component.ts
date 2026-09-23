import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';
import { User } from 'app/core/generated/circabc';

/**
 * Presentational component that renders a compact summary box for a single
 * user within the support user-management area.
 *
 * The component displays the details of the currently focused {@link User}
 * (for example name, login and related profile information) using the
 * associated template. It is purely presentational: it holds no local state
 * and relies on Transloco for label translation.
 *
 * @remarks
 * Uses the signal-based `input.required` API, so a `user` value must always be
 * bound by the parent component.
 */
@Component({
  selector: 'cbc-focused-user-box',
  templateUrl: './focused-user-box.component.html',
  styleUrl: './focused-user-box.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [TranslocoModule],
})
export class FocusedUserBoxComponent {
  /**
   * Required input holding the user to render in the box.
   *
   * Although declared as required, the underlying type allows `undefined` to
   * accommodate cases where the parent is still resolving the user data; the
   * template is expected to guard against a missing value.
   */
  user = input.required<User | undefined>();
}
