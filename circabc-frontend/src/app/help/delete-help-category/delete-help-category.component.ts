import { Component, EventEmitter, Input, Output } from '@angular/core';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { HelpService } from 'app/core/generated/circabc';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-delete-help-category',
  templateUrl: './delete-help-category.component.html',
  preserveWhitespaces: true,
})
export class DeleteHelpCategoryComponent {
  @Input()
  showModal = false;
  @Input()
  categoryId: string | undefined;
  @Output()
  readonly showModalChange = new EventEmitter();
  @Output()
  readonly categoryDeleted = new EventEmitter<ActionEmitterResult>();

  public deleting = false;

  constructor(private helpService: HelpService) {}

  public cancel() {
    this.categoryId = undefined;
    this.showModal = false;
    this.showModalChange.emit(this.showModal);
  }

  public async delete() {
    this.deleting = true;
    const res: ActionEmitterResult = {};
    res.type = ActionType.DELETE_HELP_CATEGORY;

    try {
      if (this.categoryId) {
        await firstValueFrom(
          this.helpService.deleteHelpCategory(this.categoryId)
        );

        this.categoryId = undefined;
        this.showModal = false;
        this.showModalChange.emit(this.showModal);

        res.result = ActionResult.SUCCEED;
      }
    } catch (error) {
      console.error(error);
    }
    this.deleting = false;
    this.categoryDeleted.emit(res);
  }
}
