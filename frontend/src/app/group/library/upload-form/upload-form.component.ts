import { NgClass } from '@angular/common';
import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  computed,
  inject,
  resource,
} from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import {
  FileService,
  NodesService,
  PermissionDefinition,
  PermissionService,
  TranslationsService,
} from 'app/core/generated/circabc';
import { FileInputComponent } from 'app/group/library/upload-form/file-input/file-input.component';
import { FileListComponent } from 'app/group/library/upload-form/file-list/file-list.component';
import { FileMetadataComponent } from 'app/group/library/upload-form/file-metadata/file-metadata.component';
import { FileUploadItem } from 'app/group/library/upload-form/file-upload-item';
import { ConfirmDialogComponent } from 'app/shared/confirm-dialog/confirm-dialog.component';
import { DataCyDirective } from 'app/shared/directives/data-cy.directive';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';
import { environment } from 'environments/environment';

/**
 * Multi-file upload form for a document library node.
 *
 * Renders the complete upload workflow inside a group library: a file input
 * ({@link FileInputComponent}), the list of selected files
 * ({@link FileListComponent}), the metadata editor for the currently selected
 * file ({@link FileMetadataComponent}), a "notify members" toggle and the
 * upload progress / result state.
 *
 * The component supports advanced scenarios such as marking files as pivot
 * documents and attaching translations to those pivots, per-file security
 * ranking, and (for the `echa` release) cutting permission inheritance on
 * sensitive documents and showing a mandatory SNC notification dialog.
 *
 * Key collaborators (injected generated API services):
 * - {@link FileService} — uploads files and fires new-content notifications.
 * - {@link TranslationsService} — uploads translations linked to pivot files.
 * - {@link NodesService} — resolves the target library node.
 * - {@link PermissionService} — cuts inheritance for sensitive documents.
 * - {@link PermissionService}, {@link ActivatedRoute}, {@link Router},
 *   {@link MatDialog} and {@link TranslocoService} for navigation, dialogs
 *   and i18n.
 *
 * The target library node id is read from the `nodeId` route parameter.
 */
