import { Component, Input } from '@angular/core';

@Component({
  selector: 'cbc-number-badge',
  templateUrl: './number-badge.component.html',
  styleUrls: ['./number-badge.component.scss'],
  preserveWhitespaces: true,
})
export class NumberBadgeComponent {
  @Input()
  number!: number;

  getNumberDisplay() {
    if (this.number && this.number < 100) {
      return this.number;
    } else if (this.number && this.number > 100) {
      return '99+';
    } else {
      return 0;
    }
  }
}
