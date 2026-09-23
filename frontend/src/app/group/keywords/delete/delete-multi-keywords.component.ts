import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  OnChanges,
  output,
  SimpleChanges,
  signal,
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

/**
 * Confirmation modal component that deletes a batch of keyword definitions.
 *
 * Renders a modal listing the keywords to be removed (via
 * {@link KeywordTagComponent}) along with a progress indicator
 * ({@link SpinnerComponent}) shown while deletion is in progress. The user
 * can confirm the bulk deletion or cancel the wizard; in both cases the
 * outcome is reported to the parent through the {@link modalHide} output.
 *
 * Key collaborators:
 * - {@link KeywordsService}: performs the actual keyword definition deletions.
 * - {@link TranslocoService}: resolves localized error messages.
 * - {@link UiMessageService}: surfaces error notifications to the user.
 */
@Component({
  selector: 'cbc-delete-multi-keywords',
  templateUrl: './delete-multi-keywords.component.html',
  styleUrl: './delete-multi-keywords.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [KeywordTagComponent, SpinnerComponent, TranslocoModule],
})
export class DeleteMultiKeywordsComponent implements OnChanges {
  /** API client used to delete keyword definitions on the backend. */
  private readonly keywordsService = inject(KeywordsService);
  /** Translation service used to resolve localized error messages. */
  private readonly translateService = inject(TranslocoService);
  /** Service used to display error notifications in the UI. */
  private readonly uiMessageService = inject(UiMessageService);

  /**
   * Required input holding the list of keyword definitions to delete.
   * Its length drives the progress bar maximum.
   */
  readonly keywords = input.required<KeywordDefinition[]>();

  /** Input controlling whether the deletion modal is displayed. */
  readonly showModal = input(false);
  /**
   * Output emitted when the modal should be closed, carrying the outcome
   * (success or cancellation) of the delete-all operation.
   */
  public readonly modalHide = output<ActionEmitterResult>();

  /** Whether a deletion operation is currently in progress. */
  public deleting = signal(false);
  /** Current progress value: number of keywords processed so far. */
  public progressValue = signal(0);
  /** Progress bar maximum: total number of keywords to delete. */
  public progressMax = 0;

  /**
   * Angular lifecycle hook reacting to input changes.
   *
   * Resets the progress and deleting state, and recomputes the progress
   * maximum based on the current `keywords` input value.
   *
   * @param changes - The set of changed input properties for this component.
   */
  ngOnChanges(changes: SimpleChanges) {
    this.progressValue.set(0);
    this.deleting.set(false);

    const chng = changes.keywords;
    if (chng) {
      if (chng.currentValue) {
        this.progressMax = (chng.currentValue as KeywordDefinition[]).length;
      } else {
        this.progressMax = 0;
      }
    }
  }

  /**
   * Deletes all provided keyword definitions sequentially.
   *
   * Iterates over the `keywords` input, deleting each definition that has an
   * `id`, updating the progress value after each attempt. If a deletion fails
   * because the underlying document is locked, a localized error message is
   * displayed instead of aborting the batch. Once complete, emits a
   * successful {@link ActionEmitterResult} of type `DELETE_ALL` via
   * {@link modalHide}.
   *
   * @returns A promise that resolves once all deletions have been attempted
   * and the completion result has been emitted.
   */
  public async deleteAll() {
    this.deleting.set(true);
    for (const keyword of this.keywords()) {
      if (keyword.id) {
        try {
          await this.keywordsService.deleteKeywordDefinitionAsync({
            keywordId: keyword.id,
          });
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
        this.progressValue.update((value) => value + 1);
      }
    }

    const result: ActionEmitterResult = {};
    result.result = ActionResult.SUCCEED;
    result.type = ActionType.DELETE_ALL;
    this.modalHide.emit(result);
    this.deleting.set(false);
  }

  /**
   * Cancels the deletion wizard without deleting any keywords.
   *
   * Emits a canceled {@link ActionEmitterResult} of type `DELETE_ALL` via
   * {@link modalHide} so the parent can close the modal.
   *
   * @param _action - The originating action identifier (unused).
   */
  public cancelWizard(_action: string): void {
    const result: ActionEmitterResult = {};
    result.result = ActionResult.CANCELED;
    result.type = ActionType.DELETE_ALL;
    this.modalHide.emit(result);
  }
}
