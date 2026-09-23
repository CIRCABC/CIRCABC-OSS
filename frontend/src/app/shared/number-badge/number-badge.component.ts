import {
  ChangeDetectionStrategy,
  Component,
  computed,
  input,
} from '@angular/core';

/**
 * Standalone UI component (`cbc-number-badge`) that renders a small numeric
 * badge, typically used to display a count (e.g. unread items or pending
 * notifications).
 *
 * The badge changes its background color depending on whether the displayed
 * number exceeds an optional threshold: it turns red when the value is over
 * the limit, and stays green (`#58c37e`) otherwise. When no limit is provided
 * the badge always uses the green color.
 */
@Component({
  selector: 'cbc-number-badge',
  templateUrl: './number-badge.component.html',
  styleUrl: './number-badge.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  preserveWhitespaces: true,
})
export class NumberBadgeComponent {
  /**
   * Required input holding the numeric value rendered inside the badge.
   */
  readonly number = input.required<number>();

  /**
   * Optional input defining the threshold above which the badge is considered
   * to be "over the limit". When set and exceeded by {@link number}, the badge
   * is highlighted in red. When left `undefined`, no threshold is applied.
   */
  readonly limit = input<number>();

  /**
   * Computed signal resolving the badge background color based on the current
   * {@link number} and {@link limit} values.
   *
   * @returns `'red'` when a {@link limit} is defined and {@link number}
   * exceeds it, otherwise the default green color `'#58c37e'`.
   */
  readonly backgroundColor = computed(() => {
    const limitValue = this.limit(); // Capture the value of the signal
    if (limitValue !== undefined && this.number() > limitValue) {
      return 'red';
    }
    return '#58c37e';
  });
}
