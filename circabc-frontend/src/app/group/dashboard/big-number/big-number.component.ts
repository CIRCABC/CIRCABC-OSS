import { Component, input } from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';

@Component({
  selector: 'cbc-big-number',
  templateUrl: './big-number.component.html',
  styleUrl: './big-number.component.scss',
  preserveWhitespaces: true,
  imports: [TranslocoModule],
})
export class BigNumberComponent {
  readonly value = input.required<number>();
  readonly label = input.required<string>();
}
