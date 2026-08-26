import { Injectable } from '@angular/core';
import { InterestGroup } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';

@Injectable({
  providedIn: 'root',
})
export class VisitedGroupService {
  constructor(private loginService: LoginService) {}

  private visitedGroups: Set<string> = new Set([]);

  public visitGroup(ig: InterestGroup) {
    if (!this.loginService.isGuest()) {
      if (!this.visitedGroups.has(ig.id as string)) {
        this.visitedGroups.add(ig.id as string);
      }
    }
  }

  public isVisited(id: string): boolean {
    return this.visitedGroups.has(id);
  }
}
