import {
  ChangeDetectionStrategy,
  Component,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import {
  EmailDefinition,
  EmailService,
  InterestGroup,
  InterestGroupService,
  MailTemplateDefinition,
} from 'app/core/generated/circabc';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { fileNameValidator } from 'app/core/validation.service';
import { ControlMessageComponent } from 'app/shared/control-message/control-message.component';
import { FilePickerComponent } from 'app/shared/file-picker/file-picker.component';
import { ReponsiveSubMenuComponent } from 'app/shared/reponsive-sub-menu/reponsive-sub-menu.component';
import { RichTextEditorComponent } from 'app/shared/rich-text-editor/rich-text-editor.component';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';
import { UsersPickerComponent } from 'app/shared/users/users-picker.component';

/**
 * Interest group "Contact" page component.
 *
 * Renders a tabbed contact interface (`cbc-contact`) that lets group
 * administrators and members compose and send an email to selected group
 * members and/or access profiles. The view is organised into tabs:
 * - an email tab (subject / rich-text content, driven by {@link emailForm});
 * - a recipients (user) tab backed by {@link membersForm};
 * - a file/attachment tab backed by {@link selectedNodes}.
 *
 * It also manages the user's reusable mail templates: loading them, applying
 * a selected template to the form, saving the current message as a new
 * template, and removing an existing template.
 *
 * Key collaborators: {@link EmailService} (sending emails and CRUD on mail
 * templates), {@link InterestGroupService} (resolving the current interest
 * group), {@link PermissionEvaluatorService} (permission checks),
 * {@link UiMessageService} (success/error notifications) and
 * {@link TranslocoService} (i18n of user-facing messages).
 */
@Component({
  selector: 'cbc-contact',
  templateUrl: './contact.component.html',
  styleUrl: './contact.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ReponsiveSubMenuComponent,
    RouterLink,
    ReactiveFormsModule,
    ControlMessageComponent,
    RichTextEditorComponent,
    SpinnerComponent,
    UsersPickerComponent,
    FilePickerComponent,
    TranslocoModule,
  ],
})
export class ContactComponent implements OnInit {
  /** FormBuilder used to construct the email and members reactive forms. */
  private readonly fb = inject(FormBuilder);
  /** Activated route, used to read the interest group id route parameter. */
  private readonly route = inject(ActivatedRoute);
  /** API service for sending group emails and managing mail templates. */
  private readonly emailService = inject(EmailService);
  /** API service used to load the current interest group. */
  private readonly interestGroupService = inject(InterestGroupService);
  /** Service for displaying success and error notifications to the user. */
  private readonly uiMessageService = inject(UiMessageService);
  /** Transloco service used to translate user-facing messages. */
  private readonly translateService = inject(TranslocoService);
  /** Service used to evaluate the current user's permissions on the group. */
  private readonly permEvalService = inject(PermissionEvaluatorService);

  /** Currently active tab identifier (`'email'`, `'user'` or `'file'`). */
  public selectedTab = 'email';
  /** Reactive form holding the email subject, content and template fields. */
  public emailForm!: FormGroup;
  /** Reactive form holding the selected recipients (users and/or profiles). */
  public membersForm!: FormGroup;
  /** Interest group identifier. */
  public ig!: string;
  /** The interest group currently being contacted. */
  public readonly currentIg = signal<InterestGroup | undefined>(undefined);
  /** Node ids of the files selected as email attachments. */
  public readonly selectedNodes = signal<string[]>([]);
  /** Whether an email send request is currently in progress. */
  public readonly sending = signal(false);
  /** Whether the current user is already a member of the group. */
  public alreadyMember = false;
  /** Mail templates available to the current user. */
  public readonly templates = signal<MailTemplateDefinition[]>([]);
  /** Id of the currently selected mail template (`'0'` means none). */
  public readonly selectedTemplateId = signal('0');
  /** Whether the group and templates have finished loading. */
  public readonly loaded = signal(false);

  /**
   * Angular lifecycle hook. Subscribes to the route params to load the
   * interest group and mail templates, and initialises the {@link emailForm}
   * and {@link membersForm} reactive forms with their validators.
   */
  ngOnInit() {
    this.route.params.subscribe(
      async (params) => await this.loadAll(params.id)
    );

    this.emailForm = this.fb.group(
      {
        subject: ['', Validators.required],
        content: ['', Validators.required],
        templateName: [''],
        checkSaveTemplate: [false],
      },
      {
        updateOn: 'change',
      }
    );

    this.subscribeToCheckSaveTemplateChanges();

    this.membersForm = this.fb.group(
      {
        invitedUsersOrProfiles: [[]],
      },
      {
        updateOn: 'change',
      }
    );
  }

