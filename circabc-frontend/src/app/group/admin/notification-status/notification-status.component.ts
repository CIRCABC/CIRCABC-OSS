import { Location } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

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
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { ListingOptions } from 'app/group/listing-options/listing-options';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-notification-status',
  templateUrl: './notification-status.component.html',
  styleUrls: ['./notification-status.component.scss'],
  preserveWhitespaces: true,
})
export class NotificationStatusComponent implements OnInit {
  public notificationConfigurations!: NotificationConfiguration[];
  public listingOptionsConfigurations: ListingOptions = {
    page: 1,
    limit: 5,
    sort: '',
  };
  public totalConfigurations = 5;

  public subscribedUsers!: SubscribedUser[];
  public listingOptionsUsers: ListingOptions = { page: 1, limit: 5, sort: '' };
  public totalUsers = 5;

  public nodeId!: string;
  public igId!: string;
  public fromIG = true;
  public currentNode!: ModelNode;
  public loading = false;
  public ready = false;

  public showAddModal = false;

  public user!: User;

  constructor(
    private route: ActivatedRoute,
    private notificationService: NotificationService,
    private loginService: LoginService,
    private nodesService: NodesService,
    private location: Location
  ) {}

  ngOnInit() {
    this.notificationConfigurations = [];
    this.subscribedUsers = [];
    this.route.params.subscribe(async (params) => {
      this.igId = params.id;
      await this.listNotificationStatus(params);
    });

    if (this.user === undefined) {
      this.user = this.loginService.getUser();
    }
  }

  private async listNotificationStatus(params: { [key: string]: string }) {
    if (params.nodeId === undefined) {
      // exit here if the parameter is null and try to get the parameter from the alternative route (from Library)
      return;
    }
    this.nodeId = params.nodeId;
    this.currentNode = await firstValueFrom(
      this.nodesService.getNode(this.nodeId)
    );
    if (
      this.currentNode.properties &&
      this.currentNode.properties.circaIGRootMasterGroup === undefined
    ) {
      this.fromIG = false;
    } else {
      this.igId = params.id;
    }
    await this.loadNotificationConfigurations();
    await this.loadSubscribedUsers();
    this.ready = true;
  }

  // paging of notification configurations

  private async loadNotificationConfigurations() {
    this.loading = true;
    if (this.nodeId !== undefined) {
      const result: PagedNotificationConfigurations = await firstValueFrom(
        this.notificationService.getNotificationConfigurations(
          this.nodeId,
          this.listingOptionsConfigurations.limit,
          this.listingOptionsConfigurations.page,
          this.user.uiLang
        )
      );
      this.notificationConfigurations = result.data;
      this.totalConfigurations = result.total;
    }
    this.loading = false;
  }

  public async goToPageConfigurations(page: number) {
    this.listingOptionsConfigurations.page = page;
    await this.changePageConfigurations(this.listingOptionsConfigurations);
  }

  public async changePageConfigurations(listingOptions: ListingOptions) {
    this.listingOptionsConfigurations = listingOptions;
    if (this.nodeId !== undefined) {
      const result: PagedNotificationConfigurations = await firstValueFrom(
        this.notificationService.getNotificationConfigurations(
          this.nodeId,
          this.listingOptionsConfigurations.limit,
          this.listingOptionsConfigurations.page,
          this.user.uiLang
        )
      );
      this.notificationConfigurations = result.data;
      this.totalConfigurations = result.total;
    }
  }

  public async toggleConfigurationStatus(
    configuration: NotificationConfiguration
  ) {
    const newStatus: NotificationConfiguration.StatusEnum =
      configuration.status === 'UNSUBSCRIBED' ? 'SUBSCRIBED' : 'UNSUBSCRIBED';
    await firstValueFrom(
      this.notificationService.postNotificationStatus(
        this.nodeId,
        configuration.authority as string,
        newStatus
      )
    );
    await this.loadNotificationConfigurations();
    await this.loadSubscribedUsers();
  }

  public async deleteConfiguration(configuration: NotificationConfiguration) {
    await firstValueFrom(
      this.notificationService.deleteNotification(
        this.nodeId,
        configuration.authority as string
      )
    );
    await this.loadNotificationConfigurations();
    await this.loadSubscribedUsers();
  }

  // paging of subscribed users

  private async loadSubscribedUsers() {
    this.loading = true;
    if (this.nodeId !== undefined) {
      const result: PagedSubscribedUsers = await firstValueFrom(
        this.notificationService.getNotificationSubscribedUsers(
          this.nodeId,
          this.listingOptionsUsers.limit,
          this.listingOptionsUsers.page
        )
      );
      this.subscribedUsers = result.data;
      this.totalUsers = result.total;
    }
    this.loading = false;
  }

  public async goToPageUsers(page: number) {
    this.listingOptionsUsers.page = page;
    await this.changePageUsers(this.listingOptionsUsers);
  }

  public async changePageUsers(listingOptions: ListingOptions) {
    this.listingOptionsUsers = listingOptions;
    if (this.nodeId !== undefined) {
      const result: PagedSubscribedUsers = await firstValueFrom(
        this.notificationService.getNotificationSubscribedUsers(
          this.nodeId,
          this.listingOptionsUsers.limit,
          this.listingOptionsUsers.page
        )
      );
      this.subscribedUsers = result.data;
      this.totalUsers = result.total;
    }
  }

  public async refresh(_result: ActionEmitterResult) {
    await this.loadNotificationConfigurations();
    await this.loadSubscribedUsers();
    this.showAddModal = false;
  }

  public goBack() {
    this.location.back();
  }

  isForumNewsgroupsName(name: string, type: string) {
    return name === 'Newsgroups' && type.includes('forums');
  }
}
