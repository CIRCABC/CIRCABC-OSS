import {
  Component,
  Inject,
  Input,
  OnInit,
  Optional,
  output,
  input,
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

@Component({
  selector: 'cbc-import',
  templateUrl: './import.component.html',
  styleUrl: './import.component.scss',
  preserveWhitespaces: true,
  imports: [ReactiveFormsModule, SpinnerComponent, SizePipe, TranslocoModule],
})
export class ImportComponent implements OnInit {
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  public showModal = false;
  public readonly targetNode = input.required<ModelNode>();
  public readonly modalHide = output<ActionEmitterResult>();

  public fileToUpload: File | undefined;
  public uploading = false;
  public progressValue = 0;
  public progressMax = 0;

  public importMaxSize = 20;

  public importForm!: FormGroup;

  private basePath!: string;

  constructor(
    private dialog: MatDialog,
    private interestGroupService: InterestGroupService,
    private formBuilder: FormBuilder,
    private saveAsService: SaveAsService,
    private translateService: TranslocoService,
    private uiMessageService: UiMessageService,
    @Optional()
    @Inject(BASE_PATH)
    basePath: string
  ) {
    if (basePath) {
      this.basePath = basePath;
    }
  }

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

  public async fileChangeEvent(event: Event) {
    if (environment.circabcRelease === 'echa') {
      if (!(await this.showDialogConfirmMsg())) {
        return;
      }
    }

    const input = event.target as HTMLInputElement;
    const filesList = input.files as FileList;

    this.fileToUpload = filesList[0];
  }

  public async import() {
    if (
      this.fileToUpload === undefined ||
      this.importExceeds(this.fileToUpload.size)
    ) {
      return;
    }

    this.uploading = true;

    const result: ActionEmitterResult = {};
    result.type = ActionType.IMPORT_ZIP;

    try {
      await firstValueFrom(
        this.interestGroupService.postImportZipFile(
          this.targetNode().id as string,
          this.importForm.controls.notifyUser.value,
          this.importForm.controls.deleteFile.value,
          this.importForm.controls.disableNotification.value,
          this.importForm.controls.encoding.value,
          this.fileToUpload
        )
      );

      this.showModal = false;
      this.fileToUpload = undefined;
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
      this.uploading = false;
    }
  }

  public importExceeds(size: number): boolean {
    return size > this.importMaxSize * 1024 * 1024;
  }

  public cancel(backTo: string) {
    if (backTo === 'close') {
      this.fileToUpload = undefined;
      this.showModal = false;
      const result: ActionEmitterResult = {};
      result.result = ActionResult.CANCELED;
      result.type = ActionType.IMPORT_ZIP;
      this.modalHide.emit(result);
    }
  }

  public getImportIndexFileTemplate() {
    const url = `${this.basePath}/groups/import/template`;
    this.saveAsService.saveUrlAs(url, 'index.txt');
    return false;
  }

  private async showDialogConfirmMsg() {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
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
