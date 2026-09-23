import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  output,
  signal,
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { Node as ModelNode } from 'app/core/generated/circabc';
import { UploadService } from 'app/core/upload.service';
import { ConfirmDialogComponent } from 'app/shared/confirm-dialog/confirm-dialog.component';
import { SizePipe } from 'app/shared/pipes/size.pipe';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';
import { environment } from 'environments/environment';
import { firstValueFrom } from 'rxjs';

/**
 * Standalone component that lets a user replace the binary content of an
 * existing library document (a {@link ModelNode}).
 *
 * It renders a small upload form/wizard with a drag-and-drop area and a file
 * input, plus a "notify" slide toggle that controls whether watchers are
 * notified about the update. Selecting a file and confirming delegates the
 * actual upload to the {@link UploadService}, choosing between a regular
 * content update and a checked-out content update depending on the
 * {@link UpdateContentComponent.updateCheckedOut} flag.
 *
 * For the ECHA release, updating documents whose security ranking is
 * `SENSITIVE` or `SPECIAL_HANDLING` first prompts the user with a
 * {@link ConfirmDialogComponent} before proceeding.
 *
 * When the operation completes (success or cancellation) the component emits
 * an {@link ActionEmitterResult} through the
 * {@link UpdateContentComponent.modalHide} output so a hosting modal can close.
 */
@Component({
  selector: 'cbc-update-content',
  templateUrl: './update-content.component.html',
  styleUrl: './update-content.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ReactiveFormsModule,
    MatSlideToggleModule,
    SpinnerComponent,
    SizePipe,
    TranslocoModule,
  ],
})
export class UpdateContentComponent {
  /** Form builder used to create the notification form group. */
  private readonly notifyFormBuilder = inject(FormBuilder);
  /** Service that performs the actual file content upload to the backend. */
  private readonly uploadService = inject(UploadService);
  /** Material dialog service used to display the security confirmation prompt. */
  private readonly dialog = inject(MatDialog);
  /** Transloco service used to translate dialog messages. */
  private readonly translateService = inject(TranslocoService);

  /** Input flag controlling whether the wizard-style layout is shown. */
  public readonly showWizard = input(false);
  /** Required input: the document node whose content will be replaced. */
  public readonly targetNode = input.required<ModelNode>();
  /**
   * Output emitted when the modal should close, carrying the outcome of the
   * update (success or cancellation) as an {@link ActionEmitterResult}.
   */
  public readonly modalHide = output<ActionEmitterResult>();
  /**
   * Input flag indicating the target document is currently checked out, so the
   * content must be updated without performing an implicit check-in.
   */
  public readonly updateCheckedOut = input(false);

  /** The file selected by the user to upload, or `undefined` if none chosen. */
  public readonly fileToUpload = signal<File | undefined>(undefined);
  /** Whether an upload is currently in progress (drives the spinner/state). */
  public readonly uploading = signal(false);
  /** Current progress value of the upload. */
  public progressValue = 0;
  /** Maximum progress value of the upload. */
  public progressMax = 0;

  /**
   * Reactive form group holding the `notify` toggle that determines whether
   * watchers are notified about the content update.
   */
  notifyFormGroup = this.notifyFormBuilder.group({
    notify: true,
  });

  /**
   * Handles the `dragenter` event over the drop area by suppressing the
   * browser's default handling so the drop target stays active.
   *
   * @param e The drag event to suppress.
   */
  public dragenter(e: DragEvent) {
    e.stopPropagation();
    e.preventDefault();
  }

  /**
   * Handles the `dragover` event over the drop area by suppressing the
   * browser's default handling so the element remains a valid drop target.
   *
   * @param e The drag event to suppress.
   */
  public dragover(e: DragEvent) {
    e.stopPropagation();
    e.preventDefault();
  }

  /**
   * Handles a file dropped onto the drop area, extracting the dropped files
   * and passing them to {@link handleFiles}.
   *
   * @param e The drop event containing the transferred files.
   */
  public drop(e: DragEvent) {
    e.stopPropagation();
    e.preventDefault();

    const dt = e.dataTransfer;
    if (dt !== null) {
      const files = dt.files;
      this.handleFiles(files);
    }
  }

