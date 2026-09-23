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
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import {
  ArchiveNode,
  ArchiveService,
  InterestGroup,
  InterestGroupService,
  Node as ModelNode,
} from 'app/core/generated/circabc';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { changeSort } from 'app/core/util';
import { PurgeItemComponent } from 'app/group/admin/documents/purge-item/purge-item.component';
import { RestoreItemComponent } from 'app/group/admin/documents/restore-item/restore-item.component';
import { InlineDeleteComponent } from 'app/shared/delete/inline-delete.component';
import { NumberBadgeComponent } from 'app/shared/number-badge/number-badge.component';
import { PagerComponent } from 'app/shared/pager/pager.component';
import { PagerConfigurationComponent } from 'app/shared/pager-configuration/pager-configuration.component';
import { UserCardComponent } from 'app/shared/user-card/user-card.component';

/**
 * Standalone Angular component (`cbc-deleted-items`) that renders the
 * administration view of an interest group's recycle bin (archived / deleted
 * documents).
 *
 * It displays a paginated, sortable listing of the deleted nodes (files,
 * folders and links) for a given interest group and lets administrators:
 * - restore one or several archived nodes back into the library, and
 * - permanently purge (definitively delete) one or several archived nodes.
 *
 * The listing is fetched through {@link ArchiveService}, the owning group is
 * resolved with {@link InterestGroupService}, user feedback is surfaced via
 * {@link UiMessageService}, and labels are localized with the Transloco
 * {@link TranslocoService}. Restore and purge confirmations are delegated to
 * the {@link RestoreItemComponent} and {@link PurgeItemComponent} child
 * modals respectively.
 */
@Component({
  selector: 'cbc-deleted-items',
  templateUrl: './deleted-items.component.html',
  styleUrl: './deleted-items.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    PagerComponent,
    PagerConfigurationComponent,
    NumberBadgeComponent,
    UserCardComponent,
    InlineDeleteComponent,
    RestoreItemComponent,
    PurgeItemComponent,
    DatePipe,
    TranslocoModule,
  ],
})
export class DeletedItemsComponent {
  /** API client used to list, restore and permanently delete archived documents. */
  private readonly archiveService = inject(ArchiveService);
  /** API client used to resolve the interest group that owns the recycle bin. */
  private readonly groupService = inject(InterestGroupService);
  /** Transloco service used to translate success/error feedback messages. */
  private readonly translateService = inject(TranslocoService);
  /** Service used to display success and error notifications to the user. */
  private readonly uiMessageService = inject(UiMessageService);

  /**
   * Required input carrying the identifier of the interest group whose deleted
   * items should be displayed. Drives the initial load and every subsequent
   * archive query.
   */
  readonly groupId = input.required<string>();

  /** Current 1-based page number of the archive listing. */
  public readonly page = signal(1);
  /** Current page size of the archive listing. */
  public readonly limit = signal(10);
  /** Current sort expression of the archive listing (defaults to newest deletion first). */
  public readonly sort = signal('archivedDate_DESC');

  /**
   * Resource resolving the interest group identified by {@link groupId}; owner
   * of the archived nodes.
   */
  private readonly igResource = resource({
    params: () => this.groupId() || undefined,
    loader: async ({ params: groupId }) => {
      try {
        return await this.groupService.getInterestGroupAsync({ id: groupId });
      } catch (error) {
        console.error(error);
        return undefined;
      }
    },
  });
  /** The interest group resolved from {@link groupId}; owner of the archived nodes. */
  public readonly currentIg = computed(
    () => this.igResource.value() ?? ({} as InterestGroup)
  );

