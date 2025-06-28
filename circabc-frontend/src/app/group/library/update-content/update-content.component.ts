import { Component, Input, output, input } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { Node as ModelNode } from 'app/core/generated/circabc';
import { UploadService } from 'app/core/upload.service';
import { ConfirmDialogComponent } from 'app/shared/confirm-dialog/confirm-dialog.component';
import { SizePipe } from 'app/shared/pipes/size.pipe';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';
import { environment } from 'environments/environment';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-update-content',
  templateUrl: './update-content.component.html',
  styleUrl: './update-content.component.scss',
  preserveWhitespaces: true,
  imports: [
    ReactiveFormsModule,
    MatSlideToggleModule,
    SpinnerComponent,
    SizePipe,
    TranslocoModule,
  ],
})
export class UpdateContentComponent {
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  public showWizard = false;
  public readonly targetNode = input.required<ModelNode>();
  public readonly modalHide = output<ActionEmitterResult>();
  public readonly updateCheckedOut = input(false);

  public fileToUpload: File | undefined;
  public uploading = false;
  public progressValue = 0;
  public progressMax = 0;

  notifyFormGroup = this.notifyFormBuilder.group({
    notify: true,
  });
  constructor(
    private notifyFormBuilder: FormBuilder,
    private uploadService: UploadService,
    private dialog: MatDialog,
    private translateService: TranslocoService
  ) {}

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
      this.handleFiles(files);
    }
  }

  public fileChangeEvent(event: Event) {
    const input = event.target as HTMLInputElement;
    const filesList = input.files as FileList;

    this.handleFiles(filesList);
  }
  private handleFiles(filesList: FileList) {
    for (let i = 0; i < filesList.length; i += 1) {
      const fileItem = filesList.item(i);
      if (fileItem) {
        this.fileToUpload = fileItem;
      }
    }
  }

  public async update() {
    const targetNode = this.targetNode();
    if (
      environment.circabcRelease === 'echa' &&
      (targetNode?.properties?.security_ranking === 'SENSITIVE' ||
        targetNode?.properties?.security_ranking === 'SPECIAL_HANDLING')
    ) {
      if (!(await this.showDialogConfirmMsg())) {
        return;
      }
    }

    this.uploading = true;
    if (this.fileToUpload !== undefined) {
      if (this.updateCheckedOut()) {
        // executed in case the document has been checked out
        // and the content has to be updated without checking in implicitly
        await this.uploadService.updateCheckedOutFileContent(
          this.fileToUpload,
          targetNode.id as string,
          this.notifyFormGroup.controls.notify.value === null
            ? true
            : this.notifyFormGroup.controls.notify.value
        );
      } else {
        await this.uploadService.updateExistingFileContent(
          this.fileToUpload,
          targetNode.id as string,
          this.notifyFormGroup.controls.notify.value === null
            ? true
            : this.notifyFormGroup.controls.notify.value
        );
      }

      this.showWizard = false;
      this.fileToUpload = undefined;

      const result: ActionEmitterResult = {};
      result.result = ActionResult.SUCCEED;
      result.type = ActionType.UPDATE_FILE_CONTENT;

      this.modalHide.emit(result);
    }
    this.uploading = false;
  }

  public cancelWizard(backTo: string) {
    if (backTo === 'close') {
      this.showWizard = false;
      this.fileToUpload = undefined;
      const result: ActionEmitterResult = {};
      result.result = ActionResult.CANCELED;
      result.type = ActionType.UPDATE_FILE_CONTENT;
      this.modalHide.emit(result);
    }
  }

  private async showDialogConfirmMsg() {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        messageTranslated: this.translateService.translate(
          'label.dialog.alert.snc.update',
          {
            link: `<a href="https://ec.europa.eu/transparency/documents-register/detail?ref=C(2019)1904&lang=en" target="_blank">C(2019)1904</a>`,
          }
        ),
        labelOK: 'label.confirm',
        title: 'label.dialog.alert.snc.update.title',
        layoutStyle: 'SNCNotification',
      },
    });

    return firstValueFrom(dialogRef.afterClosed());
  }
}
