import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  OnInit,
  output,
  signal,
} from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';

import { MatDialog } from '@angular/material/dialog';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import {
  BASE_PATH,
  InterestGroupService,
  Node as ModelNode,
} from 'app/core/generated/circabc';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { SaveAsService } from 'app/core/save-as.service';
import { ConfirmDialogComponent } from 'app/shared/confirm-dialog/confirm-dialog.component';
import { SizePipe } from 'app/shared/pipes/size.pipe';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';
import { environment } from 'environments/environment';
import { firstValueFrom } from 'rxjs';

/**
 * Modal component that lets users import a ZIP archive into a target library
 * node of an interest group.
 *
 * The template renders an upload dialog with drag-and-drop support, a file
 * picker, import options (notify users, delete source file, disable
 * notifications, character encoding), a progress/spinner indicator while the
 * upload is in flight, and a link to download the index-file template.
 *
 * On submission it delegates the upload to {@link InterestGroupService} and
 * reports the outcome to the parent via the {@link modalHide} output. For the
 * `echa` release variant it first prompts the user with a confirmation dialog
 * (via {@link MatDialog} and {@link ConfirmDialogComponent}) before accepting a
 * file. Error messages are surfaced through {@link UiMessageService} and
 * localized with {@link TranslocoService}.
 */
@Component({
  selector: 'cbc-import',
  templateUrl: './import.component.html',
  styleUrl: './import.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule, SpinnerComponent, SizePipe, TranslocoModule],
})
export class ImportComponent implements OnInit {
  /** Material dialog service used to open the confirmation dialog. */
  private readonly dialog = inject(MatDialog);
  /** API client used to post the ZIP file to the backend for import. */
  private readonly interestGroupService = inject(InterestGroupService);
  /** Builder used to construct the reactive import options form. */
  private readonly formBuilder = inject(FormBuilder);
  /** Service used to trigger the browser download of the index template file. */
  private readonly saveAsService = inject(SaveAsService);
  /** Translation service used to localize dialog and error messages. */
  private readonly translateService = inject(TranslocoService);
  /** Service used to display error notifications to the user. */
  private readonly uiMessageService = inject(UiMessageService);

  /** Input controlling whether the import modal is displayed. */
  public readonly showModal = input(false);
  /** Required input identifying the library node the ZIP is imported into. */
  public readonly targetNode = input.required<ModelNode>();
  /**
   * Output emitted when the modal should close, carrying the result of the
   * import action (success, failure or cancellation).
   */
  public readonly modalHide = output<ActionEmitterResult>();

  /** The file currently selected or dropped for import, if any. */
  public readonly fileToUpload = signal<File | undefined>(undefined);
  /** Whether an upload is currently in progress. */
  public readonly uploading = signal(false);
  /** Current value of the upload progress indicator. */
  public progressValue = 0;
  /** Maximum value of the upload progress indicator. */
  public progressMax = 0;

  /** Maximum allowed import file size, expressed in megabytes. */
  public importMaxSize = 20;

  /** Reactive form holding the import options (notify, delete, encoding, ...). */
  public importForm!: FormGroup;

  /** Base URL of the backend API, used to build the template download URL. */
  private readonly basePath!: string;

  /**
   * Resolves the API {@link BASE_PATH} injection token and stores it for later
   * URL construction.
   */
  constructor() {
    const basePath = inject(BASE_PATH);

    if (basePath) {
      this.basePath = basePath;
    }
  }

  /**
   * Angular lifecycle hook. Initializes the import options form with its
   * default values (notify users, delete file, disable notification and the
   * `CP437` character encoding).
   */
  ngOnInit() {
    this.importForm = this.formBuilder.group(
      {
        notifyUser: [true],
        deleteFile: [true],
        disableNotification: [true],
        encoding: ['CP437'],
      },
      {
        updateOn: 'change',
      }
    );
  }

  /**
   * Handles the `dragenter` event, suppressing the browser's default handling
   * so the drop zone can accept files.
   *
   * @param e - The drag event.
   */
  public dragenter(e: DragEvent) {
    e.stopPropagation();
    e.preventDefault();
  }

  /**
   * Handles the `dragover` event, suppressing the browser's default handling so
   * the drop zone can accept files.
   *
   * @param e - The drag event.
   */
  public dragover(e: DragEvent) {
    e.stopPropagation();
    e.preventDefault();
  }

