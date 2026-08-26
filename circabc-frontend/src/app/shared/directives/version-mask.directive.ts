import { Directive, HostListener } from '@angular/core';
import { NgControl } from '@angular/forms';

@Directive({
  selector: '[cbcVersionMask]',
})
export class VersionMaskDirective {
  constructor(public ngControl: NgControl) {}

  @HostListener('ngModelChange', ['$event'])
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  onModelChange(event: any) {
    this.onInputChange(event, false);
  }

  @HostListener('keydown.backspace', ['$event'])
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  keydownBackspace(event: any) {
    this.onInputChange(event.target.value, true);
  }

  onInputChange(event: string, backspace: boolean) {
    let newVal = event.replace(/\D/g, '');
    if (backspace && newVal.length <= 3) {
      newVal = newVal.substring(0, newVal.length - 1);
    }
    if (newVal.length === 0) {
      newVal = '';
    } else if (newVal.length <= 1) {
      newVal = newVal.replace(/^(\d{0,1})/, '$1.');
    } else if (newVal.length <= 2) {
      newVal = newVal.replace(/^(\d{0,1})(\d{0,1})/, '$1.$2');
    } else {
      newVal = newVal.substring(0, 3);
      newVal = newVal.replace(/^(\d{0,1})(\d{0,1})(\d{0,1})/, '$1$2.$3');
    }
    if (this.ngControl.valueAccessor) {
      this.ngControl.valueAccessor.writeValue(newVal);
    }
  }
}
