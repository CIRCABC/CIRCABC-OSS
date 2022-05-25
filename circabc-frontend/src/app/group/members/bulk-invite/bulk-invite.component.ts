import { Component, Inject, Input, OnInit, Optional } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslocoService } from '@ngneat/transloco';
import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import {
  BASE_PATH,
  BulkInviteData,
  Category,
  IGData,
  InterestGroup,
  InterestGroupProfile,
  InterestGroupService,
  NameValue,
  Profile,
  ProfileService,
  User,
  UserService,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { SaveAsService } from 'app/core/save-as.service';
import { SelectableBulkImportUserData } from 'app/core/ui-model';
import { truncate } from 'app/core/util';
import { ListingOptions } from 'app/group/listing-options/listing-options';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-bulk-invite',
  templateUrl: './bulk-invite.component.html',
  styleUrls: ['./bulk-invite.component.scss'],
  preserveWhitespaces: true,
})
export class BulkInviteComponent implements OnInit {
  @Input()
  public igId!: string;

  public bulkInviteForm!: FormGroup;
  public processing = false;
  public availableCategories: Category[] = [];
  public availableInterestGroups: NameValue[] = [];
  public selectedCategory!: Category;
  public selectedInterestGroups: NameValue[] = [];

  public listingOptions: ListingOptions = { page: 1, limit: 10, sort: '' };
  public totalItems: number = this.listingOptions.limit;

  public user!: User;

  public allMembers: SelectableBulkImportUserData[] = [];
  public members: SelectableBulkImportUserData[] = [];
  public profiles: Profile[] = [];
  public availableProfiles: Profile[] = [];
  public allSelected = false;
  public selectedMembers: SelectableBulkImportUserData[] = [];
  public currentGroup!: InterestGroup;
  public alreadyMember = false;
  public currentUserMemberships!: InterestGroupProfile[];
  public loading = false;

  public fileToUpload: File | undefined;

  private basePath!: string;

  constructor(
    private route: ActivatedRoute,
    private userService: UserService,
    private profileService: ProfileService,
    private interestGroupService: InterestGroupService,
    private permEvalService: PermissionEvaluatorService,
    private loginService: LoginService,
    private formBuilder: FormBuilder,
    private saveAsService: SaveAsService,
    private translateService: TranslocoService,
    private uiMessageService: UiMessageService,
    private router: Router,
    private i18nPipe: I18nPipe,
    @Optional()
    @Inject(BASE_PATH)
    basePath: string
  ) {
    if (basePath) {
      this.basePath = basePath;
    }
  }

  async ngOnInit() {
    this.buildForm();
    this.route.params.subscribe(async (params) => this.getParams(params));
  }

  private async getParams(params: { [key: string]: string }) {
    this.igId = params.igId;
    await this.load();
  }

  public buildForm(): void {
    this.bulkInviteForm = this.formBuilder.group({
      createNewProfiles: [false],
      notifyUsers: [false],
    });
  }

  private async load() {
    this.loading = true;
    if (this.user === undefined) {
      this.user = this.loginService.getUser();
    }
    this.availableCategories = await firstValueFrom(
      this.userService.getBulkInviteCategories(this.user.userId as string)
    );
    this.selectedCategory = this.availableCategories[0];
    await this.loadGroup();
    await this.getCurrentUserMemberships();
    await this.loadProfiles();
    await this.loadIGs();
    this.loading = false;
  }

  private async getCurrentUserMemberships() {
    if (!this.loginService.isGuest()) {
      const userId =
        this.user.userId !== undefined ? this.user.userId : 'guest';
      if (userId !== undefined) {
        this.currentUserMemberships = await firstValueFrom(
          this.userService.getUserMembership(userId)
        );
        for (const profile of this.currentUserMemberships) {
          if (
            profile &&
            profile.interestGroup &&
            this.currentGroup &&
            profile.interestGroup.id === this.currentGroup.id
          ) {
            this.alreadyMember = true;
          }
        }
      }
    }
  }

  private async loadGroup() {
    this.currentGroup = await firstValueFrom(
      this.interestGroupService.getInterestGroup(this.igId)
    );
  }

  private async loadProfiles() {
    this.profiles = await firstValueFrom(
      this.profileService.getProfiles(this.igId)
    );
    this.availableProfiles = this.profiles.filter(
      (profile: Profile) =>
        profile.name !== undefined &&
        profile.name !== 'guest' &&
        profile.name !== 'EVERYONE' &&
        profile.name.indexOf(':') === -1
    );
  }

  private async loadIGs() {
    this.loading = true;
    const igs = await firstValueFrom(
      this.userService.getBulkInviteIGs(
        this.selectedCategory.id as string,
        this.igId
      )
    );
    this.availableInterestGroups = igs.map((igData: IGData) => ({
      name: `${truncate(this.selectedCategory.name, 15)} / ${igData.igName}`,
      value: igData.igId,
    }));
    this.loading = false;
  }

