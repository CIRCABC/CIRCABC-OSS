import { Location } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  OnInit,
  output,
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
import { ActivatedRoute, RouterLink } from '@angular/router';

import { TranslocoModule } from '@jsverse/transloco';
import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import {
  AudienceEntry,
  EventDefinition,
  EventsService,
  InterestGroup,
  InterestGroupService,
  Node as ModelNode,
  NodesService,
  RepeatsInfo,
  User,
  UserService,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { emailWellFormed, getFormattedDate } from 'app/core/util';
import {
  attendantsValidator,
  emailsValidator,
  emailValidator,
  phoneValidator,
  repeatsSelectedValidator,
  timeInfoValidator,
} from 'app/core/validation.service';
import { TimezoneSelectorComponent } from 'app/group/agenda/timezone/timezone-selector.component';
import { ControlMessageComponent } from 'app/shared/control-message/control-message.component';
import { HistoryComponent } from 'app/shared/history/history.component';
import { LangSelectorComponent } from 'app/shared/lang/lang-selector.component';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';
import { TreeNode } from 'app/shared/treeview/tree-node';
import { TreeViewComponent } from 'app/shared/treeview/tree-view.component';
import { UserCardComponent } from 'app/shared/user-card/user-card.component';
import { UsersPickerComponent } from 'app/shared/users/users-picker.component';

/**
 * View model describing a single audience member of an event/meeting as it is
 * rendered on screen.
 */
interface AudienceDetails {
  /** Identifier or display details of the audience member (user id or email). */
  userDetails: string;
  /** Invitation/attendance status of the audience member (e.g. Accepted, Rejected). */
  status: string;
}

/**
 * Angular component that renders the detailed view/edit form for a single
 * agenda event or meeting.
 *
 * It loads an {@link EventDefinition} from the backend for the event id taken
 * from the route, builds a reactive {@link FormGroup} reflecting all event
 * properties (general information, scheduling/repeat rules, audience,
 * relevant library space and contact information), and exposes tab-based
 * navigation between those sections.
 *
 * Depending on the current user's permissions (see {@link isEveAdmin}) the
 * form is shown either in read-only mode or as an editable form. Event
 * administrators can update the event; regular invitees can accept or reject a
 * meeting request. On a successful update the component emits
 * {@link eventMeetingUpdated} so the parent agenda/calendar can refresh.
 *
 * Key collaborators: {@link EventsService} and {@link UserService} (event
 * CRUD and attendance), {@link NodesService} and {@link InterestGroupService}
 * (interest group and library tree resolution), {@link PermissionEvaluatorService}
 * (permission checks) and {@link UiMessageService} (error reporting).
 */
@Component({
  selector: 'cbc-view-edit-details-event',
  templateUrl: './view-edit-details-event.component.html',
  styleUrl: './view-edit-details-event.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ReactiveFormsModule,
    ControlMessageComponent,
    LangSelectorComponent,
    UserCardComponent,
    UsersPickerComponent,
    RouterLink,
    TreeViewComponent,
    SpinnerComponent,
    HistoryComponent,
    TranslocoModule,
    MatDatepickerModule,
    MatInputModule,
    MatFormFieldModule,
    TimezoneSelectorComponent,
  ],
})
export class ViewEditDetailsEventComponent implements OnInit {
  /** Provides access to the current route, used to read the `eventId` parameter. */
  private readonly route = inject(ActivatedRoute);
  /** Builds the reactive form describing the event/meeting. */
  private readonly formBuilder = inject(FormBuilder);
  /** Supplies the currently authenticated user. */
  private readonly loginService = inject(LoginService);
  /** Backend API for reading and updating events/meetings. */
  private readonly eventsService = inject(EventsService);
  /** Backend API for resolving nodes and library paths. */
  private readonly nodesService = inject(NodesService);
  /** Evaluates permissions such as event-administration rights. */
  private readonly permEvalService = inject(PermissionEvaluatorService);
  /** Backend API for posting a user's response to a meeting request. */
  private readonly userService = inject(UserService);
  /** Backend API for resolving the owning interest group. */
  private readonly groupService = inject(InterestGroupService);
  /** Displays UI error messages to the user. */
  private readonly uiMessageService = inject(UiMessageService);
  /** Angular location service used to navigate back after cancel/update. */
  private readonly location = inject(Location);

