import { DatePipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  effect,
  inject,
  input,
  output,
  signal,
} from '@angular/core';
import { DomSanitizer, SafeStyle } from '@angular/platform-browser';
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
import { InlineDeleteComponent } from 'app/shared/delete/inline-delete.component';
import { IfRoleGEDirective } from 'app/shared/directives/ifrolege.directive';
import { DownloadPipe } from 'app/shared/pipes/download.pipe';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { isAllowedResourceUrl, SafePipe } from 'app/shared/pipes/safe.pipe';
import { SecurePipe } from 'app/shared/pipes/secure.pipe';
import { UserCardComponent } from 'app/shared/user-card/user-card.component';
import { environment } from 'environments/environment';
import { NgxExtendedPdfViewerModule } from 'ngx-extended-pdf-viewer';

/**
 * Standalone Angular component (`cbc-news-card`) that renders a single news
 * item within a group's information service.
 *
 * Depending on the underlying {@link News} item's `pattern` and `layout`, the
 * card adapts its presentation to display different content types: plain HTML
 * content, an image, a linked/embedded document (including a PDF preview via
 * `ngx-extended-pdf-viewer`), a date-based reminder, or an external iframe/URL.
 * It also surfaces authoring metadata (author, created/modified dates) and,
 * for users with sufficient permissions, management actions such as deleting
 * the news item.
 *
 * The component can operate in a live mode (bound to a persisted `News` item)
 * or in a preview mode used while composing/editing a news item, in which case
 * a locally selected file ({@link NewsCardComponent.previewImageLocal}) is used
 * to render the image/PDF preview.
 *
 * All derived view state is exposed as {@link computed} signals so the template
 * stays reactive under {@link ChangeDetectionStrategy.OnPush}.
 *
 * Key collaborators:
 * - {@link InformationService} — backend API used to delete news items.
 * - {@link SaveAsService} — triggers download of attached files.
 * - {@link DomSanitizer} — bypasses Angular security for trusted, backend- or
 *   locally-sourced content, styles and URLs.
 * - {@link LoginService} — provides the authentication ticket used to build
 *   content/PDF rendition URLs.
 */
