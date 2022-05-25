import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges,
} from '@angular/core';
import { TranslocoService } from '@ngneat/transloco';
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

@Component({
  selector: 'cbc-change-avatar',
  templateUrl: './change-avatar.component.html',
  styleUrls: ['./change-avatar.component.scss'],
  preserveWhitespaces: true,
})
export class ChangeAvatarComponent implements OnInit, OnChanges {
  @Input()
  public showWizard = false;
  @Output()
  public readonly modalHide = new EventEmitter<ActionEmitterResult>();
  @Output()
  public readonly avatarUploaded = new EventEmitter<ActionEmitterResult>();

  public showAddWizardStep1 = false;
  public showAddWizardStep2 = false;

  public fileToUpload!: File | undefined;
  public progressValue = 0;
  public progressMax = 0;

  private user!: User;

  constructor(
    private translateService: TranslocoService,
    private uiMessageService: UiMessageService,
    private uploadService: UploadService,
    private loginService: LoginService
  ) {}

  ngOnInit() {
    if (this.user === undefined) {
      this.user = this.loginService.getUser();
    }
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes.showWizard) {
      this.showAddWizardStep1 = changes.showWizard.currentValue;
    }
  }

  public dragenter(e: DragEvent) {
    e.stopPropagation();
    e.preventDefault();
  }

  public dragover(e: DragEvent) {
    e.stopPropagation();
    e.preventDefault();
  }

  public drop(e: DragEvent) {
    e.stopPropagation();
    e.preventDefault();

    const dt = e.dataTransfer;
    if (dt !== null) {
      const files = dt.files;
      this.fileToUpload = files[0];
    }
  }

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  public fileChangeEvent(fileInput: any) {
    const filesList = fileInput.target.files as FileList;
    this.fileToUpload = filesList[0];
  }

  public launchAddWizardStep1() {
    this.showAddWizardStep1 = true;
  }

  public async launchAddWizardStep2() {
    if (!this.fileNameValid()) {
      return;
    }
    this.showAddWizardStep1 = false;
    this.showAddWizardStep2 = true;
    await this.uploadFile();
  }

  private async uploadFile() {
    if (!this.fileToUpload) {
      return;
    }
    this.progressMax = 1;
    this.progressValue = 0;
    try {
      await this.uploadService.updateAvatar(
        this.user.userId as string,
        this.fileToUpload
      );
      this.avatarUploaded.emit();
    } catch (error) {
      const res = this.translateService.translate('error.image.upload');
      this.uiMessageService.addErrorMessage(res);
      this.cancelWizard('close');
    }
    this.progressValue += 1;
    this.fileToUpload = undefined;
  }

  public cancelWizard(backTo: string) {
    if (backTo === 'close') {
      this.showWizard = false;
      this.showAddWizardStep1 = false;
      this.showAddWizardStep2 = false;
      this.fileToUpload = undefined;

      const result: ActionEmitterResult = {};
      result.result = ActionResult.CANCELED;
      result.type = ActionType.UPLOAD_FILE;

      this.modalHide.emit(result);
    } else if (backTo === 'finish') {
      this.showWizard = false;
      this.showAddWizardStep1 = false;
      this.showAddWizardStep2 = false;

      const result: ActionEmitterResult = {};
      result.result = ActionResult.SUCCEED;
      result.type = ActionType.UPLOAD_FILE;

      this.modalHide.emit(result);
    }
  }

  public fileNameValid(): boolean {
    if (this.fileToUpload === undefined) {
      return true;
    }
    return (
      imageExtensionValid(this.fileToUpload.name) &&
      this.fileToUpload.size <= 1 * 1024 * 1024
    );
  }
}
