import {
  Component,
  Input,
  OnChanges,
  SimpleChanges,
  output,
  input,
} from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';

import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { InformationService, News } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { SaveAsService } from 'app/core/save-as.service';
import { urlWellFormed } from 'app/core/util';
import { InlineDeleteComponent } from 'app/shared/delete/inline-delete.component';
import { IfRoleGEDirective } from 'app/shared/directives/ifrolege.directive';
import { DownloadPipe } from 'app/shared/pipes/download.pipe';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { SafePipe } from 'app/shared/pipes/safe.pipe';
import { SecurePipe } from 'app/shared/pipes/secure.pipe';
import { UserCardComponent } from 'app/shared/user-card/user-card.component';
import { environment } from 'environments/environment';
import { NgxExtendedPdfViewerModule } from 'ngx-extended-pdf-viewer';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-news-card',
  templateUrl: './news-card.component.html',
  styleUrl: './news-card.component.scss',
  preserveWhitespaces: true,
  imports: [
    NgxExtendedPdfViewerModule,
    UserCardComponent,
    IfRoleGEDirective,
    RouterLink,
    InlineDeleteComponent,
    DatePipe,
    DownloadPipe,
    I18nPipe,
    SafePipe,
    SecurePipe,
    TranslocoModule,
  ],
})
export class NewsCardComponent implements OnChanges {
  // TODO: Skipped for migration because:
  //  This input is used in a control flow expression (e.g. `@if` or `*ngIf`)
  //  and migrating would break narrowing currently.
  @Input()
  news: News | undefined;
  readonly previewImageLocal = input<File>();
  readonly hideActions = input(false);
  readonly highlighted = input(false);
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  highlightedMaximized = false;
  readonly preview = input(false);
  readonly newsDeleted = output<ActionEmitterResult>();
  readonly newsClicked = output();
  readonly highlightedMaxWindow = output<boolean>();

  private reflectImagePreviewChange = false;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  private imagePreviewObjectUrl: any;
  public showCreator = false;

  public contentURL!: string;

