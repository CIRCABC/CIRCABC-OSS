import { Component, EventEmitter, Input, Output } from '@angular/core';

import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { MembersService } from 'app/core/generated/circabc';
import { SelectableUserProfile } from 'app/core/ui-model/index';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-uninvite-multiple',
  templateUrl: './uninvite-multiple.component.html',
  preserveWhitespaces: true,
})
export class UninviteMultipleComponent {
  public deleting = false;

  @Input()
  users!: SelectableUserProfile[];
  @Input()
  groupId!: string;
  @Input()
  showModal!: boolean;
  @Output()
  public readonly modalHide = new EventEmitter<ActionEmitterResult>();

  constructor(private membersService: MembersService) {}

  async deleteMembers() {
    const result: ActionEmitterResult = {};
    result.type = ActionType.REMOVE_MEMBERSHIPS;
    this.deleting = true;

    try {
      for (const member of this.users) {
        if (member.user && member.user.userId) {
          await firstValueFrom(
            this.membersService.deleteMember(this.groupId, member.user.userId)
          );
        }
      }

      result.result = ActionResult.SUCCEED;
    } catch (error) {
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
