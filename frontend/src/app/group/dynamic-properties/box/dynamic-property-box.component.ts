import {
  ChangeDetectionStrategy,
  Component,
  computed,
  input,
  output,
} from '@angular/core';

import { TranslocoModule } from '@jsverse/transloco';
import { DynamicPropertyDefinition } from 'app/core/generated/circabc';
import { TitleTag } from 'app/group/dynamic-properties/title/title';
import { TitleTagComponent } from 'app/group/dynamic-properties/title/title-tag.component';

/**
 * Presentational card component that renders a single
 * {@link DynamicPropertyDefinition} as a "box".
 *
 * It displays the property's multilingual title (via {@link TitleTagComponent})
 * and exposes edit and delete actions that are surfaced to the parent
 * component through outputs. The component holds no state of its own: it
 * receives the property to display as a required input and simply bubbles
 * user intent (edit/delete) back up to its host.
 */
@Component({
  selector: 'cbc-dynamic-property-box',
  templateUrl: './dynamic-property-box.component.html',
  styleUrl: './dynamic-property-box.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [TitleTagComponent, TranslocoModule],
})
export class DynamicPropertyBoxComponent {
  /**
   * Required input holding the dynamic property definition rendered by this
   * box. Provides the title map, property type and other metadata that drive
   * the template.
   */
  readonly property = input.required<DynamicPropertyDefinition>();
  /**
   * Emitted when the user requests deletion of the displayed property.
   * The event payload is the current {@link property} value.
   */
  readonly dynnamicPropDelete = output<DynamicPropertyDefinition>();
  /**
   * Emitted when the user requests editing of the displayed property.
   * The event payload is the current {@link property} value.
   */
  readonly dynamicPropEdit = output<DynamicPropertyDefinition>();

  /**
   * Memoized view model derived from {@link property}. Converts the property's
   * `title` language map into an array of {@link TitleTag} entries, keeping
   * only the languages that have a non-empty value. Recomputed automatically
   * whenever {@link property} changes.
   */
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

  /**
   * Returns the memoized list of title tags for the current property.
   *
   * @returns The {@link TitleTag} entries built from the property's title map.
   */
  public getTitleAsArray(): TitleTag[] {
    return this.titleAsArray();
  }

  /**
   * Emits the {@link dynnamicPropDelete} output with the current property,
   * signalling to the parent that the property should be deleted.
   */
  public bubbleDynpropDeleted() {
    this.dynnamicPropDelete.emit(this.property());
  }

  /**
   * Emits the {@link dynamicPropEdit} output with the current property,
   * signalling to the parent that the property should be edited.
   */
  public bubbleDynpropEdit() {
    this.dynamicPropEdit.emit(this.property());
  }

  /**
   * Indicates whether the current property is a selection-based type.
   *
   * @returns `true` when the property's type is `SELECTION` or
   * `MULTI_SELECTION`, `false` otherwise (including when no type is defined).
   */
  public isSelection(): boolean {
    const property = this.property();
    return (
      property?.propertyType !== undefined &&
      (property.propertyType === 'SELECTION' ||
        property.propertyType === 'MULTI_SELECTION')
    );
  }
}
