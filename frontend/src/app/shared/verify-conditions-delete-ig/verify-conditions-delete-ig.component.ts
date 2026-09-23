import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  inject,
  OnInit,
} from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { TranslocoModule } from '@jsverse/transloco';
import {
  CategoryService,
  ContentService,
  GroupDeletionReport,
  GroupDeletionRequestApproval,
  InterestGroupService,
  ProfileService,
  SpaceService,
} from 'app/core/generated/circabc';
import { BreadcrumbComponent } from 'app/group/breadcrumb/breadcrumb.component';
import { HorizontalLoaderComponent } from 'app/shared/loader/horizontal-loader.component';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';

/**
 * Dialog component that verifies whether an interest group can be safely
 * deleted and, when necessary, helps the user clear the blocking conditions.
 *
 * Rendered inside a Material dialog, it displays a breakdown of the group
 * deletion report (locked nodes, shared nodes and shared profiles) together
 * with loaders/spinners while checks and clean-up operations run. It exposes
 * actions to remove the outstanding blockers (checkout locks, cross-group
 * shares and exported profiles) and re-verifies the conditions after each
 * clean-up. Once no blockers remain, it closes the dialog with a `true`
 * result to signal that deletion may proceed.
 *
 * Key collaborators:
 * - {@link InterestGroupService} — evaluates whether the group is deletable.
 * - {@link ContentService} — removes checkout locks on locked nodes.
 * - {@link SpaceService} — lists and removes shared spaces between groups.
 * - {@link ProfileService} — updates shared/exported profiles.
 * - {@link MatDialogRef} / {@link MAT_DIALOG_DATA} — dialog control and the
 *   injected group context to verify.
 */
@Component({
  selector: 'cbc-verify-conditions-delete-ig',
  templateUrl: './verify-conditions-delete-ig.component.html',
  styleUrl: './verify-conditions-delete-ig.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    SpinnerComponent,
    HorizontalLoaderComponent,
    BreadcrumbComponent,
    TranslocoModule,
  ],
})
export class VerifyConditionsDeleteIgComponent implements OnInit {
  /** Data injected into the dialog; expected to carry the target `group` to verify. */
  data = inject(MAT_DIALOG_DATA);
  /** Reference to this dialog, used to close it (with `true`) once deletion is allowed. */
  dialogRef = inject(MatDialogRef<VerifyConditionsDeleteIgComponent>);
  /** Service for category-related operations. */
  categoryService = inject(CategoryService);
  /** Service used to check whether the interest group is deletable. */
  groupService = inject(InterestGroupService);
  /** Service used to remove checkout locks on locked content nodes. */
  contentService = inject(ContentService);
  /** Service used to list and delete shared spaces between interest groups. */
  spaceService = inject(SpaceService);
  /** Service used to update shared/exported profiles. */
  profileService = inject(ProfileService);
  private readonly changeDetectorRef = inject(ChangeDetectorRef);

  /** True while the deletion conditions are being fetched/verified. */
  verifying = false;
  /** True while checkout locks are being cleaned. */
  cleaningLocks = false;
  /** True while shared nodes are being unshared. */
  cleaningSharedNodes = false;
  /** True while shared profiles are being reset. */
  cleaningSharedProfiles = false;
  /** True while a deletion is in progress. */
  deleting = false;
  /** The latest group deletion report, or `undefined` before it is loaded. */
  conditions: GroupDeletionReport | undefined;
  /** Approval payload associated with the group deletion request. */
  groupDeletionRequestApproval!: GroupDeletionRequestApproval;

  /**
   * Angular lifecycle hook. Triggers the initial verification of the group's
   * deletion conditions when the component is initialized.
   */
  ngOnInit(): void {
    this.verifyConditions();
  }

