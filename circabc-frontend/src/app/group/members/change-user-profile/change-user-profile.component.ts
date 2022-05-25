import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges,
} from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';

import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import {
  MembershipPostDefinition,
  MembersService,
  Profile,
  ProfileService,
  UserProfile,
} from 'app/core/generated/circabc';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-change-user-profile',
  templateUrl: './change-user-profile.component.html',
  preserveWhitespaces: true,
})
export class ChangeUserProfileComponent implements OnInit, OnChanges {
  @Input()
  showModal = false;
  @Input()
  groupId!: string;
  @Input()
  member!: UserProfile;
  @Output()
  readonly modalHide = new EventEmitter<ActionEmitterResult>();

  public profiles: Profile[] = [];
  public changeProfileForm!: FormGroup;
  public processing = false;
  public oldMember!: UserProfile;

  constructor(
    private fb: FormBuilder,
    private membersService: MembersService,
    private profileService: ProfileService,
    private i18nPipe: I18nPipe
  ) {}

  async ngOnInit() {
    this.changeProfileForm = this.fb.group(
      {
        selectedProfile: [''],
        userNotifications: [false],
        adminNotifications: [false],
      },
      {
        updateOn: 'change',
      }
    );

    if (this.groupId) {
      const profilesUnclean = await firstValueFrom(
        this.profileService.getProfiles(this.groupId)
      );
      for (const profile of profilesUnclean) {
        if (profile.name !== 'guest' && profile.name !== 'EVERYONE') {
          this.profiles.push(profile);
        }
      }
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.member && this.changeProfileForm) {
      this.changeProfileForm.controls.selectedProfile.setValue(
        changes.member.currentValue.profile.id
      );
    }
    if (changes.member !== undefined) {
      this.oldMember = { ...this.member };
    }
  }

  public cancel() {
    this.showModal = false;
    const res: ActionEmitterResult = {};
    res.type = ActionType.CHANGE_PROFILE;
    res.result = ActionResult.CANCELED;
    this.modalHide.emit(res);
  }

  public async changeProfile() {
    this.processing = true;

    const body: MembershipPostDefinition = {};
    body.adminNotifications = this.changeProfileForm.value.adminNotifications;
    body.userNotifications = this.changeProfileForm.value.userNotifications;
    const newMember = this.member;
    newMember.profile = this.getProfile();
    body.memberships = [newMember];

    const res: ActionEmitterResult = {};
    res.type = ActionType.CHANGE_PROFILE;

    try {
      await firstValueFrom(this.membersService.putMember(this.groupId, body));
      res.result = ActionResult.SUCCEED;
      this.showModal = false;
    } catch (error) {
      res.result = ActionResult.FAILED;
    }

    this.modalHide.emit(res);
    this.processing = false;
  }

  public getProfile(): Profile | undefined {
    if (this.changeProfileForm) {
      for (const profile of this.profiles) {
        if (this.changeProfileForm.value.selectedProfile === profile.id) {
          return profile;
        }
      }
    }

    return undefined;
  }

  public getProfileTitle(member: UserProfile): string {
    if (member.profile === undefined) {
      return '';
    }
    const title = this.i18nPipe.transform(member.profile.title);
    if (title === undefined || title === '') {
      if (member.profile.name === undefined) {
        return '';
      }
      return member.profile.name;
    }
    return title;
  }
}
