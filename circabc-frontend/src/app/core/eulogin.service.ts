import { Injectable } from '@angular/core';
import { LoginService } from 'app/core/login.service';
import { environment } from 'environments/environment';

@Injectable({
  providedIn: 'root',
})
export class EULoginService {
  public constructor(private loginService: LoginService) {}

  public euLogin() {
    if (this.loginService.isGuest()) {
      window.location.href = environment.euloginUrl;
    }
  }

  public logout() {
    window.location.href = environment.eulogoutUrl;
  }
}
