import { ChangeDetectionStrategy, Component, input } from '@angular/core';

import { TranslocoModule } from '@jsverse/transloco';
import { type User } from 'app/core/generated/circabc';
import { DownloadPipe } from 'app/shared/pipes/download.pipe';
import { SecurePipe } from 'app/shared/pipes/secure.pipe';

/**
 * Presentational component (`cbc-member-card`) that renders a summary card for a
 * single group member.
 *
 * It displays the member's account information (such as avatar/profile picture
 * and profile details) based on the supplied {@link User}. Avatar image URLs are
 * resolved through the {@link DownloadPipe} and {@link SecurePipe}, and static
 * labels are translated via Transloco.
 *
 * The component holds no local state and derives everything it shows from its
 * required `user` input, making it a purely display-oriented card used within
 * the member account views.
 */
@Component({
  selector: 'cbc-member-card',
  templateUrl: './member-card.component.html',
  styleUrl: './member-card.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [DownloadPipe, SecurePipe, TranslocoModule],
})
export class MemberCardComponent {
  /**
   * Required signal input carrying the {@link User} whose details are rendered
   * by this card. Provided by the parent component.
   */
  readonly user = input.required<User>();

  /**
   * Determines whether the member's profile is publicly visible.
   *
   * @returns The user's `visibility` flag when defined, otherwise `false`.
   */
  getUserVisibility(): boolean {
    const user = this.user();
    if (user?.visibility !== undefined) {
      return user.visibility;
    }

    return false;
  }
}
