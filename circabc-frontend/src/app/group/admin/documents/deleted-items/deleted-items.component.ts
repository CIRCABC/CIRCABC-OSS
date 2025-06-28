import { Component, OnInit, input } from '@angular/core';

import { TranslocoModule, TranslocoService } from '@jsverse/transloco';

import { DatePipe } from '@angular/common';
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
  PagedArchiveNodes,
} from 'app/core/generated/circabc';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { changeSort } from 'app/core/util';
import { PurgeItemComponent } from 'app/group/admin/documents/purge-item/purge-item.component';
import { RestoreItemComponent } from 'app/group/admin/documents/restore-item/restore-item.component';
import { ListingOptions } from 'app/group/listing-options/listing-options';
import { InlineDeleteComponent } from 'app/shared/delete/inline-delete.component';
import { NumberBadgeComponent } from 'app/shared/number-badge/number-badge.component';
import { PagerConfigurationComponent } from 'app/shared/pager-configuration/pager-configuration.component';
import { PagerComponent } from 'app/shared/pager/pager.component';
import { UserCardComponent } from 'app/shared/user-card/user-card.component';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-deleted-items',
  templateUrl: './deleted-items.component.html',
  styleUrl: './deleted-items.component.scss',
  preserveWhitespaces: true,
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
export class DeletedItemsComponent implements OnInit {
  readonly groupId = input.required<string>();

  public currentIg!: InterestGroup;
  public listingOptions: ListingOptions = {
    page: 1,
    limit: 10,
    sort: 'archivedDate_DESC',
  };
  public totalItems = 10;
  public loading = false;
  public deletedNodes: PagedArchiveNodes = { data: [], total: 0 };

  public showModal = false;
  public showPurgeModal = false;
  public restorableNodes: ArchiveNode[] = [];
  public allSelected = false;

  constructor(
    private archiveService: ArchiveService,
    private groupService: InterestGroupService,
    private translateService: TranslocoService,
    private uiMessageService: UiMessageService
  ) {}

  async ngOnInit() {
    await this.loadIg();
  }

  private async loadIg() {
    this.loading = true;
    const groupId = this.groupId();
    if (groupId) {
      this.currentIg = await firstValueFrom(
        this.groupService.getInterestGroup(groupId)
      );
      this.deletedNodes = await firstValueFrom(
        this.archiveService.getDeletedDocuments(
          groupId,
          this.listingOptions.limit,
          this.listingOptions.page,
          this.listingOptions.sort
        )
      );

      if (this.deletedNodes.total) {
        this.totalItems = this.deletedNodes.total;
      }
    }
    this.loading = false;
  }

  public async goToPage(page: number) {
    this.listingOptions.page = page;
    await this.changePage(this.listingOptions);
  }

  private async changePage(listingOptions: ListingOptions) {
    this.loading = true;
    this.listingOptions = listingOptions;
    const groupId = this.groupId();
    if (groupId !== undefined) {
      this.deletedNodes = await firstValueFrom(
        this.archiveService.getDeletedDocuments(
          groupId,
          this.listingOptions.limit,
          this.listingOptions.page,
          this.listingOptions.sort
        )
      );

      if (this.deletedNodes.total) {
        this.totalItems = this.deletedNodes.total;
      }
    }
    this.loading = false;
  }

  public async changeSort(sort: string) {
    this.listingOptions.sort = changeSort(this.listingOptions.sort, sort);
    await this.changePage(this.listingOptions);
  }

  public async changeLimit(limit: number) {
    this.listingOptions.limit = limit;
    this.listingOptions.page = 1;
    await this.changePage(this.listingOptions);
  }

  public isFile(node: ModelNode): boolean {
    if (node.type) {
      return node.type.indexOf('folder') === -1;
    }
    return false;
  }

  public isFolder(node: ModelNode): boolean {
    if (node.type) {
      return node.type.indexOf('folder') !== -1;
    }
    return false;
  }

  public isLink(node: ModelNode): boolean {
    if (node.properties?.mimetype && node.properties.url) {
      return (
        node.properties.mimetype === 'text/html' && node.properties.url !== ''
      );
    }
    return false;
  }

  public restoreNode(archiveNode: ArchiveNode) {
    this.restorableNodes = [];
    this.restorableNodes.push(archiveNode);
    this.showModal = true;
  }

  public canceled() {
    this.restorableNodes = [];
    this.showModal = false;
  }

  public async restoreFinish(res: ActionEmitterResult) {
    if (
      res.type === ActionType.RESTORE_CONTENT &&
      res.result === ActionResult.SUCCEED
    ) {
      this.restorableNodes = [];
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

    this.deletedNodes = await firstValueFrom(
      this.archiveService.getDeletedDocuments(
        this.groupId(),
        this.listingOptions.limit,
        this.listingOptions.page,
        this.listingOptions.sort
      )
    );
    this.showModal = false;
  }

  public prepareRestoreNode(archiveNode: ArchiveNode) {
    const idx = this.restorableNodes.indexOf(archiveNode);
    if (idx === -1) {
      this.restorableNodes.push(archiveNode);
    } else {
      this.restorableNodes.splice(idx, 1);
    }
  }

  public isPrepared(archiveNode: ArchiveNode): boolean {
    return this.restorableNodes.indexOf(archiveNode) !== -1;
  }

  public restoreNodes() {
    this.showModal = true;
  }

  public async purgeNode(archiveNode: ArchiveNode) {
    this.restorableNodes = [];
    this.restorableNodes.push(archiveNode);
    await this.doSingleContentPurge(archiveNode);
  }

  public async doSingleContentPurge(archiveNode: ArchiveNode) {
    const res: ActionEmitterResult = {};
    res.type = ActionType.PURGE_CONTENT;
    res.result = ActionResult.FAILED;

    try {
      if (archiveNode.id) {
        await firstValueFrom(
          this.archiveService.deleteDeletedDocument(
            this.currentIg.id as string,
            archiveNode.id
          )
        );
        res.result = ActionResult.SUCCEED;
      }
      // not sure why error is eaten !
      // eslint-disable-next-line no-empty, @typescript-eslint/no-empty-function
    } catch (_error) {}
    await this.purgeFinish(res);
  }

  public purgeNodes() {
    this.showPurgeModal = true;
  }

  public async purgeFinish(res: ActionEmitterResult) {
    if (
      res.type === ActionType.PURGE_CONTENT &&
      res.result === ActionResult.SUCCEED
    ) {
      this.restorableNodes = [];
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

    this.deletedNodes = await firstValueFrom(
      this.archiveService.getDeletedDocuments(
        this.groupId(),
        this.listingOptions.limit,
        this.listingOptions.page,
        this.listingOptions.sort
      )
    );
    this.showPurgeModal = false;
  }

  public toggleAllPrepared() {
    this.allSelected = !this.allSelected;
    if (this.deletedNodes?.data && this.allSelected) {
      for (const delNode of this.deletedNodes.data) {
        this.restorableNodes.push(delNode);
      }
    } else if (!this.allSelected) {
      this.restorableNodes = [];
    }
  }
}
