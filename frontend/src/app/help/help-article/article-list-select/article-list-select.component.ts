import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { HelpArticle } from 'app/core/generated/circabc';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';

/**
 * Presentational component that renders a navigable list of help articles.
 *
 * Each article is displayed as a router link (via {@link RouterLink}) so the
 * user can select and navigate to the corresponding help article. The article
 * currently being viewed is highlighted by matching its id against
 * {@link ArticleListSelectComponent.currentId}. Article titles are localized
 * through the {@link I18nPipe} and Transloco.
 *
 * As a purely display-oriented component it holds no internal state and uses
 * OnPush change detection; the list and selection are driven entirely by its
 * signal inputs.
 */
@Component({
  selector: 'cbc-article-list-select',
  templateUrl: './article-list-select.component.html',
  styleUrl: './article-list-select.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, I18nPipe, TranslocoModule],
})
export class ArticleListSelectComponent {
  /**
   * The collection of help articles to render in the selection list.
   * Defaults to an empty array when not provided.
   */
  readonly articles = input<HelpArticle[]>([]);

  /**
   * The identifier of the currently selected/active help article, used to
   * highlight the matching entry in the list. May be `undefined` when no
   * article is currently selected.
   */
  readonly currentId = input<string>();
}
