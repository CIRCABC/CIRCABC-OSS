import { Component, Input, OnInit } from '@angular/core';

import { TranslocoService } from '@ngneat/transloco';

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
import { ListingOptions } from 'app/group/listing-options/listing-options';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-deleted-items',
  templateUrl: './deleted-items.component.html',
  styleUrls: ['./deleted-items.component.scss'],
  preserveWhitespaces: true,
})
export class DeletedItemsComponent implements OnInit {
  @Input()
  groupId!: string;

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
    if (this.groupId) {
      this.currentIg = await firstValueFrom(
        this.groupService.getInterestGroup(this.groupId)
      );
      this.deletedNodes = await firstValueFrom(
        this.archiveService.getDeletedDocuments(
          this.groupId,
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
    if (this.groupId !== undefined) {
      this.deletedNodes = await firstValueFrom(
        this.archiveService.getDeletedDocuments(
          this.groupId,
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
    } else {
      return false;
    }
  }

  public isFolder(node: ModelNode): boolean {
    if (node.type) {
      return node.type.indexOf('folder') !== -1;
    } else {
      return false;
    }
  }

  public isLink(node: ModelNode): boolean {
    if (node.properties && node.properties.mimetype && node.properties.url) {
      return (
        node.properties.mimetype === 'text/html' && node.properties.url !== ''
      );
    } else {
      return false;
    }
  }

  public restoreNode(archiveNode: ArchiveNode) {
    this.restorableNodes = [];
    this.restorableNodes.push(archiveNode);
    this.showModal = true;
  }

  public restoreCanceled() {
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
        this.groupId,
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
    } catch (error) {}
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
        this.groupId,
        this.listingOptions.limit,
        this.listingOptions.page,
        this.listingOptions.sort
      )
    );
    this.showPurgeModal = false;
  }

  public toggleAllPrepared() {
    this.allSelected = !this.allSelected;
    if (this.deletedNodes && this.deletedNodes.data && this.allSelected) {
      for (const delNode of this.deletedNodes.data) {
        this.restorableNodes.push(delNode);
      }
    } else if (!this.allSelected) {
      this.restorableNodes = [];
    }
  }
  public trackById(_index: number, item: { id?: string | number }) {
    return item.id;
  }
}
