import { Component, Input } from '@angular/core';

@Component({
  selector: 'cbc-big-number',
  templateUrl: './big-number.component.html',
  styleUrls: ['./big-number.component.scss'],
  preserveWhitespaces: true,
})
export class BigNumberComponent {
  @Input()
  value!: number;
  @Input()
  label!: string;
}
