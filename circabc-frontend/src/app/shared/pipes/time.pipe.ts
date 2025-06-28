import { Injectable, Pipe, PipeTransform } from '@angular/core';

import { TranslocoService } from '@jsverse/transloco';
import { compensateDST } from 'app/core/util';
import { TimeZoneLookupKeys, timeZoneLookup } from './timezonehelper';

@Pipe({
  name: 'cbcTime',
  // eslint-disable-next-line @angular-eslint/no-pipe-impure
  pure: false,
})
@Injectable()
export class TimePipe implements PipeTransform {
  public constructor(private translateService: TranslocoService) {}

  transform(
    date: string | undefined,
    time: string | undefined,
    timeZone: string | undefined
  ): string {
    if (date === undefined || time === undefined || timeZone === undefined) {
      return '';
    }

    const timezone: string = timeZoneLookup[timeZone as TimeZoneLookupKeys];

    const dateNumber = Date.parse(`${date}T${time}:00.000${timezone}`);

    const dateDate = compensateDST(new Date(dateNumber));

    return dateDate.toLocaleTimeString(this.translateService.getActiveLang());
  }
}
