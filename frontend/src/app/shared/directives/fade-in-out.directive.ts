import { Directive, ElementRef, effect, inject, input } from '@angular/core';

/**
 * Attribute directive (`[cbcFadeInOut]`) that animates the visibility of its
 * host element by transitioning `opacity` and `height`.
 *
 * When applied to an element it configures CSS transitions and toggles the
 * element between a visible state (full opacity, natural height) and a hidden
 * state (zero opacity, zero height) based on the reactive {@link show} input.
 * The `overflow: hidden` style is set so the collapsing height clips the
 * content during the transition.
 */
@Directive({
  selector: '[cbcFadeInOut]',
})
export class FadeInOutDirective {
  /**
   * Reference to the host element the directive is attached to, used to read
   * and mutate its inline styles.
   */
  private readonly el = inject(ElementRef);

  /**
   * Required boolean input controlling visibility. When `true` the element
   * fades in and expands; when `false` it fades out and collapses to zero
   * height.
   */
  public show = input.required<boolean>();

  /**
   * Initializes the host element's CSS transition and overflow styles, and
   * registers a reactive {@link effect} that updates `opacity` and `height`
   * whenever the {@link show} input changes.
   */
  constructor() {
    const element = this.el.nativeElement as HTMLElement;
    element.style.transition = 'opacity 200ms ease-in, height 200ms ease-out';
    element.style.overflow = 'hidden';

    effect(() => {
      if (this.show()) {
        element.style.height = '';
        element.style.opacity = '1';
      } else {
        element.style.opacity = '0';
        element.style.height = '0';
      }
    });
  }
}
