import { ChangeDetectionStrategy, Component, input } from '@angular/core';

/**
 * Presentational component (`cbc-spinner`) that renders an animated loading
 * indicator.
 *
 * It is a lightweight, stateless component typically shown while asynchronous
 * work (HTTP requests, long-running operations) is in progress. Its colour and
 * horizontal alignment can be tuned through inputs so it fits both light and
 * dark surfaces and can be aligned within its container.
 */
@Component({
  selector: 'cbc-spinner',
  templateUrl: './spinner.component.html',
  styleUrl: './spinner.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  preserveWhitespaces: true,
})
export class SpinnerComponent {
  /**
   * When `true`, renders the spinner in a white variant suitable for dark
   * backgrounds. Defaults to `false` (standard colouring on light backgrounds).
   */
  readonly white = input(false);
  /**
   * Controls the horizontal alignment of the spinner within its container.
   * Accepts `'right'` or `'left'`; defaults to `'right'`.
   */
  readonly float = input<'right' | 'left'>('right');
}
