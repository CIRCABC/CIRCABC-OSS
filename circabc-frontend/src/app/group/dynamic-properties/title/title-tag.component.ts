import { Component, input } from '@angular/core';

import { type TitleTag } from 'app/group/dynamic-properties/title/title';

@Component({
  selector: 'cbc-title-tag',
  templateUrl: './title-tag.component.html',
  styleUrl: './title-tag.component.scss',
  preserveWhitespaces: true,
})
export class TitleTagComponent {
  readonly entry = input.required<TitleTag>();
}
