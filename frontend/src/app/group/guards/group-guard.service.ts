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
 * Retrieves the authorization decision for accessing a given interest group.
 *
 * Calls the backend guard endpoint to determine whether the current user is
 * allowed to access the group. On failure, logs the error and returns `null`
 * so the caller can decide how to react (e.g. attempt a migrated-link redirect
 * before navigating to the `/no-content` page).
 *
 * @param groupId - The identifier of the interest group to check access for.
 * @param guardService - The generated CIRCABC guard API client used to query the authorization.
 * @returns A promise resolving to the {@link GuardAuthorization} for the group,
 *          or `null` when the authorization request fails.
 */
async function checkGroupAccess(
  groupId: string,
  guardService: GuardsService
): Promise<GuardAuthorization | null> {
  try {
    return await guardService.getGuardGroupAsync({ id: groupId });
  } catch (error) {
    console.error(error);
    return null;
  }
}

/**
 * Handles the case where access to a group has been denied.
 *
 * If the current user is a guest, triggers a redirect (typically to the login
 * flow) so they can authenticate; otherwise navigates to the `/denied` page.
 *
 * @param loginService - The {@link LoginService} used to detect guest users.
 * @param redirectService - The {@link RedirectionService} used to store the redirect target for guests.
 * @param routeService - The Angular {@link Router} used to navigate to the denied page.
 * @returns Always `false`, indicating that route activation must be blocked.
 */
function handleGroupDenied(
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
 * Angular route guard that controls activation of interest group routes.
 *
 * Reads the group identifier from the route's `id` parameter and asks the
 * backend whether the current user may access the group. It blocks activation
 * when the group id is missing or `'0'`, when the authorization request fails
 * (redirecting to `/no-content`), or when access is explicitly denied
 * (delegating to {@link handleGroupDenied}). Otherwise it allows activation
 * based on the `granted` flag of the returned authorization.
 *
 * Collaborators (resolved via `inject`): the generated {@link GuardsService},
 * {@link LoginService}, {@link RedirectionService} and the Angular
 * {@link Router}.
 *
 * @param route - The activated route snapshot providing the group `id` parameter.
 * @returns A promise resolving to `true` when activation is allowed, otherwise `false`.
 */
export const canActivateGroup: CanActivateFn = async (
  route: ActivatedRouteSnapshot,
  state: RouterStateSnapshot
) => {
  const guardService = inject(GuardsService);
  const loginService = inject(LoginService);
  const redirectService = inject(RedirectionService);
  const routeService = inject(Router);
  const migratedLink = inject(MigratedLinkService);
  const groupId = route.paramMap.get('id');

  if (!groupId || groupId === '0') {
    return false;
  }

  const res = await checkGroupAccess(groupId, guardService);

  if (res === null) {
    // Old/other-host link: try to resolve the original node ref(s) and redirect.
    if (await migratedLink.tryRedirectFromOriginal(state.url)) {
      return false;
    }
    routeService.navigate(['/no-content']);
    return false;
  }

  if (res?.granted === false) {
    return handleGroupDenied(loginService, redirectService, routeService);
  }

  return res?.granted ?? false;
};
