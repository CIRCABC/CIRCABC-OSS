import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  OnChanges,
  OnInit,
  output,
  SimpleChanges,
  signal,
} from '@angular/core';

import { TranslocoModule } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { Node as ModelNode } from 'app/core/generated/circabc';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { UploadService } from 'app/core/upload.service';
import { SizePipe } from 'app/shared/pipes/size.pipe';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';
import { FileDetailsComponent } from './file-details/file-details.component';

/**
 * Associates a browser {@link File} with a locally generated numeric
 * identifier so that individual files queued for upload can be tracked and
 * removed from the selection before the upload starts.
 */
interface FileWithId {
  /** Locally generated, monotonically increasing identifier for the file. */
  id: number;
  /** The selected browser file to be uploaded. */
  file: File;
}
/**
 * Angular component that renders the "add content" upload wizard for the
 * document library.
 *
 * It displays a two-step wizard: step one lets the user select files (via a
 * file input or drag-and-drop) and review the selection (names and total
 * size); step two uploads the selected files one by one to the parent node
 * while showing progress. On completion or cancellation it emits an
 * {@link ActionEmitterResult} so the hosting modal can react.
 *
 * Key collaborators: {@link UploadService} performs the actual file upload,
 * and {@link UiMessageService} surfaces error notifications to the user.
 */
@Component({
  selector: 'cbc-add-content',
  templateUrl: './add-content.component.html',
  styleUrl: './add-content.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [SpinnerComponent, FileDetailsComponent, SizePipe, TranslocoModule],
})
export class AddContentComponent implements OnInit, OnChanges {
  /** Service used to upload each selected file to the backend. */
  private readonly uploadService = inject(UploadService);
  /** Service used to display error messages when an upload fails. */
  private readonly uiMessageService = inject(UiMessageService);

  /**
   * Input flag that, when set to `true`, opens the wizard at step one.
   * Changes to this input are observed in {@link ngOnChanges}.
   */
  public readonly showWizard = input(false);
  /**
   * Required input holding the parent {@link ModelNode} under which the
   * selected files will be uploaded.
   */
  public readonly parentNode = input.required<ModelNode>();
  /**
   * Output emitted when the wizard closes, carrying an
   * {@link ActionEmitterResult} that reports whether the upload succeeded or
   * was canceled.
   */
  public readonly modalHide = output<ActionEmitterResult>();

  /** Whether the first wizard step (file selection) is currently shown. */
  public showAddWizardStep1 = false;
  /** Whether the second wizard step (upload progress) is currently shown. */
  public showAddWizardStep2 = false;

  /** Files selected by the user that are queued for upload. */
  public filesToUpload: FileWithId[] = [];
  /** Files that have already been successfully uploaded. */
  public filesUploaded = signal<File[]>([]);
  /** Identifiers of the nodes created for successfully uploaded files. */
  private nodesID: string[] = [];

  /** Counter used to assign a unique {@link FileWithId.id} to each file. */
  public idCount = 0;

  /** Number of files uploaded so far, used to drive the progress bar. */
  public progressValue = signal(0);
  /** Total number of files to upload, used as the progress bar maximum. */
  public progressMax = 0;

  /**
   * Angular lifecycle hook. Resets the queued and uploaded file lists when
   * the component initializes.
   */
  ngOnInit() {
    this.filesToUpload = [];
    this.filesUploaded.set([]);
  }

  /**
   * Angular lifecycle hook. When the {@link showWizard} input changes, opens
   * or closes the first wizard step accordingly.
   *
   * @param changes The set of changed input properties.
   */
  ngOnChanges(changes: SimpleChanges) {
    if (changes.showWizard) {
      this.showAddWizardStep1 = changes.showWizard.currentValue;
    }
  }

  /**
   * Drag event handler that suppresses the browser default behavior when a
   * dragged item enters the drop zone.
   *
   * @param e The drag event.
   */
  public dragenter(e: DragEvent) {
    e.stopPropagation();
    e.preventDefault();
  }

  /**
   * Drag event handler that suppresses the browser default behavior while a
   * dragged item is over the drop zone (required to allow dropping).
   *
   * @param e The drag event.
   */
  public dragover(e: DragEvent) {
    e.stopPropagation();
    e.preventDefault();
  }

  /**
   * Drop event handler. Suppresses the browser default behavior and queues
   * any dropped files for upload.
   *
   * @param e The drop event carrying the transferred files.
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
   * Change handler for the file input element. Queues the files selected
   * through the input dialog for upload.
   *
   * @param event The change event whose target is the file input element.
   */
  public fileChangeEvent(event: Event) {
    const input = event.target as HTMLInputElement;
    const filesList = input.files as FileList;

    this.handleFiles(filesList);
  }

