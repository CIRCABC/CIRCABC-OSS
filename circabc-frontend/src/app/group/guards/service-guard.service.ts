import { Injectable } from '@angular/core';
import {
  ActivatedRouteSnapshot,
  CanActivate,
  Router,
  RouterStateSnapshot,
} from '@angular/router';

import { GuardsService } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { RedirectionService } from 'app/core/redirection.service';
import { firstValueFrom } from 'rxjs';

@Injectable()
export class ServiceAccessGuard implements CanActivate {
  constructor(
    private guardsService: GuardsService,
    private router: Router,
    private loginService: LoginService,
    private redirectionService: RedirectionService
  ) {}

  async canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
    if (route.parent && route.parent.parent) {
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
        let res;
        try {
          res = await firstValueFrom(
            this.guardsService.getGuardGroupService(nodeId, name)
          );
        } catch (error) {
          res = { granted: false };
        }

        if (res === null) {
          // eslint-disable-next-line @typescript-eslint/no-floating-promises
          this.router.navigate(['/no-content']);
        } else if (res !== undefined) {
          if (res.granted === false) {
            if (this.loginService.isGuest()) {
              this.redirectionService.mustRedirect();
            }
            // eslint-disable-next-line @typescript-eslint/no-floating-promises
            this.router.navigate(['/denied']);
          }
          if (res.granted !== undefined) {
            return res.granted;
          }
        }
      }
    }

    return false;
  }
}
