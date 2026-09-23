import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  inject,
  input,
  model,
  OnChanges,
  OnInit,
  output,
  SimpleChanges,
} from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';

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
import { nonEmptyTitle } from 'app/core/validation.service';
import { DynamicPropertyType } from 'app/group/dynamic-properties/type/dynamic-property-type';
import { getDynamicPropertyTypes } from 'app/group/dynamic-properties/type/dynamic-property-types';
import { ControlMessageComponent } from 'app/shared/control-message/control-message.component';
import { MultilingualInputComponent } from 'app/shared/input/multilingual-input.component';
import { ModalComponent } from 'app/shared/modal/modal.component';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';

/**
 * Modal-based component that renders a wizard for creating a new dynamic
 * property definition or editing an existing one within an interest group.
 *
 * When no {@link property} input is supplied it operates in "create" mode,
 * presenting a form to capture the property title, type and (for selection
 * types) an initial set of possible values. When a {@link property} is
 * supplied it operates in "edit" mode, loading the current definition and
 * allowing the title and the list of possible values to be added, edited,
 * removed and reordered.
 *
 * The component collaborates with {@link DynamicPropertiesService} to persist
 * property definitions and emits the outcome of each operation through
 * {@link modalHide} as an {@link ActionEmitterResult}.
 */
@Component({
  selector: 'cbc-create-dynamic-property',
  templateUrl: './create-dynamic-property.component.html',
  styleUrl: './create-dynamic-property.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ModalComponent,
    ReactiveFormsModule,
    ControlMessageComponent,
    MultilingualInputComponent,
    SpinnerComponent,
    TranslocoModule,
  ],
})
export class CreateDynamicPropertyComponent implements OnChanges, OnInit {
  /** Angular reactive-forms builder used to construct the form groups. */
  private readonly fb = inject(FormBuilder);
  /** Generated API service used to create, load and update property definitions. */
  private readonly dynamicPropertiesService = inject(DynamicPropertiesService);
  private readonly changeDetectorRef = inject(ChangeDetectorRef);

  /** Required input: identifier of the interest group the property belongs to. */
  readonly groupId = input.required<string>();

  /** Two-way model controlling whether the wizard modal is visible. */
  showModal = model(false);
  /**
   * Optional input: the existing property definition to edit. When
   * `undefined` the component works in create mode; when provided it works in
   * edit mode.
   */
  readonly property = input<DynamicPropertyDefinition>();
  /**
   * Output emitted when the modal closes, carrying the result of the create,
   * update or cancel action.
   */
  readonly modalHide = output<ActionEmitterResult>();

  /** Flag set while a create or update request is in flight. */
  public creating = false;
  /** Reactive form backing the create-mode fields (title, type, values). */
  public createForm!: FormGroup;
  /** Working list of possible values (with edit status) used in edit mode. */
  public values: DynamicPropertyDefinitionUpdatedValues[] = [];

  /** Whether the definition step of the wizard is currently shown. */
  public showDefinition = true;
  /** Index of the value currently being edited, or `-1` when none. */
  public currentIndex = -1;
  /** Reactive form backing the edit-mode definition fields (title, type). */
  public editDynPropForm!: FormGroup;
  /** Reactive form backing the single "new/edited value" input. */
  public newValueForm!: FormGroup;
  /** The property definition as originally loaded from the backend. */
  public originalProperty!: DynamicPropertyDefinition;
  /** Flag set while the existing property definition is being loaded. */
  public loading = false;

  /**
   * Angular lifecycle hook that initializes the create, edit and new-value
   * reactive forms and disables the (read-only) type control in edit mode.
   */
  ngOnInit() {
    this.createForm = this.fb.group(
      {
        title: ['', nonEmptyTitle],
        propertyType: ['', Validators.required],
        possibleValues: [''],
      },
      {
        updateOn: 'change',
      }
    );
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
  }

