import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivateFn, Router } from '@angular/router';

import { GuardsService } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { RedirectionService } from 'app/core/redirection.service';
import { firstValueFrom } from 'rxjs';

export const canActivateAdmin: CanActivateFn = async (
  route: ActivatedRouteSnapshot
) => {
  const guardsService = inject(GuardsService);
  const router = inject(Router);
  const loginService = inject(LoginService);
  const redirectionService = inject(RedirectionService);

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  let nodeId: any;
  // group admin context
  if (route.paramMap?.has('id')) {
    nodeId = route.paramMap.get('id');
    // node admin context
    if (route.paramMap.has('nodeId')) {
      nodeId = route.paramMap.get('nodeId');
    }
  }

  if (nodeId && nodeId !== '0') {
    try {
      const res = await firstValueFrom(guardsService.getGuardAdmin(nodeId));
      if (res !== undefined) {
        if (res.granted === false) {
          if (loginService.isGuest()) {
            redirectionService.mustRedirect();
          }
          // eslint-disable-next-line @typescript-eslint/no-floating-promises
          router.navigate(['/denied']);
        }
        if (res.granted !== undefined) {
          return res.granted;
        }
      }
    } catch (_error) {
      if (loginService.isGuest()) {
        redirectionService.mustRedirect();
      }
      // eslint-disable-next-line @typescript-eslint/no-floating-promises
      router.navigate(['/denied']);
      return false;
    }
  }

  return false;
};