  /**
   * Output emitted after an event/meeting has been successfully updated so the
   * parent agenda/calendar view can refresh its display.
   */
  public readonly eventMeetingUpdated = output();
  /**
   * Required input providing the default date used when initialising the form
   * (typically the date selected in the calendar).
   */
  public readonly defaultDate = input.required<Date>();
  /** Identifier of the event/meeting being viewed or edited, read from the route. */
  public eventId!: string;
  /** Identifier of the interest group owning the event. */
  public igId!: string;
  /** Whether the form has finished loading and can be rendered. */
  public readonly formReady = signal(false);
  /** Breadcrumb path of nodes to the selected library section. */
  public path: ModelNode[] = [];
  /** The interest group owning the event, resolved lazily. */
  private ig!: InterestGroup;
  /** Root node of the event, used for permission evaluation. */
  private eventRootNode!: ModelNode;

  /** Currently selected tab of the details form. */
  public selectedTab = 'GeneralInformation';

  // history modal
  /** Controls the visibility of the history modal. */
  public historyShowModal = false;

  /** The currently authenticated user. */
  public user!: User;

  // to enable/disable the spinner for lengthy operations
  /** Flag indicating a lengthy operation is in progress (drives the spinner). */
  public readonly processing = signal(false);
  // viewing or editing
  // viewing variable has been disabled because of request https://webgate.ec.europa.eu/CITnet/jira/browse/DIGITCIRCABC-3489
  /** Whether the form is in read-only viewing mode (as opposed to editing). */
  public viewing = false;

  /** Reactive form holding all editable properties of the event/meeting. */
  public updateEventForm!: FormGroup;

  /** Whether recurrence/repeat options are currently selected. */
  public repeatsSelected = false;
  /** True when the item is an event, false when it is a meeting. */
  public appointmentTypeEvent = true;
  /** Whether the users picker should be shown. */
  public showPicker = true;

  /** Raw audience entries returned by the backend for the event. */
  public audienceEntries!: AudienceEntry[];
  /** Audience view models derived from {@link audienceEntries} for display. */
  public audience: AudienceDetails[] = [];

  /** Root tree node of the interest group's library for section selection. */
  public libraryRoot!: TreeNode;
  /** Minimum selectable date in the date picker (today). */
  public minDate = new Date();

  /**
   * Angular lifecycle hook; triggers initialisation of the component.
   */
  public ngOnInit() {
    this.ngInit();
  }

  /**
   * Subscribes to route parameter changes and (re)loads the event details for
   * the current `eventId`.
   */
  private ngInit() {
    this.route.params.subscribe(async (params) => this.getParams(params));
  }

  /**
   * Reads the `eventId` from the route parameters, initialises the form and
   * applies the appropriate enabled/disabled state.
   *
   * @param params Route parameters containing the `eventId`.
   */
  private async getParams(params: { [key: string]: string }) {
    this.eventId = params.eventId;
    await this.init();
    this.checkDisableControls();
    this.formReady.set(true);
  }

  /**
   * Builds the form and derives the on-screen audience list from the loaded
   * audience entries. Errors are surfaced via {@link UiMessageService}.
   */
  private async init() {
    try {
      await this.buildForm();

      this.audience = [];

      // collect the user details for the audience to be displayed on the screen
      for (const audienceEntry of this.audienceEntries) {
        this.audience.push({
          userDetails: audienceEntry.userId,
          status: audienceEntry.status,
        } as AudienceDetails);
      }
    } catch (error) {
      if (error.error) {
        this.uiMessageService.addErrorMessage(error.error.message);
      }
    }
  }

