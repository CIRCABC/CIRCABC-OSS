/* eslint-disable @typescript-eslint/no-explicit-any */
import {
  ChangeDetectionStrategy,
  Component,
  forwardRef,
  input,
  model,
  OnInit,
  output,
} from '@angular/core';
import {
  ControlValueAccessor,
  NG_VALUE_ACCESSOR,
  ReactiveFormsModule,
} from '@angular/forms';
import { TranslocoModule } from '@jsverse/transloco';

import {
  availableTimezones,
  defaultTimezone,
  TimezoneEntry,
} from 'app/group/agenda/timezones/supported-timezones';

/**
 * Form control component that renders a dropdown for selecting an agenda
 * timezone.
 *
 * The component displays the list of supported timezones (see
 * {@link availableTimezones}) and lets the user pick one. It implements
 * {@link ControlValueAccessor} so it can be used seamlessly inside Angular
 * reactive/template-driven forms (it registers itself as an
 * `NG_VALUE_ACCESSOR` provider). When no value is supplied it falls back to
 * the application {@link defaultTimezone}.
 *
 * Selector: `cbc-timezone-selector`.
 */
@Component({
  selector: 'cbc-timezone-selector',
  templateUrl: './timezone-selector.component.html',
  styleUrl: './timezone-selector.component.scss',
  preserveWhitespaces: true,
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      multi: true,

      useExisting: forwardRef(() => TimezoneSelectorComponent),
    },
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule, TranslocoModule],
})
export class TimezoneSelectorComponent implements OnInit, ControlValueAccessor {
  /**
   * List of timezone entries rendered as options in the selector.
   * Populated from {@link availableTimezones} during {@link ngOnInit}.
   */
  public availableTimezones!: TimezoneEntry[];

  /**
   * Two-way bindable model holding the currently selected timezone value.
   * Kept in sync with the form value through {@link writeValue}.
   */
  public selectedTimezone = model<string>();

  /**
   * Output emitted whenever the user changes the selected timezone.
   * Emits the newly selected timezone value.
   */
  public readonly changedTimezone = output<string>();

  /**
   * Input controlling whether the selector is disabled.
   * Defaults to `false` (enabled).
   */
  public readonly disable = input(false);

  // implement ControlValueAccessor interface instance fields

  /**
   * Callback registered by the forms API to notify Angular of value changes.
   * Replaced by {@link registerOnChange}; no-op by default.
   */
  onChange = (_: any) => {};

  /**
   * Callback registered by the forms API to mark the control as touched.
   * Replaced by {@link registerOnTouched}; no-op by default.
   */
  onTouched = () => {};

  /**
   * Writes a new value into the control (part of {@link ControlValueAccessor}).
   *
   * Sets {@link selectedTimezone} to the provided value, or falls back to
   * {@link defaultTimezone} when no value is given.
   *
   * @param value The timezone value to set, or a falsy value to use the default.
   */
  writeValue(value: any) {
    if (value) {
      this.selectedTimezone.set(value);
    } else {
      this.selectedTimezone.set(defaultTimezone.value);
    }
  }

  /**
   * Registers the change callback (part of {@link ControlValueAccessor}).
   *
   * @param fn Callback invoked with the new value when the selection changes.
   */
  registerOnChange(fn: (_: any) => void) {
    this.onChange = fn;
  }

  /**
   * Registers the touched callback (part of {@link ControlValueAccessor}).
   *
   * @param fn Callback invoked when the control is touched.
   */
  registerOnTouched(fn: () => {}): void {
    this.onTouched = fn;
  }

  /**
   * Angular lifecycle hook. Initializes {@link availableTimezones} with the
   * supported timezone list once the component is set up.
   */
  public ngOnInit(): void {
    this.availableTimezones = availableTimezones;
  }

  /**
   * Handles a timezone selection change from the template.
   *
   * Notifies the forms API through {@link onChange} and emits the value on
   * the {@link changedTimezone} output.
   *
   * @param value The newly selected timezone value.
   */
  public onTimezoneChange(value: string) {
    if (this.onChange) {
      this.onChange(value);
    }
    this.changedTimezone.emit(value);
  }
}
