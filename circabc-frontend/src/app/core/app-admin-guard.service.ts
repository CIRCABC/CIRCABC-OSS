import { inject } from '@angular/core';
import {
  ActivatedRouteSnapshot,
  CanActivateFn,
  RouterStateSnapshot,
} from '@angular/router';
import { User } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';

export const canActivateAppAdmin: CanActivateFn = (
  _route: ActivatedRouteSnapshot,
  state: RouterStateSnapshot
) => {
  const url: string = state.url;

  return checkLogin(url);
};

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
