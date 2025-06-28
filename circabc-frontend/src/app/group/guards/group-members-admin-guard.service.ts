import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivateFn, Router } from '@angular/router';

import { GuardsService } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { RedirectionService } from 'app/core/redirection.service';
import { firstValueFrom } from 'rxjs';

export const canActivateGroupMembersAdmin: CanActivateFn = async (
  route: ActivatedRouteSnapshot
) => {
  const guardService = inject(GuardsService);
  const loginService = inject(LoginService);
  const redirectService = inject(RedirectionService);
  const routeService = inject(Router);
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  let nodeId: any;
  // group admin context
  if (route.paramMap?.has('id')) {
    nodeId = route.paramMap.get('id');
  }

  if (nodeId && nodeId !== '0') {
    const res = await firstValueFrom(
      guardService.getGuardGroupMembersAdmin(nodeId)
    );
    if (res !== undefined) {
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