  constructor(
    private informationService: InformationService,
    private saveAsService: SaveAsService,
    private sanitizer: DomSanitizer,
    private loginService: LoginService
  ) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes?.previewImageLocal) {
      if (
        changes.previewImageLocal.currentValue !==
        changes.previewImageLocal.previousValue
      ) {
        this.reflectImagePreviewChange = true;
      }
    }
  }

  titleIsNotEmpty(): boolean {
    return this.news?.title !== undefined;
  }

  async deleteNews() {
    if (this.news?.id) {
      const result: ActionEmitterResult = {};
      result.type = ActionType.DELETE_INFORMATION_NEWS;

      try {
        await firstValueFrom(this.informationService.deleteNews(this.news.id));
        result.result = ActionResult.SUCCEED;
      } catch (_error) {
        result.result = ActionResult.FAILED;
      }
      this.newsDeleted.emit(result);
    }
  }

  isImage(): boolean {
    if (this.news?.pattern) {
      return this.news.pattern === 'image';
    }
    return false;
  }

  hasPreviewImage(): boolean {
    if (this.isImage()) {
      return this.previewImageLocal() !== undefined;
    }
    return false;
  }

  public getPreviewImage() {
    if (this.reflectImagePreviewChange) {
      this.reflectImagePreviewChange = false;
      const previewImageLocal = this.previewImageLocal();
      if (previewImageLocal !== undefined) {
        this.imagePreviewObjectUrl =
          window.URL.createObjectURL(previewImageLocal);
      }
    }
    return this.sanitizer.bypassSecurityTrustStyle(this.imagePreviewObjectUrl);
  }

  public getPreviewPDF() {
    const previewImageLocal = this.previewImageLocal();
    if (previewImageLocal !== undefined) {
      return previewImageLocal;
    }

    if (this.news?.files?.[0]) {
      if (environment.useAlfrescoAPI) {
        this.contentURL = `${environment.serverURL}api/-default-/public/alfresco/versions/1/nodes/${this.news.files[0].id}/content?attachment=false&alf_ticket=${this.loginService.getTicket()}`;
      } else {
        this.contentURL = `${
          environment.serverURL
        }pdfRendition?documentId=workspace://SpacesStore/${
          this.news.files[0].id
        }&response=content&ticket=${this.loginService.getTicket()}&dummy=false`;
      }
    }

    return this.contentURL;
  }

  isLayout(item: 'important' | 'reminder'): boolean {
    if (this.news?.layout) {
      return this.news.layout === item;
    }
    return false;
  }

  isImportant(): boolean {
    return this.isLayout('important');
  }

  isReminder(): boolean {
    return this.isLayout('reminder');
  }

  private isPattern(item: 'document' | 'date' | 'iframe'): boolean {
    if (this.news?.pattern) {
      return this.news.pattern === item;
    }
    return false;
  }

  isDocument(): boolean {
    return this.isPattern('document');
  }

  isPDFDocument(): boolean {
    const filename = this.previewImageLocal();
    if (filename !== undefined) {
      return filename.name.includes('.pdf');
    }
    if (this.news?.files && this.news.files.length > 0) {
      if (this.news.files[0].name) {
        return this.news.files[0].name.includes('.pdf');
      }
    }

    return false;
  }

  isDate(): boolean {
    return this.isPattern('date');
  }

  isIframe(): boolean {
    return this.isPattern('iframe');
  }

  getNewsFileName() {
    if (this.news?.files && this.news.files.length > 0) {
      return this.news.files[0].name;
    }
    return undefined;
  }

  getNewsFileId() {
    if (this.news?.files && this.news.files.length > 0) {
      return this.news.files[0].id;
    }
    return undefined;
  }

  public hasFile(): boolean {
    if (this.news?.files && this.news.files.length > 0) {
      return true;
    }
    return false;
  }

  isNewsManage(): boolean {
    if (this.news?.permissions) {
      return (
        this.news.permissions.InfManage === 'ALLOWED' ||
        this.news.permissions.InfAdmin === 'ALLOWED'
      );
    }

    return false;
  }

  saveFile() {
    const id = this.getNewsFileId();
    const name = this.getNewsFileName();
    if (id !== undefined && name !== undefined) {
      this.saveAsService.saveAs(id, name);
    }
  }

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  prepareUrl(url: any) {
    if (url?.changingThisBreaksApplicationSecurity) {
      return this.sanitizer.bypassSecurityTrustStyle(
        `url(${url.changingThisBreaksApplicationSecurity})`
      );
    }
    return undefined;
  }

  getSafeUrl() {
    if (this.news?.url) {
      return this.news.url;
    }

    return '';
  }

  getSanitizedContent() {
    if (this.news?.content) {
      return this.sanitizer.bypassSecurityTrustHtml(this.news.content);
    }

    return '';
  }

  getNewsSize(): number {
    if (this.news?.size) {
      return this.news.size;
    }
    return 1;
  }

  public hasValidUrl(): boolean {
    return (
      this.news?.url !== undefined &&
      this.news.url !== '' &&
      urlWellFormed(this.news.url)
    );
  }

  public getAuthor(): string {
    if (this.news?.modifier && !this.preview()) {
      return this.news.modifier;
    }
    return 'John Doe';
  }

  public getDate(): Date {
    if (this.news?.modified && !this.preview()) {
      return new Date(this.news.modified);
    }
    return new Date();
  }

  public getDateCreated(): Date {
    if (this.news?.created && !this.preview()) {
      return new Date(this.news.created);
    }
    return new Date();
  }

  public isEdited(): boolean {
    if (this.news?.modified && this.news.created) {
      const modified = `${this.news.modified.toString()}`;
      const created = `${this.news.created.toString()}`;
      return modified.substring(0, 16) !== created.substring(0, 16);
    }
    return false;
  }

  public propagateClick() {
    this.newsClicked.emit();
  }

  public highlightedMaxWindowAction(value: boolean) {
    this.highlightedMaximized = value;
    this.highlightedMaxWindow.emit(value);
  }
}
