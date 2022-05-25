import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { User, UserService } from 'app/core/generated/circabc';
import { InterestGroupProfileSelectable } from 'app/support/user-management/interest-group-profile-selectable';
import { UsersMembershipsModel } from 'app/support/user-management/users-memberships-model';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-user-management',
  templateUrl: './user-management.component.html',
  styleUrls: ['./user-management.component.scss'],
})
export class UserManagementComponent implements OnInit {
  public searchForm!: FormGroup;
  public fileForm!: FormGroup;
  public searchStep: 'search' | 'import' | 'selection' = 'search';
  public searchedUsers: User[] = [];
  public selectedUsers: UsersMembershipsModel[] = [];
  public retrievedUsers: User[] = [];
  public focusedUserId = '';
  public focusedUser!: User | undefined;
  public oneUserFocus = false;
  public expectionNoAlfrescoUser = false;
  public searching = false;
  public uploadFiles!: FileList;
  public showRevocationModal = false;
  public showExpirationModal = false;
  public selectedUserIds: string[] = [];

  constructor(private fb: FormBuilder, private userService: UserService) {}

  ngOnInit() {
    this.searchForm = this.fb.group({
      searchField: [''],
    });

    this.fileForm = this.fb.group({
      listFile: [''],
    });
  }

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  public fileChangeEvent(fileInput: any) {
    this.uploadFiles = fileInput.target.files as FileList;
  }

  public resetSearch() {
    this.searchForm.controls.searchField.setValue('');
    this.searchedUsers = [];
  }

  public resetUpload() {
    this.fileForm.controls.listFile.setValue('');
    this.retrievedUsers = [];
  }

  public async searchUsers() {
    this.searching = true;
    if (
      this.searchForm.value.searchField &&
      this.searchForm.value.searchField !== ''
    ) {
      try {
        this.focusedUserId = '';
        this.oneUserFocus = false;
        this.searchedUsers = await firstValueFrom(
          this.userService.getUsers(this.searchForm.value.searchField)
        );
      } catch (error) {
        console.error(error);
      }
    }
    this.searching = false;
  }

  public async retrieveUsers() {
    try {
      if (this.uploadFiles.length > 0) {
        const file = this.uploadFiles.item(0);
        if (file) {
          this.retrievedUsers = await firstValueFrom(
            this.userService.getUsersFromList(file)
          );
        }
      }
    } catch (error) {
      console.error(error);
    }
  }

  public async focusUser(userId: string) {
    this.expectionNoAlfrescoUser = false;
    if (userId !== '') {
      this.focusedUserId = userId;
      try {
        this.focusedUser = await firstValueFrom(
          this.userService.getUser(this.focusedUserId)
        );
      } catch (error) {
        if (
          error.error.message &&
          error.error.message.indexOf(
            'User does not exist and could not be created'
          ) > -1
        ) {
          this.expectionNoAlfrescoUser = true;
        }
      }
      this.oneUserFocus = true;
    } else {
      this.focusedUserId = '';
      this.oneUserFocus = false;
      this.focusedUser = undefined;
    }
  }

  public selection(user: User) {
    const found = this.selectedUsers.find((item) => {
      return item.userid === user.userId;
    });

    if (found === undefined && user.userId) {
      this.selectedUsers.push({
        userid: user.userId,
        user: user,
        memberships: [],
        loadingMemberships: true,
      });

      const idx = this.selectedUsers.length - 1;

      this.userService
        .getUserMembership(user.userId, '', false)
        .subscribe((data) => {
          this.selectedUsers[idx].loadingMemberships = false;
          const memberships = data.filter((item) => {
            return { ...item, selected: false };
          }) as InterestGroupProfileSelectable[];
          this.selectedUsers[idx].memberships = memberships;
        });
    }
  }

  public selectAllSearch() {
    for (const searchedUser of this.searchedUsers) {
      this.selection(searchedUser);
    }
  }

  public selectAllList() {
    for (const retrievedUser of this.retrievedUsers) {
      this.selection(retrievedUser);
    }
  }

  public remove(user: User) {
    const found = this.selectedUsers.findIndex((item) => {
      return item.userid === user.userId;
    });

    if (found > -1) {
      this.selectedUsers.splice(found, 1);
    }
  }

  public refreshAfterSchedule() {
    this.selectedUserIds = [];
    this.selectedUsers = [];
    this.showRevocationModal = false;
  }

  public cancelSchedule() {
    this.showRevocationModal = false;
  }

  public cancelExpirationSchedule() {
    this.showExpirationModal = false;
  }

  public prepareRevocation() {
    this.selectedUserIds = [];
    for (const item of this.selectedUsers) {
      if (item.userid) {
        this.selectedUserIds.push(item.userid);
      }
    }
    this.showRevocationModal = true;
  }

  public prepareExpiration() {
    this.showExpirationModal = true;
  }

  public toggleProfile(
    userid: string,
    profileName: string | undefined,
    interestGroupName: string | undefined
  ) {
    if (profileName === undefined || interestGroupName === undefined) {
      return;
    }
    const found = this.selectedUsers.findIndex((item) => {
      return item.userid === userid;
    });

    const memberships = this.selectedUsers[found].memberships;
    const newMemberships: InterestGroupProfileSelectable[] = [];

    memberships.forEach((item) => {
      if (
        item.interestGroup &&
        item.interestGroup.name === interestGroupName &&
        item.profile &&
        item.profile.name === profileName
      ) {
        item.selected = true;
      }

      newMemberships.push(item);
    });

    this.selectedUsers[found].memberships = newMemberships;
  }

  public toggleProfiles(userid: string, type: string) {
    const found = this.selectedUsers.findIndex((item) => {
      return item.userid === userid;
    });

    const memberships = this.selectedUsers[found].memberships;
    const newMemberships: InterestGroupProfileSelectable[] = [];

    memberships.forEach((item) => {
      item.selected = type !== 'reset';
      newMemberships.push(item);
    });

    this.selectedUsers[found].memberships = newMemberships;
  }
}
