import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  output,
} from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import {
  type InterestGroupProfile,
  MembersService,
} from 'app/core/generated/circabc';
import { InlineDeleteComponent } from 'app/shared/delete/inline-delete.component';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';

/**
 * Displays a single interest-group membership entry for a focused user within
 * the user-management support views.
 *
 * The component renders the group's name/profile as a router link to the group
 * and provides an inline delete control that lets a support administrator
 * remove (uninvite) the user from that interest group. Removal is delegated to
 * the generated {@link MembersService}, and a `userUninvited` event is emitted
 * on success so the parent view can refresh its membership list.
 */
@Component({
  selector: 'cbc-group-box',
  templateUrl: './group-box.component.html',
  styleUrl: './group-box.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, InlineDeleteComponent, I18nPipe, TranslocoModule],
})
export class GroupBoxComponent {
  /** Generated API client used to remove the user from the interest group. */
  private readonly membersService = inject(MembersService);

  /**
   * Required input holding the interest-group profile (group and role) that
   * this box represents for the focused user.
   */
  membership = input.required<InterestGroupProfile>();
  /** Required input with the identifier of the user whose membership is shown. */
  readonly userId = input.required<string>();
  /**
   * Emitted after the user has been successfully removed (uninvited) from the
   * interest group, allowing the parent to refresh its state.
   */
  readonly userUninvited = output();

  /**
   * Indicates whether an uninvite request is currently in progress, used to
   * reflect the pending state in the template.
   */
  public uninviting = false;

  /**
   * Removes the currently displayed user from the interest group represented by
   * this box.
   *
   * Resolves the user and group identifiers from the component inputs and, when
   * both are present, calls {@link MembersService.deleteMember}. On success the
   * {@link userUninvited} output is emitted; any error is logged to the console.
   * The {@link uninviting} flag is toggled around the request to reflect the
   * pending state.
   *
   * @returns A promise that resolves once the uninvite attempt has completed.
   */
  public async uninviteUSer() {
    const userId = this.userId();
    const groupId = this.membership().interestGroup?.id;
    if (groupId && userId) {
      this.uninviting = true;
      try {
        await this.membersService.deleteMemberAsync({ id: groupId, userId });
        this.userUninvited.emit();
      } catch (error) {
        console.error(error);
      }
      this.uninviting = false;
    }
  }
}
