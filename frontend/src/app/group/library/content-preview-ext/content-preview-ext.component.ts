import { DOCUMENT } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  model,
  OnChanges,
  output,
  signal,
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

/**
 * Standalone Angular component that renders an extended, full-featured preview
 * of a library content item inside a modal.
 *
 * Depending on the MIME type of the supplied {@link SelectableNode}, it renders
 * the appropriate viewer:
 * - PDF / office documents via the {@link NgxExtendedPdfViewerModule} viewer
 *   (office files are previewed through an on-demand Alfresco PDF rendition),
 * - images, videos and audio via the shared {@link PreviewComponent}.
 *
 * It also manages preview readiness, download/rendering progress, error state,
 * a spinner and browser fullscreen behaviour while the preview is visible.
 *
 * Key collaborators:
 * - {@link LoginService} to build authenticated (ticket-based) or guest media URLs,
 * - {@link AlfrescoService} to request a PDF rendition for non-natively-previewable
 *   documents,
 * - the injected {@link Document} to enter/exit fullscreen.
 */
@Component({
  selector: 'cbc-content-preview-ext',
  templateUrl: './content-preview-ext.component.html',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [PreviewComponent, NgxExtendedPdfViewerModule, TranslocoModule],
})
export class ContentPreviewExtendedComponent implements OnChanges {
  /** Service used to determine guest status and to obtain the auth ticket for media URLs. */
  private readonly loginService = inject(LoginService);
  /** Service used to request PDF renditions of documents that are not natively previewable. */
  private readonly alfrescoService = inject(AlfrescoService);
  /** Reference to the DOM document, used to enter and exit fullscreen mode. */
  private readonly document = inject<Document>(DOCUMENT);

  /**
   * Two-way bindable model controlling whether the preview modal is shown.
   * Required model input.
   */
  public showModal = model.required<boolean>();
  /**
   * Required input: the identifier of the document to preview. Used to request
   * a PDF rendition when needed and to trigger re-evaluation on change.
   */
  public documentId = input.required<string>();
  /**
   * Required input: the URL from which the content is fetched for preview.
   * An empty string disables all preview types.
   */
  public contentURL = input.required<string>();
  /**
   * Required input: the selectable node being previewed. Its MIME type
   * determines which viewer (document, image, video, audio) is used.
   */
  public readonly content = input.required<SelectableNode>();

  /** Output emitted when the preview is closed, signalling the content was previewed. */
  public readonly contentPreviewed = output();

  /** Whether content is currently loading/rendering (drives the spinner). */
  public progressing = signal(false);
  /** The latest progress event reported by the PDF viewer, if any. */
  public progress: ProgressBarEvent | undefined;
  /** Whether an error occurred while loading/rendering the preview. */
  public error = signal(false);
  /** Whether the preview is ready to be displayed (rendition available or native type). */
  public previewReady = signal(false);
  /** The error captured when {@link error} is true, if available. */
  public theError: Error | undefined;
  /** Form control bound to the PDF viewer search/find field. */
  public searchTerm = new FormControl();
  /** Whether the document (PDF) preview should be shown. */
  public isDocumentPreview = signal(false);
  /** Whether the image preview should be shown. */
  public isImagePreview = signal(false);
  /** Whether the video preview should be shown. */
  public isVideoPreview = signal(false);
  /** Whether the audio preview should be shown. */
  public isAudioPreview = signal(false);
  /** Current load progress as a percentage (0–100). */
  public progressPercent = signal(0);
  /** Whether an error message should be displayed. */
  public isErrorMessage = false;
  /** Whether the loading spinner should be displayed. */
  public isSpinner = signal(false);

  /**
   * Angular lifecycle hook invoked when any bound input changes.
   *
   * When a document id is present and no load is in progress, it determines
   * preview readiness: for non-natively-previewable types (when the Alfresco
   * API is enabled) it requests a PDF rendition and marks the preview ready
   * only when the rendition status is `CREATED`; otherwise it marks the
   * preview ready immediately. It then resets progress/error state and
   * recomputes which preview type (document, image, video, audio) and spinner
   * should be shown.
   *
   * @returns A promise that resolves once readiness has been resolved and state recomputed.
   */
  ngOnChanges() {
    void this.handleChanges();
  }

