import {
  ChangeDetectionStrategy,
  Component,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { TranslocoModule, TranslocoService } from '@jsverse/transloco';

import {
  AutoUploadConfiguration,
  AutoUploadService,
  PagedAutoUploadConfiguration,
} from 'app/core/generated/circabc';
import { changeSort } from 'app/core/util';
import { ListingOptions } from 'app/group/listing-options/listing-options';
import { InlineDeleteComponent } from 'app/shared/delete/inline-delete.component';
import { HorizontalLoaderComponent } from 'app/shared/loader/horizontal-loader.component';
import { PagerComponent } from 'app/shared/pager/pager.component';
import { PagerConfigurationComponent } from 'app/shared/pager-configuration/pager-configuration.component';
import { SetTitlePipe } from 'app/shared/pipes/set-title.pipe';
import { AddConfigurationComponent } from './add-configuration/add-configuration.component';

/**
 * Group administration screen for managing auto-upload configurations of an
 * interest group.
 *
 * Renders a paged, sortable list of {@link AutoUploadConfiguration} entries for
 * the current interest group (resolved from the `id` route parameter). Each
 * entry can be enabled/disabled (toggled) or deleted, and new configurations
 * can be added through the embedded {@link AddConfigurationComponent} modal.
 * The template combines a horizontal loader, a pager with page-size
 * configuration and inline delete controls.
 *
 * The component also formats the cron-style `dateRestriction` of each
 * configuration into a human-readable, translated label.
 *
 * Key collaborators:
 * - {@link ActivatedRoute} to obtain the interest group id from the route.
 * - {@link AutoUploadService} (generated API client) to read, delete and
 *   update auto-upload entries.
 * - {@link TranslocoService} to translate the schedule labels.
 */
@Component({
  selector: 'cbc-auto-upload',
  templateUrl: './auto-upload.component.html',
  styleUrl: './auto-upload.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    HorizontalLoaderComponent,
    PagerComponent,
    PagerConfigurationComponent,
    InlineDeleteComponent,
    AddConfigurationComponent,
    SetTitlePipe,
    TranslocoModule,
  ],
})
export class AutoUploadComponent implements OnInit {
  /** Provides access to the current route parameters (notably the group id). */
  private readonly route = inject(ActivatedRoute);
  /** Generated API client used to list, delete and update auto-upload entries. */
  private readonly autoUploadService = inject(AutoUploadService);
  /** Transloco service used to translate schedule (day/hour) labels. */
  private readonly translateService = inject(TranslocoService);

  /** The auto-upload configurations currently displayed for the active page. */
  public readonly configurations = signal<AutoUploadConfiguration[]>([]);
  /** Identifier of the interest group whose configurations are managed. */
  public readonly igId = signal<string>(undefined as unknown as string);
  /** Current paging and sorting state for the configuration listing. */
  public listingOptions: ListingOptions = { page: 1, limit: 10, sort: '' };
  /** Total number of configurations available across all pages. */
  public readonly totalItems = signal(10);
  /** Whether a load/refresh of the configurations is in progress. */
  public readonly loading = signal(false);
  /** Whether the "add configuration" modal is currently shown. */
  public showAddModal = false;

  /**
   * Angular lifecycle hook. Initialises the configuration list and subscribes
   * to route parameter changes to (re)load the configurations for the group.
   */
  ngOnInit() {
    this.configurations.set([]);
    this.route.params.subscribe(
      async (params) => await this.listConfigurations(params)
    );
  }

  // configurations paging and listing

  /**
   * Reads the interest group id from the route parameters and loads its
   * auto-upload configurations.
   *
   * @param params Route parameters; the `id` entry holds the interest group id.
   */
  private async listConfigurations(params: { [key: string]: string }) {
    this.igId.set(params.id);
    await this.loadConfigurations();
  }

  /**
   * Loads the configurations for the current group while toggling the
   * {@link loading} flag. No request is made when {@link igId} is undefined.
   */
  private async loadConfigurations() {
    this.loading.set(true);
    if (this.igId() !== undefined) {
      await this.getAutoUploadConfigurations();
    }
    this.loading.set(false);
  }

