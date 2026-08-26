import { Component, OnInit } from '@angular/core';

import { RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { EventItemDefinition, UserService } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { getFullDate } from 'app/core/util';
import { DatePipe } from 'app/shared/pipes/date.pipe';
import { TimePipe } from 'app/shared/pipes/time.pipe';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-user-events',
  templateUrl: './user-events.component.html',
  styleUrl: './user-events.component.scss',
  preserveWhitespaces: true,
  imports: [RouterLink, DatePipe, TimePipe, TranslocoModule],
})
export class UserEventsComponent implements OnInit {
  public events!: EventItemDefinition[];

  constructor(
    private userService: UserService,
    private uiMessageService: UiMessageService,
    private loginService: LoginService
  ) {}

  async ngOnInit() {
    await this.getEvents();
  }

  private async getEvents() {
    const exactDate: Date = new Date();
    try {
      this.events = await firstValueFrom(
        this.userService.getUserEventsPeriod(
          this.getUserId(),
          getFullDate(exactDate),
          'Future'
        )
      );
    } catch (error) {
      const jsonError = JSON.parse(error._body) as Record<string, string>;
      if (jsonError && 'message' in jsonError) {
        this.uiMessageService.addErrorMessage(jsonError.message);
      }
    }
  }

  private getUserId(): string {
    return this.loginService.getCurrentUsername();
  }
}
