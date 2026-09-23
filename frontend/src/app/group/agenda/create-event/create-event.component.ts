import {
  ChangeDetectionStrategy,
  Component,
  effect,
  inject,
  input,
  model,
  OnChanges,
  OnInit,
  output,
  resource,
  signal,
} from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatRadioModule } from '@angular/material/radio';
import { RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import {
  EventDefinition,
  EventsService,
  InterestGroup,
  InterestGroupService,
  Node as ModelNode,
  NodesService,
  RepeatsInfo,
  User,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { getFormattedTime } from 'app/core/util';
import {
  attendantsValidator,
  dateInfoValidator,
  emailsValidator,
  emailValidator,
  pastDateValidator,
  phoneValidator,
  repeatsSelectedValidator,
} from 'app/core/validation.service';
import { TimezoneSelectorComponent } from 'app/group/agenda/timezone/timezone-selector.component';
import { defaultTimezone } from 'app/group/agenda/timezones/supported-timezones';
import { ControlMessageComponent } from 'app/shared/control-message/control-message.component';
import { LangSelectorComponent } from 'app/shared/lang/lang-selector.component';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';
import { TreeNode } from 'app/shared/treeview/tree-node';
import { TreeViewComponent } from 'app/shared/treeview/tree-view.component';
import { UsersPickerComponent } from 'app/shared/users/users-picker.component';

/**
 * Standalone component (`cbc-create-event`) that renders a multi-step wizard
 * for creating a new agenda event or meeting inside an interest group.
 *
 * The wizard is driven by a single reactive {@link FormGroup}
 * ({@link CreateEventComponent.newEventForm}) split across three logical steps:
 * 1. General details (title, type, date/time, timezone, language, repetition).
 * 2. Attendants and notification settings.
 * 3. Contact information.
 *
 * On save it assembles an {@link EventDefinition} and persists it through the
 * generated {@link EventsService}. It collaborates with
 * {@link InterestGroupService} to resolve the group, {@link LoginService} to
 * pre-fill contact details from the current {@link User}, and
 * {@link NodesService} to build the library section tree when the group grants
 * library access.
 *
 * The component is typically displayed as a modal; visibility is controlled by
 * the {@link CreateEventComponent.showModal} model and closing/saving is
 * signalled through its outputs.
 */
@Component({
  selector: 'cbc-create-event',
  templateUrl: './create-event.component.html',
  styleUrl: './create-event.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ReactiveFormsModule,
    MatRadioModule,
    ControlMessageComponent,
    MatDatepickerModule,
    MatInputModule,
    MatFormFieldModule,
    TimezoneSelectorComponent,
    LangSelectorComponent,
    UsersPickerComponent,
    TreeViewComponent,
    SpinnerComponent,
    RouterLink,
    TranslocoModule,
  ],
})
export class CreateEventComponent implements OnInit, OnChanges {
  /** Builder used to construct the reactive wizard form. */
  private readonly formBuilder = inject(FormBuilder);
  /** Resolves the interest group the event is being created in. */
  private readonly interestGroupService = inject(InterestGroupService);
  /** Provides the currently authenticated user for contact pre-fill. */
  private readonly loginService = inject(LoginService);
  /** Generated API service used to persist the new event/meeting. */
  private readonly eventsService = inject(EventsService);
  /** Generated API service used to resolve library nodes/paths. */
  private readonly nodesService = inject(NodesService);

  /**
   * Two-way bound flag controlling whether the wizard modal is visible.
   * Set to `false` to close the wizard.
   */
  public showModal = model(false);
  /** Emitted when the wizard modal is hidden/closed without further action. */
  public readonly modalHide = output();
  /**
   * Emitted after a new event/meeting has been successfully saved so parent
   * views (e.g. the agenda calendar) can refresh.
   */
  public readonly eventMeetingSaved = output();
  /** Required input: the date pre-selected for the new event. */
  public readonly defaultDate = input.required<Date>();
  /** Required input: identifier of the interest group hosting the event. */
  public readonly igId = input.required<string>();
  /**
   * Two-way bound flag indicating the appointment kind:
   * `true` for a plain event, `false` for a meeting.
   */
  public appointmentTypeEvent = model(true);

  /** Breadcrumb path of library nodes leading to the group's library root. */
  public path: ModelNode[] = [];

  /**
   * Resource that loads the interest group identified by {@link igId}. When no
   * id is provided the loader does not run.
   */
  private readonly igResource = resource({
    params: () => this.igId(),
    loader: ({ params }) =>
      this.interestGroupService.getInterestGroupAsync({ id: params }),
  });

  /** The resolved interest group, loaded reactively from {@link igId}. */
  private get ig(): InterestGroup | undefined {
    return this.igResource.value();
  }

  /**
   * Resource that loads the group's library breadcrumb path once the group is
   * resolved and grants library access. Idle otherwise.
   */
  private readonly libraryPathResource = resource({
    params: () => {
      const ig = this.igResource.value();
      if (ig?.libraryId && ig.permissions.library !== 'LibNoAccess') {
        return ig.libraryId;
      }
      return undefined;
    },
    loader: ({ params }) => this.nodesService.getPathAsync({ id: params }),
  });

  constructor() {
    // Keep the library section tree in sync with the loaded interest group and
    // its resolved path, so the template can render the picker once available.
    effect(() => {
      const ig = this.igResource.value();
      if (ig?.libraryId && ig.permissions.library !== 'LibNoAccess') {
        this.libraryRoot = new TreeNode('Library', ig.libraryId);
      }
      const path = this.libraryPathResource.value();
      if (path) {
        this.path = path;
      }
    });
  }

  /** The currently authenticated user, used to pre-fill contact fields. */
  public user!: User;

  /** Becomes `true` once data is loaded and the form has been built. */
  public readonly formReady = signal(false);

  // controls the step the wizard is at (1..n)
  /** Current wizard step (1-based, ranging from 1 to 3). */
  public wizardStep = 1;

  // to enable/disable the spinner for lengthy operations
  /** `true` while a lengthy operation (e.g. saving) is in progress. */
  public readonly processing = signal(false);

  /** The reactive form backing all wizard steps. */
  public newEventForm!: FormGroup;

  /** Whether recurrence/repetition is currently enabled for the event. */
  public repeatsSelected = false;

  /** Whether the library section tree picker should be shown. */
  public showPicker = true;

  /** Root node of the library section tree when the group grants access. */
  public libraryRoot!: TreeNode;

  /** Currently selected language code for the event (defaults to `en`). */
  public currentLanguage = 'en';

  /** Minimum selectable date for the event (today). */
  public minDate = new Date();

  /**
   * Angular lifecycle hook. Builds the form and marks it ready. The interest
   * group and library path are loaded reactively by their resources, so the
   * hook stays synchronous.
   */
  public ngOnInit(): void {
    this.buildForm();
    this.formReady.set(true);
  }

  /**
   * Angular lifecycle hook invoked on input changes. Rebuilds the form and
   * resets the wizard back to the first step.
   */
  public ngOnChanges(): void {
    this.formReady.set(false);
    this.buildForm();
    this.wizardStep = 1;
    this.formReady.set(true);
  }

  /**
   * Builds {@link newEventForm} with all three wizard steps, seeding sensible
   * defaults (rounded start/end times, current user contact details, default
   * timezone) and attaching the relevant validators. The library section tree
   * is populated reactively from {@link igId} by the component's resources.
   */
  private buildForm(): void {
    this.user ??= this.loginService.getUser();

    const currentDate: Date = new Date();
    currentDate.setHours(currentDate.getHours() + 1);
    currentDate.setMinutes(0, 0, 0);
    const startTimeString = getFormattedTime(currentDate);

    const endDate: Date = currentDate;
    const slotInMinutes = 15;
    endDate.setMinutes(endDate.getMinutes() + slotInMinutes);
    const endTimeString = getFormattedTime(endDate);

    const maxTitleLength = 250;
    this.newEventForm = this.formBuilder.group(
      {
        // first wizard step
        appointmentTypeEvent: [this.appointmentTypeEvent()],
        title: [
          '',
          [Validators.required, Validators.maxLength(maxTitleLength)],
        ],
        eventType: ['Task'],
        meetingType: ['FaceToFace', Validators.required],
        dateInfo: this.formBuilder.group(
          {
            date: [this.defaultDate(), pastDateValidator],
            startTime: [startTimeString],
            endTime: [endTimeString],
          },
          { validators: dateInfoValidator }
        ),
        language: ['en'],
        timezone: [defaultTimezone.value],
        abstract: [''],
        invitationMessage: [''],
        location: [''],
        eventPriority: ['Low'],
        meetingOrganisation: [''],
        meetingPublicAvailability: [true],
        meetingLibrarySection: [''],
        repeatsInfo: this.formBuilder.group(
          {
            repeats: [this.repeatsSelected],
            whenOrEverySelect: ['whenSelect'],
            timesOccurence: ['Daily'],
            everyTimesOccurence: ['days'],
            everyTime: [2, [Validators.min(2), Validators.max(10)]],
            times: [2, [Validators.min(2), Validators.max(10)]],
          },
          { validators: repeatsSelectedValidator }
        ),
        // second wizard step
        attendantsInfo: this.formBuilder.group(
          {
            audienceStatusOpen: [true],
            invitedUsersOrProfiles: [[]],
            invitedExternalEmails: ['', emailsValidator],
          },
          { validators: attendantsValidator }
        ),
        enableNotification: [false],
        useBCC: [false],
        // third wizard step
        contactName: [
          `${this.user.firstname} ${this.user.lastname}`,
          Validators.required,
        ],
        contactPhone: [this.user.phone, [phoneValidator]],
        contactEmail: [this.user.email, [Validators.required, emailValidator]],
        contactURL: [''],
      },
      {
        updateOn: 'change',
      }
    );
  }

  /**
   * Indicates whether the current user has any access to the group library.
   *
   * @returns `true` when the group is loaded and its library permission is not
   * `LibNoAccess`; otherwise `false`.
   */
  public hasLibraryAccess(): boolean {
    if (this.ig === undefined) {
      return false;
    }
    return this.ig.permissions.library !== 'LibNoAccess';
  }

  /**
   * Determines whether the user can navigate back a wizard step.
   *
   * @returns `true` when past the first step and no operation is in progress.
   */
  public canGoBack() {
    return this.wizardStep > 1 && !this.processing();
  }

  /**
   * Validates the controls relevant to the current wizard step.
   *
   * @returns `true` when the controls for the active step are valid.
   */
  public stepIsValid(): boolean {
    // could be merged into one big OR, but I leave it like this for readability
    if (
      this.wizardStep === 1 &&
      this.newEventForm.controls.title.valid &&
      this.newEventForm.controls.dateInfo.valid
    ) {
      return true;
    }
    if (this.wizardStep === 2 && this.newEventForm.controls.repeatsInfo.valid) {
      return true;
    }
    if (
      this.wizardStep === 3 &&
      this.newEventForm.controls.attendantsInfo.valid
    ) {
      return true;
    }
    return false;
  }

  /**
   * Closes the wizard by hiding the modal and emitting {@link modalHide}.
   *
   * @param _action Reserved action identifier describing how the wizard was
   * closed (currently unused).
   */
  public closeWizard(_action: string): void {
    this.showModal.set(false);
    this.modalHide.emit();
  }

  /** Toggles the local {@link repeatsSelected} recurrence flag. */
  public toggleRepeats(): void {
    this.repeatsSelected = !this.repeatsSelected;
  }

  /** Toggles between event and meeting appointment types. */
  public toggleEventOrMeeting(): void {
    this.appointmentTypeEvent.update((value) => !value);
  }

  /**
   * Sets the appointment type from a radio selection, keeping both the form
   * control and the {@link appointmentTypeEvent} model in sync.
   *
   * @param option `true` to select an event, `false` to select a meeting.
   */
  public radioEventOrMeeting(option: boolean): void {
    this.newEventForm.controls.appointmentTypeEvent.setValue(option);
    this.appointmentTypeEvent.set(option);
  }

  /**
   * Reacts to changes of the "open audience" toggle: when the audience is
   * open, clears any previously selected invited users or profiles.
   */
  public toggleAudienceStatus(): void {
    const attendantsInfo = this.newEventForm.controls.attendantsInfo;
    const audienceStatusOpen = attendantsInfo.get('audienceStatusOpen');
    const invitedUsersOrProfiles = attendantsInfo.get('invitedUsersOrProfiles');
    if (audienceStatusOpen?.value) {
      if (invitedUsersOrProfiles) {
        invitedUsersOrProfiles.setValue([]);
      }
    }
  }

  /** Hides the library section picker and shows the "no picker" text instead. */
  public showNoPickerText() {
    this.showPicker = false;
  }

  /**
   * Selects (or toggles off) a library section for the meeting from the tree.
   * Selecting the currently selected node clears the selection.
   *
   * @param node The tree node representing the chosen library section.
   */
  public setLibrarySectionNode(node: TreeNode) {
    if (node !== undefined) {
      this.newEventForm.controls.meetingLibrarySection.setValue(
        node.nodeId === this.newEventForm.controls.meetingLibrarySection.value
          ? undefined
          : node.nodeId
      );
    }
  }

  /** Advances to the next wizard step when the current step is valid. */
  public nextWizardStep(): void {
    if (this.stepIsValid()) {
      this.wizardStep += 1;
    }
  }

  /** Returns to the previous wizard step. */
  public previousWizardStep(): void {
    this.wizardStep -= 1;
  }

  /**
   * Assembles an {@link EventDefinition} from the form value and persists the
   * new event/meeting via {@link EventsService.postEvent}. Normalises the
   * date, maps invited users/profiles to their identifiers, derives the main
   * occurrence and coerces numeric recurrence values before submitting. On
   * success emits {@link eventMeetingSaved} and closes the wizard.
   *
   * The {@link processing} flag is set while the request runs and always
   * cleared afterwards.
   *
   * @returns A promise that resolves once the save flow has completed.
   */
  public async save() {
    try {
      this.processing.set(true);

      // adjust date to be passed to the backend in the expected way
      const adaptedDate: Date = this.newEventForm.controls.dateInfo.value.date;
      adaptedDate.setHours(12, 0, 0, 0);
      (
        this.newEventForm.controls.dateInfo as FormGroup
      ).controls.date.patchValue(adaptedDate);

      // uncoment when angular fix problem https://github.com/angular/angular-cli/issues/4178
      const eventDefinition: EventDefinition = { ...this.newEventForm.value };

      eventDefinition.timezone = this.newEventForm.value.timezone;

      // retrieve only the ids to send
      if (
        eventDefinition.repeatsInfo !== undefined &&
        eventDefinition.attendantsInfo?.invitedUsersOrProfiles !== undefined
      ) {
        eventDefinition.attendantsInfo.invitedUsersOrProfiles =
          eventDefinition.attendantsInfo.invitedUsersOrProfiles
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
            .map((item: any) =>
              item.userId === undefined ? item.groupName : item.userId
            );

        // add mainOccurence derived property to the eventDefinition
        eventDefinition.repeatsInfo.mainOccurence = this.getMainOccurence();

        eventDefinition.repeatsInfo.everyTime = Number(
          eventDefinition.repeatsInfo.everyTime
        );
        eventDefinition.repeatsInfo.times = Number(
          eventDefinition.repeatsInfo.times
        );

        // create new event/meeting
        await this.eventsService.postEventAsync({
          id: this.igId(),
          eventDefinition,
        });
      }

      // emit an event to signal that a new event/meeting has been created
      // will be used by the agenda->calendar to redisplay the view
      this.eventMeetingSaved.emit();

      // close form/wizard
      this.closeWizard('close');
    } finally {
      this.processing.set(false);
    }
  }

  /**
   * Derives the recurrence main-occurrence mode from the form's repeat info.
   *
   * @returns `OnlyOnce` when repetition is disabled, `Times` when repeating a
   * fixed number of times, otherwise `EveryTimes`.
   */
  private getMainOccurence(): RepeatsInfo.MainOccurenceEnum {
    if (!this.newEventForm.value.repeatsInfo.repeats) {
      return 'OnlyOnce';
    }
    return this.newEventForm.value.repeatsInfo.whenOrEverySelect ===
      'whenSelect'
      ? 'Times'
      : 'EveryTimes';
  }

  /** @returns The `title` form control. */
  get titleControl(): AbstractControl {
    return this.newEventForm.controls.title;
  }

  /** @returns The `meetingType` form control. */
  get meetingTypeControl(): AbstractControl {
    return this.newEventForm.controls.meetingType;
  }

  /** @returns The `dateInfo` form group control (date, start/end time). */
  get dateInfoControl(): AbstractControl {
    return this.newEventForm.controls.dateInfo;
  }

  /** @returns The `repeatsInfo` form group control (recurrence settings). */
  get repeatsInfoControl(): AbstractControl {
    return this.newEventForm.controls.repeatsInfo;
  }

  /** @returns The `meetingPublicAvailability` form control. */
  get meetingPublicAvailabilityControl(): AbstractControl {
    return this.newEventForm.controls.meetingPublicAvailability;
  }

  /** @returns The nested `audienceStatusOpen` control within attendants info. */
  get audienceStatusOpenControl(): AbstractControl {
    return (this.newEventForm.controls.attendantsInfo as FormGroup).controls
      .audienceStatusOpen;
  }

  /** @returns The `meetingLibrarySection` form control. */
  get meetingLibrarySectionControl(): AbstractControl {
    return this.newEventForm.controls.meetingLibrarySection;
  }

  /** @returns The nested `invitedExternalEmails` control within attendants info. */
  get invitedExternalEmailsControl(): AbstractControl {
    return (this.newEventForm.controls.attendantsInfo as FormGroup).controls
      .invitedExternalEmails;
  }

  /** @returns The `attendantsInfo` form group control. */
  get attendantsInfoControl(): AbstractControl {
    return this.newEventForm.controls.attendantsInfo;
  }

  /** @returns The `contactName` form control. */
  get contactNameControl(): AbstractControl {
    return this.newEventForm.controls.contactName;
  }

  /** @returns The `contactPhone` form control. */
  get contactPhoneControl(): AbstractControl {
    return this.newEventForm.controls.contactPhone;
  }

  /** @returns The `contactEmail` form control. */
  get contactEmailControl(): AbstractControl {
    return this.newEventForm.controls.contactEmail;
  }

  /** @returns The nested `times` control within repeat info. */
  get timesControl(): AbstractControl {
    return (this.newEventForm.controls.repeatsInfo as FormGroup).controls.times;
  }

  /** @returns The nested `everyTime` control within repeat info. */
  get everyTimeControl(): AbstractControl {
    return (this.newEventForm.controls.repeatsInfo as FormGroup).controls
      .everyTime;
  }

  /**
   * Indicates whether the selected meeting type is something other than the
   * two built-in kinds, requiring an "other" free-text option.
   *
   * @returns `true` when the meeting type is neither `FaceToFace` nor
   * `VirtualMeeting`.
   */
  otherSelected() {
    return (
      this.newEventForm.controls.meetingType.value !== 'FaceToFace' &&
      this.newEventForm.controls.meetingType.value !== 'VirtualMeeting'
    );
  }
}
