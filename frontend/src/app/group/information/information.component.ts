import { DatePipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { ActivatedRoute, Data, Router, RouterLink } from '@angular/router';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { assertDefined } from 'app/core/asserts';
import {
  InformationPage,
  InformationService,
  InterestGroup,
  Node as ModelNode,
  News,
  NodesService,
  NotificationService,
  PagedNews,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { getErrorTranslation } from 'app/core/util';
import { ListingOptions } from 'app/group/listing-options/listing-options';
import { HorizontalLoaderComponent } from 'app/shared/loader/horizontal-loader.component';
import { NotificationMessageComponent } from 'app/shared/notification-message/notification-message.component';
import { PagerComponent } from 'app/shared/pager/pager.component';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { IfRolePipe } from 'app/shared/pipes/if-role.pipe';
import { SetTitlePipe } from 'app/shared/pipes/set-title.pipe';
import { ShareComponent } from 'app/shared/share/share.component';
import { ConfigureInformationComponent } from './configure-information/configure-information.component';
import { NewsCardComponent } from './news-card/news-card.component';

/**
 * `cbc-information` renders the Information service page of an interest group.
 *
 * It displays the group's news items as a paginated collection of cards, an
 * optional highlighted/expanded view of a single news entry (driven by the
 * `filterId` query parameter or user interaction), an optional legacy
 * "old information" iframe, sharing controls and a configuration modal for
 * users with the appropriate permissions. It also lets the current user
 * manage their notification subscription for the information node.
 *
 * Key collaborators:
 * - {@link InformationService} — fetches information definitions and news.
 * - {@link NodesService} — loads the information {@link ModelNode}.
 * - {@link NotificationService} — updates notification subscriptions.
 * - {@link LoginService} — resolves the current user/guest state and ticket.
 * - {@link Router} / {@link ActivatedRoute} — read route data/params and
 *   drive query-parameter based highlighting.
 */
@Component({
  selector: 'cbc-information',
  templateUrl: './information.component.html',
  styleUrl: './information.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    HorizontalLoaderComponent,
    NotificationMessageComponent,
    RouterLink,
    ShareComponent,
    PagerComponent,
    NewsCardComponent,
    ConfigureInformationComponent,
    DatePipe,
    I18nPipe,
    IfRolePipe,
    SetTitlePipe,
    TranslocoModule,
  ],
})
export class InformationComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly informationService = inject(InformationService);
  private readonly loginService = inject(LoginService);
  private readonly notificationService = inject(NotificationService);
  private readonly nodesService = inject(NodesService);
  private readonly translateService = inject(TranslocoService);
  private readonly uiMessageService = inject(UiMessageService);

  /** Identifier of the interest group whose information page is displayed. */
  public readonly groupId = signal<string>('');
  /** The interest group resolved from the route data. */
  public readonly group = signal<InterestGroup | undefined>(undefined);
  /** The backing node of the information service, used for notifications. */
  public readonly informationNode = signal<ModelNode | undefined>(undefined);
  /** Configuration/definition of the information page (permissions, URL, etc.). */
  public readonly informationPage = signal<InformationPage | undefined>(
    undefined
  );
  /** Whether an asynchronous load of the information page is in progress. */
  public readonly loading = signal(false);
  /** Current page of news items together with the total count. */
  public readonly infoNews = signal<PagedNews>({ data: [], total: 0 });
  /** The news item currently highlighted/expanded, if any. */
  public readonly highlightedNews = signal<News | undefined>(undefined);
  /** Whether the single-news highlighted view is active. */
  public readonly highlightedMode = signal(false);
  /** Whether the highlighted news is shown in a maximised window. */
  public highlightedMaxWindow = false;
  /** Whether the highlighted news is rendered in iframe mode. */
  public readonly highlightedIframeMode = signal(false);
  /** Whether the information configuration modal is visible. */
  public showConfigureModal = false;
  /** Pagination and sorting options for the news listing. */
  public listingOptions: ListingOptions = { page: 1, limit: 10, sort: '' };
  /** Total number of items used by the pager. */
  public totalItems = 10;

  /**
   * Angular lifecycle hook. Subscribes to route data to resolve the current
   * {@link group} and to route params to (re)load the information page.
   */
  public ngOnInit(): void {
    this.route.data.subscribe((value: Data) => {
      this.group.set(value.group);
    });

    this.route.params.subscribe(
      async (params) => await this.loadInformation(params)
    );
  }

  /**
   * Loads the information node, page definitions and the current page of news
   * for the active group. When a `filterId` query parameter is present it
   * enables highlighted mode and selects the matching news item.
   *
   * @param params Route parameters; an `id` entry, when present, sets the
   *   {@link groupId} to load information for.
   * @returns A promise that resolves once loading has completed.
   */
  public async loadInformation(params: { [key: string]: string }) {
    this.loading.set(true);

    if (params.id) {
      this.groupId.set(params.id);
    }

    const group = this.group();
    if (this.groupId() && group?.informationId) {
      this.informationNode.set(
        await this.nodesService.getNodeAsync({ id: group.informationId })
      );
      this.informationPage.set(
        await this.informationService.getInformationDefinitionsAsync({
          id: this.groupId(),
        })
      );

      const searchedId = this.route.snapshot.queryParams.filterId as
        string | undefined;
      if (searchedId) {
        this.highlightedMode.set(true);
        this.highlightedIframeMode.set(false);
      }

      this.infoNews.set(
        await this.informationService.getInformationNewsAsync({
          id: this.groupId(),
          limit: this.listingOptions.limit,
          page: this.listingOptions.page,
        })
      );

      if (this.highlightedMode() && searchedId) {
        // Prefer the instance already loaded in the current page so that
        // reference-based selection (e.g. the sidebar check mark) keeps working.
        this.highlightedNews.set(
          this.infoNews().data.find((infoNews) => infoNews.id === searchedId)
        );

        // The selected item may live on another page (or not be part of the
        // paginated listing at all). Fetch it directly by id so the single
        // selected element is always shown.
        if (this.highlightedNews() === undefined) {
          try {
            this.highlightedNews.set(
              await this.informationService.getNewsAsync({ id: searchedId })
            );
          } catch (error) {
            console.error(error);
            this.highlightedNews.set(undefined);
          }
        }
      }
    }

    this.loading.set(false);
  }

  /**
   * Determines whether a legacy "old information" URL is configured and set
   * to be displayed.
   *
   * @returns `true` when both an information URL and the display flag are set.
   */
  hasOldInformation(): boolean {
    const informationPage = this.informationPage();
    if (informationPage?.url && informationPage.displayOldInformation) {
      return true;
    }

    return false;
  }

  /**
   * Builds a synthetic {@link News} item that renders the configured legacy
   * information URL inside an iframe, appending the authentication ticket when
   * the user is not a guest.
   *
   * @returns A news item describing the legacy iframe content, or `undefined`
   *   when no old information is configured to be displayed.
   */
  getOldInformation(): News | undefined {
    const informationPage = this.informationPage();
    if (informationPage?.url && informationPage.displayOldInformation) {
      const news: News = {};
      news.layout = 'normal';
      news.pattern = 'iframe';
      news.url = informationPage.url + this.checkTicket();
      news.title = {};
      news.size = 3;

      return news;
    }

    return undefined;
  }

  /**
   * Builds the authentication ticket query-string suffix for the current user.
   *
   * @returns An empty string for guests, otherwise `?ticket=<ticket>`.
   */
  public checkTicket(): string {
    if (this.loginService.isGuest()) {
      return '';
    }
    return `?ticket=${this.loginService.getTicket()}`;
  }

  /**
   * Indicates whether there is at least one news item to display.
   *
   * @returns `true` when the current news collection has a positive total.
   */
  public hasCards(): boolean {
    return this.infoNews().total > 0;
  }

  /**
   * Handles the outcome of a child action (e.g. from the configure modal or a
   * news card). Closes the configuration modal and, depending on the action,
   * reloads the information page or refreshes the page definitions.
   *
   * @param result The emitted action result describing the performed action
   *   and its outcome.
   * @returns A promise that resolves once any required reload has completed.
   */
  async refresh(result: ActionEmitterResult) {
    this.showConfigureModal = false;

    if (
      result.type === ActionType.DELETE_INFORMATION_NEWS &&
      result.result === ActionResult.SUCCEED
    ) {
      this.highlightedMode.set(false);
      this.highlightedIframeMode.set(false);
      this.highlightedNews.set(undefined);
      this.resetFilterAndScreen();
      await this.loadInformation({ id: this.groupId() });
    }

    if (
      result.type === ActionType.UPDATE_INFORMATION_CONFIGURATION &&
      result.result === ActionResult.SUCCEED
    ) {
      if (this.groupId()) {
        this.informationPage.set(
          await this.informationService.getInformationDefinitionsAsync({
            id: this.groupId(),
          })
        );
      }
    }
  }

  /**
   * Checks whether the current user is allowed to add news items.
   *
   * @returns `true` when the information page permissions grant `InfManage`
   *   or `InfFullEdit`.
   */
  canAddNews(): boolean {
    const informationPage = this.informationPage();
    if (informationPage?.permissions) {
      return (
        informationPage.permissions.InfManage === 'ALLOWED' ||
        informationPage.permissions.InfFullEdit === 'ALLOWED'
      );
    }

    return false;
  }

  /**
   * Toggles the highlighted view for the given news item. Selecting the
   * already-highlighted item clears the highlight; selecting a different item
   * enables highlighted mode and reflects the selection in the `filterId`
   * query parameter.
   *
   * @param news The news item to highlight, or `undefined` to do nothing.
   * @returns A promise that resolves once navigation/state updates complete.
   */
  public async highlight(news: News | undefined) {
    if (news === undefined) {
      return;
    }

    if (this.highlightedNews() === news) {
      this.resetFilterAndScreen();
    } else {
      this.highlightedMode.set(true);
      this.highlightedIframeMode.set(false);
      this.highlightedNews.set(news);

      this.router.navigate([], {
        relativeTo: this.route,
        queryParams: {
          ...this.route.snapshot.queryParams,
          filterId: news.id,
        },
      });
    }
  }

  /**
   * Clears all highlight-related state and removes the `filterId` query
   * parameter from the URL.
   */
  private resetFilterAndScreen() {
    this.highlightedMode.set(false);
    this.highlightedIframeMode.set(false);
    this.highlightedNews.set(undefined);

    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {},
    });
  }

  /**
   * Updates the current listing page and reloads the news collection.
   *
   * @param p The new page number to display.
   * @returns A promise that resolves once the page has been reloaded.
   */
  async changePage(p: number) {
    this.listingOptions.page = p;
    await this.loadInformation({});
  }

  /**
   * Indicates whether the current user is subscribed to notifications for the
   * information node.
   *
   * @returns `true` when the information node's notifications are `ALLOWED`.
   */
  public isSubscribedToNotifications(): boolean {
    const informationNode = this.informationNode();
    if (informationNode) {
      return informationNode.notifications === 'ALLOWED';
    }
    return false;
  }

  /**
   * Changes the current user's notification subscription for the information
   * node and reloads the node to reflect the new state. Displays a UI error
   * message if the update fails.
   *
   * @param value The desired subscription value to apply.
   * @returns A promise that resolves once the subscription has been updated.
   * @throws Never rethrows; update failures are caught and surfaced as a UI
   *   error message.
   */
  public async changeNotificationSubscription(value: string) {
    const informationNode = this.informationNode();
    assertDefined(informationNode);
    if (value && value !== '' && informationNode.id) {
      try {
        await this.notificationService.putNotificationAuthorityAsync({
          id: informationNode.id,
          authority: this.loginService.getCurrentUsername(),
          body: value,
        });
        this.informationNode.set(
          await this.nodesService.getNodeAsync({ id: informationNode.id })
        );
      } catch (error) {
        console.error(error);
        const text = this.translateService.translate(
          getErrorTranslation(ActionType.CHANGE_SUBSCRIPTION)
        );
        this.uiMessageService.addErrorMessage(text);
      }
    }
  }

  /**
   * Indicates whether the current user is an unauthenticated guest.
   *
   * @returns `true` when the user is a guest.
   */
  public isGuest(): boolean {
    return this.loginService.isGuest();
  }

  /**
   * Toggles the iframe presentation of the highlighted content, disabling the
   * standard highlighted mode when iframe mode is turned on.
   */
  public highlightIFrame() {
    if (this.highlightedIframeMode()) {
      this.highlightedMode.set(false);
      this.highlightedIframeMode.set(false);
    } else {
      this.highlightedMode.set(false);
      this.highlightedIframeMode.set(true);
    }
  }

  /**
   * Sets whether the highlighted content is displayed in a maximised window.
   *
   * @param value `true` to maximise the highlighted window, `false` otherwise.
   */
  maxWindowHighlighted(value: boolean) {
    this.highlightedMaxWindow = value;
  }
}
