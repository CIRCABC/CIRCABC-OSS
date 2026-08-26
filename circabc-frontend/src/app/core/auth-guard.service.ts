import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { LoginService } from 'app/core/login.service';
import { RedirectionService } from 'app/core/redirection.service';

export const canActivateAuth: CanActivateFn = async () => {
  if (!inject(LoginService).isGuest()) {
    return true;
  }

  // store the attempted URL for redirecting
  inject(RedirectionService).mustRedirect();

  // eslint-disable-next-line @typescript-eslint/no-floating-promises
  inject(Router).navigate(['/denied']);

  return false;
};
