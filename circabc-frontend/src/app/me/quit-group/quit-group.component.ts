import { Component, Input, output, input } from '@angular/core';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { ActionType } from 'app/action-result';
import { type InterestGroup, MembersService } from 'app/core/generated/circabc';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { getErrorTranslation, getSuccessTranslation } from 'app/core/util';
import { ModalComponent } from 'app/shared/modal/modal.component';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-quit-group',
  templateUrl: './quit-group.component.html',
  imports: [ModalComponent, TranslocoModule],
})
export class QuitGroupComponent {
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  public show = false;

  public readonly group = input.required<InterestGroup>();

  public readonly username = input('');

  public readonly showChange = output<boolean>();

  public readonly membershipRemoved = output();

  public processing = false;

  constructor(
    private membersService: MembersService,
    private i18nPipe: I18nPipe,
    private translateService: TranslocoService,
    private uiMessageService: UiMessageService
  ) {}

  public async removeMembership() {
    this.processing = true;

    try {
      const group = this.group();
      if (group?.id) {
        await firstValueFrom(
          this.membersService.deleteMember(group.id, this.username())
        );
        this.show = false;
        this.showChange.emit(this.show);
        this.membershipRemoved.emit();
        const res = this.translateService.translate(
          getSuccessTranslation(ActionType.REMOVE_MEMBERSHIP)
        );
        this.uiMessageService.addSuccessMessage(res, true);
      }
    } catch (_error) {
      const res = this.translateService.translate(
        getErrorTranslation(ActionType.REMOVE_MEMBERSHIP)
      );
      this.uiMessageService.addErrorMessage(res, true);
    }

    this.processing = false;
  }

  public cancel() {
    this.show = false;
    this.showChange.emit(this.show);
  }

  public getGroupLabel(): string {
    let result = '';

    const group = this.group();
    if (group.name) {
      result = group.name;
    }

    if (group.title) {
      const title = this.i18nPipe.transform(group.title);

      if (title !== '') {
        result = title;
      }
    }

    return result;
  }
}
