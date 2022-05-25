import { Component, Input } from '@angular/core';

import { TranslocoService } from '@ngneat/transloco';

import {
  InterestGroup,
  InterestGroupProfile,
  Profile,
} from 'app/core/generated/circabc';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';

@Component({
  selector: 'cbc-user-memberships',
  templateUrl: './user-memberships.component.html',
  styleUrls: ['./user-memberships.component.scss'],
  preserveWhitespaces: true,
})
export class UserMembershipsComponent {
  @Input()
  memberships!: InterestGroupProfile[];
  @Input()
  loading = false;

  constructor(
    private translateService: TranslocoService,
    private i18nPipe: I18nPipe
  ) {}

  public getGroupDisplay(interestGroup: InterestGroup | undefined): string {
    if (interestGroup === undefined) {
      return 'unknown';
    }
    let result = interestGroup.name;

    if (interestGroup.title) {
      const title = this.i18nPipe.transform(interestGroup.title);
      if (title !== '' && title !== undefined) {
        result = title;
      }
    }

    return result;
  }

  public getProfileDisplay(profile: Profile | undefined): string {
    if (profile === undefined) {
      return '';
    }
    let result = profile.name !== undefined ? profile.name : '';

    if (profile.title) {
      const title = this.i18nPipe.transform(profile.title);
      if (title !== '' && title !== undefined) {
        result = title;
      }
    }

    return result;
  }

  public getCurrentLang(): string {
    return this.translateService.getActiveLang();
  }

  public getDefaultLang(): string {
    return this.translateService.getDefaultLang();
  }
}
