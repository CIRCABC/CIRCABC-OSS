import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  linkedSignal,
  resource,
} from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';
import { HelpService } from 'app/core/generated/circabc';
import { ArticleCardComponent } from './article-card/article-card.component';

/**
 * Standalone component that renders a carousel-like highlight strip of FAQ /
 * help articles on the help landing page.
 *
 * It fetches the set of highlighted help articles from the
 * {@link HelpService} and displays up to three of them at a time (using
 * {@link ArticleCardComponent} for each card). The {@link previous} and
 * {@link next} methods rotate the underlying article list so the user can
 * cycle through all highlighted articles three at a time.
 *
 * Key collaborators:
 * - {@link HelpService}: source of the highlighted articles.
 * - {@link ArticleCardComponent}: renders each individual article card.
 */
@Component({
  selector: 'cbc-faq-highlights',
  templateUrl: './faq-highlights.component.html',
  styleUrl: './faq-highlights.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ArticleCardComponent, TranslocoModule],
})
export class FaqHighlightsComponent {
  /** Generated API client used to retrieve the highlighted help articles. */
  private readonly helpService = inject(HelpService);

  /**
   * Loads the highlighted help articles from {@link HelpService}. Errors are
   * logged to the console and swallowed (resolving to an empty list) so the
   * component fails gracefully instead of ending up in an error state.
   */
  private readonly highlightedArticlesResource = resource({
    loader: async () => {
      try {
        return await this.helpService.getHelpHighlightedArticlesAsync();
      } catch (error) {
        console.error(error);
        return [];
      }
    },
  });

  /**
   * Full, ordered list of highlighted articles. Defaults to the resource's
   * loaded value but is rotated in place by {@link previous} and {@link next}
   * to drive the visible window of articles, so it must remain locally
   * writable.
   */
  public highlightedArticles = linkedSignal(
    () => this.highlightedArticlesResource.value() ?? []
  );

  /**
   * The subset of {@link highlightedArticles} currently shown in the view
   * (at most the first three entries).
   */
  public displayedArticles = computed(() =>
    this.highlightedArticles().slice(0, 3)
  );

  /**
   * Rotates the article list backwards by one position. The last article is
   * moved to the front of {@link highlightedArticles}. Does nothing when the
   * list is empty.
   */
  public previous() {
    const articles = [...this.highlightedArticles()];
    const item = articles.pop();
    if (item) {
      this.highlightedArticles.set([item].concat(articles));
    }
  }

  /**
   * Rotates the article list forwards by one position. The first article is
   * moved to the end of {@link highlightedArticles}. Does nothing when the
   * list is empty.
   */
  public next() {
    const articles = [...this.highlightedArticles()];
    const item = articles.shift();
    if (item) {
      this.highlightedArticles.set(articles.concat([item]));
    }
  }
}
