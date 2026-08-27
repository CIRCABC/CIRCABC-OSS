import { Component, Input, output } from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { HelpService } from 'app/core/generated/circabc';
import { ModalComponent } from 'app/shared/modal/modal.component';
import { HttpErrorResponse } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-delete-help-subcategory',
  templateUrl: './delete-help-subcategory.component.html',
  preserveWhitespaces: true,
  imports: [ModalComponent, TranslocoModule],
})
export class DeleteHelpSubcategoryComponent {
  @Input()
  showModal = false;
  @Input()
  subcategoryId: string | undefined;
  readonly showModalChange = output<boolean>();
  readonly subcategoryDeleted = output<ActionEmitterResult>();

  public deleting = false;
  public errorMessage = '';

  constructor(private helpService: HelpService) {}

  public cancel() {
    this.subcategoryId = undefined;
    this.showModal = false;
    this.errorMessage = '';
    this.showModalChange.emit(this.showModal);
  }

  public async delete() {
    this.deleting = true;
    this.errorMessage = '';
    const res: ActionEmitterResult = {};
    res.type = ActionType.DELETE_HELP_SUBCATEGORY;

    try {
      if (this.subcategoryId) {
        await firstValueFrom(
          this.helpService.deleteHelpSubcategory(this.subcategoryId)
        );

        this.subcategoryId = undefined;
        this.showModal = false;
        this.showModalChange.emit(this.showModal);

        res.result = ActionResult.SUCCEED;
      }
    } catch (error) {
      if (error instanceof HttpErrorResponse && error.status === 409) {
        this.errorMessage = 'help.delete.subcategory.has.articles';
      } else {
        console.error(error);
      }
    }
    this.deleting = false;
    this.subcategoryDeleted.emit(res);
  }
}
