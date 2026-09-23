import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  linkedSignal,
  model,
  output,
  signal,
} from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { HelpService } from 'app/core/generated/circabc';
import { ModalComponent } from 'app/shared/modal/modal.component';

/**
 * Confirmation dialog component for deleting a help category.
 *
 * Renders a modal (via {@link ModalComponent}) that asks the user to confirm
 * deletion of a help category. When confirmed, it delegates the deletion to
 * the generated {@link HelpService} REST client and reports the outcome back
 * to the parent component.
 *
 * Typical usage is to bind `showModal` and `categoryId` and to listen to the
 * `categoryDeleted` output for the result of the operation.
 */
@Component({
  selector: 'cbc-delete-help-category',
  templateUrl: './delete-help-category.component.html',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ModalComponent, TranslocoModule],
})
export class DeleteHelpCategoryComponent {
  /** Generated CIRCABC help API client used to perform the deletion. */
  private readonly helpService = inject(HelpService);

  /**
   * Input controlling the initial visibility of the modal.
   *
   * Exposed to templates under the alias `showModal`; the internal writable
   * {@link showModal} signal is derived from this input.
   */
  // eslint-disable-next-line @angular-eslint/no-input-rename
  readonly showModalInput = input(false, { alias: 'showModal' });
  /**
   * Writable signal tracking whether the modal is currently displayed.
   *
   * Linked to {@link showModalInput}, so it re-initialises when the input
   * changes but can also be updated locally (e.g. when cancelling or after a
   * successful deletion).
   */
  readonly showModal = linkedSignal(this.showModalInput);
  /** Two-way bindable model holding the identifier of the category to delete. */
  readonly categoryId = model<string>();
  /** Emits the updated visibility state whenever the modal is closed. */
  readonly showModalChange = output<boolean>();
  /** Emits the result of the delete operation once it completes. */
  readonly categoryDeleted = output<ActionEmitterResult>();

  /** Indicates whether a deletion request is currently in progress. */
  public readonly deleting = signal(false);

  /**
   * Cancels the deletion, clearing the selected category and closing the modal.
   *
   * Resets {@link categoryId}, hides the modal and notifies the parent via
   * {@link showModalChange}.
   */
  public cancel() {
    this.categoryId.set(undefined);
    this.showModal.set(false);
    this.showModalChange.emit(this.showModal());
  }

  /**
   * Deletes the currently selected help category.
   *
   * When a {@link categoryId} is set, calls
   * {@link HelpService.deleteHelpCategory} and, on success, clears the
   * selection, closes the modal and marks the result as succeeded. Any error
   * is logged to the console and leaves the result without a success flag.
   * Regardless of the outcome, the result is emitted via
   * {@link categoryDeleted}.
   *
   * @returns A promise that resolves once the deletion attempt has completed
   *   and the result has been emitted.
   */
  public async delete() {
    this.deleting.set(true);
    const res: ActionEmitterResult = {};
    res.type = ActionType.DELETE_HELP_SECTION;

    try {
      const categoryId = this.categoryId();
      if (categoryId) {
        await this.helpService.deleteHelpCategoryAsync({ id: categoryId });

        this.categoryId.set(undefined);
        this.showModal.set(false);
        this.showModalChange.emit(this.showModal());

        res.result = ActionResult.SUCCEED;
      }
    } catch (error) {
      console.error(error);
    }
    this.deleting.set(false);
    this.categoryDeleted.emit(res);
  }
}
