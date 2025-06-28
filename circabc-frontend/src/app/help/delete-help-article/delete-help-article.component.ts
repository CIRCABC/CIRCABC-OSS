import { Component, Input, output } from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { HelpService } from 'app/core/generated/circabc';
import { ModalComponent } from 'app/shared/modal/modal.component';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-delete-help-article',
  templateUrl: './delete-help-article.component.html',
  preserveWhitespaces: true,
  imports: [ModalComponent, TranslocoModule],
})
export class DeleteHelpArticleComponent {
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  showModal = false;
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  articleId: string | undefined;
  readonly showModalChange = output<boolean>();
  readonly articleDeleted = output<ActionEmitterResult>();

  public deleting = false;

  constructor(private helpService: HelpService) {}

  public cancel() {
    this.articleId = undefined;
    this.showModal = false;
    this.showModalChange.emit(this.showModal);
  }

  public async delete() {
    this.deleting = true;
    const res: ActionEmitterResult = {};
    res.type = ActionType.DELETE_HELP_ARTICLE;

    try {
      if (this.articleId) {
        await firstValueFrom(
          this.helpService.deleteHelpArticle(this.articleId)
        );

        this.articleId = undefined;
        this.showModal = false;
        this.showModalChange.emit(this.showModal);

        res.result = ActionResult.SUCCEED;
      }
    } catch (error) {
      console.error(error);
    }
    this.deleting = false;
    this.articleDeleted.emit(res);
  }
}
