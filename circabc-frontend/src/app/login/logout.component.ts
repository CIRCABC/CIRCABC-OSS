import { Component, OnInit } from '@angular/core';

import { Router } from '@angular/router';
import { EULoginService } from 'app/core/eulogin.service';
import { LoginService } from 'app/core/login.service';
import { environment } from 'environments/environment';

@Component({
  selector: 'cbc-logout',
  templateUrl: './logout.component.html',
  styleUrls: ['./logout.component.scss'],
  preserveWhitespaces: true,
})
export class LogoutComponent implements OnInit {
  public constructor(
    private loginService: LoginService,
    private euloginservice: EULoginService,
    private router: Router
  ) {}

  public async ngOnInit() {
    await this.loginService.logout();
    if (environment.circabcRelease === 'oss') {
      this.router.navigate(['/welcome']);
    } else {
      this.euloginservice.logout();
    }
  }
}
