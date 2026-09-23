import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivateFn, Router } from '@angular/router';

import { InterestGroupService } from 'app/core/generated/circabc';

/**
 * Route guard that authorises navigation to the interest-group deletion flow.
 *
 * It resolves the target group from the `id` route parameter and grants access
 * only when the current user holds the `IgDelete` permission on that group.
 * When the permission is missing (or the group/parameter cannot be resolved)
 * the user is redirected to the `/denied` access-denied page and activation is
 * blocked.
 *
 * Key collaborators:
 * - {@link InterestGroupService} — fetches the interest group and its permissions.
 * - {@link Router} — performs the redirect to `/denied` on failure.
 *
 * @param route The activated route snapshot; the `id` path parameter identifies
 *   the interest group whose deletion permission is being checked.
 * @returns A promise resolving to `true` when the user may delete the group, or
 *   `false` (after redirecting to `/denied`) otherwise.
 */
export const canActivateGroupDelete: CanActivateFn = async (
  route: ActivatedRouteSnapshot
) => {
  const interestGroupService = inject(InterestGroupService);
  const routeService = inject(Router);
  const groupId = route.paramMap.get('id');
  if (groupId) {
    const group = await interestGroupService.getInterestGroupAsync({
      id: groupId,
    });
    if (group?.permissions?.IgDelete === 'true') {
      return true;
    }
  }

  routeService.navigate(['/denied']);
  return false;
};
