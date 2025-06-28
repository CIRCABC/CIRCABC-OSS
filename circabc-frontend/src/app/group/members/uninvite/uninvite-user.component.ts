import { Component, Input, output, input } from '@angular/core';

import { MembersService, User } from 'app/core/generated/circabc';

import { TranslocoModule } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { ModalComponent } from 'app/shared/modal/modal.component';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-uninvite-user',
  templateUrl: './uninvite-user.component.html',
  preserveWhitespaces: true,
  imports: [ModalComponent, TranslocoModule],
})
export class UninviteUserComponent {
  // TODO: Skipped for migration because:
  //  This input is used in a control flow expression (e.g. `@if` or `*ngIf`)
  //  and migrating would break narrowing currently.
  @Input()
  user: User | undefined;
  readonly groupId = input.required<string>();
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  showDialog!: boolean;
  public readonly modalHide = output<ActionEmitterResult>();

  public removing = false;

  constructor(private membersService: MembersService) {}

  public cancelWizard(backTo: string) {
    if (backTo === 'close') {
      this.showDialog = false;
      this.modalHide.emit({ result: ActionResult.CANCELED });
    }
  }

  public async uninviteMember() {
    const groupId = this.groupId();
    if (groupId !== undefined && this.user && this.user.userId !== undefined) {
      this.removing = true;
      await firstValueFrom(
        this.membersService.deleteMember(groupId, this.user.userId)
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
