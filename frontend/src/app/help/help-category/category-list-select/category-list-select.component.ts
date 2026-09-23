import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { HelpCategory } from 'app/core/generated/circabc';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';

/**
 * Presentational component that renders a navigable list of help categories.
 *
 * Each category is displayed as a router link (via {@link RouterLink}) allowing
 * the user to select and navigate to a specific help category. The currently
 * active category can be highlighted by matching its identifier against
 * {@link CategoryListSelectComponent.currentId}. Category labels are localised
 * through the {@link I18nPipe} and Transloco.
 *
 * This is a stateless, input-only component: it holds no internal state and
 * emits no outputs, delegating navigation to the Angular router.
 */
@Component({
  selector: 'cbc-category-list-select',
  templateUrl: './category-list-select.component.html',
  styleUrl: './category-list-select.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, I18nPipe, TranslocoModule],
})
export class CategoryListSelectComponent {
  /**
   * The collection of help categories to render in the list.
   *
   * Defaults to an empty array when no categories are provided.
   */
  readonly categories = input<HelpCategory[]>([]);

  /**
   * Identifier of the currently selected/active help category.
   *
   * Used to highlight or mark the matching entry in the rendered list.
   * May be `undefined` when no category is currently selected.
   */
  readonly currentId = input<string>();
}
