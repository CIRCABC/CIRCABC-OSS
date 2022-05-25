import { Injectable, Pipe, PipeTransform } from '@angular/core';

import { TranslocoService } from '@ngneat/transloco';
import { TimeZoneHelper, TimeZoneLookupKeys } from './timezonehelper';

@Pipe({
  name: 'cbcDate',
  // eslint-disable-next-line @angular-eslint/no-pipe-impure
  pure: false,
})
@Injectable()
export class DatePipe implements PipeTransform {
  public constructor(private translateService: TranslocoService) {}

  transform(
    date: string | undefined,
    time: string | undefined,
    timeZone: string | undefined
  ): string {
    if (date === undefined || time === undefined || timeZone === undefined) {
      return '';
    }

    const timezone: string =
      TimeZoneHelper.timeZoneLookup[timeZone as TimeZoneLookupKeys];

    const dateNumber = Date.parse(`${date}T${time}:00.000${timezone}`);

    const dateDate = new Date(dateNumber);

    return dateDate.toLocaleDateString(
      this.translateService.getActiveLang() === 'en'
        ? 'en-GB'
        : this.translateService.getActiveLang()
    );
  }
}
