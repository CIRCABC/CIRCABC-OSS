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
import { MatTooltipModule } from '@angular/material/tooltip';
import { GroupLockService, PagedUserProfile } from 'app/core/generated/circabc';
import { GroupLockInfo } from 'app/core/generated/circabc/model/groupLockInfo';

import { InterestGroup } from 'app/core/generated/circabc/model/interestGroup';
import { User } from 'app/core/generated/circabc';
import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { GroupLockStateService } from 'app/core/group-lock-state.service';
import { DeleteRequestGroupComponent } from './delete-request-group/delete-request-group.component';
import {
  LockGroupDialogComponent,
  LockGroupDialogData,
} from './lock-group/lock-group-dialog.component';
import { VerifyConditionsDeleteIgComponent } from 'app/shared/verify-conditions-delete-ig/verify-conditions-delete-ig.component';
import { firstValueFrom } from 'rxjs';

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
    MatTooltipModule,
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
  public lockInfo: GroupLockInfo | undefined;

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private loginService: LoginService,
    public dialog: MatDialog,
    private permissionEvaluatorService: PermissionEvaluatorService,
    private groupLockService: GroupLockService,
    private uiMessageService: UiMessageService,
    private groupLockStateService: GroupLockStateService
  ) {}

  async ngOnInit() {
    this.isExternalRepositoryEnabled = environment.aresBridgeEnabled;
    this.route.data.subscribe((value: Data) => {
      this.group = value.group;
      this.loadLockInfo();
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
    // Defensive check: never open the request-delete flow for a locked IG.
    // The button is also hidden in the template when locked; this guards
    // against race conditions where the lock state changed since the last
    // template render.
    if (this.isGroupLocked()) {
      this.uiMessageService.addWarningMessage(
        'text.group.delete.locked.tooltip',
        true
      );
      return;
    }

    const dialogRefValidation = this.dialog.open(
      VerifyConditionsDeleteIgComponent,
      {
        data: {
          group: this.group,
        },
      }
    );

    dialogRefValidation.afterClosed().subscribe((isReadyToDelete) => {
      if (isReadyToDelete) {
        const dialogRef = this.dialog.open(DeleteRequestGroupComponent, {
          data: {
            group: this.group,
          },
        });

        dialogRef.afterClosed().subscribe(() => {});
      }
    });
  }

  isGroupLocked(): boolean {
    return this.lockInfo?.locked === true;
  }

  openLockDialog(): void {
    if (!this.group?.id) {
      return;
    }

    const dialogRef = this.dialog.open(LockGroupDialogComponent, {
      data: { groupId: this.group.id } as LockGroupDialogData,
    });

    dialogRef.afterClosed().subscribe((result: boolean) => {
      if (result) {
        this.loadLockInfo();
        this.groupLockStateService.setLocked(true);
      }
    });
  }

  async unlockGroup(): Promise<void> {
    if (!this.group?.id) {
      return;
    }

    try {
      await firstValueFrom(this.groupLockService.unlockGroup(this.group.id));
      this.uiMessageService.addSuccessMessage(
        'successfully unlocked the interest group',
        true
      );
      this.groupLockStateService.setLocked(false);
      await this.loadLockInfo();
    } catch (err) {
      this.uiMessageService.addErrorMessage(err);
    }
  }

  private async loadLockInfo(): Promise<void> {
    if (!this.group?.id) {
      return;
    }

    try {
      this.lockInfo = await firstValueFrom(
        this.groupLockService.getGroupLockInfo(this.group.id)
      );
    } catch {
      this.lockInfo = { locked: false };
    }

    // Keep the group's own lockInfo in sync with the freshly loaded state so
    // that any consumer receiving `this.group` (e.g. child dialogs that read
    // `data.group.lockInfo`) sees the current lock status instead of the
    // stale value returned by the route resolver.
    if (this.group) {
      this.group.lockInfo = this.lockInfo;
    }
  }
}
