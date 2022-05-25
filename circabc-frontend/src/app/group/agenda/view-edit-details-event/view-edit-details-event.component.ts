import { Location } from '@angular/common';
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  Validators,
} from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

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
import { emailWellFormed } from 'app/core/util';
import { ValidationService } from 'app/core/validation.service';
import { TreeNode } from 'app/shared/treeview/tree-node';
import { firstValueFrom } from 'rxjs';

interface AudienceDetails {
  userDetails: string;
  status: string;
}

@Component({
  selector: 'cbc-view-edit-details-event',
  templateUrl: './view-edit-details-event.component.html',
  styleUrls: ['./view-edit-details-event.component.scss'],
  preserveWhitespaces: true,
})
export class ViewEditDetailsEventComponent implements OnInit {
  @Output()
  public readonly eventMeetingUpdated = new EventEmitter();
  @Input()
  public defaultDate!: Date;
  public eventId!: string;
  public igId!: string;
  public formReady = false;
  public path: ModelNode[] = [];
  private ig!: InterestGroup;
  private eventRootNode!: ModelNode;

  public selectedTab = 'GeneralInformation';

  // history modal
  public historyShowModal = false;

  public user!: User;

  // to enable/disable the spinner for lengthy operations
  public processing = false;
  // viewing or editing
  // viewing variable has been disabled because of request https://webgate.ec.europa.eu/CITnet/jira/browse/DIGITCIRCABC-3489
  public viewing = false;

  public updateEventForm!: FormGroup;

  public repeatsSelected = false;
  public appointmentTypeEvent = true;
  public showPicker = true;

  public audienceEntries!: AudienceEntry[];
  public audience: AudienceDetails[] = [];

  public libraryRoot!: TreeNode;

  public constructor(
    private route: ActivatedRoute,
    private formBuilder: FormBuilder,
    private loginService: LoginService,
    private eventsService: EventsService,
    private nodesService: NodesService,
    private permEvalService: PermissionEvaluatorService,
    private userService: UserService,
    private groupService: InterestGroupService,
    private uiMessageService: UiMessageService,
    private location: Location
  ) {}

  public ngOnInit() {
    this.ngInit();
  }

  private ngInit() {
    this.route.params.subscribe(async (params) => this.getParams(params));
  }

  private async getParams(params: { [key: string]: string }) {
    this.eventId = params.eventId;
    await this.init();
    this.checkDisableControls();
    this.formReady = true;
  }

