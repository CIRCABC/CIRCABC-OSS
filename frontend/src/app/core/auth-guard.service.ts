import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { LoginService } from 'app/core/login.service';
import { RedirectionService } from 'app/core/redirection.service';

/**
 * Route guard that restricts access to authenticated (non-guest) users.
 *
 * Implemented as an Angular functional `CanActivateFn`. It uses `inject()`
 * within the guard's execution context to resolve its collaborators:
 * - {@link LoginService} to determine whether the current user is a guest.
 * - {@link RedirectionService} to remember the URL the user attempted to
 *   reach so they can be redirected back after authenticating.
 * - {@link Router} to navigate guests to the access-denied page.
 *
 * When the user is authenticated the guard allows activation; otherwise it
 * stores the attempted URL, redirects to `/denied`, and blocks activation.
 *
 * @returns A promise resolving to `true` when navigation is permitted
 * (authenticated user), or `false` when access is denied (guest user).
 */
export const canActivateAuth: CanActivateFn = async () => {
  if (!inject(LoginService).isGuest()) {
    return true;
  }

  // store the attempted URL for redirecting
  inject(RedirectionService).mustRedirect();

  inject(Router).navigate(['/denied']);

  return false;
};
