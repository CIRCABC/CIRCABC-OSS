import { Component, Input, OnInit, output, input } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';

import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result/index';

import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { InterestGroupService } from 'app/core/generated/circabc';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { imageExtensionValid } from 'app/core/util';
import { ModalComponent } from 'app/shared/modal/modal.component';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-add-group-logo',
  templateUrl: './add-group-logo.component.html',
  preserveWhitespaces: true,
  imports: [ModalComponent, ReactiveFormsModule, TranslocoModule],
})
export class AddGroupLogoComponent implements OnInit {
  readonly groupId = input<string>();
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  showModal = false;
  readonly modalHide = output<ActionEmitterResult>();

  public processing = false;
  public logoForm!: FormGroup;
  public filesToUpload: File[] = [];

  constructor(
    private fb: FormBuilder,
    private translateService: TranslocoService,
    private uiMessageService: UiMessageService,
    private groupService: InterestGroupService
  ) {}

  ngOnInit() {
    this.logoForm = this.fb.group({
      file: [],
    });
  }

  public cancel() {
    this.showModal = false;
    this.filesToUpload = [];
    const res: ActionEmitterResult = {};
    this.modalHide.emit(res);
  }

  public async uploadLogo() {
    const groupId = this.groupId();
    if (groupId === undefined) {
      return;
    }
    this.processing = true;
    const res: ActionEmitterResult = {};
    res.type = ActionType.ADD_CATEGORY_LOGO;

    try {
      await firstValueFrom(
        this.groupService.postGroupNewLogo(groupId, this.filesToUpload[0])
      );

      res.result = ActionResult.SUCCEED;
      this.filesToUpload = [];
      this.showModal = false;
    } catch (_error) {
      const result = this.translateService.translate('error.image.upload');
      this.uiMessageService.addInfoMessage(result);
      res.result = ActionResult.FAILED;
    }

    this.modalHide.emit(res);
    this.processing = false;
  }

  public fileChangeEvent(event: Event) {
    const input = event.target as HTMLInputElement;
    const filesList = input.files as FileList;
    this.handleFiles(filesList);
  }

  private handleFiles(filesList: FileList) {
    this.filesToUpload = [];
    for (let i = 0; i < filesList.length; i += 1) {
      const fileItem = filesList.item(i);
      if (fileItem) {
        this.filesToUpload.push(fileItem);
      }
    }
  }

  public getFileName() {
    if (this.filesToUpload && this.filesToUpload.length > 0) {
      return this.filesToUpload[0].name;
    }
    return '';
  }

  public hasFile(): boolean {
    return this.filesToUpload && this.filesToUpload.length > 0;
  }

  public isValidImage(): boolean {
    return (
      this.filesToUpload &&
      this.filesToUpload.length > 0 &&
      imageExtensionValid(this.filesToUpload[0].name) &&
      this.filesToUpload[0].size <= 1 * 1024 * 1024
    );
  }
}
