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
import { mimetypes } from 'app/group/library/mimetypes/supported-mimetypes';

/**
 * Form control component that renders a MIME type selector (typically a
 * dropdown/`<select>`) populated from the application's list of supported
 * MIME types.
 *
 * It implements {@link ControlValueAccessor} so it can be used seamlessly
 * inside Angular reactive or template-driven forms (e.g. bound with
 * `formControlName` or `ngModel`). Internally it wraps a single-field
 * {@link FormGroup} whose `mimetype` control holds the currently selected
 * value, and propagates changes back to the parent form via the registered
 * `onChange` callback.
 *
 * @see mimetypes for the source list of selectable MIME types.
 */
@Component({
  selector: 'cbc-mimetype-input',
  templateUrl: './mimetype-input.component.html',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      multi: true,

      useExisting: forwardRef(() => MimetypeInputComponent),
    },
  ],
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule, TranslocoModule],
})
export class MimetypeInputComponent implements OnInit, ControlValueAccessor {
  /** Angular `FormBuilder` used to construct the internal reactive form. */
  private readonly fb = inject(FormBuilder);

  /**
   * Signal input controlling whether the MIME type control is disabled.
   * When `true`, the internal `mimetype` control is disabled during
   * initialization. Defaults to `false`.
   */
  readonly disabled = input(false);

  /**
   * Internal reactive form backing the component. Holds a single `mimetype`
   * control representing the selected MIME type. Initialized in
   * {@link ngOnInit}.
   */
  form!: FormGroup;

  // impement ControlValueAccessor interface

  /**
   * Callback registered by Angular's forms API and invoked whenever the
   * selected value changes so the parent form model stays in sync.
   * Replaced by {@link registerOnChange}; defaults to a no-op.
   */
  onChange = (_: any) => {};

  /**
   * Callback registered by Angular's forms API and invoked when the control
   * is touched. Replaced by {@link registerOnTouched}; defaults to a no-op.
   */
  onTouched = () => {};

  /**
   * Writes a new value into the control from the parent form model
   * ({@link ControlValueAccessor} implementation).
   *
   * @param value The MIME type value to set. Falsy values are ignored and
   * leave the current selection unchanged.
   */
  writeValue(value: any) {
    if (value) {
      this.form.patchValue({ mimetype: value });
    }
  }

  /**
   * Registers the callback to be invoked when the control's value changes
   * ({@link ControlValueAccessor} implementation).
   *
   * @param fn The change-notification callback provided by Angular's forms API.
   */
  registerOnChange(fn: (_: any) => void) {
    this.onChange = fn;
  }

  /**
   * Registers the callback to be invoked when the control is touched
   * ({@link ControlValueAccessor} implementation).
   *
   * @param fn The touch-notification callback provided by Angular's forms API.
   */
  registerOnTouched(fn: () => {}): void {
    this.onTouched = fn;
  }

  /**
   * Angular lifecycle hook. Builds the internal {@link form} with an empty
   * `mimetype` control, subscribes to its value changes to propagate them via
   * {@link onChange}, and disables the control when {@link disabled} is `true`.
   */
  ngOnInit() {
    this.form = this.fb.group(
      {
        mimetype: '',
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
      this.form.controls.mimetype.disable();
    }
  }

  /**
   * Returns the list of supported MIME types available for selection.
   *
   * @returns The array of supported MIME type entries used to populate the selector.
   */
  getAvailableMimetypes() {
    return mimetypes;
  }
}
