import { Injectable } from '@angular/core';
import { TranslocoService } from '@ngneat/transloco';

@Injectable({
  providedIn: 'root',
})
export class LocalizationService {
  private cacheDayNames = new Map();
  private cacheMonthNames = new Map();

  constructor(private translateService: TranslocoService) {}
  getDayNames(
    firstDay: 'Sunday' | 'Monday' = 'Sunday',
    name: 'full' | 'short' = 'full',
    numberOfDays: 5 | 7 = 7
  ): { [key: string]: string } {
    const cacheKey = `${firstDay}-${name}-${numberOfDays}-${this.translateService.getActiveLang()}`;

    if (this.cacheDayNames.has(cacheKey)) {
      return this.cacheDayNames.get(cacheKey);
    }

    const dayNamesArray = this.getDayNamesArray(name);
    const dayNames = dayNamesArray.data;
    if (firstDay === 'Monday') {
      dayNames.push(dayNames.shift() as string);
    }
    let result: { [key: string]: string };
    if (numberOfDays === 5) {
      result = {
        0: dayNames[0],
        1: dayNames[1],
        2: dayNames[2],
        3: dayNames[3],
        4: dayNames[4],
      };
    } else {
      result = {
        0: dayNames[0],
        1: dayNames[1],
        2: dayNames[2],
        3: dayNames[3],
        4: dayNames[4],
        5: dayNames[5],
        6: dayNames[6],
      };
    }
    if (dayNamesArray.shouldCache) {
      this.cacheDayNames.set(cacheKey, result);
    }
    return result;
  }
  getMonthsNames(): { [key: string]: string } {
    const cacheKey = `${this.translateService.getActiveLang()}`;
    if (this.cacheMonthNames.has(cacheKey)) {
      return this.cacheMonthNames.get(cacheKey);
    }
    const monthsNamesArray = this.getMonthsNamesArray();
    const monthNames = monthsNamesArray.data;
    const result = {
      1: monthNames[0],
      2: monthNames[1],
      3: monthNames[2],
      4: monthNames[3],
      5: monthNames[4],
      6: monthNames[5],
      7: monthNames[6],
      8: monthNames[7],
      9: monthNames[8],
      10: monthNames[9],
      11: monthNames[10],
      12: monthNames[11],
    };
    if (monthsNamesArray.shouldCache) {
      this.cacheMonthNames.set(cacheKey, result);
    }
    return result;
  }
  private getDayNamesArray(name: 'full' | 'short'): {
    data: string[];
    shouldCache: boolean;
  } {
    const result =
      name === 'full'
        ? this.translateService.translateObject('primeng')['dayNames']
        : this.translateService.translateObject('primeng')['dayNamesShort'];
    if (result !== undefined) {
      return { data: result as string[], shouldCache: true };
    } else {
      return name === 'full'
        ? {
            data: [
              'Sunday',
              'Monday',
              'Tuesday',
              'Wednesday',
              'Thursday',
              'Friday',
              'Saturday',
            ],
            shouldCache: false,
          }
        : {
            data: ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'],
            shouldCache: false,
          };
    }
  }
  private getMonthsNamesArray(): {
    data: readonly string[];
    shouldCache: boolean;
  } {
    const result: undefined | readonly string[] =
      this.translateService.translateObject('primeng')['monthNames'];

    if (result !== undefined) {
      return { data: result as string[], shouldCache: true };
    } else {
      return {
        data: [
          'January',
          'February',
          'March',
          'April',
          'May',
          'June',
          'July',
          'August',
          'September',
          'October',
          'November',
          'December',
        ],
        shouldCache: false,
      };
    }
  }
  getFirstDayOfWeek(): number {
    if (this.translateService.getActiveLang() === 'pt') {
      return 0;
    } else {
      return 1;
    }
  }
}
