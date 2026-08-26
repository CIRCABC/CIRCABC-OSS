import { inject } from '@angular/core';
import {
  ActivatedRouteSnapshot,
  CanActivateFn,
  Router,
  RouterStateSnapshot,
} from '@angular/router';

import { GuardsService } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { RedirectionService } from 'app/core/redirection.service';
import { firstValueFrom } from 'rxjs';

export const canActivateService: CanActivateFn = async (
  route: ActivatedRouteSnapshot,
  state: RouterStateSnapshot
) => {
  const guardService = inject(GuardsService);
  const loginService = inject(LoginService);
  const redirectService = inject(RedirectionService);
  const routeService = inject(Router);
  if (route.parent?.parent) {
    const nodeId = route.parent.parent.paramMap.get('id');
    let name = '';

    if (state.url.indexOf('members') !== -1) {
      name = 'members';
    } else if (state.url.indexOf('applicants') !== -1) {
      name = 'applicants';
    } else if (state.url.indexOf('information') !== -1) {
      name = 'information';
    }

    if (nodeId && nodeId !== '0') {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      let res: any;
      try {
        res = await firstValueFrom(
          guardService.getGuardGroupService(nodeId, name)
        );
      } catch (_error) {
        res = { granted: false };
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
  }

  return false;
};
