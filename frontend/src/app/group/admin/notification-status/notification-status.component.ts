import { Location } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ActivatedRoute } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { ActionEmitterResult } from 'app/action-result';
import {
  Node as ModelNode,
  NodesService,
  NotificationConfiguration,
  NotificationService,
  PagedNotificationConfigurations,
  PagedSubscribedUsers,
  SubscribedUser,
  User,
} from 'app/core/generated/circabc/';
import { LoginService } from 'app/core/login.service';
import { ListingOptions } from 'app/group/listing-options/listing-options';
import { AddNotificationsComponent } from 'app/shared/add-notifications/add-notifications.component';
import { HorizontalLoaderComponent } from 'app/shared/loader/horizontal-loader.component';
import { PagerComponent } from 'app/shared/pager/pager.component';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';

/**
 * Administration screen that renders the notification status for a given
 * node (document, folder or forum) within an interest group.
 *
 * The component displays two paged, searchable tables:
 * - Notification configurations: the per-authority subscription settings
 *   (with the ability to toggle their status or delete them).
 * - Subscribed users: the users currently receiving notifications for the
 *   node.
 *
 * It also exposes an "add notifications" modal (via
 * {@link AddNotificationsComponent}) and a back navigation action.
 *
 * Data is retrieved through the generated {@link NotificationService} and
 * {@link NodesService}; the current user (used for UI language) is obtained
 * from {@link LoginService}. Both listings support independent search forms,
 * paging and page-size changes.
 *
 * Route parameters provide the target `nodeId` and interest group `id`, while
 * the `from` query parameter records the originating page (e.g. `library`).
 */
