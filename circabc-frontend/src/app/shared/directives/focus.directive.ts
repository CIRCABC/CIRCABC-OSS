import { AfterContentInit, Directive, ElementRef, input } from '@angular/core';

@Directive({
  selector: '[cbcFocus]',
})
export class FocusDirective implements AfterContentInit {
  readonly cbcFocus = input.required<boolean>();

  public constructor(private el: ElementRef) {}

  public ngAfterContentInit() {
    setTimeout(() => {
      this.el.nativeElement.focus();
    }, 500);
  }
}