  /**
   * Angular lifecycle hook triggered when inputs change. Resets all forms and,
   * when a {@link property} input is present, loads its definition for editing.
   *
   * @param _changes the set of changed inputs (unused).
   * @returns a promise that resolves once any required property load completes.
   */
  ngOnChanges(_changes: SimpleChanges) {
    void this.handleChanges();
  }

  private async handleChanges() {
    this.cleanForms();
    const property = this.property();
    if (property !== undefined) {
      await this.loadDynamicProperty(property.id as string);
    }
  }

  /** Resets every form control back to its empty/default value. */
  private cleanForms() {
    if (this.createForm !== undefined) {
      this.createForm.controls.title.setValue({});
      this.createForm.controls.propertyType.setValue('');
      this.createForm.controls.possibleValues.setValue('');
    }
    if (this.editDynPropForm !== undefined) {
      this.editDynPropForm.controls.title.setValue({});
      this.editDynPropForm.controls.type.setValue('');
    }
    if (this.newValueForm !== undefined) {
      this.newValueForm.controls.newValue.setValue('');
    }
  }

  /**
   * Validates the create form and, when valid, submits a new property
   * definition to the backend. Invalid controls are marked dirty so that
   * validation messages are shown. On success the form is cleared and reset.
   * Always emits the outcome through {@link modalHide}.
   *
   * @returns a promise that resolves once the create request completes and the
   * result has been emitted.
   */
  public async create() {
    if (this.createForm.invalid) {
      Object.keys(this.createForm.controls).forEach((key) => {
        const control = this.createForm.controls[key];
        if (control.invalid) {
          control.markAsDirty();
        }
      });
      return;
    }
    this.creating = true;

    let newProp: DynamicPropertyDefinition = {
      title: this.createForm.value.title,
      propertyType: this.createForm.value.propertyType,
    };

    const result: ActionEmitterResult = {};
    result.type = ActionType.CREATE_DYNAMIC_PROPERTY;
    result.result = ActionResult.FAILED;

    const possibleValues = { possibleValues: this.transformPossibleValues() };
    newProp = { ...newProp, ...possibleValues };

    const res = await this.dynamicPropertiesService.postPropertyDefinitionAsync(
      {
        id: this.groupId(),
        dynamicPropertyDefinition: newProp,
      }
    );
    if (res) {
      result.result = ActionResult.SUCCEED;

      this.createForm.controls.title.setValue({});
      this.createForm.controls.title.markAsUntouched();
      this.createForm.controls.propertyType.setValue('');
      this.createForm.controls.propertyType.markAsUntouched();
      this.createForm.controls.possibleValues.setValue('');
      this.createForm.controls.possibleValues.markAsUntouched();
    }
    this.creating = false;
    this.createForm.reset();
    this.modalHide.emit(result);
    // OnPush: creating is cleared after the async create completes.
    this.changeDetectorRef.markForCheck();
  }

  /**
   * Parses the raw multi-line `possibleValues` textarea into a trimmed list of
   * non-empty value strings.
   *
   * @returns the list of possible values entered by the user.
   */
  private transformPossibleValues(): string[] {
    const result: string[] = [];
    if (this.createForm.value.possibleValues) {
      const lines = this.createForm.value.possibleValues
        .replace('/\r\n/g', '\n')
        .split('\n');
      for (const str of lines) {
        if (str !== '') {
          result.push(str.trim());
        }
      }
    }
    return result;
  }

  /**
   * Determines whether the primary create/update action can be triggered:
   * in create mode the form must be valid, and in both modes no request may be
   * in flight.
   *
   * @returns `true` when the create or update action is allowed.
   */
  public canCreateOrUpdate(): boolean {
    const property = this.property();
    return (
      (this.createForm.valid && !this.creating && property === undefined) ||
      (!this.creating && property !== undefined)
    );
  }

