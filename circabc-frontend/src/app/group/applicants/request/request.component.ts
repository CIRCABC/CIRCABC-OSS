import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges,
} from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  Validators,
} from '@angular/forms';

import { TranslocoService } from '@ngneat/transloco';
import { ActionType } from 'app/action-result';
import {
  Applicant,
  ApplicantAction,
  MembershipPostDefinition,
  MembersService,
  Profile,
  UserProfile,
} from 'app/core/generated/circabc';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { getErrorTranslation } from 'app/core/util';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-request',
  templateUrl: './request.component.html',
  styleUrls: ['./request.component.scss'],
  preserveWhitespaces: true,
})
export class RequestComponent implements OnInit, OnChanges {
  @Input()
  applicant!: Applicant;
  @Input()
  availableProfiles: Profile[] = [];
  @Input()
  groupId!: string;
  @Output()
  readonly requestProcessed = new EventEmitter<Applicant>();

  public showAcceptForm = false;
  public showDeclineForm = false;
  public inviteForm!: FormGroup;
  public declineForm!: FormGroup;
  public processing = false;

  constructor(
    private fb: FormBuilder,
    private membersService: MembersService,
    private uiMessageService: UiMessageService,
    private translateService: TranslocoService
  ) {}

  ngOnInit() {
    this.inviteForm = this.fb.group(
      {
        selectedProfile: ['', Validators.required],
        notifyUser: [true],
      },
      {
        updateOn: 'change',
      }
    );

    this.declineForm = this.fb.group(
      {
        declineText: [''],
      },
      {
        updateOn: 'change',
      }
    );
  }

  ngOnChanges(changes: SimpleChanges) {
    const chng = changes.applicant;
    if (chng) {
      if (
        chng.currentValue !== undefined &&
        chng.previousValue !== undefined &&
        chng.currentValue !== chng.previousValue
      ) {
        this.showAcceptForm = false;
        this.showDeclineForm = false;
        this.inviteForm.reset();
        this.declineForm.reset();
      }
    }
  }

  prepareAccept() {
    this.showAcceptForm = true;
    if (
      this.inviteForm.controls.selectedProfile.value === '' &&
      this.availableProfiles &&
      this.availableProfiles.length > 0
    ) {
      this.inviteForm.controls.selectedProfile.setValue(
        this.availableProfiles[0].id
      );
    }
  }

  prepareDecline() {
    this.showDeclineForm = true;
  }

  cancel() {
    this.showAcceptForm = false;
    this.showDeclineForm = false;
  }

  public async invite() {
    this.processing = true;

    const body: MembershipPostDefinition = {};
    body.userNotifications = this.inviteForm.value.notifyUser;

    let profileTmp: Profile = this.availableProfiles[0];
    for (const prof of this.availableProfiles) {
      if (prof.id === this.inviteForm.value.selectedProfile) {
        profileTmp = prof;
      }
    }

    const up: UserProfile = {};
    up.profile = profileTmp;
    up.user = this.applicant.user;

    body.memberships = [up];

    const appAction: ApplicantAction = {};
    appAction.action = 'clean';
    if (up && up.user) {
      appAction.username = up.user.userId;
    }

    try {
      await firstValueFrom(this.membersService.postMember(this.groupId, body));

      await firstValueFrom(
        this.membersService.putApplicant(this.groupId, appAction, 'clean')
      );

      this.requestProcessed.emit(this.applicant);
    } catch (error) {
      const res = this.translateService.translate(
        getErrorTranslation(ActionType.ADD_MEMBERSHIPS)
      );
      this.uiMessageService.addErrorMessage(res, false);
    }

    this.processing = false;
  }

  public async decline() {
    this.processing = true;

    const appAction: ApplicantAction = {};
    appAction.action = 'decline';
    if (this.applicant && this.applicant.user) {
      appAction.username = this.applicant.user.userId;
    }
    appAction.message = this.declineForm.value.declineText;
    await firstValueFrom(
      this.membersService.putApplicant(this.groupId, appAction, 'decline')
    );
    this.requestProcessed.emit(this.applicant);
    this.processing = false;
  }
  get firstname(): string {
    if (
      this.applicant &&
      this.applicant.user &&
      this.applicant.user.firstname
    ) {
      return this.applicant.user.firstname;
    }
    return '';
  }

  get lastname(): string {
    if (this.applicant && this.applicant.user && this.applicant.user.lastname) {
      return this.applicant.user.lastname;
    }
    return '';
  }

  get email(): string {
    if (this.applicant && this.applicant.user && this.applicant.user.email) {
      return this.applicant.user.email;
    }
    return '';
  }

  get selectedProfileControl(): AbstractControl {
    return this.inviteForm.controls.selectedProfile;
  }
}