  /**
   * Loads the {@link EventDefinition} for the current event and constructs the
   * reactive form (title, type, scheduling, repeat rules, audience, contact
   * and library section). Also resolves the owning interest group and, when
   * library access is available, builds the library section tree.
   * Errors are surfaced via {@link UiMessageService}.
   */
  private async buildForm() {
    try {
      this.user ??= this.loginService.getUser();

      // get the details of the event
      const eventDefinition = await this.eventsService.getEventAsync({
        id: this.eventId,
      });

      const titleMaxLength = 250;

      this.updateEventForm = this.formBuilder.group(
        {
          id: [this.eventId],
          appointmentTypeEvent: [eventDefinition.appointmentTypeEvent],
          occurrenceSelection: ['Single'],
          updateInfo: ['GeneralInformation'],
          title: [
            eventDefinition.title,
            [Validators.required, Validators.maxLength(titleMaxLength)],
          ],
          eventType: [eventDefinition.eventType],
          meetingType: [eventDefinition.meetingType, Validators.required],
          dateInfo: this.formBuilder.group(
            {
              date: [new Date(eventDefinition.dateInfo.date)],
              startTime: [eventDefinition.dateInfo.startTime],
              endTime: [eventDefinition.dateInfo.endTime],
            },
            { validators: timeInfoValidator }
          ),
          language: [eventDefinition.language],
          timezone: [eventDefinition.timezone],
          abstract: [eventDefinition.eventAbstract],
          invitationMessage: [eventDefinition.invitationMessage],
          location: [eventDefinition.location],
          eventPriority: [eventDefinition.eventPriority],
          meetingOrganisation: [eventDefinition.meetingOrganisation],
          meetingPublicAvailability: [
            eventDefinition.meetingPublicAvailability,
          ],
          meetingLibrarySection: [eventDefinition.meetingLibrarySection],
          repeatsInfo: this.formBuilder.group(
            {
              repeats: [
                eventDefinition.repeatsInfo.mainOccurence !==
                  ('OnlyOnce' as RepeatsInfo.MainOccurenceEnum),
              ],
              whenOrEverySelect: [
                eventDefinition.repeatsInfo.mainOccurence ===
                  ('EveryTimes' as RepeatsInfo.MainOccurenceEnum),
              ],
              mainOccurence: [eventDefinition.repeatsInfo.mainOccurence],
              timesOccurence: [eventDefinition.repeatsInfo.timesOccurence],
              everyTimesOccurence: [
                eventDefinition.repeatsInfo.everyTimesOccurence,
              ],
              everyTime: [eventDefinition.repeatsInfo.everyTime],
              times: [eventDefinition.repeatsInfo.times],
            },
            { validators: repeatsSelectedValidator }
          ),
          attendantsInfo: this.formBuilder.group(
            {
              audienceStatusOpen: [
                eventDefinition.attendantsInfo.audienceStatusOpen,
              ],
              invitedUsersOrProfiles: [
                eventDefinition.attendantsInfo.invitedUsersOrProfiles,
              ],
              invitedExternalEmails: [
                this.emailsToEnterString(
                  eventDefinition.attendantsInfo.invitedExternalEmails
                ),
                emailsValidator,
              ],
            },
            { validators: attendantsValidator }
          ),
          enableNotification: [eventDefinition.enableNotification],
          contactName: [eventDefinition.contactName, Validators.required],
          contactPhone: [eventDefinition.contactPhone, [phoneValidator]],
          contactEmail: [
            eventDefinition.contactEmail,
            [Validators.required, emailValidator],
          ],
          contactURL: [eventDefinition.contactUrl],
        },
        {
          updateOn: 'change',
        }
      );

      if (eventDefinition.igId !== undefined) {
        this.igId = eventDefinition.igId;
        this.ig ??= await this.groupService.getInterestGroupAsync({
          id: this.igId,
        });
      }

      if (eventDefinition.attendantsInfo.audience !== undefined) {
        this.audienceEntries = eventDefinition.attendantsInfo.audience;
      }

      // just enable the update mode selection field
      this.updateEventForm.controls.occurrenceSelection.enable();

      this.appointmentTypeEvent = eventDefinition.appointmentTypeEvent;

      if (this.hasLibraryAccess()) {
        await this.buildLibrarySectionTree();
      }
    } catch (error) {
      if (error.error) {
        this.uiMessageService.addErrorMessage(error.error.message);
      }
    }
  }

