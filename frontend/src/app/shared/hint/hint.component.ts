import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  HostListener,
  inject,
  input,
  output,
} from '@angular/core';

/**
 * Standalone component (`cbc-hint`) that renders a small hint / tooltip-style
 * element used to display contextual help information to the user.
 *
 * It shows an optional title together with a required text body, and can
 * optionally float above surrounding content. The component tracks its own
 * open/closed state and automatically closes and notifies listeners when the
 * user clicks anywhere outside of its host element.
 */
@Component({
  selector: 'cbc-hint',
  templateUrl: './hint.component.html',
  styleUrl: './hint.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  preserveWhitespaces: true,
})
export class HintComponent {
  /**
   * Optional heading displayed at the top of the hint. When omitted, no title
   * is rendered.
   */
  public readonly title = input<string>();
  /**
   * Required body text of the hint that describes the contextual help message.
   */
  public readonly text = input.required<string>();
  /**
   * Whether the hint should float above surrounding content. Defaults to
   * `true`.
   */
  public readonly floatEnable = input(true);
  /**
   * Emitted when a click is detected outside of the component's host element,
   * carrying the originating {@link MouseEvent}. Consumers can use this to
   * react to the hint being dismissed.
   */
  public readonly clickOutside = output<MouseEvent>();

  /**
   * Reflects whether the hint is currently displayed. Set to `false` when an
   * outside click closes the hint.
   */
  public shown = false;
  /** Reference to the component's own host element, used for hit-testing clicks. */
  private readonly elementRef: ElementRef;

  /**
   * Creates the component and injects the host {@link ElementRef} that is used
   * to determine whether subsequent document clicks occur inside or outside of
   * this component.
   */
  public constructor() {
    const myElement = inject(ElementRef);

    this.elementRef = myElement;
  }

  /**
   * Document-level click handler that detects clicks occurring outside of this
   * component. When such an outside click happens, it emits the
   * {@link clickOutside} event and hides the hint by setting {@link shown} to
   * `false`.
   *
   * @param event The originating mouse event from the document click.
   * @param targetElement The element that was clicked, or `null` if unavailable.
   * @returns Nothing; exits early when `targetElement` is `null`.
   */
  @HostListener('document:click', ['$event', '$event.target'])
  public onClick(event: MouseEvent, targetElement: EventTarget | null): void {
    if (!targetElement) {
      return;
    }

    const clickedInside = this.elementRef.nativeElement.contains(targetElement);
    if (!clickedInside) {
      this.clickOutside.emit(event);
      this.shown = false;
    }
  }
}
