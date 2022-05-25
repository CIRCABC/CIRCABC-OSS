import { Component, HostListener, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'cbc-members-dropdown',
  templateUrl: './members-dropdown.component.html',
  preserveWhitespaces: true,
})
export class MembersDropdownComponent {
  //   @Input() currentHeader!: Header;
  //   @Input() currentCategory!: Category;

  public showActionsDropdown = false;
  @Output() public readonly showWizard = new EventEmitter<boolean>();
  @Output() public readonly showUserCreateWizard = new EventEmitter<boolean>();

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

  public showWizardClick() {
    this.showWizard.emit(true);
  }

  public showUserCreateWizardClick() {
    this.showUserCreateWizard.emit(true);
  }
}
