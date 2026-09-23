import {
  ChangeDetectionStrategy,
  Component,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { CategoryService, InterestGroup } from 'app/core/generated/circabc';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { sortI18nProperty } from 'app/core/util';
import { DataCyDirective } from 'app/shared/directives/data-cy.directive';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';

/**
 * Standalone Angular component (`cbc-category-group`) that renders the list of
 * interest groups belonging to a given category.
 *
 * The category identifier is read from the current route parameters (`id`).
 * On initialisation, and whenever those parameters change, the component
 * fetches the interest groups for that category, sorts them by their
 * localised title (falling back to their technical name), and exposes them to
 * the template for display as router links.
 *
 * Key collaborators:
 * - {@link CategoryService} to retrieve the interest groups from the backend.
 * - {@link TranslocoService} to resolve the active and default languages used
 *   for localisation and sorting.
 * - {@link UiMessageService} to surface errors raised while loading data.
 * - {@link I18nPipe} to resolve the localised label of each interest group.
 */
@Component({
  selector: 'cbc-category-group',
  templateUrl: './category-groups.component.html',
  styleUrl: './category-groups.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [DataCyDirective, RouterLink, TranslocoModule],
})
export class CategoryGroupsComponent implements OnInit {
  /** Active route, used to read the category `id` parameter. */
  private readonly route = inject(ActivatedRoute);
  /** Generated API client used to fetch interest groups by category. */
  private readonly categoryService = inject(CategoryService);
  /** Provides the active and default languages for localisation and sorting. */
  private readonly translateService = inject(TranslocoService);
  /** Displays error notifications to the user. */
  private readonly uiMessageService = inject(UiMessageService);
  /** Resolves the localised label of an interest group's title. */
  private readonly i18nPipe = inject(I18nPipe);

  /**
   * The interest groups of the current category, sorted by localised title.
   * Rendered by the template and empty until the first successful load.
   */
  public readonly interestGroups = signal<InterestGroup[]>([]);

  /**
   * Angular lifecycle hook. Subscribes to route parameter changes and loads
   * the interest groups for the category identified by the `id` parameter.
   */
  ngOnInit() {
    this.route.params.subscribe(async (params) => {
      await this.listInterestGroups(params.id);
    });
  }

  /**
   * Fetches the interest groups for the given category, sorts them by their
   * localised title (with a fallback to the technical name), and stores the
   * result in {@link interestGroups}. Errors are reported via
   * {@link UiMessageService} rather than propagated.
   *
   * @param categoryId The identifier of the category whose interest groups
   *   should be loaded. When falsy, no request is made.
   * @returns A promise that resolves once the groups have been loaded (or the
   *   error has been handled).
   */
  public async listInterestGroups(categoryId: string) {
    try {
      if (categoryId) {
        const unsortedInterestGroups =
          await this.categoryService.getInterestGroupsByCategoryIdAsync({
            id: categoryId,
            language: this.getCurrentLang(),
          });
        unsortedInterestGroups.sort((a: InterestGroup, b: InterestGroup) =>
          sortI18nProperty(
            a.title,
            b.title,
            this.getCurrentLang(),
            this.getDefaultLang(),
            a.name,
            b.name
          )
        );
        this.interestGroups.set(unsortedInterestGroups);
      }
    } catch (err) {
      this.uiMessageService.addErrorMessage(err);
    }
  }
  /**
   * Resolves a human-readable label for an interest group. Prefers the
   * localised title when available, otherwise falls back to the technical
   * name.
   *
   * @param item The interest group to derive a label from.
   * @returns The localised title, the technical name, or an empty string when
   *   neither is available.
   */
  getNameOrTitle(item: InterestGroup): string {
    let result = '';

    if (item.title && Object.keys(item.title).length > 0) {
      result = this.i18nPipe.transform(item.title);
    }

    if (result === '' && item.name) {
      result = item.name;
    }

    return result;
  }

  /**
   * @returns The currently active language code from Transloco.
   */
  private getCurrentLang(): string {
    return this.translateService.getActiveLang();
  }

  /**
   * @returns The default language code from Transloco.
   */
  private getDefaultLang(): string {
    return this.translateService.getDefaultLang();
  }
}