  /**
   * Resource loading the current page of deleted nodes for {@link groupId},
   * re-fetched whenever the group, page, limit or sort change.
   */
  private readonly deletedNodesResource = resource({
    params: () => {
      const groupId = this.groupId();
      return groupId
        ? {
            groupId,
            page: this.page(),
            limit: this.limit(),
            sort: this.sort(),
          }
        : undefined;
    },
    loader: async ({ params }) => {
      try {
        return await this.archiveService.getDeletedDocumentsAsync({
          id: params.groupId,
          limit: params.limit,
          page: params.page,
          order: params.sort,
        });
      } catch (error) {
        console.error(error);
        return { data: [], total: 0 };
      }
    },
    defaultValue: { data: [], total: 0 },
  });
  /** The current page of deleted nodes together with the overall total. */
  public readonly deletedNodes = this.deletedNodesResource.value;
  /** Whether an archive request is currently in flight (drives loading UI). */
  public readonly loading = this.deletedNodesResource.isLoading;
  /** Total number of deleted items across all pages, used to configure the pager. */
  public readonly totalItems = computed(() => this.deletedNodes().total || 10);

  /** Whether the restore confirmation modal is currently visible. */
  public readonly showModal = signal(false);
  /** Whether the purge confirmation modal is currently visible. */
  public readonly showPurgeModal = signal(false);
  /** Nodes currently selected for a restore or purge operation. */
  public readonly restorableNodes = signal<ArchiveNode[]>([]);
  /** Whether the "select all" toggle for the current page is active. */
  public allSelected = false;

  /**
   * Navigates the listing to the given page.
   *
   * @param page The 1-based page number to display.
   */
  public goToPage(page: number) {
    this.page.set(page);
  }

  /**
   * Toggles/applies sorting on the given column.
   *
   * @param sort The sort key/column to apply (its direction is toggled via {@link changeSort}).
   */
  public changeSort(sort: string) {
    this.sort.set(changeSort(this.sort(), sort));
  }

  /**
   * Changes the page size and resets to the first page.
   *
   * @param limit The new number of items to show per page.
   */
  public changeLimit(limit: number) {
    this.limit.set(limit);
    this.page.set(1);
  }

  /**
   * Determines whether the given node is a file (i.e. not a folder).
   *
   * @param node The node to inspect.
   * @returns `true` when the node has a type that is not a folder, otherwise `false`.
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
   * @returns `true` when the node's type indicates a folder, otherwise `false`.
   */
  public isFolder(node: ModelNode): boolean {
    if (node.type) {
      return node.type.includes('folder');
    }
    return false;
  }

  /**
   * Determines whether the given node represents a link (an HTML node carrying
   * a non-empty URL).
   *
   * @param node The node to inspect.
   * @returns `true` when the node has an `text/html` mimetype and a non-empty URL, otherwise `false`.
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
   * Prepares a single archived node for restoration and opens the restore
   * confirmation modal.
   *
   * @param archiveNode The archived node to restore.
   */
  public restoreNode(archiveNode: ArchiveNode) {
    this.restorableNodes.set([archiveNode]);
    this.showModal.set(true);
  }

  /**
   * Cancels any pending restore or purge operation, clearing the selection and
   * closing both modals.
   */
  public canceled() {
    this.restorableNodes.set([]);
    this.showModal.set(false);
    this.showPurgeModal.set(false);
  }

  /**
   * Handles completion of a restore operation emitted by the child restore
   * modal: displays the appropriate success/failure notification, refreshes the
   * listing and closes the restore modal.
   *
   * @param res The action result emitted by the restore modal.
   */
  public restoreFinish(res: ActionEmitterResult) {
    if (
      res.type === ActionType.RESTORE_CONTENT &&
      res.result === ActionResult.SUCCEED
    ) {
      this.restorableNodes.set([]);
      const txt = this.translateService.translate(
        'admin.deleted.items.restore.succeed'
      );
      this.uiMessageService.addSuccessMessage(txt);
    } else if (
      res.type === ActionType.RESTORE_CONTENT &&
      res.result === ActionResult.FAILED
    ) {
      const txt = this.translateService.translate(
        'admin.deleted.items.restore.failed'
      );
      this.uiMessageService.addSuccessMessage(txt);
    }

    this.deletedNodesResource.reload();
    this.showModal.set(false);
  }

