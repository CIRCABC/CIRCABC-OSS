import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivateFn, Router } from '@angular/router';

import { GuardsService } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { RedirectionService } from 'app/core/redirection.service';

/**
 * Extracts the node identifier to be checked for admin access from the
 * activated route snapshot.
 *
 * Requires the route to carry an `id` parameter; when a more specific
 * `nodeId` parameter is present it takes precedence over `id`.
 *
 * @param route The activated route snapshot whose parameter map is inspected.
 * @returns The resolved node id, or `null` when the route has no `id` parameter.
 */
function getNodeId(route: ActivatedRouteSnapshot): string | null {
  if (!route.paramMap?.has('id')) return null;
  return route.paramMap.has('nodeId')
    ? route.paramMap.get('nodeId')
    : route.paramMap.get('id');
}

/**
 * Handles a denied admin access decision.
 *
 * When the current user is a guest, it flags that a redirection (typically to
 * the login flow) must occur. In all cases it navigates the user to the
 * `/denied` page and reports the access as not granted.
 *
 * @param loginService Used to determine whether the current user is a guest.
 * @param redirectionService Used to request a mandatory redirection for guests.
 * @param router Used to navigate the user to the access denied page.
 * @returns Always `false`, indicating the route activation is denied.
 */
function handleDenied(
  loginService: LoginService,
  redirectionService: RedirectionService,
  router: Router
): boolean {
  if (loginService.isGuest()) {
    redirectionService.mustRedirect();
  }

  router.navigate(['/denied']);
  return false;
}

/**
 * Angular route guard that authorizes activation of admin-only routes.
 *
 * It resolves the target node id from the route, then queries the backend
 * {@link GuardsService} to check whether the current user has admin rights on
 * that node. Activation is denied when there is no valid node id, when access
 * is not granted, or when the check fails; in the latter two cases guests are
 * flagged for redirection and the user is sent to the `/denied` page.
 *
 * Key collaborators: {@link GuardsService} (permission check),
 * {@link LoginService} (guest detection), {@link RedirectionService}
 * (mandatory redirection) and Angular {@link Router} (navigation).
 *
 * @param route The activated route snapshot being guarded.
 * @returns A promise resolving to `true` when admin access is granted,
 * otherwise `false`.
 */
export const canActivateAdmin: CanActivateFn = async (
  route: ActivatedRouteSnapshot
) => {
  const guardsService = inject(GuardsService);
  const router = inject(Router);
  const loginService = inject(LoginService);
  const redirectionService = inject(RedirectionService);

  const nodeId = getNodeId(route);

  if (!nodeId || nodeId === '0') {
    return false;
  }

  try {
    const res = await guardsService.getGuardAdminAsync({ id: nodeId });

    if (res?.granted === false) {
      return handleDenied(loginService, redirectionService, router);
    }

    return res?.granted ?? false;
  } catch (error) {
    console.error(error);
    return handleDenied(loginService, redirectionService, router);
  }
};
