import { Component, Input } from '@angular/core';

@Component({
  selector: 'cbc-flat-message',
  templateUrl: './flat-message.component.html',
  styleUrls: ['./flat-message.component.scss'],
  preserveWhitespaces: true,
})
export class FlatMessageComponent {
  @Input()
  public message!: string;
}
