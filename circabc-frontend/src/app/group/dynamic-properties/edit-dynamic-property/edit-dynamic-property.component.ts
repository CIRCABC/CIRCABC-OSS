import { Location } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
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
import { DynamicPropertyType } from 'app/group/dynamic-properties/type/dynamic-property-type';
import { DynamicPropertyTypes } from 'app/group/dynamic-properties/type/dynamic-property-types';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-edit-dynamic-property',
  templateUrl: './edit-dynamic-property.component.html',
  styleUrls: ['./edit-dynamic-property.component.scss'],
  preserveWhitespaces: true,
})
export class EditDynamicPropertyComponent implements OnInit {
  public loading = false;
  public executing = false;
  public showDefinition = true;
  public editDynPropForm!: FormGroup;
  public newValueForm!: FormGroup;
  public originalProperty!: DynamicPropertyDefinition;
  public values: DynamicPropertyDefinitionUpdatedValues[] = [];
  public currentIndex = -1;

  constructor(
    private fb: FormBuilder,
    private dynamicPropertiesService: DynamicPropertiesService,
    private route: ActivatedRoute,
    private location: Location
  ) {}

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

    if (this.route && this.route.params) {
      this.route.params.subscribe(async (params) => {
        if (params.dpId) {
          await this.loadDynamicProperty(params.dpId);
        }
      });
    }
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

  public async ok() {
    this.executing = true;
    const res: ActionEmitterResult = {};
    res.type = ActionType.UPDATE_DYNAMIC_PROPERTIES;

    try {
      if (this.originalProperty && this.originalProperty.id) {
        this.originalProperty.updatedValues = this.values;
        this.originalProperty.title = this.editDynPropForm.value.title;
        await firstValueFrom(
          this.dynamicPropertiesService.putDynamicPropertyDefinition(
            this.originalProperty.id,
            this.originalProperty
          )
        );
        res.result = ActionResult.SUCCEED;
      }
    } catch (error) {
      res.result = ActionResult.FAILED;
    }

    if (res.result === ActionResult.SUCCEED) {
      this.location.back();
    }
    this.executing = false;
  }

  public cancel() {
    this.location.back();
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
}
