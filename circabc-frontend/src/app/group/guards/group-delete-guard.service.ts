import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivateFn, Router } from '@angular/router';

import { InterestGroupService } from 'app/core/generated/circabc';
import { firstValueFrom } from 'rxjs';

export const canActivateGroupDelete: CanActivateFn = async (
  route: ActivatedRouteSnapshot
) => {
  const interestGroupService = inject(InterestGroupService);
  const routeService = inject(Router);
  const groupId = route.paramMap.get('id');
  if (groupId) {
    const group = await firstValueFrom(
      interestGroupService.getInterestGroup(groupId)
    );
    if (group?.permissions && group.permissions.IgDelete === 'true') {
      return true;
    }
  }

  routeService.navigate(['/denied']);
  return false;
};
