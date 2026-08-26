import { Component, OnInit } from '@angular/core';
import {
  ActivatedRoute,
  Data,
  Router,
  RouterLink,
  RouterOutlet,
} from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { DataCyDirective } from 'app/shared/directives/data-cy.directive';
import { HorizontalLoaderComponent } from 'app/shared/loader/horizontal-loader.component';
import { SetTitlePipe } from 'app/shared/pipes/set-title.pipe';
import { environment } from 'environments/environment';
import { LoginService } from 'app/core/login.service';
import { MatDialog } from '@angular/material/dialog';
import { PagedUserProfile } from 'app/core/generated/circabc';

import { InterestGroup } from 'app/core/generated/circabc/model/interestGroup';
import { User } from 'app/core/generated/circabc';
import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import { DeleteRequestGroupComponent } from './delete-request-group/delete-request-group.component';

@Component({
  selector: 'cbc-group-admin',
  templateUrl: './group-admin.component.html',
  preserveWhitespaces: true,
  imports: [
    HorizontalLoaderComponent,
    DataCyDirective,
    RouterLink,
    RouterOutlet,
    SetTitlePipe,
    TranslocoModule,
  ],
})
export class GroupAdminComponent implements OnInit {
  public loading = false;
  public featureDisabled = false;
  public group?: InterestGroup;
  public isExternalRepositoryEnabled = false;
  public pageUserProfile: PagedUserProfile | undefined;
  public displayRequestDeleteIg = false;
  public user: User | undefined;
  public showActionsDropdown = false;
  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private loginService: LoginService,
    public dialog: MatDialog,
    private permissionEvaluatorService: PermissionEvaluatorService
  ) {}

  async ngOnInit() {
    this.isExternalRepositoryEnabled = environment.aresBridgeEnabled;
    this.route.data.subscribe((value: Data) => {
      this.group = value.group;
    });
    this.checkIfUserIsLeader();
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
    if (this.group?.permissions && this.group.permissions.IgDelete === 'true') {
      return true;
    }
    return false;
  }
  private async checkIfUserIsLeader(): Promise<void> {
    this.displayRequestDeleteIg = await this.isUserLeader();
  }
  private async isUserLeader(): Promise<boolean> {
    this.user = this.loginService.getUser();
    if (this.group) {
      return this.permissionEvaluatorService.isGroupAdmin(this.group);
    }
    return false;
  }

  toggleDropdown(event: Event) {
    this.showActionsDropdown = !this.showActionsDropdown;
    event.stopPropagation();
  }

  openDialogRequestDeleteGroup() {
    const dialogRef = this.dialog.open(DeleteRequestGroupComponent, {
      data: {
        group: this.group,
      },
    });

    dialogRef.afterClosed().subscribe(() => {});
  }
}
