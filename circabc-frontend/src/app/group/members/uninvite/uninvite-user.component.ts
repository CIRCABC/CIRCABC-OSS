import { Component, EventEmitter, Input, Output } from '@angular/core';

import { MembersService, User } from 'app/core/generated/circabc';

import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-uninvite-user',
  templateUrl: './uninvite-user.component.html',
  preserveWhitespaces: true,
})
export class UninviteUserComponent {
  @Input()
  user: User | undefined;
  @Input()
  groupId!: string;
  @Input()
  showDialog!: boolean;
  @Output()
  public readonly modalHide = new EventEmitter();

  public removing = false;

  constructor(private membersService: MembersService) {}

  public cancelWizard(backTo: string) {
    if (backTo === 'close') {
      this.showDialog = false;
      this.modalHide.emit();
    }
  }

  public async uninviteMember() {
    if (
      this.groupId !== undefined &&
      this.user &&
      this.user.userId !== undefined
    ) {
      this.removing = true;
      await firstValueFrom(
        this.membersService.deleteMember(this.groupId, this.user.userId)
      );

      this.showDialog = false;

      const result: ActionEmitterResult = {};
      result.result = ActionResult.SUCCEED;
      result.type = ActionType.REMOVE_MEMBERSHIP;

      this.modalHide.emit(result);
      this.removing = false;
    }
  }
}
