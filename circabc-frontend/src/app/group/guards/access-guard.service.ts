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

export const canActivateNodeAccess: CanActivateFn = async (
  route: ActivatedRouteSnapshot,
  state: RouterStateSnapshot
) => {
  const guardService = inject(GuardsService);
  const loginService = inject(LoginService);
  const redirectService = inject(RedirectionService);
  const routeService = inject(Router);
  const migratedLink = inject(MigratedLinkService);
  let nodeId = route.paramMap.get('nodeId');

  if (route.paramMap.get('forumId')) {
    nodeId = route.paramMap.get('forumId');
  }

  if (route.paramMap.get('topicId')) {
    nodeId = route.paramMap.get('topicId');
  }

  if (nodeId && nodeId !== '0') {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    let res: any;

    try {
      res = await firstValueFrom(guardService.getGuardAccess(nodeId));
    } catch (error) {
      console.error(error);
    }
    if (res === null) {
      // Old/other-host link: try to resolve the original node ref and redirect.
      if (await migratedLink.tryRedirectFromOriginal(state.url)) {
        return false;
      }
      // eslint-disable-next-line @typescript-eslint/no-floating-promises
      routeService.navigate(['/no-content']);
    } else if (res !== undefined) {
      if (res.granted === false) {
        if (loginService.isGuest()) {
          redirectService.mustRedirect();
        }

        if (route.queryParamMap.get('fromLink') === 'true') {
          //let library-browser display error message
          return false;
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