  public async loadMembers() {
    this.loading = true;
    if (
      this.selectedInterestGroups !== undefined &&
      this.selectedInterestGroups.length !== 0
    ) {
      this.allMembers = await firstValueFrom(
        this.userService.getBulkInviteMembers(
          this.selectedInterestGroups.map(
            (item: NameValue) => item.value as string
          ),
          this.igId
        )
      );
    } else {
      this.allMembers = [];
      this.members = [];
    }
    // get all members from the file in case a file is selected
    if (this.fileToUpload !== undefined) {
      let allMembersFromFile = [];
      try {
        allMembersFromFile = await firstValueFrom(
          this.userService.bulkInviteUsersDigestFile(
            this.igId,
            this.fileToUpload
          )
        );
        // add members from file, but ignore duplicates
        for (const member of allMembersFromFile) {
          if (!this.containsMember(this.allMembers, member)) {
            this.extractProfile(member);
            this.allMembers.push(member);
          }
        }
      } catch (error) {
        const theError: string = error.error.message;
        const errPos = theError.indexOf('ERR::');
        let result = '';
        if (errPos > -1) {
          const errorDetails: string = theError.substring(errPos + 5);
          if (errorDetails.startsWith('NumCol<9:')) {
            const colNum: string = errorDetails.substring(9);
            result = this.translateService.translate(
              'label.error.invalid.bulk.file.columns',
              {
                details: colNum,
              }
            );
          } else if (errorDetails.startsWith('NoColumn:')) {
            const match = errorDetails.match(/NoColumn:(.*)\|(\d+)\|(.*)/);
            if (match !== null) {
              const name = match[1];
              const pos = match[2];
              const found = match[3];
              result = this.translateService.translate(
                'label.error.invalid.bulk.file.nocolumn',
                {
                  name: name,
                  pos: pos,
                  found: found,
                }
              );
            }
          } else if (errorDetails.startsWith('NullColumn:')) {
            const colNum: string = errorDetails.substring(11);
            result = this.translateService.translate(
              'label.error.invalid.bulk.file.nullcolumn',
              {
                details: colNum,
              }
            );
          }
        }
        if (result === '') {
          result = this.translateService.translate(
            'label.error.invalid.bulk.file'
          );
        }
        this.uiMessageService.addInfoMessage(result);
        this.fileToUpload = undefined;
      }
    }

    this.moveAccessToTop();
    this.loading = false;
    this.selectedMembers = [];
    this.listingOptions.page = 1;
    this.changePage(this.listingOptions);
  }

  // move Access profile to the top of the list
  private moveAccessToTop() {
    let tempProfile!: Profile;
    for (let idx = 0; idx < this.availableProfiles.length; idx++) {
      const profile: Profile = this.availableProfiles[idx];
      if (profile.name === 'Access') {
        tempProfile = profile;
        this.availableProfiles.splice(idx, 1);
        break;
      }
    }
    if (tempProfile !== undefined) {
      this.availableProfiles.unshift(tempProfile);
    }
  }

  private containsMember(
    members: SelectableBulkImportUserData[],
    theMember: SelectableBulkImportUserData
  ): boolean {
    for (const member of members) {
      if (member.username === theMember.username) {
        return true;
      }
    }
    return false;
  }

  public isMember(): boolean {
    return this.alreadyMember;
  }

  public isDirAdmin(): boolean {
    return this.permEvalService.isDirAdmin(this.currentGroup);
  }

  public isDirManageMembers(): boolean {
    return this.permEvalService.isDirManageMembers(this.currentGroup);
  }

  public async setCategory(value: string) {
    for (const category of this.availableCategories) {
      if (category.id === value) {
        this.selectedCategory = category;
        break;
      }
    }
    await this.loadIGs();
  }

  public setProfile(profileId: string, username: string) {
    for (const member of this.members) {
      if (member.username === username) {
        member.profileId = profileId;
        break;
      }
    }
  }

  public setSelectedProfiles(profileId: string) {
    for (const member of this.selectedMembers) {
      member.profileId = profileId;
    }
  }

  public setSelectedProfile(
    member: SelectableBulkImportUserData,
    profileId: string
  ) {
    member.profileId = profileId;
  }

  public truncateProfileTitle(
    title: { [key: string]: string } | undefined
  ): string {
    if (title === undefined) {
      return '';
    }
    return truncate(this.i18nPipe.transform(title), 12);
  }

  public deleteMember(member: SelectableBulkImportUserData) {
    let idx = this.members.indexOf(member);
    if (idx > -1) {
      this.members.splice(idx, 1);
    }
    idx = this.allMembers.indexOf(member);
    if (idx > -1) {
      this.allMembers.splice(idx, 1);
      this.totalItems -= 1;
    }
  }

