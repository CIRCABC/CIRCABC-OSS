import { Component, Input, output, input, computed } from '@angular/core';

import { TranslocoModule } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import {
  DynamicPropertiesService,
  DynamicPropertyDefinition,
} from 'app/core/generated/circabc';
import { TitleTag } from 'app/group/dynamic-properties/title/title';
import { TitleTagComponent } from 'app/group/dynamic-properties/title/title-tag.component';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-dynamic-property-delete',
  templateUrl: './dynamic-property-delete.component.html',
  preserveWhitespaces: true,
  imports: [TitleTagComponent, SpinnerComponent, TranslocoModule],
})
export class DynamicPropertyDeleteComponent {
  readonly property = input<DynamicPropertyDefinition>();
  readonly modalHide = output<ActionEmitterResult>();
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  showModal = false;
  public deleting = false;

  constructor(private dynamicPropertiesService: DynamicPropertiesService) {}

  // Use computed signal to memoize the title array
  readonly titleAsArray = computed(() => {
    const result: TitleTag[] = [];
    const property = this.property();

    if (property) {
      for (const key of Object.keys(property.title)) {
        if (property.title) {
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

  public async delete() {
    this.deleting = true;

    const result: ActionEmitterResult = {};
    result.type = ActionType.DELETE_DYNAMIC_PROPERTY;
    result.result = ActionResult.FAILED;

    const property = this?.property();
    if (property?.id) {
      await firstValueFrom(
        this.dynamicPropertiesService.deleteDynamicPropertyDefinition(
          property.id
        )
      );
      result.result = ActionResult.SUCCEED;
    }
    this.deleting = false;
    this.showModal = false;
    this.modalHide.emit(result);
  }

  public cancelWizard(_action: string): void {
    this.showModal = false;
    const result: ActionEmitterResult = {};
    result.result = ActionResult.CANCELED;
    result.type = ActionType.DELETE_DYNAMIC_PROPERTY;
    this.modalHide.emit(result);
  }
}
