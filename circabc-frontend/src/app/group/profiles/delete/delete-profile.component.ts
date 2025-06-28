import { Component, Input, output } from '@angular/core';

import { TranslocoModule, TranslocoService } from '@jsverse/transloco';

import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { Profile, ProfileService } from 'app/core/generated/circabc';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { getErrorTranslation } from 'app/core/util';
import { ModalComponent } from 'app/shared/modal/modal.component';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-delete-profile',
  templateUrl: './delete-profile.component.html',
  preserveWhitespaces: true,
  imports: [ModalComponent, I18nPipe, TranslocoModule],
})
export class DeleteProfileComponent {
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  profile: Profile | undefined;
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  showModal = false;
  readonly profileDeleted = output<ActionEmitterResult>();

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
    if (this.profile?.id) {
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
      } catch (_error) {
        const res = this.translateService.translate(
          getErrorTranslation(ActionType.DELETE_PROFILE)
        );
        this.uiMessageService.addErrorMessage(res);
      }
    }

    this.deleting = false;
  }
}
