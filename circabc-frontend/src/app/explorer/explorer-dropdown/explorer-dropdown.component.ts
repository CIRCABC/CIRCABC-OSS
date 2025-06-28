import { Component, HostListener, Input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { Header, Node as ModelNode } from 'app/core/generated/circabc';

@Component({
  selector: 'cbc-explorer-dropdown',
  templateUrl: './explorer-dropdown.component.html',
  preserveWhitespaces: true,
  imports: [RouterLink, TranslocoModule],
})
export class ExplorerDropdownComponent {
  // TODO: Skipped for migration because:
  //  This input is used in a control flow expression (e.g. `@if` or `*ngIf`)
  //  and migrating would break narrowing currently.
  @Input() currentHeader: Header | undefined;
  // TODO: Skipped for migration because:
  //  This input is used in a control flow expression (e.g. `@if` or `*ngIf`)
  //  and migrating would break narrowing currently.
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