  public deleteSelectedMembers() {
    for (const member of this.selectedMembers) {
      let idx = this.members.indexOf(member);
      if (idx > -1) {
        this.members.splice(idx, 1);
      }
      idx = this.allMembers.indexOf(member);
      if (idx > -1) {
        this.allMembers.splice(idx, 1);
        this.totalItems -= 1;
      }
    }
    this.selectedMembers = [];
    this.allSelected = false;
  }

  public async deleteFile() {
    this.fileToUpload = undefined;
    await this.loadMembers();
  }

  public canInvite() {
    return this.allMembers.some((member) => member.status === 'ok');
  }

  public toggleSelectedUser(selectedUser: SelectableBulkImportUserData) {
    this.members.forEach((member) => {
      if (member.username) {
        if (member.username === selectedUser.username) {
          member.selected = !member.selected;

          if (member.selected) {
            this.selectedMembers.push(member);
          } else {
            const idx = this.selectedMembers.indexOf(member);
            if (idx > -1) {
              this.selectedMembers.splice(idx, 1);
              // deselect the all selected checkbox if all members were unselected
              if (this.selectedMembers.length === 0) {
                this.allSelected = false;
              }
            }
          }
        }
      }
    });
    if (this.selectedMembers.length === 0) {
      this.allSelected = false;
    }
  }

  public selectAll() {
    if (this.allMembers === undefined || this.allMembers === []) {
      return;
    }
    if (!this.allSelected) {
      this.selectedMembers = [];
      this.allSelected = true;
      this.members.forEach((member) => {
        if (member.username) {
          member.selected = true;
          this.selectedMembers.push(member);
        }
      });
    } else {
      this.allSelected = false;
      this.selectedMembers = [];
      this.members.forEach((member) => {
        member.selected = false;
      });
    }
  }

  public saveWork() {
    //
  }

  public async invite() {
    this.processing = true;

    try {
      const igProfiles: NameValue[] = this.profiles.map((profile: Profile) => {
        return {
          name:
            profile.title === undefined
              ? profile.name
              : this.i18nPipe.transform(profile.title),
          value: profile.name,
        };
      });

      const okMembers = this.allMembers.filter(
        (value) => value.status === 'ok'
      );
      const members = okMembers.map((value) => {
        if (value.profileId === undefined) {
          value.profileId = this.availableProfiles[0].name;
        }
        return value;
      });

      const bulkInviteData: BulkInviteData = {
        bulkImportUserData: members,
        igProfiles: igProfiles,
      };

      await firstValueFrom(
        this.userService.bulkInviteUsers(
          this.igId,
          bulkInviteData,
          this.bulkInviteForm.controls.createNewProfiles.value,
          this.bulkInviteForm.controls.notifyUsers.value
        )
      );

      this.close();
    } finally {
      this.processing = false;
    }
  }

  public close() {
    this.reset();
    this.router.navigate(['../..'], { relativeTo: this.route });
  }

  private reset() {
    this.selectedInterestGroups = [];
    this.selectedMembers = [];
    this.allMembers = [];
    this.members = [];
    this.fileToUpload = undefined;
  }

  // eslint-disable-next-line no-empty, @typescript-eslint/no-empty-function
  public getImportUsersFileTemplate() {
    const url = `${this.basePath}/users/bulkinvite/template`;
    const name = 'template.xls';
    this.saveAsService.saveUrlAs(url, name);
    return false;
  }

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  public async fileChangeEvent(fileInput: any) {
    const filesList = fileInput.target.files as FileList;
    this.fileToUpload = filesList[0];
    await this.loadMembers();
  }

  // pagination

  public async changeLimit(limit: number) {
    this.listingOptions.limit = limit;
    this.listingOptions.page = 1;
    this.changePage(this.listingOptions);
  }

  public goToPage(page: number) {
    this.listingOptions.page = page;
    this.changePage(this.listingOptions);
  }

  public changePage(listingOptions: ListingOptions) {
    this.listingOptions = listingOptions;

    if (listingOptions.limit === -1) {
      listingOptions.limit = this.allMembers.length;
    }

    this.members = [];

    const startItem =
      (this.listingOptions.page - 1) * this.listingOptions.limit;
    const endItem =
      startItem + this.listingOptions.limit > this.allMembers.length
        ? this.allMembers.length
        : startItem + this.listingOptions.limit;

    for (let index = startItem; index < endItem; index = index + 1) {
      this.members.push(this.allMembers[index]);
    }

    this.totalItems =
      this.allMembers.length > 0
        ? this.allMembers.length
        : this.listingOptions.limit;
  }

  private extractProfile(member: SelectableBulkImportUserData) {
    const profileUser = member.profileId;
    const found =
      this.availableProfiles.find((profile) => {
        return profile.name === profileUser;
      }) !== undefined;

    if (!found) {
      member.profileId = this.availableProfiles[0].name;
    }
  }
  public trackMember(_index: number, item: SelectableBulkImportUserData) {
    return `${item.username}-${item.profileId}`;
  }
}
