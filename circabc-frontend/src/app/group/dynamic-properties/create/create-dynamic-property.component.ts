import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges,
} from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  Validators,
} from '@angular/forms';

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
import { ValidationService } from 'app/core/validation.service';
import { DynamicPropertyType } from 'app/group/dynamic-properties/type/dynamic-property-type';
import { DynamicPropertyTypes } from 'app/group/dynamic-properties/type/dynamic-property-types';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-create-dynamic-property',
  templateUrl: './create-dynamic-property.component.html',
  styleUrls: ['./create-dynamic-property.component.scss'],
  preserveWhitespaces: true,
})
export class CreateDynamicPropertyComponent implements OnChanges, OnInit {
  @Input()
  groupId!: string;
  @Input()
  showModal = false;
  @Input()
  property: DynamicPropertyDefinition | undefined;
  @Output()
  readonly modalHide = new EventEmitter<ActionEmitterResult>();

  public creating = false;
  public createForm!: FormGroup;
  public values: DynamicPropertyDefinitionUpdatedValues[] = [];

  public showDefinition = true;
  public currentIndex = -1;
  public editDynPropForm!: FormGroup;
  public newValueForm!: FormGroup;
  public originalProperty!: DynamicPropertyDefinition;
  public loading = false;

  constructor(
    private fb: FormBuilder,
    private dynamicPropertiesService: DynamicPropertiesService
  ) {}

  ngOnInit() {
    this.createForm = this.fb.group(
      {
        title: ['', ValidationService.nonEmptyTitle],
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

  async ngOnChanges(_changes: SimpleChanges) {
    this.cleanForms();
    if (this.property !== undefined) {
      await this.loadDynamicProperty(this.property.id as string);
    }
  }

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

    const res = await firstValueFrom(
      this.dynamicPropertiesService.postPropertyDefinition(
        this.groupId,
        newProp
      )
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
  }

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

  public canCreateOrUpdate(): boolean {
    return (
      (this.createForm.valid &&
        !this.creating &&
        this.property === undefined) ||
      (!this.creating && this.property !== undefined)
    );
  }

  public async update() {
    this.creating = true;
    const res: ActionEmitterResult = {};
    res.type = ActionType.UPDATE_DYNAMIC_PROPERTIES;

    try {
      if (this.property && this.property.id) {
        this.property.updatedValues = this.values;
        this.property.title = this.editDynPropForm.value.title;
        await firstValueFrom(
          this.dynamicPropertiesService.putDynamicPropertyDefinition(
            this.property.id,
            this.property
          )
        );
        res.result = ActionResult.SUCCEED;
      }
    } catch (error) {
      res.result = ActionResult.FAILED;
    }

    this.creating = false;
    this.showModal = false;
    this.showDefinition = true;
    this.modalHide.emit(res);
  }

  public async loadDynamicProperty(id: string) {
    this.loading = true;
    this.originalProperty = await firstValueFrom(
      this.dynamicPropertiesService.getDynamicPropertyDefinition(id)
    );
    this.editDynPropForm.controls.title.setValue(this.originalProperty.title);
    this.editDynPropForm.controls.type.setValue(
      this.originalProperty.propertyType
    );

    this.initValues();

    this.loading = false;
  }

  public getTypes(): DynamicPropertyType[] {
    return DynamicPropertyTypes.getTypes();
  }

  public hasValues(): boolean {
    if (this.originalProperty) {
      return (
        this.originalProperty.propertyType === 'SELECTION' ||
        this.originalProperty.propertyType === 'MULTI_SELECTION'
      );
    }

    return false;
  }

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
        valEdit._new = value;
        valEdit.status = '';
        this.values.push(valEdit);
      });

      return this.values;
    }

    return [];
  }

  public addValue() {
    const valEdit: DynamicPropertyDefinitionUpdatedValues = {};
    valEdit.old = this.newValueForm.value.newValue;
    valEdit._new = this.newValueForm.value.newValue;
    valEdit.status = 'new';
    this.values.push(valEdit);
    this.newValueForm.reset();
  }

  public cancelAddValue() {
    this.newValueForm.reset();
    this.currentIndex = -1;
  }

  public newValueFormValid(): boolean {
    return this.newValueForm.valid;
  }

  public tagAsEdited(item: DynamicPropertyDefinitionUpdatedValues) {
    this.currentIndex = this.values.indexOf(item);
    this.newValueForm.controls.newValue.setValue(item._new);
  }

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

  public finishEdit() {
    this.values[this.currentIndex].status = 'edited';
    this.values[this.currentIndex]._new = this.newValueForm.value.newValue;
    this.newValueForm.reset();
    this.currentIndex = -1;
  }

  public cancelEdit(_item: DynamicPropertyDefinitionUpdatedValues) {
    this.newValueForm.reset();
    this.currentIndex = -1;
  }

  public resetEdited(item: DynamicPropertyDefinitionUpdatedValues) {
    const index = this.values.indexOf(item);
    this.newValueForm.reset();
    this.currentIndex = -1;
    this.values[index].status = '';
    this.values[index]._new = this.values[index].old;
  }

  public cancelWizard() {
    this.showModal = false;
    this.showDefinition = true;
    const result: ActionEmitterResult = {};
    if (this.property === undefined) {
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

  get propertyTypeControl(): AbstractControl {
    return this.createForm.controls.propertyType;
  }

  get titleControl(): AbstractControl {
    return this.createForm.controls.title;
  }
}
