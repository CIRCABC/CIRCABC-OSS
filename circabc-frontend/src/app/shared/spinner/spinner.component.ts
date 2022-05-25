import { Component, Input } from '@angular/core';

@Component({
  selector: 'cbc-spinner',
  templateUrl: './spinner.component.html',
  styleUrls: ['./spinner.component.scss'],
  preserveWhitespaces: true,
})
export class SpinnerComponent {
  @Input() white = false;
  @Input() float: 'right' | 'left' = 'right';
}
