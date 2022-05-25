import { Component, Input } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';

import { News } from 'app/core/generated/circabc';
import { SaveAsService } from 'app/core/save-as.service';
import { urlWellFormed } from 'app/core/util';

@Component({
  selector: 'cbc-simple-news-card',
  templateUrl: './simple-news-card.component.html',
  styleUrls: ['./simple-news-card.component.scss'],
  preserveWhitespaces: true,
})
export class SimpleNewsCardComponent {
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
    if (this.news && this.news.properties && this.news.properties.newsLayout) {
      return this.news.properties.newsLayout === 'important';
    }
    return false;
  }

  isReminder(): boolean {
    if (this.news && this.news.properties && this.news.properties.newsLayout) {
      return this.news.properties.layout === 'reminder';
    }
    return false;
  }

  private isNewsPattern(pattern: 'image' | 'document' | 'date'): boolean {
    if (this.news && this.news.properties && this.news.properties.newsPattern) {
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
    if (this.news && this.news.properties && this.news.properties.newsPattern) {
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
    if (this.news && this.news.properties && this.news.properties.newsContent) {
      return this.sanitizer.bypassSecurityTrustHtml(
        this.news.properties.newsContent
      );
    }

    return '';
  }

  getNewsSize(): number {
    if (this?.news?.size !== undefined) {
      return +this.news.size;
    } else {
      return 1;
    }
  }

  public hasValidUrl(): boolean {
    return (
      this.news !== undefined &&
      this.news.properties !== undefined &&
      this.news.properties.newsUrl !== undefined &&
      this.news.properties.newsUrl !== '' &&
      urlWellFormed(this.news.properties.newsUrl)
    );
  }

  public getAuthor(): string {
    if (this?.news?.modifier !== undefined) {
      return this.news.modifier;
    } else {
      return 'John Doe';
    }
  }

  public getDate(): Date {
    if (this?.news?.modified !== undefined) {
      return new Date(this.news.modified);
    } else {
      return new Date();
    }
  }
}
