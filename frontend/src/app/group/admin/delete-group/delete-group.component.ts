import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  OnInit,
  output,
  signal,
} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import {
  ContentService,
  GroupDeletionReport,
  InterestGroupService,
  ProfileService,
  SpaceService,
} from 'app/core/generated/circabc';

import { BreadcrumbComponent } from 'app/group/breadcrumb/breadcrumb.component';
import { DataCyDirective } from 'app/shared/directives/data-cy.directive';
import { HorizontalLoaderComponent } from 'app/shared/loader/horizontal-loader.component';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';

/**
 * Component that drives the deletion workflow for an interest group.
 *
 * It renders a guided screen that first verifies whether a group can be
 * safely deleted (checking for locked nodes, shared nodes and shared
 * profiles), then lets the administrator clean up each blocking condition,
 * and finally performs the deletion. While long-running operations are in
 * progress it shows loading indicators (spinner / horizontal loader) and a
 * breadcrumb for navigation context.
 *
 * The component supports two usage modes:
 * - Route mode: the group id is read from the route parameter (`params.id`)
 *   and, on successful deletion, the user is navigated to `/explore`.
 * - Embedded mode: the group id is provided via the {@link groupId} input and
 *   the outcome is communicated to the parent through the
 *   {@link groupDeleted} output instead of navigating.
 *
 * Key collaborators are the generated CIRCABC API services
 * ({@link InterestGroupService}, {@link ContentService},
 * {@link SpaceService}, {@link ProfileService}) used to inspect and resolve
 * the deletion preconditions and to delete the group.
 */
@Component({
  selector: 'cbc-delete-group',
  templateUrl: './delete-group.component.html',
  styleUrl: './delete-group.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    DataCyDirective,
    SpinnerComponent,
    HorizontalLoaderComponent,
    BreadcrumbComponent,
    TranslocoModule,
  ],
})
export class DeleteGroupComponent implements OnInit {
  /** API service used to check deletability of and to delete the group. */
  private readonly groupService = inject(InterestGroupService);
  /** Provides access to the current route parameters (e.g. the group id). */
  private readonly route = inject(ActivatedRoute);
  /** Router used to navigate away after deletion or cancellation. */
  private readonly router = inject(Router);
  /** API service used to release locks (cancel check-outs) on nodes. */
  private readonly contentService = inject(ContentService);
  /** API service used to inspect and remove space shares. */
  private readonly spaceService = inject(SpaceService);
  /** API service used to update (un-share) access profiles. */
  private readonly profileService = inject(ProfileService);

  /**
   * Optional input carrying the id of the interest group to delete.
   *
   * When provided the component operates in embedded mode: instead of
   * navigating after deletion it emits the outcome through
   * {@link groupDeleted}.
   */
  readonly groupId = input<string>();
  /**
   * Emitted when the deletion workflow finishes in embedded mode.
   *
   * Emits `true` when the group has been deleted and `false` when the user
   * cancels the operation.
   */
  readonly groupDeleted = output<boolean>();

  /** Resolved id of the interest group being deleted (route param or input). */
  public igNode!: string;
  /** Latest deletability report describing the blocking conditions. */
  public readonly conditions = signal<GroupDeletionReport>(
    undefined as unknown as GroupDeletionReport
  );
  /** True while the deletability verification request is in progress. */
  public readonly verifying = signal(false);
  /** True once a deletability report has been successfully retrieved. */
  public readonly verified = signal(false);
  /** True while locked nodes are being released. */
  public readonly cleaningLocks = signal(false);
  /** True while shared nodes are being un-shared. */
  public readonly cleaningSharedNodes = signal(false);
  /** True while shared profiles are being un-shared. */
  public readonly cleaningSharedProfiles = signal(false);
  /** True while the group deletion request is in progress. */
  public readonly deleting = signal(false);

  /**
   * Angular lifecycle hook that resolves the target group id.
   *
   * It subscribes to the route parameters to pick up an `id` param and, if a
   * {@link groupId} input was supplied, uses it as the effective group id
   * (taking precedence over the route parameter).
   */
  ngOnInit() {
    this.route.params.subscribe((params) => {
      if (params.id) {
        this.igNode = params.id;
      }
    });
    const groupId = this.groupId();
    if (groupId) {
      this.igNode = groupId;
    }
  }

  /**
   * Requests the deletability report for the current group and stores it in
   * {@link conditions}.
   *
   * Toggles {@link verifying} around the asynchronous call and, on success,
   * sets {@link verified} to true. Errors are caught and logged so the UI can
   * recover; they are not re-thrown.
   *
   * @returns A promise that resolves once verification has completed.
   */
  public async verifyConditions() {
    this.verifying.set(true);
    try {
      this.conditions.set(
        await this.groupService.isGroupDeletableAsync({ id: this.igNode })
      );
      this.verified.set(true);
    } catch (error) {
      console.error(error);
    }
    this.verifying.set(false);
  }

