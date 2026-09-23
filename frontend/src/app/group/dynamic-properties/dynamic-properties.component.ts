import {
  ChangeDetectionStrategy,
  Component,
  inject,
  OnInit,
  signal,
} from '@angular/core';
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
import { LoadingService } from 'app/core/loading.service';
import { HorizontalLoaderComponent } from 'app/shared/loader/horizontal-loader.component';
import { ReponsiveSubMenuComponent } from 'app/shared/reponsive-sub-menu/reponsive-sub-menu.component';
import { DynamicPropertyBoxComponent } from './box/dynamic-property-box.component';
import { CreateDynamicPropertyComponent } from './create/create-dynamic-property.component';
import { DynamicPropertyDeleteComponent } from './delete/dynamic-property-delete.component';

/**
 * Standalone Angular component that renders the management view for an interest
 * group's dynamic property definitions.
 *
 * The component lists the dynamic properties configured for a group (each shown
 * via {@link DynamicPropertyBoxComponent}), and hosts the modals used to create,
 * edit ({@link CreateDynamicPropertyComponent}) and delete
 * ({@link DynamicPropertyDeleteComponent}) those definitions. It resolves the
 * current group id from the active route, loads the group's dynamic property
 * definitions, the {@link InterestGroup} and its library {@link ModelNode}, and
 * exposes helpers to gate administrative actions based on library-admin
 * permissions.
 *
 * Key collaborators: {@link DynamicPropertiesService} (fetch definitions),
 * {@link InterestGroupService} (load the group), {@link NodesService} (load the
 * library node), {@link PermissionEvaluatorService} (permission checks) and the
 * {@link ActivatedRoute} (group id resolution).
 */
@Component({
  selector: 'cbc-dynamic-properties',
  templateUrl: './dynamic-properties.component.html',
  styleUrl: './dynamic-properties.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
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
  private readonly dynamicPropertiesService = inject(DynamicPropertiesService);
  private readonly route = inject(ActivatedRoute);
  private readonly permEvalService = inject(PermissionEvaluatorService);
  private readonly groupService = inject(InterestGroupService);
  private readonly nodesService = inject(NodesService);
  private readonly loadingService = inject(LoadingService);

  /** Identifier of the interest group whose dynamic properties are managed. */
  public readonly groupId = signal('');
  /** Dynamic property definitions currently configured for the group. */
  public readonly dynamicProperties = signal<DynamicPropertyDefinition[]>([]);
  /** Whether the create/edit dynamic property modal is visible. */
  public readonly createModalShown = signal(false);
  /** Whether the delete-confirmation modal is visible. */
  public readonly deleteModalShown = signal(false);
  /** Property targeted by the delete modal, if any. */
  public readonly selectedProperty = signal<
    DynamicPropertyDefinition | undefined
  >(undefined);
  /** Property pre-filled into the create/edit modal when editing, if any. */
  public readonly propertyToUpdate = signal<
    DynamicPropertyDefinition | undefined
  >(undefined);
  /** Whether an asynchronous load is in progress (drives the loader UI). */
  public readonly loading = signal(false);
  /** The interest group resolved for {@link groupId}. */
  public currentIg!: InterestGroup;
  /** The library node of {@link currentIg}, used for permission checks. */
  public readonly currentLibrary = signal<ModelNode | undefined>(undefined);

  /**
   * Angular lifecycle hook. Subscribes to route parameter changes and (re)loads
   * the dynamic properties for the group identified by the current route.
   */
  ngOnInit() {
    this.route.params.subscribe(async (params) => {
      await this.loadDynamicProperties(params);
    });
  }

  /**
   * Loads the group's dynamic property definitions along with the interest group
   * and its library node, toggling {@link loading} around the asynchronous work.
   *
   * @param params - Route parameters expected to contain the group `id`.
   * @returns A promise that resolves once all data has been loaded.
   */
  private async loadDynamicProperties(params: { [key: string]: string }) {
    this.groupId.set(params.id);
    if (!this.groupId()) {
      return;
    }
    await this.loadingService.run(this.loading, async () => {
      this.dynamicProperties.set(
        await this.dynamicPropertiesService.getDynamicPropertyDefinitionsAsync({
          id: this.groupId(),
        })
      );

      this.currentIg = await this.groupService.getInterestGroupAsync({
        id: this.groupId(),
      });

      if (this.currentIg.libraryId) {
        this.currentLibrary.set(
          await this.nodesService.getNodeAsync({ id: this.currentIg.libraryId })
        );
      }
    });
  }

  /**
   * Reacts to results emitted by the create, edit and delete child components.
   *
   * When a dynamic property is successfully created, updated or deleted the
   * definitions are reloaded. Regardless of the outcome, all modals are closed
   * and their associated selections are reset.
   *
   * @param result - The action result reported by a child component.
   * @returns A promise that resolves once any triggered reload has completed.
   */
  public async refresh(result: ActionEmitterResult) {
    if (
      result.type === ActionType.DELETE_DYNAMIC_PROPERTY &&
      result.result === ActionResult.SUCCEED
    ) {
      await this.loadDynamicProperties({ id: this.groupId() });
    }
    if (
      result.type === ActionType.CREATE_DYNAMIC_PROPERTY &&
      result.result === ActionResult.SUCCEED
    ) {
      await this.loadDynamicProperties({ id: this.groupId() });
    }
    if (
      result.type === ActionType.UPDATE_DYNAMIC_PROPERTIES &&
      result.result === ActionResult.SUCCEED
    ) {
      await this.loadDynamicProperties({ id: this.groupId() });
    }
    this.deleteModalShown.set(false);
    this.selectedProperty.set(undefined);
    this.createModalShown.set(false);
    this.propertyToUpdate.set(undefined);
  }

  /**
   * Opens the delete-confirmation modal for the given property.
   *
   * @param property - The dynamic property definition to be deleted.
   */
  public showModalDelete(property: DynamicPropertyDefinition) {
    this.deleteModalShown.set(true);
    this.selectedProperty.set(property);
  }

  /**
   * Opens the create/edit modal pre-filled with the given property for editing.
   *
   * @param property - The dynamic property definition to edit.
   */
  public showModalEdit(property: DynamicPropertyDefinition) {
    this.createModalShown.set(true);
    this.propertyToUpdate.set(property);
  }

  /**
   * Indicates whether the current user is an administrator of the group's
   * library, which governs access to the management actions.
   *
   * @returns `true` if the library node is loaded and the user is a library
   * administrator; otherwise `false`.
   */
  public isLibAdmin(): boolean {
    const library = this.currentLibrary();
    if (library) {
      return this.permEvalService.isLibAdmin(library);
    }
    return false;
  }

  /**
   * Opens the create modal, provided the group has fewer than the maximum of 20
   * dynamic property definitions. Does nothing when the limit is reached.
   */
  public prepareCreateModal() {
    if (this.dynamicProperties().length < 20) {
      this.createModalShown.set(true);
    }
  }
}
