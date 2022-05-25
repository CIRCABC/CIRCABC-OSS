import { Component, Input } from '@angular/core';

@Component({
  selector: 'cbc-summary-box',
  templateUrl: './summary-box.component.html',
  styleUrls: ['./summary-box.component.scss'],
  preserveWhitespaces: true,
})
export class SummaryBoxComponent {
  @Input()
  number!: number;
  @Input()
  label!: string;
}
