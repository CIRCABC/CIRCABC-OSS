import {
  ChangeDetectionStrategy,
  Component,
  HostListener,
  input,
} from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { Header, Node as ModelNode } from 'app/core/generated/circabc';

/**
 * Renders the explorer context-menu dropdown (`cbc-explorer-dropdown`).
 *
 * The component displays an actions dropdown associated with the currently
 * browsed header and category. It shows action links (rendered through
 * `RouterLink` and localised via Transloco) that appear when the user clicks
 * the dropdown trigger element, and it automatically closes the menu when a
 * click occurs anywhere outside that trigger.
 */
@Component({
  selector: 'cbc-explorer-dropdown',
  templateUrl: './explorer-dropdown.component.html',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, TranslocoModule],
})
export class ExplorerDropdownComponent {
  /**
   * Signal input holding the header currently being explored, used to build
   * the header-scoped actions offered by the dropdown. Undefined when no
   * header context is available.
   */
  currentHeader = input<Header | undefined>();
  /**
   * Signal input holding the category node currently being explored, used to
   * build the category-scoped actions offered by the dropdown. Undefined when
   * no category context is available.
   */
  currentCategory = input<ModelNode | undefined>();

  /**
   * Whether the actions dropdown is currently visible. Toggled by
   * {@link onClick} in response to document click events.
   */
  public showActionsDropdown = false;

  /**
   * Document-level click handler that controls the visibility of the actions
   * dropdown.
   *
   * When the clicked element carries the `dropdown-trigger` CSS class the
   * dropdown visibility is toggled; any click elsewhere on the document closes
   * the dropdown.
   *
   * @param event The DOM click event dispatched at the document level. Its
   * `target` is inspected for the `dropdown-trigger` class.
   */
  @HostListener('document:click', ['$event'])

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  public onClick(event: any) {
    // only trigger display or hide when clicking the dropdown-trigger html element
    if (event.target.classList.contains('dropdown-trigger')) {
      this.showActionsDropdown = !this.showActionsDropdown;
    } else {
      // if the click is done outside then close the displayed html element
      this.showActionsDropdown = false;
    }
  }
}
