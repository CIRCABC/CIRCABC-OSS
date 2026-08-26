import { Component, input } from '@angular/core';

@Component({
  selector: 'cbc-spinner',
  templateUrl: './spinner.component.html',
  styleUrl: './spinner.component.scss',
  preserveWhitespaces: true,
})
export class SpinnerComponent {
  readonly white = input(false);
  readonly float = input<'right' | 'left'>('right');
}
