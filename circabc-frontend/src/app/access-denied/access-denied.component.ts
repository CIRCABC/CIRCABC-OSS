import { Location } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { EULoginService } from 'app/core/eulogin.service';
import { LoginService } from 'app/core/login.service';
import { environment } from 'environments/environment';

@Component({
  selector: 'cbc-access-denied',
  templateUrl: './access-denied.component.html',
  styleUrls: ['./access-denied.component.scss'],
  preserveWhitespaces: true,
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
