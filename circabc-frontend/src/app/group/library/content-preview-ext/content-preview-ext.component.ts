import { DOCUMENT } from '@angular/common';
import {
  Component,
  Inject,
  OnChanges,
  output,
  input,
  model,
} from '@angular/core';
import { FormControl } from '@angular/forms';
import { TranslocoModule } from '@jsverse/transloco';
import { AlfrescoService } from 'app/core/alfresco.service';
import { LoginService } from 'app/core/login.service';
import { type SelectableNode } from 'app/core/ui-model/index';
import {
  isContentAudio,
  isContentImage,
  isContentPdf,
  isContentVideo,
} from 'app/core/util';
import { PreviewComponent } from 'app/shared/preview/preview.component';
import { environment } from 'environments/environment';
import {
  NgxExtendedPdfViewerModule,
  ProgressBarEvent,
} from 'ngx-extended-pdf-viewer';

@Component({
  selector: 'cbc-content-preview-ext',
  templateUrl: './content-preview-ext.component.html',
  preserveWhitespaces: true,
  imports: [PreviewComponent, NgxExtendedPdfViewerModule, TranslocoModule],
})
export class ContentPreviewExtendedComponent implements OnChanges {
  public showModal = model.required<boolean>();
  public documentId = input.required<string>();
  public contentURL = input.required<string>();
  public readonly content = input.required<SelectableNode>();

  public readonly contentPreviewed = output();

  public progressing = false;
  public progress: ProgressBarEvent | undefined;
  public error = false;
  public previewReady = false;
  public theError: Error | undefined;
  public searchTerm = new FormControl();
  public isDocumentPreview = false;
  public isImagePreview = false;
  public isVideoPreview = false;
  public isAudioPreview = false;
  public progressPercent = 0;
  public isErrorMessage = false;
  public isSpinner = false;

  public constructor(
    private loginService: LoginService,
    private alfrescoService: AlfrescoService,
    @Inject(DOCUMENT) private document: Document
  ) {}

  async ngOnChanges() {
    const documentId = this.documentId();
    if (documentId !== undefined && !this.progressing) {
      if (
        environment.useAlfrescoAPI &&
        !this.isImage() &&
        !this.isVideo() &&
        !this.isAudio() &&
        !this.isPdf()
      ) {
        try {
          const rendition = await this.alfrescoService.getRendition(
            documentId,
            'pdf'
          );
          if (rendition.entry.status === 'CREATED') {
            this.previewReady = true;
          } else {
            this.previewReady = false;
          }
        } catch {
          this.previewReady = false;
        }
      } else {
        this.previewReady = true;
      }
    }
    this.progressing = false;
    this.error = false;
    this.searchTerm.setValue('');
    this.isDocumentPreview = this.showDocumentPreview();
    this.isImagePreview = this.showImagePreview();
    this.isVideoPreview = this.showVideoPreview();
    this.isAudioPreview = this.showAudioPreview();
    this.progressPercent = 0;
    this.isSpinner = this.showSpinner();
  }

  public onError(event: Error) {
    this.theError = event;
    this.error = true;
    this.progressing = false;
    this.isSpinner = this.showSpinner();
  }

  public onProgress(event: ProgressBarEvent) {
    if (event.type === 'load') {
      this.progressing = true;
      this.progress = event;
      this.progressPercent = event.percent;
    }
  }

  public visibleChange(isVisible: boolean) {
    if (isVisible) {
      this.requestFullscreen();
    } else {
      this.exitFullscreen();
    }
  }

  private isImage(): boolean {
    return isContentImage(this.content());
  }
  private isVideo(): boolean {
    return isContentVideo(this.content());
  }
  private isAudio(): boolean {
    return isContentAudio(this.content());
  }
  private isPdf(): boolean {
    return isContentPdf(this.content());
  }

  private showSpinner(): boolean {
    return this.progressing;
  }

  private showDocumentPreview(): boolean {
    return (
      this.contentURL() !== '' &&
      !this.isImage() &&
      !this.isVideo() &&
      !this.isAudio() &&
      this.previewReady
    );
  }

  private showImagePreview(): boolean {
    return this.contentURL() !== '' && this.isImage();
  }

  private showVideoPreview(): boolean {
    return this.contentURL() !== '' && this.isVideo();
  }

  private showAudioPreview(): boolean {
    return this.contentURL() !== '' && this.isAudio();
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

  public mediaContentURL(): string {
    if (this.loginService.isGuest()) {
      return `${environment.serverURL}d/d/workspace/SpacesStore/${this.content().id}/file.bin?property=%7Bhttp://www.alfresco.org/model/content/1.0%7Dcontent&guest=true`;
    }
    return `${environment.serverURL}d/d/workspace/SpacesStore/${
      this.content().id
    }/file.bin?property=%7Bhttp://www.alfresco.org/model/content/1.0%7Dcontent&ticket=${this.loginService.getTicket()}`;
  }

  public close(): void {
    this.contentPreviewed.emit();
    this.error = false;
    this.previewReady = false;
    this.exitFullscreen();
  }
}
