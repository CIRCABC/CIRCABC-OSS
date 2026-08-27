import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
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

import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-verify-conditions-delete-ig',
  templateUrl: './verify-conditions-delete-ig.component.html',
  styleUrl: './verify-conditions-delete-ig.component.scss',
  imports: [
    SpinnerComponent,
    HorizontalLoaderComponent,
    BreadcrumbComponent,
    TranslocoModule,
    CommonModule,
  ],
})
export class VerifyConditionsDeleteIgComponent implements OnInit {
  data = inject(MAT_DIALOG_DATA);
  dialogRef = inject(MatDialogRef<VerifyConditionsDeleteIgComponent>);
  categoryService = inject(CategoryService);
  groupService = inject(InterestGroupService);
  contentService = inject(ContentService);
  spaceService = inject(SpaceService);
  profileService = inject(ProfileService);

  verifying = false;
  cleaningLocks = false;
  cleaningSharedNodes = false;
  cleaningSharedProfiles = false;
  deleting = false;
  conditions: GroupDeletionReport | undefined;
  groupDeletionRequestApproval!: GroupDeletionRequestApproval;

  /**
   * True when the Interest Group is currently locked. A locked IG cannot be
   * deleted or requested for deletion — the user must unlock it first.
   */
  public isGroupLocked = false;

  ngOnInit(): void {
    this.isGroupLocked = this.data?.group?.lockInfo?.locked === true;
    if (this.isGroupLocked) {
      // Skip the (possibly heavy) deletability check when the IG is locked —
      // the user must unlock it before anything else can happen.
      return;
    }
    this.verifyConditions();
  }

  public async verifyConditions() {
    // Guard again in case the dialog is left open long enough for the state
    // to change: if the IG became locked, do not run the deletability check.
    if (this.data?.group?.lockInfo?.locked === true) {
      this.isGroupLocked = true;
      return;
    }
    this.verifying = true;
    if (this.data.group.id) {
      try {
        this.conditions = await firstValueFrom(
          this.groupService.isGroupDeletable(this.data.group.id)
        );
        this.isReadyForDeletion();
      } catch (error) {
        console.error(error);
      }
    }
    this.verifying = false;
  }

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

  public async cleanLocks() {
    this.cleaningLocks = true;
    try {
      if (this.conditions?.lockedNodes) {
        for (const node of this.conditions.lockedNodes) {
          if (node.id) {
            await firstValueFrom(this.contentService.deleteCheckout(node.id));
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
  }

  public async cleanSharedNodes() {
    this.cleaningSharedNodes = true;
    try {
      if (this.conditions?.sharedNodes) {
        for (const sharedNode of this.conditions.sharedNodes) {
          if (sharedNode.id) {
            const invitedIgs = await firstValueFrom(
              this.spaceService.getShareSpaces(sharedNode.id, 0, 1)
            );
            for (const share of invitedIgs.data) {
              await firstValueFrom(
                this.spaceService.deleteShareSpace(
                  sharedNode.id,
                  share.igId as string
                )
              );
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
  }

  public async cleanSharedProfiles() {
    this.cleaningSharedProfiles = true;
    try {
      if (this.conditions?.sharedProfiles) {
        for (const profile of this.conditions.sharedProfiles) {
          profile.exported = false;
          if (profile.id) {
            await firstValueFrom(
              this.profileService.putProfile(profile.id, profile)
            );
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
  }
}
