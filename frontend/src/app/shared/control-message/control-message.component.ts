import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { AbstractControl } from '@angular/forms';

import { TranslocoModule } from '@jsverse/transloco';
import { getErrorMessageTranslationCode } from 'app/core/validation.service';

/**
 * Form control validation message component.
 *
 * Renders the list of translated validation error messages for a given
 * Angular reactive form control. Each error key reported by the control is
 * mapped to a Transloco translation code via
 * {@link getErrorMessageTranslationCode} and displayed together with its
 * associated error value, so the template can interpolate error details
 * (e.g. required length, min/max bounds).
 *
 * Messages are only surfaced once the control has been interacted with
 * (`dirty` or `touched`) or when {@link ControlMessageComponent.showInvalid}
 * is enabled and the control is invalid.
 *
 * @example
 * ```html
 * <cbc-control-message [control]="form.controls.email" [showInvalid]="submitted" />
 * ```
 */
@Component({
  selector: 'cbc-control-message',
  templateUrl: './control-message.component.html',
  styleUrl: './control-message.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [TranslocoModule],
})
export class ControlMessageComponent {
  /**
   * Required input: the reactive form control whose validation errors are
   * inspected and displayed. Its `errors`, `dirty`, `touched` and `valid`
   * state determine which messages are rendered.
   */
  readonly control = input.required<AbstractControl>();

  /**
   * Optional input: when `true`, error messages are shown for an invalid
   * control even if the user has not yet interacted with it (i.e. not
   * `dirty` or `touched`). Typically enabled on form submission.
   * Defaults to `false`.
   */
  readonly showInvalid = input(false);

  /**
   * Snapshot of the control's current validation errors, keyed by error name.
   * Populated by {@link ControlMessageComponent.getErrorsKeys} and used by the
   * template to access individual error values.
   */
  public error!: {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    [key: string]: any;
  };

  /**
   * Builds the list of displayable validation errors for the bound control.
   *
   * If the control has errors and is either `dirty`, `touched`, or
   * (when {@link ControlMessageComponent.showInvalid} is `true`) invalid, each
   * error key is translated to a message code and paired with its error value.
   *
   * @returns An array of `{ key, value }` entries where `key` is the Transloco
   * translation code for the error and `value` is the raw error payload. Returns
   * an empty array when there are no errors to display.
   */
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
