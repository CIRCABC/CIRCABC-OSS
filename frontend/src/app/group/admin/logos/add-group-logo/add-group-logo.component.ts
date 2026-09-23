import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  model,
  OnInit,
  output,
  signal,
} from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result/index';
import { InterestGroupService } from 'app/core/generated/circabc';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { imageExtensionValid } from 'app/core/util';
import { ModalComponent } from 'app/shared/modal/modal.component';

/**
 * Modal component that lets an administrator upload a logo image for an
 * interest group.
 *
 * It renders a modal dialog (via {@link ModalComponent}) containing a file
 * input backed by a reactive form. The selected image is validated for
 * extension and size before being sent to the backend through
 * {@link InterestGroupService.postGroupNewLogo}. On completion the component
 * emits an {@link ActionEmitterResult} through the {@link modalHide} output so
 * the parent can react to success or failure and close the modal.
 *
 * Key collaborators:
 * - {@link FormBuilder} to build the reactive logo form.
 * - {@link InterestGroupService} to perform the logo upload API call.
 * - {@link UiMessageService} to surface upload error notifications.
 * - {@link TranslocoService} to translate error messages.
 */
@Component({
  selector: 'cbc-add-group-logo',
  templateUrl: './add-group-logo.component.html',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ModalComponent, ReactiveFormsModule, TranslocoModule],
})
export class AddGroupLogoComponent implements OnInit {
  /** Reactive forms builder used to construct {@link logoForm}. */
  private readonly fb = inject(FormBuilder);
  /** Translation service used to resolve localized error messages. */
  private readonly translateService = inject(TranslocoService);
  /** Service used to display informational/error messages to the user. */
  private readonly uiMessageService = inject(UiMessageService);
  /** Generated API client used to upload the group logo. */
  private readonly groupService = inject(InterestGroupService);

  /**
   * Input: identifier of the interest group the logo will be attached to.
   * When undefined the upload is skipped.
   */
  readonly groupId = input<string>();
  /**
   * Two-way bindable model controlling the visibility of the modal dialog.
   */
  showModal = model(false);
  /**
   * Output: emitted when the modal should close, carrying the outcome of the
   * cancel or upload operation as an {@link ActionEmitterResult}.
   */
  readonly modalHide = output<ActionEmitterResult>();

  /** Whether an upload request is currently in progress. */
  public readonly processing = signal(false);
  /** Reactive form holding the selected file control. */
  public logoForm!: FormGroup;
  /** The list of files currently staged for upload (only the first is used). */
  public readonly filesToUpload = signal<File[]>([]);

  /**
   * Angular lifecycle hook. Initializes {@link logoForm} with a single
   * `file` control.
   */
  ngOnInit() {
    this.logoForm = this.fb.group({
      file: [],
    });
  }

  /**
   * Cancels the logo upload: clears any staged files and emits an empty
   * {@link ActionEmitterResult} through {@link modalHide} to close the modal.
   */
  public cancel() {
    this.filesToUpload.set([]);
    const res: ActionEmitterResult = {};
    this.modalHide.emit(res);
  }

  /**
   * Uploads the first staged file as the group's new logo.
   *
   * Does nothing when {@link groupId} is undefined. On success the staged
   * files are cleared; on failure a localized error message is shown via
   * {@link UiMessageService}. In both cases an {@link ActionEmitterResult}
   * describing the outcome is emitted through {@link modalHide}.
   *
   * @returns A promise that resolves once the upload attempt completes and the
   * result has been emitted.
   */
  public async uploadLogo() {
    const groupId = this.groupId();
    if (groupId === undefined) {
      return;
    }
    this.processing.set(true);
    const res: ActionEmitterResult = {};
    res.type = ActionType.ADD_CATEGORY_LOGO;

    try {
      await this.groupService.postGroupNewLogoAsync({
        id: groupId,
        fileData: this.filesToUpload()[0],
      });

      res.result = ActionResult.SUCCEED;
      this.filesToUpload.set([]);
    } catch (error) {
      console.error(error);
      const result = this.translateService.translate('error.image.upload');
      this.uiMessageService.addInfoMessage(result);
      res.result = ActionResult.FAILED;
    }

    this.modalHide.emit(res);
    this.processing.set(false);
  }

  /**
   * Handles the native file input `change` event by extracting the selected
   * files and staging them for upload.
   *
   * @param event The DOM change event originating from the file input element.
   */
  public fileChangeEvent(event: Event) {
    const input = event.target as HTMLInputElement;
    const filesList = input.files as FileList;
    this.handleFiles(filesList);
  }

  /**
   * Replaces {@link filesToUpload} with the files contained in the given list.
   *
   * @param filesList The list of files selected by the user.
   */
  private handleFiles(filesList: FileList) {
    const files: File[] = [];
    for (let i = 0; i < filesList.length; i += 1) {
      const fileItem = filesList.item(i);
      if (fileItem) {
        files.push(fileItem);
      }
    }
    this.filesToUpload.set(files);
  }

  /**
   * Returns the name of the first staged file.
   *
   * @returns The file name, or an empty string when no file is staged.
   */
  public getFileName() {
    const files = this.filesToUpload();
    if (files.length > 0) {
      return files[0].name;
    }
    return '';
  }

  /**
   * Indicates whether at least one file is staged for upload.
   *
   * @returns `true` when a file has been selected, otherwise `false`.
   */
  public hasFile(): boolean {
    return this.filesToUpload().length > 0;
  }

  /**
   * Validates the staged file as an acceptable logo image.
   *
   * The file must exist, have a valid image extension (see
   * {@link imageExtensionValid}) and be at most 1 MB in size.
   *
   * @returns `true` when the staged file is a valid image, otherwise `false`.
   */
  public isValidImage(): boolean {
    const files = this.filesToUpload();
    return (
      files.length > 0 &&
      imageExtensionValid(files[0].name) &&
      files[0].size <= 1 * 1024 * 1024
    );
  }
}