  /**
   * Determines whether the group has no remaining blocking conditions.
   *
   * @returns `true` when the latest {@link conditions} report contains no
   * locked nodes, shared nodes or shared profiles; otherwise `false`
   * (including when no report is available yet).
   */
  public isReadyForDeletion() {
    const conditions = this.conditions();
    if (
      conditions?.lockedNodes &&
      conditions.sharedNodes &&
      conditions.sharedProfiles
    ) {
      return (
        conditions.lockedNodes.length === 0 &&
        conditions.sharedNodes.length === 0 &&
        conditions.sharedProfiles.length === 0
      );
    }

    return false;
  }

  /**
   * Releases the locks on every locked node reported in {@link conditions}
   * by cancelling their check-outs.
   *
   * Toggles {@link cleaningLocks} around the operation. If no error occurs the
   * locked nodes are cleared locally (via a cloned {@link conditions} object
   * to avoid mutating the original reference). Errors are caught and logged so
   * the local state is left unchanged.
   *
   * @returns A promise that resolves once all locks have been processed.
   */
  public async cleanLocks() {
    this.cleaningLocks.set(true);
    let errorOccurred = false;
    try {
      const conditions = this.conditions();
      if (conditions?.lockedNodes) {
        for (const node of conditions.lockedNodes) {
          if (node.id) {
            await this.contentService.deleteCheckoutAsync({ id: node.id });
          }
        }
      }
    } catch (error) {
      errorOccurred = true;
      console.error(error);
    }
    if (!errorOccurred) {
      // clone this.conditions to avoid reference issues
      this.conditions.set({ ...this.conditions(), lockedNodes: [] });
    }
    this.cleaningLocks.set(false);
  }

  /**
   * Removes every share on the shared nodes reported in {@link conditions}.
   *
   * For each shared node it looks up the interest groups it is shared with and
   * deletes each share, then re-runs {@link verifyConditions} to refresh the
   * report. Toggles {@link cleaningSharedNodes} around the operation. Errors
   * are caught and logged; they are not re-thrown.
   *
   * @returns A promise that resolves once all shared nodes have been processed.
   */
  public async cleanSharedNodes() {
    this.cleaningSharedNodes.set(true);
    try {
      const conditions = this.conditions();
      if (conditions?.sharedNodes) {
        for (const sharedNode of conditions.sharedNodes) {
          if (sharedNode.id) {
            const invitedIgs = await this.spaceService.getShareSpacesAsync({
              id: sharedNode.id,
              limit: 0,
              page: 1,
            });
            for (const share of invitedIgs.data) {
              await this.spaceService.deleteShareSpaceAsync({
                id: sharedNode.id,
                sharedIGId: share.igId as string,
              });
            }
          }
        }
        this.verifyConditions();
      }
    } catch (error) {
      console.error(error);
    }
    this.cleaningSharedNodes.set(false);
  }

  /**
   * Un-shares (un-exports) every shared profile reported in
   * {@link conditions}.
   *
   * For each profile it sets `exported` to false and persists the change via
   * the profile service, then re-runs {@link verifyConditions} to refresh the
   * report. Toggles {@link cleaningSharedProfiles} around the operation.
   * Errors are caught and logged; they are not re-thrown.
   *
   * @returns A promise that resolves once all shared profiles have been
   * processed.
   */
  public async cleanSharedProfiles() {
    this.cleaningSharedProfiles.set(true);
    try {
      const conditions = this.conditions();
      if (conditions?.sharedProfiles) {
        for (const profile of conditions.sharedProfiles) {
          profile.exported = false;
          if (profile.id) {
            await this.profileService.putProfileAsync({
              id: profile.id,
              profile: profile,
            });
          }
        }
      }
      this.verifyConditions();
    } catch (error) {
      console.error(error);
    }
    this.cleaningSharedProfiles.set(false);
  }

  /**
   * Performs the group deletion.
   *
   * In embedded mode (when {@link groupId} is set) it does not call the API
   * and instead emits `true` through {@link groupDeleted}. Otherwise it calls
   * the API to delete the interest group and navigates to `/explore`. Toggles
   * {@link deleting} around the operation. Errors are caught and logged; they
   * are not re-thrown.
   *
   * @returns A promise that resolves once the deletion flow has completed.
   */
  public async deleteGroup() {
    this.deleting.set(true);

    try {
      if (this.groupId()) {
        this.groupDeleted.emit(true);
      } else {
        await this.groupService.deleteInterestGroupAsync({
          id: this.igNode,
          purgedata: true,
          purgelogs: true,
        });
        this.router.navigate(['/explore']);
      }
    } catch (error) {
      console.error(error);
    }
    this.deleting.set(false);
  }
  /**
   * Handles a cancellation of the deletion workflow.
   *
   * In embedded mode (when {@link groupId} is set) it emits `false` through
   * {@link groupDeleted}; otherwise it navigates one level up in the route.
   */
  onCancelClick(): void {
    if (this.groupId()) {
      this.groupDeleted.emit(false);
    } else {
      this.router.navigate(['..']);
    }
  }
}
