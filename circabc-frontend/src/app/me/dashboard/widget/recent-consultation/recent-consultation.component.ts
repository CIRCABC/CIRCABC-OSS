import { Component, OnInit } from '@angular/core';

import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { DashboardService, UserActionLog } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { removeDuplicates } from 'app/core/util';
import { FileExtensionIconComponent } from 'app/shared/file-extension-icon/file-extension-icon.component';
import { HorizontalLoaderComponent } from 'app/shared/loader/horizontal-loader.component';
import { UserCardComponent } from 'app/shared/user-card/user-card.component';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-recent-consultation',
  templateUrl: './recent-consultation.component.html',
  styleUrl: './recent-consultation.component.scss',
  preserveWhitespaces: true,
  imports: [
    HorizontalLoaderComponent,
    FileExtensionIconComponent,
    RouterLink,
    UserCardComponent,
    DatePipe,
    TranslocoModule,
  ],
})
export class RecentConsultationComponent implements OnInit {
  public lastDownloads: UserActionLog[] = [];
  public lastUploads: UserActionLog[] = [];
  public loadingDownloads = false;
  public loadingUploads = false;
  public showDownloads = true;

  constructor(
    private dashboardService: DashboardService,
    private loginService: LoginService
  ) {}

  async ngOnInit() {
    this.loadingDownloads = true;
    this.loadingUploads = true;

    const userName = this.loginService.getCurrentUsername();
    if (userName) {
      this.lastDownloads = removeDuplicates(
        await firstValueFrom(this.dashboardService.getUserDownloads(userName)),
        this.compare
      );
      this.loadingDownloads = false;

      this.lastUploads = removeDuplicates(
        await firstValueFrom(this.dashboardService.getUserUploads(userName)),
        this.compare
      );
      this.loadingUploads = false;
    }
  }

  private compare(item1: UserActionLog, item2: UserActionLog): boolean {
    return (
      item1.node !== undefined &&
      item2.node !== undefined &&
      item1.node.id === item2.node.id
    );
  }
}
