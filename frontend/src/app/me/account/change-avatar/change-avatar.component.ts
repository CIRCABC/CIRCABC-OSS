import {
  ChangeDetectionStrategy,
  Component,
  inject,
  model,
  OnChanges,
  OnInit,
  output,
  SimpleChanges,
  signal,
} from '@angular/core';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { User } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { UploadService } from 'app/core/upload.service';
import { imageExtensionValid } from 'app/core/util';
import { SizePipe } from 'app/shared/pipes/size.pipe';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';

/**
 * Standalone Angular component that renders the "change avatar" wizard used in
 * the user's personal account area.
 *
 * The component displays a two-step modal wizard: the first step lets the user
 * select an image file (via a file input or drag-and-drop) and validates it,
 * while the second step uploads the selected file as the current user's avatar
 * and shows upload progress through a spinner.
 *
 * Key collaborators:
 * - {@link UploadService} performs the actual avatar upload.
 * - {@link LoginService} provides the currently authenticated user.
 * - {@link UiMessageService} surfaces error notifications to the user.
 * - {@link TranslocoService} resolves localized error messages.
 */
@Component({
  selector: 'cbc-change-avatar',
  templateUrl: './change-avatar.component.html',
  styleUrl: './change-avatar.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [SpinnerComponent, SizePipe, TranslocoModule],
})
export class ChangeAvatarComponent implements OnInit, OnChanges {
  /** Service used to resolve localized text (e.g. upload error messages). */
  private readonly translateService = inject(TranslocoService);
  /** Service used to display UI-level error notifications. */
  private readonly uiMessageService = inject(UiMessageService);
  /** Service that performs the avatar upload request against the backend. */
  private readonly uploadService = inject(UploadService);
  /** Service providing access to the currently authenticated user. */
  private readonly loginService = inject(LoginService);

  /**
   * Two-way bindable model controlling whether the wizard is visible.
   * When set to `false` the wizard is closed and its steps are reset.
   */
  public readonly showWizard = model(false);
  /**
   * Emits when the modal wizard should be hidden, carrying the outcome
   * (cancelled or succeeded) of the interaction.
   */
  public readonly modalHide = output<ActionEmitterResult>();
  /**
   * Emits when an avatar has been successfully uploaded, allowing parent
   * components to react (e.g. refresh the displayed avatar).
   */
  public readonly avatarUploaded = output<ActionEmitterResult>();

  /** Whether the first wizard step (file selection) is currently shown. */
  public showAddWizardStep1 = false;
  /** Whether the second wizard step (upload/progress) is currently shown. */
  public showAddWizardStep2 = false;

  /** The image file selected by the user, or `undefined` when none is chosen. */
  public fileToUpload = signal<File | undefined>(undefined);
  /** Current upload progress value, used to drive the progress indicator. */
  public progressValue = signal(0);
  /** Maximum upload progress value, used to drive the progress indicator. */
  public progressMax = 0;

  /** The currently authenticated user whose avatar is being changed. */
  private user!: User;

  /**
   * Angular lifecycle hook. Lazily initializes the current {@link User} from
   * the {@link LoginService} if it has not already been set.
   */
  ngOnInit() {
    this.user ??= this.loginService.getUser();
  }

  /**
   * Angular lifecycle hook. Reflects changes to the `showWizard` model onto the
   * first wizard step so the file-selection step opens when the wizard is shown.
   *
   * @param changes The set of input/model changes detected by Angular.
   */
  ngOnChanges(changes: SimpleChanges) {
    if (changes.showWizard) {
      this.showAddWizardStep1 = changes.showWizard.currentValue;
    }
  }

  /**
   * Drag-and-drop handler for the `dragenter` event that suppresses the
   * browser's default handling so the drop zone can accept files.
   *
   * @param e The drag event.
   */
  public dragenter(e: DragEvent) {
    e.stopPropagation();
    e.preventDefault();
  }

  /**
   * Drag-and-drop handler for the `dragover` event that suppresses the
   * browser's default handling so the drop zone can accept files.
   *
   * @param e The drag event.
   */
  public dragover(e: DragEvent) {
    e.stopPropagation();
    e.preventDefault();
  }

