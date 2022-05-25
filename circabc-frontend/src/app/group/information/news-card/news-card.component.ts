import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  Output,
  SimpleChanges,
} from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';

import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { InformationService, News } from 'app/core/generated/circabc';
import { SaveAsService } from 'app/core/save-as.service';
import { urlWellFormed } from 'app/core/util';
import { firstValueFrom } from 'rxjs';
import { environment } from 'environments/environment';
import { LoginService } from 'app/core/login.service';

@Component({
  selector: 'cbc-news-card',
  templateUrl: './news-card.component.html',
  styleUrls: ['./news-card.component.scss'],
  preserveWhitespaces: true,
})
export class NewsCardComponent implements OnChanges {
  @Input()
  news: News | undefined;
  @Input()
  previewImageLocal: File | undefined;
  @Input()
  hideActions = false;
  @Input()
  highlighted = false;
  @Input()
  preview = false;
  @Output()
  readonly newsDeleted = new EventEmitter<ActionEmitterResult>();
  @Output()
  readonly newsClicked = new EventEmitter();

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
    if (changes && changes.previewImageLocal) {
      if (
        changes.previewImageLocal.currentValue !==
        changes.previewImageLocal.previousValue
      ) {
        this.reflectImagePreviewChange = true;
      }
    }
  }

  titleIsNotEmpty(): boolean {
    return this.news !== undefined && this.news.title !== undefined;
  }

  async deleteNews() {
    if (this.news && this.news.id) {
      const result: ActionEmitterResult = {};
      result.type = ActionType.DELETE_INFORMATION_NEWS;

      try {
        await firstValueFrom(this.informationService.deleteNews(this.news.id));
        result.result = ActionResult.SUCCEED;
      } catch (error) {
        result.result = ActionResult.FAILED;
      }
      this.newsDeleted.emit(result);
    }
  }

  isImage(): boolean {
    if (this.news && this.news.pattern) {
      return this.news.pattern === 'image';
    }
    return false;
  }

  hasPreviewImage(): boolean {
    if (this.isImage()) {
      return this.previewImageLocal !== undefined;
    }
    return false;
  }

  public getPreviewImage() {
    if (this.reflectImagePreviewChange) {
      this.reflectImagePreviewChange = false;
      if (this.previewImageLocal !== undefined) {
        this.imagePreviewObjectUrl = window.URL.createObjectURL(
          this.previewImageLocal
        );
      }
    }
    return this.sanitizer.bypassSecurityTrustStyle(this.imagePreviewObjectUrl);
  }

  public getPreviewPDF() {
    if (this.previewImageLocal !== undefined) {
      return this.previewImageLocal;
    }

    if (this.news && this.news.files && this.news.files[0]) {
      this.contentURL = `${
        environment.serverURL
      }pdfRendition?documentId=workspace://SpacesStore/${
        this.news.files[0].id
      }&response=content&ticket=${this.loginService.getTicket()}&dummy=false`;
    }

    return this.contentURL;
  }

  isLayout(item: 'important' | 'reminder'): boolean {
    if (this.news && this.news.layout) {
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
    if (this.news && this.news.pattern) {
      return this.news.pattern === item;
    }
    return false;
  }

  isDocument(): boolean {
    return this.isPattern('document');
  }

  isPDFDocument(): boolean {
    const filename = this.previewImageLocal;
    if (filename !== undefined) {
      return filename.name.includes('.pdf');
    }
    if (this.news && this.news.files && this.news.files.length > 0) {
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
    if (this.news && this.news.files && this.news.files.length > 0) {
      return this.news.files[0].name;
    } else {
      return undefined;
    }
  }

  getNewsFileId() {
    if (this.news && this.news.files && this.news.files.length > 0) {
      return this.news.files[0].id;
    } else {
      return undefined;
    }
  }

  public hasFile(): boolean {
    if (this.news && this.news.files && this.news.files.length > 0) {
      return true;
    } else {
      return false;
    }
  }

  isNewsManage(): boolean {
    if (this.news && this.news.permissions) {
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
    if (url && url.changingThisBreaksApplicationSecurity) {
      return this.sanitizer.bypassSecurityTrustStyle(
        `url(${url.changingThisBreaksApplicationSecurity})`
      );
    } else {
      return undefined;
    }
  }

  getSafeUrl() {
    if (this.news && this.news.url) {
      return this.news.url;
    }

    return '';
  }

  getSanitizedContent() {
    if (this.news && this.news.content) {
      return this.sanitizer.bypassSecurityTrustHtml(this.news.content);
    }

    return '';
  }

  getNewsSize(): number {
    if (this.news && this.news.size) {
      return this.news.size;
    } else {
      return 1;
    }
  }

  public hasValidUrl(): boolean {
    return (
      this.news !== undefined &&
      this.news.url !== undefined &&
      this.news.url !== '' &&
      urlWellFormed(this.news.url)
    );
  }

  public getAuthor(): string {
    if (this.news && this.news.modifier && !this.preview) {
      return this.news.modifier;
    } else {
      return 'John Doe';
    }
  }

  public getDate(): Date {
    if (this.news && this.news.modified && !this.preview) {
      return new Date(this.news.modified);
    } else {
      return new Date();
    }
  }

  public getDateCreated(): Date {
    if (this.news && this.news.created && !this.preview) {
      return new Date(this.news.created);
    } else {
      return new Date();
    }
  }

  public isEdited(): boolean {
    if (this.news && this.news.modified && this.news.created) {
      const modified = `${this.news.modified.toString()}`;
      const created = `${this.news.created.toString()}`;
      return modified.substring(0, 16) !== created.substring(0, 16);
    }
    return false;
  }

  public propagateClick() {
    this.newsClicked.emit();
  }
}
