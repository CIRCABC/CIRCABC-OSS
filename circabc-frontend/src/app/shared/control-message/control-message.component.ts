import { Component, Input } from '@angular/core';
import { AbstractControl } from '@angular/forms';

import { ValidationService } from 'app/core/validation.service';

@Component({
  selector: 'cbc-control-message',
  templateUrl: './control-message.component.html',
  styleUrls: ['./control-message.component.scss'],
  preserveWhitespaces: true,
})
export class ControlMessageComponent {
  @Input()
  control!: AbstractControl;
  @Input()
  showInvalid = false;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  public error!: {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    [key: string]: any;
  };

  public getErrorsKeys() {
    const result = [];

    if (
      this.control !== undefined &&
      this.control !== null &&
      this.control.errors
    ) {
      if (
        this.control.dirty ||
        this.control.touched ||
        (this.showInvalid && !this.control.valid)
      ) {
        this.error = this.control.errors;
        for (const key of Object.keys(this.control.errors)) {
          if (this.error[key]) {
            result.push({
              key: ValidationService.getErrorMessageTranslationCode(key),
              value: this.error[key],
            });
          }
        }
      }
    }
    return result;
  }
}