@Component({
  selector: 'cbc-upload-form',
  templateUrl: './upload-form.component.html',
  styleUrl: './upload-form.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    FileInputComponent,
    NgClass,
    FileListComponent,
    FileMetadataComponent,
    MatSlideToggleModule,
    ReactiveFormsModule,
    DataCyDirective,
    SpinnerComponent,
    TranslocoModule,
  ],
})
export class UploadFormComponent {
  private readonly permissionService = inject(PermissionService);
  private readonly fileService = inject(FileService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly translationsService = inject(TranslationsService);
  private readonly nodesService = inject(NodesService);
  private readonly dialog = inject(MatDialog);
  private readonly translateService = inject(TranslocoService);
  private readonly changeDetectorRef = inject(ChangeDetectorRef);

  /** All files queued for upload, including pivots and translations. */
  public filesToUpload: FileUploadItem[] = [];
  /** Subset of {@link filesToUpload} flagged as pivot documents. */
  public pivotDocuments: FileUploadItem[] = [];
  /** Subset of {@link filesToUpload} flagged as translations of a pivot. */
  public translations: FileUploadItem[] = [];
  /** The file currently selected in the list and shown in the metadata editor. */
  public fileSelected: FileUploadItem | undefined;
  /** The current route params, as a signal. */
  private readonly routeParams = toSignal(this.route.params);
  /** Id of the target library node the files are uploaded into (from the route). */
  public readonly targetNodeId = computed(
    () => this.routeParams()?.nodeId ?? ''
  );
  /**
   * Resource loading the target library node identified by {@link targetNodeId}.
   * Idle (loader not called) while the target node id is not yet known.
   */
  private readonly targetNodeResource = resource({
    params: () => this.targetNodeId() || undefined,
    loader: ({ params: id }) => this.nodesService.getNodeAsync({ id }),
  });
  /** The resolved target library node, loaded via {@link targetNodeResource}. */
  public readonly targetNode = this.targetNodeResource.value;
  /** Name of the file currently being uploaded (for progress display). */
  public uploadingFileName: string | undefined;
  /** Number of files processed so far; compared against total to detect completion. */
  public uploadingProgress = 0;
  /** Whether an upload is currently in progress. */
  public uploading = false;
  /** Whether the current upload batch has finished. */
  public uploadFinished = false;
  /** Whether the user canceled/closed the form. */
  public canceled = false;
  /** Maximum allowed file size in bytes (300MB). */
  public maxFileSize = 1024 * 1024 * 300; // 300MB

  /** Toggle controlling whether members are notified after a successful upload. */
  public notify = new FormControl(true);

  /** Permission definition used when cutting inheritance on sensitive files. */
  public perms!: PermissionDefinition;

  /**
   * Adds newly picked files to the upload selection, skipping any whose name
   * already exists in the current selection, and selects the last added file.
   * For the `echa` release, also shows the mandatory SNC notification dialog.
   *
   * @param files The files chosen by the user to add to the queue.
   */
  public addNewFiles(files: FileUploadItem[]) {
    for (const file of files) {
      if (!this.isFileInSelection(file)) {
        this.filesToUpload.push(file);
        this.fileSelected = file;
      }
    }
    if (environment.circabcRelease === 'echa') {
      this.showDialogSimpleMsg();
    }
  }

  /**
   * Checks whether a file with the same name is already in the selection.
   *
   * @param file The file to test.
   * @returns `true` if a file with the same name is already queued.
   */
  public isFileInSelection(file: FileUploadItem) {
    return this.filesToUpload.some((fileUploadItem) => {
      return file.name === fileUploadItem.name;
    });
  }

  /**
   * Replaces the current file selection with the given list and updates
   * {@link fileSelected} to the file flagged as selected, if any.
   *
   * @param files The new full list of files to upload.
   */
  public fileListChanged(files: FileUploadItem[]) {
    this.fileSelected = undefined;
    this.filesToUpload = files;
    this.filesToUpload.forEach((file) => {
      if (file.selected) {
        this.fileSelected = file;
      }
    });
  }

  /**
   * Applies an updated file item back into the selection and recomputes the
   * derived pivot/translation lists. If the file was demoted from a pivot,
   * its former translations are detached; otherwise the translation list is
   * refreshed.
   *
   * @param file The updated file item (matched by its `id`).
   */
  public propagateFileChange(file: FileUploadItem) {
    const idx = this.filesToUpload.findIndex((fileItem) => {
      return fileItem.id === file.id;
    });

    const wasPivot = this.filesToUpload[idx].isPivot && !file.isPivot;

    if (idx) {
      this.filesToUpload[idx] = file;
    }

    this.preparePivotDocuments();

    if (wasPivot) {
      this.removePivotTranslations(file);
    } else {
      this.prepareTranslations();
    }
  }

  /**
   * Uploads all non-translation files (regular files and pivots), skipping and
   * flagging as errored any file exceeding {@link maxFileSize}. Once the last
   * pivot has been uploaded, triggers {@link uploadTranslations} so that
   * translations can be linked to their now-created pivot node references.
   *
   * @returns A promise that resolves when all files have been processed.
   */
  private async uploadFilesOrPivot() {
    const nbPivot = this.filesToUpload.filter((f) => f.isPivot).length;
    let currentPivot = 0;

    for (const fileUpload of this.filesToUpload) {
      if (fileUpload.isTranslation) continue;

      if (fileUpload.file.size > this.maxFileSize) {
        fileUpload.uploadStatus = 'error';
        this.verifyEndUpload();
        continue;
      }

      await this.uploadSingleFile(fileUpload);

      if (fileUpload.isPivot) {
        currentPivot++;
        if (currentPivot === nbPivot) {
          this.uploadTranslations();
        }
      }
    }
  }

  /**
   * Uploads a single file, updating its status through the upload lifecycle,
   * storing the resulting node reference, and (for sensitive files on `echa`)
   * cutting permission inheritance. Errors are routed to {@link onError}.
   *
   * @param fileUpload The file to upload.
   * @returns A promise that resolves once the upload attempt completes.
   */
  private async uploadSingleFile(fileUpload: FileUploadItem) {
    fileUpload.uploadStatus = 'uploading';
    // OnPush: per-file status is mutated across async awaits; mark for check.
    this.changeDetectorRef.markForCheck();

    try {
      const end = await this.performUpload(fileUpload);
      fileUpload.uploadStatus = 'finished';
      fileUpload.nodeRef = end.nodeRef;
      this.verifyEndUpload();

      await this.handleSecurityCutInheritance(fileUpload);
    } catch (error) {
      this.onError(error, fileUpload);
    }
  }

  /**
   * Performs the actual upload API call for a file, serializing the expiration
   * date and forwarding all metadata and dynamic properties.
   *
   * @param fileUpload The file whose content and metadata are uploaded.
   * @returns A promise resolving to the created node (including its nodeRef).
   */
  private async performUpload(fileUpload: FileUploadItem) {
    const expirationDate = JSON.stringify(fileUpload.expirationDate);

    return await this.fileService.uploadFileAsync({
      id: this.targetNodeId(),
      name: fileUpload.name,
      title: fileUpload.title,
      description: fileUpload.description,
      keywords: fileUpload.keywords,
      author: fileUpload.author,
      reference: fileUpload.reference,
      expirationDate: expirationDate
        ? expirationDate.replace('"', '')
        : undefined,
      securityRanking: fileUpload.securityRanking,
      status: fileUpload.status,
      isPivot: fileUpload.isPivot,
      lang: fileUpload.lang,
      dynamicProperties: this.getDynProp(fileUpload),
      fileName: fileUpload.file,
    });
  }

  /**
   * For the `echa` release, cuts permission inheritance on an uploaded file
   * when its security ranking is `SENSITIVE` or `SPECIAL_HANDLING`.
   *
   * @param fileUpload The uploaded file to evaluate and, if needed, isolate.
   * @returns A promise that resolves once inheritance handling is complete.
   */
  private async handleSecurityCutInheritance(fileUpload: FileUploadItem) {
    const isSensitive =
      fileUpload.securityRanking === 'SENSITIVE' ||
      fileUpload.securityRanking === 'SPECIAL_HANDLING';

    if (
      isSensitive &&
      environment.circabcRelease === 'echa' &&
      fileUpload.nodeRef
    ) {
      await this.cutInheritance(fileUpload.nodeRef);
    }
  }

  /**
   * Handles an upload failure by logging it, marking the file as errored and
   * advancing the progress counter.
   *
   * @param error The error thrown during upload.
   * @param fileUpload The file whose upload failed.
   */
  private onError(error: unknown, fileUpload: FileUploadItem) {
    console.error(error);
    fileUpload.uploadStatus = 'error';
    this.uploadingProgress = this.uploadingProgress + 1;
    // OnPush: called from async upload continuations; mark for check.
    this.changeDetectorRef.markForCheck();
  }

  /**
   * Increments the progress counter and, when every queued file has been
   * processed, marks the batch as finished and fires member notifications if
   * the {@link notify} toggle is enabled.
   */
  private verifyEndUpload() {
    this.uploadingProgress = this.uploadingProgress + 1;
    if (this.uploadingProgress === this.filesToUpload.length) {
      this.uploadFinished = true;
      this.uploading = false;
      if (this.notify.value) {
        this.fireNotifications();
      }
    }
    // OnPush: progress/finished state advances from async upload callbacks.
    this.changeDetectorRef.markForCheck();
  }

  /**
   * Uploads all translation files, linking each to its pivot's node reference
   * via {@link TranslationsService}. Translations exceeding {@link maxFileSize}
   * are flagged as errored. Upload errors are routed to {@link onError}.
   *
   * @returns A promise that resolves once all translations are processed.
   */
  private async uploadTranslations() {
    // proceed with translations
    for (const fileUpload of this.filesToUpload) {
      if (
        fileUpload.file.size <= this.maxFileSize &&
        fileUpload.isTranslation &&
        fileUpload.lang &&
        fileUpload.translationOf
      ) {
        fileUpload.uploadStatus = 'uploading';
        // OnPush: status mutated across async awaits; mark for check.
        this.changeDetectorRef.markForCheck();
        const nodeRef = this.getNodeRefOfPivot(fileUpload.translationOf);
        const expirationDate = JSON.stringify(fileUpload.expirationDate);
        try {
          await this.translationsService.postTranslationEnhancedAsync({
            id: nodeRef,
            notify: false,
            name: fileUpload.name,
            title: fileUpload.title,
            description: fileUpload.description,
            keywords: fileUpload.keywords,
            author: fileUpload.author,
            reference: fileUpload.reference,
            expirationDate: expirationDate
              ? expirationDate.replace('"', '')
              : undefined,
            securityRanking: fileUpload.securityRanking,
            status: fileUpload.status,
            lang: fileUpload.lang,
            dynamicProperties: this.getDynProp(fileUpload),
            file: fileUpload.file,
          });

          fileUpload.uploadStatus = 'finished';
          this.verifyEndUpload();
        } catch (error) {
          this.onError(error, fileUpload);
        }
      } else if (
        fileUpload.file.size > this.maxFileSize &&
        fileUpload.isTranslation
      ) {
        fileUpload.uploadStatus = 'error';
        this.verifyEndUpload();
      }
    }
  }

  /**
   * Starts the upload of the current selection by flipping the {@link uploading}
   * flag and delegating to {@link uploadFilesOrPivot}.
   */
  public uploadFiles() {
    this.uploading = true;
    this.uploadFilesOrPivot();
  }

  /**
   * Resets the form to its initial empty state so a new upload batch can be
   * started, clearing progress, the finished flag and the file selection.
   */
  public startNewUpload() {
    this.uploadingProgress = 0;
    this.uploadFinished = false;
    this.filesToUpload = [];
    this.fileSelected = undefined;
  }

  /** Recomputes {@link pivotDocuments} from the files flagged as pivots. */
  public preparePivotDocuments() {
    this.pivotDocuments = this.filesToUpload.filter((file) => {
      return file.isPivot;
    });
  }

  /**
   * Detaches all translations linked to the given (former) pivot file by
   * clearing their translation flag and language, then refreshes the
   * translation list.
   *
   * @param file The pivot file whose translations should be detached.
   */
  public removePivotTranslations(file: FileUploadItem) {
    this.filesToUpload.forEach((fileUpload) => {
      if (fileUpload.isTranslation && fileUpload.translationOf === file.id) {
        fileUpload.isTranslation = false;
        fileUpload.lang = '';
      }
    });

    this.prepareTranslations();
  }

  /** Recomputes {@link translations} from the files flagged as translations. */
  public prepareTranslations() {
    this.translations = this.filesToUpload.filter((file) => {
      return file.isTranslation;
    });
  }

  /**
   * Resolves the uploaded node reference of a pivot file by its id.
   *
   * @param pivotId The id of the pivot file.
   * @returns The pivot's node reference, or an empty string if not found or
   * not yet uploaded.
   */
  public getNodeRefOfPivot(pivotId: string | undefined): string {
    const res = this.filesToUpload.find((file) => {
      return file.id === pivotId;
    });

    if (res?.nodeRef) {
      return res.nodeRef;
    }
    return '';
  }

  /**
   * Fires a "new content" notification to group members for every successfully
   * uploaded file. Errors are logged and swallowed.
   *
   * @returns A promise that resolves once the notification call completes.
   */
  private async fireNotifications() {
    const nodeRefs: string[] = [];
    for (const file of this.filesToUpload) {
      if (file.nodeRef && file.uploadStatus === 'finished') {
        nodeRefs.push(file.nodeRef);
      }
    }
    try {
      await this.fileService.fireNewContentNotificationAsync({
        id: this.targetNodeId(),
        requestBody: nodeRefs,
      });
    } catch (error) {
      console.error(error);
    }
  }

  /**
   * Extracts the dynamic property values from a file item, i.e. all keys whose
   * name contains `dynAttr`, into a plain string map.
   *
   * @param file The file whose dynamic properties are collected.
   * @returns A map of dynamic property keys to their string values.
   */
  private getDynProp(file: FileUploadItem): { [key: string]: string } {
    const keys = Object.keys(file);
    const result: { [key: string]: string } = {};
    for (const key of keys) {
      if (key.includes('dynAttr')) {
        result[key] = file[key];
      }
    }
    return result;
  }

  /**
   * Cancels/closes the form and navigates back to the parent route relative to
   * the current library node.
   */
  public cancelOrClose() {
    this.canceled = true;
    this.router.navigate(['..'], { relativeTo: this.route });
  }

  /**
   * Opens the mandatory SNC (sensitive non-classified) notification dialog
   * shown on the `echa` release, including the link to Commission decision
   * C(2019)1904.
   */
  private showDialogSimpleMsg() {
    this.dialog.open(ConfirmDialogComponent, {
      ariaLabel: 'Dialog',
      data: {
        messageTranslated: this.translateService.translate(
          'label.dialog.alert.snc.upload',
          {
            link: `<a href="https://ec.europa.eu/transparency/documents-register/detail?ref=C(2019)1904&lang=en" target="_blank">C(2019)1904</a>`,
          }
        ),
        labelOK: 'label.confirm',
        title: 'label.dialog.alert.snc.upload.title',
        layoutStyle: 'SNCNotification',
      },
    });
  }

  /**
   * Removes permission inheritance from a node by writing a permission
   * definition with `inherited: false` and no explicit permissions.
   *
   * @param nodeRef The node reference to isolate.
   * @returns A promise that resolves once the permission update completes.
   */
  private async cutInheritance(nodeRef: string) {
    const body: PermissionDefinition = {
      inherited: false,
      permissions: {},
    };

    await this.permissionService.putPermissionAsync({
      id: nodeRef,
      permissionDefinition: body,
    });
  }

  /**
   * Determines whether any queued file has an empty or invalid name.
   *
   * @returns `true` if at least one file has a blank name or fails
   * {@link nameValidator}.
   */
  anyInvalidName(): boolean {
    return this.filesToUpload.some((file) => {
      if (
        file.name === '' ||
        file.name === undefined ||
        file.name === null ||
        file.name.trim() === ''
      ) {
        return true;
      }
      return this.nameValidator(file.name) !== null;
    });
  }

  /**
   * Determines whether any queued file exceeds {@link maxFileSize}.
   *
   * @returns `true` if at least one file is too large.
   */
  anyFileTooLarge(): boolean {
    return this.filesToUpload.some((file) => file.file.size > this.maxFileSize);
  }

  /**
   * Validates a proposed file name against CIRCABC naming rules, rejecting
   * empty/whitespace-only names and names containing forbidden characters
   * (`" * \ < > ? / : |`), trailing dots or trailing spaces.
   *
   * @param value The file name to validate.
   * @returns `null` if the name is valid, otherwise a validation error object
   * with an `invalidFileName` key describing the problem.
   */
  nameValidator(value: string) {
    if (value === null) {
      return null;
    }
    if (value === undefined || value.trim().length === 0) {
      return { invalidFileName: { additionalInfo: 'empty name' } };
    }
    // prettier-ignore
    if (/(.*["*\\><?/:|]+.*)|(.*[.]?.*[.]+$)|(.*[ ]+$)/.exec(value)) { // NOSONAR
      return {
        invalidFileName: { additionalInfo: String.raw` " * \ < > ? / : |` },
      };
    }
    return null;
  }
}
