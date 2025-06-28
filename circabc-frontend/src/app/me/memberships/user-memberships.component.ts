import { Component, Input, input } from '@angular/core';

import { TranslocoModule, TranslocoService } from '@jsverse/transloco';

import { RouterLink } from '@angular/router';
import {
  InterestGroup,
  InterestGroupProfile,
  Profile,
} from 'app/core/generated/circabc';
import { HintComponent } from 'app/shared/hint/hint.component';
import { HorizontalLoaderComponent } from 'app/shared/loader/horizontal-loader.component';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';

@Component({
  selector: 'cbc-user-memberships',
  templateUrl: './user-memberships.component.html',
  styleUrl: './user-memberships.component.scss',
  preserveWhitespaces: true,
  imports: [
    HintComponent,
    HorizontalLoaderComponent,
    RouterLink,
    TranslocoModule,
  ],
})
export class UserMembershipsComponent {
  // TODO: Skipped for migration because:
  //  This input is used in a control flow expression (e.g. `@if` or `*ngIf`)
  //  and migrating would break narrowing currently.
  @Input()
  memberships!: InterestGroupProfile[];
  readonly loading = input(false);

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
