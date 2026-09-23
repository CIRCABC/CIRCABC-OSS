import {
  ChangeDetectionStrategy,
  Component,
  HostListener,
  output,
} from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';

/**
 * Dropdown menu used in the group members management view.
 *
 * Renders an actions dropdown (via `members-dropdown.component.html`) whose
 * visibility is toggled by clicking the associated `dropdown-trigger` element.
 * The dropdown exposes entry points for launching the "add existing users"
 * wizard and the "create new user" wizard, emitting outputs the parent
 * component listens to in order to open the corresponding wizard.
 *
 * The component listens to document-level clicks so it can auto-close when the
 * user clicks anywhere outside the trigger.
 */
@Component({
  selector: 'cbc-members-dropdown',
  templateUrl: './members-dropdown.component.html',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [TranslocoModule],
})
export class MembersDropdownComponent {
  /**
   * Whether the actions dropdown panel is currently expanded/visible.
   * Toggled by {@link onClick} and reset when clicking outside the trigger.
   */
  public showActionsDropdown = false;

  /**
   * Emitted when the user selects the option to add existing members.
   * Always emits `true` to signal the parent to open the members wizard.
   */
  public readonly showWizard = output<boolean>();

  /**
   * Emitted when the user selects the option to create a new user.
   * Always emits `true` to signal the parent to open the user-creation wizard.
   */
  public readonly showUserCreateWizard = output<boolean>();

  /**
   * Document-level click handler that controls the dropdown visibility.
   *
   * Toggles {@link showActionsDropdown} when the `dropdown-trigger` element is
   * clicked, and closes the dropdown when the click occurs anywhere else on the
   * page.
   *
   * @param event The DOM click event captured on the document.
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

  /**
   * Handles selection of the "add existing members" action by emitting
   * {@link showWizard} with `true`.
   */
  public showWizardClick() {
    this.showWizard.emit(true);
  }

  /**
   * Handles selection of the "create new user" action by emitting
   * {@link showUserCreateWizard} with `true`.
   */
  public showUserCreateWizardClick() {
    this.showUserCreateWizard.emit(true);
  }
}
