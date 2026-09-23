import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  resource,
  signal,
  viewChild,
} from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import {
  BASE_PATH,
  EventItemDefinition,
  EventsService,
  InterestGroup,
  InterestGroupService,
  Node as ModelNode,
  NodesService,
} from 'app/core/generated/circabc';
import { SaveAsService } from 'app/core/save-as.service';
import { changeSort, getFormattedDate, getFullDate } from 'app/core/util';
import { DeleteEventComponent } from 'app/group/agenda/delete-event/delete-event.component';
import { ListingOptions } from 'app/group/listing-options/listing-options';
import { HintComponent } from 'app/shared/hint/hint.component';
import { HorizontalLoaderComponent } from 'app/shared/loader/horizontal-loader.component';
import { PagerComponent } from 'app/shared/pager/pager.component';
import { ReponsiveSubMenuComponent } from 'app/shared/reponsive-sub-menu/reponsive-sub-menu.component';

/** Temporal filter applied to the agenda listing. */
export type AgendaFilter = 'Exact' | 'Future' | 'Previous';

/**
 * Standalone component (`cbc-agenda-list`) that renders the agenda/events
 * listing for an interest group.
 *
 * It displays a paginated, sortable table of {@link EventItemDefinition}
 * appointments retrieved from the {@link EventsService}, and provides:
 * - date-based and temporal filtering (exact date, future or previous events);
 * - paging, page-size and sort controls;
 * - export of the current listing to CSV, XML or Excel via
 *   {@link SaveAsService};
 * - deletion of individual events through the embedded
 *   {@link DeleteEventComponent} modal.
 *
 * Administration-only actions are gated by the {@link PermissionEvaluatorService}
 * against the resolved event root {@link ModelNode}.
 */
@Component({
  selector: 'cbc-agenda-list',
  templateUrl: './list.component.html',
  styleUrl: './list.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    HorizontalLoaderComponent,
    ReponsiveSubMenuComponent,
    RouterLink,
    PagerComponent,
    HintComponent,
    ReactiveFormsModule,
    MatDatepickerModule,
    MatInputModule,
    MatFormFieldModule,
    DeleteEventComponent,
    TranslocoModule,
  ],
})
export class ListComponent {
  /** Provides access to the current route parameters (notably the group id). */
  private readonly route = inject(ActivatedRoute);
  /** Generated API client used to fetch and export interest group events. */
  private readonly eventsService = inject(EventsService);
  /** Factory used to build the reactive forms (export and date change). */
  private readonly formBuilder = inject(FormBuilder);
  /** Generated API client used to resolve the event root node. */
  private readonly nodesService = inject(NodesService);
  /** Evaluates whether the current user has event administration rights. */
  private readonly permEvalService = inject(PermissionEvaluatorService);
  /** Generated API client used to load the interest group details. */
  private readonly interestGroupService = inject(InterestGroupService);
  /** Service that triggers a browser download for the export URL. */
  private readonly saveAsService = inject(SaveAsService);

  /** Reference to the Material datepicker template element. */
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  readonly datePicker = viewChild<any>('datePicker');

  /** The current route params, as a signal. */
  private readonly routeParams = toSignal(this.route.params);

  /** Identifier of the interest group whose events are listed, read from the route. */
  public readonly igId = computed(() => this.routeParams()?.id ?? '');

  /** Current page (1-based) of the listing. */
  private readonly page = signal(1);
  /** Current page size of the listing. */
  private readonly limit = signal(10);
  /** Current sort specification of the listing (e.g. `title_ASC`). */
  private readonly sort = signal('');
  /** Active temporal filter applied to the listing. */
  private readonly filterSignal = signal<AgendaFilter>('Exact');
  /**
   * Reference date used for filtering, kept in sync with the
   * {@link changeDateForm} `date` control so it can be read reactively by
   * {@link eventsResource}.
   */
  private readonly date = signal(new Date());

  /**
   * Current paging, page-size and sort state for the listing, exposed for the
   * template and the pager component.
   */
  public readonly listingOptions = computed<ListingOptions>(() => ({
    page: this.page(),
    limit: this.limit(),
    sort: this.sort(),
  }));

  /** Active temporal filter applied to the listing. */
  public get filter(): AgendaFilter {
    return this.filterSignal();
  }

  /**
   * Resource loading the current page of events for the interest group taken
   * from the route parameters, according to the current paging, sort, filter
   * and reference date.
   *
   * The sort order is forced to descending appointment date for `Exact` and
   * `Future` filters, and ascending for `Previous`.
   */
  private readonly eventsResource = resource({
    params: () => {
      const igId = this.igId();
      if (!igId) {
        return undefined;
      }
      return {
        igId,
        filter: this.filterSignal(),
        exactDate: getFullDate(this.date()),
        limit: this.limit(),
        page: this.page(),
        sort:
          this.filterSignal() === 'Exact' || this.filterSignal() === 'Future'
            ? 'appointmentDate_DESC'
            : 'appointmentDate_ASC',
      };
    },
    loader: ({ params }) => {
      this.sort.set(params.sort);
      return this.eventsService.getInterestGroupListEventsAsync({
        id: params.igId,
        filter: params.filter,
        exactDate: params.exactDate,
        limit: params.limit,
        page: params.page,
        sort: params.sort,
      });
    },
  });

