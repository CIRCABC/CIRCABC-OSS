import { Component, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { EULoginService } from 'app/core/eulogin.service';
import { LoginService } from 'app/core/login.service';
import { RedirectionService } from 'app/core/redirection.service';
import { DataCyDirective } from 'app/shared/directives/data-cy.directive';
import { LangSelectorComponent } from 'app/shared/lang/lang-selector.component';
import { SystemMessageIndicatorComponent } from 'app/shared/system-message-indicator/system-message-indicator.component';
import { environment } from 'environments/environment';
import { CookieService } from 'ngx-cookie-service';

@Component({
  selector: 'cbc-welcome',
  templateUrl: './welcome.component.html',
  styleUrl: './welcome.component.scss',
  preserveWhitespaces: true,
  providers: [CookieService],
  imports: [
    SystemMessageIndicatorComponent,
    RouterLink,
    LangSelectorComponent,
    DataCyDirective,
    TranslocoModule,
  ],
})
export class WelcomeComponent implements OnInit {
  public circabcRelease = environment.circabcRelease;
  public waitingAfterLogin = false;
  public isOSS = false;

  constructor(
    private cookieService: CookieService,
    private translateService: TranslocoService,
    private euLoginService: EULoginService,
    private loginService: LoginService,
    private redirectionService: RedirectionService,
    private router: Router
  ) {}

  async ngOnInit() {
    if (environment.circabcRelease === 'oss') {
      this.isOSS = true;
    } else {
      const username = this.cookieService.get('username');
      const ticket = this.cookieService.get('ticket');
      const route = this.cookieService.get('route');
      this.cookieService.delete('username', '/');
      this.cookieService.delete('ticket', '/');
      this.cookieService.delete('route', '/');
      if (username.length > 0 && ticket.length > 0) {
        this.waitingAfterLogin = true;
        const result = await this.loginService.loadUser(username, ticket);
        if (result) {
          if (route.length > 0) {
            if (route === 'calendar') {
              await this.router.navigate(['/me/calendar']);
            } else if (route === 'roles') {
              await this.router.navigate(['/me/roles']);
            } else if (route === 'explore') {
              await this.router.navigate(['explore']);
            } else {
              await this.router.navigate(['/me']);
            }
          } else {
            await this.redirectionService.redirect();
          }
        } else {
          this.waitingAfterLogin = false;
        }
      } else if (!this.loginService.isGuest()) {
        await this.redirectionService.redirect();
      }
    }
  }

  public async euLogin() {
    if (this.loginService.isGuest()) {
      this.euLoginService.euLogin();
    } else {
      await this.router.navigate(['/me']);
    }
  }

  public euLoginCreate() {
    window.location.href =
      '';
  }

  public get useEULogin(): boolean {
    return true;
  }

  get currentLang(): string {
    return this.translateService.getActiveLang();
  }

  public refreshUILang(event: string): void {
    this.translateService.setActiveLang(event);
  }
}
