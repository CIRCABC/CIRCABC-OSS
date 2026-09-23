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
import { CategoryService } from 'app/core/generated/circabc';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { imageExtensionValid } from 'app/core/util';
import { ModalComponent } from 'app/shared/modal/modal.component';

/**
 * Modal component that lets an administrator upload a logo image for a
 * category.
 *
 * The component renders a modal dialog (via {@link ModalComponent}) containing
 * a reactive form with a single file input. The selected file is validated for
 * an allowed image extension and a maximum size of 1 MB before it is uploaded
 * to the backend through {@link CategoryService}. Upload success/failure is
 * reported back to the parent component through the {@link modalHide} output,
 * and user-facing error feedback is surfaced via {@link UiMessageService}.
 *
 * Key collaborators:
 * - {@link CategoryService}: performs the actual logo upload REST call.
 * - {@link UiMessageService}: displays error messages to the user.
 * - {@link TranslocoService}: translates the error message text.
 */
@Component({
  selector: 'cbc-add-category-logo',
  templateUrl: './add-category-logo.component.html',
  styleUrl: './add-category-logo.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ModalComponent, ReactiveFormsModule, TranslocoModule],
})
export class AddCategoryLogoComponent implements OnInit {
  /** Angular reactive forms builder used to construct {@link logoForm}. */
  private readonly fb = inject(FormBuilder);
  /** Transloco service used to translate user-facing error messages. */
  private readonly translateService = inject(TranslocoService);
  /** Service used to display info/error messages to the user. */
  private readonly uiMessageService = inject(UiMessageService);
  /** Generated API client used to upload the category logo. */
  private readonly categoryService = inject(CategoryService);

  /**
   * Required input: identifier of the category whose logo is being uploaded.
   */
  readonly categoryId = input.required<string>();
  /**
   * Required two-way bound model controlling the visibility of the modal.
   * `true` shows the dialog, `false` hides it.
   */
  showModal = model.required<boolean>();

  /**
   * Output emitted when the modal should close, carrying the outcome of the
   * cancel or upload action as an {@link ActionEmitterResult}.
   */
  readonly modalHide = output<ActionEmitterResult>();

  /** Whether an upload is currently in progress (used to disable UI/actions). */
  public readonly processing = signal(false);
  /** Reactive form holding the selected file control. */
  public logoForm!: FormGroup;
  /** The files currently selected for upload; only the first is used. */
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
   * Clears the selected files and closes the modal without uploading,
   * emitting an empty {@link ActionEmitterResult} through {@link modalHide}.
   */
  public cancel() {
    this.filesToUpload.set([]);
    const res: ActionEmitterResult = {};
    this.modalHide.emit(res);
  }

  /**
   * Uploads the first selected file as the category logo.
   *
   * Sets {@link processing} while the request is in flight, calls
   * {@link CategoryService.postCategoryLogoByCategoryId} and, on success,
   * clears the selected files. On failure, a translated error message is
   * displayed via {@link UiMessageService}. In both cases a
   * {@link ActionEmitterResult} (type `ADD_CATEGORY_LOGO`) reflecting the
   * outcome is emitted through {@link modalHide}.
   *
   * @returns A promise that resolves once the upload attempt has completed
   * and the result has been emitted.
   */
  public async uploadLogo() {
    this.processing.set(true);
    const res: ActionEmitterResult = {};
    res.type = ActionType.ADD_CATEGORY_LOGO;

    try {
      await this.categoryService.postCategoryLogoByCategoryIdAsync({
        id: this.categoryId(),
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
   * Handles the file input `change` event, extracting the selected files.
   *
   * @param event The DOM change event fired by the file `<input>` element.
   */
  public fileChangeEvent(event: Event) {
    const input = event.target as HTMLInputElement;
    const filesList = input.files as FileList;
    this.handleFiles(filesList);
  }

  /**
   * Replaces {@link filesToUpload} with the files from the given list.
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
   * Returns the name of the first selected file.
   *
   * @returns The file name, or an empty string if no file is selected.
   */
  public getFileName() {
    const files = this.filesToUpload();
    if (files.length > 0) {
      return files[0].name;
    }
    return '';
  }

  /**
   * Indicates whether at least one file has been selected.
   *
   * @returns `true` if a file is selected, otherwise `false`.
   */
  public hasFile(): boolean {
    return this.filesToUpload().length > 0;
  }

  /**
   * Validates the currently selected file as an acceptable logo image.
   *
   * A file is considered valid when it exists, has an allowed image
   * extension (see {@link imageExtensionValid}) and does not exceed 1 MB.
   *
   * @returns `true` if the selected file is a valid image within the size
   * limit, otherwise `false`.
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