  /**
   * Toggles the selection state of a node for a bulk restore/purge operation:
   * adds it to {@link restorableNodes} when not already selected, or removes it
   * when it is.
   *
   * @param archiveNode The archived node whose selection state should be toggled.
   */
  public prepareRestoreNode(archiveNode: ArchiveNode) {
    this.restorableNodes.update((nodes) =>
      nodes.includes(archiveNode)
        ? nodes.filter((node) => node !== archiveNode)
        : [...nodes, archiveNode]
    );
  }

  /**
   * Indicates whether the given node is currently part of the pending
   * restore/purge selection.
   *
   * @param archiveNode The archived node to check.
   * @returns `true` when the node is currently selected, otherwise `false`.
   */
  public isPrepared(archiveNode: ArchiveNode): boolean {
    return this.restorableNodes().includes(archiveNode);
  }

  /**
   * Opens the restore confirmation modal for the currently selected nodes.
   */
  public restoreNodes() {
    this.showModal.set(true);
  }

  /**
   * Permanently purges a single archived node. Sets it as the sole selection
   * and delegates to {@link doSingleContentPurge}.
   *
   * @param archiveNode The archived node to purge.
   * @returns A promise that resolves once the purge has completed.
   */
  public async purgeNode(archiveNode: ArchiveNode) {
    this.restorableNodes.set([archiveNode]);
    await this.doSingleContentPurge(archiveNode);
  }

  /**
   * Permanently deletes a single archived document from the current group's
   * recycle bin, swallowing any error, then reports the outcome via
   * {@link purgeFinish}.
   *
   * @param archiveNode The archived node to definitively delete.
   * @returns A promise that resolves once the purge attempt and follow-up handling complete.
   */
  public async doSingleContentPurge(archiveNode: ArchiveNode) {
    const res: ActionEmitterResult = {};
    res.type = ActionType.PURGE_CONTENT;
    res.result = ActionResult.FAILED;

    try {
      if (archiveNode.id) {
        await this.archiveService.deleteDeletedDocumentAsync({
          id: this.currentIg().id as string,
          nodeId: archiveNode.id,
        });
        res.result = ActionResult.SUCCEED;
      }
      // not sure why error is eaten !
      // eslint-disable-next-line no-empty
    } catch (_error) {}
    this.purgeFinish(res);
  }

  /**
   * Opens the purge confirmation modal for the currently selected nodes.
   */
  public purgeNodes() {
    this.showPurgeModal.set(true);
  }

  /**
   * Handles completion of a purge operation: displays the appropriate
   * success/failure notification, refreshes the listing and closes the purge
   * modal.
   *
   * @param res The action result describing the purge outcome.
   */
  public purgeFinish(res: ActionEmitterResult) {
    if (
      res.type === ActionType.PURGE_CONTENT &&
      res.result === ActionResult.SUCCEED
    ) {
      this.restorableNodes.set([]);
      const txt = this.translateService.translate(
        'admin.deleted.items.purge.succeed'
      );
      this.uiMessageService.addSuccessMessage(txt);
    } else if (
      res.type === ActionType.PURGE_CONTENT &&
      res.result === ActionResult.FAILED
    ) {
      const txt = this.translateService.translate(
        'admin.deleted.items.purge.failed'
      );
      this.uiMessageService.addErrorMessage(txt);
    }

    this.deletedNodesResource.reload();
    this.showPurgeModal.set(false);
  }

  /**
   * Toggles the "select all" state for the current page: selecting adds every
   * node on the current page to {@link restorableNodes}, while deselecting
   * clears the entire selection.
   */
  public toggleAllPrepared() {
    this.allSelected = !this.allSelected;
    const data = this.deletedNodes()?.data;
    if (data && this.allSelected) {
      this.restorableNodes.set([...this.restorableNodes(), ...data]);
    } else if (!this.allSelected) {
      this.restorableNodes.set([]);
    }
  }
}
