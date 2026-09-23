import { Location } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import {
  DynamicPropertiesService,
  DynamicPropertyDefinition,
  DynamicPropertyDefinitionUpdatedValues,
} from 'app/core/generated/circabc';
import { LoadingService } from 'app/core/loading.service';
import { DynamicPropertyType } from 'app/group/dynamic-properties/type/dynamic-property-type';
import { getDynamicPropertyTypes } from 'app/group/dynamic-properties/type/dynamic-property-types';
import { MultilingualInputComponent } from 'app/shared/input/multilingual-input.component';
import { HorizontalLoaderComponent } from 'app/shared/loader/horizontal-loader.component';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';

/**
 * Standalone Angular component (`cbc-edit-dynamic-property`) that renders the
 * edit screen for a single dynamic property definition of an interest group.
 *
 * The view lets a user update the multilingual title of a dynamic property and,
 * for `SELECTION` / `MULTI_SELECTION` property types, manage the list of
 * possible values: adding, editing, reordering and marking values for removal.
 * The property type itself is displayed read-only (the corresponding form
 * control is disabled).
 *
 * The property to edit is resolved from the `dpId` route parameter. On submit
 * the accumulated changes are persisted through the
 * {@link DynamicPropertiesService} and the user is navigated back to the
 * previous location.
 *
 * Key collaborators:
 * - {@link DynamicPropertiesService} — loads and persists the property definition.
 * - {@link ActivatedRoute} — supplies the `dpId` route parameter.
 * - {@link Location} — navigates back after save/cancel.
 * - {@link FormBuilder} — builds the reactive forms used by the template.
 */
@Component({
  selector: 'cbc-edit-dynamic-property',
  templateUrl: './edit-dynamic-property.component.html',
  styleUrl: './edit-dynamic-property.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    HorizontalLoaderComponent,
    ReactiveFormsModule,
    MultilingualInputComponent,
    SpinnerComponent,
    TranslocoModule,
  ],
})
export class EditDynamicPropertyComponent implements OnInit {
  /** Reactive forms builder used to construct the component's form groups. */
  private readonly fb = inject(FormBuilder);
  /** API client used to load and persist dynamic property definitions. */
  private readonly dynamicPropertiesService = inject(DynamicPropertiesService);
  private readonly loadingService = inject(LoadingService);
  /** Provides access to the current route parameters (notably `dpId`). */
  private readonly route = inject(ActivatedRoute);
  /** Used to navigate back to the previous page after save or cancel. */
  private readonly location = inject(Location);

  /** Whether the property definition is currently being loaded. */
  public readonly loading = signal(false);
  /** Whether a save (update) operation is currently in progress. */
  public readonly executing = signal(false);
  /** Controls visibility of the property definition section in the template. */
  public showDefinition = true;
  /** Reactive form holding the editable `title` and the read-only `type`. */
  public editDynPropForm!: FormGroup;
  /** Reactive form used to enter a new or edited possible value. */
  public newValueForm!: FormGroup;
  /** The property definition as originally loaded from the backend. */
  public readonly originalProperty = signal<
    DynamicPropertyDefinition | undefined
  >(undefined);
  /**
   * Working list of possible values with their edit status (`''`, `'new'`,
   * `'edited'` or `'deleted'`), submitted as the property's updated values.
   */
  public readonly values = signal<DynamicPropertyDefinitionUpdatedValues[]>([]);
  /** Index of the value currently being edited, or `-1` when none is. */
  public currentIndex = -1;

  /**
   * Angular lifecycle hook. Initialises the reactive forms, disables the
   * read-only `type` control and, when a `dpId` route parameter is present,
   * loads the corresponding dynamic property definition.
   */
  ngOnInit() {
    this.editDynPropForm = this.fb.group(
      {
        title: [],
        type: [],
      },
      {
        updateOn: 'change',
      }
    );

    this.newValueForm = this.fb.group(
      {
        newValue: ['', Validators.required],
      },
      {
        updateOn: 'change',
      }
    );

    this.editDynPropForm.controls.type.disable();

    if (this.route?.params) {
      this.route.params.subscribe(async (params) => {
        if (params.dpId) {
          await this.loadDynamicProperty(params.dpId);
        }
      });
    }
  }

