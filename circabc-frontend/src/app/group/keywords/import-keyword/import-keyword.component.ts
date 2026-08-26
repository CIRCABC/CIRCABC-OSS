import { Component, Input, output, input } from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { KeywordsService } from 'app/core/generated/circabc';
import { ModalComponent } from 'app/shared/modal/modal.component';
import { SizePipe } from 'app/shared/pipes/size.pipe';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-import-keyword',
  templateUrl: './import-keyword.component.html',
  preserveWhitespaces: true,
  imports: [ModalComponent, SizePipe, TranslocoModule],
})
export class ImportKeywordComponent {
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  showModal = false;
  readonly parentIgId = input.required<string>();
  public readonly modalHide = output<ActionEmitterResult>();

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

  public fileChangeEvent(event: Event) {
    const input = event.target as HTMLInputElement;
    const filesList = input.files as FileList;

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
            this.parentIgId(),
            this.fileToUpload
          )
        );
      }

      this.showModal = false;
      this.fileToUpload = undefined;

      result.result = ActionResult.SUCCEED;
    } catch (_error) {
      result.result = ActionResult.FAILED;
    } finally {
      this.modalHide.emit(result);
      this.uploading = false;
    }
  }
}
