import { DatePipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  inject,
  OnDestroy,
  OnInit,
  signal,
} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import {
  CategoryService,
  PagedStatisticsContents,
  StatisticsContent,
} from 'app/core/generated/circabc';
import { SaveAsService } from 'app/core/save-as.service';
import { ALF_BASE_PATH } from 'app/core/variables';
import { ListingOptions } from 'app/group/listing-options/listing-options';
import { HorizontalLoaderComponent } from 'app/shared/loader/horizontal-loader.component';
import { PagerComponent } from 'app/shared/pager/pager.component';

/**
 * Statistics view for an interest group category.
 *
 * Renders a paged, auto-refreshing table of statistics report entries
 * ({@link StatisticsContent}) for the category identified by the current
 * route. Each entry can be downloaded as a file, and new statistics reports
 * can be generated on demand.
 *
 * The component reads the category id from the {@link ActivatedRoute}
 * parameters, fetches paged data through {@link CategoryService}, and polls
 * the backend every 10 seconds to keep the listing up to date. Downloads are
 * delegated to {@link SaveAsService} using the Alfresco content service path
 * provided by the {@link ALF_BASE_PATH} injection token.
 *
 * Rendered elements include a horizontal loader, a pager, and localized
 * date formatting via Transloco.
 */
@Component({
  selector: 'cbc-ig-statistics',
  templateUrl: './ig-statistics.component.html',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    HorizontalLoaderComponent,
    PagerComponent,
    DatePipe,
    TranslocoModule,
  ],
})
export class IgStatisticsComponent implements OnInit, OnDestroy {
  /** Route used to read the current category id from its parameters. */
  private readonly route = inject(ActivatedRoute);
  /** Generated API client used to fetch and generate category statistics. */
  private readonly categoryService = inject(CategoryService);
  /** Service used to trigger browser downloads of statistics content. */
  private readonly saveAsService = inject(SaveAsService);

  /**
   * Handle of the polling timer that periodically reloads the statistics.
   * Stored so it can be cleared on destroy. Typed as `any` because
   * `setInterval` returns a platform-dependent handle type.
   */
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  private interval: any;

  /** Current page of statistics entries displayed in the table. */
  public readonly contents = signal<StatisticsContent[] | undefined>(undefined);
  /** Paging and sorting options driving the statistics request. */
  public listingOptions: ListingOptions = { page: 1, limit: 5, sort: '' };
  /** Total number of statistics entries available across all pages. */
  public readonly totalItems = signal(5);

  /** Id of the category whose statistics are being displayed. */
  public categoryId!: string;
  /** Whether a statistics request is currently in progress. */
  public readonly loading = signal(false);
  /** Base path of the Alfresco content service, used to build download URLs. */
  private readonly alfrescoServicePath = inject(ALF_BASE_PATH);

  /** Controls the visibility of the "add" modal in the template. */
  public showAddModal = false;

  /**
   * Angular lifecycle hook. Initializes the content list and subscribes to
   * route parameter changes to (re)load statistics whenever the category
   * changes.
   */
  ngOnInit() {
    this.contents.set([]);

    this.route.params.subscribe(
      async (params) => await this.listContents(params)
    );
  }

  /**
   * Angular lifecycle hook. Stops the polling timer to avoid further requests
   * after the component is destroyed.
   */
  ngOnDestroy(): void {
    clearInterval(this.interval);
  }

  /**
   * Loads statistics for the category taken from the route parameters and
   * starts a 10-second polling interval that keeps the listing refreshed.
   *
   * @param params - Route parameters; the `id` entry is used as the category id.
   * @returns A promise that resolves once the initial load has completed.
   */
  private async listContents(params: { [key: string]: string }) {
    this.categoryId = params.id;
    await this.loadContents();
    this.interval = setInterval(async () => {
      await this.loadContents();
    }, 10000);
  }

  /**
   * Fetches the current page of statistics for {@link categoryId} and updates
   * {@link contents} and {@link totalItems}. Toggles {@link loading} around the
   * request. No request is made when the category id is undefined.
   *
   * @returns A promise that resolves once the request has completed.
   */
  private async loadContents() {
    this.loading.set(true);
    if (this.categoryId !== undefined) {
      const result: PagedStatisticsContents =
        await this.categoryService.getCategoryStatisticsAsync({
          id: this.categoryId,
          limit: this.listingOptions.limit,
          page: this.listingOptions.page,
        });
      this.contents.set(result.data);
      this.totalItems.set(result.total);
    }
    this.loading.set(false);
  }

  /**
   * Navigates the statistics listing to the given page and reloads the data.
   *
   * @param page - The 1-based page number to display.
   * @returns A promise that resolves once the page has been loaded.
   */
  public async goToPage(page: number) {
    this.listingOptions.page = page;
    await this.changePage(this.listingOptions);
  }

  /**
   * Applies new listing options and reloads the statistics accordingly,
   * updating {@link contents} and {@link totalItems}. No request is made when
   * the category id is undefined.
   *
   * @param listingOptions - The paging and sorting options to apply.
   * @returns A promise that resolves once the request has completed.
   */
  public async changePage(listingOptions: ListingOptions) {
    this.listingOptions = listingOptions;
    if (this.categoryId !== undefined) {
      const result: PagedStatisticsContents =
        await this.categoryService.getCategoryStatisticsAsync({
          id: this.categoryId,
          limit: this.listingOptions.limit,
          page: this.listingOptions.page,
        });
      this.contents.set(result.data);
      this.totalItems.set(result.total);
    }
  }

  /**
   * Requests the backend to generate a new statistics report for the current
   * category.
   *
   * @returns A promise that resolves once the generation request has completed.
   */
  public async generateStatistics() {
    await this.categoryService.postCategoryStatisticsAsync({
      id: this.categoryId,
    });
  }

  /**
   * Downloads the file associated with a statistics entry.
   *
   * Builds the Alfresco content URL from the entry's download URL and delegates
   * the actual download to {@link SaveAsService}.
   *
   * @param content - The statistics entry whose content should be downloaded.
   */
  public download(content: StatisticsContent) {
    const url = `${this.alfrescoServicePath}/node/content${(
      content.downloadURL as string
    ).substring(4, 63)}`;
    this.saveAsService.saveUrlAs(url, content.name as string);
  }
}
