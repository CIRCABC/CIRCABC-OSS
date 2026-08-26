import {
  Component,
  Input,
  OnChanges,
  OnInit,
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
  emailValidator,
  emailsValidator,
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
import { DatePicker } from 'primeng/datepicker';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-create-event',
  templateUrl: './create-event.component.html',
  styleUrl: './create-event.component.scss',
  preserveWhitespaces: true,
  imports: [
    ReactiveFormsModule,
    MatRadioModule,
    ControlMessageComponent,
    DatePicker,
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
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  public showModal = false;
  public readonly modalHide = output();
  public readonly eventMeetingSaved = output();
  public readonly defaultDate = input.required<Date>();
  public readonly igId = input.required<string>();
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  public appointmentTypeEvent = true;

  public path: ModelNode[] = [];

  private ig!: InterestGroup;

  public user!: User;

  public formReady = false;

  // controls the step the wizard is at (1..n)
  public wizardStep = 1;

  // to enable/disable the spinner for lengthy operations
  public processing = false;

  public newEventForm!: FormGroup;

  public repeatsSelected = false;

  public showPicker = true;

  public libraryRoot!: TreeNode;

  public currentLanguage = 'en';

  public minDate = new Date();

  public constructor(
    private formBuilder: FormBuilder,
    private interestGroupService: InterestGroupService,
    private loginService: LoginService,
    private eventsService: EventsService,
    private nodesService: NodesService
  ) {}

  public async ngOnInit() {
    this.formReady = false;
    await this.loadData();
    await this.buildForm();
    this.formReady = true;
  }

  public async ngOnChanges() {
    this.formReady = false;
    await this.buildForm();
    this.wizardStep = 1;
    this.formReady = true;
  }

  private async loadData() {
    if (this.ig === undefined) {
      this.ig = await firstValueFrom(
        this.interestGroupService.getInterestGroup(this.igId())
      );
    }
  }

  private async buildForm() {
    if (this.user === undefined) {
      this.user = this.loginService.getUser();
    }

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
        appointmentTypeEvent: [this.appointmentTypeEvent],
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

    if (this.hasLibraryAccess()) {
      await this.buildLibrarySectionTree();
    }
  }

  private async buildLibrarySectionTree() {
    if (this.ig?.libraryId) {
      this.path = await firstValueFrom(
        this.nodesService.getPath(this.ig.libraryId)
      );
      const root = new TreeNode('Library', this.ig.libraryId);
      this.libraryRoot = root;
    }
  }

  public hasLibraryAccess(): boolean {
    if (this.ig === undefined) {
      return false;
    }
    return this.ig.permissions.library !== 'LibNoAccess';
  }

  public canGoBack() {
    return this.wizardStep > 1 && !this.processing;
  }

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

  public closeWizard(_action: string): void {
    this.showModal = false;
    this.modalHide.emit();
  }

  public toggleRepeats(): void {
    this.repeatsSelected = !this.repeatsSelected;
  }

  public toggleEventOrMeeting(): void {
    this.appointmentTypeEvent = !this.appointmentTypeEvent;
  }

  public radioEventOrMeeting(option: boolean): void {
    this.newEventForm.controls.appointmentTypeEvent.setValue(option);
    this.appointmentTypeEvent = option;
  }

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

  public showNoPickerText() {
    this.showPicker = false;
  }

  public setLibrarySectionNode(node: TreeNode) {
    if (node !== undefined) {
      this.newEventForm.controls.meetingLibrarySection.setValue(
        node.nodeId !== this.newEventForm.controls.meetingLibrarySection.value
          ? node.nodeId
          : undefined
      );
    }
  }

  public nextWizardStep(): void {
    if (this.stepIsValid()) {
      this.wizardStep += 1;
    }
  }

  public previousWizardStep(): void {
    this.wizardStep -= 1;
  }

  public async save() {
    try {
      this.processing = true;

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
        eventDefinition.attendantsInfo !== undefined &&
        eventDefinition.attendantsInfo.invitedUsersOrProfiles !== undefined
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
        await firstValueFrom(
          this.eventsService.postEvent(this.igId(), eventDefinition)
        );
      }

      // emit an event to signal that a new event/meeting has been created
      // will be used by the agenda->calendar to redisplay the view
      this.eventMeetingSaved.emit();

      // close form/wizard
      this.closeWizard('close');
    } finally {
      this.processing = false;
    }
  }

  private getMainOccurence(): RepeatsInfo.MainOccurenceEnum {
    if (!this.newEventForm.value.repeatsInfo.repeats) {
      return 'OnlyOnce';
    }
    return this.newEventForm.value.repeatsInfo.whenOrEverySelect ===
      'whenSelect'
      ? 'Times'
      : 'EveryTimes';
  }

  get titleControl(): AbstractControl {
    return this.newEventForm.controls.title;
  }

  get meetingTypeControl(): AbstractControl {
    return this.newEventForm.controls.meetingType;
  }

  get dateInfoControl(): AbstractControl {
    return this.newEventForm.controls.dateInfo;
  }

  get repeatsInfoControl(): AbstractControl {
    return this.newEventForm.controls.repeatsInfo;
  }

  get meetingPublicAvailabilityControl(): AbstractControl {
    return this.newEventForm.controls.meetingPublicAvailability;
  }

  get audienceStatusOpenControl(): AbstractControl {
    return (this.newEventForm.controls.attendantsInfo as FormGroup).controls
      .audienceStatusOpen;
  }

  get meetingLibrarySectionControl(): AbstractControl {
    return this.newEventForm.controls.meetingLibrarySection;
  }

  get invitedExternalEmailsControl(): AbstractControl {
    return (this.newEventForm.controls.attendantsInfo as FormGroup).controls
      .invitedExternalEmails;
  }

  get attendantsInfoControl(): AbstractControl {
    return this.newEventForm.controls.attendantsInfo;
  }

  get contactNameControl(): AbstractControl {
    return this.newEventForm.controls.contactName;
  }

  get contactPhoneControl(): AbstractControl {
    return this.newEventForm.controls.contactPhone;
  }

  get contactEmailControl(): AbstractControl {
    return this.newEventForm.controls.contactEmail;
  }

  get timesControl(): AbstractControl {
    return (this.newEventForm.controls.repeatsInfo as FormGroup).controls.times;
  }

  get everyTimeControl(): AbstractControl {
    return (this.newEventForm.controls.repeatsInfo as FormGroup).controls
      .everyTime;
  }

  otherSelected() {
    return (
      this.newEventForm.controls.meetingType.value !== 'FaceToFace' &&
      this.newEventForm.controls.meetingType.value !== 'VirtualMeeting'
    );
  }
}
