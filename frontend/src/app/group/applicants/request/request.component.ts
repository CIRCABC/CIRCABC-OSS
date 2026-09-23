import { DatePipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  OnChanges,
  OnInit,
  output,
  SimpleChanges,
  signal,
} from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { ActionType } from 'app/action-result';
import {
  type Applicant,
  ApplicantAction,
  MembershipPostDefinition,
  MembersService,
  Profile,
  UserProfile,
} from 'app/core/generated/circabc';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { getErrorTranslation } from 'app/core/util';
import { ControlMessageComponent } from 'app/shared/control-message/control-message.component';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';

/**
 * Standalone component (`cbc-request`) that renders a single membership
 * application (applicant) row together with the controls needed to process it.
 *
 * It displays the applicant's identity (name, email, request date) and exposes
 * two inline actions:
 * - **Accept**: reveals a form to pick an access {@link Profile} and choose
 *   whether the user should be notified, then creates the membership and clears
 *   the pending applicant.
 * - **Decline**: reveals a form with an optional message and rejects the
 *   application.
 *
 * The component collaborates with {@link MembersService} to persist the
 * accept/decline decisions, with {@link UiMessageService} to surface errors and
 * with {@link TranslocoService} for translating error messages. Once an action
 * completes, it emits {@link RequestComponent.requestProcessed} so the parent
 * can remove the handled applicant from its list.
 */
@Component({
  selector: 'cbc-request',
  templateUrl: './request.component.html',
  styleUrl: './request.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
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
  /** Reactive forms builder used to construct the accept/decline forms. */
  private readonly fb = inject(FormBuilder);
  /** API client for creating memberships and updating applicant state. */
  private readonly membersService = inject(MembersService);
  /** Service used to display error notifications to the user. */
  private readonly uiMessageService = inject(UiMessageService);
  /** Transloco service used to translate error messages. */
  private readonly translateService = inject(TranslocoService);

  /** Required input: the membership applicant being displayed and processed. */
  readonly applicant = input.required<Applicant>();
  /** Access profiles that can be assigned when accepting the applicant. Defaults to an empty list. */
  readonly availableProfiles = input<Profile[]>([]);
  /** Required input: identifier of the interest group the applicant is requesting to join. */
  readonly groupId = input.required<string>();
  /** Emits the processed {@link Applicant} once it has been accepted or declined. */
  readonly requestProcessed = output<Applicant>();

  /** Whether the inline "accept" form (profile selection) is currently shown. */
  public showAcceptForm = false;
  /** Whether the inline "decline" form (rejection message) is currently shown. */
  public showDeclineForm = false;
  /** Reactive form holding the selected profile and notification preference used when accepting. */
  public inviteForm!: FormGroup;
  /** Reactive form holding the optional message used when declining. */
  public declineForm!: FormGroup;
  /** Whether an accept/decline request is in progress; used to drive the spinner and disable controls. */
  public processing = signal(false);

  /**
   * Angular lifecycle hook that initializes the reactive `inviteForm` and
   * `declineForm` with their controls and validators.
   */
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

  /**
   * Angular lifecycle hook that reacts to input changes. When the `applicant`
   * input changes to a different, defined value, it hides both inline forms and
   * resets their contents so the previous applicant's state does not leak into
   * the newly displayed one.
   *
   * @param changes The set of changed inputs provided by Angular.
   */
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

  /**
   * Shows the accept form and, if no profile is selected yet, pre-selects the
   * first available profile so the user can accept with a single click.
   */
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

  /** Shows the inline decline form. */
  prepareDecline() {
    this.showDeclineForm = true;
  }

  /** Hides both the accept and decline inline forms without processing the applicant. */
  cancel() {
    this.showAcceptForm = false;
    this.showDeclineForm = false;
  }

  /**
   * Accepts the applicant: creates a membership for the applicant's user with
   * the selected profile and notification preference, then clears the pending
   * applicant. On success emits {@link requestProcessed}; on failure a
   * translated error message is shown via {@link UiMessageService}. Toggles
   * {@link processing} around the asynchronous work.
   *
   * @returns A promise that resolves once the accept flow has completed.
   */
  public async invite() {
    this.processing.set(true);

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
      await this.membersService.postMemberAsync({
        id: this.groupId(),
        membershipPostDefinition: body,
      });

      await this.membersService.putApplicantAsync({
        id: this.groupId(),
        applicantAction: appAction,
        action: 'clean',
      });

      this.requestProcessed.emit(this.applicant());
    } catch (error) {
      console.error(error);
      const res = this.translateService.translate(
        getErrorTranslation(ActionType.ADD_MEMBERSHIPS)
      );
      this.uiMessageService.addErrorMessage(res, false);
    }

    this.processing.set(false);
  }

  /**
   * Declines the applicant, sending the optional message entered in
   * `declineForm`, then emits {@link requestProcessed}. Toggles
   * {@link processing} around the asynchronous work.
   *
   * @returns A promise that resolves once the decline flow has completed.
   */
  public async decline() {
    this.processing.set(true);

    const appAction: ApplicantAction = {};
    appAction.action = 'decline';
    const applicant = this.applicant();
    if (applicant?.user) {
      appAction.username = applicant.user.userId;
    }
    appAction.message = this.declineForm.value.declineText;
    await this.membersService.putApplicantAsync({
      id: this.groupId(),
      applicantAction: appAction,
      action: 'decline',
    });
    this.requestProcessed.emit(applicant);
    this.processing.set(false);
  }
  /**
   * The applicant's first name.
   * @returns The first name, or an empty string when unavailable.
   */
  get firstname(): string {
    const applicant = this.applicant();
    if (applicant?.user?.firstname) {
      return applicant.user.firstname;
    }
    return '';
  }

  /**
   * The applicant's last name.
   * @returns The last name, or an empty string when unavailable.
   */
  get lastname(): string {
    const applicant = this.applicant();
    if (applicant?.user?.lastname) {
      return applicant.user.lastname;
    }
    return '';
  }

  /**
   * The applicant's email address.
   * @returns The email, or an empty string when unavailable.
   */
  get email(): string {
    const applicant = this.applicant();
    if (applicant?.user?.email) {
      return applicant.user.email;
    }
    return '';
  }

  /**
   * The `selectedProfile` control of the accept form, exposed for template
   * binding and validation messages.
   * @returns The reactive form control for the selected profile.
   */
  get selectedProfileControl(): AbstractControl {
    return this.inviteForm.controls.selectedProfile;
  }
}
