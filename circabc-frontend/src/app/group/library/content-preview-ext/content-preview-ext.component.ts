import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  Output,
  Inject,
} from '@angular/core';
import { DOCUMENT } from '@angular/common';
import { FormControl } from '@angular/forms';
import { ContentService, PreviewResult } from 'app/core/generated/circabc';
import { SelectableNode } from 'app/core/ui-model/index';
import { isContentAudio, isContentImage, isContentVideo } from 'app/core/util';
import { firstValueFrom } from 'rxjs';
import { environment } from 'environments/environment';
import { LoginService } from 'app/core/login.service';

@Component({
  selector: 'cbc-content-preview-ext',
  templateUrl: './content-preview-ext.component.html',
  preserveWhitespaces: true,
})
export class ContentPreviewExtendedComponent implements OnChanges {
  @Input()
  public showModal = false;
  @Input()
  public documentId!: string;
  @Input()
  public contentURL!: string;
  @Input()
  public content!: SelectableNode;

  @Output()
  public readonly contentPreviewed = new EventEmitter();

  public progressing = false;
  // use any instead of PDFProgressData
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  public progress: any | undefined;
  public error = false;
  public previewResult!: PreviewResult;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  public theError: any;
  public searchTerm = new FormControl();
  public isDocumentPreview = false;
  public isImagePreview = false;
  public isVideoPreview = false;
  public isAudioPreview = false;
  public progressPercent = 0;
  public isErrorMessage = false;
  public isSpinner = false;

  public constructor(
    private contentService: ContentService,
    private loginService: LoginService,
    @Inject(DOCUMENT) private document: Document
  ) {}

  async ngOnChanges() {
    if (this.documentId !== undefined && !this.progressing) {
      this.previewResult = await firstValueFrom(
        this.contentService.getCheckPreview(this.documentId)
      );
    }
    this.progressing = false;
    this.error = false;
    this.searchTerm.setValue('');
    this.isDocumentPreview = this.showDocumentPreview();
    this.isImagePreview = this.showImagePreview();
    this.isVideoPreview = this.showVideoPreview();
    this.isAudioPreview = this.showAudioPreview();
    this.progressPercent = 0;
    this.isErrorMessage = this.showErrorMessage();
    this.isSpinner = this.showSpinner();
  }

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  public onError(event: any) {
    this.theError = event;
    this.error = true;
    this.isErrorMessage = this.showErrorMessage();
    this.isSpinner = this.showSpinner();
  }
  // use any instead of PDFDocumentProxy
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  public onProgress(event: any) {
    this.progressing = true;
    this.progress = event;
    this.progressPercent = this.calculatePercentage();
  }

  public visibleChange(isVisible: boolean) {
    if (isVisible) {
      this.requestFullscreen();
    } else {
      this.exitFullscreen();
    }
  }

  private isImage(): boolean {
    return isContentImage(this.content);
  }
  private isVideo(): boolean {
    return isContentVideo(this.content);
  }
  private isAudio(): boolean {
    return isContentAudio(this.content);
  }

  private showErrorMessage(): boolean {
    if (this.previewResult) {
      return (
        !this.previewResult.ready &&
        !this.isImage() &&
        !this.isVideo() &&
        !this.isAudio()
      );
    } else {
      return false;
    }
  }

  private showSpinner(): boolean {
    return (
      (this.showErrorMessage() &&
        this.previewResult.messageCode !== 'not.available') ||
      this.progressing
    );
  }

  private showDocumentPreview(): boolean {
    return (
      this.contentURL !== '' &&
      !this.isImage() &&
      !this.isVideo() &&
      !this.isAudio() &&
      this.previewResult &&
      this.previewResult.ready
    );
  }

  private showImagePreview(): boolean {
    return this.contentURL !== '' && this.isImage();
  }

  private showVideoPreview(): boolean {
    return this.contentURL !== '' && this.isVideo();
  }

  private showAudioPreview(): boolean {
    return this.contentURL !== '' && this.isAudio();
  }

  private requestFullscreen() {
    if (!this.document.fullscreenElement) {
      this.document.documentElement.requestFullscreen();
    }
  }

  private exitFullscreen() {
    if (this.document.fullscreenElement !== null) {
      this.document.exitFullscreen();
    }
  }

  public calculatePercentage(): number {
    if (
      this.progress.loaded &&
      this.previewResult &&
      this.previewResult.contentLength
    ) {
      return Math.round(
        (this.progress.loaded / this.previewResult.contentLength) * 100
      );
    } else {
      return 0;
    }
  }

  public mediaContentURL(): string {
    if (this.loginService.isGuest()) {
      return `${environment.serverURL}d/d/workspace/SpacesStore/${this.content.id}/file.bin?property=%7Bhttp://www.alfresco.org/model/content/1.0%7Dcontent&guest=true`;
    } else {
      return `${environment.serverURL}d/d/workspace/SpacesStore/${
        this.content.id
      }/file.bin?property=%7Bhttp://www.alfresco.org/model/content/1.0%7Dcontent&ticket=${this.loginService.getTicket()}`;
    }
  }

  public close(): void {
    this.showModal = false;
    this.contentPreviewed.emit();
    this.contentURL = '';
    this.error = false;
    this.exitFullscreen();
  }
}
