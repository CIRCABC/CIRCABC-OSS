import { Component, EventEmitter, Input, Output } from '@angular/core';
import { TranslocoService } from '@ngneat/transloco';
import { ActionType } from 'app/action-result';
import { InterestGroup, MembersService } from 'app/core/generated/circabc';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { getErrorTranslation, getSuccessTranslation } from 'app/core/util';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-quit-group',
  templateUrl: './quit-group.component.html',
})
export class QuitGroupComponent {
  @Input()
  public show = false;

  @Input()
  public group!: InterestGroup;

  @Input()
  public username = '';

  @Output()
  public readonly showChange = new EventEmitter();

  @Output()
  public readonly membershipRemoved = new EventEmitter();

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
      if (this.group && this.group.id) {
        await firstValueFrom(
          this.membersService.deleteMember(this.group.id, this.username)
        );
        this.show = false;
        this.showChange.emit(this.show);
        this.membershipRemoved.emit();
        const res = this.translateService.translate(
          getSuccessTranslation(ActionType.REMOVE_MEMBERSHIP)
        );
        this.uiMessageService.addSuccessMessage(res, true);
      }
    } catch (error) {
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

    if (this.group.name) {
      result = this.group.name;
    }

    if (this.group.title) {
      const title = this.i18nPipe.transform(this.group.title);

      if (title !== '') {
        result = title;
      }
    }

    return result;
  }
}
