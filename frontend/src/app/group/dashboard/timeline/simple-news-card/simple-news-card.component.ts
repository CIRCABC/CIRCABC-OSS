import { DatePipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
} from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
import { TranslocoModule } from '@jsverse/transloco';
import { News } from 'app/core/generated/circabc';
import { SaveAsService } from 'app/core/save-as.service';
import { DownloadPipe } from 'app/shared/pipes/download.pipe';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { isAllowedResourceUrl, SafePipe } from 'app/shared/pipes/safe.pipe';
import { SecurePipe } from 'app/shared/pipes/secure.pipe';
import { UserCardComponent } from 'app/shared/user-card/user-card.component';

/**
 * Presentational card that renders a single {@link News} item within the group
 * dashboard timeline.
 *
 * Depending on the news item's layout and pattern properties, the card can be
 * displayed in different visual variants (important, reminder) and render
 * different kinds of content (image, document, date, iframe, rich HTML).
 * It also exposes helpers to safely sanitize backend-provided URLs/HTML,
 * download the attached document, and surface author/date metadata for the
 * embedded {@link UserCardComponent}.
 *
 * Key collaborators:
 * - {@link SaveAsService} to trigger download of the attached news document.
 * - {@link DomSanitizer} to mark trusted backend-provided HTML and CSS URLs.
 */
