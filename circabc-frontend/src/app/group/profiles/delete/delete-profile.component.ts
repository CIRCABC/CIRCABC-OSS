import { Component, EventEmitter, Input, Output } from '@angular/core';

import { TranslocoService } from '@ngneat/transloco';

import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { Profile, ProfileService } from 'app/core/generated/circabc';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { getErrorTranslation } from 'app/core/util';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-delete-profile',
  templateUrl: './delete-profile.component.html',
  preserveWhitespaces: true,
})
export class DeleteProfileComponent {
  @Input()
  profile: Profile | undefined;
  @Input()
  showModal = false;
  @Output()
  readonly profileDeleted = new EventEmitter<ActionEmitterResult>();

  public deleting = false;

  constructor(
    private profileService: ProfileService,
    private uiMessageService: UiMessageService,
    private translateService: TranslocoService
  ) {}

  cancelWizard() {
    this.showModal = false;
    this.profile = undefined;

    const result: ActionEmitterResult = {};
    result.type = ActionType.DELETE_PROFILE;
    result.result = ActionResult.CANCELED;

    this.profileDeleted.emit(result);
  }

  async delete() {
    this.deleting = true;
    if (this.profile && this.profile.id) {
      const result: ActionEmitterResult = {};
      result.type = ActionType.DELETE_PROFILE;

      try {
        await firstValueFrom(
          this.profileService.deleteProfile(this.profile.id)
        );
        result.result = ActionResult.SUCCEED;
        this.profileDeleted.emit(result);
        this.showModal = false;
        this.profile = undefined;
      } catch (error) {
        const res = this.translateService.translate(
          getErrorTranslation(ActionType.DELETE_PROFILE)
        );
        this.uiMessageService.addErrorMessage(res);
      }
    }

    this.deleting = false;
  }
}