  private async init() {
    try {
      await this.buildForm();

      this.audience = [];

      // collect the user details for the audience to be displayed on the screen
      for (const audienceEntry of this.audienceEntries) {
        // eslint-disable-next-line @typescript-eslint/consistent-type-assertions
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

  // eslint-disable-next-line
  private async buildForm() {
    try {
      if (this.user === undefined) {
        this.user = this.loginService.getUser();
      }

      // get the details of the event
      const eventDefinition = await firstValueFrom(
        this.eventsService.getEvent(this.eventId)
      );

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
              date: [eventDefinition.dateInfo.date],
              startTime: [eventDefinition.dateInfo.startTime],
              endTime: [eventDefinition.dateInfo.endTime],
            },
            { validators: ValidationService.timeInfoValidator }
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
            { validators: ValidationService.repeatsSelectedValidator }
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
                ValidationService.emailsValidator,
              ],
            },
            { validators: ValidationService.attendantsValidator }
          ),
          enableNotification: [eventDefinition.enableNotification],
          contactName: [eventDefinition.contactName, Validators.required],
          contactPhone: [
            eventDefinition.contactPhone,
            [ValidationService.phoneValidator],
          ],
          contactEmail: [
            eventDefinition.contactEmail,
            [Validators.required, ValidationService.emailValidator],
          ],
          contactURL: [eventDefinition.contactUrl],
        },
        {
          updateOn: 'change',
        }
      );

      if (eventDefinition.igId !== undefined) {
        this.igId = eventDefinition.igId;
        if (this.ig === undefined) {
          this.ig = await firstValueFrom(
            this.groupService.getInterestGroup(this.igId)
          );
        }
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

  private async buildLibrarySectionTree() {
    this.eventRootNode = await firstValueFrom(
      this.nodesService.getNode(this.ig.eventId as string)
    );

    // if I don't check for this.libraryRoot === undefined it will destroy the tree, as the root has already been set
    // (onInit will not run again on the tree)
    if (this.ig && this.ig.libraryId && this.libraryRoot === undefined) {
      const root = new TreeNode('Library', this.ig.libraryId);
      this.libraryRoot = root;

      if (
        this.updateEventForm.controls.meetingLibrarySection.value !== undefined
      ) {
        this.path = await firstValueFrom(
          this.nodesService.getPath(
            this.updateEventForm.controls.meetingLibrarySection.value
          )
        );
      }
    }
  }

  public hasLibraryAccess(): boolean {
    if (this.ig === undefined) {
      return false;
    }
    return this.ig.permissions.library !== 'LibNoAccess';
  }

  public showNoPickerText() {
    this.showPicker = false;
  }

  public isGeneralTab(): boolean {
    return this.selectedTab === 'GeneralInformation';
  }

  public isRelevantSpaceTab(): boolean {
    return this.selectedTab === 'RelevantSpace';
  }

  public isAudienceTab(): boolean {
    return this.selectedTab === 'Audience';
  }

  public isContactTab(): boolean {
    return this.selectedTab === 'ContactInformation';
  }

  public setTab(tab: string) {
    this.updateEventForm.controls.updateInfo.patchValue(tab);
    this.selectedTab = tab;
  }

  public goBack() {
    this.location.back();
  }

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

  public toggleRepeats(): void {
    this.repeatsSelected = !this.repeatsSelected;
  }

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

  public setLibrarySectionNode(node: TreeNode) {
    if (node !== undefined) {
      this.updateEventForm.controls.meetingLibrarySection.setValue(
        node.nodeId !==
          this.updateEventForm.controls.meetingLibrarySection.value
          ? node.nodeId
          : undefined
      );
    }
  }

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

  public enableEdit() {
    // enable all form controls for edit
    Object.keys(this.updateEventForm.controls).forEach((key) => {
      (this.updateEventForm.get(key) as AbstractControl).enable();
    });

    this.viewing = false;
  }

  // check if the current user is part of the audience and if the status is the one given as parameter
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

  public async updateMeetingRequest(action: 'Accepted' | 'Rejected') {
    if (this.user.userId !== undefined) {
      this.processing = true;

      await firstValueFrom(
        this.userService.postUserEvent(
          this.user.userId,
          this.eventId,
          action,
          this.updateEventForm.controls.occurrenceSelection.value
        )
      );

      this.processing = false;

      // reload and redisplay
      this.ngInit();
    }
  }

  public async cancel() {
    this.checkDisableControls();

    // just enable the update mode selection field
    this.updateEventForm.controls.occurrenceSelection.enable();

    this.goBack();
  }

  public async update() {
    try {
      this.processing = true;

      // uncoment when angular fix problem https://github.com/angular/angular-cli/issues/4178
      const eventDefinition: EventDefinition = {
        ...this.updateEventForm.value,
      };

      // retrieve only the ids to send
      if (
        eventDefinition.attendantsInfo !== undefined &&
        eventDefinition.attendantsInfo.invitedUsersOrProfiles !== undefined
      ) {
        eventDefinition.attendantsInfo.invitedUsersOrProfiles =
          eventDefinition.attendantsInfo.invitedUsersOrProfiles
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
            .map((item: any) =>
              item.userId === undefined ? item : item.userId
            );

        // update the event/meeting
        await firstValueFrom(
          this.eventsService.putEvent(
            this.eventId,
            'All',
            this.updateEventForm.controls.occurrenceSelection.value,
            eventDefinition
          )
        );

        // emit an event to signal that a new event/meeting has been updated
        // will be used by the agenda->calendar to redisplay the view
        this.eventMeetingUpdated.emit();
      }
    } finally {
      this.processing = false;
    }

    this.goBack();
  }

  public isEmail(audienceEntry: AudienceEntry) {
    return (
      audienceEntry.userId !== undefined &&
      emailWellFormed(audienceEntry.userId)
    );
  }

  public isEvent(): boolean {
    return this.appointmentTypeEvent;
  }

  public isMeeting(): boolean {
    return !this.appointmentTypeEvent;
  }

  public isEveAdmin(): boolean {
    return this.permEvalService.isEveAdmin(this.eventRootNode);
  }

  get isEventViewDetails(): boolean {
    return this.appointmentTypeEvent && !this.isEveAdmin();
  }

  get isMeetingViewDetails(): boolean {
    return !this.appointmentTypeEvent && !this.isEveAdmin();
  }

  get isEventEditDetails(): boolean {
    return this.appointmentTypeEvent && !this.viewing && this.isEveAdmin();
  }

  get isMeetingEditDetails(): boolean {
    return !this.appointmentTypeEvent && !this.viewing && this.isEveAdmin();
  }

  get repeatsControl(): AbstractControl {
    return (this.updateEventForm.controls.repeatsInfo as FormGroup).controls
      .repeats;
  }

  get meetingTypeControl(): AbstractControl {
    return this.updateEventForm.controls.meetingType;
  }

  get everyTimeControl(): AbstractControl {
    return (this.updateEventForm.controls.repeatsInfo as FormGroup).controls
      .everyTime;
  }

  get everyTimesOccurenceControl(): AbstractControl {
    return (this.updateEventForm.controls.repeatsInfo as FormGroup).controls
      .everyTimesOccurence;
  }

  get timesControl(): AbstractControl {
    return (this.updateEventForm.controls.repeatsInfo as FormGroup).controls
      .times;
  }

  get timesOccurenceControl(): AbstractControl {
    return (this.updateEventForm.controls.repeatsInfo as FormGroup).controls
      .timesOccurence;
  }

  get meetingPublicAvailabilityControl(): AbstractControl {
    return this.updateEventForm.controls.meetingPublicAvailability;
  }

  get timezoneControl(): AbstractControl {
    return this.updateEventForm.controls.timezone;
  }

  get meetingLibrarySectionControl(): AbstractControl {
    return this.updateEventForm.controls.meetingLibrarySection;
  }

  get whenOrEverySelectControl(): AbstractControl {
    return (this.updateEventForm.controls.repeatsInfo as FormGroup).controls
      .whenOrEverySelect;
  }
  get audienceStatusOpenControl(): AbstractControl {
    return (this.updateEventForm.controls.attendantsInfo as FormGroup).controls
      .audienceStatusOpen;
  }

  get dateControl(): AbstractControl {
    return (this.updateEventForm.controls.dateInfo as FormGroup).controls.date;
  }

  get titleControl(): AbstractControl {
    return this.updateEventForm.controls.title;
  }

  get dateInfoControl(): AbstractControl {
    return this.updateEventForm.controls.dateInfo;
  }

  get repeatsInfoControl(): AbstractControl {
    return this.updateEventForm.controls.repeatsInfo;
  }

  get contactNameControl(): AbstractControl {
    return this.updateEventForm.controls.contactName;
  }

  get contactPhoneControl(): AbstractControl {
    return this.updateEventForm.controls.contactPhone;
  }

  get contactEmailControl(): AbstractControl {
    return this.updateEventForm.controls.contactEmail;
  }

  get invitedExternalEmailsControl(): AbstractControl {
    return (this.updateEventForm.controls.attendantsInfo as FormGroup).controls
      .invitedExternalEmails;
  }

  get attendantsInfoControl(): AbstractControl {
    return this.updateEventForm.controls.attendantsInfo;
  }

  otherSelected() {
    return (
      this.updateEventForm.controls.meetingType.value !== 'FaceToFace' &&
      this.updateEventForm.controls.meetingType.value !== 'VirtualMeeting'
    );
  }
}
