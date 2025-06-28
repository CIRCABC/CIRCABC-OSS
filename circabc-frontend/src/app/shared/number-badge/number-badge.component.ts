import { Component, computed, input } from '@angular/core';

@Component({
  selector: 'cbc-number-badge',
  templateUrl: './number-badge.component.html',
  styleUrl: './number-badge.component.scss',
  preserveWhitespaces: true,
})
export class NumberBadgeComponent {
  readonly number = input.required<number>();
  readonly limit = input<number>();

  readonly backgroundColor = computed(() => {
    const limitValue = this.limit(); // Capture the value of the signal
    if (limitValue !== undefined && this.number() > limitValue) {
      return 'red';
    }
    return '#58c37e';
  });
}
