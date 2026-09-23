import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  output,
  signal,
} from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { KeywordsService } from 'app/core/generated/circabc';
import { ModalComponent } from 'app/shared/modal/modal.component';
import { SizePipe } from 'app/shared/pipes/size.pipe';

/**
 * Modal component that lets a user bulk-import keyword definitions into an
 * interest group by uploading a file.
 *
 * Rendered as a `cbc-import-keyword` modal dialog, it presents a file picker
 * plus a drag-and-drop area, then uploads the selected file to the backend via
 * {@link KeywordsService#postBulkKeywordDefinitions}. When the operation
 * completes (successfully, with a failure, or on cancel) it emits an
 * {@link ActionEmitterResult} through the {@link ImportKeywordComponent#modalHide}
 * output so the parent can close the modal and react to the result.
 *
 * Key collaborators:
 * - {@link KeywordsService} — performs the bulk keyword import request.
 * - {@link ModalComponent} — provides the surrounding modal UI shell.
 */
@Component({
  selector: 'cbc-import-keyword',
  templateUrl: './import-keyword.component.html',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ModalComponent, SizePipe, TranslocoModule],
})
export class ImportKeywordComponent {
  /** Generated API client used to send the bulk keyword definition upload. */
  private readonly keywordsService = inject(KeywordsService);

  /**
   * Input controlling whether the import modal is visible.
   * @default false
   */
  readonly showModal = input(false);

  /**
   * Required input holding the id of the interest group into which the
   * keyword definitions are imported.
   */
  readonly parentIgId = input.required<string>();

  /**
   * Output emitted when the modal should be dismissed, carrying the outcome
   * of the import (succeeded, failed or canceled).
   */
  public readonly modalHide = output<ActionEmitterResult>();

  /** Currently selected file to upload, or `undefined` when none is chosen. */
  public readonly fileToUpload = signal<File | undefined>(undefined);

  /** Whether an upload request is currently in progress. */
  public readonly uploading = signal(false);

  /**
   * Cancels the import wizard: clears the selected file and emits a
   * {@link modalHide} event with a canceled {@link ActionEmitterResult}.
   */
  public cancelWizard(): void {
    const result: ActionEmitterResult = {};
    result.result = ActionResult.CANCELED;
    result.type = ActionType.IMPORT_KEYWORD;
    this.fileToUpload.set(undefined);
    this.modalHide.emit(result);
  }

  /**
   * Handles the `dragenter` event over the drop zone by suppressing the
   * browser's default handling so the element can accept dropped files.
   * @param e The drag event fired when a dragged item enters the drop zone.
   */
  public dragenter(e: DragEvent) {
    e.stopPropagation();
    e.preventDefault();
  }

  /**
   * Handles the `dragover` event over the drop zone by suppressing the
   * browser's default handling so the element remains a valid drop target.
   * @param e The drag event fired while a dragged item is over the drop zone.
   */
  public dragover(e: DragEvent) {
    e.stopPropagation();
    e.preventDefault();
  }

  /**
   * Handles a file drop: prevents the default browser behavior and stores the
   * first dropped file as the {@link fileToUpload} candidate.
   * @param e The drop event carrying the transferred files.
   */
  public drop(e: DragEvent) {
    e.stopPropagation();
    e.preventDefault();

    const dt = e.dataTransfer;
    if (dt !== null) {
      const files = dt.files;
      this.fileToUpload.set(files[0]);
    }
  }

  /**
   * Handles selection from a file `<input>` element, storing the first chosen
   * file as the {@link fileToUpload} candidate.
   * @param event The change event whose target is the file input element.
   */
  public fileChangeEvent(event: Event) {
    const input = event.target as HTMLInputElement;
    const filesList = input.files as FileList;

    this.fileToUpload.set(filesList[0]);
  }

  /**
   * Uploads the selected file as bulk keyword definitions for the target
   * interest group.
   *
   * Sets {@link uploading} while the request is in flight, clears the selected
   * file on success, and always emits a {@link modalHide} event with a
   * succeeded or failed {@link ActionEmitterResult}. Errors from the upload
   * are caught internally and reported via the emitted result rather than
   * thrown.
   *
   * @returns A promise that resolves once the import attempt has completed and
   * the result has been emitted.
   */
  public async import() {
    this.uploading.set(true);
    const result: ActionEmitterResult = {};
    result.type = ActionType.IMPORT_KEYWORD;

    try {
      if (this.fileToUpload()) {
        await this.keywordsService.postBulkKeywordDefinitionsAsync({
          id: this.parentIgId(),
          fileData: this.fileToUpload() as File,
        });
      }

      this.fileToUpload.set(undefined);

      result.result = ActionResult.SUCCEED;
    } catch (error) {
      console.error(error);
      result.result = ActionResult.FAILED;
    } finally {
      this.modalHide.emit(result);
      this.uploading.set(false);
    }
  }
}
