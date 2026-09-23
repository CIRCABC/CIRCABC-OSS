import {
  ChangeDetectionStrategy,
  Component,
  inject,
  resource,
  signal,
} from '@angular/core';
import { MatTabsModule } from '@angular/material/tabs';
import { ActivatedRoute } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import {
  CategoryService,
  GroupCreationRequest,
} from 'app/core/generated/circabc';
import { HorizontalLoaderComponent } from 'app/shared/loader/horizontal-loader.component';
import { PagerComponent } from 'app/shared/pager/pager.component';
import { GroupRequestCreateElementComponent } from './group-request-create-element/group-request-create-element.component';

/**
 * Displays and manages interest-group creation requests for a given category.
 *
 * The component renders a tabbed interface (backed by Angular Material tabs)
 * that lets a category administrator browse group-creation requests grouped by
 * their processing status: `waiting`, `approved` and `rejected`. Each request
 * is rendered as a {@link GroupRequestCreateElementComponent}, and the paged
 * list is navigated with a {@link PagerComponent}. A
 * {@link HorizontalLoaderComponent} is shown while requests are being fetched.
 *
 * Request data is retrieved from the backend through {@link CategoryService},
 * and the category identifier is resolved from the current route via
 * {@link ActivatedRoute}. For each request whose proposed name matches an
 * existing interest group, the corresponding group id is attached so the UI can
 * link to the created group.
 */
@Component({
  selector: 'cbc-group-requests-create',
  templateUrl: './group-requests-create.component.html',
  styleUrl: './group-requests-create.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    HorizontalLoaderComponent,
    GroupRequestCreateElementComponent,
    TranslocoModule,
    MatTabsModule,
    PagerComponent,
  ],
})
export class GroupRequestsCreateComponent {
  /** Backend client used to fetch group-creation requests and interest groups. */
  categoryService = inject(CategoryService);
  /** Provides access to the current route parameters (notably the category id). */
  route = inject(ActivatedRoute);
  /** Identifier of the category whose group-creation requests are displayed. */
  categoryId = signal('');
  /** Current page of results (1-based). */
  page = signal(1);
  /** Number of requests to display per page. */
  limit = signal(5);
  /** Status filter driving which requests are fetched and which tab is active. */
  status = signal<'waiting' | 'approved' | 'rejected'>('waiting');

  /**
   * Loads the current page of group-creation requests for the active category
   * and status, then attaches the matching `interestGroupId` to any request
   * whose proposed name corresponds to an existing interest group.
   *
   * Idle while {@link categoryId} has not been resolved from the route yet.
   */
  private readonly requestsResource = resource({
    params: () => {
      const id = this.categoryId();
      return id
        ? {
            id,
            limit: this.limit(),
            page: this.page(),
            filter: this.status(),
          }
        : undefined;
    },
    loader: async ({ params }) => {
      try {
        const requests: {
          data: Array<GroupCreationRequest & { interestGroupId?: string }>;
          total: number;
        } = await this.categoryService.getInterestGroupRequestsAsync(params);

        if (requests.data.length > 0) {
          const groups =
            await this.categoryService.getInterestGroupsByCategoryIdAsync({
              id: params.id,
              language: 'en',
            });

          requests.data = requests.data.map((request) => {
            const group = groups.find((g) => g.name === request.proposedName);
            if (group) {
              request.interestGroupId = group.id;
            }
            return request;
          });
        }

        return requests;
      } catch (e) {
        console.error(e);
        return { data: [], total: 0 };
      }
    },
    defaultValue: { data: [], total: 0 },
  });

  /**
   * Current page of group-creation requests together with the total count.
   *
   * Each request is augmented with an optional `interestGroupId` that links it
   * to an already-created interest group when the proposed name matches.
   */
  requests = this.requestsResource.value;
  /** Indicates whether a request-loading operation is currently in progress. */
  loading = this.requestsResource.isLoading;

  constructor() {
    this.route.params.subscribe((params) => {
      if (params?.id) {
        this.categoryId.set(params.id);
      }
    });
  }

  /**
   * Handles a tab-selection change by mapping the selected tab index to the
   * corresponding request status and resetting to the first page. The
   * requests resource reactively reloads.
   *
   * @param event The Material tab-change event carrying the selected tab index
   *   (0 = waiting, 1 = approved, 2 = rejected).
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
   * Navigates to the given page. The requests resource reactively reloads.
   *
   * @param page The 1-based page number to display.
   */
  goToPage(page: number) {
    this.page.set(page);
  }

  /**
   * Changes the page size and resets to the first page. The requests resource
   * reactively reloads.
   *
   * @param limit The number of requests to display per page.
   */
  changeLimit(limit: number) {
    this.limit.set(limit);
    this.page.set(1);
  }

  /**
   * Resets pagination to its default values and reloads the group-creation
   * requests. Typically invoked after a request has been processed to refresh
   * the list.
   */
  reloadGroupRequests() {
    this.page.set(1);
    this.limit.set(5);
    this.requestsResource.reload();
  }
}