@Component({
  selector: 'cbc-simple-news-card',
  templateUrl: './simple-news-card.component.html',
  styleUrl: './simple-news-card.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
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
  /** Service used to download the news attachment as a file. */
  private readonly saveAsService = inject(SaveAsService);
  /** Angular sanitizer used to bypass security for trusted backend content. */
  private readonly sanitizer = inject(DomSanitizer);

  /**
   * The news item to render in this card. Provided by the parent timeline
   * component via the `news` input.
   */
  readonly news = input<News>();

  /**
   * Indicates whether the news item has a defined title.
   *
   * @returns `true` when the news item exposes a title, otherwise `false`.
   */
  titleIsNotEmpty(): boolean {
    return this?.news()?.title !== undefined;
  }

  /**
   * Indicates whether the news item should be rendered with the "important"
   * layout emphasis.
   *
   * @returns `true` when the news layout is `'important'`, otherwise `false`.
   */
  isImportant(): boolean {
    const news = this.news();
    if (news?.properties?.newsLayout) {
      return news.properties.newsLayout === 'important';
    }
    return false;
  }

  /**
   * Indicates whether the news item should be rendered with the "reminder"
   * layout.
   *
   * @returns `true` when the news layout is `'reminder'`, otherwise `false`.
   */
  isReminder(): boolean {
    const news = this.news();
    if (news?.properties?.newsLayout) {
      return news.properties.layout === 'reminder';
    }
    return false;
  }

  /**
   * Checks whether the news item's rendering pattern matches the given value.
   *
   * @param pattern - The news pattern to compare against
   * (`'image'`, `'document'` or `'date'`).
   * @returns `true` when the news pattern equals `pattern`, otherwise `false`.
   */
  private isNewsPattern(pattern: 'image' | 'document' | 'date'): boolean {
    const news = this.news();
    if (news?.properties?.newsPattern) {
      return news.properties.newsPattern === pattern;
    }
    return false;
  }

  /**
   * Indicates whether the news item should be rendered using the image pattern.
   *
   * @returns `true` when the news pattern is `'image'`, otherwise `false`.
   */
  isImage(): boolean {
    return this.isNewsPattern('image');
  }

  /**
   * Indicates whether the news item should be rendered using the document
   * pattern.
   *
   * @returns `true` when the news pattern is `'document'`, otherwise `false`.
   */
  isDocument(): boolean {
    return this.isNewsPattern('document');
  }

  /**
   * Indicates whether the news item should be rendered using the date pattern.
   *
   * @returns `true` when the news pattern is `'date'`, otherwise `false`.
   */
  isDate(): boolean {
    return this.isNewsPattern('date');
  }

  /**
   * Indicates whether the news item should be rendered as an embedded iframe.
   *
   * @returns `true` when the news pattern is `'iframe'`, otherwise `false`.
   */
  isIframe(): boolean {
    const news = this.news();
    if (news?.properties?.newsPattern) {
      return news.properties.newsPattern === 'iframe';
    }
    return false;
  }

  /**
   * Returns the file name of the document attached to the news item, if any.
   *
   * @returns The attached document's name, or `undefined` when none is set.
   */
  getNewsFileName() {
    return this.news()?.properties?.newsDocName;
  }

  /**
   * Returns the node identifier of the document attached to the news item, if
   * any.
   *
   * @returns The attached document's id, or `undefined` when none is set.
   */
  getNewsFileId() {
    return this.news()?.properties?.newsDocId;
  }

  /**
   * Triggers a download of the attached news document through the
   * {@link SaveAsService}. No action is taken when the document id or name is
   * missing.
   */
  saveFile() {
    const id = this.getNewsFileId();
    const name = this.getNewsFileName();
    if (id !== undefined && name !== undefined) {
      this.saveAsService.saveAs(id, name);
    }
  }

  /**
   * Builds a sanitized CSS `url(...)` value from a secured URL so it can safely
   * be used as a background image. The incoming value originates from the
   * {@link SecurePipe}, which fetches authenticated content from the backend.
   *
   * @param url - The secured URL wrapper produced by the `SecurePipe`.
   * @returns A trusted CSS style value, or `undefined` when no URL is present.
   */
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  prepareUrl(url: any) {
    if (url?.changingThisBreaksApplicationSecurity) {
      // prettier-ignore
      return this.sanitizer.bypassSecurityTrustStyle(`url(${url.changingThisBreaksApplicationSecurity})`); // NOSONAR Safe - URL comes from SecurePipe which fetches authenticated content from backend API
    }
    return undefined;
  }

  /**
   * Returns the news item's URL used for linking/embedding.
   *
   * @returns The news URL, or an empty string when none is set.
   */
  getSafeUrl() {
    const news = this.news();
    if (news?.url) {
      return news.url;
    }

    return '';
  }

  /**
   * Returns the news rich-text content as a plain string. Returning the raw
   * string lets Angular sanitize it when bound via `[innerHTML]`, preventing
   * stored cross-site scripting from news content.
   *
   * @returns The news HTML content, or an empty string when none is set.
   */
  getSanitizedContent(): string {
    const news = this.news();
    if (news?.properties?.newsContent) {
      return news.properties.newsContent;
    }

    return '';
  }

  /**
   * Returns the relative display size of the news card, used by the timeline
   * layout to allocate space.
   *
   * @returns The numeric size of the news item, defaulting to `1` when unset.
   */
  getNewsSize(): number {
    const news = this?.news();
    if (news?.size !== undefined) {
      return +news.size;
    }
    return 1;
  }

  /**
   * Indicates whether the news item carries a safe, absolute `http`/`https`
   * URL that can be embedded.
   *
   * @returns `true` when `newsUrl` is an absolute `http`/`https` URL, otherwise
   * `false`.
   */
  public hasValidUrl(): boolean {
    return isAllowedResourceUrl(this.news()?.properties?.newsUrl);
  }

  /**
   * Returns the identifier of the user who last modified the news item, used to
   * populate the embedded user card.
   *
   * @returns The modifier's identifier, or the placeholder `'John Doe'` when
   * unset.
   */
  public getAuthor(): string {
    const news = this?.news();
    if (news?.modifier !== undefined) {
      return news.modifier;
    }
    return 'John Doe';
  }

  /**
   * Returns the last modification date of the news item.
   *
   * @returns The modification date, or the current date when no modification
   * date is available.
   */
  public getDate(): Date {
    const news = this?.news();
    if (news?.modified !== undefined) {
      return new Date(news.modified);
    }
    return new Date();
  }
}
