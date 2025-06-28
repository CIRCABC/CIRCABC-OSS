import {
  Component,
  ElementRef,
  HostListener,
  Input,
  output,
  input,
} from '@angular/core';

@Component({
  selector: 'cbc-hint',
  templateUrl: './hint.component.html',
  styleUrl: './hint.component.scss',
  preserveWhitespaces: true,
})
export class HintComponent {
  // TODO: Skipped for migration because:
  //  This input is used in a control flow expression (e.g. `@if` or `*ngIf`)
  //  and migrating would break narrowing currently.
  @Input()
  public title!: string;
  public readonly text = input.required<string>();
  public readonly floatEnable = input(true);
  public readonly clickOutside = output<MouseEvent>();

  public shown = false;
  private elementRef: ElementRef;

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
      this.shown = false;
    }
  }
}
