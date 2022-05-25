import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { ActionEmitterResult, ActionResult } from 'app/action-result';
import { PagedShares, Share, SpaceService } from 'app/core/generated/circabc';
import { ListingOptions } from 'app/group/listing-options/listing-options';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-manage-space-sharing',
  templateUrl: './manage-space-sharing.component.html',
  preserveWhitespaces: true,
})
export class ManageSpaceSharingComponent implements OnInit {
  public shares!: Share[];
  public spaceId!: string;
  public listingOptions: ListingOptions = { page: 1, limit: 10, sort: '' };
  public totalItems = 10;
  // eslint-disable-next-line no-magic-numbers
  public loading = false;

  public showShareSpaceModal = false;

  constructor(
    private route: ActivatedRoute,
    private spaceService: SpaceService
  ) {}

  ngOnInit() {
    this.shares = [];
    this.route.params.subscribe(
      async (params) => await this.listShares(params)
    );
  }

  // shares paging and listing

  private async listShares(params: { [key: string]: string }) {
    this.spaceId = params.nodeId;
    await this.loadShares();
  }

  private async loadShares() {
    if (this.spaceId !== undefined) {
      this.loading = true;
      const result: PagedShares = await firstValueFrom(
        this.spaceService.getShareSpaces(
          this.spaceId,
          this.listingOptions.limit,
          this.listingOptions.page
        )
      );
      this.shares = result.data;
      this.totalItems = result.total;
      this.loading = false;
    }
  }

  // paging

  public async goToPage(page: number) {
    this.listingOptions.page = page;
    await this.changePage(this.listingOptions);
  }

  public async changePage(listingOptions: ListingOptions) {
    this.listingOptions = listingOptions;
    await this.loadShares();
  }

  public async changeLimit(limit: number) {
    this.listingOptions.limit = limit;
    this.listingOptions.page = 1;
    await this.changePage(this.listingOptions);
  }

  // actions

  public inviteIGToShareSpace() {
    this.showShareSpaceModal = true;
  }

  public async refresh(result: ActionEmitterResult) {
    this.showShareSpaceModal = false;
    if (result.result === ActionResult.SUCCEED) {
      await this.loadShares();
    }
  }

  public async removeShare(share: Share) {
    await firstValueFrom(
      this.spaceService.deleteShareSpace(this.spaceId, share.igId as string)
    );
    await this.loadShares();
  }
}
