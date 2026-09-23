import { inject } from '@angular/core';
import {
  ActivatedRouteSnapshot,
  CanActivateFn,
  Router,
  RouterStateSnapshot,
} from '@angular/router';

import { GuardAuthorization, GuardsService } from 'app/core/generated/circabc';
import { MigratedLinkService } from 'app/core/guards/migrated-link.service';
import { LoginService } from 'app/core/login.service';
import { RedirectionService } from 'app/core/redirection.service';

/**
 * Extracts the identifier of the node being accessed from a route snapshot.
 *
 * Looks up the route's parameter map in priority order, returning the first
 * of `forumId`, `topicId` or `nodeId` that is present.
 *
 * @param route The activated route snapshot to inspect.
 * @returns The resolved node identifier, or `null` when none of the
 * recognised route parameters are present.
 */
function getNodeIdFromRoute(route: ActivatedRouteSnapshot): string | null {
  return (
    route.paramMap.get('forumId') ||
    route.paramMap.get('topicId') ||
    route.paramMap.get('nodeId')
  );
}

/**
 * Queries the backend for the current user's authorization on a node.
 *
 * Wraps the {@link GuardsService.getGuardAccess} observable in a promise and
 * swallows any error, logging it to the console, so that callers can treat a
 * failed request as an absence of authorization data rather than a thrown
 * exception.
 *
 * @param nodeId The identifier of the node to check access for.
 * @param guardService The generated guards API service used to query access.
 * @returns A promise resolving to the {@link GuardAuthorization} for the node,
 * or `null` when the request fails.
 */
async function checkAccess(
  nodeId: string,
  guardService: GuardsService
): Promise<GuardAuthorization | null> {
  try {
    return await guardService.getGuardAccessAsync({ id: nodeId });
  } catch (error) {
    console.error(error);
    return null;
  }
}

/**
 * Reacts to a denied access decision by redirecting the user appropriately.
 *
 * Guest users are sent through the login redirection flow. Otherwise, unless
 * the navigation originated from an external link (indicated by the
 * `fromLink=true` query parameter), the user is routed to the access-denied
 * page.
 *
 * @param route The activated route snapshot whose query parameters are
 * inspected for the `fromLink` flag.
 * @param loginService Service used to determine whether the current user is a
 * guest.
 * @param redirectService Service that triggers the login redirection for
 * guests.
 * @param routeService The Angular router used to navigate to the denied page.
 */
function handleAccessDenied(
  route: ActivatedRouteSnapshot,
  loginService: LoginService,
  redirectService: RedirectionService,
  routeService: Router
): void {
  if (loginService.isGuest()) {
    redirectService.mustRedirect();
  }

  if (route.queryParamMap.get('fromLink') !== 'true') {
    routeService.navigate(['/denied']);
  }
}

/**
 * Angular functional route guard that authorizes navigation to a node.
 *
 * Resolves the target node identifier from the route and consults the backend
 * guards service to decide whether the current user may access it. The guard:
 * - denies navigation when no valid node identifier is present (missing or `'0'`);
 * - redirects to the `/no-content` page and denies navigation when the access
 *   check fails to return data;
 * - delegates to {@link handleAccessDenied} and denies navigation when access
 *   is explicitly not granted;
 * - otherwise allows navigation based on the `granted` flag.
 *
 * Collaborators are resolved through Angular's dependency injection:
 * {@link GuardsService}, {@link LoginService}, {@link RedirectionService} and
 * the {@link Router}.
 *
 * @param route The activated route snapshot being navigated to.
 * @returns A promise resolving to `true` when access is granted, otherwise
 * `false`. Redirection side effects may occur before the value resolves.
 */
export const canActivateNodeAccess: CanActivateFn = async (
  route: ActivatedRouteSnapshot,
  state: RouterStateSnapshot
) => {
  const guardService = inject(GuardsService);
  const loginService = inject(LoginService);
  const redirectService = inject(RedirectionService);
  const routeService = inject(Router);
  const migratedLink = inject(MigratedLinkService);

  const nodeId = getNodeIdFromRoute(route);

  if (!nodeId || nodeId === '0') {
    return false;
  }

  const res = await checkAccess(nodeId, guardService);

  if (res === null) {
    // Old/other-host link: try to resolve the original node ref and redirect.
    if (await migratedLink.tryRedirectFromOriginal(state.url)) {
      return false;
    }
    routeService.navigate(['/no-content']);
    return false;
  }

  if (res?.granted === false) {
    handleAccessDenied(route, loginService, redirectService, routeService);
    return false;
  }

  return res?.granted ?? false;
};