  // conditional validation: subscribe to the checkSaveTemplate filed changes
  // to add the necessary validator to templateName dynamically only when
  // the checkbox is ticked (checkSaveTemplate === true), and remove it if not
  /**
   * Subscribes to changes of the `checkSaveTemplate` checkbox control to
   * dynamically manage validation of the `templateName` control. When the
   * checkbox is ticked, a required validator and a
   * {@link fileNameValidator} are applied; when unticked, validators are
   * removed. The control's validity is re-evaluated after each change.
   */
  subscribeToCheckSaveTemplateChanges() {
    const checkSaveTemplateChanges =
      this.emailForm.controls.checkSaveTemplate.valueChanges;

    // subscribe to changes in order to update the validator when a change happens
    checkSaveTemplateChanges.subscribe((checkSaveTemplate) => {
      if (checkSaveTemplate) {
        // add validator
        this.emailForm.controls.templateName.setValidators([
          Validators.required,
          (templateNameControl: AbstractControl) =>
            fileNameValidator(templateNameControl),
        ]);
        this.emailForm.controls.templateName.updateValueAndValidity();
      } else {
        // remove validator
        this.emailForm.controls.templateName.setValidators(null);
        this.emailForm.controls.templateName.updateValueAndValidity();
      }
    });
  }

  /**
   * Loads all data required by the view: the interest group and the user's
   * mail templates, then flags the component as {@link loaded}.
   *
   * @param id - The identifier of the interest group to contact.
   * @returns A promise that resolves once the group and templates are loaded.
   */
  public async loadAll(id: string) {
    await this.loadGroup(id);
    await this.loadMailTemplates();
    this.loaded.set(true);
  }

  /**
   * Loads the interest group with the given id into {@link currentIg}.
   *
   * @param id - The identifier of the interest group to load.
   * @returns A promise that resolves once the group has been loaded.
   */
  public async loadGroup(id: string) {
    this.currentIg.set(
      await this.interestGroupService.getInterestGroupAsync({ id })
    );
  }

  /**
   * Loads the current user's mail templates into {@link templates}, prepends
   * an empty placeholder template (id `'0'`) representing "no template" and
   * selects it as the current {@link selectedTemplateId}.
   *
   * @returns A promise that resolves once the templates have been loaded.
   */
  public async loadMailTemplates() {
    const templates = await this.emailService.getUserMailTemplatesAsync();
    this.templates.set([
      { id: '0', name: '', subject: '', text: '' },
      ...templates,
    ]);
    this.selectedTemplateId.set('0');
  }

  /**
   * @returns `true` when the email tab is currently selected.
   */
  isEmailTab(): boolean {
    return this.selectedTab === 'email';
  }

  /**
   * @returns `true` when the recipients (user) tab is currently selected.
   */
  isUserTab(): boolean {
    return this.selectedTab === 'user';
  }

  /**
   * @returns `true` when the file (attachments) tab is currently selected.
   */
  isFileTab(): boolean {
    return this.selectedTab === 'file';
  }

  /**
   * Sets the currently active tab.
   *
   * @param tab - The tab identifier to activate (`'email'`, `'user'` or
   * `'file'`).
   */
  setTab(tab: string) {
    this.selectedTab = tab;
  }

  /**
   * Builds an {@link EmailDefinition} from the current form values, splitting
   * the selected recipients into users and profiles, attaching the selected
   * nodes, sends the email for the current group and resets the forms on
   * completion. Toggles {@link sending} around the request.
   *
   * @returns A promise that resolves once the email has been sent and the
   * forms have been reset.
   */
  async sendEmail() {
    this.sending.set(true);

    const body: EmailDefinition = {};
    body.subject = this.emailForm.value.subject;
    body.content = this.emailForm.value.content;
    body.users = [];
    body.profiles = [];
    body.attachments = this.selectedNodes();

    for (const auth of this.membersForm.value.invitedUsersOrProfiles) {
      if (auth.userId && body.users) {
        body.users.push(auth);
      } else if (body.profiles) {
        body.profiles.push(auth);
      }
    }

    await this.emailService.postGroupEmailAsync({
      id: this.currentIg()?.id as string,
      emailDefinition: body,
    });
    this.cancel();

    this.sending.set(false);
  }

  /**
   * Resets the email and members forms, clears the touched state of the
   * content control and empties the selected attachment nodes.
   */
  cancel() {
    this.emailForm.reset();
    this.emailForm.controls.content.markAsUntouched();
    this.membersForm.reset();
    this.selectedNodes.set([]);
  }

  /**
   * Determines whether the email can be sent.
   *
   * @returns `true` when there is at least one recipient, a non-empty subject
   * and content, and no send is currently in progress.
   */
  isSendable(): boolean {
    return (
      this.getNumberRecipients() > 0 &&
      this.emailForm.value.subject !== '' &&
      this.emailForm.value.content !== '' &&
      !this.sending()
    );
  }

  /**
   * @returns The number of selected recipients (users and/or profiles).
   */
  getNumberRecipients(): number {
    if (this.membersForm.value.invitedUsersOrProfiles) {
      return this.membersForm.value.invitedUsersOrProfiles.length;
    }
    return 0;
  }

