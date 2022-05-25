import { Component, Input, OnInit } from '@angular/core';
import { EventItemDefinition, EventsService } from 'app/core/generated/circabc';
import { getFullDate } from 'app/core/util';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-events-dashlet',
  templateUrl: './events-dashlet.component.html',
  styleUrls: ['./events-dashlet.component.scss'],
  preserveWhitespaces: true,
})
export class EventsDashletComponent implements OnInit {
  @Input()
  public igId!: string;
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
          this.igId,
          getFullDate(startDate),
          getFullDate(endDate)
        )
      );
    } catch (error) {
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