  /**
   * Loads a dynamic property definition by id, populates the edit form with its
   * title and type, and initialises the editable list of possible values.
   *
   * @param id - Identifier of the dynamic property definition to load.
   * @returns A promise that resolves once the property and its values are loaded.
   */
  public async loadDynamicProperty(id: string) {
    const originalProperty = await this.loadingService.run(this.loading, () =>
      this.dynamicPropertiesService.getDynamicPropertyDefinitionAsync({ id })
    );
    if (!originalProperty) {
      return;
    }
    this.originalProperty.set(originalProperty);
    this.editDynPropForm.controls.title.setValue(originalProperty.title);
    this.editDynPropForm.controls.type.setValue(originalProperty.propertyType);

    this.initValues();
  }

  /**
   * Returns the list of available dynamic property types used to render the
   * (read-only) type selector.
   *
   * @returns The array of supported {@link DynamicPropertyType} descriptors.
   */
  public getTypes(): DynamicPropertyType[] {
    return getDynamicPropertyTypes();
  }

  /**
   * Indicates whether the loaded property is of a type that carries a list of
   * selectable values (`SELECTION` or `MULTI_SELECTION`).
   *
   * @returns `true` when the property supports possible values, otherwise `false`.
   */
  public hasValues(): boolean {
    const originalProperty = this.originalProperty();
    if (originalProperty) {
      return (
        originalProperty.propertyType === 'SELECTION' ||
        originalProperty.propertyType === 'MULTI_SELECTION'
      );
    }

    return false;
  }

  /**
   * Persists the pending changes (updated title and value list) to the backend.
   *
   * Applies the working `values` and the form title onto the original property
   * and sends an update request. On success the user is navigated back to the
   * previous page; failures are captured and reflected in the action result
   * without throwing.
   *
   * @returns A promise that resolves once the update attempt completes.
   */
  public async ok() {
    this.executing.set(true);
    const res: ActionEmitterResult = {};
    res.type = ActionType.UPDATE_DYNAMIC_PROPERTIES;

    try {
      const originalProperty = this.originalProperty();
      if (originalProperty?.id) {
        originalProperty.updatedValues = this.values();
        originalProperty.title = this.editDynPropForm.value.title;
        await this.dynamicPropertiesService.putDynamicPropertyDefinitionAsync({
          id: originalProperty.id,
          dynamicPropertyDefinition: originalProperty,
        });
        res.result = ActionResult.SUCCEED;
      }
    } catch (error) {
      console.error(error);
      res.result = ActionResult.FAILED;
    }

    if (res.result === ActionResult.SUCCEED) {
      this.location.back();
    }
    this.executing.set(false);
  }

  /** Discards changes and navigates back to the previous page. */
  public cancel() {
    this.location.back();
  }

  /**
   * Builds the working list of editable values from the loaded property's
   * possible values, when the property is of a selection type. Each entry is
   * seeded with matching `old`/`_new` values and an empty status.
   *
   * @returns The initialised list of values, or an empty array when the
   * property has no possible values or is not a selection type.
   */
  public initValues(): DynamicPropertyDefinitionUpdatedValues[] {
    const originalProperty = this.originalProperty();
    if (
      originalProperty &&
      (originalProperty.propertyType === 'SELECTION' ||
        originalProperty.propertyType === 'MULTI_SELECTION') &&
      originalProperty.possibleValues
    ) {
      const values: DynamicPropertyDefinitionUpdatedValues[] = [];
      originalProperty.possibleValues.forEach((value) => {
        const valEdit: DynamicPropertyDefinitionUpdatedValues = {};
        valEdit.old = value;
        valEdit.new = value;
        valEdit.status = '';
        values.push(valEdit);
      });
      this.values.set(values);

      return values;
    }

    return [];
  }

  /**
   * Appends the value currently entered in {@link newValueForm} to the working
   * list, marking it with a `'new'` status, then resets the input form.
   */
  public addValue() {
    const valEdit: DynamicPropertyDefinitionUpdatedValues = {};
    valEdit.old = this.newValueForm.value.newValue;
    valEdit.new = this.newValueForm.value.newValue;
    valEdit.status = 'new';
    this.values.update((values) => [...values, valEdit]);
    this.newValueForm.reset();
  }