  /**
   * Enables all form controls when the current user is an event administrator,
   * otherwise disables them (read-only view).
   */
  private checkDisableControls() {
    if (this.isEveAdmin()) {
      Object.keys(this.updateEventForm.controls).forEach((key) => {
        (this.updateEventForm.get(key) as AbstractControl).enable();
      });
    } else {
      Object.keys(this.updateEventForm.controls).forEach((key) => {
        (this.updateEventForm.get(key) as AbstractControl).disable();
      });
    }
  }

  /**
   * Resolves the event's root node and builds the interest group's library
   * section {@link TreeNode} tree, resolving the breadcrumb path to the
   * currently selected meeting library section when one is set.
   */
  private async buildLibrarySectionTree() {
    this.eventRootNode = await this.nodesService.getNodeAsync({
      id: this.ig.eventId as string,
    });

    // if I don't check for this.libraryRoot === undefined it will destroy the tree, as the root has already been set
    // (onInit will not run again on the tree)
    if (this.ig?.libraryId && this.libraryRoot === undefined) {
      const root = new TreeNode('Library', this.ig.libraryId);
      this.libraryRoot = root;

      if (
        this.updateEventForm.controls.meetingLibrarySection.value !== undefined
      ) {
        this.path = await this.nodesService.getPathAsync(
          this.updateEventForm.controls.meetingLibrarySection.value
        );
      }
    }
  }

  /**
   * Indicates whether the current interest group grants access to its library.
   *
   * @returns `true` when the interest group is loaded and its library
   * permission is not `LibNoAccess`; otherwise `false`.
   */
  public hasLibraryAccess(): boolean {
    if (this.ig === undefined) {
      return false;
    }
    return this.ig.permissions.library !== 'LibNoAccess';
  }

  /**
   * Hides the users picker and shows the "no picker" text instead.
   */
  public showNoPickerText() {
    this.showPicker = false;
  }

  /**
   * @returns `true` when the General Information tab is selected.
   */
  public isGeneralTab(): boolean {
    return this.selectedTab === 'GeneralInformation';
  }

  /**
   * @returns `true` when the Relevant Space tab is selected.
   */
  public isRelevantSpaceTab(): boolean {
    return this.selectedTab === 'RelevantSpace';
  }

  /**
   * @returns `true` when the Audience tab is selected.
   */
  public isAudienceTab(): boolean {
    return this.selectedTab === 'Audience';
  }

  /**
   * @returns `true` when the Contact Information tab is selected.
   */
  public isContactTab(): boolean {
    return this.selectedTab === 'ContactInformation';
  }

  /**
   * Selects the given tab and records it in the form's `updateInfo` control.
   *
   * @param tab Identifier of the tab to activate.
   */
  public setTab(tab: string) {
    this.updateEventForm.controls.updateInfo.patchValue(tab);
    this.selectedTab = tab;
  }

  /**
   * Navigates back to the previous location in the browser history.
   */
  public goBack() {
    this.location.back();
  }

  /**
   * Converts a list of email addresses into a single newline-separated string
   * for display in a textarea control.
   *
   * @param emails Email addresses to join, or `undefined`.
   * @returns The newline-separated emails, or an empty string when none.
   */
  private emailsToEnterString(emails: string[] | undefined) {
    if (!emails) {
      return '';
    }

    let emailString = '';

    for (const email of emails) {
      emailString += `${email}\n`;
    }

    return emailString;
  }

