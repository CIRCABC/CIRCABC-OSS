import { Location } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { EULoginService } from 'app/core/eulogin.service';
import { LoginService } from 'app/core/login.service';
import { HeaderComponent } from 'app/shared/header/header.component';
import { NavigatorComponent } from 'app/shared/navigator/navigator.component';
import { environment } from 'environments/environment';

@Component({
  selector: 'cbc-access-denied',
  templateUrl: './access-denied.component.html',
  styleUrl: './access-denied.component.scss',
  preserveWhitespaces: true,
  imports: [HeaderComponent, NavigatorComponent, TranslocoModule],
})
export class AccessDeniedComponent implements OnInit {
  public isGuest = false;
  public requestedUrl!: string;

  constructor(
    private loginService: LoginService,
    private euLoginService: EULoginService,
    private location: Location,
    private router: Router
  ) {}
  ngOnInit(): void {
    this.isGuest = this.loginService.isGuest();
  }
  goBack() {
    this.location.back();
  }
  async goToLogin() {
    // eslint-disable-next-line @typescript-eslint/no-floating-promises
    this.router.navigate(['/login']);
  }

  public euLogin() {
    this.euLoginService.euLogin();
  }

  public get useEULogin(): boolean {
    return environment.circabcRelease !== 'oss';
  }
}
