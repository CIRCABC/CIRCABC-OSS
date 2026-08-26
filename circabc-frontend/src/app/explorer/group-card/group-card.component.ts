import { Component, input } from '@angular/core';

import { RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { type InterestGroup } from 'app/core/generated/circabc';
import { DownloadPipe } from 'app/shared/pipes/download.pipe';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { SecurePipe } from 'app/shared/pipes/secure.pipe';
import { ShareComponent } from 'app/shared/share/share.component';

@Component({
  selector: 'cbc-group-card',
  templateUrl: './group-card.component.html',
  styleUrl: './group-card.component.scss',
  preserveWhitespaces: true,
  imports: [
    RouterLink,
    ShareComponent,
    DownloadPipe,
    SecurePipe,
    TranslocoModule,
  ],
})
export class GroupCardComponent {
  readonly group = input.required<InterestGroup>();
  readonly counter = input<number>();

  constructor(private i18nPipe: I18nPipe) {}

  getGroupNameOrTitle(): string {
    const group = this.group();
    if (group.title && Object.keys(group.title).length > 0) {
      return (
        this.i18nPipe.transform(group.title).slice(0, 40) +
        (Object.keys(group.title).length > 40 ? '...' : '')
      );
    }
    return group.name;
  }

  getLink(): string {
    return `${window.location.href.substring(
      0,
      window.location.href.indexOf('explore')
    )}group/${this.group().id}`;
  }

  hasLogo(): boolean {
    if (this.group()?.logoUrl) {
      return true;
    }
    return false;
  }

  getLogoUrl(): string {
    const group = this.group();
    if (group?.logoUrl) {
      // workspace:\/\/SpacesStore\/
      if (group.logoUrl.indexOf('workspace') !== -1) {
        return group.logoUrl.substring(24);
      }
    }

    return '';
  }
}