  /**
   * Toggles the local flag tracking whether recurrence/repeat options are shown.
   */
  public toggleRepeats(): void {
    this.repeatsSelected = !this.repeatsSelected;
  }

  /**
   * Clears the invited users/profiles list when the audience is set to open,
   * since an open audience does not use an explicit invitee list.
   */
  public toggleAudienceStatus(): void {
    if (
      (
        this.updateEventForm.controls.attendantsInfo.get(
          'audienceStatusOpen'
        ) as AbstractControl
      ).value
    ) {
      (
        this.updateEventForm.controls.attendantsInfo.get(
          'invitedUsersOrProfiles'
        ) as AbstractControl
      ).setValue([]);
    }
  }

  /**
   * Sets or toggles the selected meeting library section from a tree node.
   * Selecting the currently selected node clears the selection.
   *
   * @param node The library section tree node that was selected.
   */
  public setLibrarySectionNode(node: TreeNode) {
    if (node !== undefined) {
      this.updateEventForm.controls.meetingLibrarySection.setValue(
        node.nodeId ===
          this.updateEventForm.controls.meetingLibrarySection.value
          ? undefined
          : node.nodeId
      );
    }
  }

  /**
   * @returns `true` when a meeting library section has been selected
   * (i.e. the control value is not null, empty or undefined).
   */
  public isMeetingLibrarySectionSelected() {
    const meetingLibrarySection = this.updateEventForm.get(
      'meetingLibrarySection'
    ) as AbstractControl;

    return !(
      meetingLibrarySection.value === null ||
      meetingLibrarySection.value === '' ||
      meetingLibrarySection.value === undefined
    );
  }

  /**
   * Enables all form controls to allow editing and clears the viewing flag.
   */
  public enableEdit() {
    // enable all form controls for edit
    Object.keys(this.updateEventForm.controls).forEach((key) => {
      (this.updateEventForm.get(key) as AbstractControl).enable();
    });

    this.viewing = false;
  }

  // check if the current user is part of the audience and if the status is the one given as parameter
  /**
   * Checks whether the current user is part of the audience with the given
   * status (and the audience is not open). Always returns `false` for events.
   *
   * @param status The attendance status to match (e.g. `Accepted`, `Rejected`).
   * @returns `true` when the current user has the given status in a
   * non-open-audience meeting; otherwise `false`.
   */
  public userInAudienceAndStatus(status: string): boolean {
    if (this.updateEventForm.controls.appointmentTypeEvent.value) {
      return false;
    }

    for (const audienceEntry of this.audienceEntries) {
      if (
        audienceEntry.userId === this.user.userId &&
        audienceEntry.status === status &&
        !(
          this.updateEventForm.controls.attendantsInfo.get(
            'audienceStatusOpen'
          ) as AbstractControl
        ).value
      ) {
        return true;
      }
    }

    return false;
  }

  /**
   * Posts the current user's response to a meeting request (accept or reject)
   * for the selected occurrence, then reloads the view.
   *
   * @param action The user's response, either `Accepted` or `Rejected`.
   */
  public async updateMeetingRequest(action: 'Accepted' | 'Rejected') {
    if (this.user.userId !== undefined) {
      this.processing.set(true);

      await this.userService.postUserEventAsync({
        userId: this.user.userId,
        meetingId: this.eventId,
        action,
        updateMode: this.updateEventForm.controls.occurrenceSelection.value,
      });

      this.processing.set(false);

      // reload and redisplay
      this.ngInit();
    }
  }

  /**
   * Cancels editing by restoring the enabled/disabled control state, re-enabling
   * the occurrence selection field and navigating back.
   */
  public async cancel() {
    this.checkDisableControls();

    // just enable the update mode selection field
    this.updateEventForm.controls.occurrenceSelection.enable();

    this.goBack();
  }

