import { Component, Input, output } from '@angular/core';

import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import {
  type KeywordDefinition,
  KeywordsService,
} from 'app/core/generated/circabc';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { KeywordTagComponent } from 'app/group/keywords/tag/keyword-tag.component';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-delete-keyword',
  templateUrl: './delete-keyword.component.html',
  preserveWhitespaces: true,
  imports: [KeywordTagComponent, SpinnerComponent, TranslocoModule],
})
export class DeleteKeywordComponent {
  // TODO: Skipped for migration because:
  //  This input is used in a control flow expression (e.g. `@if` or `*ngIf`)
  //  and migrating would break narrowing currently.
  @Input()
  keyword!: KeywordDefinition;
  public readonly modalHide = output<ActionEmitterResult>();

  public deleting = false;
  public showModal = false;

  constructor(
    private keywordsService: KeywordsService,
    private translateService: TranslocoService,
    private uiMessageService: UiMessageService
  ) {}

  public async delete() {
    if (this.keyword.id) {
      this.deleting = true;
      const result: ActionEmitterResult = {};
      result.type = ActionType.DELETE_KEYWORD;
      try {
        await firstValueFrom(
          this.keywordsService.deleteKeywordDefinition(this.keyword.id)
        );
        result.result = ActionResult.SUCCEED;
        this.showModal = false;
        this.modalHide.emit(result);
      } catch (error) {
        if (error.message.indexOf('locked')) {
          const text = this.translateService.translate(
            'keywords.deletion.failed.locked.document'
          );
          if (text) {
            this.uiMessageService.addErrorMessage(text, false);
          }
        }
        result.result = ActionResult.FAILED;
      }
      this.modalHide.emit(result);
      this.deleting = false;
    }
  }

  public cancelWizard(_action: string): void {
    this.showModal = false;
    const result: ActionEmitterResult = {};
    result.result = ActionResult.CANCELED;
    result.type = ActionType.DELETE_KEYWORD;
    this.modalHide.emit(result);
  }
}
