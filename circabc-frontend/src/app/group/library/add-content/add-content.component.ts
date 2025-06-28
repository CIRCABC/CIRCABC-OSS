import {
  Component,
  Input,
  OnChanges,
  OnInit,
  SimpleChanges,
  output,
  input,
} from '@angular/core';

import { TranslocoModule } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { Node as ModelNode } from 'app/core/generated/circabc';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { UploadService } from 'app/core/upload.service';
import { SizePipe } from 'app/shared/pipes/size.pipe';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';
import { FileDetailsComponent } from './file-details/file-details.component';

interface FileWithId {
  id: number;
  file: File;
}
@Component({
  selector: 'cbc-add-content',
  templateUrl: './add-content.component.html',
  styleUrl: './add-content.component.scss',
  preserveWhitespaces: true,
  imports: [SpinnerComponent, FileDetailsComponent, SizePipe, TranslocoModule],
})
export class AddContentComponent implements OnInit, OnChanges {
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  public showWizard = false;
  public readonly parentNode = input.required<ModelNode>();
  public readonly modalHide = output<ActionEmitterResult>();

  public showAddWizardStep1 = false;
  public showAddWizardStep2 = false;

  public filesToUpload: FileWithId[] = [];
  public filesUploaded: File[] = [];
  private nodesID: string[] = [];

  public idCount = 0;

  public progressValue = 0;
  public progressMax = 0;

  constructor(
    private uploadService: UploadService,
    private uiMessageService: UiMessageService
  ) {}

  ngOnInit() {
    this.filesToUpload = [];
    this.filesUploaded = [];
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
        this.filesToUpload.push({ file: fileItem, id: this.idCount });
        this.idCount += 1;
      }
    }
  }

  public launchAddWizardStep1() {
    this.showAddWizardStep1 = true;
  }

  public async launchAddWizardStep2() {
    /// await this.uploadFiles();
    if (this.filesToUpload.length > 0) {
      this.showAddWizardStep1 = false;
      this.showAddWizardStep2 = true;
      await this.uploadFilesOneByOne();
    }
  }

  private async uploadFilesOneByOne() {
    const parentNode = this.parentNode();
    if (parentNode.id === undefined) {
      return;
    }
    this.progressMax = this.filesToUpload.length;
    this.progressValue = 0;
    for (const fileWithId of this.filesToUpload) {
      const file: File = fileWithId.file;
      try {
        const nodeId = await this.uploadService.uploadNewFile(
          file,
          parentNode.id
        );
        this.nodesID.push(nodeId);
        this.filesUploaded.push(file);
      } catch (_error) {
        this.uiMessageService.addErrorMessage('Error during file upload');
      }
      this.progressValue += 1;
    }
  }

  public deleteSelectedFile(file: FileWithId) {
    let index = 0;
    for (const fileToUpload of this.filesToUpload) {
      if (fileToUpload.id === file.id) {
        this.filesToUpload.splice(index, 1);
      }
      index += 1;
    }
  }

  public cancelWizard(backTo: string) {
    this.nodesID = [];
    this.filesToUpload = [];
    this.filesUploaded = [];

    if (backTo === 'close') {
      this.showWizard = false;
      this.showAddWizardStep1 = false;
      this.showAddWizardStep2 = false;

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

  public fileNames() {
    return this.filesToUpload.map((f) => f.file.name);
  }

  public filesSize(): number {
    const filesizes = this.filesToUpload.map((f) => f.file.size);
    return filesizes.reduce((a, b) => a + b, 0);
  }

  public isUploadFinished() {
    return this.progressValue === this.progressMax;
  }
}
