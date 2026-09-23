import { DatePipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  input,
  resource,
  signal,
} from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import {
  ContentService,
  ExpiredService,
  InterestGroupService,
  Node as ModelNode,
} from 'app/core/generated/circabc';
import { UiMessageService } from 'app/core/message/ui-message.service';
import {
  changeSort,
  getErrorTranslation,
  getSuccessTranslation,
} from 'app/core/util';
import { ListingOptions } from 'app/group/listing-options/listing-options';
import { InlineDeleteComponent } from 'app/shared/delete/inline-delete.component';
import { NumberBadgeComponent } from 'app/shared/number-badge/number-badge.component';
import { PagerComponent } from 'app/shared/pager/pager.component';
import { PagerConfigurationComponent } from 'app/shared/pager-configuration/pager-configuration.component';
import { UserCardComponent } from 'app/shared/user-card/user-card.component';
import { UpdateExpiredDateComponent } from './update-expired-date/update-expired-date.component';

/**
 * Administration component that lists the expired documents and folders of an
 * interest group and lets administrators manage them.
 *
 * It renders a paginated, sortable table (via {@link PagerComponent} and
 * {@link PagerConfigurationComponent}) of expired nodes. For each node it shows
 * the type, modifier ({@link UserCardComponent}), expiration date and provides
 * actions to select nodes, delete them individually or in bulk
 * ({@link InlineDeleteComponent}) and update their expiration date
 * ({@link UpdateExpiredDateComponent}).
 *
 * Key collaborators:
 * - {@link InterestGroupService} to resolve the current interest group.
 * - {@link ExpiredService} to fetch the paged list of expired documents.
 * - {@link ContentService} to delete nodes.
 * - {@link UiMessageService} to surface success/error notifications.
 * - {@link TranslocoService} to translate notification messages.
 */
@Component({
  selector: 'cbc-expired-items',
  templateUrl: './expired-items.component.html',
  styleUrl: './expired-items.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    PagerComponent,
    PagerConfigurationComponent,
    NumberBadgeComponent,
    InlineDeleteComponent,
    RouterLink,
    UserCardComponent,
    UpdateExpiredDateComponent,
    DatePipe,
    TranslocoModule,
  ],
})
export class ExpiredItemsComponent {
  private readonly groupService = inject(InterestGroupService);
  private readonly uiMessageService = inject(UiMessageService);
  private readonly translateService = inject(TranslocoService);
  private readonly expiredService = inject(ExpiredService);
  private readonly contentService = inject(ContentService);

  /**
   * Required input carrying the identifier of the interest group whose expired
   * items should be displayed and managed.
   */
  readonly groupId = input.required<string>();

  /**
   * Current listing state (page, page size and sort criterion) used when
   * querying the expired documents endpoint. Defaults to the first page of 10
   * items sorted by descending expiration date.
   */
  public listingOptions = signal<ListingOptions>({
    page: 1,
    limit: 10,
    sort: 'expirationDate_DESC',
  });

  /**
   * Loads the current interest group together with the current page of its
   * expired documents according to {@link listingOptions}. Idle while
   * {@link groupId} is empty.
   */
  private readonly igResource = resource({
    params: () => {
      const groupId = this.groupId();
      return groupId
        ? { groupId, listingOptions: this.listingOptions() }
        : undefined;
    },
    loader: async ({ params }) => {
      const { groupId, listingOptions } = params;
      const currentIg = await this.groupService.getInterestGroupAsync({
        id: groupId,
      });
      const expiredNodes = await this.expiredService.getExpiredDocumentsAsync({
        id: groupId,
        limit: listingOptions.limit,
        page: listingOptions.page,
        order: listingOptions.sort,
      });
      return { currentIg, expiredNodes };
    },
  });

