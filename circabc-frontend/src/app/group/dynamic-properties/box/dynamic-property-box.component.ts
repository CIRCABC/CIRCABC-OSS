import { Component, output, input, computed } from '@angular/core';

import { TranslocoModule } from '@jsverse/transloco';
import { DynamicPropertyDefinition } from 'app/core/generated/circabc';
import { TitleTag } from 'app/group/dynamic-properties/title/title';
import { TitleTagComponent } from 'app/group/dynamic-properties/title/title-tag.component';

@Component({
  selector: 'cbc-dynamic-property-box',
  templateUrl: './dynamic-property-box.component.html',
  styleUrl: './dynamic-property-box.component.scss',
  preserveWhitespaces: true,
  imports: [TitleTagComponent, TranslocoModule],
})
export class DynamicPropertyBoxComponent {
  readonly property = input.required<DynamicPropertyDefinition>();
  readonly dynnamicPropDelete = output<DynamicPropertyDefinition>();
  readonly dynamicPropEdit = output<DynamicPropertyDefinition>();

  // Use computed signal to memoize the title array
  readonly titleAsArray = computed(() => {
    const result: TitleTag[] = [];
    const property = this.property();

    if (property) {
      for (const key of Object.keys(property.title)) {
        if (property.title?.[key] !== undefined && property.title[key] !== '') {
          const tag: TitleTag = { lang: key, value: property.title[key] };
          result.push(tag);
        }
      }
    }
    return result;
  });

  public getTitleAsArray(): TitleTag[] {
    return this.titleAsArray();
  }

  public bubbleDynpropDeleted() {
    this.dynnamicPropDelete.emit(this.property());
  }

  public bubbleDynpropEdit() {
    this.dynamicPropEdit.emit(this.property());
  }

  public isSelection(): boolean {
    const property = this.property();
    return (
      property?.propertyType !== undefined &&
      (property.propertyType === 'SELECTION' ||
        property.propertyType === 'MULTI_SELECTION')
    );
  }
}
