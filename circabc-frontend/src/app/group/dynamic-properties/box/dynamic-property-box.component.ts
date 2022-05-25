import { Component, EventEmitter, Input, Output } from '@angular/core';

import { DynamicPropertyDefinition } from 'app/core/generated/circabc';
import { TitleTag } from 'app/group/dynamic-properties/title/title';

@Component({
  selector: 'cbc-dynamic-property-box',
  templateUrl: './dynamic-property-box.component.html',
  styleUrls: ['./dynamic-property-box.component.scss'],
  preserveWhitespaces: true,
})
export class DynamicPropertyBoxComponent {
  @Input()
  property!: DynamicPropertyDefinition;
  @Output()
  readonly dynnamicPropDelete = new EventEmitter<DynamicPropertyDefinition>();
  @Output()
  readonly dynamicPropEdit = new EventEmitter<DynamicPropertyDefinition>();

  public getTitleAsArray(): TitleTag[] {
    const result: TitleTag[] = [];

    if (this.property) {
      for (const key of Object.keys(this.property.title)) {
        if (
          this.property.title &&
          this.property.title[key] !== undefined &&
          this.property.title[key] !== ''
        ) {
          const tag: TitleTag = { lang: key, value: this.property.title[key] };
          result.push(tag);
        }
      }
    }
    return result;
  }

  public bubbleDynpropDeleted() {
    this.dynnamicPropDelete.emit(this.property);
  }

  public bubbleDynpropEdit() {
    this.dynamicPropEdit.emit(this.property);
  }

  public isSelection(): boolean {
    return (
      this.property !== undefined &&
      this.property.propertyType !== undefined &&
      (this.property.propertyType === 'SELECTION' ||
        this.property.propertyType === 'MULTI_SELECTION')
    );
  }
}