  /** Events currently displayed on the active page. */
  public readonly appointments = computed(
    () => this.eventsResource.value()?.data ?? []
  );
  /** Total number of events matching the current filter (for the pager). */
  public readonly totalItems = computed(
    () => this.eventsResource.value()?.total ?? 10
  );

  /** Whether an events request is in flight (drives the loader). */
  public readonly loading = this.eventsResource.isLoading;

  /**
   * Resource resolving the interest group's event root node, used for
   * permission checks. Loads once per {@link igId}.
   */
  private readonly eventRootNodeResource = resource({
    params: () => this.igId() || undefined,
    loader: async ({ params: igId }) => {
      const ig: InterestGroup =
        await this.interestGroupService.getInterestGroupAsync({
          id: igId,
        });
      return this.nodesService.getNodeAsync({ id: ig.eventId as string });
    },
  });

  /** Root node of the group's event space, used for permission checks. */
  public readonly eventRootNode = this.eventRootNodeResource.value;

  /** Reactive form holding the selected export format. */
  public exportForm!: FormGroup;
  /** Reactive form holding the reference date used for filtering. */
  public changeDateForm!: FormGroup;
  /** Available export formats offered to the user (code and display name). */
  // properties for the exporter (format to export the file and the file id)
  public exportFormats = [
    { code: 'csv', name: 'CSV' },
    { code: 'xml', name: 'XML' },
    { code: 'xls', name: 'Excel' },
  ];

  /** API base path used to build the export download URL. */
  private readonly basePath!: string;

  /** Whether the delete-event confirmation modal is visible. */
  public deleteEventShowModal = false;
  /** Event selected for deletion, bound to the delete modal. */
  public eventToDelete!: EventItemDefinition;

  /**
   * Injects and stores the API {@link BASE_PATH} token when available so it
   * can later be used to build export URLs. Also initialises the export and
   * date-change reactive forms and wires the date field to refresh the
   * listing on change.
   */
  constructor() {
    const basePath = inject(BASE_PATH);

    if (basePath) {
      this.basePath = basePath;
    }

    this.exportForm = this.formBuilder.group(
      {
        export: [this.exportFormats[0]],
      },
      {
        updateOn: 'change',
      }
    );

    this.changeDateForm = this.formBuilder.group(
      {
        date: [this.date()],
      },
      {
        updateOn: 'change',
      }
    );

    this.changeDateForm.controls.date.valueChanges.subscribe((value) => {
      this.date.set(value as Date);
      this.filterSignal.set('Exact');
      this.page.set(1);
    });
  }

  /**
   * Indicates whether the current user is an event administrator for the
   * resolved event root node.
   *
   * @returns `true` if the user has event administration rights, otherwise
   * `false`.
   */
  public isEveAdmin(): boolean {
    return this.permEvalService.isEveAdmin(this.eventRootNode() as ModelNode);
  }

  // parameter setting

  /**
   * Returns today's date formatted for display/use as the export exact date.
   *
   * @returns The current date as a formatted string.
   */
  public getFormattedTodaysDate() {
    const currentDate = new Date();
    return getFormattedDate(currentDate);
  }

  /**
   * Applies a new temporal filter and resets to the first page. The listing
   * reloads reactively via {@link eventsResource}.
   *
   * @param filter - The temporal filter to apply (`Exact`, `Future` or
   * `Previous`).
   */
  public changeFilter(filter: AgendaFilter) {
    this.filterSignal.set(filter);
    this.page.set(1);
  }

  // events paging and listing

  /**
   * Navigates to the given page. The listing reloads reactively via
   * {@link eventsResource}.
   *
   * @param page - The 1-based page number to display.
   */
  public goToPage(page: number) {
    this.page.set(page);
  }

  /**
   * Toggles/updates the sort order for the given column. The listing reloads
   * reactively via {@link eventsResource}.
   *
   * @param sort - The sort key to apply, combined with the current sort state
   * via {@link changeSort}.
   */
  public changeSort(sort: string) {
    this.sort.set(changeSort(this.sort(), sort));
  }

  /**
   * Changes the number of events displayed per page and resets to the first
   * page. The listing reloads reactively via {@link eventsResource}.
   *
   * @param limit - The new page size.
   */
  public changeLimit(limit: number) {
    this.limit.set(limit);
    this.page.set(1);
  }

  // event deletion

  /**
   * Selects an event for deletion and opens the delete confirmation modal.
   *
   * @param event - The event to delete; ignored when `undefined`.
   */
  public popupDeleteEvent(event: EventItemDefinition) {
    if (event !== undefined) {
      this.eventToDelete = event;
      this.deleteEventShowModal = true;
    }
  }

  /**
   * Reloads the current page of the listing, typically after an event has
   * been deleted.
   */
  public redisplayListAfterDelete() {
    this.eventsResource.reload();
  }

  // events export

  /**
   * Builds the export URL for the current group, filter, selected format and
   * reference date, then triggers a browser download through
   * {@link SaveAsService}.
   *
   * @returns `false` to prevent the default browser action of the triggering
   * element (e.g. an anchor click).
   */
  public export() {
    const exportCode: string = this.exportForm.value.export.code;
    const url = `${this.basePath}/groups/${this.igId()}/events/export?filter=${
      this.filter
    }&format=${exportCode}&exactDate=${getFormattedDate(
      this.changeDateForm.controls.date.value
    )}`;
    const name = `Events.${exportCode}`;
    this.saveAsService.saveUrlAs(url, name);
    return false;
  }
}