  /**
   * Persists edits to the current property definition, applying the updated
   * title and value list, then closes the modal and emits the result.
   *
   * @returns a promise that resolves once the update request completes and the
   * result has been emitted.
   */
  public async update() {
    this.creating = true;
    const res: ActionEmitterResult = {};
    res.type = ActionType.UPDATE_DYNAMIC_PROPERTIES;

    try {
      const property = this.property();
      if (property?.id) {
        property.updatedValues = this.values;
        property.title = this.editDynPropForm.value.title;
        await this.dynamicPropertiesService.putDynamicPropertyDefinitionAsync({
          id: property.id,
          dynamicPropertyDefinition: property,
        });
        res.result = ActionResult.SUCCEED;
      }
    } catch (error) {
      console.error(error);
      res.result = ActionResult.FAILED;
    }

    this.creating = false;
    this.showModal.set(false);
    this.showDefinition = true;
    this.modalHide.emit(res);
    // OnPush: creating/showDefinition are updated after the async update.
    this.changeDetectorRef.markForCheck();
  }

  /**
   * Loads the property definition with the given identifier from the backend,
   * populates the edit form and initializes the editable value list.
   *
   * @param id the identifier of the property definition to load.
   * @returns a promise that resolves once the definition has been loaded.
   */
  public async loadDynamicProperty(id: string) {
    this.loading = true;
    this.originalProperty =
      await this.dynamicPropertiesService.getDynamicPropertyDefinitionAsync({
        id,
      });
    this.editDynPropForm.controls.title.setValue(this.originalProperty.title);
    this.editDynPropForm.controls.type.setValue(
      this.originalProperty.propertyType
    );

    this.initValues();

    this.loading = false;
    // OnPush: originalProperty/values/loading are populated asynchronously.
    this.changeDetectorRef.markForCheck();
  }

  /**
   * Provides the list of selectable dynamic property types for the type field.
   *
   * @returns the available {@link DynamicPropertyType} options.
   */
  public getTypes(): DynamicPropertyType[] {
    return getDynamicPropertyTypes();
  }

  /**
   * Indicates whether the loaded property type supports a list of possible
   * values (selection or multi-selection types).
   *
   * @returns `true` when the property is a `SELECTION` or `MULTI_SELECTION`.
   */
  public hasValues(): boolean {
    if (this.originalProperty) {
      return (
        this.originalProperty.propertyType === 'SELECTION' ||
        this.originalProperty.propertyType === 'MULTI_SELECTION'
      );
    }

    return false;
  }

  /**
   * Builds the editable {@link values} list from the loaded property's possible
   * values (for selection types), seeding each entry's old/new value and an
   * empty status.
   *
   * @returns the populated value list, or an empty array for non-selection
   * types or when no possible values exist.
   */
  public initValues(): DynamicPropertyDefinitionUpdatedValues[] {
    if (
      this.originalProperty &&
      (this.originalProperty.propertyType === 'SELECTION' ||
        this.originalProperty.propertyType === 'MULTI_SELECTION') &&
      this.originalProperty.possibleValues
    ) {
      this.values = [];
      this.originalProperty.possibleValues.forEach((value) => {
        const valEdit: DynamicPropertyDefinitionUpdatedValues = {};
        valEdit.old = value;
        valEdit.new = value;
        valEdit.status = '';
        this.values.push(valEdit);
      });

      return this.values;
    }

    return [];
  }

  /**
   * Appends the value currently held in the new-value form to {@link values},
   * marking it as `new`, then resets the new-value form.
   */
  public addValue() {
    const valEdit: DynamicPropertyDefinitionUpdatedValues = {};
    valEdit.old = this.newValueForm.value.newValue;
    valEdit.new = this.newValueForm.value.newValue;
    valEdit.status = 'new';
    this.values.push(valEdit);
    this.newValueForm.reset();
  }

  /** Cancels adding a value, resetting the new-value form and edit index. */
  public cancelAddValue() {
    this.newValueForm.reset();
    this.currentIndex = -1;
  }

  /**
   * Reports whether the new-value form currently holds a valid entry.
   *
   * @returns `true` when the new-value form is valid.
   */
  public newValueFormValid(): boolean {
    return this.newValueForm.valid;
  }