  /**
   * Fetches the group deletion report for the current group and evaluates
   * whether the group is ready for deletion. Toggles {@link verifying} around
   * the asynchronous request; errors are logged and swallowed so the dialog
   * stays responsive.
   *
   * @returns A promise that resolves once verification has completed.
   */
  public async verifyConditions() {
    this.verifying = true;
    if (this.data.group.id) {
      try {
        this.conditions = await this.groupService.isGroupDeletableAsync({
          id: this.data.group.id,
        });
        this.isReadyForDeletion();
      } catch (error) {
        console.error(error);
      }
    }
    this.verifying = false;
    // OnPush: verifying/conditions are updated from this async verification.
    this.changeDetectorRef.markForCheck();
  }

  /**
   * Determines whether the group has no remaining deletion blockers. When the
   * report contains no locked nodes, shared nodes or shared profiles, the
   * dialog is closed with a `true` result to indicate deletion may proceed.
   *
   * @returns Always `false`; readiness is signalled by closing the dialog
   *   rather than through the return value.
   */
  public isReadyForDeletion() {
    if (
      this.conditions?.lockedNodes &&
      this.conditions.sharedNodes &&
      this.conditions.sharedProfiles
    ) {
      if (
        this.conditions.lockedNodes.length === 0 &&
        this.conditions.sharedNodes.length === 0 &&
        this.conditions.sharedProfiles.length === 0
      ) {
        this.dialogRef.close(true);
      }
    }
    return false;
  }

  /**
   * Removes all outstanding checkout locks reported for the group by deleting
   * each locked node's checkout, then re-runs {@link verifyConditions} shortly
   * after. Toggles {@link cleaningLocks}; errors are logged and swallowed.
   *
   * @returns A promise that resolves once the clean-up requests have been sent.
   */
  public async cleanLocks() {
    this.cleaningLocks = true;
    try {
      if (this.conditions?.lockedNodes) {
        for (const node of this.conditions.lockedNodes) {
          if (node.id) {
            await this.contentService.deleteCheckoutAsync({ id: node.id });
          }
        }
      }
      setTimeout(() => {
        this.verifyConditions();
      }, 1000);
    } catch (error) {
      console.error(error);
    }
    this.cleaningLocks = false;
    // OnPush: cleaningLocks is cleared after the async clean-up.
    this.changeDetectorRef.markForCheck();
  }

  /**
   * Unshares every shared node reported for the group. For each shared node it
   * looks up the invited interest groups and deletes each share, then re-runs
   * {@link verifyConditions} shortly after. Toggles {@link cleaningSharedNodes};
   * errors are logged and swallowed.
   *
   * @returns A promise that resolves once the unshare requests have been sent.
   */
  public async cleanSharedNodes() {
    this.cleaningSharedNodes = true;
    try {
      if (this.conditions?.sharedNodes) {
        for (const sharedNode of this.conditions.sharedNodes) {
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
        setTimeout(() => {
          this.verifyConditions();
        }, 1000);
      }
    } catch (error) {
      console.error(error);
    }
    this.cleaningSharedNodes = false;
    // OnPush: cleaningSharedNodes is cleared after the async clean-up.
    this.changeDetectorRef.markForCheck();
  }

  /**
   * Resets every shared profile reported for the group by marking it as no
   * longer exported and persisting the change, then re-runs
   * {@link verifyConditions} shortly after. Toggles
   * {@link cleaningSharedProfiles}; errors are logged and swallowed.
   *
   * @returns A promise that resolves once the profile updates have been sent.
   */
  public async cleanSharedProfiles() {
    this.cleaningSharedProfiles = true;
    try {
      if (this.conditions?.sharedProfiles) {
        for (const profile of this.conditions.sharedProfiles) {
          profile.exported = false;
          if (profile.id) {
            await this.profileService.putProfileAsync({
              id: profile.id,
              profile,
            });
          }
        }
      }
      setTimeout(() => {
        this.verifyConditions();
      }, 1000);
    } catch (error) {
      console.error(error);
    }
    this.cleaningSharedProfiles = false;
    // OnPush: cleaningSharedProfiles is cleared after the async clean-up.
    this.changeDetectorRef.markForCheck();
  }
}
