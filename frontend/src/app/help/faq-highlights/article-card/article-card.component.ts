import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { type HelpArticle } from 'app/core/generated/circabc';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';

/**
 * Presentational card component that renders a single help/FAQ article
 * within the FAQ highlights section.
 *
 * It displays the article's summary information (such as its title and date)
 * and links to the full article via {@link RouterLink}. Dates are formatted
 * with {@link DatePipe} and localized text is resolved through {@link I18nPipe}.
 *
 * This is a purely display-oriented component driven entirely by its required
 * `article` input; it holds no internal state and triggers no side effects.
 *
 * @selector `cbc-article-card`
 */
@Component({
  selector: 'cbc-article-card',
  templateUrl: './article-card.component.html',
  styleUrl: './article-card.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, DatePipe, I18nPipe],
})
export class ArticleCardComponent {
  /**
   * Required input providing the help article to render in the card.
   *
   * The card reads the article's fields (e.g. title and date) to build its
   * display and the link to the full article. Being `input.required`, the
   * component expects a value to always be supplied by its parent.
   */
  readonly article = input.required<HelpArticle>();
}