  /**
   * Marks the given value as the one being edited, loading its current value
   * into the new-value form.
   *
   * @param item the value entry to edit.
   */
  public tagAsEdited(item: DynamicPropertyDefinitionUpdatedValues) {
    this.currentIndex = this.values.indexOf(item);
    this.newValueForm.controls.newValue.setValue(item.new);
  }

  /**
   * Toggles the removal state of the given value. Newly added values are
   * dropped entirely, already-deleted values are restored, and other values
   * are marked as `deleted` (reverting any pending edit first).
   *
   * @param item the value entry to remove or restore.
   */
  public tagAsRemoved(item: DynamicPropertyDefinitionUpdatedValues) {
    const index = this.values.indexOf(item);
    if (this.values[index].status === 'deleted') {
      this.values[index].status = '';
    } else if (this.values[index].status === 'new') {
      this.values.splice(index, 1);
    } else if (this.values[index].status === 'edited') {
      this.resetEdited(item);
      this.values[index].status = 'deleted';
    } else {
      this.values[index].status = 'deleted';
    }
  }

  /**
   * Moves the given value one position up in the list, wrapping the first entry
   * to the end.
   *
   * @param item the value entry to move up.
   */
  public moveUp(item: DynamicPropertyDefinitionUpdatedValues) {
    const index = this.values.indexOf(item);
    if (index === 0) {
      this.values.splice(index, 1);
      this.values.push(item);
    } else {
      const newValues = this.values;
      newValues.splice(index, 1);
      newValues.splice(index - 1, 0, item);
      this.values = newValues;
    }
  }

  /**
   * Moves the given value one position down in the list, wrapping the last
   * entry to the front.
   *
   * @param item the value entry to move down.
   */
  public moveDown(item: DynamicPropertyDefinitionUpdatedValues) {
    const index = this.values.indexOf(item);
    if (index === this.values.length - 1) {
      this.values.splice(index, 1);
      this.values.splice(0, 0, item);
    } else {
      const newValues = this.values;
      newValues.splice(index, 1);
      newValues.splice(index + 1, 0, item);
      this.values = newValues;
    }
  }

  /**
   * Commits the pending edit: marks the value at {@link currentIndex} as
   * `edited`, applies the new value from the form, then resets edit state.
   */
  public finishEdit() {
    this.values[this.currentIndex].status = 'edited';
    this.values[this.currentIndex].new = this.newValueForm.value.newValue;
    this.newValueForm.reset();
    this.currentIndex = -1;
  }

  /**
   * Cancels the current edit without applying changes, resetting the new-value
   * form and edit index.
   *
   * @param _item the value entry whose edit is cancelled (unused).
   */
  public cancelEdit(_item: DynamicPropertyDefinitionUpdatedValues) {
    this.newValueForm.reset();
    this.currentIndex = -1;
  }

  /**
   * Reverts the given value back to its original value and clears its edit
   * status, also resetting the new-value form and edit index.
   *
   * @param item the value entry to reset.
   */
  public resetEdited(item: DynamicPropertyDefinitionUpdatedValues) {
    const index = this.values.indexOf(item);
    this.newValueForm.reset();
    this.currentIndex = -1;
    this.values[index].status = '';
    this.values[index].new = this.values[index].old;
  }

  /**
   * Closes the wizard without saving, emitting a cancelled result whose action
   * type reflects create vs edit mode, and resets all forms.
   */
  public cancelWizard() {
    this.showModal.set(false);
    this.showDefinition = true;
    const result: ActionEmitterResult = {};
    if (this.property() === undefined) {
      result.type = ActionType.CREATE_DYNAMIC_PROPERTY;
    } else {
      result.type = ActionType.UPDATE_DYNAMIC_PROPERTIES;
    }
    result.result = ActionResult.CANCELED;
    this.modalHide.emit(result);
    this.createForm.reset();
    this.editDynPropForm.reset();
    this.newValueForm.reset();
  }

  /** Accessor for the create form's property-type control. */
  get propertyTypeControl(): AbstractControl {
    return this.createForm.controls.propertyType;
  }

  /** Accessor for the create form's title control. */
  get titleControl(): AbstractControl {
    return this.createForm.controls.title;
  }
}
