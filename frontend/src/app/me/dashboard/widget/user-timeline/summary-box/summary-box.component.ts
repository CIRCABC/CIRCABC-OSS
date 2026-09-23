import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';

/**
 * Presentational component that renders a compact statistic tile used within
 * the user timeline widget of the personal dashboard.
 *
 * It displays a single numeric value together with a translatable descriptive
 * label (for example a count of documents or events), relying on Transloco for
 * i18n of the label text. The component is purely presentational: it holds no
 * state and derives its output entirely from its inputs.
 *
 * @example
 * ```html
 * <cbc-summary-box [number]="42" label="dashboard.documents" />
 * ```
 */
@Component({
  selector: 'cbc-summary-box',
  templateUrl: './summary-box.component.html',
  styleUrl: './summary-box.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [TranslocoModule],
})
export class SummaryBoxComponent {
  /**
   * Required numeric value shown prominently in the summary box.
   */
  readonly number = input.required<number>();

  /**
   * Required label describing the displayed number. Expected to be a Transloco
   * translation key that is resolved for display.
   */
  readonly label = input.required<string>();
}