  /**
   * Adds each file in the given list to {@link filesToUpload}, assigning a
   * unique {@link FileWithId.id} to every file.
   *
   * @param filesList The list of files selected or dropped by the user.
   */
  private handleFiles(filesList: FileList) {
    for (let i = 0; i < filesList.length; i += 1) {
      const fileItem = filesList.item(i);
      if (fileItem) {
        this.filesToUpload.push({ file: fileItem, id: this.idCount });
        this.idCount += 1;
      }
    }
  }

  /**
   * Opens the first wizard step (file selection).
   */
  public launchAddWizardStep1() {
    this.showAddWizardStep1 = true;
  }

  /**
   * Advances to the second wizard step (upload progress) when at least one
   * file is queued, and starts uploading the queued files sequentially.
   *
   * @returns A promise that resolves once all queued files have been
   * processed.
   */
  public async launchAddWizardStep2() {
    /// await this.uploadFiles();
    if (this.filesToUpload.length > 0) {
      this.showAddWizardStep1 = false;
      this.showAddWizardStep2 = true;
      await this.uploadFilesOneByOne();
    }
  }

  /**
   * Uploads the queued files one at a time to the parent node, tracking the
   * created node identifiers and updating the progress counters. Failed
   * uploads produce an error message but do not interrupt the remaining
   * uploads. Returns early if the parent node has no identifier.
   *
   * @returns A promise that resolves once every queued file has been
   * attempted.
   */
  private async uploadFilesOneByOne() {
    const parentNode = this.parentNode();
    if (parentNode.id === undefined) {
      return;
    }
    this.progressMax = this.filesToUpload.length;
    this.progressValue.set(0);
    for (const fileWithId of this.filesToUpload) {
      const file: File = fileWithId.file;
      try {
        const nodeId = await this.uploadService.uploadNewFile(
          file,
          parentNode.id
        );
        this.nodesID.push(nodeId);
        this.filesUploaded.update((files) => [...files, file]);
      } catch (error) {
        console.error(error);
        this.uiMessageService.addErrorMessage('Error during file upload');
      }
      this.progressValue.update((value) => value + 1);
    }
  }

  /**
   * Removes the given file from the upload queue by matching its
   * {@link FileWithId.id}.
   *
   * @param file The queued file to remove from {@link filesToUpload}.
   */
  public deleteSelectedFile(file: FileWithId) {
    let index = 0;
    for (const fileToUpload of this.filesToUpload) {
      if (fileToUpload.id === file.id) {
        this.filesToUpload.splice(index, 1);
      }
      index += 1;
    }
  }

  /**
   * Closes the wizard, resetting all internal state, and emits
   * {@link modalHide} with the outcome derived from the requested action.
   *
   * @param backTo The close reason: `'close'` emits a canceled result and
   * `'finish'` emits a succeeded result; any other value only resets state.
   */
  public cancelWizard(backTo: string) {
    this.nodesID = [];
    this.filesToUpload = [];
    this.filesUploaded.set([]);

    if (backTo === 'close') {
      this.showAddWizardStep1 = false;
      this.showAddWizardStep2 = false;

      const result: ActionEmitterResult = {};
      result.result = ActionResult.CANCELED;
      result.type = ActionType.UPLOAD_FILE;

      this.modalHide.emit(result);
    } else if (backTo === 'finish') {
      this.showAddWizardStep1 = false;
      this.showAddWizardStep2 = false;

      const result: ActionEmitterResult = {};
      result.result = ActionResult.SUCCEED;
      result.type = ActionType.UPLOAD_FILE;

      this.modalHide.emit(result);
    }
  }

  /**
   * Returns the names of the files currently queued for upload.
   *
   * @returns An array of the queued files' names.
   */
  public fileNames() {
    return this.filesToUpload.map((f) => f.file.name);
  }

  /**
   * Computes the combined size of all files currently queued for upload.
   *
   * @returns The total size, in bytes, of the queued files.
   */
  public filesSize(): number {
    const filesizes = this.filesToUpload.map((f) => f.file.size);
    return filesizes.reduce((a, b) => a + b, 0);
  }

  /**
   * Indicates whether the sequential upload has processed every queued file.
   *
   * @returns `true` when the progress value has reached its maximum.
   */
  public isUploadFinished() {
    return this.progressValue() === this.progressMax;
  }
}
