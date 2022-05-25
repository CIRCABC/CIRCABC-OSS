import { Component, ElementRef, Input, ViewChild } from '@angular/core';

@Component({
  selector: 'cbc-permission-descriptor',
  templateUrl: './permission-descriptor.component.html',
  styleUrls: ['./permission-descriptor.component.scss'],
  preserveWhitespaces: true,
})
export class PermissionDescriptorComponent {
  @Input()
  label!: string;
  @ViewChild('textExplanation')
  elementView!: ElementRef;
  public mustExpand = false;

  public toggleExpand() {
    if (
      this.elementView.nativeElement.offsetWidth <
      this.elementView.nativeElement.scrollWidth
    ) {
      this.mustExpand = true;
    } else {
      this.mustExpand = false;
    }
  }
}
