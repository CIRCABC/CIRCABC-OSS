import {
  ChangeDetectionStrategy,
  Component,
  input,
  output,
} from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';
import { FileUploadItem } from 'app/group/library/upload-form/file-upload-item';
import { DataCyDirective } from 'app/shared/directives/data-cy.directive';
import { NotificationMessageComponent } from 'app/shared/notification-message/notification-message.component';
import { SizePipe } from 'app/shared/pipes/size.pipe';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';

/**
 * Standalone component that renders the file selection area of the library
 * upload form.
 *
 * It presents a drag-and-drop drop zone together with a native file input,
 * allowing the user to pick one or more files to upload. Selected files are
 * wrapped into {@link FileUploadItem} objects and emitted to the parent
 * component through the {@link FileInputComponent.fileSelected} output.
 *
 * The template also displays contextual information (such as the maximum
 * allowed upload size and guest-access notices) using the imported
 * notification, spinner and size-formatting building blocks.
 */
@Component({
  selector: 'cbc-file-input',
  templateUrl: './file-input.component.html',
  styleUrl: './file-input.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    NotificationMessageComponent,
    DataCyDirective,
    SpinnerComponent,
    SizePipe,
    TranslocoModule,
  ],
})
export class FileInputComponent {
  /**
   * Emits the list of {@link FileUploadItem} instances built from the files the
   * user just selected (via drag-and-drop or the file dialog). The list is
   * emitted once per selection and the internal buffer is cleared afterwards.
   */
  readonly fileSelected = output<FileUploadItem[]>();

  /**
   * When `true`, the file input is considered disabled and the UI should
   * prevent further file selection. Defaults to `false`.
   */
  readonly disable = input(false);

  /**
   * Maximum allowed size, in bytes, for an uploaded file. Defaults to
   * 300 MB (300 * 1000 * 1000 bytes). Used by the template to inform the user
   * of the upload size limit.
   */
  readonly maxFileUpload = input(300 * 1000 * 1000);

  /**
   * Indicates whether the current group grants guest access, used by the
   * template to display the appropriate notice. May be `undefined` when the
   * information has not been provided.
   */
  readonly hasGuestAccess = input<boolean>();

  /**
   * Temporary buffer collecting the {@link FileUploadItem} objects created for
   * the current selection before they are emitted. Reset to an empty array
   * after each emission.
   */
  public filesToUpload: FileUploadItem[] = [];

  /**
   * Handles the native `dragenter` event on the drop zone.
   *
   * Stops propagation and prevents the browser's default handling so the
   * element can act as a valid drop target.
   *
   * @param e The DOM drag event.
   */
  public dragenter(e: DragEvent) {
    e.stopPropagation();
    e.preventDefault();
  }

  /**
   * Handles the native `dragover` event on the drop zone.
   *
   * Stops propagation and prevents the browser's default handling so a drop
   * can subsequently occur on the element.
   *
   * @param e The DOM drag event.
   */
  public dragover(e: DragEvent) {
    e.stopPropagation();
    e.preventDefault();
  }

  /**
   * Handles the native `drop` event on the drop zone.
   *
   * Stops propagation and prevents default handling, then, when file data is
   * present on the transfer, forwards the dropped files to
   * {@link FileInputComponent.handleFiles}.
   *
   * @param e The DOM drag event carrying the dropped files.
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
   * Handles the `change` event of the native file input element.
   *
   * Reads the selected {@link FileList} from the input, forwards it to
   * {@link FileInputComponent.handleFiles} and resets the input value so that
   * selecting the same file again still triggers a new change event.
   *
   * @param event The DOM change event originating from the file input.
   */
  public fileChangeEvent(event: Event) {
    const input = event.target as HTMLInputElement;
    const filesList = input.files as FileList;

    this.handleFiles(filesList);
    input.value = '';
  }

  /**
   * Converts the given browser {@link FileList} into {@link FileUploadItem}
   * objects, emits them through {@link FileInputComponent.fileSelected} and
   * clears the internal buffer.
   *
   * Each created item is initialised with a `selected` flag of `false`, zero
   * progress and an `uploadStatus` of `'start'`. The file name is used as both
   * the item id and name.
   *
   * @param filesList The list of files selected via drag-and-drop or the file
   * dialog.
   */
  private handleFiles(filesList: FileList) {
    for (let i = 0; i < filesList.length; i += 1) {
      const fileItem = filesList.item(i);
      if (fileItem) {
        this.filesToUpload.push({
          file: fileItem,
          id: fileItem.name,
          name: fileItem.name,
          selected: false,
          progress: 0,
          uploadStatus: 'start',
        });
      }
    }

    this.fileSelected.emit(this.filesToUpload);
    this.filesToUpload = [];
  }
}
