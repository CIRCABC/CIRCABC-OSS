import { Component, OnInit } from '@angular/core';

import { RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { EULoginService } from 'app/core/eulogin.service';
import { User, UserService } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { DownloadPipe } from 'app/shared/pipes/download.pipe';
import { SecurePipe } from 'app/shared/pipes/secure.pipe';
import { environment } from 'environments/environment';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-personal-menu',
  templateUrl: './personal-menu.component.html',
  preserveWhitespaces: true,
  imports: [RouterLink, TranslocoModule, DownloadPipe, SecurePipe],
})
export class PersonalMenuComponent implements OnInit {
  public user!: User;
  public subMenu = false;
  get theme(): string {
    const theme = document.documentElement.getAttribute('theme');
    return theme ? theme : 'white';
  }

  set theme(name: string) {
    localStorage.setItem('theme', name);
    if (name === 'dark') {
      document.documentElement.setAttribute('theme', name);
    } else {
      document.documentElement.removeAttribute('theme');
    }
  }

  public constructor(
    private loginService: LoginService,
    private userService: UserService,
    private euLoginService: EULoginService
  ) {}

  public async ngOnInit() {
    if (!this.isGuest()) {
      await this.setUser();
    }
  }

  public isGuest(): boolean {
    return this.loginService.isGuest();
  }

  public isUser(): boolean {
    return !this.loginService.isGuest() && this.user !== undefined;
  }

  public userName(): string {
    return this.loginService.getCurrentUsername();
  }

  public async setUser() {
    this.user = this.loginService.getUser();
    // load user data from the backend to reflect
    // changes/updates, like for example the avatar
    this.user = await firstValueFrom(
      this.userService.getUser(this.user.userId as string)
    );
  }

  public isAppAdmin(): boolean {
    const user: User = this.loginService.getUser();
    if (
      user.properties === null ||
      user.userId === '' ||
      user.userId === 'guest'
    ) {
      return false;
    }
    return (
      user.properties !== undefined &&
      (user.properties.isAdmin === 'true' ||
        user.properties.isCircabcAdmin === 'true')
    );
  }

  public get useEULogin(): boolean {
    return environment.circabcRelease !== 'oss';
  }

  public euLogin() {
    this.euLoginService.euLogin();
  }

  public setScreenMode() {
    if (this.theme === 'dark') {
      this.theme = 'white';
    } else {
      this.theme = 'dark';
    }
  }
}
