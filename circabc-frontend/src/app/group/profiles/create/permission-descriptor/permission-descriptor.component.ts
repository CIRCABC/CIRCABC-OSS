import { Component, ElementRef, Input, viewChild } from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';

@Component({
  selector: 'cbc-permission-descriptor',
  templateUrl: './permission-descriptor.component.html',
  styleUrl: './permission-descriptor.component.scss',
  preserveWhitespaces: true,
  imports: [TranslocoModule],
})
export class PermissionDescriptorComponent {
  // TODO: Skipped for migration because:
  //  This input is used in a control flow expression (e.g. `@if` or `*ngIf`)
  //  and migrating would break narrowing currently.
  @Input()
  label!: string;
  readonly elementView = viewChild.required<ElementRef>('textExplanation');
  public mustExpand = false;

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