@Component({
  selector: 'cbc-notification-status',
  templateUrl: './notification-status.component.html',
  styleUrl: './notification-status.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    HorizontalLoaderComponent,
    PagerComponent,
    ReactiveFormsModule,
    MatTooltipModule,
    AddNotificationsComponent,
    I18nPipe,
    TranslocoModule,
  ],
})
export class NotificationStatusComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly notificationService = inject(NotificationService);
  private readonly loginService = inject(LoginService);
  private readonly nodesService = inject(NodesService);
  private readonly location = inject(Location);
  private readonly fb = inject(FormBuilder);
  private readonly fbConfig = inject(FormBuilder);

  /** Current page of notification configurations shown in the first table. */
  public readonly notificationConfigurations = signal<
    NotificationConfiguration[]
  >([]);
  /** Paging/sorting state (page, limit, sort) for the configurations table. */
  public readonly listingOptionsConfigurations = signal<ListingOptions>({
    page: 1,
    limit: 5,
    sort: '',
  });
  /** Total number of notification configurations available across all pages. */
  public readonly totalConfigurations = signal(5);

  /** Current page of subscribed users shown in the second table. */
  public readonly subscribedUsers = signal<SubscribedUser[]>([]);
  /** Paging/sorting state (page, limit, sort) for the subscribed users table. */
  public readonly listingOptionsUsers = signal<ListingOptions>({
    page: 1,
    limit: 5,
    sort: '',
  });
  /** Total number of subscribed users available across all pages. */
  public readonly totalUsers = signal(5);

  /** Identifier of the node whose notifications are being managed. */
  public nodeId!: string;
  /** Identifier of the interest group the node belongs to. */
  public readonly igId = signal('');
  /**
   * Whether the node is reached from within an interest group. `false` when the
   * node has no interest group root master group (e.g. a standalone library node).
   */
  public fromIG = true;
  /** The resolved node model loaded from the backend for `nodeId`. */
  public readonly currentNode = signal<ModelNode | undefined>(undefined);
  /** Indicates an in-flight request; used to show the loading indicator. */
  public readonly loading = signal(false);
  /** Becomes `true` once initial data has finished loading and the view can render. */
  public readonly ready = signal(false);
  /** Reactive form backing the subscribed users search box. */
  public searchForm!: FormGroup;
  /** Reactive form backing the notification configurations search box. */
  public searchFormConfig!: FormGroup;
  /** Toggles visibility of the configurations search box. */
  public showSearchBox1 = false;
  /** Toggles visibility of the subscribed users search box. */
  public showSearchBox2 = false;
  /** Toggles visibility of the "add notifications" modal. */
  public readonly showAddModal = signal(false);
  /** The currently authenticated user (used for the UI language). */
  public user!: User;
  /** Originating page recorded from the `from` query parameter (defaults to `library`). */
  public readonly fromPage = signal('library');

  /**
   * Angular lifecycle hook. Initializes the listing arrays, subscribes to route
   * query and path parameters to resolve the originating page and node, loads
   * the current user, and builds the two reactive search forms.
   *
   * @returns Nothing.
   */
  ngOnInit() {
    this.route.queryParams.subscribe((params) => {
      this.fromPage.set(params.from);
    });

    this.route.params.subscribe(async (params) => {
      this.igId.set(params.id);
      await this.listNotificationStatus(params);
    });

    this.user ??= this.loginService.getUser();

    this.searchFormConfig = this.fbConfig.group(
      {
        configType: [''],
        configUserName: [''],
        configStatus: [''],
      },
      {
        updateOn: 'change',
      }
    );

    this.searchForm = this.fb.group(
      {
        userName: [''],
        firstName: [''],
        lastName: [''],
        email: [''],
      },
      {
        updateOn: 'change',
      }
    );
  }

  /**
   * Resolves the target node from the route parameters and, when found, loads
   * the initial notification configurations and subscribed users.
   *
   * If `nodeId` is absent the method returns early (the alternative Library
   * route may not provide it). Also determines {@link fromIG} based on whether
   * the node belongs to an interest group root master group.
   *
   * @param params Route path parameters, expected to contain `id` (interest
   *   group id) and optionally `nodeId`.
   * @returns A promise that resolves once loading has completed and the view is ready.
   */
  private async listNotificationStatus(params: { [key: string]: string }) {
    if (params.nodeId === undefined) {
      // exit here if the parameter is null and try to get the parameter from the alternative route (from Library)
      return;
    }
    this.nodeId = params.nodeId;
    const node = await this.nodesService.getNodeAsync({ id: this.nodeId });
    this.currentNode.set(node);
    if (
      node.properties &&
      node.properties.circaIGRootMasterGroup === undefined
    ) {
      this.fromIG = false;
    } else {
      this.igId.set(params.id);
    }
    await this.loadNotificationConfigurations(10);
    await this.loadSubscribedUsers(10);
    this.ready.set(true);
  }

  // paging of notification configurations

  /**
   * Loads the first page of notification configurations for the current node
   * using the given page size and the current configurations search filters.
   * Updates {@link notificationConfigurations} and {@link totalConfigurations}.
   *
   * @param limit Maximum number of configurations to fetch per page.
   * @returns A promise that resolves once the configurations have been loaded.
   */
  public async loadNotificationConfigurations(limit: number) {
    this.loading.set(true);
    this.listingOptionsConfigurations.update((options) => ({
      ...options,
      limit,
    }));
    if (this.nodeId !== undefined) {
      const result: PagedNotificationConfigurations =
        await this.notificationService.getNotificationConfigurationsAsync({
          id: this.nodeId,
          limit: this.listingOptionsConfigurations().limit,
          page: this.listingOptionsConfigurations().page,
          language: this.user.uiLang,
          type: this.searchFormConfig.controls.configType.value,
          userName: this.searchFormConfig.controls.configUserName.value,
          status: this.searchFormConfig.controls.configStatus.value,
        });
      this.notificationConfigurations.set(result.data);
      this.totalConfigurations.set(result.total);
    }
    this.loading.set(false);
  }

  /**
   * Navigates the configurations table to the requested page and reloads it.
   *
   * @param page One-based page number to display.
   * @returns A promise that resolves once the page has been loaded.
   */
  public async goToPageConfigurations(page: number) {
    this.listingOptionsConfigurations.update((options) => ({
      ...options,
      page,
    }));
    await this.changePageConfigurations(this.listingOptionsConfigurations());
  }

  /**
   * Applies new listing options to the configurations table and reloads it
   * using the current configurations search filters. Updates
   * {@link notificationConfigurations} and {@link totalConfigurations}.
   *
   * @param listingOptions The paging/sorting options to apply.
   * @returns A promise that resolves once the configurations have been reloaded.
   */
  public async changePageConfigurations(listingOptions: ListingOptions) {
    this.listingOptionsConfigurations.set(listingOptions);
    if (this.nodeId !== undefined) {
      const result: PagedNotificationConfigurations =
        await this.notificationService.getNotificationConfigurationsAsync({
          id: this.nodeId,
          limit: this.listingOptionsConfigurations().limit,
          page: this.listingOptionsConfigurations().page,
          language: this.user.uiLang,
          type: this.searchFormConfig.controls.configType.value,
          userName: this.searchFormConfig.controls.configUserName.value,
          status: this.searchFormConfig.controls.configStatus.value,
        });
      this.notificationConfigurations.set(result.data);
      this.totalConfigurations.set(result.total);
    }
  }

  /**
   * Toggles the subscription status of a single configuration between
   * `SUBSCRIBED` and `UNSUBSCRIBED`, persists it, then refreshes both listings.
   *
   * @param configuration The configuration whose status should be flipped.
   * @returns A promise that resolves once the status has been updated and the
   *   listings reloaded.
   */
  public async toggleConfigurationStatus(
    configuration: NotificationConfiguration
  ) {
    const newStatus: NotificationConfiguration.StatusEnum =
      configuration.status === 'UNSUBSCRIBED' ? 'SUBSCRIBED' : 'UNSUBSCRIBED';
    await this.notificationService.postNotificationStatusAsync({
      id: this.nodeId,
      authority: configuration.authority as string,
      status: newStatus,
    });
    await this.loadNotificationConfigurations(10);
    await this.loadSubscribedUsers(10);
  }

  /**
   * Deletes a notification configuration for the current node, then refreshes
   * both listings.
   *
   * @param configuration The configuration to delete.
   * @returns A promise that resolves once the configuration has been deleted
   *   and the listings reloaded.
   */
  public async deleteConfiguration(configuration: NotificationConfiguration) {
    await this.notificationService.deleteNotificationAsync({
      id: this.nodeId,
      authority: configuration.authority as string,
    });
    await this.loadNotificationConfigurations(10);
    await this.loadSubscribedUsers(10);
  }

  // paging of subscribed users

  /**
   * Loads the first page of subscribed users for the current node using the
   * given page size and the current users search filters. Updates
   * {@link subscribedUsers} and {@link totalUsers}.
   *
   * @param limit Maximum number of users to fetch per page.
   * @returns A promise that resolves once the subscribed users have been loaded.
   */
  public async loadSubscribedUsers(limit: number) {
    this.loading.set(true);
    this.listingOptionsUsers.update((options) => ({ ...options, limit }));
    if (this.nodeId !== undefined) {
      const result: PagedSubscribedUsers =
        await this.notificationService.getNotificationSubscribedUsersAsync({
          id: this.nodeId,
          limit: this.listingOptionsUsers().limit,
          page: this.listingOptionsUsers().page,
          userName: this.searchForm.controls.userName.value,
          firstName: this.searchForm.controls.firstName.value,
          lastName: this.searchForm.controls.lastName.value,
          email: this.searchForm.controls.email.value,
        });
      this.subscribedUsers.set(result.data);
      this.totalUsers.set(result.total);
    }
    this.loading.set(false);
  }

  /**
   * Navigates the subscribed users table to the requested page and reloads it.
   *
   * @param page One-based page number to display.
   * @returns A promise that resolves once the page has been loaded.
   */
  public async goToPageUsers(page: number) {
    this.listingOptionsUsers.update((options) => ({ ...options, page }));
    await this.changePageUsers(this.listingOptionsUsers());
  }

  /**
   * Applies new listing options to the subscribed users table and reloads it
   * using the current users search filters. Updates {@link subscribedUsers}
   * and {@link totalUsers}.
   *
   * @param listingOptions The paging/sorting options to apply.
   * @returns A promise that resolves once the users have been reloaded.
   */
  public async changePageUsers(listingOptions: ListingOptions) {
    this.listingOptionsUsers.set(listingOptions);

    if (this.nodeId !== undefined) {
      const result: PagedSubscribedUsers =
        await this.notificationService.getNotificationSubscribedUsersAsync({
          id: this.nodeId,
          limit: this.listingOptionsUsers().limit,
          page: this.listingOptionsUsers().page,
          userName: this.searchForm.controls.userName.value,
          firstName: this.searchForm.controls.firstName.value,
          lastName: this.searchForm.controls.lastName.value,
          email: this.searchForm.controls.email.value,
        });
      this.subscribedUsers.set(result.data);
      this.totalUsers.set(result.total);
    }
  }

  /**
   * Callback invoked after notifications are added via the modal. Reloads both
   * listings and closes the "add notifications" modal.
   *
   * @param _result The result emitted by the add-notifications action (unused).
   * @returns A promise that resolves once the listings have been reloaded.
   */
  public async refresh(_result: ActionEmitterResult) {
    await this.loadNotificationConfigurations(10);
    await this.loadSubscribedUsers(10);
    this.showAddModal.set(false);
  }

  /**
   * Navigates back to the previous location in the browser history.
   *
   * @returns Nothing.
   */
  public goBack() {
    this.location.back();
  }

  /**
   * Determines whether a node represents the special "Newsgroups" node of a
   * forum, used to tailor the displayed label.
   *
   * @param name The node name.
   * @param type The node type string.
   * @returns `true` when the name is `Newsgroups` and the type includes
   *   `forums`; otherwise `false`.
   */
  isForumNewsgroupsName(name: string, type: string) {
    return name === 'Newsgroups' && type.includes('forums');
  }

  /**
   * Clears the notification configurations search form and reloads the
   * configurations table.
   *
   * @returns A promise that resolves once the configurations have been reloaded.
   */
  public async resetSearchConfig() {
    this.searchFormConfig.patchValue({
      configType: '',
      configUserName: '',
      configStatus: '',
    });

    await this.loadNotificationConfigurations(10);
  }

  /**
   * Clears the subscribed users search form and reloads the users table.
   *
   * @returns A promise that resolves once the users have been reloaded.
   */
  public async resetSearch() {
    this.searchForm.patchValue({
      userName: '',
      firstName: '',
      lastName: '',
      email: '',
    });

    await this.loadSubscribedUsers(10);
  }

  /**
   * Changes the page size and resets to the first page, then reloads the
   * notification configurations table.
   *
   * @param limit The new page size to apply.
   * @returns A promise that resolves once the configurations have been reloaded.
   */
  public async changeLimit(limit: number) {
    this.listingOptionsUsers.update((options) => ({
      ...options,
      limit,
      page: 1,
    }));
    await this.loadNotificationConfigurations(limit);
  }

  /**
   * Changes the page size and resets to the first page, then reloads the
   * subscribed users table.
   *
   * @param limit The new page size to apply.
   * @returns A promise that resolves once the users have been reloaded.
   */
  public async changeLimitResult(limit: number) {
    this.listingOptionsUsers.update((options) => ({
      ...options,
      limit,
      page: 1,
    }));
    await this.loadSubscribedUsers(limit);
  }
}