@Component({
  selector: 'cbc-news-card',
  templateUrl: './news-card.component.html',
  styleUrl: './news-card.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
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
export class NewsCardComponent {
  /** Backend API client used to delete news items. */
  private readonly informationService = inject(InformationService);
  /** Service used to trigger download ("save as") of attached files. */
  private readonly saveAsService = inject(SaveAsService);
  /** Sanitizer used to mark trusted content, styles and URLs as safe. */
  private readonly sanitizer = inject(DomSanitizer);
  /** Provides the current authentication ticket for building content URLs. */
  private readonly loginService = inject(LoginService);

  /** Input: the news item to render. */
  readonly news = input<News>();
  /**
   * Input: a locally selected file used to render an image or PDF preview
   * while composing/editing a news item (before it is persisted).
   */
  readonly previewImageLocal = input<File>();
  /** Input: when `true`, hides the management/action controls on the card. */
  readonly hideActions = input(false);
  /** Input: when `true`, renders the card in a highlighted state. */
  readonly highlighted = input(false);
  /**
   * Input: when `true`, the card is rendered in preview mode (e.g. while
   * composing a news item), using placeholder author/date values.
   */
  readonly preview = input(false);

  /**
   * Output: emitted after a delete attempt, carrying the {@link ActionEmitterResult}
   * describing whether the deletion succeeded or failed.
   */
  readonly newsDeleted = output<ActionEmitterResult>();
  /** Output: emitted when the card is clicked. */
  readonly newsClicked = output();
  /**
   * Output: emitted when the highlighted card is maximized (`true`) or
   * restored (`false`).
   */
  readonly highlightedMaxWindow = output<boolean>();

  /** Whether the highlighted card is currently displayed maximized. */
  public highlightedMaximized = false;
  /** Whether the news creator (as opposed to modifier) should be shown. */
  public showCreator = false;
  /** URL pointing to the news attachment content (used for PDF rendering). */
  public contentURL!: string;

  /**
   * Object URL created from the locally selected preview file. Kept in a signal
   * so it can be consumed reactively and revoked when the file changes or the
   * component is destroyed (see the constructor effect).
   */
  private readonly previewObjectUrl = signal<string | undefined>(undefined);

  constructor() {
    const destroyRef = inject(DestroyRef);
    // Create an object URL for the locally selected image preview and revoke it
    // whenever the file changes or the component is destroyed, avoiding leaks.
    effect((onCleanup) => {
      const file = this.previewImageLocal();
      if (file === undefined || !this.isImage()) {
        this.previewObjectUrl.set(undefined);
        return;
      }
      const url = globalThis.URL.createObjectURL(file);
      this.previewObjectUrl.set(url);
      onCleanup(() => globalThis.URL.revokeObjectURL(url));
    });
    destroyRef.onDestroy(() => {
      const url = this.previewObjectUrl();
      if (url !== undefined) {
        globalThis.URL.revokeObjectURL(url);
      }
    });
  }

  /**
   * Whether the current news item has a defined (non-empty) title.
   */
  readonly titleIsNotEmpty = computed<boolean>(
    () => this.news()?.title !== undefined
  );

  /**
   * Deletes the current news item via {@link InformationService} and emits the
   * outcome through {@link newsDeleted}.
   *
   * Does nothing if there is no current news item or it has no id. Deletion
   * failures are caught and reported as a failed {@link ActionResult} rather
   * than being propagated.
   *
   * @returns A promise that resolves once the deletion attempt has completed
   *   and the result has been emitted.
   */
  async deleteNews() {
    const news = this.news();
    if (news?.id) {
      const result: ActionEmitterResult = {};
      result.type = ActionType.DELETE_INFORMATION_NEWS;

      try {
        await this.informationService.deleteNewsAsync({ id: news.id });
        result.result = ActionResult.SUCCEED;
      } catch (error) {
        console.error(error);
        result.result = ActionResult.FAILED;
      }
      this.newsDeleted.emit(result);
    }
  }

  /** Whether the current news item follows the `image` pattern. */
  readonly isImage = computed<boolean>(() => this.news()?.pattern === 'image');

  /**
   * Whether an image news item has a locally selected preview file.
   */
  readonly hasPreviewImage = computed<boolean>(
    () => this.isImage() && this.previewImageLocal() !== undefined
  );

  /**
   * Returns a sanitized CSS style value referencing the locally selected image
   * preview, or `undefined` when there is no preview file.
   *
   * @returns A `SafeStyle` wrapping the preview image object URL, or
   *   `undefined`.
   */
  readonly getPreviewImage = computed<SafeStyle | undefined>(() => {
    const url = this.previewObjectUrl();
    // NOSONAR: Safe - URL is created locally from user-uploaded file via URL.createObjectURL
    return url === undefined
      ? undefined
      : this.sanitizer.bypassSecurityTrustStyle(url); // NOSONAR
  });

  /**
   * Resolves the source used to render the PDF preview.
   *
   * If a locally selected file is present it is returned directly. Otherwise a
   * backend content/rendition URL is built for the first attached file, using
   * either the Alfresco public API or the legacy PDF rendition endpoint
   * depending on the environment configuration, and including the current
   * authentication ticket.
   *
   * @returns The locally selected `File` when available, otherwise the built
   *   content URL string.
   */
  public getPreviewPDF() {
    const previewImageLocal = this.previewImageLocal();
    if (previewImageLocal !== undefined) {
      return previewImageLocal;
    }

    const news = this.news();
    if (news?.files?.[0]) {
      if (environment.useAlfrescoAPI) {
        this.contentURL = `${environment.serverURL}api/-default-/public/alfresco/versions/1/nodes/${news.files[0].id}/content?attachment=false&alf_ticket=${this.loginService.getTicket()}`;
      } else {
        this.contentURL = `${
          environment.serverURL
        }pdfRendition?documentId=workspace://SpacesStore/${
          news.files[0].id
        }&response=content&ticket=${this.loginService.getTicket()}&dummy=false`;
      }
    }

    return this.contentURL;
  }

  /**
   * Whether the current news item uses the given layout.
   *
   * @param item The layout to test against (`'important'` or `'reminder'`).
   */
  private isLayout(item: 'important' | 'reminder'): boolean {
    return this.news()?.layout === item;
  }

  /** Whether the news item uses the `important` layout. */
  readonly isImportant = computed<boolean>(() => this.isLayout('important'));

  /** Whether the news item uses the `reminder` layout. */
  readonly isReminder = computed<boolean>(() => this.isLayout('reminder'));

  /**
   * Whether the current news item uses the given content pattern.
   *
   * @param item The pattern to test against (`'document'`, `'date'` or `'iframe'`).
   */
  private isPattern(item: 'document' | 'date' | 'iframe'): boolean {
    return this.news()?.pattern === item;
  }

  /** Whether the news item follows the `document` pattern. */
  readonly isDocument = computed<boolean>(() => this.isPattern('document'));

  /**
   * Whether the previewed/attached file is a PDF.
   *
   * Prefers the locally selected preview file when present; otherwise inspects
   * the first attached file's name.
   */
  readonly isPDFDocument = computed<boolean>(() => {
    const file = this.previewImageLocal();
    if (file !== undefined) {
      return file.name.includes('.pdf');
    }
    return this.news()?.files?.[0]?.name?.includes('.pdf') ?? false;
  });

  /** Whether the news item follows the `date` pattern. */
  readonly isDate = computed<boolean>(() => this.isPattern('date'));

  /** Whether the news item follows the `iframe` pattern. */
  readonly isIframe = computed<boolean>(() => this.isPattern('iframe'));

  /** Name of the first file attached to the news item, or `undefined`. */
  readonly getNewsFileName = computed<string | undefined>(
    () => this.news()?.files?.[0]?.name
  );

  /** Id of the first file attached to the news item, or `undefined`. */
  readonly getNewsFileId = computed<string | undefined>(
    () => this.news()?.files?.[0]?.id
  );

  /** Whether the current news item has at least one attached file. */
  readonly hasFile = computed<boolean>(
    () => (this.news()?.files?.length ?? 0) > 0
  );

  /**
   * Whether the current user is allowed to manage the news item, i.e. the
   * item's permissions grant `InfManage` or `InfAdmin`.
   */
  readonly isNewsManage = computed<boolean>(() => {
    const permissions = this.news()?.permissions;
    return (
      permissions?.InfManage === 'ALLOWED' ||
      permissions?.InfAdmin === 'ALLOWED'
    );
  });

  /**
   * Triggers a download ("save as") of the first attached file via
   * {@link SaveAsService}. Does nothing if the file id or name is unavailable.
   */
  saveFile() {
    const id = this.getNewsFileId();
    const name = this.getNewsFileName();
    if (id !== undefined && name !== undefined) {
      this.saveAsService.saveAs(id, name);
    }
  }

  /**
   * Builds a sanitized CSS `url(...)` background style from a previously
   * sanitized secure URL (as produced by `SecurePipe`).
   *
   * @param url A sanitized URL object exposing
   *   `changingThisBreaksApplicationSecurity`.
   * @returns A `SafeStyle` wrapping the `url(...)` value, or `undefined` if the
   *   provided URL is not usable.
   */
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  prepareUrl(url: any) {
    if (url?.changingThisBreaksApplicationSecurity) {
      // prettier-ignore
      return this.sanitizer.bypassSecurityTrustStyle(`url(${url.changingThisBreaksApplicationSecurity})`); // NOSONAR Safe - URL comes from SecurePipe which fetches authenticated content from backend API
    }
    return undefined;
  }

  /** The external URL associated with the news item, or an empty string. */
  readonly getSafeUrl = computed<string>(() => this.news()?.url ?? '');

  /**
   * The news item's HTML content as a plain string, or an empty string when
   * there is no content.
   *
   * The raw string is returned so Angular sanitizes it when bound via
   * `[innerHTML]`, preventing stored cross-site scripting from news content.
   */
  readonly getSanitizedContent = computed<string>(
    () => this.news()?.content ?? ''
  );

  /** The display size (grid span) of the news item, defaulting to `1`. */
  readonly getNewsSize = computed<number>(() => this.news()?.size ?? 1);

  /**
   * Whether the news item has a safe, absolute `http`/`https` URL.
   */
  readonly hasValidUrl = computed<boolean>(() =>
    isAllowedResourceUrl(this.news()?.url)
  );

  /**
   * The author (modifier) to display for the news item, or the placeholder
   * `'John Doe'` when in preview mode or when no modifier is available.
   */
  readonly getAuthor = computed<string>(() => {
    const news = this.news();
    if (news?.modifier && !this.preview()) {
      return news.modifier;
    }
    return 'John Doe';
  });

  /**
   * The last-modified date to display for the news item, or the current date
   * when in preview mode or when no modification date is available.
   */
  readonly getDate = computed<Date>(() => {
    const news = this.news();
    if (news?.modified && !this.preview()) {
      return new Date(news.modified);
    }
    return new Date();
  });

  /**
   * The creation date to display for the news item, or the current date when in
   * preview mode or when no creation date is available.
   */
  readonly getDateCreated = computed<Date>(() => {
    const news = this.news();
    if (news?.created && !this.preview()) {
      return new Date(news.created);
    }
    return new Date();
  });

  /**
   * Whether the news item has been edited since creation, by comparing the
   * created and modified timestamps at minute precision.
   */
  readonly isEdited = computed<boolean>(() => {
    const news = this.news();
    if (news?.modified && news.created) {
      const modified = `${news.modified.toString()}`;
      const created = `${news.created.toString()}`;
      return modified.substring(0, 16) !== created.substring(0, 16);
    }
    return false;
  });

  /**
   * Emits the {@link newsClicked} output to notify listeners that the card was
   * clicked.
   */
  public propagateClick() {
    this.newsClicked.emit();
  }

  /**
   * Updates the maximized state of the highlighted card and emits the new state
   * through {@link highlightedMaxWindow}.
   *
   * @param value `true` to maximize the card, `false` to restore it.
   */
  public highlightedMaxWindowAction(value: boolean) {
    this.highlightedMaximized = value;
    this.highlightedMaxWindow.emit(value);
  }
}
