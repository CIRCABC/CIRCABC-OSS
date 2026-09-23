import {
  ChangeDetectionStrategy,
  Component,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import {
  ActivatedRoute,
  Data,
  Router,
  RouterLink,
  RouterOutlet,
} from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import { PagedUserProfile, User } from 'app/core/generated/circabc';
import { InterestGroup } from 'app/core/generated/circabc/model/interestGroup';
import { LoginService } from 'app/core/login.service';
import { DataCyDirective } from 'app/shared/directives/data-cy.directive';
import { HorizontalLoaderComponent } from 'app/shared/loader/horizontal-loader.component';
import { SetTitlePipe } from 'app/shared/pipes/set-title.pipe';
import { VerifyConditionsDeleteIgComponent } from 'app/shared/verify-conditions-delete-ig/verify-conditions-delete-ig.component';
import { environment } from 'environments/environment';
import { DeleteRequestGroupComponent } from './delete-request-group/delete-request-group.component';

/**
 * Interest group administration shell component.
 *
 * Renders the group administration area for a single interest group: a
 * navigation surface (tabs / links backed by {@link RouterLink}) that switches
 * between the various admin sub-sections (general, security, logos, document
 * lifecycle, summary, auto-upload, paste notifications, notification status,
 * log, external repository and delete) and a {@link RouterOutlet} hosting the
 * currently selected sub-section. It also exposes an actions dropdown that lets
 * an authorised group leader request the deletion of the group.
 *
 * The component resolves the current {@link InterestGroup} from the activated
 * route data, determines the current user's leadership status via the
 * {@link PermissionEvaluatorService} and reflects environment feature flags
 * (such as the ARES external repository integration).
 */
@Component({
  selector: 'cbc-group-admin',
  templateUrl: './group-admin.component.html',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
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
  /** Router used to inspect the active URL and drive route-based navigation state. */
  private readonly router = inject(Router);
  /** Activated route providing the resolved {@link InterestGroup} via route data. */
  private readonly route = inject(ActivatedRoute);
  /** Service exposing the currently authenticated user. */
  private readonly loginService = inject(LoginService);
  /** Angular Material dialog service used to open the group deletion dialogs. */
  dialog = inject(MatDialog);
  /** Evaluates the current user's permissions/roles against the current group. */
  private readonly permissionEvaluatorService = inject(
    PermissionEvaluatorService
  );

  /** Whether an asynchronous operation is in progress; drives the loader display. */
  public loading = false;
  /** Whether the current admin feature/section is disabled. */
  public featureDisabled = false;
  /** The interest group currently being administered, resolved from route data. */
  public group = signal<InterestGroup | undefined>(undefined);
  /** Whether the ARES external repository integration is enabled in this environment. */
  public isExternalRepositoryEnabled = false;
  /** Paged collection of user profiles associated with the group, when loaded. */
  public pageUserProfile: PagedUserProfile | undefined;
  /** Whether the "request group deletion" action should be shown (user is a leader). */
  public displayRequestDeleteIg = signal(false);
  /** The currently authenticated user, populated during leadership evaluation. */
  public user: User | undefined;
  /** Whether the actions dropdown menu is currently expanded. */
  public showActionsDropdown = false;

  /**
   * Angular lifecycle hook run after component initialization.
   *
   * Reads the ARES external repository feature flag from the environment,
   * subscribes to the route data to obtain the {@link InterestGroup} being
   * administered, and evaluates whether the current user is a group leader.
   *
   * @returns A promise that resolves once initialization has completed.
   */
  ngOnInit() {
    this.isExternalRepositoryEnabled = environment.aresBridgeEnabled;
    this.route.data.subscribe((value: Data) => {
      this.group.set(value.group);
    });
    this.checkIfUserIsLeader();
  }

  /**
   * Determines whether the given route segment is part of the active URL.
   *
   * @param routeName The route segment/name to look for in the current URL.
   * @returns `true` if the current router URL contains the segment, otherwise `false`.
   */
  public checkCurrentRouteActive(routeName: string): boolean {
    return this.router.url.includes(routeName);
  }

  /**
   * @returns `true` when the delete sub-section is active.
   */
  public isDeleteRoute(): boolean {
    return this.checkCurrentRouteActive('delete');
  }

  /**
   * @returns `true` when the general settings sub-section is active.
   */
  public isGeneralRoute(): boolean {
    return this.checkCurrentRouteActive('general');
  }

  /**
   * @returns `true` when the logos sub-section is active.
   */
  public isLogosRoute(): boolean {
    return this.checkCurrentRouteActive('logos');
  }

  /**
   * @returns `true` when the security sub-section is active.
   */
  public isSecurityRoute(): boolean {
    return this.checkCurrentRouteActive('security');
  }

  /**
   * @returns `true` when the document lifecycle sub-section is active.
   */
  public isDocumentLifecycleRoute(): boolean {
    return this.checkCurrentRouteActive('documents');
  }

  /**
   * @returns `true` when the summary sub-section is active.
   */
  public isSummaryRoute(): boolean {
    return this.checkCurrentRouteActive('summary');
  }

  /**
   * @returns `true` when the auto-upload sub-section is active.
   */
  public isAutoUploadRoute(): boolean {
    return this.checkCurrentRouteActive('auto-upload');
  }

  /**
   * @returns `true` when the paste notifications sub-section is active.
   */
  public isPasteNotificationsRoute(): boolean {
    return this.checkCurrentRouteActive('paste-notifications');
  }

  /**
   * @returns `true` when the notification status sub-section is active.
   */
  public isNotificationStatusRoute(): boolean {
    return this.checkCurrentRouteActive('notification-status');
  }

  /**
   * Determines whether the log sub-section is active.
   *
   * Explicitly excludes the `logos` route so that the shared `log` substring
   * does not cause a false positive while browsing logos.
   *
   * @returns `true` when the log sub-section is active (and not the logos route).
   */
  public isLogRoute(): boolean {
    return (
      this.checkCurrentRouteActive('log') &&
      !this.checkCurrentRouteActive('logos')
    );
  }

  /**
   * @returns `true` when the external repository sub-section is active.
   */
  public isExternalRepositoryRoute(): boolean {
    return this.checkCurrentRouteActive('external-repository');
  }

  /**
   * Indicates whether the current user is allowed to delete the group.
   *
   * @returns `true` if the resolved group grants the `IgDelete` permission, otherwise `false`.
   */
  public canDeleteGroup() {
    if (this.group()?.permissions?.IgDelete === 'true') {
      return true;
    }
    return false;
  }
  /**
   * Evaluates whether the current user is a group leader and toggles the
   * visibility of the "request group deletion" action accordingly.
   *
   * @returns A promise that resolves once {@link displayRequestDeleteIg} has been set.
   */
  private async checkIfUserIsLeader(): Promise<void> {
    this.displayRequestDeleteIg.set(await this.isUserLeader());
  }
  /**
   * Loads the current user and checks whether they administer the current group.
   *
   * @returns A promise resolving to `true` when the current user is a group
   * admin/leader of the resolved group, or `false` when there is no group or
   * the user lacks the role.
   */
  private async isUserLeader(): Promise<boolean> {
    this.user = this.loginService.getUser();
    const group = this.group();
    if (group) {
      return this.permissionEvaluatorService.isGroupAdmin(group);
    }
    return false;
  }

  /**
   * Toggles the actions dropdown menu.
   *
   * @param event The triggering DOM event; its propagation is stopped to
   * prevent immediate closing by outside-click handlers.
   */
  toggleDropdown(event: Event) {
    this.showActionsDropdown = !this.showActionsDropdown;
    event.stopPropagation();
  }

  /**
   * Opens the group deletion request flow.
   *
   * First opens the {@link VerifyConditionsDeleteIgComponent} dialog to confirm
   * the pre-conditions for deletion. If the user confirms readiness, the
   * {@link DeleteRequestGroupComponent} dialog is opened to submit the actual
   * deletion request. The current {@link group} is passed as dialog data.
   */
  openDialogRequestDeleteGroup() {
    const dialogRefValidation = this.dialog.open(
      VerifyConditionsDeleteIgComponent,
      {
        ariaLabel: 'Dialog',
        data: {
          group: this.group(),
        },
      }
    );

    dialogRefValidation.afterClosed().subscribe((isReadyToDelete) => {
      if (isReadyToDelete) {
        const dialogRef = this.dialog.open(DeleteRequestGroupComponent, {
          ariaLabel: 'Dialog',
          data: {
            group: this.group(),
          },
        });

        dialogRef.afterClosed().subscribe(() => {});
      }
    });
  }
}
