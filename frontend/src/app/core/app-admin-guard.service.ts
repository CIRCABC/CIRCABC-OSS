import { inject } from '@angular/core';
import {
  ActivatedRouteSnapshot,
  CanActivateFn,
  RouterStateSnapshot,
} from '@angular/router';
import { User } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';

/**
 * Route guard that restricts access to application administration routes.
 *
 * This `CanActivateFn` only allows navigation to proceed for authenticated
 * users who hold administrative privileges (either a general admin or a
 * CIRCABC admin). It collaborates with {@link LoginService} to resolve the
 * currently authenticated {@link User} and inspect their properties.
 *
 * Intended to be used in the route configuration via the `canActivate`
 * property.
 *
 * @param _route - The activated route snapshot for the target route
 * (unused).
 * @param state - The router state snapshot, used to read the target URL.
 * @returns `true` when the current user is an admin and navigation is
 * allowed; otherwise `false`.
 */
export const canActivateAppAdmin: CanActivateFn = (
  _route: ActivatedRouteSnapshot,
  state: RouterStateSnapshot
) => {
  const url: string = state.url;

  return checkLogin(url);
};

/**
 * Determines whether the current user is permitted to access administration
 * routes.
 *
 * Resolves the current {@link User} through {@link LoginService} and returns
 * `false` for missing/guest users. For valid users it grants access only
 * when the user's properties flag them as an admin (`isAdmin`) or a CIRCABC
 * admin (`isCircabcAdmin`).
 *
 * @param _url - The target URL being navigated to (unused).
 * @returns `true` if the user has administrative rights; otherwise `false`.
 */
function checkLogin(_url: string): boolean {
  const user: User = inject(LoginService).getUser();
  if (
    user.properties === null ||
    user.userId === '' ||
    user.userId === 'guest'
  ) {
    return false;
  }
  return (
    user.properties !== undefined &&
    (user.properties.isAdmin === 'true' ||
      user.properties.isCircabcAdmin === 'true')
  );
}
