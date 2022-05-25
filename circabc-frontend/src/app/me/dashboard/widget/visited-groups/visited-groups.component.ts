import { Component, OnInit } from '@angular/core';

import {
  InterestGroup,
  InterestGroupService,
} from 'app/core/generated/circabc';
import { VisitedGroupService } from 'app/core/visited-groups/visited-group.service';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-visited-groups',
  templateUrl: './visited-groups.component.html',
  styleUrls: ['./visited-groups.component.scss'],
  preserveWhitespaces: true,
})
export class VisitedGroupsComponent implements OnInit {
  // keep the old way just in case?
  private useBrowserStorage = false;

  public visitedIgs: InterestGroup[] = [];

  constructor(
    private visitedGroupService: VisitedGroupService,
    private interestGroupService: InterestGroupService,
    private i18nPipe: I18nPipe
  ) {}

  async ngOnInit() {
    if (this.useBrowserStorage) {
      this.visitedIgs = this.visitedGroupService.getVisitedGroups();
    } else {
      // maximum amount of visited IGs to retrieve (change if more or less is desired)
      this.visitedIgs = await firstValueFrom(
        this.interestGroupService.getVisitedInterestGroups(10)
      );
    }
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
