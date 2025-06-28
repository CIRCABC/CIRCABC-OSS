import { Component, OnInit, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { EventItemDefinition, EventsService } from 'app/core/generated/circabc';
import { getFullDate } from 'app/core/util';
import { HorizontalLoaderComponent } from 'app/shared/loader/horizontal-loader.component';
import { DatePipe } from 'app/shared/pipes/date.pipe';
import { TimePipe } from 'app/shared/pipes/time.pipe';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-events-dashlet',
  templateUrl: './events-dashlet.component.html',
  styleUrl: './events-dashlet.component.scss',
  preserveWhitespaces: true,
  imports: [
    HorizontalLoaderComponent,
    RouterLink,
    DatePipe,
    TimePipe,
    TranslocoModule,
  ],
})
export class EventsDashletComponent implements OnInit {
  public readonly igId = input.required<string>();
  public events: EventItemDefinition[] = [];
  public more = false;
  public loading = false;
  public restCallError = false;

  constructor(private eventsService: EventsService) {}

  async ngOnInit() {
    await this.getEvents();
  }

  private async getEvents() {
    const startDate: Date = new Date();
    const endDate: Date = new Date();
    const intervalInDays = 365;
    endDate.setDate(endDate.getDate() + intervalInDays); // get events happening in the next 10 days from today
    this.loading = true;
    this.restCallError = false;
    try {
      this.events = await firstValueFrom(
        this.eventsService.getInterestGroupEvents(
          this.igId(),
          getFullDate(startDate),
          getFullDate(endDate)
        )
      );
    } catch (_error) {
      this.events = [];
      this.restCallError = true;
    }
    this.loading = false;
  }

  public getListOfEvents(): EventItemDefinition[] {
    let result: EventItemDefinition[] = [];

    if (this.more) {
      result = this.events;
    } else {
      for (let i = 0; i < this.events.length && i < 8; i += 1) {
        result.push(this.events[i]);
      }
    }

    return result;
  }
}
