import {
  Component,
  ElementRef,
  EventEmitter,
  HostListener,
  Input,
  Output,
} from '@angular/core';

@Component({
  selector: 'cbc-create-dropdown',
  templateUrl: './dropdown.component.html',
  styleUrls: ['./dropdown.component.scss'],
  preserveWhitespaces: true,
})
export class DropdownComponent {
  @Input()
  public enableCreateEvent = true;
  @Input()
  public enableCreateMeeting = true;
  @Input()
  public currentDate!: Date;
  @Input()
  public igId!: string;
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

  // tslint:disable-next-line:no-any
  public toggleAddDropdown(event: any) {
    // only trigger display or hide when clicking the dropdown-trigger html element
    if (event.target.classList.contains('dropdown-trigger')) {
      this.showCreateDropdown = !this.showCreateDropdown;
    }
  }

  public launchCreateEventWizard(): void {
    this.showCreateDropdown = false;
    this.launchCreate.emit(true);
  }

  public launchCreateMeetingWizard(): void {
    this.showCreateDropdown = false;
    this.launchCreate.emit(false);
  }
}