  /**
   * Handles a file `drop` on the upload zone, storing the first dropped file as
   * the file to import.
   *
   * @param e - The drag event containing the dropped files.
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
   * Handles selection of a file through the file input. For the `echa` release
   * the user must first accept a confirmation dialog; if declined, the
   * selection is ignored. Otherwise the first selected file is stored as the
   * file to import.
   *
   * @param event - The change event fired by the file input element.
   * @returns A promise that resolves once the selection has been processed.
   */
  public async fileChangeEvent(event: Event) {
    if (environment.circabcRelease === 'echa') {
      if (!(await this.showDialogConfirmMsg())) {
        return;
      }
    }

    const input = event.target as HTMLInputElement;
    const filesList = input.files as FileList;

    this.fileToUpload.set(filesList[0]);
  }

  /**
   * Uploads the selected ZIP file to the target node using the current form
   * options. Does nothing if no file is selected or the file exceeds the
   * maximum allowed size. On success or failure it emits an
   * {@link ActionEmitterResult} through {@link modalHide}; known backend errors
   * (name conflict, file too large) are translated and shown via
   * {@link UiMessageService}.
   *
   * @returns A promise that resolves once the import attempt has completed.
   */
  public async import() {
    const file = this.fileToUpload();
    if (file === undefined || this.importExceeds(file.size)) {
      return;
    }

    this.uploading.set(true);

    const result: ActionEmitterResult = {};
    result.type = ActionType.IMPORT_ZIP;

    try {
      await this.interestGroupService.postImportZipFileAsync({
        folderId: this.targetNode().id as string,
        notifyUser: this.importForm.controls.notifyUser.value,
        deleteFile: this.importForm.controls.deleteFile.value,
        disableNotification: this.importForm.controls.disableNotification.value,
        encoding: this.importForm.controls.encoding.value,
        fileData: file,
      });

      this.fileToUpload.set(undefined);
      result.result = ActionResult.SUCCEED;

      this.modalHide.emit(result);
    } catch (error) {
      result.result = ActionResult.FAILED;
      result.type = ActionType.IMPORT_ZIP;
      let res = '';
      if (
        error.error.message.includes('A file with this name already exists')
      ) {
        res = this.translateService.translate('import.zip.exists.failed');
      } else if (
        error.error.message.includes('File is too big to be imported')
      ) {
        res = this.translateService.translate('import.zip.big.failed');
      }
      if (res !== '') {
        this.uiMessageService.addErrorMessage(res);
      }
      this.modalHide.emit(result);
    } finally {
      this.uploading.set(false);
    }
  }

  /**
   * Determines whether a given file size exceeds the configured import limit.
   *
   * @param size - The file size in bytes.
   * @returns `true` if the size is larger than {@link importMaxSize} megabytes.
   */
  public importExceeds(size: number): boolean {
    return size > this.importMaxSize * 1024 * 1024;
  }

  /**
   * Cancels the import flow. When invoked with `'close'` it clears the selected
   * file and emits a cancellation result through {@link modalHide}.
   *
   * @param backTo - Action identifier; the modal closes only when this equals
   * `'close'`.
   */
  public cancel(backTo: string) {
    if (backTo === 'close') {
      this.fileToUpload.set(undefined);
      const result: ActionEmitterResult = {};
      result.result = ActionResult.CANCELED;
      result.type = ActionType.IMPORT_ZIP;
      this.modalHide.emit(result);
    }
  }

  /**
   * Triggers a download of the ZIP import index-file template (`index.txt`)
   * from the backend.
   *
   * @returns `false` to prevent default anchor navigation when used as a click
   * handler.
   */
  public getImportIndexFileTemplate() {
    const url = `${this.basePath}/groups/import/template`;
    this.saveAsService.saveUrlAs(url, 'index.txt');
    return false;
  }

  /**
   * Opens the SNC import confirmation dialog (used for the `echa` release) and
   * waits for the user's response.
   *
   * @returns A promise resolving to the dialog result: a truthy value when the
   * user confirms and a falsy value when they dismiss it.
   */
  private async showDialogConfirmMsg() {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      ariaLabel: 'Dialog',
      data: {
        messageTranslated: this.translateService.translate(
          'label.dialog.alert.snc.import',
          {
            link: `<a href="https://ec.europa.eu/transparency/documents-register/detail?ref=C(2019)1904&lang=en" target="_blank">C(2019)1904</a>`,
          }
        ),
        labelOK: 'label.confirm',
        title: 'label.dialog.alert.snc.import.title',
        layoutStyle: 'SNCNotification',
      },
    });

    return firstValueFrom(dialogRef.afterClosed());
  }
}