  /**
   * Fetches the current page of auto-upload configurations from the API and
   * populates {@link configurations} and {@link totalItems}. For each entry the
   * cron-style `dateRestriction` is converted into a translated,
   * human-readable schedule label (e.g. "Monday 9:00" or "Monday every hour").
   */
  private async getAutoUploadConfigurations() {
    const result: PagedAutoUploadConfiguration =
      await this.autoUploadService.getAutoUploadEntriesAsync({
        id: this.igId(),
        limit: this.listingOptions.limit,
        page: this.listingOptions.page,
      });
    const configurations = result.data;

    // pretty print dateRestriction
    for (const configuration of configurations) {
      const values: string[] = this.prettyPrintDateRestriction(configuration);
      if (values.length === 0) {
        configuration.dateRestriction = '-';
      } else if (values.length > 1) {
        const day: string = this.translateService.translate(
          `cron-num-day.${values[1]}`
        );
        let hour: string;
        if (values[0] === '*') {
          hour = this.translateService.translate('label.every.hour.s');
        } else {
          hour = `${values[0]}:00`;
        }
        configuration.dateRestriction = `${day} ${hour}`;
      }
    }

    this.configurations.set(configurations);
    this.totalItems.set(result.total);
  }

  /**
   * Handles the event emitted when a new configuration has been added: closes
   * the add modal and refreshes the configuration list.
   */
  public async configurationAdded() {
    this.showAddModal = false;
    await this.loadConfigurations();
  }

  /**
   * Navigates to the given page of the configuration listing.
   *
   * @param page The 1-based page number to display.
   */
  public async goToPage(page: number) {
    this.listingOptions.page = page;
    await this.changePage(this.listingOptions);
  }

  /**
   * Applies the provided listing options and reloads the current page of
   * configurations. No request is made when {@link igId} is undefined.
   *
   * @param listingOptions The paging/sorting options to apply.
   */
  private async changePage(listingOptions: ListingOptions) {
    this.listingOptions = listingOptions;
    if (this.igId() !== undefined) {
      await this.getAutoUploadConfigurations();
    }
  }
  /**
   * Toggles/updates the sort order for the given column and reloads the list.
   *
   * @param sort The column key to sort by.
   */
  public async changeSort(sort: string) {
    this.listingOptions.sort = changeSort(this.listingOptions.sort, sort);
    await this.changePage(this.listingOptions);
  }

  /**
   * Changes the page size, resets to the first page and reloads the list.
   *
   * @param limit The number of items to display per page.
   */
  public async changeLimit(limit: number) {
    this.listingOptions.limit = limit;
    this.listingOptions.page = 1;
    await this.changePage(this.listingOptions);
  }

  // configuration deletion

  /**
   * Deletes the given auto-upload configuration and refreshes the list.
   * The call is skipped unless both {@link igId} and the configuration's
   * `idConfiguration` are defined.
   *
   * @param configuration The configuration to delete.
   */
  public async deleteConfiguration(configuration: AutoUploadConfiguration) {
    if (
      this.igId() !== undefined &&
      configuration.idConfiguration !== undefined
    ) {
      await this.autoUploadService.deleteAutoUploadEntryAsync({
        id: this.igId(),
        configurationId: configuration.idConfiguration.toString(),
      });
      await this.loadConfigurations();
    }
  }

  /**
   * Refreshes the listing after an inline delete operation completes,
   * re-fetching the current page.
   */
  public async redisplayListAfterDelete() {
    await this.changePage(this.listingOptions);
  }

  /**
   * Enables or disables (toggles) the given configuration and refreshes the
   * list. Configurations with a `status` of 2 are considered non-toggleable
   * and are left unchanged, as is the case when {@link igId} is undefined.
   * The new enabled state is derived from the current status (enabled when the
   * current status equals 0).
   *
   * @param configuration The configuration whose enabled state is toggled.
   */
  public async toggleConfiguration(configuration: AutoUploadConfiguration) {
    if (this.igId() !== undefined && configuration.status !== 2) {
      await this.autoUploadService.putAutoUploadEntryAsync({
        id: this.igId(),
        configurationId: String(configuration.idConfiguration),
        enable: configuration.status === 0,
      });
      await this.loadConfigurations();
    }
  }

  /**
   * Parses the cron-style `dateRestriction` of a configuration into its schedule
   * components.
   *
   * @param configuration The configuration whose `dateRestriction` is parsed.
   * @returns An empty array when no restriction is set; a two-element
   * `[hour, dayNumber]` array when the value matches the expected cron pattern
   * (`* * <hour> ? * <day>`); otherwise a single-element array with the raw
   * `dateRestriction` value.
   */
  public prettyPrintDateRestriction(
    configuration: AutoUploadConfiguration
  ): string[] {
    if (configuration.dateRestriction === undefined) {
      return [];
    }
    const match = /\* \* (\d+|.*) \? \* (\d+|.*)/.exec(
      configuration.dateRestriction
    );
    if (match !== null) {
      const hour = match[1];
      const dayNum = match[2];
      return [hour, dayNum];
    }
    return [configuration.dateRestriction];
  }
}
