import {
  AfterContentInit,
  Directive,
  ElementRef,
  inject,
  input,
} from '@angular/core';

/**
 * Attribute directive that programmatically moves keyboard focus to its host
 * element shortly after the host's content has been initialised.
 *
 * Applied via the `[cbcFocus]` attribute selector, it is typically used to
 * auto-focus an input or interactive element when a view (for example a modal
 * or form) is rendered. Focus is applied with a short delay so that animations
 * and structural changes settle before the element receives focus.
 */
@Directive({
  selector: '[cbcFocus]',
})
export class FocusDirective implements AfterContentInit {
  /**
   * Reference to the host element, used to invoke `focus()` on the underlying
   * native DOM node.
   */
  private readonly el = inject(ElementRef);

  /**
   * Required boolean input bound through the `cbcFocus` attribute.
   *
   * Carries the directive's activation flag; its presence enables the
   * auto-focus behaviour on the host element.
   */
  readonly cbcFocus = input.required<boolean>();

  /**
   * Angular lifecycle hook invoked once the directive's content has been fully
   * initialised.
   *
   * Schedules a focus call on the host's native element after a 500 ms delay,
   * allowing surrounding view transitions to complete before focus is applied.
   */
  public ngAfterContentInit() {
    setTimeout(() => {
      this.el.nativeElement.focus();
    }, 500);
  }
}