  /**
   * @returns The number of selected file attachments.
   */
  getNumberAttachments(): number {
    return this.selectedNodes().length;
  }

  /** @returns The reactive control backing the email subject field. */
  get subjectControl(): AbstractControl {
    return this.emailForm.controls.subject;
  }

  /** @returns The reactive control backing the email content field. */
  get contentControl(): AbstractControl {
    return this.emailForm.controls.content;
  }

  /**
   * @returns `true` when the current user is already a member of the group.
   */
  isMember() {
    return this.alreadyMember;
  }

  /**
   * @returns `true` when the current user is a directory administrator of the
   * current interest group.
   */
  public isDirAdmin(): boolean {
    const group = this.currentIg();
    return group ? this.permEvalService.isDirAdmin(group) : false;
  }

  /**
   * @returns `true` when the current user is allowed to manage members of the
   * current interest group.
   */
  public isDirManageMembers(): boolean {
    const group = this.currentIg();
    return group ? this.permEvalService.isDirManageMembers(group) : false;
  }

  /**
   * Selects the given mail template and applies its fields to the email form.
   *
   * @param value - The id of the mail template to select.
   */
  public selectTemplate(value: string) {
    this.selectedTemplateId.set(value);
    this.updateTemplateFields(value);
  }

  /**
   * Patches the email form with the subject and content of the template
   * matching the given id, resetting the template name and save flag. Does
   * nothing when no template matches.
   *
   * @param value - The id of the template whose fields should populate the
   * form.
   */
  private updateTemplateFields(value: string) {
    const retrievedTemplate = this.templates().find(
      (template: MailTemplateDefinition) => template.id === value
    );
    if (retrievedTemplate !== undefined) {
      this.emailForm.patchValue({
        subject: retrievedTemplate.subject,
        content: retrievedTemplate.text,
        templateName: '',
        checkSaveTemplate: false,
      });
    }
  }

  /**
   * Determines whether the current message is eligible to be saved as a
   * template.
   *
   * @returns `true` when the save-template checkbox is ticked and the
   * template name, subject and content are all present, non-empty and the
   * form is valid.
   */
  public isSaveTemplate(): boolean {
    return (
      this.emailForm.value.checkSaveTemplate &&
      this.emailForm.value.templateName !== undefined &&
      this.emailForm.value.subject !== undefined &&
      this.emailForm.value.content !== undefined &&
      this.emailForm.value.templateName.length > 0 &&
      this.emailForm.value.subject.length > 0 &&
      this.emailForm.value.content !== null &&
      this.emailForm.value.content.length > 0 &&
      this.emailForm.valid
    );
  }

  /**
   * @returns `true` when the save-template checkbox is currently ticked.
   */
  public saveTemplateChecked(): boolean {
    return this.emailForm.value.checkSaveTemplate;
  }

  /**
   * Saves the current subject and content as a user mail template using the
   * entered template name, displaying a success or error notification and
   * reloading the templates afterwards. A "Duplicate child" backend error is
   * translated into a dedicated "template exists" message.
   *
   * @returns A promise that resolves once the save attempt and template
   * reload have completed.
   */
  public async saveTemplate() {
    try {
      await this.emailService.saveUserMailTemplateAsync({
        templateName: this.emailForm.value.templateName,
        templateSubject: this.emailForm.value.subject,
        templateText: this.emailForm.value.content,
        overwrite: false,
      });

      const res = this.translateService.translate('label.template.saved');
      this.uiMessageService.addSuccessMessage(res, true);
    } catch (error) {
      let errMessage = 'label.template.error';
      if (error.error.message.substring(0, 15) === 'Duplicate child') {
        errMessage = 'label.template.exists';
      }
      const res = this.translateService.translate(errMessage);
      this.uiMessageService.addErrorMessage(res);
    }
    await this.loadMailTemplates();
  }

  /**
   * @returns `true` when the currently selected template is a real, removable
   * template (i.e. not the empty placeholder with id `'0'`).
   */
  public isRemovableTemplate(): boolean {
    return this.selectedTemplateId() !== '0';
  }

  /**
   * Deletes the currently selected mail template, resets the form fields to
   * the placeholder template, displays a success or error notification and
   * reloads the templates afterwards.
   *
   * @returns A promise that resolves once the delete attempt and template
   * reload have completed.
   */
  public async removeTemplate() {
    try {
      await this.emailService.deleteUserMailTemplatesAsync({
        templateIds: [this.selectedTemplateId()],
      });
      this.updateTemplateFields('0');

      const res = this.translateService.translate('label.template.removed');
      this.uiMessageService.addSuccessMessage(res, true);
    } catch (error) {
      console.error(error);
      const res = this.translateService.translate(
        'label.template.removed.error'
      );
      this.uiMessageService.addErrorMessage(res);
    }
    await this.loadMailTemplates();
  }

  /** @returns The reactive control backing the template name field. */
  get templateNameControl(): AbstractControl {
    return this.emailForm.controls.templateName;
  }
}
