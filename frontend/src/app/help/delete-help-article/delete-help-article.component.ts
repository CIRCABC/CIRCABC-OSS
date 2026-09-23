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
 * Confirmation dialog component for deleting a help article.
 *
 * Renders a modal (via {@link ModalComponent}) that asks the user to confirm
 * the deletion of a help article. On confirmation it delegates the deletion to
 * the generated {@link HelpService} and reports the outcome back to the parent
 * component through its outputs.
 *
 * The modal visibility is driven by the aliased `showModal` input, which is
 * mirrored into an internal writable {@link linkedSignal} so the component can
 * close the dialog itself while still emitting the change to the parent.
 */
@Component({
  selector: 'cbc-delete-help-article',
  templateUrl: './delete-help-article.component.html',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ModalComponent, TranslocoModule],
})
export class DeleteHelpArticleComponent {
  /** Generated API client used to perform the help article deletion. */
  private readonly helpService = inject(HelpService);

  /**
   * Input (aliased as `showModal`) controlling whether the confirmation modal
   * is initially displayed. Defaults to `false`.
   */
  // eslint-disable-next-line @angular-eslint/no-input-rename
  readonly showModalInput = input(false, { alias: 'showModal' });
  /**
   * Internal, writable signal that tracks the modal's visibility. It is
   * initialised from {@link showModalInput} but can be updated locally (e.g.
   * when the dialog is cancelled or the deletion succeeds).
   */
  readonly showModal = linkedSignal(this.showModalInput);
  /** Two-way bindable model holding the id of the help article to delete. */
  readonly articleId = model<string>();
  /** Emits the new modal visibility state whenever the dialog is closed. */
  readonly showModalChange = output<boolean>();
  /** Emits the result of the delete operation once it has completed. */
  readonly articleDeleted = output<ActionEmitterResult>();

  /** Indicates whether a deletion request is currently in progress. */
  public readonly deleting = signal(false);

  /**
   * Cancels the deletion: clears the selected article id, hides the modal and
   * notifies the parent of the visibility change.
   *
   * @returns Nothing.
   */
  public cancel() {
    this.articleId.set(undefined);
    this.showModal.set(false);
    this.showModalChange.emit(this.showModal());
  }

  /**
   * Performs the deletion of the currently selected help article.
   *
   * Sets the {@link deleting} flag while the request is in flight, calls
   * {@link HelpService.deleteHelpArticle} for the current {@link articleId} and,
   * on success, clears the id, closes the modal and emits the visibility
   * change. Any error is logged to the console and does not propagate.
   * Regardless of the outcome, an {@link ActionEmitterResult} (of type
   * `DELETE_HELP_ARTICLE`) is emitted through {@link articleDeleted}.
   *
   * @returns A promise that resolves once the deletion attempt has finished.
   */
  public async delete() {
    this.deleting.set(true);
    const res: ActionEmitterResult = {};
    res.type = ActionType.DELETE_HELP_ARTICLE;

    try {
      const articleId = this.articleId();
      if (articleId) {
        await this.helpService.deleteHelpArticleAsync({ id: articleId });

        this.articleId.set(undefined);
        this.showModal.set(false);
        this.showModalChange.emit(this.showModal());

        res.result = ActionResult.SUCCEED;
      }
    } catch (error) {
      console.error(error);
    }
    this.deleting.set(false);
    this.articleDeleted.emit(res);
  }
}