  /**
   * Persists the current form values as an updated {@link EventDefinition} via
   * {@link EventsService}. Formats the date, reduces invited users/profiles to
   * their identifiers, emits {@link eventMeetingUpdated} on success and finally
   * navigates back. The processing flag is always reset in a `finally` block.
   */
  public async update() {
    try {
      this.processing.set(true);

      const myDate = getFormattedDate(this.updateEventForm.value.dateInfo.date);

      if (myDate) {
        this.updateEventForm.controls.dateInfo.patchValue({ date: myDate });
      }

      // uncoment when angular fix problem https://github.comissues/4178
      const eventDefinition: EventDefinition = {
        ...this.updateEventForm.value,
      };

      // retrieve only the ids to send
      if (
        eventDefinition.attendantsInfo?.invitedUsersOrProfiles !== undefined
      ) {
        eventDefinition.attendantsInfo.invitedUsersOrProfiles =
          eventDefinition.attendantsInfo.invitedUsersOrProfiles
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
            .map((item: any) =>
              item.userId === undefined ? item : item.userId
            );

        // update the event/meeting
        await this.eventsService.putEventAsync({
          id: this.eventId,
          updateInfo: 'All',
          updateMode: this.updateEventForm.controls.occurrenceSelection.value,
          eventDefinition,
        });

        // emit an event to signal that a new event/meeting has been updated
        // will be used by the agenda->calendar to redisplay the view
        this.eventMeetingUpdated.emit();
      }
    } finally {
      this.processing.set(false);
    }

    this.goBack();
  }

  /**
   * Determines whether an audience entry identifies an external invitee by
   * a well-formed email address rather than a user id.
   *
   * @param audienceEntry The audience entry to inspect.
   * @returns `true` when the entry's `userId` is a well-formed email address.
   */
  public isEmail(audienceEntry: AudienceEntry) {
    return (
      audienceEntry.userId !== undefined &&
      emailWellFormed(audienceEntry.userId)
    );
  }

  /**
   * @returns `true` when the item is an event.
   */
  public isEvent(): boolean {
    return this.appointmentTypeEvent;
  }

  /**
   * @returns `true` when the item is a meeting.
   */
  public isMeeting(): boolean {
    return !this.appointmentTypeEvent;
  }

  /**
   * @returns `true` when the current user has event-administration rights on
   * the event's root node.
   */
  public isEveAdmin(): boolean {
    return this.permEvalService.isEveAdmin(this.eventRootNode);
  }

  /**
   * @returns `true` when an event should be shown in read-only detail mode
   * (an event and the user is not an event admin).
   */
  get isEventViewDetails(): boolean {
    return this.appointmentTypeEvent && !this.isEveAdmin();
  }

  /**
   * @returns `true` when a meeting should be shown in read-only detail mode
   * (a meeting and the user is not an event admin).
   */
  get isMeetingViewDetails(): boolean {
    return !(this.appointmentTypeEvent || this.isEveAdmin());
  }

  /**
   * @returns `true` when an event should be shown in editable detail mode
   * (an event, not viewing, and the user is an event admin).
   */
  get isEventEditDetails(): boolean {
    return this.appointmentTypeEvent && !this.viewing && this.isEveAdmin();
  }

  /**
   * @returns `true` when a meeting should be shown in editable detail mode
   * (a meeting, not viewing, and the user is an event admin).
   */
  get isMeetingEditDetails(): boolean {
    return !(this.appointmentTypeEvent || this.viewing) && this.isEveAdmin();
  }

  /** @returns The `repeats` control from the repeats-info form group. */
  get repeatsControl(): AbstractControl {
    return (this.updateEventForm.controls.repeatsInfo as FormGroup).controls
      .repeats;
  }

  /** @returns The `meetingType` control. */
  get meetingTypeControl(): AbstractControl {
    return this.updateEventForm.controls.meetingType;
  }

