import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import {
  ContentService,
  GroupDeletionReport,
  InterestGroupService,
  ProfileService,
  SpaceService,
} from 'app/core/generated/circabc';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-delete-group',
  templateUrl: './delete-group.component.html',
  styleUrls: ['./delete-group.component.scss'],
})
export class DeleteGroupComponent implements OnInit {
  public igNode!: string;
  public conditions!: GroupDeletionReport;
  public verifying = false;
  public verified = false;
  public cleaningLocks = false;
  public cleaningSharedNodes = false;
  public cleaningSharedProfiles = false;
  public deleting = false;

  constructor(
    private groupService: InterestGroupService,
    private route: ActivatedRoute,
    private router: Router,
    private contentService: ContentService,
    private spaceService: SpaceService,
    private profileService: ProfileService
  ) {}

  ngOnInit() {
    this.route.params.subscribe((params) => {
      if (params.id) {
        this.igNode = params.id;
      }
    });
  }

  public async verifyConditions() {
    this.verifying = true;
    try {
      this.conditions = await firstValueFrom(
        this.groupService.isGroupDeletable(this.igNode)
      );
      this.verified = true;
    } catch (error) {
      console.error(error);
    }
    this.verifying = false;
  }

  public isReadyForDeletion() {
    if (
      this.conditions &&
      this.conditions.lockedNodes &&
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
      if (this.conditions && this.conditions.lockedNodes) {
        for (const node of this.conditions.lockedNodes) {
          if (node.id) {
            await firstValueFrom(this.contentService.deleteCheckout(node.id));
          }
        }
      }
      this.verifyConditions();
    } catch (error) {
      console.error(error);
    }
    this.cleaningLocks = false;
  }

  public async cleanSharedNodes() {
    this.cleaningSharedNodes = true;
    try {
      if (this.conditions && this.conditions.sharedNodes) {
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
        this.verifyConditions();
      }
    } catch (error) {
      console.error(error);
    }
    this.cleaningSharedNodes = false;
  }

  public async cleanSharedProfiles() {
    this.cleaningSharedProfiles = true;
    try {
      if (this.conditions && this.conditions.sharedProfiles) {
        for (const profile of this.conditions.sharedProfiles) {
          profile.exported = false;
          if (profile.id) {
            await firstValueFrom(
              this.profileService.putProfile(profile.id, profile)
            );
          }
        }
      }
      this.verifyConditions();
    } catch (error) {
      console.error(error);
    }
    this.cleaningSharedProfiles = false;
  }

  public async deleteGroup() {
    this.deleting = true;
    try {
      await firstValueFrom(
        this.groupService.deleteInterestGroup(this.igNode, true, true)
      );
      this.router.navigate(['/explore']);
    } catch (error) {
      console.error(error);
    }
    this.deleting = false;
  }
}