  /**
   * Handles selection of a file through the `<input type="file">` element.
   *
   * @param event The change event emitted by the file input.
   */
  public fileChangeEvent(event: Event) {
    const input = event.target as HTMLInputElement;
    const filesList = input.files as FileList;

    this.handleFiles(filesList);
  }
  /**
   * Stores the selected file into {@link fileToUpload}. When multiple files are
   * provided only the last one is retained, as a single content update targets
   * one document.
   *
   * @param filesList The list of files chosen or dropped by the user.
   */
  private handleFiles(filesList: FileList) {
    for (let i = 0; i < filesList.length; i += 1) {
      const fileItem = filesList.item(i);
      if (fileItem) {
        this.fileToUpload.set(fileItem);
      }
    }
  }

  /**
   * Performs the content update for the {@link targetNode} using the currently
   * selected {@link fileToUpload}.
   *
   * For the ECHA release, if the target document is ranked `SENSITIVE` or
   * `SPECIAL_HANDLING`, the user is first asked to confirm via
   * {@link showDialogConfirmMsg}; the update is aborted if they decline.
   * The upload is delegated to {@link UploadService}, using
   * `updateCheckedOutFileContent` when {@link updateCheckedOut} is `true`
   * (updating without an implicit check-in) or `updateExistingFileContent`
   * otherwise. On success a {@link ActionResult.SUCCEED} result of type
   * {@link ActionType.UPDATE_FILE_CONTENT} is emitted through
   * {@link modalHide}.
   *
   * @returns A promise that resolves once the update flow completes.
   */
  public async update() {
    const targetNode = this.targetNode();
    if (
      environment.circabcRelease === 'echa' &&
      (targetNode?.properties?.security_ranking === 'SENSITIVE' ||
        targetNode?.properties?.security_ranking === 'SPECIAL_HANDLING')
    ) {
      if (!(await this.showDialogConfirmMsg())) {
        return;
      }
    }

    this.uploading.set(true);
    const fileToUpload = this.fileToUpload();
    if (fileToUpload !== undefined) {
      if (this.updateCheckedOut()) {
        // executed in case the document has been checked out
        // and the content has to be updated without checking in implicitly
        await this.uploadService.updateCheckedOutFileContent(
          fileToUpload,
          targetNode.id as string,
          this.notifyFormGroup.controls.notify.value ?? true
        );
      } else {
        await this.uploadService.updateExistingFileContent(
          fileToUpload,
          targetNode.id as string,
          this.notifyFormGroup.controls.notify.value ?? true
        );
      }

      this.fileToUpload.set(undefined);

      const result: ActionEmitterResult = {};
      result.result = ActionResult.SUCCEED;
      result.type = ActionType.UPDATE_FILE_CONTENT;

      this.modalHide.emit(result);
    }
    this.uploading.set(false);
  }

  /**
   * Cancels the update flow. When invoked with `'close'`, it clears the
   * selected file and emits a {@link ActionResult.CANCELED} result of type
   * {@link ActionType.UPDATE_FILE_CONTENT} through {@link modalHide} so the
   * hosting modal can close.
   *
   * @param backTo Destination hint; only `'close'` triggers cancellation.
   */
  public cancelWizard(backTo: string) {
    if (backTo === 'close') {
      this.fileToUpload.set(undefined);
      const result: ActionEmitterResult = {};
      result.result = ActionResult.CANCELED;
      result.type = ActionType.UPDATE_FILE_CONTENT;
      this.modalHide.emit(result);
    }
  }

  /**
   * Opens a {@link ConfirmDialogComponent} warning the user about updating a
   * security-sensitive document, including a link to the relevant Commission
   * decision, and waits for their response.
   *
   * @returns A promise resolving to the dialog result: `true` if the user
   * confirmed, a falsy value otherwise.
   */
  private async showDialogConfirmMsg() {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      ariaLabel: 'Dialog',
      data: {
        messageTranslated: this.translateService.translate(
          'label.dialog.alert.snc.update',
          {
            link: `<a href="https://ec.europa.eu/transparency/documents-register/detail?ref=C(2019)1904&lang=en" target="_blank">C(2019)1904</a>`,
          }
        ),
        labelOK: 'label.confirm',
        title: 'label.dialog.alert.snc.update.title',
        layoutStyle: 'SNCNotification',
      },
    });

    return firstValueFrom(dialogRef.afterClosed());
  }
}
