import { DatePipe } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';
import {
  HistoryService,
  PagedUserRevocationRequest,
} from 'app/core/generated/circabc';
import { ListingOptions } from 'app/group/listing-options/listing-options';
import { PagerConfigurationComponent } from 'app/shared/pager-configuration/pager-configuration.component';
import { PagerComponent } from 'app/shared/pager/pager.component';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-revocation-jobs',
  templateUrl: './revocation-jobs.component.html',
  imports: [
    PagerComponent,
    PagerConfigurationComponent,
    DatePipe,
    TranslocoModule,
  ],
})
export class RevocationJobsComponent implements OnInit {
  public revocations!: PagedUserRevocationRequest;
  public listingOptions: ListingOptions = { page: 1, limit: 10, sort: '' };
  public loading = false;
  public totalItems = 0;

  constructor(private historyService: HistoryService) {}

  async ngOnInit() {
    this.loading = true;
    try {
      this.revocations = await firstValueFrom(
        this.historyService.getRevocations(
          this.listingOptions.limit,
          this.listingOptions.page,
          this.listingOptions.sort
        )
      );

      if (this.revocations.total) {
        this.totalItems = this.revocations.total;
      }
    } catch (error) {
      console.error(error);
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
    try {
      this.revocations = await firstValueFrom(
        this.historyService.getRevocations(
          this.listingOptions.limit,
          this.listingOptions.page,
          this.listingOptions.sort
        )
      );
      if (this.revocations.total) {
        this.totalItems = this.revocations.total;
      }
    } catch (error) {
      console.error(error);
    }
    this.loading = false;
  }

  public async changeLimit(limit: number) {
    this.listingOptions.limit = limit;
    this.listingOptions.page = 0;
    await this.changePage(this.listingOptions);
  }
}
