/* eslint-disable @typescript-eslint/no-explicit-any */
import { Component, OnInit, forwardRef, input } from '@angular/core';
import {
  ControlValueAccessor,
  FormBuilder,
  FormGroup,
  NG_VALUE_ACCESSOR,
  ReactiveFormsModule,
} from '@angular/forms';

import { TranslocoModule } from '@jsverse/transloco';
import { mimetypes } from 'app/group/library/mimetypes/supported-mimetypes';

@Component({
  selector: 'cbc-mimetype-input',
  templateUrl: './mimetype-input.component.html',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      multi: true,
      // eslint-disable-next-line @angular-eslint/no-forward-ref
      useExisting: forwardRef(() => MimetypeInputComponent),
    },
  ],
  preserveWhitespaces: true,
  imports: [ReactiveFormsModule, TranslocoModule],
})
export class MimetypeInputComponent implements OnInit, ControlValueAccessor {
  readonly disabled = input(false);
  form!: FormGroup;

  // impement ControlValueAccessor interface
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  // eslint-disable-next-line no-empty,@typescript-eslint/no-empty-function
  onChange = (_: any) => {};
  // eslint-disable-next-line no-empty, @typescript-eslint/no-empty-function
  onTouched = () => {};

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  writeValue(value: any) {
    if (value) {
      this.form.patchValue({ mimetype: value });
    }
  }

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  registerOnChange(fn: (_: any) => void) {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => {}): void {
    this.onTouched = fn;
  }

  constructor(private fb: FormBuilder) {}

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

  getAvailableMimetypes() {
    return mimetypes;
  }
}
