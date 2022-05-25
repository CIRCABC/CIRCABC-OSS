/* eslint-disable @typescript-eslint/no-explicit-any */
import {
  Component,
  EventEmitter,
  forwardRef,
  Input,
  OnInit,
  Output,
} from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

import {
  SupportedTimezones,
  TimezoneEntry,
} from 'app/group/agenda/timezones/supported-timezones';

@Component({
  selector: 'cbc-timezone-selector',
  templateUrl: './timezone-selector.component.html',
  styleUrls: ['./timezone-selector.component.scss'],
  preserveWhitespaces: true,
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      multi: true,
      // eslint-disable-next-line @angular-eslint/no-forward-ref
      useExisting: forwardRef(() => TimezoneSelectorComponent),
    },
  ],
})
export class TimezoneSelectorComponent implements OnInit, ControlValueAccessor {
  public availableTimezones!: TimezoneEntry[];
  @Input()
  public selectedTimezone!: string;
  @Output()
  public readonly changedTimezone: EventEmitter<string> = new EventEmitter();

  @Input()
  public disable = false;

  // implement ControlValueAccessor interface instance fields
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  // eslint-disable-next-line no-empty,@typescript-eslint/no-empty-function
  onChange = (_: any) => {};
  // eslint-disable-next-line no-empty, @typescript-eslint/no-empty-function
  onTouched = () => {};

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  writeValue(value: any) {
    if (value) {
      this.selectedTimezone = value;
    } else {
      this.selectedTimezone = SupportedTimezones.defaultTimezone.value;
    }
  }

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  registerOnChange(fn: (_: any) => void) {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => {}): void {
    this.onTouched = fn;
  }

  public ngOnInit(): void {
    this.availableTimezones = SupportedTimezones.availableTimezones;
  }

  public onTimezoneChange(value: string) {
    if (this.onChange) {
      this.onChange(value);
    }
    this.changedTimezone.emit(value);
  }
}
