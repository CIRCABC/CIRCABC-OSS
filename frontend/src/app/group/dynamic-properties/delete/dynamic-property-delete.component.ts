import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  input,
  model,
  output,
  signal,
} from '@angular/core';

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

/**
 * Confirmation modal component for deleting a dynamic property definition.
 *
 * Renders a modal dialog that displays the localized title(s) of the dynamic
 * property targeted for deletion (via {@link TitleTagComponent}) and lets the
 * user confirm or cancel the operation. A {@link SpinnerComponent} is shown
 * while the delete request is in flight.
 *
 * On confirmation it calls the backend through
 * {@link DynamicPropertiesService} to remove the property definition and
 * notifies the parent of the outcome via the {@link modalHide} output.
 */
@Component({
  selector: 'cbc-dynamic-property-delete',
  templateUrl: './dynamic-property-delete.component.html',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [TitleTagComponent, SpinnerComponent, TranslocoModule],
})
export class DynamicPropertyDeleteComponent {
  /** Generated API client used to delete the dynamic property definition. */
  private readonly dynamicPropertiesService = inject(DynamicPropertiesService);

  /** Input holding the dynamic property definition to be deleted. */
  readonly property = input<DynamicPropertyDefinition>();
  /**
   * Emitted when the modal closes, carrying the result of the operation
   * (succeeded, failed or canceled) so the parent can react accordingly.
   */
  readonly modalHide = output<ActionEmitterResult>();

  /** Two-way bound flag controlling the visibility of the modal. */
  showModal = model(false);
  /** True while the delete request is being processed, used to show the spinner. */
  public readonly deleting = signal(false);

  /**
   * Memoized signal that converts the property's localized title map into an
   * array of {@link TitleTag} entries (one per language) for rendering.
   */
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

  /**
   * Returns the current list of localized title tags for the property.
   *
   * @returns An array of {@link TitleTag} entries derived from the property title map.
   */
  public getTitleAsArray(): TitleTag[] {
    return this.titleAsArray();
  }

  /**
   * Deletes the currently targeted dynamic property definition.
   *
   * Sets the deleting flag while the backend request is in progress, closes
   * the modal, and emits {@link modalHide} with an {@link ActionEmitterResult}
   * whose result is {@link ActionResult.SUCCEED} on success or
   * {@link ActionResult.FAILED} when no property id is available.
   *
   * @returns A promise that resolves once the deletion attempt has completed
   * and the outcome has been emitted.
   */
  public async delete() {
    this.deleting.set(true);

    const result: ActionEmitterResult = {};
    result.type = ActionType.DELETE_DYNAMIC_PROPERTY;
    result.result = ActionResult.FAILED;

    const property = this?.property();
    if (property?.id) {
      await this.dynamicPropertiesService.deleteDynamicPropertyDefinitionAsync({
        id: property.id,
      });
      result.result = ActionResult.SUCCEED;
    }
    this.deleting.set(false);
    this.showModal.set(false);
    this.modalHide.emit(result);
  }

  /**
   * Cancels the delete operation without contacting the backend.
   *
   * Closes the modal and emits {@link modalHide} with an
   * {@link ActionEmitterResult} whose result is {@link ActionResult.CANCELED}.
   *
   * @param _action The originating action identifier (unused).
   */
  public cancelWizard(_action: string): void {
    this.showModal.set(false);
    const result: ActionEmitterResult = {};
    result.result = ActionResult.CANCELED;
    result.type = ActionType.DELETE_DYNAMIC_PROPERTY;
    this.modalHide.emit(result);
  }
}
