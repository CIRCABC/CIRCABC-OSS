import { Component, Input } from '@angular/core';

import { InterestGroup } from 'app/core/generated/circabc';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';

@Component({
  selector: 'cbc-group-card',
  templateUrl: './group-card.component.html',
  styleUrls: ['./group-card.component.scss'],
  preserveWhitespaces: true,
})
export class GroupCardComponent {
  @Input()
  group!: InterestGroup;
  @Input()
  counter: number | undefined;

  constructor(private i18nPipe: I18nPipe) {}

  getGroupNameOrTitle(): string {
    if (this.group.title && Object.keys(this.group.title).length > 0) {
      return (
        this.i18nPipe.transform(this.group.title).slice(0, 40) +
        (Object.keys(this.group.title).length > 40 ? '...' : '')
      );
    } else {
      return this.group.name;
    }
  }

  getLink(): string {
    return `${window.location.href.substring(
      0,
      window.location.href.indexOf('explore')
    )}group/${this.group.id}`;
  }

  hasLogo(): boolean {
    if (this.group && this.group.logoUrl) {
      return true;
    }
    return false;
  }

  getLogoUrl(): string {
    if (this.group && this.group.logoUrl) {
      // workspace:\/\/SpacesStore\/
      if (this.group.logoUrl.indexOf('workspace') !== -1) {
        return this.group.logoUrl.substring(24);
      }
    }

    return '';
  }
}
