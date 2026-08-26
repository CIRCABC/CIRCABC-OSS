import { AbstractControl } from '@angular/forms';
import { pairwise, startWith } from 'rxjs';

/**
 * Sets up a subscription to handle calendar date changes for a form control,
 * specifically adjusting the hour when minutes change from 59 to 0 or from 0 to 59.
 *
 * @param control The AbstractControl to monitor for date changes
 * @returns The subscription that can be unsubscribed if needed
 */
export function setupCalendarDateHandling(control: AbstractControl) {
  return control.valueChanges
    .pipe(startWith(control.value), pairwise())
    .subscribe(([previousValue, currentValue]) => {
      if (previousValue && currentValue) {
        const previousDate = new Date(previousValue);
        const currentDate = new Date(currentValue);

        const previousMinutes = previousDate.getMinutes();
        const currentMinutes = currentDate.getMinutes();

        const previousHours = previousDate.getHours();
        const currentHours = currentDate.getHours();

        const previousDay = previousDate.getDate();
        const currentDay = currentDate.getDate();

        const previousMonth = previousDate.getMonth();
        const currentMonth = currentDate.getMonth();

        const previousYear = previousDate.getFullYear();
        const currentYear = currentDate.getFullYear();

        if (previousMinutes === 59 && currentMinutes === 0) {
          currentDate.setHours(currentDate.getHours() + 1);

          if (previousHours === 23 && currentHours === 0) {
            currentDate.setDate(currentDate.getDate() + 1);

            if (
              previousDay ===
                new Date(previousYear, previousMonth + 1, 0).getDate() &&
              currentDay === 1
            ) {
              currentDate.setMonth(currentDate.getMonth() + 1);

              if (previousMonth === 11 && currentMonth === 0) {
                currentDate.setFullYear(currentDate.getFullYear() + 1);
              }
            }
          }
        } else if (previousMinutes === 0 && currentMinutes === 59) {
          currentDate.setHours(currentDate.getHours() - 1);

          if (previousHours === 0 && currentHours === 23) {
            currentDate.setDate(currentDate.getDate() - 1);

            if (
              previousDay === 1 &&
              currentDay ===
                new Date(currentYear, currentMonth + 1, 0).getDate()
            ) {
              currentDate.setMonth(currentDate.getMonth() - 1);

              if (previousMonth === 0 && currentMonth === 11) {
                currentDate.setFullYear(currentDate.getFullYear() - 1);
              }
            }
          }
        }

        if (
          previousHours === 23 &&
          currentHours === 0 &&
          previousMinutes === currentMinutes
        ) {
          currentDate.setDate(currentDate.getDate() + 1);

          if (
            previousDay ===
              new Date(previousYear, previousMonth + 1, 0).getDate() &&
            currentDay === 1
          ) {
            currentDate.setMonth(currentDate.getMonth() + 1);

            if (previousMonth === 11 && currentMonth === 0) {
              currentDate.setFullYear(currentDate.getFullYear() + 1);
            }
          }
        } else if (
          previousHours === 0 &&
          currentHours === 23 &&
          previousMinutes === currentMinutes
        ) {
          currentDate.setDate(currentDate.getDate() - 1);

          if (
            previousDay === 1 &&
            currentDay === new Date(currentYear, currentMonth + 1, 0).getDate()
          ) {
            currentDate.setMonth(currentDate.getMonth() - 1);

            if (previousMonth === 0 && currentMonth === 11) {
              currentDate.setFullYear(currentDate.getFullYear() - 1);
            }
          }
        }

        control.setValue(currentDate, { emitEvent: false });
      }
    });
}
