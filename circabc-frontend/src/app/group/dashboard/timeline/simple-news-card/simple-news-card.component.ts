import { Component, Input } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';

import { DatePipe } from '@angular/common';
import { TranslocoModule } from '@jsverse/transloco';
import { News } from 'app/core/generated/circabc';
import { SaveAsService } from 'app/core/save-as.service';
import { urlWellFormed } from 'app/core/util';
import { DownloadPipe } from 'app/shared/pipes/download.pipe';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { SafePipe } from 'app/shared/pipes/safe.pipe';
import { SecurePipe } from 'app/shared/pipes/secure.pipe';
import { UserCardComponent } from 'app/shared/user-card/user-card.component';

@Component({
  selector: 'cbc-simple-news-card',
  templateUrl: './simple-news-card.component.html',
  styleUrl: './simple-news-card.component.scss',
  preserveWhitespaces: true,
  imports: [
    UserCardComponent,
    DatePipe,
    DownloadPipe,
    I18nPipe,
    SafePipe,
    SecurePipe,
    TranslocoModule,
  ],
})
export class SimpleNewsCardComponent {
  // TODO: Skipped for migration because:
  //  This input is used in a control flow expression (e.g. `@if` or `*ngIf`)
  //  and migrating would break narrowing currently.
  @Input()
  news: News | undefined;

  constructor(
    private saveAsService: SaveAsService,
    private sanitizer: DomSanitizer
  ) {}

  titleIsNotEmpty(): boolean {
    return this?.news?.title !== undefined;
  }

  isImportant(): boolean {
    if (this.news?.properties?.newsLayout) {
      return this.news.properties.newsLayout === 'important';
    }
    return false;
  }

  isReminder(): boolean {
    if (this.news?.properties?.newsLayout) {
      return this.news.properties.layout === 'reminder';
    }
    return false;
  }

  private isNewsPattern(pattern: 'image' | 'document' | 'date'): boolean {
    if (this.news?.properties?.newsPattern) {
      return this.news.properties.newsPattern === pattern;
    }
    return false;
  }

  isImage(): boolean {
    return this.isNewsPattern('image');
  }

  isDocument(): boolean {
    return this.isNewsPattern('document');
  }

  isDate(): boolean {
    return this.isNewsPattern('date');
  }

  isIframe(): boolean {
    if (this.news?.properties?.newsPattern) {
      return this.news.properties.newsPattern === 'iframe';
    }
    return false;
  }

  getNewsFileName() {
    return this.news?.properties?.newsDocName;
  }

  getNewsFileId() {
    return this.news?.properties?.newsDocId;
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
    if (this.news?.properties?.newsContent) {
      return this.sanitizer.bypassSecurityTrustHtml(
        this.news.properties.newsContent
      );
    }

    return '';
  }

  getNewsSize(): number {
    if (this?.news?.size !== undefined) {
      return +this.news.size;
    }
    return 1;
  }

  public hasValidUrl(): boolean {
    return (
      this.news?.properties?.newsUrl !== undefined &&
      this.news.properties.newsUrl !== '' &&
      urlWellFormed(this.news.properties.newsUrl)
    );
  }

  public getAuthor(): string {
    if (this?.news?.modifier !== undefined) {
      return this.news.modifier;
    }
    return 'John Doe';
  }

  public getDate(): Date {
    if (this?.news?.modified !== undefined) {
      return new Date(this.news.modified);
    }
    return new Date();
  }
}
