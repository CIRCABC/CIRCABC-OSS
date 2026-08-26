import { Component, Input, OnInit, output, input } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { TranslocoModule } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import {
  MembersService,
  MembershipPostDefinition,
  Profile,
  ProfileService,
  UserProfile,
} from 'app/core/generated/circabc';
import { SelectableUserProfile } from 'app/core/ui-model';
import { ModalComponent } from 'app/shared/modal/modal.component';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-change-profiles-multiple',
  templateUrl: './change-profiles-multiple.component.html',
  imports: [ModalComponent, ReactiveFormsModule, I18nPipe, TranslocoModule],
})
export class ChangeProfilesMultipleComponent implements OnInit {
  readonly users = input.required<SelectableUserProfile[]>();
  readonly groupId = input.required<string>();
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  showModal!: boolean;
  public readonly modalHide = output<ActionEmitterResult>();

  public profiles: Profile[] = [];
  public changeProfileForm!: FormGroup;
  public processing = false;

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

    await this.loadProfiles();
  }

  private async loadProfiles() {
    const groupId = this.groupId();
    if (groupId) {
      const profilesUnclean = await firstValueFrom(
        this.profileService.getProfiles(groupId)
      );
      for (const profile of profilesUnclean) {
        if (profile.name !== 'guest' && profile.name !== 'EVERYONE') {
          this.profiles.push(profile);
        }
      }
    }
  }

  public async changeProfiles() {
    this.processing = true;

    const body: MembershipPostDefinition = {};
    body.adminNotifications = this.changeProfileForm.value.adminNotifications;
    body.userNotifications = this.changeProfileForm.value.userNotifications;
    body.memberships = [];

    for (const user of this.users()) {
      const newMember = user;
      newMember.profile = this.getProfile();
      body.memberships.push(newMember);
    }

    const res: ActionEmitterResult = {};
    res.type = ActionType.CHANGE_PROFILE;

    try {
      await firstValueFrom(this.membersService.putMember(this.groupId(), body));
      res.result = ActionResult.SUCCEED;
    } catch (_error) {
      console.error('Error while updating the users profiles');
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

  public cancel() {
    this.showModal = false;
    const res: ActionEmitterResult = {};
    res.type = ActionType.CHANGE_PROFILE;
    res.result = ActionResult.CANCELED;
    this.modalHide.emit(res);
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
