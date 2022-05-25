import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import {
  CategoryService,
  PagedGroupCreationRequests,
} from 'app/core/generated/circabc';
import { ListingOptions } from 'app/group/listing-options/listing-options';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-group-requests',
  templateUrl: './group-requests.component.html',
  styleUrls: ['./group-requests.component.scss'],
})
export class GroupRequestsComponent implements OnInit {
  public categoryId!: string;
  public filter = 'waiting';
  public requests!: PagedGroupCreationRequests;
  public listingOptions: ListingOptions = { page: 1, limit: 5, sort: '' };
  public totalItems = 0;
  public loading = false;

  constructor(
    private categoryService: CategoryService,
    private route: ActivatedRoute
  ) {}

  async ngOnInit() {
    this.route.params.subscribe(async (params) => {
      if (params && params.id) {
        this.categoryId = params.id;
        await this.loadGroupRequests();
      }
    });
  }

  public async loadGroupRequests() {
    this.requests = { data: [], total: 0 };
    this.loading = true;
    try {
      this.requests = await firstValueFrom(
        this.categoryService.getInterestGroupRequests(
          this.categoryId,
          this.listingOptions.limit,
          this.listingOptions.page,
          this.filter
        )
      );
      this.totalItems = this.requests.total;
    } catch (error) {
      console.error(error);
    }
    this.loading = false;
  }

  public async selectWaiting() {
    this.filter = 'waiting';
    this.listingOptions = { page: 1, limit: 5, sort: '' };
    await this.loadGroupRequests();
  }

  public async selectRejected() {
    await this.selectItems('rejected');
  }

  public async selectApproved() {
    await this.selectItems('approved');
  }

  public async selectItems(filter: 'rejected' | 'approved') {
    this.filter = filter;
    this.listingOptions = { page: 1, limit: 5, sort: '' };
    await this.loadGroupRequests();
  }

  public async goToPage(page: number) {
    this.listingOptions.page = page;
    await this.loadGroupRequests();
  }

  public trackById(_index: number, item: { id?: string | number }) {
    return item.id;
  }
}
