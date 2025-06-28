import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import {
  CategoryService,
  GroupCreationRequest,
} from 'app/core/generated/circabc';
import { ListingOptions } from 'app/group/listing-options/listing-options';
import { HorizontalLoaderComponent } from 'app/shared/loader/horizontal-loader.component';
import { firstValueFrom } from 'rxjs';
import { MatTabsModule } from '@angular/material/tabs';
import { GroupRequestCreateElementComponent } from './group-request-create-element/group-request-create-element.component';
import { PagerComponent } from 'app/shared/pager/pager.component';

@Component({
  selector: 'cbc-group-requests-create',
  templateUrl: './group-requests-create.component.html',
  styleUrl: './group-requests-create.component.scss',
  imports: [
    HorizontalLoaderComponent,
    GroupRequestCreateElementComponent,
    TranslocoModule,
    MatTabsModule,
    PagerComponent,
  ],
})
export class GroupRequestsCreateComponent implements OnInit {
  categoryService = inject(CategoryService);
  route = inject(ActivatedRoute);
  categoryId!: string;
  requests!: {
    data: Array<GroupCreationRequest & { interestGroupId?: string }>;
    total: number;
  };
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
        this.categoryId = params.id;
        await this.loadGroupRequests();
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
        this.categoryService.getInterestGroupRequests(
          this.categoryId,
          this.listingOptions.limit,
          this.listingOptions.page,
          this.status
        )
      );

      if (this.requests.data.length > 0) {
        const groups = await firstValueFrom(
          this.categoryService.getInterestGroupsByCategoryId(
            this.categoryId,
            'en'
          )
        );

        this.requests.data = this.requests.data.map((request) => {
          const group = groups.find((g) => g.name === request.proposedName);
          if (group) {
            request.interestGroupId = group.id;
          }
          return request;
        });
      }

      this.loading = false;
    }
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
