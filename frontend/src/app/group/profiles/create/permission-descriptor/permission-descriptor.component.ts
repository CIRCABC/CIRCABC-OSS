import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  input,
  viewChild,
} from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';

/**
 * Displays a single permission label together with its (potentially long)
 * explanatory text.
 *
 * The component renders the translated permission `label` and its description
 * text. When the description text overflows its container, the component can
 * toggle an expanded state so the full text becomes visible; otherwise it stays
 * collapsed. It is used within the access profile creation UI to describe the
 * meaning of individual permissions.
 */
@Component({
  selector: 'cbc-permission-descriptor',
  templateUrl: './permission-descriptor.component.html',
  styleUrl: './permission-descriptor.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [TranslocoModule],
})
export class PermissionDescriptorComponent {
  /**
   * Required input holding the permission label/key to display and describe.
   */
  readonly label = input.required<string>();
  /**
   * Reference to the template element (marked `#textExplanation`) that holds the
   * explanation text. Used to measure whether the text overflows its container.
   */
  readonly elementView = viewChild.required<ElementRef>('textExplanation');
  /**
   * Whether the explanation text should be shown in its fully expanded form.
   * `true` when the text overflows and needs to be expanded, `false` otherwise.
   */
  public mustExpand = false;

  /**
   * Toggles the expanded state of the explanation text.
   *
   * Compares the visible width (`offsetWidth`) of the explanation element against
   * its full content width (`scrollWidth`). If the content is wider than the
   * visible area (i.e. it is truncated/overflowing), {@link mustExpand} is set to
   * `true` to reveal the full text; otherwise it is set to `false`.
   *
   * @returns Nothing; updates {@link mustExpand} as a side effect.
   */
  public toggleExpand() {
    if (
      this.elementView().nativeElement.offsetWidth <
      this.elementView().nativeElement.scrollWidth
    ) {
      this.mustExpand = true;
    } else {
      this.mustExpand = false;
    }
  }
}
