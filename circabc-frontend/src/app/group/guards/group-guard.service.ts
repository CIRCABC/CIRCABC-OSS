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
export class GroupGuard implements CanActivate {
  constructor(
    private guardsService: GuardsService,
    private router: Router,
    private loginService: LoginService,
    private redirectionService: RedirectionService
  ) {}

  async canActivate(
    route: ActivatedRouteSnapshot,
    _state: RouterStateSnapshot
  ) {
    const groupId = route.paramMap.get('id');

    if (groupId && groupId !== '0') {
      let res;

      try {
        res = await firstValueFrom(this.guardsService.getGuardGroup(groupId));
      } catch (error) {
        console.error(error);
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

    return false;
  }
}
