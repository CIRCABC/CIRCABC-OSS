import { Component, Input, output, input } from '@angular/core';

import { TranslocoModule } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { MembersService } from 'app/core/generated/circabc';
import { SelectableUserProfile } from 'app/core/ui-model/index';
import { ModalComponent } from 'app/shared/modal/modal.component';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-uninvite-multiple',
  templateUrl: './uninvite-multiple.component.html',
  preserveWhitespaces: true,
  imports: [ModalComponent, I18nPipe, TranslocoModule],
})
export class UninviteMultipleComponent {
  public deleting = false;

  readonly users = input.required<SelectableUserProfile[]>();
  readonly groupId = input.required<string>();
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  showModal!: boolean;
  public readonly modalHide = output<ActionEmitterResult>();

  constructor(private membersService: MembersService) {}

  async deleteMembers() {
    const result: ActionEmitterResult = {};
    result.type = ActionType.REMOVE_MEMBERSHIPS;
    this.deleting = true;

    try {
      for (const member of this.users()) {
        if (member.user?.userId) {
          await firstValueFrom(
            this.membersService.deleteMember(this.groupId(), member.user.userId)
          );
        }
      }

      result.result = ActionResult.SUCCEED;
    } catch (_error) {
      result.result = ActionResult.FAILED;
    }

    this.modalHide.emit(result);
    this.deleting = false;
    this.showModal = false;
  }

  public cancel() {
    const result: ActionEmitterResult = {};
    result.type = ActionType.REMOVE_MEMBERSHIPS;
    result.result = ActionResult.CANCELED;
    this.modalHide.emit(result);
    this.showModal = false;
  }
}
