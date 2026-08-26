import {
  Component,
  Input,
  OnChanges,
  SimpleChanges,
  output,
  input,
} from '@angular/core';

import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { KeywordDefinition, KeywordsService } from 'app/core/generated/circabc';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { KeywordTagComponent } from 'app/group/keywords/tag/keyword-tag.component';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-delete-multi-keywords',
  templateUrl: './delete-multi-keywords.component.html',
  styleUrl: './delete-multi-keywords.component.scss',
  preserveWhitespaces: true,
  imports: [KeywordTagComponent, SpinnerComponent, TranslocoModule],
})
export class DeleteMultiKeywordsComponent implements OnChanges {
  readonly keywords = input.required<KeywordDefinition[]>();
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  showModal = false;
  public readonly modalHide = output<ActionEmitterResult>();

  public deleting = false;
  public progressValue = 0;
  public progressMax = 0;

  constructor(
    private keywordsService: KeywordsService,
    private translateService: TranslocoService,
    private uiMessageService: UiMessageService
  ) {}

  ngOnChanges(changes: SimpleChanges) {
    this.progressValue = 0;
    this.deleting = false;

    const chng = changes.keywords;
    if (chng) {
      if (chng.currentValue) {
        this.progressMax = (chng.currentValue as KeywordDefinition[]).length;
      } else {
        this.progressMax = 0;
      }
    }
  }

  public async deleteAll() {
    this.deleting = true;
    for (const keyword of this.keywords()) {
      if (keyword.id) {
        try {
          await firstValueFrom(
            this.keywordsService.deleteKeywordDefinition(keyword.id)
          );
        } catch (error) {
          if (error.message.indexOf('locked')) {
            const text = this.translateService.translate(
              'keywords.deletion.failed.locked.document'
            );
            if (text) {
              this.uiMessageService.addErrorMessage(text, false);
            }
          }
        }
        this.progressValue += 1;
      }
    }

    this.showModal = false;
    const result: ActionEmitterResult = {};
    result.result = ActionResult.SUCCEED;
    result.type = ActionType.DELETE_ALL;
    this.modalHide.emit(result);
    this.deleting = false;
  }

  public cancelWizard(_action: string): void {
    this.showModal = false;
    const result: ActionEmitterResult = {};
    result.result = ActionResult.CANCELED;
    result.type = ActionType.DELETE_ALL;
    this.modalHide.emit(result);
  }
}
