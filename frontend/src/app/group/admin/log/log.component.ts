import {
  ChangeDetectionStrategy,
  Component,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { ActivatedRoute, Params } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import {
  AuditActivity,
  AuditService,
  BASE_PATH,
  LogSearchResult,
  MembersService,
  PagedUserProfile,
} from 'app/core/generated/circabc';
import { SaveAsService } from 'app/core/save-as.service';
import { IdName } from 'app/core/ui-model';
import { dateLessThan, futureDateValidator } from 'app/core/validation.service';
import { ControlMessageComponent } from 'app/shared/control-message/control-message.component';
import { SetTitlePipe } from 'app/shared/pipes/set-title.pipe';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';

/**
 * Standalone Angular component that renders the audit log search screen for a
 * group's administration area.
 *
 * It presents a reactive filter form (user, service, activity and a date
 * range) built from the group's members and available audit activities, lets
 * administrators run an audit search whose results are displayed in the
 * template, and offers export of the audit log to a downloadable file in a
 * chosen format.
 *
 * Key collaborators:
 * - {@link MembersService} to load the group's members for the user filter.
 * - {@link AuditService} to load available audit activities and to run audit
 *   searches.
 * - {@link SaveAsService} to trigger the download of an exported audit log.
 *
 * The current group is resolved from the `id` route parameter via
 * {@link ActivatedRoute}.
 */
@Component({
  selector: 'cbc-log',
  templateUrl: 'log.component.html',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ReactiveFormsModule,
    MatDatepickerModule,
    MatInputModule,
    MatFormFieldModule,
    ControlMessageComponent,
    SpinnerComponent,
    SetTitlePipe,
    TranslocoModule,
  ],
})
export class LogComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly fb = inject(FormBuilder);
  private readonly membersService = inject(MembersService);
  private readonly auditService = inject(AuditService);
  private readonly saveAsService = inject(SaveAsService);

  /** Reactive form holding the audit search filters (users, services, activities, dateFrom, dateTo). */
  public form!: FormGroup;
  /** Identifier of the group whose audit log is being searched, resolved from the route. */
  public groupId!: string;
  /** Raw paged list of the group's user profiles used to build the {@link users} filter options. */
  private pagedUserProfile!: PagedUserProfile;
  /** Full list of audit activities for the group, used to derive the service and activity filter options. */
  private allActivities!: AuditActivity[];
  /** User filter options (id/name pairs), prefixed with an "All" entry. */
  public readonly users = signal<IdName[] | undefined>(undefined);
  /** Activity filter options (id/name pairs), prefixed with an "All" entry; may be narrowed by the selected service. */
  public readonly activities = signal<IdName[]>([]);
  /** Service filter options (id/name pairs), prefixed with an "All" entry. */
  public readonly services = signal<IdName[] | undefined>(undefined);
  /** Results of the most recent audit search. */
  public readonly searchResults = signal<LogSearchResult[] | undefined>(
    undefined
  );
  /** Whether a search or export request is currently in progress. */
  public readonly loading = signal(false);
  /** Whether the initial page data (users and activities) is currently being loaded. */
  public readonly preloading = signal(false);

  /** Backend API base path used to build the audit export URL. */
  private readonly basePath!: string;

  /**
   * Injects the backend {@link BASE_PATH} token and stores it in
   * {@link basePath} when available, for later use when building export URLs.
   */
  constructor() {
    const basePath = inject(BASE_PATH);

    if (basePath) {
      this.basePath = basePath;
    }
  }

  /**
   * Angular lifecycle hook. Builds the filter form with default values (a
   * date range spanning the last 7 days) and validators, then subscribes to
   * route parameter changes to (re)load the page data for the current group.
   */
  ngOnInit() {
    this.form = this.fb.group(
      {
        users: [''],
        services: [''],
        activities: [''],
        dateFrom: [
          this.get7DaysAgo(),
          [Validators.required, futureDateValidator],
        ],
        dateTo: [new Date(), [Validators.required, futureDateValidator]],
      },
      {
        validators: dateLessThan('dateFrom', 'dateTo'),
        updateOn: 'change',
      }
    );
    this.route.params.subscribe(async (params) => {
      this.preloading.set(true);
      await this.loadPageData(params);
      this.preloading.set(false);
    });
  }

  /**
   * Loads and prepares all data needed to populate the filter dropdowns for a
   * group: members (users), audit activities, and the derived services and
   * activities option lists.
   *
   * @param params - Route parameters; the `id` entry identifies the group.
   * @returns A promise that resolves once all page data has been loaded and processed.
   */
  private async loadPageData(params: Params) {
    this.groupId = params.id;
    this.pagedUserProfile = await this.loadUsers();
    this.createUsers();
    this.allActivities = await this.loadActivities();
    this.createServices();
    this.createActivities();
  }

  /**
   * Rebuilds the {@link activities} filter options from {@link allActivities},
   * removing duplicates, sorting alphabetically and prepending an "All" entry.
   *
   * @param service - Optional service identifier; when provided, only
   * activities belonging to that service are included.
   */
  private createActivities(service?: string) {
    let duplicateActivities: string[] = [];
    if (service) {
      duplicateActivities = this.allActivities
        .filter((activity) => activity.service === service)
        .map((activity) => activity.name);
    } else {
      duplicateActivities = this.allActivities.map((activity) => activity.name);
    }

    const uniqueActivities = duplicateActivities.filter((elem, pos) => {
      return duplicateActivities.indexOf(elem) === pos;
    });

    uniqueActivities.sort((a, b) => a.localeCompare(b));
    const activities = uniqueActivities.map((activity) => {
      return { id: activity, name: activity };
    });
    activities.unshift({ id: '', name: 'All' });
    this.activities.set(activities);
  }

  /**
   * Builds the {@link services} filter options from the distinct services
   * found in {@link allActivities}, sorted alphabetically and prefixed with an
   * "All" entry.
   */
  private createServices() {
    const duplicateServices = this.allActivities.map(
      (activity) => activity.service
    );
    const uniqueServices = duplicateServices.filter((elem, pos) => {
      return duplicateServices.indexOf(elem) === pos;
    });
    uniqueServices.sort((a, b) => a.localeCompare(b));
    const services = uniqueServices.map((value) => {
      return { id: value, name: value };
    });
    services.unshift({ id: '', name: 'All' });
    this.services.set(services);
  }

  /**
   * Builds the {@link users} filter options from {@link pagedUserProfile},
   * mapping each profile to an id/name pair (full name) and prepending an
   * "All" entry. Profiles missing required fields map to an empty entry.
   */
  private createUsers() {
    if (this.pagedUserProfile.data && this.pagedUserProfile.data.length > 0) {
      const users = this.pagedUserProfile.data.map((userProfile) => {
        if (
          userProfile.user?.userId &&
          userProfile.user.firstname &&
          userProfile.user.lastname
        ) {
          return {
            id: userProfile.user.userId,
            name: `${userProfile.user.firstname} ${userProfile.user.lastname}`,
          };
        }
        return { id: '', name: '' };
      });
      users.unshift({ id: '', name: 'All' });
      this.users.set(users);
    }
  }

  /**
   * Computes the date exactly 7 days before now, used as the default value for
   * the `dateFrom` filter.
   *
   * @returns A {@link Date} representing the moment 7 days ago.
   */
  private get7DaysAgo(): Date {
    const milliseconds = Date.now() - 7 * 24 * 60 * 60 * 1000;
    return new Date(milliseconds);
  }

  /**
   * Loads the members of the current group via {@link MembersService}.
   *
   * @returns A promise resolving to the paged user profiles for the group.
   */
  private async loadUsers() {
    return this.membersService.getMembersAsync({
      id: this.groupId,
      searchQuery: '',
    });
  }
  /**
   * Loads all audit activities available for the current group via
   * {@link AuditService}.
   *
   * @returns A promise resolving to the list of {@link AuditActivity} items.
   */
  private async loadActivities() {
    return this.auditService.getAllAuditActivitiesAsync({ id: this.groupId });
  }

  /**
   * Handles a change of the selected service: rebuilds the activity options
   * restricted to that service and resets the activity filter selection.
   *
   * @param _id - The selected service identifier (currently unused; the value
   * is read from the form control instead).
   */
  public serviceClick(_id: string) {
    const service = this.form.controls.services.value;
    this.createActivities(service);
    this.form.controls.activities.setValue('');
  }

  /**
   * Builds the audit export URL from the current filter values and triggers a
   * file download in the requested format via {@link SaveAsService}.
   *
   * @param exportCode - Export format code (e.g. file extension) that also
   * determines the downloaded file name.
   */
  public export(exportCode: string) {
    this.loading.set(true);
    const from = new Date(
      new Date(this.form.controls.dateFrom.value).setHours(0, 0, 0, 0)
    ).toISOString();
    const to = new Date(
      new Date(this.form.controls.dateTo.value).setHours(23, 59, 59, 999)
    ).toISOString();
    const userId = this.form.controls.users.value;
    const service = this.form.controls.services.value;
    const activity = this.form.controls.activities.value;
    const url = `${this.basePath}/audit/${this.groupId}?userId=${userId}&service=${service}&activity=${activity}&from=${from}&to=${to}&format=${exportCode}`;
    // eslint-disable-next-line no-console
    console.log(url);
    const name = `AuditLog.${exportCode}`;
    this.saveAsService.saveUrlAs(url, name);
    this.loading.set(false);
  }

  /**
   * Runs an audit search using the current filter values (when the form is
   * valid) and stores the outcome in {@link searchResults}. The date range is
   * normalized so that `dateFrom` starts at the beginning of the day and
   * `dateTo` ends at the end of the day.
   *
   * @returns A promise that resolves once the search completes; no search is
   * performed when the form is invalid.
   */
  public async search() {
    if (this.form.valid) {
      this.loading.set(true);
      const from = new Date(
        new Date(this.form.controls.dateFrom.value).setHours(0, 0, 0, 0)
      );
      const to = new Date(
        new Date(this.form.controls.dateTo.value).setHours(23, 59, 59, 999)
      );
      this.searchResults.set(
        await this.auditService.getAuditsAsync({
          id: this.groupId,
          from: from.toISOString(),
          to: to.toISOString(),
          userId: this.form.controls.users.value,
          service: this.form.controls.services.value,
          activity: this.form.controls.activities.value,
        })
      );
      this.loading.set(false);
    }
  }

  /**
   * Accessor for the `dateFrom` form control, convenient for template
   * validation messages.
   *
   * @returns The `dateFrom` {@link AbstractControl}.
   */
  get dateFrom(): AbstractControl {
    return this.form.controls.dateFrom;
  }

  /**
   * Accessor for the `dateTo` form control, convenient for template validation
   * messages.
   *
   * @returns The `dateTo` {@link AbstractControl}.
   */
  get dateTo(): AbstractControl {
    return this.form.controls.dateTo;
  }
}
