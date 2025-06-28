/* eslint-disable @typescript-eslint/no-explicit-any */
import {
  Component,
  Input,
  OnInit,
  forwardRef,
  output,
  input,
} from '@angular/core';
import {
  ControlValueAccessor,
  NG_VALUE_ACCESSOR,
  ReactiveFormsModule,
} from '@angular/forms';

import {
  TimezoneEntry,
  availableTimezones,
  defaultTimezone,
} from 'app/group/agenda/timezones/supported-timezones';

@Component({
  selector: 'cbc-timezone-selector',
  templateUrl: './timezone-selector.component.html',
  styleUrl: './timezone-selector.component.scss',
  preserveWhitespaces: true,
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      multi: true,
      // eslint-disable-next-line @angular-eslint/no-forward-ref
      useExisting: forwardRef(() => TimezoneSelectorComponent),
    },
  ],
  imports: [ReactiveFormsModule],
})
export class TimezoneSelectorComponent implements OnInit, ControlValueAccessor {
  public availableTimezones!: TimezoneEntry[];
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  public selectedTimezone!: string;
  public readonly changedTimezone = output<string>();

  public readonly disable = input(false);

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
      this.selectedTimezone = defaultTimezone.value;
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
    this.availableTimezones = availableTimezones;
  }

  public onTimezoneChange(value: string) {
    if (this.onChange) {
      this.onChange(value);
    }
    this.changedTimezone.emit(value);
  }
}
