import { inject } from '@angular/core';
import { CanActivateFn } from '@angular/router';
import { User } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';

/**
 * Route guard that restricts activation of a route to Alfresco administrators.
 *
 * Implemented as an Angular functional `CanActivateFn`, it resolves the
 * currently authenticated {@link User} through the {@link LoginService}
 * collaborator and checks the user's properties for the `isAdmin` flag.
 *
 * @returns `true` when the current user has defined properties and their
 * `isAdmin` property equals the string `'true'`, allowing navigation to
 * proceed; otherwise `false`, blocking the route.
 */
export const canActivateAlfrescoAdmin: CanActivateFn = () => {
  const user: User = inject(LoginService).getUser();
  return user.properties?.isAdmin === 'true';
};