  /** @returns The `everyTime` control from the repeats-info form group. */
  get everyTimeControl(): AbstractControl {
    return (this.updateEventForm.controls.repeatsInfo as FormGroup).controls
      .everyTime;
  }

  /** @returns The `everyTimesOccurence` control from the repeats-info form group. */
  get everyTimesOccurenceControl(): AbstractControl {
    return (this.updateEventForm.controls.repeatsInfo as FormGroup).controls
      .everyTimesOccurence;
  }

  /** @returns The `times` control from the repeats-info form group. */
  get timesControl(): AbstractControl {
    return (this.updateEventForm.controls.repeatsInfo as FormGroup).controls
      .times;
  }

  /** @returns The `timesOccurence` control from the repeats-info form group. */
  get timesOccurenceControl(): AbstractControl {
    return (this.updateEventForm.controls.repeatsInfo as FormGroup).controls
      .timesOccurence;
  }

  /** @returns The `meetingPublicAvailability` control. */
  get meetingPublicAvailabilityControl(): AbstractControl {
    return this.updateEventForm.controls.meetingPublicAvailability;
  }

  /** @returns The `timezone` control. */
  get timezoneControl(): AbstractControl {
    return this.updateEventForm.controls.timezone;
  }

  /** @returns The `meetingLibrarySection` control. */
  get meetingLibrarySectionControl(): AbstractControl {
    return this.updateEventForm.controls.meetingLibrarySection;
  }

  /** @returns The `whenOrEverySelect` control from the repeats-info form group. */
  get whenOrEverySelectControl(): AbstractControl {
    return (this.updateEventForm.controls.repeatsInfo as FormGroup).controls
      .whenOrEverySelect;
  }
  /** @returns The `audienceStatusOpen` control from the attendants-info form group. */
  get audienceStatusOpenControl(): AbstractControl {
    return (this.updateEventForm.controls.attendantsInfo as FormGroup).controls
      .audienceStatusOpen;
  }

  /** @returns The `date` control from the date-info form group. */
  get dateControl(): AbstractControl {
    return (this.updateEventForm.controls.dateInfo as FormGroup).controls.date;
  }

  /** @returns The `title` control. */
  get titleControl(): AbstractControl {
    return this.updateEventForm.controls.title;
  }

  /** @returns The `dateInfo` form group control. */
  get dateInfoControl(): AbstractControl {
    return this.updateEventForm.controls.dateInfo;
  }

  /** @returns The `repeatsInfo` form group control. */
  get repeatsInfoControl(): AbstractControl {
    return this.updateEventForm.controls.repeatsInfo;
  }

  /** @returns The `contactName` control. */
  get contactNameControl(): AbstractControl {
    return this.updateEventForm.controls.contactName;
  }

  /** @returns The `contactPhone` control. */
  get contactPhoneControl(): AbstractControl {
    return this.updateEventForm.controls.contactPhone;
  }

  /** @returns The `contactEmail` control. */
  get contactEmailControl(): AbstractControl {
    return this.updateEventForm.controls.contactEmail;
  }

  /** @returns The `invitedExternalEmails` control from the attendants-info form group. */
  get invitedExternalEmailsControl(): AbstractControl {
    return (this.updateEventForm.controls.attendantsInfo as FormGroup).controls
      .invitedExternalEmails;
  }

  /** @returns The `attendantsInfo` form group control. */
  get attendantsInfoControl(): AbstractControl {
    return this.updateEventForm.controls.attendantsInfo;
  }

  /**
   * @returns `true` when the selected meeting type is neither `FaceToFace` nor
   * `VirtualMeeting` (i.e. an "other" type requiring a custom description).
   */
  otherSelected() {
    return (
      this.updateEventForm.controls.meetingType.value !== 'FaceToFace' &&
      this.updateEventForm.controls.meetingType.value !== 'VirtualMeeting'
    );
  }
}
