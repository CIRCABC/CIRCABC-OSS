import { DatePipe } from '@angular/common';
import { Component, OnInit, input } from '@angular/core';
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
  InterestGroup,
  InterestGroupService,
  Node as ModelNode,
  PagedNodes,
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
import { PagerConfigurationComponent } from 'app/shared/pager-configuration/pager-configuration.component';
import { PagerComponent } from 'app/shared/pager/pager.component';
import { UserCardComponent } from 'app/shared/user-card/user-card.component';
import { firstValueFrom } from 'rxjs';
import { UpdateExpiredDateComponent } from './update-expired-date/update-expired-date.component';

@Component({
  selector: 'cbc-expired-items',
  templateUrl: './expired-items.component.html',
  styleUrl: './expired-items.component.scss',
  preserveWhitespaces: true,
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
export class ExpiredItemsComponent implements OnInit {
  readonly groupId = input.required<string>();

  public currentIg!: InterestGroup;
  public listingOptions: ListingOptions = {
    page: 1,
    limit: 10,
    sort: 'expirationDate_DESC',
  };
  public totalItems = 10;
  public loading = false;
  public expiredNodes: PagedNodes = { data: [], total: 0 };
  public deletableNodes: ModelNode[] = [];
  public allSelected = false;
  public deleting = false;
  public mustConfirm = false;
  public showUpdateExpirateDateModal = false;
  public nodeSelected!: ModelNode;

  constructor(
    private groupService: InterestGroupService,
    private uiMessageService: UiMessageService,
    private translateService: TranslocoService,
    private expiredService: ExpiredService,
    private contentService: ContentService
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
      this.expiredNodes = await firstValueFrom(
        this.expiredService.getExpiredDocuments(
          groupId,
          this.listingOptions.limit,
          this.listingOptions.page,
          this.listingOptions.sort
        )
      );

      if (this.expiredNodes.total) {
        this.totalItems = this.expiredNodes.total;
      }
    }
    this.loading = false;
  }

  public async goToPage(page: number) {
    this.listingOptions.page = page;
    await this.changePage(this.listingOptions);
  }

  public async changePage(listingOptions: ListingOptions) {
    this.loading = true;
    this.listingOptions = listingOptions;
    const groupId = this.groupId();
    if (groupId !== undefined) {
      this.expiredNodes = await firstValueFrom(
        this.expiredService.getExpiredDocuments(
          groupId,
          this.listingOptions.limit,
          this.listingOptions.page,
          this.listingOptions.sort
        )
      );

      if (this.expiredNodes.total) {
        this.totalItems = this.expiredNodes.total;
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

  public prepareNode(node: ModelNode) {
    const idx = this.deletableNodes.indexOf(node);
    if (idx === -1) {
      this.deletableNodes.push(node);
    } else {
      this.deletableNodes.splice(idx, 1);
    }
  }

  public isPrepared(node: ModelNode): boolean {
    return this.deletableNodes.indexOf(node) !== -1;
  }

  public async deleteNodes() {
    let isError = false;

    this.deleting = true;
    this.mustConfirm = true;

    for (const node of this.deletableNodes) {
      if (node.id) {
        try {
          await firstValueFrom(this.contentService.deleteContent(node.id));
        } catch (_error) {
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

    await this.changePage(this.listingOptions);

    this.deleting = false;
    this.mustConfirm = false;
  }

  public async deleteNode(node: ModelNode) {
    if (node.id) {
      try {
        await firstValueFrom(this.contentService.deleteContent(node.id));
        const txt = this.translateService.translate(
          getSuccessTranslation(ActionType.DELETE_CONTENT)
        );
        this.uiMessageService.addSuccessMessage(txt, true);
        await this.changePage(this.listingOptions);
      } catch (_error) {
        const txt = this.translateService.translate(
          getErrorTranslation(ActionType.DELETE_CONTENT)
        );
        this.uiMessageService.addErrorMessage(txt);
      }
    }
  }

  public toggleAllPrepared() {
    this.allSelected = !this.allSelected;
    if (this.expiredNodes?.data && this.allSelected) {
      for (const delNode of this.expiredNodes.data) {
        this.deletableNodes.push(delNode);
      }
    } else if (!this.allSelected) {
      this.deletableNodes = [];
    }
  }

  public async onDeletedElement(result: ActionEmitterResult) {
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
    await this.changePage(this.listingOptions);
  }

  public getModifier(node: ModelNode): string {
    if (node.properties) {
      return node.properties.modifier;
    }
    return '';
  }

  public getExpirationDate(node: ModelNode): string | null {
    if (node.properties) {
      return node.properties.expiration_date;
    }
    return null;
  }

  public updateExpiredDate(node: ModelNode) {
    this.nodeSelected = node;
    this.showUpdateExpirateDateModal = true;
  }

  public async refreshExpiredItemsList() {
    await this.loadIg();
  }
}
