import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { TranslocoModule } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import {
  DynamicPropertiesService,
  DynamicPropertyDefinition,
  InterestGroup,
  InterestGroupService,
  Node as ModelNode,
  NodesService,
} from 'app/core/generated/circabc';
import { HorizontalLoaderComponent } from 'app/shared/loader/horizontal-loader.component';
import { ReponsiveSubMenuComponent } from 'app/shared/reponsive-sub-menu/reponsive-sub-menu.component';
import { firstValueFrom } from 'rxjs';
import { DynamicPropertyBoxComponent } from './box/dynamic-property-box.component';
import { CreateDynamicPropertyComponent } from './create/create-dynamic-property.component';
import { DynamicPropertyDeleteComponent } from './delete/dynamic-property-delete.component';

@Component({
  selector: 'cbc-dynamic-properties',
  templateUrl: './dynamic-properties.component.html',
  styleUrl: './dynamic-properties.component.scss',
  preserveWhitespaces: true,
  imports: [
    HorizontalLoaderComponent,
    ReponsiveSubMenuComponent,
    RouterLink,
    DynamicPropertyBoxComponent,
    DynamicPropertyDeleteComponent,
    CreateDynamicPropertyComponent,
    TranslocoModule,
  ],
})
export class DynamicPropertiesComponent implements OnInit {
  public groupId!: string;
  public dynamicProperties: DynamicPropertyDefinition[] = [];
  public createModalShown = false;
  public deleteModalShown = false;
  public selectedProperty: DynamicPropertyDefinition | undefined;
  public propertyToUpdate: DynamicPropertyDefinition | undefined;
  public loading = false;
  public currentIg!: InterestGroup;
  public currentLibrary!: ModelNode;

  constructor(
    private dynamicPropertiesService: DynamicPropertiesService,
    private route: ActivatedRoute,
    private permEvalService: PermissionEvaluatorService,
    private groupService: InterestGroupService,
    private nodesService: NodesService
  ) {}

  ngOnInit() {
    this.route.params.subscribe(async (params) => {
      await this.loadDynamicProperties(params);
    });
  }

  private async loadDynamicProperties(params: { [key: string]: string }) {
    this.loading = true;
    this.groupId = params.id;

    if (this.groupId) {
      this.dynamicProperties = await firstValueFrom(
        this.dynamicPropertiesService.getDynamicPropertyDefinitions(
          this.groupId
        )
      );

      this.currentIg = await firstValueFrom(
        this.groupService.getInterestGroup(this.groupId)
      );

      if (this.currentIg.libraryId) {
        this.currentLibrary = await firstValueFrom(
          this.nodesService.getNode(this.currentIg.libraryId)
        );
      }
    }

    this.loading = false;
  }

  public async refresh(result: ActionEmitterResult) {
    if (
      result.type === ActionType.DELETE_DYNAMIC_PROPERTY &&
      result.result === ActionResult.SUCCEED
    ) {
      await this.loadDynamicProperties({ id: this.groupId });
    }
    if (
      result.type === ActionType.CREATE_DYNAMIC_PROPERTY &&
      result.result === ActionResult.SUCCEED
    ) {
      await this.loadDynamicProperties({ id: this.groupId });
    }
    if (
      result.type === ActionType.UPDATE_DYNAMIC_PROPERTIES &&
      result.result === ActionResult.SUCCEED
    ) {
      await this.loadDynamicProperties({ id: this.groupId });
    }
    this.deleteModalShown = false;
    this.selectedProperty = undefined;
    this.createModalShown = false;
    this.propertyToUpdate = undefined;
  }

  public showModalDelete(property: DynamicPropertyDefinition) {
    this.deleteModalShown = true;
    this.selectedProperty = property;
  }

  public showModalEdit(property: DynamicPropertyDefinition) {
    this.createModalShown = true;
    this.propertyToUpdate = property;
  }

  public isLibAdmin(): boolean {
    if (this.currentLibrary) {
      return this.permEvalService.isLibAdmin(this.currentLibrary);
    }
    return false;
  }

  public prepareCreateModal() {
    if (this.dynamicProperties.length < 20) {
      this.createModalShown = true;
    }
  }
}
