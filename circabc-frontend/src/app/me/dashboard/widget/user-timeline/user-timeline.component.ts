import { Component, Input, OnInit } from '@angular/core';
import { DashboardService, UserNewsFeed } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-user-timeline',
  templateUrl: './user-timeline.component.html',
  styleUrls: ['./user-timeline.component.scss'],
  preserveWhitespaces: true,
})
export class UserTimelineComponent implements OnInit {
  @Input()
  when!: string;

  public userFeed!: UserNewsFeed;
  public loading = false;
  public error = false;

  constructor(
    private dashboardService: DashboardService,
    private loginService: LoginService
  ) {}

  async ngOnInit() {
    this.loading = true;
    const username = this.loginService.getCurrentUsername();
    try {
      this.userFeed = await firstValueFrom(
        this.dashboardService.getUserNewsFeed(username, this.when)
      );
    } catch (error) {
      console.error(error);
      this.error = true;
    } finally {
      this.loading = false;
    }
  }
}