  /** Cancels the add-value action, resetting the input form and edit index. */
  public cancelAddValue() {
    this.newValueForm.reset();
    this.currentIndex = -1;
  }

  /**
   * Reports whether the new-value input form currently holds a valid entry.
   *
   * @returns `true` when {@link newValueForm} is valid, otherwise `false`.
   */
  public newValueFormValid(): boolean {
    return this.newValueForm.valid;
  }

  /**
   * Enters edit mode for the given value: records its index as the current
   * edit target and loads its current value into the input form.
   *
   * @param item - The value entry to edit.
   */
  public tagAsEdited(item: DynamicPropertyDefinitionUpdatedValues) {
    this.currentIndex = this.values().indexOf(item);
    this.newValueForm.controls.newValue.setValue(item.new);
  }

  /**
   * Toggles the removal state of the given value entry.
   *
   * Behaviour depends on the entry's current status:
   * - `'deleted'` → un-marks it (status cleared).
   * - `'new'` → removes it from the list entirely.
   * - `'edited'` → resets its edits, then marks it as `'deleted'`.
   * - otherwise → marks it as `'deleted'`.
   *
   * @param item - The value entry whose removal state should be toggled.
   */
  public tagAsRemoved(item: DynamicPropertyDefinitionUpdatedValues) {
    const values = this.values();
    const index = values.indexOf(item);
    if (values[index].status === 'deleted') {
      values[index].status = '';
    } else if (values[index].status === 'new') {
      this.values.update((current) =>
        current.filter((_value, i) => i !== index)
      );
    } else if (values[index].status === 'edited') {
      this.resetEdited(item);
      values[index].status = 'deleted';
    } else {
      values[index].status = 'deleted';
    }
  }

  /**
   * Moves the given value one position towards the start of the list. When the
   * item is already first, it wraps around to the end of the list instead.
   *
   * @param item - The value entry to move up.
   */
  public moveUp(item: DynamicPropertyDefinitionUpdatedValues) {
    this.values.update((values) => {
      const newValues = [...values];
      const index = newValues.indexOf(item);
      if (index === 0) {
        newValues.splice(index, 1);
        newValues.push(item);
      } else {
        newValues.splice(index, 1);
        newValues.splice(index - 1, 0, item);
      }
      return newValues;
    });
  }

  /**
   * Moves the given value one position towards the end of the list. When the
   * item is already last, it wraps around to the start of the list instead.
   *
   * @param item - The value entry to move down.
   */
  public moveDown(item: DynamicPropertyDefinitionUpdatedValues) {
    this.values.update((values) => {
      const newValues = [...values];
      const index = newValues.indexOf(item);
      if (index === newValues.length - 1) {
        newValues.splice(index, 1);
        newValues.splice(0, 0, item);
      } else {
        newValues.splice(index, 1);
        newValues.splice(index + 1, 0, item);
      }
      return newValues;
    });
  }

  /**
   * Commits the in-progress edit: marks the value at {@link currentIndex} as
   * `'edited'`, applies the new text from the input form, then resets the form
   * and clears the edit index.
   */
  public finishEdit() {
    const values = this.values();
    values[this.currentIndex].status = 'edited';
    values[this.currentIndex].new = this.newValueForm.value.newValue;
    this.newValueForm.reset();
    this.currentIndex = -1;
  }

  /**
   * Cancels the in-progress edit without applying changes, resetting the input
   * form and clearing the edit index.
   *
   * @param _item - The value entry whose edit is being cancelled (unused).
   */
  public cancelEdit(_item: DynamicPropertyDefinitionUpdatedValues) {
    this.newValueForm.reset();
    this.currentIndex = -1;
  }

  /**
   * Reverts a previously edited value back to its original text and clears its
   * status, also resetting the input form and the edit index.
   *
   * @param item - The value entry whose edits should be reverted.
   */
  public resetEdited(item: DynamicPropertyDefinitionUpdatedValues) {
    const values = this.values();
    const index = values.indexOf(item);
    this.newValueForm.reset();
    this.currentIndex = -1;
    values[index].status = '';
    values[index].new = values[index].old;
  }
}
