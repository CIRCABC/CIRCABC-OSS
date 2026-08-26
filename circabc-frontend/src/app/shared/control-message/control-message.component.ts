import { Component, input } from '@angular/core';
import { AbstractControl } from '@angular/forms';

import { TranslocoModule } from '@jsverse/transloco';
import { getErrorMessageTranslationCode } from 'app/core/validation.service';

@Component({
  selector: 'cbc-control-message',
  templateUrl: './control-message.component.html',
  styleUrl: './control-message.component.scss',
  preserveWhitespaces: true,
  imports: [TranslocoModule],
})
export class ControlMessageComponent {
  readonly control = input.required<AbstractControl>();
  readonly showInvalid = input(false);
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  public error!: {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    [key: string]: any;
  };

  public getErrorsKeys() {
    const result = [];

    const control = this.control();
    if (control?.errors) {
      if (
        control.dirty ||
        control.touched ||
        (this.showInvalid() && !control.valid)
      ) {
        this.error = control.errors;
        for (const key of Object.keys(control.errors)) {
          if (this.error[key]) {
            result.push({
              key: getErrorMessageTranslationCode(key),
              value: this.error[key],
            });
          }
        }
      }
    }
    return result;
  }
}
