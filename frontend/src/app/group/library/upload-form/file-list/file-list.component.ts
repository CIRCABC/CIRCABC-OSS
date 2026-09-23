import {
  ChangeDetectionStrategy,
  Component,
  input,
  output,
} from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';
import { FileUploadItem } from 'app/group/library/upload-form/file-upload-item';
import { SizePipe } from 'app/shared/pipes/size.pipe';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';

/**
 * Presentational component (`cbc-file-list`) used within the library upload
 * form to render the list of files staged for upload.
 *
 * It displays each {@link FileUploadItem} together with its size (via
 * {@link SizePipe}), shows an upload progress indicator (via
 * {@link SpinnerComponent}) for the file currently being transferred, and lets
 * the user select or remove individual files. Any mutation to the list (a
 * selection toggle or a removal) is surfaced to the parent through the
 * {@link FileListComponent.fileListChange} output.
 */
@Component({
  selector: 'cbc-file-list',
  templateUrl: './file-list.component.html',
  styleUrl: './file-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [SpinnerComponent, SizePipe, TranslocoModule],
})
export class FileListComponent {
  /** Input: the collection of files staged for upload that this component renders. Defaults to an empty array. */
  readonly fileList = input<FileUploadItem[]>([]);
  /** Input: name of the file currently being uploaded, used to display the progress indicator against the matching row. */
  readonly uploadingFileName = input<string>();
  /** Input: upload progress (0–100) for the file currently being transferred. Defaults to 0. */
  readonly uploadingProgress = input(0);
  /** Input: maximum allowed file size in bytes. Defaults to 300 MB (1000 * 1000 * 300). */
  readonly maxFileSize = input(1000 * 1000 * 300);
  /** Output: emits the updated file list whenever a file is selected/deselected or removed. */
  readonly fileListChange = output<FileUploadItem[]>();

  /**
   * Toggles the selection state of the given file, enforcing single selection:
   * the matching file is flipped between selected and deselected while all
   * other files are deselected. Emits the updated list through
   * {@link FileListComponent.fileListChange}.
   *
   * @param file The file whose selection state should be toggled. Matching is done by `name`.
   */
  public selectFile(file: FileUploadItem) {
    this.fileList().forEach((fileItem) => {
      if (fileItem.name === file.name && fileItem.selected) {
        fileItem.selected = false;
      } else if (fileItem.name === file.name && !fileItem.selected) {
        fileItem.selected = true;
      } else {
        fileItem.selected = false;
      }
    });

    this.fileListChange.emit(this.fileList());
  }

  /**
   * Removes the given file from the list (matched by `name`) and emits the
   * updated list through {@link FileListComponent.fileListChange}.
   *
   * @param file The file to remove. Matching is done by `name`.
   */
  public removeFile(file: FileUploadItem) {
    const idx = this.fileList().findIndex((fileItem) => {
      return fileItem.name === file.name;
    });
    this.fileList().splice(idx, 1);
    this.fileListChange.emit(this.fileList());
  }
}
