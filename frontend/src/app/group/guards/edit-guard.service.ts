import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivateFn, Router } from '@angular/router';

import { GuardAuthorization, GuardsService } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { RedirectionService } from 'app/core/redirection.service';

/**
 * Extracts the identifier of the node whose edit access must be checked from
 * the activated route parameters.
 *
 * The identifier is resolved in priority order, falling back to the next
 * parameter when the previous one is absent: `forumId`, then `topicId`, then
 * `nodeId`.
 *
 * @param route The activated route snapshot to read parameters from.
 * @returns The first matching node identifier, or `null` when none of the
 * expected parameters are present.
 */
function getNodeIdFromRoute(route: ActivatedRouteSnapshot): string | null {
  return (
    route.paramMap.get('forumId') ||
    route.paramMap.get('topicId') ||
    route.paramMap.get('nodeId')
  );
}

/**
 * Queries the backend guard service to determine whether the current user is
 * authorized to edit the given node.
 *
 * Any error raised while calling the service is caught and logged, and the
 * function resolves to `null` so that callers can distinguish a failed lookup
 * from an explicit authorization decision.
 *
 * @param nodeId The identifier of the node to check edit access for.
 * @param guardService The generated guard service used to perform the check.
 * @returns A promise resolving to the {@link GuardAuthorization} decision, or
 * `null` when the request fails.
 */
async function checkEditAccess(
  nodeId: string,
  guardService: GuardsService
): Promise<GuardAuthorization | null> {
  try {
    return await guardService.getGuardEditAsync({ id: nodeId });
  } catch (error) {
    console.error(error);
    return null;
  }
}

/**
 * Handles the case where edit access has been explicitly denied.
 *
 * Guest users are redirected through the {@link RedirectionService} (typically
 * to trigger a login flow), while all users are navigated to the access denied
 * page.
 *
 * @param loginService Used to detect whether the current user is a guest.
 * @param redirectService Used to trigger a redirection for guest users.
 * @param routeService The router used to navigate to the `/denied` page.
 * @returns Always `false`, signalling that navigation must be blocked.
 */
function handleEditDenied(
  loginService: LoginService,
  redirectService: RedirectionService,
  routeService: Router
): boolean {
  if (loginService.isGuest()) {
    redirectService.mustRedirect();
  }

  routeService.navigate(['/denied']);
  return false;
}

/**
 * Route guard that protects edit routes for group nodes (forums, topics and
 * generic library nodes).
 *
 * The guard resolves the target node identifier from the route, then asks the
 * backend {@link GuardsService} whether the current user may edit it. It
 * collaborates with {@link LoginService} to detect guest users, with
 * {@link RedirectionService} to trigger login redirection, and with the
 * {@link Router} to redirect to the appropriate error page.
 *
 * Navigation is blocked (returns `false`) when:
 * - no valid node identifier is present (missing or `'0'`);
 * - the authorization lookup fails, in which case the user is sent to
 *   `/no-content`;
 * - access is explicitly denied, in which case the user is sent to `/denied`
 *   (and guests are additionally redirected to log in).
 *
 * Otherwise the guard returns the granted flag from the authorization result.
 *
 * @param route The activated route snapshot being navigated to.
 * @returns A promise resolving to `true` when edit access is granted, or
 * `false` when navigation must be blocked.
 */
export const canActivateNodeEdit: CanActivateFn = async (
  route: ActivatedRouteSnapshot
) => {
  const guardService = inject(GuardsService);
  const loginService = inject(LoginService);
  const redirectService = inject(RedirectionService);
  const routeService = inject(Router);

  const nodeId = getNodeIdFromRoute(route);

  if (!nodeId || nodeId === '0') {
    return false;
  }

  const res = await checkEditAccess(nodeId, guardService);

  if (res === null) {
    routeService.navigate(['/no-content']);
    return false;
  }

  if (res?.granted === false) {
    return handleEditDenied(loginService, redirectService, routeService);
  }

  return res?.granted ?? false;
};
