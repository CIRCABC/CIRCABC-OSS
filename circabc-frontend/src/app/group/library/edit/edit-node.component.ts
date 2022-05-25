import { Component, OnInit } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormControl,
  FormGroup,
  Validators,
} from '@angular/forms';

import { ActivatedRoute, Router } from '@angular/router';

import { TranslocoService } from '@ngneat/transloco';

import {
  ContentService,
  DynamicPropertiesService,
  DynamicPropertyDefinition,
  Node as ModelNode,
  NodesService,
  SpaceService,
} from 'app/core/generated/circabc';
import { ValidationService } from 'app/core/validation.service';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-edit-node',
  templateUrl: './edit-node.component.html',
  styleUrls: ['./edit-node.component.scss'],
  preserveWhitespaces: true,
})
export class EditNodeComponent implements OnInit {
  public node!: ModelNode;
  public editNodeForm!: FormGroup;
  public dynamicPropertiesModel: DynamicPropertyDefinition[] = [];

  public selectedTab = 'GeneralInformation';

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private nodesService: NodesService,
    private spaceService: SpaceService,
    private contentService: ContentService,
    private translateService: TranslocoService,
    private dynamicPropertiesService: DynamicPropertiesService
  ) {}

  public ngOnInit() {
    this.editNodeForm = this.fb.group(
      {
        name: ['', [Validators.required, ValidationService.fileNameValidator]],
        title: [{ EN: '' }],
        description: [{ EN: '' }],
        author: [],
        url: [],
        status: [],
        encoding: [],
        mimetype: [],
        reference: [],
        security: [],
        issueDate: [],
        expirationDate: [],
        expirationDateActived: [],
      },
      {
        updateOn: 'change',
      }
    );
    this.route.params.subscribe(async (params) => {
      await this.loadDynamicPropertiesModel(params.id);
      await this.loadNode(params.nodeId);
    });
  }

  public async loadDynamicPropertiesModel(groupId: string) {
    if (groupId) {
      this.dynamicPropertiesModel = await firstValueFrom(
        this.dynamicPropertiesService.getDynamicPropertyDefinitions(groupId)
      );

      for (const dynprop of this.dynamicPropertiesModel) {
        this.editNodeForm.addControl(this.getName(dynprop), new FormControl());
      }
    }
  }

  public async loadNode(nodeId: string) {
    this.node = await firstValueFrom(this.nodesService.getNode(nodeId));
    const tmpDynProps = this.dynamicPropertiesModel;

    if (this.node.properties !== undefined) {
      const props = this.node.properties;

      this.editNodeForm.controls.name.setValue(this.node.name);
      this.editNodeForm.controls.title.setValue(this.node.title);
      this.editNodeForm.controls.description.setValue(this.node.description);
      this.editNodeForm.controls.author.setValue(
        props.author ? props.author : ''
      );
      if (this.isLink()) {
        this.editNodeForm.controls.url.setValue(props.url ? props.url : '');
      }
      this.editNodeForm.controls.status.setValue(props.status);
      this.editNodeForm.controls.encoding.setValue(props.encoding);
      this.editNodeForm.controls.mimetype.setValue(props.mimetype);
      this.editNodeForm.controls.reference.setValue(
        props.reference ? props.reference : ''
      );
      this.editNodeForm.controls.security.setValue(props.security_ranking);
      this.editNodeForm.controls.issueDate.setValue(
        props.issue_date ? new Date(props.issue_date) : undefined
      );
      this.editNodeForm.controls.expirationDate.setValue(
        props.expiration_date ? new Date(props.expiration_date) : undefined
      );

      if (
        this.editNodeForm.value.expirationDate === '' ||
        this.editNodeForm.value.expirationDate === undefined
      ) {
        this.editNodeForm.controls.expirationDateActived.setValue(false);
      } else {
        this.editNodeForm.controls.expirationDateActived.setValue(true);
      }

      if (this.node.type && this.node.type.indexOf('folder') === -1) {
        for (const dynprop of tmpDynProps) {
          const controlName = this.getName(dynprop);
          if (this.isSelectionOrMultiSelection(dynprop)) {
            if (this.isSelection(dynprop)) {
              this.editNodeForm.controls[controlName].setValue(
                props[controlName]
              );
            } else {
              const array = this.convertToArray(props[controlName]);
              this.editNodeForm.controls[controlName].setValue(array);
            }
          } else if (this.isDateField(dynprop)) {
            if (props[controlName]) {
              const date = props[controlName]
                ? new Date(props[controlName])
                : undefined;
              this.editNodeForm.controls[controlName].setValue(date);
            }
          } else {
            this.editNodeForm.controls[controlName].setValue(
              props[controlName] === undefined || props[controlName] === ''
                ? ''
                : props[controlName]
            );
          }
        }
      }
    }
  }

  private convertToArray(value: string): string[] {
    const res = [];

    if (value !== undefined && value !== '') {
      const parts = value.split(', ');
      for (const part of parts) {
        res.push(part);
      }
    }
    return res;
  }

  public isFile(): boolean {
    let result = false;
    if (this.node !== undefined && this.node.type !== undefined) {
      result = this.node.type.indexOf('folder') === -1;
    }
    return result;
  }

  public isLink(): boolean {
    if (this.node !== undefined && this.node.type && this.node.properties) {
      return (
        this.node.properties.mimetype === 'text/html' &&
        this.node.properties.url !== ''
      );
    } else {
      return false;
    }
  }

  public get currentLanguage(): string {
    return this.translateService.getActiveLang();
  }

  public async updateProperties() {
    const tmpNode: ModelNode = {
      id: this.node.id,
      name: this.editNodeForm.value.name,
      title: this.editNodeForm.value.title,
      description: this.editNodeForm.value.description,
      properties: {},
    };
    const isFile = this.isFile();

    if (!isFile) {
      tmpNode.properties = {
        expiration_date:
          this.editNodeForm.value.expirationDate === undefined ||
          this.editNodeForm.value.expirationDate === '' ||
          !this.editNodeForm.value.expirationDateActived
            ? 'null'
            : this.editNodeForm.value.expirationDate,
      };
    } else {
      tmpNode.properties = {
        expiration_date:
          this.editNodeForm.value.expirationDate === undefined ||
          this.editNodeForm.value.expirationDate === '' ||
          !this.editNodeForm.value.expirationDateActived
            ? 'null'
            : this.editNodeForm.value.expirationDate,
        issue_date:
          this.editNodeForm.value.issueDate === undefined
            ? ''
            : this.editNodeForm.value.issueDate,
        encoding:
          this.editNodeForm.value.encoding === undefined
            ? ''
            : this.editNodeForm.value.encoding,
        mimetype:
          this.editNodeForm.value.mimetype === undefined
            ? ''
            : this.editNodeForm.value.mimetype.mimetype,
        reference:
          this.editNodeForm.value.reference === undefined
            ? ''
            : this.editNodeForm.value.reference,
        author:
          this.editNodeForm.value.author === undefined
            ? ''
            : this.editNodeForm.value.author,
        url:
          this.editNodeForm.value.url === undefined
            ? ''
            : this.editNodeForm.value.url,
        status:
          this.editNodeForm.value.status === undefined
            ? 'DRAFT'
            : this.editNodeForm.value.status,
        security:
          this.editNodeForm.value.security === undefined
            ? ''
            : this.editNodeForm.value.security,
      };

      if (this.node.type && this.node.type.indexOf('folder') === -1) {
        for (const dynprop of this.dynamicPropertiesModel) {
          const controlName = this.getName(dynprop);
          if (this.editNodeForm.value[controlName] === null) {
            tmpNode.properties[controlName] = '';
          } else {
            let propValue = this.editNodeForm.value[controlName];
            if (Array.isArray(propValue)) {
              propValue = propValue.join(', ');
            }
            tmpNode.properties[controlName] = propValue;
          }
        }
      }
    }

    if (isFile) {
      await firstValueFrom(
        this.contentService.putContent(this.node.id as string, tmpNode)
      );
    } else {
      await firstValueFrom(
        this.spaceService.putSpace(this.node.id as string, tmpNode)
      );
    }

    // eslint-disable-next-line @typescript-eslint/no-floating-promises
    this.router.navigate(['../details'], { relativeTo: this.route });
  }

  public isGeneralTab(): boolean {
    return this.selectedTab === 'GeneralInformation';
  }

  public isDetailsTab(): boolean {
    return this.selectedTab === 'Details';
  }

  public isDynamicPropertiesTab(): boolean {
    return this.selectedTab === 'DynamicProperties';
  }

  public setTab(tab: string) {
    this.selectedTab = tab;
  }

  get nameControl(): AbstractControl {
    return this.editNodeForm.controls.name;
  }

  get authorControl(): AbstractControl {
    return this.editNodeForm.controls.author;
  }

  get urlControl(): AbstractControl {
    return this.editNodeForm.controls.url;
  }

  public isDateField(dpd: DynamicPropertyDefinition): boolean {
    return dpd.propertyType === 'DATE_FIELD';
  }

  public isTextField(dpd: DynamicPropertyDefinition): boolean {
    return dpd.propertyType === 'TEXT_FIELD';
  }

  public isTextArea(dpd: DynamicPropertyDefinition): boolean {
    return dpd.propertyType === 'TEXT_AREA';
  }

  public isSelectionOrMultiSelection(dpd: DynamicPropertyDefinition): boolean {
    return (
      dpd.propertyType === 'SELECTION' || dpd.propertyType === 'MULTI_SELECTION'
    );
  }

  public isMultiSelection(dpd: DynamicPropertyDefinition): boolean {
    return dpd.propertyType === 'MULTI_SELECTION';
  }

  public isSelection(dpd: DynamicPropertyDefinition): boolean {
    return dpd.propertyType === 'SELECTION';
  }

  public getIndex(dpd: DynamicPropertyDefinition): number {
    return dpd.index as number;
  }

  private getName(dynprop: DynamicPropertyDefinition): string {
    return `dynAttr${dynprop.index}`;
  }

  public isSelectedOption(
    prop: string,
    dynprop: DynamicPropertyDefinition
  ): boolean {
    const controlValue = this.editNodeForm.value[`dynAttr${dynprop.index}`];
    if (controlValue) {
      return controlValue.indexOf(prop) !== -1;
    } else {
      return false;
    }
  }

  public compareFn(optionOne?: string, optionTwo?: string): boolean {
    if (optionOne && optionTwo) {
      return optionOne === optionTwo;
    } else {
      return false;
    }
  }

  public isExpired() {
    return this.editNodeForm.value.expirationDate < Date.now();
  }
}
