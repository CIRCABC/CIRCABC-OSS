import {
  Component,
  ElementRef,
  EventEmitter,
  HostListener,
  Input,
  Output,
} from '@angular/core';

@Component({
  selector: 'cbc-add-permission-dropdown',
  templateUrl: './dropdown.component.html',
  styleUrls: ['./dropdown.component.scss'],
  preserveWhitespaces: true,
})
export class DropdownComponent {
  @Input()
  public disabled = false;
  @Output()
  public readonly launchCreate = new EventEmitter<boolean>();
  @Output()
  public readonly clickOutside = new EventEmitter<MouseEvent>();

  private elementRef: ElementRef;

  public showCreateDropdown = false;

  public constructor(myElement: ElementRef) {
    this.elementRef = myElement;
  }

  @HostListener('document:click', ['$event', '$event.target'])
  public onClick(event: MouseEvent, targetElement: HTMLElement): void {
    if (!targetElement) {
      return;
    }

    const clickedInside = this.elementRef.nativeElement.contains(targetElement);
    if (!clickedInside) {
      this.clickOutside.emit(event);
      this.showCreateDropdown = false;
    }
  }

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  public toggleAddDropdown(event: any) {
    // only trigger display or hide when clicking the dropdown-trigger html element
    if (!this.disabled && event.target.classList.contains('dropdown-trigger')) {
      this.showCreateDropdown = !this.showCreateDropdown;
    }
  }

  public launchAddPermissionWizard(): void {
    this.showCreateDropdown = false;
    this.launchCreate.emit(false);
  }

  public launchAddShareWizard(): void {
    this.showCreateDropdown = false;
    this.launchCreate.emit(true);
  }
}