  /** The interest group resolved from {@link groupId}. */
  public readonly currentIg = computed(
    () => this.igResource.value()?.currentIg
  );
  /** The current page of expired nodes returned by the backend. */
  public readonly expiredNodes = computed(
    () => this.igResource.value()?.expiredNodes ?? { data: [], total: 0 }
  );
  /** Total number of expired items, used to configure the pager. */
  public readonly totalItems = computed(() => this.expiredNodes().total || 10);
  /** Whether an asynchronous load/refresh is in progress. */
  public readonly loading = this.igResource.isLoading;
  /** Nodes currently selected for deletion. */
  public deletableNodes: ModelNode[] = [];
  /** Whether every node on the current page is selected. */
  public allSelected = false;
  /** Whether a bulk deletion is in progress. */
  public deleting = signal(false);
  /** Whether the bulk deletion confirmation state is active. */
  public mustConfirm = signal(false);
  /** Whether the modal for updating a node's expiration date is visible. */
  public showUpdateExpirateDateModal = false;
  /** The node targeted by the update-expiration-date modal. */
  public nodeSelected!: ModelNode;

  /**
   * Navigates to the given page of expired items.
   *
   * @param page The 1-based page number to load.
   */
  public goToPage(page: number) {
    this.changePage({ ...this.listingOptions(), page });
  }

  /**
   * Reloads the expired documents using the supplied listing options and
   * refreshes {@link totalItems}.
   *
   * @param listingOptions The page, limit and sort criteria to apply.
   */
  public changePage(listingOptions: ListingOptions) {
    this.listingOptions.set(listingOptions);
  }

  /**
   * Toggles the sort direction for the given column and reloads the list.
   *
   * @param sort The name of the column/field to sort by.
   */
  public changeSort(sort: string) {
    this.changePage({
      ...this.listingOptions(),
      sort: changeSort(this.listingOptions().sort, sort),
    });
  }

  /**
   * Changes the number of items displayed per page, resets to the first page
   * and reloads the list.
   *
   * @param limit The new page size.
   */
  public changeLimit(limit: number) {
    this.changePage({ ...this.listingOptions(), limit, page: 1 });
  }

  /**
   * Determines whether the given node is a file (i.e. not a folder).
   *
   * @param node The node to inspect.
   * @returns `true` if the node has a type that is not a folder, otherwise
   * `false`.
   */
  public isFile(node: ModelNode): boolean {
    if (node.type) {
      return !node.type.includes('folder');
    }
    return false;
  }

  /**
   * Determines whether the given node is a folder.
   *
   * @param node The node to inspect.
   * @returns `true` if the node's type indicates a folder, otherwise `false`.
   */
  public isFolder(node: ModelNode): boolean {
    if (node.type) {
      return node.type.includes('folder');
    }
    return false;
  }

  /**
   * Determines whether the given node represents an external link (an HTML
   * node carrying a non-empty URL).
   *
   * @param node The node to inspect.
   * @returns `true` if the node has an `text/html` mimetype and a non-empty
   * URL, otherwise `false`.
   */
  public isLink(node: ModelNode): boolean {
    if (node.properties?.mimetype && node.properties.url) {
      return (
        node.properties.mimetype === 'text/html' && node.properties.url !== ''
      );
    }
    return false;
  }

  /**
   * Toggles the selection of a node for deletion, adding it to
   * {@link deletableNodes} if absent or removing it if already present.
   *
   * @param node The node whose selection state should be toggled.
   */
  public prepareNode(node: ModelNode) {
    const idx = this.deletableNodes.indexOf(node);
    if (idx === -1) {
      this.deletableNodes.push(node);
    } else {
      this.deletableNodes.splice(idx, 1);
    }
  }

  /**
   * Indicates whether the given node is currently selected for deletion.
   *
   * @param node The node to check.
   * @returns `true` if the node is in {@link deletableNodes}, otherwise
   * `false`.
   */
  public isPrepared(node: ModelNode): boolean {
    return this.deletableNodes.includes(node);
  }

