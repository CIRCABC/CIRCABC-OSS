import { Component, EventEmitter, Input, Output } from '@angular/core';

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
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-dynamic-property-delete',
  templateUrl: './dynamic-property-delete.component.html',
  preserveWhitespaces: true,
})
export class DynamicPropertyDeleteComponent {
  @Input()
  property: DynamicPropertyDefinition | undefined;
  @Output()
  readonly modalHide = new EventEmitter<ActionEmitterResult>();
  @Input()
  showModal = false;
  public deleting = false;

  constructor(private dynamicPropertiesService: DynamicPropertiesService) {}

  public getTitleAsArray(): TitleTag[] {
    const result: TitleTag[] = [];

    if (this.property) {
      for (const key of Object.keys(this.property.title)) {
        if (this.property.title) {
          const tag: TitleTag = { lang: key, value: this.property.title[key] };
          result.push(tag);
        }
      }
    }
    return result;
  }

  public async delete() {
    this.deleting = true;

    const result: ActionEmitterResult = {};
    result.type = ActionType.DELETE_DYNAMIC_PROPERTY;
    result.result = ActionResult.FAILED;

    if (this?.property?.id) {
      await firstValueFrom(
        this.dynamicPropertiesService.deleteDynamicPropertyDefinition(
          this.property.id
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
