import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivateFn, Router } from '@angular/router';

import { GuardsService } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { RedirectionService } from 'app/core/redirection.service';
import { firstValueFrom } from 'rxjs';

export const canActivateNodeAccess: CanActivateFn = async (
  route: ActivatedRouteSnapshot
) => {
  const guardService = inject(GuardsService);
  const loginService = inject(LoginService);
  const redirectService = inject(RedirectionService);
  const routeService = inject(Router);
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
      // eslint-disable-next-line @typescript-eslint/no-floating-promises
      routeService.navigate(['/no-content']);
    } else if (res !== undefined) {
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