  /**
   * Deletes all nodes currently selected in {@link deletableNodes}. Displays a
   * success or error message depending on the outcome and refreshes the list.
   * Individual deletion failures are caught and reported collectively.
   *
   * @returns A promise that resolves once all deletions have been attempted
   * and the list has been refreshed.
   */
  public async deleteNodes() {
    let isError = false;

    this.deleting.set(true);
    this.mustConfirm.set(true);

    for (const node of this.deletableNodes) {
      if (node.id) {
        try {
          await this.contentService.deleteContentAsync({ id: node.id });
        } catch (error) {
          console.error(error);
          isError = true;
        }
      }
    }

    if (isError) {
      const txt = this.translateService.translate(
        getErrorTranslation(ActionType.DELETE_CONTENT)
      );
      this.uiMessageService.addErrorMessage(txt);
    } else if (!isError && this.deletableNodes.length !== 0) {
      const txt = this.translateService.translate(
        getSuccessTranslation(ActionType.DELETE_CONTENT)
      );
      this.uiMessageService.addSuccessMessage(txt, true);
    }

    this.igResource.reload();

    this.deleting.set(false);
    this.mustConfirm.set(false);
  }

  /**
   * Deletes a single node, showing a success or error message and refreshing
   * the list on success.
   *
   * @param node The node to delete; ignored if it has no identifier.
   * @returns A promise that resolves once the deletion has been attempted.
   */
  public async deleteNode(node: ModelNode) {
    if (node.id) {
      try {
        await this.contentService.deleteContentAsync({ id: node.id });
        const txt = this.translateService.translate(
          getSuccessTranslation(ActionType.DELETE_CONTENT)
        );
        this.uiMessageService.addSuccessMessage(txt, true);
        this.igResource.reload();
      } catch (error) {
        console.error(error);
        const txt = this.translateService.translate(
          getErrorTranslation(ActionType.DELETE_CONTENT)
        );
        this.uiMessageService.addErrorMessage(txt);
      }
    }
  }

  /**
   * Toggles selection of every node on the current page. When enabling, all
   * nodes of the current page are added to {@link deletableNodes}; when
   * disabling, the selection is cleared.
   */
  public toggleAllPrepared() {
    this.allSelected = !this.allSelected;
    if (this.expiredNodes()?.data && this.allSelected) {
      for (const delNode of this.expiredNodes().data) {
        this.deletableNodes.push(delNode);
      }
    } else if (!this.allSelected) {
      this.deletableNodes = [];
    }
  }

  /**
   * Handles a deletion event emitted by a child component. Shows the relevant
   * success message for deleted spaces or content and refreshes the list.
   *
   * @param result The action result describing what was deleted and its
   * outcome.
   */
  public onDeletedElement(result: ActionEmitterResult) {
    if (
      result.result === ActionResult.SUCCEED &&
      result.type === ActionType.DELETE_SPACE
    ) {
      const text = this.translateService.translate(
        getSuccessTranslation(ActionType.DELETE_SPACE)
      );
      if (text) {
        this.uiMessageService.addSuccessMessage(text, true);
      }
    } else if (
      result.result === ActionResult.SUCCEED &&
      result.type === ActionType.DELETE_CONTENT
    ) {
      const text = this.translateService.translate(
        getSuccessTranslation(ActionType.DELETE_CONTENT)
      );
      if (text) {
        this.uiMessageService.addSuccessMessage(text, true);
      }
    }
    this.igResource.reload();
  }

  /**
   * Returns the identifier of the user who last modified the node.
   *
   * @param node The node to inspect.
   * @returns The modifier, or an empty string if the node has no properties.
   */
  public getModifier(node: ModelNode): string {
    if (node.properties) {
      return node.properties.modifier;
    }
    return '';
  }

  /**
   * Returns the expiration date of the given node.
   *
   * @param node The node to inspect.
   * @returns The expiration date string, or `null` if the node has no
   * properties.
   */
  public getExpirationDate(node: ModelNode): string | null {
    if (node.properties) {
      return node.properties.expiration_date;
    }
    return null;
  }

  /**
   * Selects a node and opens the modal used to update its expiration date.
   *
   * @param node The node whose expiration date should be updated.
   */
  public updateExpiredDate(node: ModelNode) {
    this.nodeSelected = node;
    this.showUpdateExpirateDateModal = true;
  }

  /**
   * Reloads the interest group and its expired items, typically after an
   * expiration date has been updated.
   */
  public refreshExpiredItemsList() {
    this.igResource.reload();
  }
}
