import { Component, EventEmitter, Input, Output } from '@angular/core';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { Node as ModelNode } from 'app/core/generated/circabc';
import { UploadService } from 'app/core/upload.service';

@Component({
  selector: 'cbc-update-content',
  templateUrl: './update-content.component.html',
  styleUrls: ['./update-content.component.scss'],
  preserveWhitespaces: true,
})
export class UpdateContentComponent {
  @Input()
  public showWizard = false;
  @Input()
  public targetNode!: ModelNode;
  @Output()
  public readonly modalHide = new EventEmitter<ActionEmitterResult>();
  @Input()
  public updateCheckedOut = false;

  public fileToUpload: File | undefined;
  public uploading = false;
  public progressValue = 0;
  public progressMax = 0;

  constructor(private uploadService: UploadService) {}

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

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  public fileChangeEvent(fileInput: any) {
    const filesList = fileInput.target.files as FileList;
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
    this.uploading = true;

    if (this.fileToUpload !== undefined) {
      if (this.updateCheckedOut) {
        // executed in case the document has been checked out
        // and the content has to be updated without checking in implicitly
        await this.uploadService.updateCheckedOutFileContent(
          this.fileToUpload,
          this.targetNode.id as string
        );
      } else {
        await this.uploadService.updateExistingFileContent(
          this.fileToUpload,
          this.targetNode.id as string
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
}
