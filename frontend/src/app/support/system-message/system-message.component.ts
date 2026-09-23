import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  resource,
  signal,
} from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import {
  AppMessageService,
  PagedAppMessages,
} from 'app/core/generated/circabc';
import { ListingOptions } from 'app/group/listing-options/listing-options';
import { HorizontalLoaderComponent } from 'app/shared/loader/horizontal-loader.component';
import { PagerComponent } from 'app/shared/pager/pager.component';
import { OldUiConfigurationComponent } from './old-ui-configuration/old-ui-configuration.component';
import { TemplateRendererComponent } from './template-renderer/template-renderer.component';

/** Default (empty) page returned while templates are loading or on error. */
const EMPTY_TEMPLATES: PagedAppMessages = { data: [], total: 0 };

/**
 * Support administration screen that lists the platform's system message
 * (app message) templates in a paginated table.
 *
 * The component renders a loading indicator while fetching, the paged list of
 * templates, a {@link PagerComponent} to navigate between pages, and hosts the
 * old-UI configuration and template-renderer sub-components. It fetches the
 * templates from the backend via {@link AppMessageService} on initialisation
 * and reloads them on pagination or refresh actions.
 */
@Component({
  selector: 'cbc-system-message',
  templateUrl: './system-message.component.html',
  styleUrl: './system-message.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    HorizontalLoaderComponent,
    RouterLink,
    PagerComponent,
    OldUiConfigurationComponent,
    TemplateRendererComponent,
    TranslocoModule,
  ],
})
export class SystemMessageComponent {
  /** Generated API client used to retrieve the paged app message templates. */
  private readonly appMessageService = inject(AppMessageService);

  /** Pagination and sorting state driving the template listing request. */
  public readonly listingOptions = signal<ListingOptions>({
    page: 1,
    limit: 5,
    sort: '',
  });
  /** Controls the visibility of the delete confirmation modal. */
  public showDeleteModal = false;

  // add in  order to compile
  /** Controls the visibility of the create template modal. */
  public showModalCreate = false;

  /**
   * Loads the current page of app message templates from the backend,
   * re-running whenever {@link listingOptions} changes (pagination) or a
   * manual {@link templatesResource.reload} is triggered (e.g. after a
   * create/delete operation). Errors are caught and logged so the resource
   * never enters the error state, matching the previous behaviour of simply
   * logging and showing an empty list.
   */
  private readonly templatesResource = resource({
    params: () => ({
      limit: this.listingOptions().limit,
      page: this.listingOptions().page,
    }),
    loader: async ({ params }) => {
      try {
        return await this.appMessageService.getPagedAppMessagesTemplateAsync({
          limit: params.limit,
          page: params.page,
        });
      } catch (_error) {
        console.error('problem getting the list of templates');
        return EMPTY_TEMPLATES;
      }
    },
    defaultValue: EMPTY_TEMPLATES,
  });

  /** The current page of app message templates returned by the backend. */
  public readonly templates = this.templatesResource.value;
  /** Whether a template fetch is currently in progress. */
  public readonly loading = this.templatesResource.isLoading;
  /** Total number of templates available, used to size the pager. */
  public readonly totalItems = computed(() => this.templates().total);

  /**
   * Navigates to the given page of templates and reloads the listing.
   *
   * @param page The 1-based page number to display.
   */
  public goToPage(page: number) {
    this.listingOptions.update((options) => ({ ...options, page }));
  }

  /**
   * Closes the delete confirmation modal and reloads the current page of
   * templates, typically invoked after a create or delete operation.
   */
  public refresh() {
    this.showDeleteModal = false;
    this.templatesResource.reload();
  }
}
