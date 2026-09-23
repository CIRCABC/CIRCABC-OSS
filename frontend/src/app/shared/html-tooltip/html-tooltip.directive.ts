import { ConnectedPosition, Overlay, OverlayRef } from '@angular/cdk/overlay';
import { ComponentPortal } from '@angular/cdk/portal';
import {
  ChangeDetectionStrategy,
  Component,
  Directive,
  ElementRef,
  HostListener,
  inject,
  input,
  OnDestroy,
  ViewContainerRef,
} from '@angular/core';

/**
 * Internal component rendered inside the CDK overlay.
 * Displays sanitized HTML content.
 */
@Component({
  selector: 'cbc-html-tooltip-overlay',
  template: `<div class="cbc-html-tooltip" [innerHTML]="content"></div>`,
  styles: `
    .cbc-html-tooltip {
      background: rgba(50, 50, 50, 0.95);
      color: #fff;
      padding: 8px 12px;
      border-radius: 4px;
      font-size: 12px;
      line-height: 1.4;
      max-width: 300px;
      word-wrap: break-word;

      ul {
        list-style: disc;
        margin: 4px 0;
        padding-left: 16px;
      }

      ol {
        list-style: decimal;
        margin: 4px 0;
        padding-left: 16px;
      }
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HtmlTooltipOverlayComponent {
  /**
   * Sanitized HTML markup bound to the overlay's `innerHTML`.
   * Set by {@link HtmlTooltipDirective} when the tooltip is shown.
   */
  content = '';
}

/**
 * Directive that displays an HTML tooltip using CDK Overlay.
 *
 * Usage:
 *   <a [cbcHtmlTooltip]="item.description | cbcI18n"
 *      cbcHtmlTooltipPosition="above"
 *      [cbcHtmlTooltipShowDelay]="1200"
 *      [cbcHtmlTooltipDisabled]="!item.description">
 */
@Directive({
  selector: '[cbcHtmlTooltip]',
})
export class HtmlTooltipDirective implements OnDestroy {
  /** CDK Overlay service used to create and position the tooltip overlay. */
  private readonly overlay = inject(Overlay);
  /** Reference to the host element the tooltip is anchored to. */
  private readonly elementRef = inject(ElementRef);
  /** Container used to attach the overlay's component portal into the view. */
  private readonly viewContainerRef = inject(ViewContainerRef);

  /** The HTML content to display in the tooltip. */
  readonly cbcHtmlTooltip = input.required<string>();

  /** Tooltip position: 'above' | 'below' | 'left' | 'right'. Default: 'above'. */
  readonly cbcHtmlTooltipPosition = input<'above' | 'below' | 'left' | 'right'>(
    'above'
  );

  /** Delay in milliseconds before showing the tooltip. */
  readonly cbcHtmlTooltipShowDelay = input(0);

  /** Whether the tooltip is disabled. */
  readonly cbcHtmlTooltipDisabled = input(false);

  /** Active overlay reference while the tooltip is visible, or `null` when hidden. */
  private overlayRef: OverlayRef | null = null;
  /** Handle for the pending show-delay timer, or `null` when no timer is scheduled. */
  private showTimeout: ReturnType<typeof setTimeout> | null = null;

  /**
   * Shows the tooltip when the host element is hovered or receives focus.
   *
   * Does nothing when the tooltip is disabled or has no content. When a
   * positive show delay is configured, display is deferred via a timer;
   * otherwise the tooltip is shown immediately.
   */
  @HostListener('mouseenter')
  @HostListener('focusin')
  onShow(): void {
    if (this.cbcHtmlTooltipDisabled() || !this.cbcHtmlTooltip()) {
      return;
    }

    const delay = this.cbcHtmlTooltipShowDelay();
    if (delay > 0) {
      this.showTimeout = setTimeout(() => this.show(), delay);
    } else {
      this.show();
    }
  }

  /**
   * Hides the tooltip when the pointer leaves the host element or focus is lost.
   */
  @HostListener('mouseleave')
  @HostListener('focusout')
  onHide(): void {
    this.hide();
  }

  /**
   * Angular lifecycle hook. Ensures the overlay and any pending timer are
   * cleaned up when the directive is destroyed.
   */
  ngOnDestroy(): void {
    this.hide();
  }

  /**
   * Creates the CDK overlay (if not already open), attaches the
   * {@link HtmlTooltipOverlayComponent} portal and passes the tooltip's HTML
   * content to it. No-op when an overlay is already displayed.
   */
  private show(): void {
    if (this.overlayRef) {
      return;
    }

    const positionStrategy = this.overlay
      .position()
      .flexibleConnectedTo(this.elementRef)
      .withPositions(this.getPositions());

    this.overlayRef = this.overlay.create({
      positionStrategy,
      scrollStrategy: this.overlay.scrollStrategies.close(),
      panelClass: 'cbc-html-tooltip-panel',
    });

    const portal = new ComponentPortal(
      HtmlTooltipOverlayComponent,
      this.viewContainerRef
    );
    const componentRef = this.overlayRef.attach(portal);
    componentRef.instance.content = this.cbcHtmlTooltip();
  }

  /**
   * Cancels any pending show timer and disposes the active overlay, returning
   * the directive to its hidden state. Safe to call when already hidden.
   */
  private hide(): void {
    if (this.showTimeout) {
      clearTimeout(this.showTimeout);
      this.showTimeout = null;
    }
    if (this.overlayRef) {
      this.overlayRef.dispose();
      this.overlayRef = null;
    }
  }

  /**
   * Builds the ordered list of CDK connected positions for the tooltip based on
   * the configured {@link cbcHtmlTooltipPosition}. The preferred position is
   * listed first, followed by fallbacks so the overlay stays on-screen.
   *
   * @returns The candidate connected positions in priority order.
   */
  private getPositions(): ConnectedPosition[] {
    const position = this.cbcHtmlTooltipPosition();

    const above: ConnectedPosition = {
      originX: 'center',
      originY: 'top',
      overlayX: 'center',
      overlayY: 'bottom',
      offsetY: -8,
    };
    const below: ConnectedPosition = {
      originX: 'center',
      originY: 'bottom',
      overlayX: 'center',
      overlayY: 'top',
      offsetY: 8,
    };
    const left: ConnectedPosition = {
      originX: 'start',
      originY: 'center',
      overlayX: 'end',
      overlayY: 'center',
      offsetX: -8,
    };
    const right: ConnectedPosition = {
      originX: 'end',
      originY: 'center',
      overlayX: 'start',
      overlayY: 'center',
      offsetX: 8,
    };

    switch (position) {
      case 'below':
        return [below, above, left, right];
      case 'left':
        return [left, right, above, below];
      case 'right':
        return [right, left, above, below];
      default:
        return [above, below, left, right];
    }
  }
}
