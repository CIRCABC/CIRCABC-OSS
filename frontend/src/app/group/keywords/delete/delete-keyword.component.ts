import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  output,
  signal,
} from '@angular/core';

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

/**
 * Confirmation dialog component for deleting a keyword definition.
 *
 * Renders a modal that displays the target keyword (via {@link KeywordTagComponent})
 * and lets the user confirm or cancel its deletion. On confirmation it calls the
 * backend through {@link KeywordsService} to remove the keyword definition, shows a
 * spinner while the request is in flight, surfaces a localized error message when the
 * keyword is locked, and reports the outcome to the parent component through the
 * {@link DeleteKeywordComponent.modalHide} output.
 *
 * Key collaborators:
 * - {@link KeywordsService} performs the actual deletion request.
 * - {@link TranslocoService} resolves localized error text.
 * - {@link UiMessageService} displays user-facing error notifications.
 */
@Component({
  selector: 'cbc-delete-keyword',
  templateUrl: './delete-keyword.component.html',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [KeywordTagComponent, SpinnerComponent, TranslocoModule],
})
export class DeleteKeywordComponent {
  /** API client used to delete the keyword definition on the backend. */
  private readonly keywordsService = inject(KeywordsService);
  /** Translation service used to resolve localized error messages. */
  private readonly translateService = inject(TranslocoService);
  /** Service used to display error notifications in the UI. */
  private readonly uiMessageService = inject(UiMessageService);

  /**
   * Required input holding the keyword definition targeted for deletion.
   * Its `id` is used to perform the delete request.
   */
  readonly keyword = input.required<KeywordDefinition>();
  /**
   * Output emitted when the modal closes, carrying the outcome of the
   * operation (success, failure or cancellation) as an {@link ActionEmitterResult}.
   */
  public readonly modalHide = output<ActionEmitterResult>();

  /** Whether a deletion request is currently in progress (drives the spinner). */
  public readonly deleting = signal(false);
  /** Whether the confirmation modal is currently visible. */
  public readonly showModal = signal(false);

  /**
   * Deletes the currently targeted keyword definition.
   *
   * When the keyword has an `id`, sets {@link deleting} to `true`, invokes
   * {@link KeywordsService.deleteKeywordDefinition} and, on success, hides the modal
   * and emits a `SUCCEED` result. On failure it emits a `FAILED` result and, when the
   * error indicates the keyword is locked, shows a localized error message via
   * {@link UiMessageService}. The result is emitted through {@link modalHide}.
   *
   * @returns A promise that resolves once the deletion attempt has completed and the
   * outcome has been emitted.
   */
  public async delete() {
    const keyword = this.keyword();
    if (keyword.id) {
      this.deleting.set(true);
      const result: ActionEmitterResult = {};
      result.type = ActionType.DELETE_KEYWORD;
      try {
        await this.keywordsService.deleteKeywordDefinitionAsync({
          keywordId: keyword.id,
        });
        result.result = ActionResult.SUCCEED;
        this.showModal.set(false);
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
      this.deleting.set(false);
    }
  }

  /**
   * Cancels the deletion wizard without deleting the keyword.
   *
   * Hides the modal and emits a `CANCELED` result through {@link modalHide}.
   *
   * @param _action The originating action identifier (unused).
   */
  public cancelWizard(_action: string): void {
    this.showModal.set(false);
    const result: ActionEmitterResult = {};
    result.result = ActionResult.CANCELED;
    result.type = ActionType.DELETE_KEYWORD;
    this.modalHide.emit(result);
  }
}
