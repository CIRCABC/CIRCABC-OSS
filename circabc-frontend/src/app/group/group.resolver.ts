import {
  ActivatedRouteSnapshot,
  Resolve,
  RouterStateSnapshot,
} from '@angular/router';

import { catchError, Observable, of } from 'rxjs';

import { Injectable } from '@angular/core';
import {
  InterestGroup,
  InterestGroupService,
} from 'app/core/generated/circabc';

@Injectable()
export class GroupResolver implements Resolve<InterestGroup | { id: string }> {
  public constructor(private interestGroupService: InterestGroupService) {}

  resolve(
    route: ActivatedRouteSnapshot,
    _state: RouterStateSnapshot
  ): Observable<InterestGroup | { id: string }> {
    const id = route.params.id;

    return this.interestGroupService
      .getInterestGroup(id)
      .pipe(catchError((_error) => of({ id: id })));
  }
}
