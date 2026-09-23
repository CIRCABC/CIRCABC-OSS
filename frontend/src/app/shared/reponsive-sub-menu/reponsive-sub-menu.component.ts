import { ChangeDetectionStrategy, Component } from '@angular/core';

import { TranslocoModule } from '@jsverse/transloco';

/**
 * Responsive sub-menu component.
 *
 * Renders a collapsible sub-menu (via its `reponsive-sub-menu.component.html`
 * template) whose visibility on small screens is driven by a CSS class toggle.
 * On narrow viewports the menu is collapsed by default and can be expanded by
 * the user; the component tracks this state through the `className` property
 * that the template binds to the sub-menu container.
 *
 * Uses OnPush change detection and preserves template whitespace.
 */
@Component({
  selector: 'cbc-reponsive-sub-menu',
  templateUrl: './reponsive-sub-menu.component.html',
  styleUrl: './reponsive-sub-menu.component.scss',
  imports: [TranslocoModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  preserveWhitespaces: true,
})
export class ReponsiveSubMenuComponent {
  /**
   * CSS class(es) applied to the sub-menu container in the template.
   *
   * Defaults to `'sub-menu'` (collapsed state). When the menu is expanded the
   * `'responsive'` modifier class is appended, yielding `'sub-menu responsive'`.
   */
  public className = 'sub-menu';

  /**
   * Toggles the sub-menu between its collapsed and expanded states.
   *
   * When currently collapsed (`className === 'sub-menu'`), the `'responsive'`
   * modifier is appended to expand the menu; otherwise the class is reset to
   * `'sub-menu'` to collapse it again.
   */
  public toggle() {
    if (this.className === 'sub-menu') {
      this.className += ' responsive';
    } else {
      this.className = 'sub-menu';
    }
  }
}
