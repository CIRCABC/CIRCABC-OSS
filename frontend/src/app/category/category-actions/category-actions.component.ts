import { ChangeDetectionStrategy, Component } from '@angular/core';

/**
 * Presentational component (`cbc-category-actions`) that renders the set of
 * actions available for a category.
 *
 * The component itself holds no logic or state; its markup is defined entirely
 * in the associated `category-actions.component.html` template, making it a
 * pure template host used within the category management views.
 *
 * @remarks
 * Change detection is configured as {@link ChangeDetectionStrategy.OnPush} and
 * whitespace is preserved (`preserveWhitespaces: true`) to keep the rendered
 * layout faithful to the template markup.
 */
@Component({
  selector: 'cbc-category-actions',
  templateUrl: './category-actions.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  preserveWhitespaces: true,
})
export class CategoryActionsComponent {}
