import { FormControl } from '@angular/forms';
import { setupCalendarDateHandling } from 'app/core/util/date-calendar-util';

describe('setupCalendarDateHandling', () => {
  it('should increment hour when minutes roll from 59 to 0', () => {
    const control = new FormControl(new Date(2026, 3, 15, 10, 59, 0));
    const sub = setupCalendarDateHandling(control);

    control.setValue(new Date(2026, 3, 15, 10, 0, 0));

    const result = control.value as Date;
    expect(result.getHours()).toBe(11);
    expect(result.getMinutes()).toBe(0);
    sub.unsubscribe();
  });

  it('should decrement hour when minutes roll from 0 to 59', () => {
    const control = new FormControl(new Date(2026, 3, 15, 10, 0, 0));
    const sub = setupCalendarDateHandling(control);

    control.setValue(new Date(2026, 3, 15, 10, 59, 0));

    const result = control.value as Date;
    expect(result.getHours()).toBe(9);
    expect(result.getMinutes()).toBe(59);
    sub.unsubscribe();
  });

  it('should increment day and hour when minute rolls over at hour 23→0', () => {
    const control = new FormControl(new Date(2026, 3, 15, 23, 59, 0));
    const sub = setupCalendarDateHandling(control);

    control.setValue(new Date(2026, 3, 15, 0, 0, 0));

    const result = control.value as Date;
    expect(result.getDate()).toBe(16);
    expect(result.getHours()).toBe(1);
    sub.unsubscribe();
  });

  it('should decrement day and hour when minute rolls back at hour 0→23', () => {
    const control = new FormControl(new Date(2026, 3, 15, 0, 0, 0));
    const sub = setupCalendarDateHandling(control);

    control.setValue(new Date(2026, 3, 15, 23, 59, 0));

    const result = control.value as Date;
    expect(result.getDate()).toBe(14);
    expect(result.getHours()).toBe(22);
    sub.unsubscribe();
  });

  it('should increment day when hours roll from 23 to 0 (hour change only)', () => {
    const control = new FormControl(new Date(2026, 3, 15, 23, 30, 0));
    const sub = setupCalendarDateHandling(control);

    control.setValue(new Date(2026, 3, 15, 0, 30, 0));

    const result = control.value as Date;
    expect(result.getDate()).toBe(16);
    expect(result.getHours()).toBe(0);
    sub.unsubscribe();
  });

  it('should decrement day when hours roll from 0 to 23 (hour change only)', () => {
    const control = new FormControl(new Date(2026, 3, 15, 0, 30, 0));
    const sub = setupCalendarDateHandling(control);

    control.setValue(new Date(2026, 3, 15, 23, 30, 0));

    const result = control.value as Date;
    expect(result.getDate()).toBe(14);
    expect(result.getHours()).toBe(23);
    sub.unsubscribe();
  });

  it('should roll month forward at end of month', () => {
    // prev: April 30, 23:30 → curr: April 1, 0:30 (day shows last→1, hour 23→0)
    const control = new FormControl(new Date(2026, 3, 30, 23, 30, 0));
    const sub = setupCalendarDateHandling(control);

    control.setValue(new Date(2026, 3, 1, 0, 30, 0));

    const result = control.value as Date;
    expect(result.getMonth()).toBe(4); // May
    expect(result.getDate()).toBe(2);
    sub.unsubscribe();
  });

  it('should roll month backward at start of month', () => {
    // prev: April 1, 0:30 → curr: April 30, 23:30 (day shows 1→last, hour 0→23)
    const control = new FormControl(new Date(2026, 3, 1, 0, 30, 0));
    const sub = setupCalendarDateHandling(control);

    control.setValue(new Date(2026, 3, 30, 23, 30, 0));

    const result = control.value as Date;
    expect(result.getMonth()).toBe(2); // March
    expect(result.getDate()).toBe(29);
    sub.unsubscribe();
  });

  it('should roll year forward from Dec 31 to Jan 1', () => {
    // prev: Dec 31, 23:30 → curr: Jan 1, 0:30
    const control = new FormControl(new Date(2026, 11, 31, 23, 30, 0));
    const sub = setupCalendarDateHandling(control);

    control.setValue(new Date(2026, 0, 1, 0, 30, 0));

    const result = control.value as Date;
    expect(result.getFullYear()).toBe(2027);
    expect(result.getMonth()).toBe(1); // Feb (month incremented from Jan)
    expect(result.getDate()).toBe(2);
    sub.unsubscribe();
  });

  it('should roll year backward from Jan 1 to Dec 31', () => {
    // prev: Jan 1, 0:30 → curr: Dec 31, 23:30
    const control = new FormControl(new Date(2026, 0, 1, 0, 30, 0));
    const sub = setupCalendarDateHandling(control);

    control.setValue(new Date(2026, 11, 31, 23, 30, 0));

    const result = control.value as Date;
    expect(result.getFullYear()).toBe(2025);
    expect(result.getMonth()).toBe(10); // November (month decremented from Dec)
    expect(result.getDate()).toBe(30);
    sub.unsubscribe();
  });

  it('should not modify date for normal minute changes', () => {
    const control = new FormControl(new Date(2026, 3, 15, 10, 30, 0));
    const sub = setupCalendarDateHandling(control);

    control.setValue(new Date(2026, 3, 15, 10, 31, 0));

    const result = control.value as Date;
    expect(result.getHours()).toBe(10);
    expect(result.getMinutes()).toBe(31);
    expect(result.getDate()).toBe(15);
    sub.unsubscribe();
  });

  it('should handle null values gracefully', () => {
    const control = new FormControl<Date | null>(null);
    const sub = setupCalendarDateHandling(control);

    control.setValue(new Date(2026, 3, 15, 10, 0, 0));

    expect(control.value).toBeDefined();
    sub.unsubscribe();
  });
});
