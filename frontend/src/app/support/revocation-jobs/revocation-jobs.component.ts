import { DatePipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  resource,
  signal,
} from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';
import { HistoryService } from 'app/core/generated/circabc';
import { ListingOptions } from 'app/group/listing-options/listing-options';
import { PagerComponent } from 'app/shared/pager/pager.component';
import { PagerConfigurationComponent } from 'app/shared/pager-configuration/pager-configuration.component';

/**
 * Standalone Angular component that renders a paginated listing of user
 * account revocation requests within the support area.
 *
 * The template displays each revocation request (with its status and dates)
 * together with pagination controls provided by {@link PagerComponent} and
 * {@link PagerConfigurationComponent}. Data is retrieved from the backend
 * through its key collaborator, the generated {@link HistoryService}.
 *
 * @remarks
 * Uses `ChangeDetectionStrategy.OnPush` and loads its data reactively via
 * `resource()`, re-fetching whenever the pagination/sort state changes.
 */
@Component({
  selector: 'cbc-revocation-jobs',
  templateUrl: './revocation-jobs.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    PagerComponent,
    PagerConfigurationComponent,
    DatePipe,
    TranslocoModule,
  ],
})
export class RevocationJobsComponent {
  /** Generated API client used to fetch the paged revocation requests. */
  private readonly historyService = inject(HistoryService);

  /**
   * Current pagination and sorting state (page number, page size and the
   * status used to sort/filter the listing). Drives the {@link revocationsResource}.
   */
  public readonly listingOptions = signal<ListingOptions>({
    page: 1,
    limit: 10,
    sort: '',
  });

  /** Loads the page of revocation requests matching {@link listingOptions}. */
  private readonly revocationsResource = resource({
    params: () => this.listingOptions(),
    loader: async ({ params }) => {
      try {
        return await this.historyService.getRevocationsAsync({
          limit: params.limit,
          page: params.page,
          filter: params.sort as
            'waiting' | 'approved' | 'rejected' | undefined,
        });
      } catch (error) {
        console.error(error);
        return undefined;
      }
    },
  });

  /** The current page of revocation requests returned by the backend. */
  public readonly revocations = this.revocationsResource.value;
  /** Indicates whether a data fetch is currently in progress. */
  public readonly loading = this.revocationsResource.isLoading;
  /** Total number of revocation requests available across all pages. */
  public readonly totalItems = computed(
    () => this.revocationsResource.value()?.total ?? 0
  );

  /**
   * Navigates to the given page and reloads the listing.
   *
   * @param page - The 1-based page number to display.
   */
  public goToPage(page: number) {
    this.changePage({ ...this.listingOptions(), page });
  }

  /**
   * Reloads the revocation requests using the supplied pagination options.
   *
   * @param listingOptions - The pagination and sorting state to apply for the
   * fetch.
   */
  public changePage(listingOptions: ListingOptions) {
    this.listingOptions.set(listingOptions);
  }

  /**
   * Changes the number of items displayed per page and reloads the listing
   * from the first page.
   *
   * @param limit - The new page size (number of items per page).
   */
  public changeLimit(limit: number) {
    this.changePage({ ...this.listingOptions(), limit, page: 0 });
  }
}
