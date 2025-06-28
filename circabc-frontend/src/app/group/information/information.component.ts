import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Data, Router, RouterLink } from '@angular/router';

import { DatePipe } from '@angular/common';
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
  News,
  Node as ModelNode,
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
import { firstValueFrom } from 'rxjs';
import { ConfigureInformationComponent } from './configure-information/configure-information.component';
import { NewsCardComponent } from './news-card/news-card.component';

@Component({
  selector: 'cbc-information',
  templateUrl: './information.component.html',
  styleUrl: './information.component.scss',
  preserveWhitespaces: true,
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
  public groupId!: string;
  public group?: InterestGroup;
  public informationNode?: ModelNode;
  public informationPage!: InformationPage;
  public loading = false;
  public infoNews: PagedNews = { data: [], total: 0 };
  public highlightedNews: News | undefined;
  public highlightedMode = false;
  public highlightedMaxWindow = false;
  public highlightedIframeMode = false;
  public showConfigureModal = false;
  public listingOptions: ListingOptions = { page: 1, limit: 10, sort: '' };
  public totalItems = 10;

  public constructor(
    private route: ActivatedRoute,
    private router: Router,
    private informationService: InformationService,
    private loginService: LoginService,
    private notificationService: NotificationService,
    private nodesService: NodesService,
    private translateService: TranslocoService,
    private uiMessageService: UiMessageService
  ) {}

  public ngOnInit(): void {
    this.route.data.subscribe((value: Data) => {
      this.group = value.group;
    });

    this.route.params.subscribe(
      async (params) => await this.loadInformation(params)
    );
  }

  public async loadInformation(params: { [key: string]: string }) {
    this.loading = true;

    if (params.id) {
      this.groupId = params.id;
    }

    if (this.groupId && this.group && this.group.informationId) {
      this.informationNode = await firstValueFrom(
        this.nodesService.getNode(this.group.informationId)
      );
      this.informationPage = await firstValueFrom(
        this.informationService.getInformationDefinitions(this.groupId)
      );

      let searchedId = '';
      this.route.queryParams.subscribe((qParams) => {
        if (qParams.filterId) {
          this.highlightedMode = true;
          this.highlightedIframeMode = false;
          searchedId = qParams.filterId;
        }
      });

      this.infoNews = await firstValueFrom(
        this.informationService.getInformationNews(
          this.groupId,
          this.listingOptions.limit,
          this.listingOptions.page
        )
      );

      if (this.highlightedMode) {
        for (const infoNews of this.infoNews.data) {
          if (infoNews.id === searchedId) {
            this.highlightedNews = infoNews;
          }
        }
      }
    }

    this.loading = false;
  }

  hasOldInformation(): boolean {
    if (
      this.informationPage?.url &&
      this.informationPage.displayOldInformation
    ) {
      return true;
    }

    return false;
  }

  getOldInformation(): News | undefined {
    if (
      this.informationPage?.url &&
      this.informationPage.displayOldInformation
    ) {
      const news: News = {};
      news.layout = 'normal';
      news.pattern = 'iframe';
      news.url = this.informationPage.url + this.checkTicket();
      news.title = {};
      news.size = 3;

      return news;
    }

    return undefined;
  }

  public checkTicket(): string {
    if (this.loginService.isGuest()) {
      return '';
    }
    return `?ticket=${this.loginService.getTicket()}`;
  }

  public hasCards(): boolean {
    return this.infoNews.total > 0;
  }

  async refresh(result: ActionEmitterResult) {
    if (
      result.type === ActionType.DELETE_INFORMATION_NEWS &&
      result.result === ActionResult.SUCCEED
    ) {
      this.highlightedMode = false;
      this.highlightedIframeMode = false;
      this.highlightedNews = undefined;
      this.resetFilterAndScreen();
      await this.loadInformation({ id: this.groupId });
    }

    if (
      result.type === ActionType.UPDATE_INFORMATION_CONFIGURATION &&
      result.result === ActionResult.CANCELED
    ) {
      this.showConfigureModal = false;
    }

    if (
      result.type === ActionType.UPDATE_INFORMATION_CONFIGURATION &&
      result.result === ActionResult.SUCCEED
    ) {
      this.showConfigureModal = false;
      if (this.groupId) {
        this.informationPage = await firstValueFrom(
          this.informationService.getInformationDefinitions(this.groupId)
        );
      }
    }
  }

  canAddNews(): boolean {
    if (this.informationPage?.permissions) {
      return (
        this.informationPage.permissions.InfManage === 'ALLOWED' ||
        this.informationPage.permissions.InfFullEdit === 'ALLOWED'
      );
    }

    return false;
  }

  public async highlight(news: News | undefined) {
    if (news === undefined) {
      return;
    }

    if (this.highlightedNews !== news) {
      this.highlightedMode = true;
      this.highlightedIframeMode = false;
      this.highlightedNews = news;
      // eslint-disable-next-line @typescript-eslint/no-floating-promises
      this.router.navigate([], {
        relativeTo: this.route,
        queryParams: {
          ...this.route.snapshot.queryParams,
          filterId: news.id,
        },
      });
    } else {
      this.resetFilterAndScreen();
    }
  }

  private resetFilterAndScreen() {
    this.highlightedMode = false;
    this.highlightedIframeMode = false;
    this.highlightedNews = undefined;
    // eslint-disable-next-line @typescript-eslint/no-floating-promises
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {},
    });
  }

  async changePage(p: number) {
    this.listingOptions.page = p;
    await this.loadInformation({});
  }

  public isSubscribedToNotifications(): boolean {
    if (this.informationNode) {
      return this.informationNode.notifications === 'ALLOWED';
    }
    return false;
  }

  public async changeNotificationSubscription(value: string) {
    assertDefined(this.informationNode);
    if (value && value !== '' && this.informationNode.id) {
      try {
        await firstValueFrom(
          this.notificationService.putNotificationAuthority(
            this.informationNode.id,
            this.loginService.getCurrentUsername(),
            value
          )
        );
        this.informationNode = await firstValueFrom(
          this.nodesService.getNode(this.informationNode.id)
        );
      } catch (_error) {
        const text = this.translateService.translate(
          getErrorTranslation(ActionType.CHANGE_SUBSCRIPTION)
        );
        this.uiMessageService.addErrorMessage(text);
      }
    }
  }

  public isGuest(): boolean {
    return this.loginService.isGuest();
  }

  public highlightIFrame() {
    if (this.highlightedIframeMode) {
      this.highlightedMode = false;
      this.highlightedIframeMode = false;
    } else {
      this.highlightedMode = false;
      this.highlightedIframeMode = true;
    }
  }

  maxWindowHighlighted(value: boolean) {
    this.highlightedMaxWindow = value;
  }
}
