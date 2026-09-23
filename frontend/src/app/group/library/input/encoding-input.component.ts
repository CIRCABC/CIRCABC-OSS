/* eslint-disable @typescript-eslint/no-explicit-any */
import {
  ChangeDetectionStrategy,
  Component,
  forwardRef,
  inject,
  input,
  OnInit,
} from '@angular/core';
import {
  ControlValueAccessor,
  FormBuilder,
  FormGroup,
  NG_VALUE_ACCESSOR,
  ReactiveFormsModule,
} from '@angular/forms';

import { TranslocoModule } from '@jsverse/transloco';
import { availableEncodings } from 'app/group/library/encodings/encodings';

/**
 * Form control component that lets the user pick a character encoding
 * (e.g. UTF-8, ISO-8859-1) from the list of {@link availableEncodings}.
 *
 * It renders a reactive form containing a single `encoding` select control
 * and implements {@link ControlValueAccessor}, so it can be used directly
 * with `ngModel` or `formControlName` inside a parent form. The selected
 * value is propagated to the parent through the registered `onChange`
 * callback whenever the internal form changes.
 */
@Component({
  selector: 'cbc-encoding-input',
  templateUrl: './encoding-input.component.html',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      multi: true,

      useExisting: forwardRef(() => EncodingInputComponent),
    },
  ],
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule, TranslocoModule],
})
export class EncodingInputComponent implements OnInit, ControlValueAccessor {
  /** Angular reactive forms builder used to create the internal form group. */
  private readonly fb = inject(FormBuilder);

  /**
   * Input that, when set to `true`, disables the encoding select control so
   * the user cannot change the value.
   */
  readonly disabled = input(false);
  /** Reactive form holding the single `encoding` control. Initialised in {@link ngOnInit}. */
  form!: FormGroup;

  // impement ControlValueAccessor interface

  /**
   * Callback registered by the Angular forms API and invoked whenever the
   * selected encoding changes, propagating the value to the parent control.
   */
  onChange = (_: any) => {};

  /** Callback registered by the Angular forms API to mark the control as touched. */
  onTouched = () => {};

  /**
   * Writes a new encoding value into the internal form from the parent
   * control (part of {@link ControlValueAccessor}).
   *
   * @param value The encoding value to set; ignored when falsy.
   */
  writeValue(value: any) {
    if (value) {
      this.form.patchValue({ encoding: value });
    }
  }

  /**
   * Registers the change callback provided by the Angular forms API
   * (part of {@link ControlValueAccessor}).
   *
   * @param fn Callback invoked with the current value when it changes.
   */
  registerOnChange(fn: (_: any) => void) {
    this.onChange = fn;
  }

  /**
   * Registers the touched callback provided by the Angular forms API
   * (part of {@link ControlValueAccessor}).
   *
   * @param fn Callback invoked when the control is touched.
   */
  registerOnTouched(fn: () => {}): void {
    this.onTouched = fn;
  }

  /**
   * Lifecycle hook that builds the reactive form, subscribes to its value
   * changes to propagate them through {@link onChange}, and disables the
   * `encoding` control when the {@link disabled} input is `true`.
   */
  ngOnInit() {
    this.form = this.fb.group(
      {
        encoding: '',
      },
      {
        updateOn: 'change',
      }
    );

    this.form.valueChanges.subscribe((value) => {
      if (this.onChange) {
        this.onChange(value);
      }
    });

    if (this.disabled()) {
      this.form.controls.encoding.disable();
    }
  }

  /**
   * Returns the list of encodings offered by the select control.
   *
   * @returns The array of {@link availableEncodings}.
   */
  getAvailableEncodings() {
    return availableEncodings;
  }
}
