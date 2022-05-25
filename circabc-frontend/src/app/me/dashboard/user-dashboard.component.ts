import { Component, OnInit } from '@angular/core';

import { InterestGroupProfile, UserService } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-user-dashboard',
  templateUrl: './user-dashboard.component.html',
  styleUrls: ['./user-dashboard.component.scss'],
  preserveWhitespaces: true,
})
export class UserDashboardComponent implements OnInit {
  public timeline!: string[];
  public memberships!: InterestGroupProfile[];
  public loadingMemberships = false;

  public constructor(
    private loginService: LoginService,
    private userService: UserService
  ) {}

  public async ngOnInit() {
    await this.loadMemberships(this.getUserId());
  }

  private async loadMemberships(userId: string) {
    this.loadingMemberships = true;
    this.memberships = await firstValueFrom(
      this.userService.getUserMembership(userId)
    );
    this.loadingMemberships = false;
  }

  private getUserId(): string {
    return this.loginService.getCurrentUsername();
  }
}
