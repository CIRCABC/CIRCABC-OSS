import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivateFn, Router } from '@angular/router';

import { GuardsService } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { RedirectionService } from 'app/core/redirection.service';

/**
 * Route guard (`CanActivateFn`) that protects the group members administration
 * area. It authorizes navigation only when the current user has been granted
 * members-admin rights on the target group node.
 *
 * The guard resolves the group node identifier from the route's `id` parameter
 * and queries the backend {@link GuardsService} to check whether access is
 * granted. When access is denied, guests are redirected to the login flow via
 * {@link RedirectionService} and all other users are routed to the `/denied`
 * page.
 *
 * Key collaborators:
 * - {@link GuardsService} — backend permission check (`getGuardGroupMembersAdmin`).
 * - {@link LoginService} — determines whether the current user is a guest.
 * - {@link RedirectionService} — triggers redirection for unauthenticated users.
 * - {@link Router} — navigates denied users to the access-denied page.
 *
 * @param route - The activated route snapshot; its `id` param supplies the
 *   group node identifier to authorize against.
 * @returns A promise resolving to `true` when access is granted, or `false`
 *   when the node id is missing/`'0'`, the permission is not granted, or the
 *   backend response is unavailable.
 */
export const canActivateGroupMembersAdmin: CanActivateFn = async (
  route: ActivatedRouteSnapshot
) => {
  const guardService = inject(GuardsService);
  const loginService = inject(LoginService);
  const redirectService = inject(RedirectionService);
  const routeService = inject(Router);
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  let nodeId: any;
  // group admin context
  if (route.paramMap?.has('id')) {
    nodeId = route.paramMap.get('id');
  }

  if (nodeId && nodeId !== '0') {
    const res = await guardService.getGuardGroupMembersAdminAsync({
      id: nodeId,
    });
    if (res !== undefined) {
      if (res.granted === false) {
        if (loginService.isGuest()) {
          redirectService.mustRedirect();
        }

        routeService.navigate(['/denied']);
      }
      if (res.granted !== undefined) {
        return res.granted;
      }
    }
  }

  return false;
};
