import { Component, OnInit, input } from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';
import { DashboardService, UserNewsFeed } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';
import { firstValueFrom } from 'rxjs';
import { TimelineStepComponent } from './timeline-step/timeline-step.component';

@Component({
  selector: 'cbc-user-timeline',
  templateUrl: './user-timeline.component.html',
  styleUrl: './user-timeline.component.scss',
  preserveWhitespaces: true,
  imports: [SpinnerComponent, TimelineStepComponent, TranslocoModule],
})
export class UserTimelineComponent implements OnInit {
  readonly when = input.required<string>();

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
        this.dashboardService.getUserNewsFeed(username, this.when())
      );
    } catch (error) {
      console.error(error);
      this.error = true;
    } finally {
      this.loading = false;
    }
  }
}
