import { Component, EventEmitter, Input, Output } from '@angular/core';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { HelpService } from 'app/core/generated/circabc';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-delete-help-article',
  templateUrl: './delete-help-article.component.html',
  preserveWhitespaces: true,
})
export class DeleteHelpArticleComponent {
  @Input()
  showModal = false;
  @Input()
  articleId: string | undefined;
  @Output()
  readonly showModalChange = new EventEmitter();
  @Output()
  readonly articleDeleted = new EventEmitter<ActionEmitterResult>();

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
