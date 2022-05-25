import { Component, EventEmitter, Input, Output } from '@angular/core';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { KeywordsService } from 'app/core/generated/circabc';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-import-keyword',
  templateUrl: './import-keyword.component.html',
  preserveWhitespaces: true,
})
export class ImportKeywordComponent {
  @Input()
  showModal = false;
  @Input()
  parentIgId!: string;
  @Output()
  public readonly modalHide = new EventEmitter<ActionEmitterResult>();

  public fileToUpload: File | undefined;
  public uploading = false;

  constructor(private keywordsService: KeywordsService) {}

  public cancelWizard(): void {
    this.showModal = false;
    const result: ActionEmitterResult = {};
    result.result = ActionResult.CANCELED;
    result.type = ActionType.IMPORT_KEYWORD;
    this.fileToUpload = undefined;
    this.modalHide.emit(result);
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

  public async import() {
    this.uploading = true;
    const result: ActionEmitterResult = {};
    result.type = ActionType.IMPORT_KEYWORD;

    try {
      if (this.fileToUpload) {
        await firstValueFrom(
          this.keywordsService.postBulkKeywordDefinitions(
            this.parentIgId,
            this.fileToUpload
          )
        );
      }

      this.showModal = false;
      this.fileToUpload = undefined;

      result.result = ActionResult.SUCCEED;
    } catch (error) {
      result.result = ActionResult.FAILED;
    } finally {
      this.modalHide.emit(result);
      this.uploading = false;
    }
  }
}
