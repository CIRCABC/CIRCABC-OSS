import { inject } from '@angular/core';
import {
  ActivatedRouteSnapshot,
  CanActivateFn,
  Router,
  RouterStateSnapshot,
} from '@angular/router';

import { GuardAuthorization, GuardsService } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { RedirectionService } from 'app/core/redirection.service';

/**
 * Derives the logical service name expected by the backend guard endpoint
 * from the current router URL.
 *
 * @param url - The full router URL being activated (e.g. `state.url`).
 * @returns The matched service name (`'members'`, `'applicants'` or
 * `'information'`), or an empty string when the URL matches none of the
 * known service segments.
 */
function getServiceName(url: string): string {
  if (url.includes('members')) return 'members';
  if (url.includes('applicants')) return 'applicants';
  if (url.includes('information')) return 'information';
  return '';
}

/**
 * Queries the backend guard service to determine whether the current user
 * is allowed to access a given service within an interest group node.
 *
 * Any error returned by the underlying request is swallowed and treated as
 * a denial, so the guard can fail safely without throwing.
 *
 * @param nodeId - The identifier of the interest group node being accessed.
 * @param serviceName - The service name to check access for (see
 * {@link getServiceName}).
 * @param guardService - The generated {@link GuardsService} used to perform
 * the authorization request.
 * @returns A promise resolving to the {@link GuardAuthorization} response, or
 * `{ granted: false }` when the request fails.
 */
async function checkServiceAccess(
  nodeId: string,
  serviceName: string,
  guardService: GuardsService
): Promise<GuardAuthorization> {
  try {
    return await guardService.getGuardGroupServiceAsync({
      id: nodeId,
      name: serviceName,
    });
  } catch (_error) {
    return { granted: false };
  }
}

/**
 * Handles the navigation side effects that occur when access to a service is
 * denied.
 *
 * Guest users are redirected through the {@link RedirectionService} (typically
 * to trigger authentication), while all users are routed to the `/denied`
 * page.
 *
 * @param loginService - Service used to determine whether the current user is
 * a guest.
 * @param redirectService - Service used to trigger a redirect for guest users.
 * @param routeService - The Angular {@link Router} used to navigate to the
 * denied page.
 * @returns Always `false`, indicating the route must not be activated.
 */
function handleServiceDenied(
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
 * Angular route guard ({@link CanActivateFn}) that protects an interest
 * group's service routes (members, applicants and information).
 *
 * It resolves the group node id from the route hierarchy, determines the
 * requested service from the URL and asks the backend
 * {@link GuardsService} whether access is granted. Depending on the result it
 * either allows activation, routes to `/no-content`, or delegates denial
 * handling (redirect for guests, `/denied` otherwise). Collaborates with
 * {@link LoginService} and {@link RedirectionService} for guest handling.
 *
 * @param route - The activated route snapshot; the group node id is read from
 * `route.parent?.parent?.paramMap`.
 * @param state - The router state snapshot, whose `url` is used to derive the
 * service name.
 * @returns A promise resolving to `true` when activation is permitted, or
 * `false` when access is denied or the node id is invalid.
 */
export const canActivateService: CanActivateFn = async (
  route: ActivatedRouteSnapshot,
  state: RouterStateSnapshot
) => {
  const guardService = inject(GuardsService);
  const loginService = inject(LoginService);
  const redirectService = inject(RedirectionService);
  const routeService = inject(Router);

  const nodeId = route.parent?.parent?.paramMap.get('id');

  if (!nodeId || nodeId === '0' || !route.parent?.parent) {
    return false;
  }

  const serviceName = getServiceName(state.url);
  const res = await checkServiceAccess(nodeId, serviceName, guardService);

  if (res === null) {
    routeService.navigate(['/no-content']);
    return false;
  }

  if (res?.granted === false) {
    return handleServiceDenied(loginService, redirectService, routeService);
  }

  return res?.granted ?? false;
};
