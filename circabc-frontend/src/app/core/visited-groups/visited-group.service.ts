import { Injectable } from '@angular/core';
import { InterestGroup } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';

@Injectable({
  providedIn: 'root',
})
export class VisitedGroupService {
  private keySuffix = '_ig-visited';

  constructor(private loginService: LoginService) {}

  visitGroup(ig: InterestGroup) {
    if (!this.loginService.isGuest()) {
      const username = this.loginService.getUser().userId;
      let visitedIgsObject = localStorage.getItem(this.itemKey(username));

      if (visitedIgsObject !== undefined && visitedIgsObject !== null) {
        const visitedIgs: { visits: InterestGroup[] } =
          JSON.parse(visitedIgsObject);

        if (visitedIgs.visits === undefined) {
          visitedIgs.visits = [];
        }

        if (!this.containsGroup(ig, visitedIgs.visits)) {
          visitedIgs.visits.push(ig);
        }

        visitedIgsObject = JSON.stringify(visitedIgs);
        localStorage.setItem(this.itemKey(username), visitedIgsObject);
      } else {
        const visitedIgs: { visits: InterestGroup[] } = { visits: [] };
        visitedIgs.visits.push(ig);
        visitedIgsObject = JSON.stringify(visitedIgs);
        localStorage.setItem(this.itemKey(username), visitedIgsObject);
      }
    }
  }

  getVisitedGroups(): InterestGroup[] {
    if (!this.loginService.isGuest()) {
      const username = this.loginService.getUser().userId;
      const visitedIgsObject = localStorage.getItem(this.itemKey(username));

      if (visitedIgsObject !== undefined && visitedIgsObject !== null) {
        const visitedIgs: { visits: InterestGroup[] } =
          JSON.parse(visitedIgsObject);

        if (visitedIgs.visits === undefined) {
          visitedIgs.visits = [];
        }

        return visitedIgs.visits;
      }
    }
    return [];
  }

  private containsGroup(group: InterestGroup, list: InterestGroup[]): boolean {
    let result = false;

    for (const ig of list) {
      if (ig.id === group.id) {
        result = true;
        break;
      }
    }

    return result;
  }

  private itemKey(username: string | undefined): string {
    return `${username}${window.location.hostname}${this.keySuffix}`;
  }
}
