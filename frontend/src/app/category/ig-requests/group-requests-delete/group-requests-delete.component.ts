import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  resource,
  signal,
} from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatTabsModule } from '@angular/material/tabs';
import { ActivatedRoute } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { CategoryService } from 'app/core/generated/circabc';
import { HorizontalLoaderComponent } from 'app/shared/loader/horizontal-loader.component';
import { PagerComponent } from 'app/shared/pager/pager.component';
import { GroupRequestDeleteElementComponent } from './group-request-delete-element/group-request-delete-element.component';

/**
 * Displays and manages interest-group deletion requests belonging to a category.
 *
 * The component renders a set of Material tabs (waiting, approved, rejected) that
 * let an administrator browse {@link GroupDeletionRequest} entries for the current
 * category. Each request is rendered through
 * {@link GroupRequestDeleteElementComponent}, and the list is paged via
 * {@link PagerComponent}. A {@link HorizontalLoaderComponent} is shown while the
 * data is being fetched.
 *
 * The category identifier is resolved from the activated route parameters, and the
 * request list is retrieved from the backend through {@link CategoryService}.
 */
@Component({
  selector: 'cbc-group-requests-delete',
  templateUrl: './group-requests-delete.component.html',
  styleUrl: './group-requests-delete.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    HorizontalLoaderComponent,
    GroupRequestDeleteElementComponent,
    TranslocoModule,
    MatTabsModule,
    PagerComponent,
  ],
})
export class GroupRequestsDeleteComponent {
  /** Generated API client used to fetch group deletion requests for the category. */
  categoryService = inject(CategoryService);
  /** Provides access to the route parameters, notably the category id. */
  route = inject(ActivatedRoute);

  /** Raw route parameters, kept up to date as the router navigates. */
  private readonly routeParams = toSignal(this.route.params, {
    initialValue: {} as Record<string, string>,
  });
  /** Identifier of the category whose deletion requests are being displayed. */
  categoryId = computed(() => this.routeParams()?.id);

  /** Current page number of deletion requests being displayed. */
  page = signal(1);
  /** Number of deletion requests displayed per page. */
  limit = signal(5);
  /** The status filter currently selected by the active tab. */
  status = signal<'waiting' | 'approved' | 'rejected'>('waiting');

  /**
   * Loads the current page of deletion requests for {@link categoryId},
   * {@link page}, {@link limit} and {@link status}. Idle while no category id
   * is present in the route, mirroring the previous `if (categoryId)` guard.
   * Falls back to an empty page on error, matching the previous behaviour (no
   * dedicated error UI for this call).
   */
  private readonly requestsResource = resource({
    params: () => {
      const id = this.categoryId();
      return id
        ? {
            id,
            page: this.page(),
            limit: this.limit(),
            filter: this.status(),
          }
        : undefined;
    },
    loader: async ({ params }) => {
      try {
        return await this.categoryService.getGroupDeletionRequestsAsync({
          id: params.id,
          limit: params.limit,
          page: params.page,
          filter: params.filter,
        });
      } catch (e) {
        console.error(e);
        return { data: [], total: 0 };
      }
    },
    defaultValue: { data: [], total: 0 },
  });

  /** Current page of deletion requests together with the total count available. */
  requests = this.requestsResource.value;
  /** Whether a request-loading operation is currently in progress. */
  loading = this.requestsResource.isLoading;

  /**
   * Reacts to a Material tab change by mapping the selected tab index to the
   * corresponding status filter and resetting to the first page.
   *
   * @param event The tab-change event carrying the selected tab index
   *              (0 = waiting, 1 = approved, 2 = rejected).
   */
  loadRequests(event: { index: number }) {
    switch (event.index) {
      case 0:
        this.status.set('waiting');
        break;
      case 1:
        this.status.set('approved');
        break;
      case 2:
        this.status.set('rejected');
        break;
      default:
        break;
    }
    this.page.set(1);
  }

  /**
   * Navigates to the given page of deletion requests.
   *
   * @param page The 1-based page number to display.
   */
  goToPage(page: number) {
    this.page.set(page);
  }

  /**
   * Changes the number of requests displayed per page and resets to the
   * first page.
   *
   * @param limit The new page size (number of requests per page).
   */
  changeLimit(limit: number) {
    this.limit.set(limit);
    this.page.set(1);
  }

  /**
   * Resets the pagination options to their defaults (first page, five items)
   * and reloads the deletion requests.
   */
  reloadGroupRequests() {
    this.limit.set(5);
    this.page.set(1);
    this.requestsResource.reload();
  }
}
