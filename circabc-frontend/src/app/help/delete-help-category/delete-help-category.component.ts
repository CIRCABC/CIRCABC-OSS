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
  selector: 'cbc-delete-help-category',
  templateUrl: './delete-help-category.component.html',
  preserveWhitespaces: true,
  imports: [ModalComponent, TranslocoModule],
})
export class DeleteHelpCategoryComponent {
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  showModal = false;
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  categoryId: string | undefined;
  readonly showModalChange = output<boolean>();
  readonly categoryDeleted = output<ActionEmitterResult>();

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
    res.type = ActionType.DELETE_HELP_SECTION;

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
