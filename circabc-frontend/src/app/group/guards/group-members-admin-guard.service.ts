import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router } from '@angular/router';

import { GuardsService } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { RedirectionService } from 'app/core/redirection.service';
import { firstValueFrom } from 'rxjs';

@Injectable()
export class GroupMembersAdminGuard implements CanActivate {
  constructor(
    private guardsService: GuardsService,
    private router: Router,
    private loginService: LoginService,
    private redirectionService: RedirectionService
  ) {}

  async canActivate(route: ActivatedRouteSnapshot) {
    let nodeId;
    // group admin context
    if (route.paramMap && route.paramMap.has('id')) {
      nodeId = route.paramMap.get('id');
    }

    if (nodeId && nodeId !== '0') {
      const res = await firstValueFrom(
        this.guardsService.getGuardGroupMembersAdmin(nodeId)
      );
      if (res !== undefined) {
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
