import { Service } from '@angular/core';
import {
  TimeZoneLookupKeys,
  timeZoneLookup,
} from 'app/shared/pipes/timezonehelper';
import { EventItemDefinition } from './generated/circabc';
import { compensateDST, getFormattedDate, getFormattedTime } from './util';

/**
 * Root-provided Angular service that converts agenda/calendar event times
 * from their stored source time zone into the browser's local date and time.
 *
 * It relies on the following collaborators:
 * - `timeZoneLookup` (from `app/shared/pipes/timezonehelper`) to resolve an
 *   event's stored time-zone identifier into a UTC offset suffix.
 * - `compensateDST` to adjust for daylight-saving-time discrepancies.
 * - `getFormattedDate` / `getFormattedTime` to format the resulting dates back
 *   into the string representation used by {@link EventItemDefinition}.
 */
@Service()
export class TimeZoneHelperService {
  /**
   * Converts each event's appointment date, start time and end time from the
   * event's source time zone into the local date/time of the current browser.
   *
   * For every event the stored `timeZone` is resolved to an offset, the start
   * and end instants are parsed, adjusted for daylight saving time and then
   * written back onto the event as formatted date/time strings.
   *
   * @param events The events to convert. Each item is mutated in place; its
   * `appointmentDate`, `startTime` and `endTime` fields are overwritten with
   * their locally formatted equivalents.
   * @returns The same array of events with their date/time fields converted to
   * local time.
   */
  toLocalDateTime(events: EventItemDefinition[]): EventItemDefinition[] {
    return events.map((event) => {
      const timezone: string =
        timeZoneLookup[event.timeZone as TimeZoneLookupKeys];

      const dateStartNumber = Date.parse(
        `${event.appointmentDate}T${event.startTime}:00.000${timezone}`
      );
      const dateEndNumber = Date.parse(
        `${event.appointmentDate}T${event.endTime}:00.000${timezone}`
      );
      const dateStartDate = compensateDST(new Date(dateStartNumber));
      const dateEndDate = compensateDST(new Date(dateEndNumber));

      event.appointmentDate = getFormattedDate(dateStartDate);
      event.startTime = getFormattedTime(dateStartDate);
      event.endTime = getFormattedTime(dateEndDate);

      return event;
    });
  }
}
