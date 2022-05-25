import { Injectable } from '@angular/core';
import {
  TimeZoneHelper,
  TimeZoneLookupKeys,
} from 'app/shared/pipes/timezonehelper';
import { EventItemDefinition } from './generated/circabc';
import { getFormattedDate, getFormattedTime } from './util';

@Injectable({
  providedIn: 'root',
})
export class TimeZoneHelperService {
  toLocalDateTime(events: EventItemDefinition[]): EventItemDefinition[] {
    return events.map((event) => {
      const timezone: string =
        TimeZoneHelper.timeZoneLookup[event.timeZone as TimeZoneLookupKeys];

      const dateStartNumber = Date.parse(
        `${event.appointmentDate}T${event.startTime}:00.000${timezone}`
      );
      const dateEndNumber = Date.parse(
        `${event.appointmentDate}T${event.endTime}:00.000${timezone}`
      );
      const dateStartDate = new Date(dateStartNumber);
      const dateEndDate = new Date(dateEndNumber);

      event.appointmentDate = getFormattedDate(dateStartDate);
      event.startTime = getFormattedTime(dateStartDate);
      event.endTime = getFormattedTime(dateEndDate);

      return event;
    });
  }
}
