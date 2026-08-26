import { Component, input } from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';

@Component({
  selector: 'cbc-summary-box',
  templateUrl: './summary-box.component.html',
  styleUrl: './summary-box.component.scss',
  preserveWhitespaces: true,
  imports: [TranslocoModule],
})
export class SummaryBoxComponent {
  readonly number = input.required<number>();
  readonly label = input.required<string>();
}
