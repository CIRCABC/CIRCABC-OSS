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
import { DataCyDirective } from 'app/shared/directives/data-cy.directive';
import { HorizontalLoaderComponent } from 'app/shared/loader/horizontal-loader.component';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-delete-form',
  templateUrl: './delete-form.component.html',
  styleUrl: './delete-form.component.scss',
  imports: [
    DataCyDirective,
    SpinnerComponent,
    HorizontalLoaderComponent,
    BreadcrumbComponent,
    TranslocoModule,
  ],
})
export class AcceptDeleteFormComponent implements OnInit {
  data = inject(MAT_DIALOG_DATA);
  dialogRef = inject(MatDialogRef<AcceptDeleteFormComponent>);
  categoryService = inject(CategoryService);
  groupService = inject(InterestGroupService);
  contentService = inject(ContentService);
  spaceService = inject(SpaceService);
  profileService = inject(ProfileService);

  verifying = false;
  verified = false;
  cleaningLocks = false;
  cleaningSharedNodes = false;
  cleaningSharedProfiles = false;
  deleting = false;
  conditions: GroupDeletionReport | undefined;
  groupDeletionRequestApproval!: GroupDeletionRequestApproval;

  ngOnInit(): void {
    this.verifyConditions();
  }

  public async verifyConditions() {
    this.verifying = true;
    if (this.data.request.groupId) {
      try {
        this.conditions = await firstValueFrom(
          this.groupService.isGroupDeletable(this.data.request.groupId)
        );
        this.verified = true;
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
      return (
        this.conditions.lockedNodes.length === 0 &&
        this.conditions.sharedNodes.length === 0 &&
        this.conditions.sharedProfiles.length === 0
      );
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

  public async updateRequestDeleteGroup() {
    this.deleting = true;
    this.groupDeletionRequestApproval = {
      id: this.data.request.id,
      agreement: 1,
      argument: '',
    };
    const res = await firstValueFrom(
      this.categoryService.validateInterestGroupDeleteRequests(
        this.data.categoryId,
        this.groupDeletionRequestApproval
      )
    );

    if (res) {
      await firstValueFrom(
        this.groupService.deleteInterestGroup(
          this.data.request.groupId,
          true,
          true
        )
      );
    }

    this.deleting = false;
    this.dialogRef.close(true);
  }
}
