import { Component, OnInit } from '@angular/core';

import { RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import {
  InterestGroup,
  InterestGroupService,
} from 'app/core/generated/circabc';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-visited-groups',
  templateUrl: './visited-groups.component.html',
  styleUrl: './visited-groups.component.scss',
  preserveWhitespaces: true,
  imports: [RouterLink, TranslocoModule],
})
export class VisitedGroupsComponent implements OnInit {
  public visitedIgs: InterestGroup[] = [];

  constructor(
    private interestGroupService: InterestGroupService,
    private i18nPipe: I18nPipe
  ) {}

  async ngOnInit() {
    // maximum amount of visited IGs to retrieve (change if more or less is desired)
    this.visitedIgs = await firstValueFrom(
      this.interestGroupService.getVisitedInterestGroups(10)
    );
  }

  getGroupNameOrTitle(group: InterestGroup): string {
    let result = '';

    if (group.title && Object.keys(group.title).length > 0) {
      result = this.i18nPipe.transform(group.title);
    }

    if (result === '' && group.name) {
      result = group.name;
    }

    return result;
  }
}
