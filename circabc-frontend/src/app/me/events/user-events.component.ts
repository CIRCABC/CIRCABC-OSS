import { Component, OnInit } from '@angular/core';

import { EventItemDefinition, UserService } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { getFullDate } from 'app/core/util';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-user-events',
  templateUrl: './user-events.component.html',
  styleUrls: ['./user-events.component.scss'],
  preserveWhitespaces: true,
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
      const jsonError = JSON.parse(error._body);
      if (jsonError) {
        this.uiMessageService.addErrorMessage(jsonError.message);
      }
    }
  }

  private getUserId(): string {
    return this.loginService.getCurrentUsername();
  }
}
