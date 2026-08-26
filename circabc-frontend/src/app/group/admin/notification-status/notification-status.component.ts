import { Location } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { MatTooltipModule } from '@angular/material/tooltip';
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
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-notification-status',
  templateUrl: './notification-status.component.html',
  styleUrl: './notification-status.component.scss',
  preserveWhitespaces: true,
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
  public searchForm!: FormGroup;
  public searchFormConfig!: FormGroup;
  public showSearchBox1 = false;
  public showSearchBox2 = false;
  public showAddModal = false;
  public user!: User;
  public fromPage = 'library';

  constructor(
    private route: ActivatedRoute,
    private notificationService: NotificationService,
    private loginService: LoginService,
    private nodesService: NodesService,
    private location: Location,
    private fb: FormBuilder,
    private fbConfig: FormBuilder
  ) {}

  ngOnInit() {
    this.notificationConfigurations = [];
    this.subscribedUsers = [];

    this.route.queryParams.subscribe((params) => {
      this.fromPage = params.from;
    });

    this.route.params.subscribe(async (params) => {
      this.igId = params.id;
      await this.listNotificationStatus(params);
    });

    if (this.user === undefined) {
      this.user = this.loginService.getUser();
    }

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
    await this.loadNotificationConfigurations(10);
    await this.loadSubscribedUsers(10);
    this.ready = true;
  }

  // paging of notification configurations

  public async loadNotificationConfigurations(limit: number) {
    this.loading = true;
    this.listingOptionsConfigurations.limit = limit;
    if (this.nodeId !== undefined) {
      const result: PagedNotificationConfigurations = await firstValueFrom(
        this.notificationService.getNotificationConfigurations(
          this.nodeId,
          this.listingOptionsConfigurations.limit,
          this.listingOptionsConfigurations.page,
          this.user.uiLang,
          this.searchFormConfig.controls.configType.value,
          this.searchFormConfig.controls.configUserName.value,
          this.searchFormConfig.controls.configStatus.value
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
          this.user.uiLang,
          this.searchFormConfig.controls.configType.value,
          this.searchFormConfig.controls.configUserName.value,
          this.searchFormConfig.controls.configStatus.value
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
    await this.loadNotificationConfigurations(10);
    await this.loadSubscribedUsers(10);
  }

  public async deleteConfiguration(configuration: NotificationConfiguration) {
    await firstValueFrom(
      this.notificationService.deleteNotification(
        this.nodeId,
        configuration.authority as string
      )
    );
    await this.loadNotificationConfigurations(10);
    await this.loadSubscribedUsers(10);
  }

  // paging of subscribed users

  public async loadSubscribedUsers(limit: number) {
    this.loading = true;
    this.listingOptionsUsers.limit = limit;
    if (this.nodeId !== undefined) {
      const result: PagedSubscribedUsers = await firstValueFrom(
        this.notificationService.getNotificationSubscribedUsers(
          this.nodeId,
          this.listingOptionsUsers.limit,
          this.listingOptionsUsers.page,
          this.searchForm.controls.userName.value,
          this.searchForm.controls.firstName.value,
          this.searchForm.controls.lastName.value,
          this.searchForm.controls.email.value
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
          this.listingOptionsUsers.page,
          this.searchForm.controls.userName.value,
          this.searchForm.controls.firstName.value,
          this.searchForm.controls.lastName.value,
          this.searchForm.controls.email.value
        )
      );
      this.subscribedUsers = result.data;
      this.totalUsers = result.total;
    }
  }

  public async refresh(_result: ActionEmitterResult) {
    await this.loadNotificationConfigurations(10);
    await this.loadSubscribedUsers(10);
    this.showAddModal = false;
  }

  public goBack() {
    this.location.back();
  }

  isForumNewsgroupsName(name: string, type: string) {
    return name === 'Newsgroups' && type.includes('forums');
  }

  public async resetSearchConfig() {
    this.searchFormConfig.patchValue({
      configType: '',
      configUserName: '',
      configStatus: '',
    });

    await this.loadNotificationConfigurations(10);
  }

  public async resetSearch() {
    this.searchForm.patchValue({
      userName: '',
      firstName: '',
      lastName: '',
      email: '',
    });

    await this.loadSubscribedUsers(10);
  }

  public async changeLimit(limit: number) {
    this.listingOptionsUsers.limit = limit;
    this.listingOptionsUsers.page = 1;
    await this.loadNotificationConfigurations(limit);
  }

  public async changeLimitResult(limit: number) {
    this.listingOptionsUsers.limit = limit;
    this.listingOptionsUsers.page = 1;
    await this.loadSubscribedUsers(limit);
  }
}
