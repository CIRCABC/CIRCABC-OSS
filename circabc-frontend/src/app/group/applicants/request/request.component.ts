import {
  Component,
  OnChanges,
  OnInit,
  SimpleChanges,
  output,
  input,
} from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';

import { DatePipe } from '@angular/common';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { ActionType } from 'app/action-result';
import {
  type Applicant,
  ApplicantAction,
  MembersService,
  MembershipPostDefinition,
  Profile,
  UserProfile,
} from 'app/core/generated/circabc';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { getErrorTranslation } from 'app/core/util';
import { ControlMessageComponent } from 'app/shared/control-message/control-message.component';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-request',
  templateUrl: './request.component.html',
  styleUrl: './request.component.scss',
  preserveWhitespaces: true,
  imports: [
    ReactiveFormsModule,
    ControlMessageComponent,
    SpinnerComponent,
    DatePipe,
    I18nPipe,
    TranslocoModule,
  ],
})
export class RequestComponent implements OnInit, OnChanges {
  readonly applicant = input.required<Applicant>();
  readonly availableProfiles = input<Profile[]>([]);
  readonly groupId = input.required<string>();
  readonly requestProcessed = output<Applicant>();

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
    const availableProfiles = this.availableProfiles();
    if (
      this.inviteForm.controls.selectedProfile.value === '' &&
      availableProfiles &&
      availableProfiles.length > 0
    ) {
      this.inviteForm.controls.selectedProfile.setValue(
        availableProfiles[0].id
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

    let profileTmp: Profile = this.availableProfiles()[0];
    for (const prof of this.availableProfiles()) {
      if (prof.id === this.inviteForm.value.selectedProfile) {
        profileTmp = prof;
      }
    }

    const up: UserProfile = {};
    up.profile = profileTmp;
    up.user = this.applicant().user;

    body.memberships = [up];

    const appAction: ApplicantAction = {};
    appAction.action = 'clean';
    if (up?.user) {
      appAction.username = up.user.userId;
    }

    try {
      await firstValueFrom(
        this.membersService.postMember(this.groupId(), body)
      );

      await firstValueFrom(
        this.membersService.putApplicant(this.groupId(), appAction, 'clean')
      );

      this.requestProcessed.emit(this.applicant());
    } catch (_error) {
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
    const applicant = this.applicant();
    if (applicant?.user) {
      appAction.username = applicant.user.userId;
    }
    appAction.message = this.declineForm.value.declineText;
    await firstValueFrom(
      this.membersService.putApplicant(this.groupId(), appAction, 'decline')
    );
    this.requestProcessed.emit(applicant);
    this.processing = false;
  }
  get firstname(): string {
    const applicant = this.applicant();
    if (applicant?.user?.firstname) {
      return applicant.user.firstname;
    }
    return '';
  }

  get lastname(): string {
    const applicant = this.applicant();
    if (applicant?.user?.lastname) {
      return applicant.user.lastname;
    }
    return '';
  }

  get email(): string {
    const applicant = this.applicant();
    if (applicant?.user?.email) {
      return applicant.user.email;
    }
    return '';
  }

  get selectedProfileControl(): AbstractControl {
    return this.inviteForm.controls.selectedProfile;
  }
}
