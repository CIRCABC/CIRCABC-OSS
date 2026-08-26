import { Component, OnInit, inject } from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';

import { ListingOptions } from 'app/group/listing-options/listing-options';
import { HorizontalLoaderComponent } from 'app/shared/loader/horizontal-loader.component';
import { firstValueFrom } from 'rxjs';
import { GroupRequestDeleteElementComponent } from './group-request-delete-element/group-request-delete-element.component';
import { ActivatedRoute } from '@angular/router';
import {
  GroupDeletionRequest,
  CategoryService,
} from 'app/core/generated/circabc';
import { MatTabsModule } from '@angular/material/tabs';
import { PagerComponent } from 'app/shared/pager/pager.component';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'cbc-group-requests-delete',
  templateUrl: './group-requests-delete.component.html',
  styleUrl: './group-requests-delete.component.scss',
  imports: [
    HorizontalLoaderComponent,
    GroupRequestDeleteElementComponent,
    TranslocoModule,
    MatTabsModule,
    PagerComponent,
    CommonModule,
  ],
})
export class GroupRequestsDeleteComponent implements OnInit {
  categoryService = inject(CategoryService);
  route = inject(ActivatedRoute);
  categoryId?: string;
  requests!: { data: Array<GroupDeletionRequest>; total: number };
  listingOptions: ListingOptions = {
    page: 1,
    limit: 5,
    sort: '',
  };
  loading = false;
  status = 'waiting';

  async ngOnInit() {
    this.route.params.subscribe(async (params) => {
      if (params?.id) {
        this.categoryId = params?.id;
        if (this.categoryId) {
          await this.loadGroupRequests();
        }
      }
    });
  }

  loadRequests(event: { index: number }) {
    switch (event.index) {
      case 0:
        this.status = 'waiting';
        break;
      case 1:
        this.status = 'approved';
        break;
      case 2:
        this.status = 'rejected';
        break;
      default:
        break;
    }
    this.listingOptions.page = 1;
    this.loadGroupRequests();
  }

  public async loadGroupRequests() {
    this.requests = { data: [], total: 0 };
    this.loading = true;
    if (this.categoryId) {
      this.requests = await firstValueFrom(
        this.categoryService.getGroupDeletionRequests(
          this.categoryId,
          this.listingOptions.limit,
          this.listingOptions.page,
          this.status
        )
      );
    }
    this.loading = false;
  }

  goToPage(page: number) {
    this.listingOptions.page = page;
    this.loadGroupRequests();
  }

  changeLimit(limit: number) {
    this.listingOptions.limit = limit;
    this.listingOptions.page = 1;
    this.loadGroupRequests();
  }

  reloadGroupRequests() {
    this.listingOptions = {
      page: 1,
      limit: 5,
      sort: '',
    };
    this.loadGroupRequests();
  }
}
