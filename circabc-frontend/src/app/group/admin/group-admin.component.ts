import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Data, Router } from '@angular/router';
import { InterestGroup } from 'app/core/generated/circabc';
import { environment } from 'environments/environment';

@Component({
  selector: 'cbc-group-admin',
  templateUrl: './group-admin.component.html',
  preserveWhitespaces: true,
})
export class GroupAdminComponent implements OnInit {
  public loading = false;
  public featureDisabled = false;
  public group!: InterestGroup;
  public isExternalRepositoryEnabled = false;

  constructor(private router: Router, private route: ActivatedRoute) {}

  ngOnInit() {
    this.isExternalRepositoryEnabled = environment.aresBridgeEnabled;
    this.route.data.subscribe((value: Data) => {
      this.group = value.group;
    });
  }

  public checkCurrentRouteActive(routeName: string): boolean {
    return this.router.url.includes(routeName);
  }

  public isDeleteRoute(): boolean {
    return this.checkCurrentRouteActive('delete');
  }

  public isGeneralRoute(): boolean {
    return this.checkCurrentRouteActive('general');
  }

  public isLogosRoute(): boolean {
    return this.checkCurrentRouteActive('logos');
  }

  public isSecurityRoute(): boolean {
    return this.checkCurrentRouteActive('security');
  }

  public isDocumentLifecycleRoute(): boolean {
    return this.checkCurrentRouteActive('documents');
  }

  public isSummaryRoute(): boolean {
    return this.checkCurrentRouteActive('summary');
  }

  public isAutoUploadRoute(): boolean {
    return this.checkCurrentRouteActive('auto-upload');
  }

  public isPasteNotificationsRoute(): boolean {
    return this.checkCurrentRouteActive('paste-notifications');
  }

  public isNotificationStatusRoute(): boolean {
    return this.checkCurrentRouteActive('notification-status');
  }

  public isLogRoute(): boolean {
    return (
      this.checkCurrentRouteActive('log') &&
      !this.checkCurrentRouteActive('logos')
    );
  }

  public isExternalRepositoryRoute(): boolean {
    return this.checkCurrentRouteActive('external-repository');
  }

  public canDeleteGroup() {
    if (
      this.group &&
      this.group.permissions &&
      this.group.permissions.IgDelete === 'true'
    ) {
      return true;
    } else {
      return false;
    }
  }
}
