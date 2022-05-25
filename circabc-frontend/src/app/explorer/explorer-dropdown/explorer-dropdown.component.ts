import { Component, Input, HostListener } from '@angular/core';
import { Node as ModelNode, Header } from 'app/core/generated/circabc';

@Component({
  selector: 'cbc-explorer-dropdown',
  templateUrl: './explorer-dropdown.component.html',
  preserveWhitespaces: true,
})
export class ExplorerDropdownComponent {
  @Input() currentHeader: Header | undefined;
  @Input() currentCategory!: ModelNode | undefined;

  public showActionsDropdown = false;

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
