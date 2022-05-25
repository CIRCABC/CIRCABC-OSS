import { Component, Input } from '@angular/core';

import { TitleTag } from 'app/group/dynamic-properties/title/title';

@Component({
  selector: 'cbc-title-tag',
  templateUrl: './title-tag.component.html',
  styleUrls: ['./title-tag.component.scss'],
  preserveWhitespaces: true,
})
export class TitleTagComponent {
  @Input()
  entry!: TitleTag;
}
