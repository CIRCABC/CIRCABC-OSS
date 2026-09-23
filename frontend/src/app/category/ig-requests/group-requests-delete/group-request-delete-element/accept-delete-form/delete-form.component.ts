import {
  ChangeDetectionStrategy,
  Component,
  inject,
  OnInit,
  signal,
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
import { DataCyDirective } from 'app/shared/directives/data-cy.directive';
import { HorizontalLoaderComponent } from 'app/shared/loader/horizontal-loader.component';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';

/**
 * Dialog component used to review and approve an interest group deletion
 * request.
 *
 * Rendered inside a Material dialog, it verifies whether the target group is
 * deletable by fetching a {@link GroupDeletionReport} and displaying the
 * blocking conditions (locked nodes, shared nodes and shared profiles). It
 * offers actions to clean up each category of blocker and, once all
 * conditions are cleared, to approve the deletion request and delete the
 * interest group.
 *
 * The dialog input is provided through `MAT_DIALOG_DATA` and is expected to
 * expose `request` (with `id` and `groupId`) and `categoryId`. The dialog
 * closes with `true` when the group has been successfully deleted.
 */
@Component({
  selector: 'cbc-delete-form',
  templateUrl: './delete-form.component.html',
  styleUrl: './delete-form.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    DataCyDirective,
    SpinnerComponent,
    HorizontalLoaderComponent,
    BreadcrumbComponent,
    TranslocoModule,
  ],
})
export class AcceptDeleteFormComponent implements OnInit {
  /**
   * Data injected into the dialog via `MAT_DIALOG_DATA`. Provides the
   * deletion `request` (containing at least `id` and `groupId`) and the
   * `categoryId` under which the request was raised.
   */
  data = inject(MAT_DIALOG_DATA);
  /** Reference to the hosting Material dialog, used to close it with a result. */
  dialogRef = inject(MatDialogRef<AcceptDeleteFormComponent>);
  /** API client used to validate/approve the group deletion request. */
  categoryService = inject(CategoryService);
  /** API client used to check group deletability and to delete the group. */
  groupService = inject(InterestGroupService);
  /** API client used to remove document checkouts (locks) on nodes. */
  contentService = inject(ContentService);
  /** API client used to inspect and remove shared spaces on nodes. */
  spaceService = inject(SpaceService);
  /** API client used to update profiles so they are no longer exported/shared. */
  profileService = inject(ProfileService);

  /** True while the deletability conditions are being (re)verified. */
  verifying = signal(false);
  /** True once the deletability conditions have been successfully fetched. */
  verified = signal(false);
  /** True while locked nodes are being cleaned up. */
  cleaningLocks = signal(false);
  /** True while shared nodes are being cleaned up. */
  cleaningSharedNodes = signal(false);
  /** True while shared profiles are being cleaned up. */
  cleaningSharedProfiles = signal(false);
  /** True while the group deletion is being performed. */
  deleting = signal(false);
  /**
   * The latest deletion report describing the blocking conditions
   * (locked nodes, shared nodes and shared profiles), or `undefined` until
   * the first successful verification.
   */
  conditions = signal<GroupDeletionReport | undefined>(undefined);
  /**
   * The approval payload sent to the backend when the deletion request is
   * validated. Populated in {@link updateRequestDeleteGroup}.
   */
  groupDeletionRequestApproval!: GroupDeletionRequestApproval;

  /**
   * Angular lifecycle hook. Triggers the initial verification of the
   * group's deletability conditions.
   */
  ngOnInit(): void {
    this.verifyConditions();
  }

  /**
   * Fetches the deletion report for the target group and updates the
   * component state accordingly.
   *
   * Sets {@link verifying} while running, populates {@link conditions} and
   * marks {@link verified} on success, then evaluates readiness for
   * deletion. Errors are caught and logged without being rethrown.
   *
   * @returns A promise that resolves once verification has completed.
   */
  public async verifyConditions() {
    this.verifying.set(true);
    if (this.data.request.groupId) {
      try {
        this.conditions.set(
          await this.groupService.isGroupDeletableAsync({
            id: this.data.request.groupId,
          })
        );
        this.verified.set(true);
        this.isReadyForDeletion();
      } catch (error) {
        console.error(error);
      }
    }
    this.verifying.set(false);
  }

  /**
   * Determines whether the group is ready to be deleted, i.e. there are no
   * remaining locked nodes, shared nodes or shared profiles.
   *
   * @returns `true` when all blocking conditions are empty, otherwise
   * `false` (including when the conditions have not yet been loaded).
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
   * Releases the checkout (lock) on every locked node reported in
   * {@link conditions}, then schedules a re-verification to refresh the
   * report. Errors are caught and logged without being rethrown.
   *
   * @returns A promise that resolves once the cleanup requests have been
   * issued.
   */
  public async cleanLocks() {
    this.cleaningLocks.set(true);
    try {
      const conditions = this.conditions();
      if (conditions?.lockedNodes) {
        for (const node of conditions.lockedNodes) {
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
    this.cleaningLocks.set(false);
  }

  /**
   * Removes every share associated with the shared nodes reported in
   * {@link conditions}. For each shared node, the invited interest groups
   * are fetched and their shares deleted, then a re-verification is
   * scheduled. Errors are caught and logged without being rethrown.
   *
   * @returns A promise that resolves once the unshare requests have been
   * issued.
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
        setTimeout(() => {
          this.verifyConditions();
        }, 1000);
      }
    } catch (error) {
      console.error(error);
    }
    this.cleaningSharedNodes.set(false);
  }

  /**
   * Marks every shared profile reported in {@link conditions} as not
   * exported and persists the change, then schedules a re-verification.
   * Errors are caught and logged without being rethrown.
   *
   * @returns A promise that resolves once the profile updates have been
   * issued.
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
    this.cleaningSharedProfiles.set(false);
  }

  /**
   * Approves the deletion request and, if the approval succeeds, deletes the
   * interest group.
   *
   * Builds the {@link groupDeletionRequestApproval} payload (agreement set
   * to `1`), validates it against the backend and, on success, deletes the
   * group. Finally closes the dialog with `true` to signal completion.
   *
   * @returns A promise that resolves once the request has been processed and
   * the dialog closed.
   */
  public async updateRequestDeleteGroup() {
    this.deleting.set(true);
    this.groupDeletionRequestApproval = {
      id: this.data.request.id,
      agreement: 1,
      argument: '',
    };
    await this.categoryService.validateInterestGroupDeleteRequestsAsync({
      id: this.data.categoryId,
      groupDeletionRequestApproval: this.groupDeletionRequestApproval,
    });

    await this.groupService.deleteInterestGroupAsync({
      id: this.data.request.groupId,
      purgedata: true,
      purgelogs: true,
    });

    this.deleting.set(false);
    this.dialogRef.close(true);
  }
}
