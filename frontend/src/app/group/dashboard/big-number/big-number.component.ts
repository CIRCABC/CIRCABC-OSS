import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';

/**
 * Presentational dashboard widget that renders a single prominent numeric
 * statistic together with a descriptive, translatable label.
 *
 * Used on the group dashboard to display headline metrics (for example a
 * count of members, documents or events) in a compact "big number" card
 * layout. The component is purely presentational: it holds no state and
 * performs no data fetching, receiving both the value and its label from the
 * parent component via required inputs. The label is resolved through
 * Transloco for internationalisation.
 *
 * @example
 * ```html
 * <cbc-big-number [value]="memberCount" label="dashboard.members" />
 * ```
 */
@Component({
  selector: 'cbc-big-number',
  templateUrl: './big-number.component.html',
  styleUrl: './big-number.component.scss',
  preserveWhitespaces: true,
  imports: [TranslocoModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BigNumberComponent {
  /**
   * Required numeric value displayed as the primary "big number" of the
   * widget.
   */
  readonly value = input.required<number>();

  /**
   * Required label shown alongside the value. Typically a Transloco
   * translation key that is resolved to a localised, human-readable
   * description of what the number represents.
   */
  readonly label = input.required<string>();
}
