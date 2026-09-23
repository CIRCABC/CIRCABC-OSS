import { Directive, HostListener, inject } from '@angular/core';
import { NgControl } from '@angular/forms';

/**
 * Attribute directive that masks and formats user input as a version number.
 *
 * Applied via the `[cbcVersionMask]` selector on a form control (e.g. an
 * `<input>` bound with `ngModel`), it strips all non-digit characters and
 * reformats the remaining digits into a dotted version string:
 * - a single digit `d` becomes `d.`
 * - two digits `dd` become `d.d`
 * - three or more digits are truncated to three and rendered as `dd.d`
 *
 * The formatted value is written back to the host control through its
 * {@link NgControl} value accessor, so the visible input always reflects the
 * masked version format as the user types.
 *
 * @example
 * ```html
 * <input cbcVersionMask [(ngModel)]="version" />
 * ```
 */
@Directive({
  selector: '[cbcVersionMask]',
})
export class VersionMaskDirective {
  /**
   * Reference to the host element's {@link NgControl}, used to access the
   * value accessor so the reformatted version string can be written back to
   * the underlying form control.
   */
  ngControl = inject(NgControl);

  /**
   * Host listener for the `ngModelChange` event.
   *
   * Reformats the new model value as a version number (without backspace
   * handling) whenever the bound value changes.
   *
   * @param event - The new value emitted by `ngModelChange`.
   */
  @HostListener('ngModelChange', ['$event'])
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  onModelChange(event: any) {
    this.onInputChange(event, false);
  }

  /**
   * Host listener for the Backspace keydown event.
   *
   * Reformats the current input value while accounting for character deletion,
   * ensuring the mask behaves naturally when the user removes characters.
   *
   * @param event - The keyboard event; its `target.value` provides the current
   * input value.
   */
  @HostListener('keydown.backspace', ['$event'])
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  keydownBackspace(event: any) {
    this.onInputChange(event.target.value, true);
  }

  /**
   * Strips non-digit characters from the input, formats the remaining digits
   * into a dotted version string, and writes the result back to the host
   * form control via its value accessor.
   *
   * Formatting rules based on the number of digits:
   * - 0 digits: empty string
   * - 1 digit `d`: `d.`
   * - 2 digits `dd`: `d.d`
   * - 3+ digits: truncated to three digits and formatted as `dd.d`
   *
   * @param event - The raw input value to be masked and formatted.
   * @param backspace - Whether the change originated from a Backspace press;
   * when `true` and three or fewer digits remain, the last digit is dropped to
   * mirror the deletion.
   */
  onInputChange(event: string, backspace: boolean) {
    let newVal = event.replaceAll(/\D/g, '');
    if (backspace && newVal.length <= 3) {
      newVal = newVal.substring(0, newVal.length - 1);
    }
    if (newVal.length === 0) {
      newVal = '';
    } else if (newVal.length <= 1) {
      newVal = newVal.replace(/^(\d?)/, '$1.');
    } else if (newVal.length <= 2) {
      newVal = newVal.replace(/^(\d?)(\d?)/, '$1.$2');
    } else {
      newVal = newVal.substring(0, 3);
      newVal = newVal.replace(/^(\d?)(\d?)(\d?)/, '$1$2.$3');
    }
    if (this.ngControl.valueAccessor) {
      this.ngControl.valueAccessor.writeValue(newVal);
    }
  }
}
