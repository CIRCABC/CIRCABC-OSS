import { Injectable } from '@angular/core';
import {
  ActivatedRouteSnapshot,
  CanActivate,
  Router,
  RouterStateSnapshot,
} from '@angular/router';
import { LoginService } from 'app/core/login.service';
import { RedirectionService } from 'app/core/redirection.service';

@Injectable({
  providedIn: 'root',
})
export class AuthGuard implements CanActivate {
  public constructor(
    private loginService: LoginService,
    private router: Router,
    private redirectionService: RedirectionService
  ) {}
  public async canActivate(
    _route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Promise<boolean> {
    const url: string = state.url;

    return await this.checkLogin(url);
  }

  private async checkLogin(_url: string): Promise<boolean> {
    if (!this.loginService.isGuest()) {
      return true;
    }

    // store the attempted URL for redirecting
    this.redirectionService.mustRedirect();

    // eslint-disable-next-line @typescript-eslint/no-floating-promises
    this.router.navigate(['/denied']);

    return false;
  }
}
