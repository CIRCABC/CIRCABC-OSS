import { DOCUMENT } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import {
  Component,
  ElementRef,
  Inject,
  OnChanges,
  output,
  input,
  model,
  ViewChild,
} from '@angular/core';
import { FormControl } from '@angular/forms';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { TranslocoModule } from '@jsverse/transloco';
import { AlfrescoService } from 'app/core/alfresco.service';
import { LoginService } from 'app/core/login.service';
import { type SelectableNode } from 'app/core/ui-model/index';
import {
  isContentAudio,
  isContentCsv,
  isContentExcel,
  isContentHtml,
  isContentImage,
  isContentMarkdown,
  isContentPdf,
  isContentPptx,
  isContentRtf,
  isContentSvg,
  isContentText,
  isContentTiff,
  isContentVideo,
  isContentWebp,
  isContentWord,
} from 'app/core/util';
import { PreviewComponent } from 'app/shared/preview/preview.component';
import { environment } from 'environments/environment';
import {
  NgxExtendedPdfViewerModule,
  ProgressBarEvent,
} from 'ngx-extended-pdf-viewer';
import { firstValueFrom } from 'rxjs';

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
  public isTextPreview = false;
  public isHtmlPreview = false;
  public isCsvPreview = false;
  public isMarkdownPreview = false;
  public isSvgPreview = false;
  public isWordPreview = false;
  public isExcelPreview = false;
  public isTiffPreview = false;
  public isRtfPreview = false;
  public isPptxPreview = false;
  public isWebpPreview = false;
  public textContent = '';
  public htmlContent: SafeHtml | undefined;
  public imageDataUrl = '';

  @ViewChild('pptxContainer')
  private pptxContainer?: ElementRef<HTMLDivElement>;
  private pptxBuffer: ArrayBuffer | undefined;
  public progressPercent = 0;
  public isErrorMessage = false;
  public isSpinner = false;

  public constructor(
    private loginService: LoginService,
    private alfrescoService: AlfrescoService,
    private http: HttpClient,
    private sanitizer: DomSanitizer,
    @Inject(DOCUMENT) private document: Document
  ) {}

  async ngOnChanges() {
    const documentId = this.documentId();
    if (documentId !== undefined && !this.progressing) {
      if (this.isWord() || this.isExcel()) {
        await this.loadOfficeContent();
      } else if (this.isTiff() || this.isRtf() || this.isPptx()) {
        await this.loadRichBinaryContent();
      } else if (
        this.isText() ||
        this.isHtml() ||
        this.isCsv() ||
        this.isMarkdown() ||
        this.isSvg()
      ) {
        await this.loadTextualContent();
      } else if (
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
    this.isTextPreview = this.showTextPreview();
    this.isHtmlPreview = this.showHtmlPreview();
    this.isCsvPreview = this.showCsvPreview();
    this.isMarkdownPreview = this.showMarkdownPreview();
    this.isSvgPreview = this.showSvgPreview();
    this.isWordPreview = this.showWordPreview();
    this.isExcelPreview = this.showExcelPreview();
    this.isTiffPreview = this.showTiffPreview();
    this.isRtfPreview = this.showRtfPreview();
    this.isPptxPreview = this.showPptxPreview();
    this.isWebpPreview = this.showWebpPreview();
    this.progressPercent = 0;
    this.isSpinner = this.showSpinner();
  }

  /**
   * Loads and renders binary Office documents (Word .docx, Excel .xls/.xlsx)
   * entirely client-side, then wraps the result with a strict CSP for the
   * sandboxed iframe (see template).
   */
  private async loadOfficeContent(): Promise<void> {
    this.textContent = '';
    this.htmlContent = undefined;
    try {
      const buffer = await firstValueFrom(
        this.http.get(this.mediaContentURL(), { responseType: 'arraybuffer' })
      );
      let body: string;
      if (this.isExcel()) {
        body = await this.renderWorkbook(buffer);
      } else {
        const mammoth = await import('mammoth');
        const result = await mammoth.convertToHtml({ arrayBuffer: buffer });
        body = result.value;
      }
      this.htmlContent = this.sanitizer.bypassSecurityTrustHtml(
        // Excel tables use the full-width layout so wide sheets scroll
        // horizontally instead of being centered/clipped.
        this.buildSafeDocument(body, false, this.isExcel())
      );
      this.previewReady = true;
    } catch (error) {
      this.previewReady = false;
      this.onError(error as Error);
    }
  }

  /**
   * Loads TIFF / RTF / PPTX documents (fetched as raw bytes) and renders them
   * client-side: TIFF via UTIF.js to a canvas image, RTF via rtf.js to HTML,
   * PPTX via pptx-preview into a container element.
   */
  private async loadRichBinaryContent(): Promise<void> {
    this.textContent = '';
    this.htmlContent = undefined;
    this.imageDataUrl = '';
    this.pptxBuffer = undefined;
    try {
      const buffer = await firstValueFrom(
        this.http.get(this.mediaContentURL(), { responseType: 'arraybuffer' })
      );
      if (this.isTiff()) {
        this.imageDataUrl = await this.renderTiff(buffer);
      } else if (this.isRtf()) {
        const { RTFJS } = await import('rtf.js');
        RTFJS.loggingEnabled(false);
        const elements = await new RTFJS.Document(buffer, {}).render();
        const body = elements.map((element) => element.outerHTML).join('');
        this.htmlContent = this.sanitizer.bypassSecurityTrustHtml(
          this.buildSafeDocument(body, false)
        );
      } else if (this.isPptx()) {
        this.pptxBuffer = buffer;
      }
      this.previewReady = true;
      if (this.isPptx()) {
        // The container only exists once previewReady renders it, so defer.
        setTimeout(() => this.renderPptx(), 0);
      }
    } catch (error) {
      this.previewReady = false;
      this.onError(error as Error);
    }
  }

  /** Decodes the first page of a TIFF and returns a PNG data URL. */
  private async renderTiff(buffer: ArrayBuffer): Promise<string> {
    const UTIF = await import('utif');
    const ifds = UTIF.decode(buffer);
    if (!ifds || ifds.length === 0) {
      throw new Error('Invalid TIFF');
    }
    const ifd = ifds[0];
    UTIF.decodeImage(buffer, ifd);
    const rgba = UTIF.toRGBA8(ifd);
    const canvas = this.document.createElement('canvas');
    canvas.width = ifd.width;
    canvas.height = ifd.height;
    const context = canvas.getContext('2d');
    if (context === null) {
      throw new Error('Canvas not available');
    }
    const imageData = context.createImageData(ifd.width, ifd.height);
    imageData.data.set(rgba);
    context.putImageData(imageData, 0, 0);
    return canvas.toDataURL('image/png');
  }

  /** Renders the loaded PPTX into the container element via pptx-preview. */
  private async renderPptx(): Promise<void> {
    const container = this.pptxContainer?.nativeElement;
    if (container === undefined || this.pptxBuffer === undefined) {
      return;
    }
    container.innerHTML = '';
    const width = container.clientWidth > 0 ? container.clientWidth : 960;
    try {
      // Loaded on demand so that pptx-preview (and its heavy echarts
      // dependency) is split into a separate chunk fetched only for PPTX files.
      const { init } = await import('pptx-preview');
      const previewer = init(container, {
        width,
        height: Math.round(width * 0.5625),
        mode: 'slide',
      });
      await previewer.preview(this.pptxBuffer);
    } catch (error) {
      this.onError(error as Error);
    }
  }

  /** Converts every sheet of a workbook into HTML tables (one section each). */
  private async renderWorkbook(buffer: ArrayBuffer): Promise<string> {
    const XLSX = await import('xlsx');
    const workbook = XLSX.read(new Uint8Array(buffer), { type: 'array' });
    const parser = new DOMParser();
    const sections: string[] = [];
    for (const sheetName of workbook.SheetNames) {
      const worksheet = workbook.Sheets[sheetName];
      const sheetHtml = XLSX.utils.sheet_to_html(worksheet);
      const table = parser
        .parseFromString(sheetHtml, 'text/html')
        .querySelector('table');
      sections.push(
        `<h3>${this.escapeHtml(sheetName)}</h3>${table ? table.outerHTML : ''}`
      );
    }
    return sections.join('\n');
  }

  /** Escapes the few characters that matter when injecting text into markup. */
  private escapeHtml(value: string): string {
    return value
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;');
  }

  /** Builds an HTML table from parsed CSV rows (first row as header). */
  private csvRowsToTableHtml(rows: string[][]): string {
    const body = rows
      .map((row, rowIndex) => {
        const tag = rowIndex === 0 ? 'th' : 'td';
        const cells = row
          .map((cell) => `<${tag}>${this.escapeHtml(cell)}</${tag}>`)
          .join('');
        return `<tr>${cells}</tr>`;
      })
      .join('');
    return `<table>${body}</table>`;
  }

  private async loadTextualContent(): Promise<void> {
    this.textContent = '';
    this.htmlContent = undefined;
    try {
      const content = await firstValueFrom(
        this.http.get(this.mediaContentURL(), { responseType: 'text' })
      );
      if (this.isMarkdown()) {
        // Markdown -> HTML, then scrubbed and wrapped with a strict CSP before
        // being rendered inside the sandboxed iframe (see template).
        const { marked } = await import('marked');
        const body = await marked.parse(content);
        this.htmlContent = this.sanitizer.bypassSecurityTrustHtml(
          this.buildSafeDocument(body, false)
        );
      } else if (this.isHtml()) {
        // Raw HTML file: scrub external references and wrap with a strict CSP.
        this.htmlContent = this.sanitizer.bypassSecurityTrustHtml(
          this.buildSafeDocument(content, true)
        );
      } else if (this.isSvg()) {
        // Wrap SVG markup and scrub/CSP it so any embedded script is
        // neutralised, then render it in the sandboxed iframe.
        this.htmlContent = this.sanitizer.bypassSecurityTrustHtml(
          this.buildSafeDocument(content, false)
        );
      } else if (this.isCsv()) {
        // Render CSV/TSV as an HTML table inside the same sandboxed, full-width
        // iframe as Excel, so scrolling behaves identically.
        const rows = this.parseCsv(content);
        this.htmlContent = this.sanitizer.bypassSecurityTrustHtml(
          this.buildSafeDocument(this.csvRowsToTableHtml(rows), false, true)
        );
      } else {
        this.textContent = content;
      }
      this.previewReady = true;
    } catch (error) {
      this.previewReady = false;
      this.onError(error as Error);
    }
  }

  /**
   * Builds a self-contained HTML document for the preview iframe:
   * - removes dangerous/redirecting elements and attributes,
   * - strips every reference to external resources (links, images, styles),
   * - injects a strict Content-Security-Policy.
   * Together with the empty `sandbox` on the iframe this guarantees that no
   * script runs and nothing loads from / navigates to an external origin.
   */
  private buildSafeDocument(
    input: string,
    isFullDocument: boolean,
    fullWidth = false
  ): string {
    const html = isFullDocument
      ? input
      : `<!DOCTYPE html><html><head></head><body>${input}</body></html>`;
    const doc = new DOMParser().parseFromString(html, 'text/html');

    // 1) Drop elements that can execute, embed, submit or redirect.
    doc
      .querySelectorAll(
        'script, iframe, object, embed, frame, frameset, applet, form, base, link'
      )
      .forEach((el) => el.remove());

    // 2) Drop meta refresh redirects.
    doc.querySelectorAll('meta[http-equiv]').forEach((el) => {
      if ((el.getAttribute('http-equiv') ?? '').toLowerCase() === 'refresh') {
        el.remove();
      }
    });

    // 3) Scrub attributes on every remaining element.
    doc.querySelectorAll('*').forEach((el) => {
      Array.from(el.attributes).forEach((attr) => {
        const name = attr.name.toLowerCase();
        // Inline event handlers.
        if (name.startsWith('on')) {
          el.removeAttribute(attr.name);
          return;
        }
        // External hyperlinks (keep internal "#..." anchors).
        if (name === 'href' && this.isExternalUrl(attr.value)) {
          el.removeAttribute(attr.name);
          return;
        }
        // Any external resource reference.
        if (
          [
            'src',
            'srcset',
            'poster',
            'background',
            'data',
            'formaction',
          ].includes(name) &&
          this.isExternalUrl(attr.value)
        ) {
          el.removeAttribute(attr.name);
          return;
        }
        // Inline styles referencing an external url(...).
        if (
          name === 'style' &&
          /url\s*\(\s*['"]?\s*(?:[a-z][a-z0-9+.-]*:|\/\/)/i.test(attr.value)
        ) {
          el.removeAttribute(attr.name);
        }
      });
    });

    // 4) Inject strict CSP + readable styling into <head>.
    const head = doc.head ?? doc.documentElement;
    const csp = doc.createElement('meta');
    csp.setAttribute('http-equiv', 'Content-Security-Policy');
    csp.setAttribute(
      'content',
      "default-src 'none'; img-src data:; style-src 'unsafe-inline'; " +
        "font-src data:; base-uri 'none'; form-action 'none'; frame-src 'none'"
    );
    head.insertBefore(csp, head.firstChild);

    const baseStyle =
      'html,body{margin:0;}' +
      'body{font-family:-apple-system,Segoe UI,Roboto,Arial,sans-serif;' +
      'line-height:1.5;color:#222;}' +
      'pre{background:#f5f5f5;padding:8px;overflow:auto;}' +
      'code{background:#f5f5f5;padding:2px 4px;}' +
      'table{border-collapse:collapse;}' +
      'th,td{border:1px solid #ccc;padding:4px 8px;}' +
      'img{max-width:100%;}' +
      'blockquote{border-left:4px solid #ddd;margin:0;padding-left:12px;color:#555;}';
    // Full-width mode (Excel/CSV tables): the body itself becomes a bounded,
    // scrollable viewport (height = iframe height) so the horizontal scrollbar
    // sits at the bottom of the visible area; the table keeps its intrinsic
    // width and cells never wrap.
    const layoutStyle = fullWidth
      ? 'html,body{height:100%;}' +
        'body{padding:16px;box-sizing:border-box;overflow:auto;}' +
        'table{width:max-content;max-width:none;}' +
        'th,td{white-space:nowrap;}'
      : 'body{padding:16px;max-width:900px;margin:0 auto;overflow:auto;}';
    const style = doc.createElement('style');
    style.textContent = baseStyle + layoutStyle;
    head.appendChild(style);

    return `<!DOCTYPE html>${doc.documentElement.outerHTML}`;
  }

  /** True when the URL targets an external origin (has a scheme or is protocol-relative). */
  private isExternalUrl(url: string | null): boolean {
    if (!url) {
      return false;
    }
    const value = url.trim();
    if (value === '' || value.startsWith('#')) {
      return false;
    }
    return /^(?:[a-z][a-z0-9+.-]*:|\/\/)/i.test(value);
  }

  /** Detects the most likely delimiter used in the first line of a CSV. */
  private detectCsvDelimiter(text: string): string {
    const firstLine = text.split(/\r?\n/, 1)[0] ?? '';
    const candidates = [',', ';', '\t', '|'];
    let best = ',';
    let bestCount = -1;
    for (const delimiter of candidates) {
      const count = firstLine.split(delimiter).length - 1;
      if (count > bestCount) {
        bestCount = count;
        best = delimiter;
      }
    }
    return best;
  }

  /** Minimal RFC-4180-ish CSV parser (handles quotes, escaped quotes). */
  private parseCsv(text: string): string[][] {
    const delimiter = this.detectCsvDelimiter(text);
    const rows: string[][] = [];
    let field = '';
    let row: string[] = [];
    let inQuotes = false;
    for (let i = 0; i < text.length; i++) {
      const char = text[i];
      if (inQuotes) {
        if (char === '"') {
          if (text[i + 1] === '"') {
            field += '"';
            i++;
          } else {
            inQuotes = false;
          }
        } else {
          field += char;
        }
      } else if (char === '"') {
        inQuotes = true;
      } else if (char === delimiter) {
        row.push(field);
        field = '';
      } else if (char === '\r') {
        // ignored; handled together with \n
      } else if (char === '\n') {
        row.push(field);
        rows.push(row);
        row = [];
        field = '';
      } else {
        field += char;
      }
    }
    if (field !== '' || row.length > 0) {
      row.push(field);
      rows.push(row);
    }
    return rows;
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
  private isText(): boolean {
    return isContentText(this.content());
  }
  private isHtml(): boolean {
    return isContentHtml(this.content());
  }
  private isCsv(): boolean {
    return isContentCsv(this.content());
  }
  private isMarkdown(): boolean {
    return isContentMarkdown(this.content());
  }
  private isSvg(): boolean {
    return isContentSvg(this.content());
  }
  private isWord(): boolean {
    return isContentWord(this.content());
  }
  private isExcel(): boolean {
    return isContentExcel(this.content());
  }
  private isTiff(): boolean {
    return isContentTiff(this.content());
  }
  private isRtf(): boolean {
    return isContentRtf(this.content());
  }
  private isPptx(): boolean {
    return isContentPptx(this.content());
  }
  private isWebp(): boolean {
    return isContentWebp(this.content());
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
      !this.isText() &&
      !this.isHtml() &&
      !this.isCsv() &&
      !this.isMarkdown() &&
      !this.isSvg() &&
      !this.isWord() &&
      !this.isExcel() &&
      !this.isTiff() &&
      !this.isRtf() &&
      !this.isPptx() &&
      !this.isWebp() &&
      this.previewReady
    );
  }

  private showImagePreview(): boolean {
    return this.contentURL() !== '' && this.isImage();
  }

  private showTextPreview(): boolean {
    return this.isText() && this.previewReady;
  }

  private showHtmlPreview(): boolean {
    return this.isHtml() && this.previewReady;
  }

  private showCsvPreview(): boolean {
    return this.isCsv() && this.previewReady;
  }

  private showMarkdownPreview(): boolean {
    return this.isMarkdown() && this.previewReady;
  }

  private showSvgPreview(): boolean {
    return this.isSvg() && this.previewReady;
  }

  private showWordPreview(): boolean {
    return this.isWord() && this.previewReady;
  }

  private showExcelPreview(): boolean {
    return this.isExcel() && this.previewReady;
  }

  private showTiffPreview(): boolean {
    return this.isTiff() && this.previewReady;
  }

  private showRtfPreview(): boolean {
    return this.isRtf() && this.previewReady;
  }

  private showPptxPreview(): boolean {
    return this.isPptx() && this.previewReady;
  }

  private showWebpPreview(): boolean {
    return this.isWebp() && this.previewReady;
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
    this.textContent = '';
    this.htmlContent = undefined;
    this.imageDataUrl = '';
    this.pptxBuffer = undefined;
    this.exitFullscreen();
  }
}
