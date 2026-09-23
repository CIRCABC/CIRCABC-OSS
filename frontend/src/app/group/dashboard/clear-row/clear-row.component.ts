import { ChangeDetectionStrategy, Component } from '@angular/core';

/**
 * Presentational layout helper component used within the group dashboard.
 *
 * Rendered via the `cbc-clear-row` selector, it outputs the static markup
 * defined in `clear-row.component.html` — typically a clearing/spacer row
 * used to break the float or grid flow of dashboard content. It holds no
 * inputs, outputs or state and exists purely for layout composition.
 *
 * `preserveWhitespaces` is enabled so the surrounding whitespace in the
 * template is retained, which the row relies on for correct rendering.
 */
@Component({
  selector: 'cbc-clear-row',
  templateUrl: './clear-row.component.html',
  styleUrl: './clear-row.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ClearRowComponent {}
