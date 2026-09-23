import { ChangeDetectionStrategy, Component, input } from '@angular/core';

import { type TitleTag } from 'app/group/dynamic-properties/title/title';

/**
 * Presentational component that renders a single localized title tag.
 *
 * A {@link TitleTag} pairs a language code with its corresponding title
 * value. This component displays that pairing (typically showing the value
 * along with its language) and is used within the dynamic-properties title
 * feature to list a node's titles across the different available languages.
 *
 * The component is purely presentational: it takes a single required input
 * and has no outputs or side effects.
 *
 * @example
 * ```html
 * <cbc-title-tag [entry]="{ lang: 'en', value: 'My document' }" />
 * ```
 */
@Component({
  selector: 'cbc-title-tag',
  templateUrl: './title-tag.component.html',
  styleUrl: './title-tag.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  preserveWhitespaces: true,
})
export class TitleTagComponent {
  /**
   * Required input holding the localized title to render.
   *
   * Contains the language code (`lang`) and the associated title text
   * (`value`) that the template displays.
   */
  readonly entry = input.required<TitleTag>();
}