  private async handleChanges() {
    const documentId = this.documentId();
    if (documentId !== undefined && !this.progressing()) {
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
            this.previewReady.set(true);
          } else {
            this.previewReady.set(false);
          }
        } catch {
          this.previewReady.set(false);
        }
      } else {
        this.previewReady.set(true);
      }
    }
    this.progressing.set(false);
    this.error.set(false);
    this.searchTerm.setValue('');
    this.isDocumentPreview.set(this.showDocumentPreview());
    this.isImagePreview.set(this.showImagePreview());
    this.isVideoPreview.set(this.showVideoPreview());
    this.isAudioPreview.set(this.showAudioPreview());
    this.progressPercent.set(0);
    this.isSpinner.set(this.showSpinner());
  }

  /**
   * Handles an error raised by the underlying preview/viewer.
   *
   * @param event - The error emitted by the viewer.
   */
  public onError(event: Error) {
    this.theError = event;
    this.error.set(true);
    this.progressing.set(false);
    this.isSpinner.set(this.showSpinner());
  }

  /**
   * Handles progress events from the PDF viewer, updating the load state
   * and percentage while content is being loaded.
   *
   * @param event - The progress event reported by the viewer.
   */
  public onProgress(event: ProgressBarEvent) {
    if (event.type === 'load') {
      this.progressing.set(true);
      this.progress = event;
      this.progressPercent.set(event.percent);
    }
  }

  /**
   * Reacts to visibility changes of the preview by entering or exiting
   * browser fullscreen mode.
   *
   * @param isVisible - `true` when the preview becomes visible, `false` when hidden.
   */
  public visibleChange(isVisible: boolean) {
    // prettier-ignore
    if (isVisible) { // NOSONAR
      this.show();
    } else {
      this.hide();
    }
  }

  /** Enters fullscreen mode to display the preview. */
  private show() {
    this.requestFullscreen();
  }

  /** Exits fullscreen mode when the preview is hidden. */
  private hide() {
    this.exitFullscreen();
  }

  /**
   * @returns `true` if the current content is an image.
   */
  private isImage(): boolean {
    return isContentImage(this.content());
  }
  /**
   * @returns `true` if the current content is a video.
   */
  private isVideo(): boolean {
    return isContentVideo(this.content());
  }
  /**
   * @returns `true` if the current content is audio.
   */
  private isAudio(): boolean {
    return isContentAudio(this.content());
  }
  /**
   * @returns `true` if the current content is a PDF.
   */
  private isPdf(): boolean {
    return isContentPdf(this.content());
  }

  /**
   * @returns `true` while content is loading/rendering and the spinner should be shown.
   */
  private showSpinner(): boolean {
    return this.progressing();
  }

  /**
   * @returns `true` if the document (PDF) preview should be displayed: a
   * content URL exists, the content is not image/video/audio, and the
   * preview is ready.
   */
  private showDocumentPreview(): boolean {
    return (
      this.contentURL() !== '' &&
      !this.isImage() &&
      !this.isVideo() &&
      !this.isAudio() &&
      this.previewReady()
    );
  }

  /**
   * @returns `true` if the image preview should be displayed (a content URL
   * exists and the content is an image).
   */
  private showImagePreview(): boolean {
    return this.contentURL() !== '' && this.isImage();
  }

  /**
   * @returns `true` if the video preview should be displayed (a content URL
   * exists and the content is a video).
   */
  private showVideoPreview(): boolean {
    return this.contentURL() !== '' && this.isVideo();
  }

  /**
   * @returns `true` if the audio preview should be displayed (a content URL
   * exists and the content is audio).
   */
  private showAudioPreview(): boolean {
    return this.contentURL() !== '' && this.isAudio();
  }

  /** Requests browser fullscreen on the document element if not already fullscreen. */
  private requestFullscreen() {
    if (!this.document.fullscreenElement) {
      this.document.documentElement.requestFullscreen();
    }
  }

  /** Exits browser fullscreen if a fullscreen element is currently active. */
  private exitFullscreen() {
    if (this.document.fullscreenElement !== null) {
      this.document.exitFullscreen();
    }
  }

  /**
   * Builds the authenticated Alfresco URL used to stream the media content.
   *
   * For guest users the URL includes a `guest=true` flag; for authenticated
   * users it appends the current login ticket.
   *
   * @returns The fully-qualified URL from which the content binary can be fetched.
   */
  public mediaContentURL(): string {
    if (this.loginService.isGuest()) {
      return `${environment.serverURL}d/d/workspace/SpacesStore/${this.content().id}/file.bin?property=%7Bhttp://www.alfresco.org/model/content/1.0%7Dcontent&guest=true`;
    }
    return `${environment.serverURL}d/d/workspace/SpacesStore/${
      this.content().id
    }/file.bin?property=%7Bhttp://www.alfresco.org/model/content/1.0%7Dcontent&ticket=${this.loginService.getTicket()}`;
  }

  /**
   * Closes the preview: emits {@link contentPreviewed}, resets error and
   * readiness state, and exits fullscreen mode.
   */
  public close(): void {
    this.contentPreviewed.emit();
    this.error.set(false);
    this.previewReady.set(false);
    this.exitFullscreen();
  }
}
