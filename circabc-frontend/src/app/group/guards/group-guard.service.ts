import { inject } from '@angular/core';
import {
  ActivatedRouteSnapshot,
  CanActivateFn,
  Router,
  RouterStateSnapshot,
} from '@angular/router';

import { GuardsService } from 'app/core/generated/circabc';
import { MigratedLinkService } from 'app/core/guards/migrated-link.service';
import { LoginService } from 'app/core/login.service';
import { RedirectionService } from 'app/core/redirection.service';
import { firstValueFrom } from 'rxjs';

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

  if (groupId && groupId !== '0') {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    let res: any;
    let errored = false;

    try {
      res = await firstValueFrom(guardService.getGuardGroup(groupId));
    } catch (error) {
      console.error(error);
      errored = true;
    }

    if (res === null || errored) {
      // Old/other-host link: try to resolve the original node refs and redirect.
      if (await migratedLink.tryRedirectFromOriginal(state.url)) {
        return false;
      }
      // eslint-disable-next-line @typescript-eslint/no-floating-promises
      routeService.navigate(['/no-content']);
    } else if (res !== undefined) {
      if (res.locked === true) {
        // IG is locked and user is not a leader/admin — go to locked page
        // eslint-disable-next-line @typescript-eslint/no-floating-promises
        routeService.navigate(['/group', groupId, 'locked']);
        return false;
      }
      if (res.granted === false) {
        if (loginService.isGuest()) {
          redirectService.mustRedirect();
        }
        // eslint-disable-next-line @typescript-eslint/no-floating-promises
        routeService.navigate(['/denied']);
      }
      if (res.granted !== undefined) {
        return res.granted;
      }
    }
  }

  return false;
};
