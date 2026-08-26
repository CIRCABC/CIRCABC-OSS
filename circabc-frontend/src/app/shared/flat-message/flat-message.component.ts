import { Component, input } from '@angular/core';

@Component({
  selector: 'cbc-flat-message',
  templateUrl: './flat-message.component.html',
  styleUrl: './flat-message.component.scss',
  preserveWhitespaces: true,
})
export class FlatMessageComponent {
  public readonly message = input.required<string>();
}