  /**
   * Drag-and-drop handler for the `drop` event. Prevents the browser's default
   * handling and stores the first dropped file as the file to upload.
   *
   * @param e The drag event carrying the dropped file(s).
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
   * Handles selection of a file through the native file input, storing the
   * first chosen file as the file to upload.
   *
   * @param event The change event emitted by the `<input type="file">` element.
   */
  public fileChangeEvent(event: Event) {
    const input = event.target as HTMLInputElement;
    const filesList = input.files as FileList;

    this.fileToUpload.set(filesList[0]);
  }

  /**
   * Opens the first wizard step (file selection).
   */
  public launchAddWizardStep1() {
    this.showAddWizardStep1 = true;
  }

  /**
   * Advances from the first to the second wizard step and starts the upload.
   *
   * If the currently selected file is invalid (see {@link fileNameValid}) the
   * method returns early without changing steps.
   *
   * @returns A promise that resolves once the upload attempt has completed.
   */
  public async launchAddWizardStep2() {
    if (!this.fileNameValid()) {
      return;
    }
    this.showAddWizardStep1 = false;
    this.showAddWizardStep2 = true;
    await this.uploadFile();
  }

  /**
   * Uploads the currently selected file as the user's avatar, updating the
   * progress fields and emitting {@link avatarUploaded} on success.
   *
   * On failure a localized error message is shown via {@link UiMessageService}
   * and the wizard is closed. Does nothing if no file is selected.
   *
   * @returns A promise that resolves once the upload attempt has completed.
   */
  private async uploadFile() {
    const fileToUpload = this.fileToUpload();
    if (!fileToUpload) {
      return;
    }
    this.progressMax = 1;
    this.progressValue.set(0);
    try {
      await this.uploadService.updateAvatar(
        this.user.userId as string,
        fileToUpload
      );
      this.avatarUploaded.emit({ result: ActionResult.SUCCEED });
    } catch (error) {
      console.error(error);
      const res = this.translateService.translate('error.image.upload');
      this.uiMessageService.addErrorMessage(res);
      this.cancelWizard('close');
    }
    this.progressValue.set(this.progressValue() + 1);
    this.fileToUpload.set(undefined);
  }

  /**
   * Closes the wizard and emits {@link modalHide} with an outcome that depends
   * on how it was closed.
   *
   * - `'close'`: resets all wizard state (including the selected file) and
   *   emits a {@link ActionResult.CANCELED} result.
   * - `'finish'`: resets the wizard steps and emits a
   *   {@link ActionResult.SUCCEED} result.
   *
   * Any other value is ignored.
   *
   * @param backTo Determines the close behavior; either `'close'` or `'finish'`.
   */
  public cancelWizard(backTo: string) {
    if (backTo === 'close') {
      this.showWizard.set(false);
      this.showAddWizardStep1 = false;
      this.showAddWizardStep2 = false;
      this.fileToUpload.set(undefined);

      const result: ActionEmitterResult = {};
      result.result = ActionResult.CANCELED;
      result.type = ActionType.UPLOAD_FILE;

      this.modalHide.emit(result);
    } else if (backTo === 'finish') {
      this.showWizard.set(false);
      this.showAddWizardStep1 = false;
      this.showAddWizardStep2 = false;

      const result: ActionEmitterResult = {};
      result.result = ActionResult.SUCCEED;
      result.type = ActionType.UPLOAD_FILE;

      this.modalHide.emit(result);
    }
  }

  /**
   * Validates the currently selected file for use as an avatar.
   *
   * A file is considered valid when it has an accepted image extension (see
   * {@link imageExtensionValid}) and its size does not exceed 1&nbsp;MB. When no
   * file is selected the method returns `true`.
   *
   * @returns `true` if there is no selected file or the selected file is a
   * valid image within the size limit; otherwise `false`.
   */
  public fileNameValid(): boolean {
    const fileToUpload = this.fileToUpload();
    if (fileToUpload === undefined) {
      return true;
    }
    return (
      imageExtensionValid(fileToUpload.name) &&
      fileToUpload.size <= 1 * 1024 * 1024
    );
  }
}
