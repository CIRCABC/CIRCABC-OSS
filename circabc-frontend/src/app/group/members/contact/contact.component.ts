import { Component, OnInit } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  Validators,
} from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { TranslocoService } from '@ngneat/transloco';
import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import {
  EmailDefinition,
  EmailService,
  InterestGroup,
  InterestGroupService,
  MailTemplateDefinition,
} from 'app/core/generated/circabc';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { ValidationService } from 'app/core/validation.service';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-contact',
  templateUrl: './contact.component.html',
  styleUrls: ['./contact.component.scss'],
  preserveWhitespaces: true,
})
export class ContactComponent implements OnInit {
  public selectedTab = 'email';
  public emailForm!: FormGroup;
  public membersForm!: FormGroup;
  public ig!: string;
  public currentIg!: InterestGroup;
  public selectedNodes: string[] = [];
  public sending = false;
  public alreadyMember = false;
  public templates!: MailTemplateDefinition[];
  public selectedTemplateId!: string;
  public loaded = false;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private emailService: EmailService,
    private interestGroupService: InterestGroupService,
    private uiMessageService: UiMessageService,
    private translateService: TranslocoService,
    private permEvalService: PermissionEvaluatorService
  ) {}

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
            ValidationService.fileNameValidator(templateNameControl),
        ]);
        this.emailForm.controls.templateName.updateValueAndValidity();
      } else {
        // remove validator
        this.emailForm.controls.templateName.setValidators(null);
        this.emailForm.controls.templateName.updateValueAndValidity();
      }
    });
  }

  public async loadAll(id: string) {
    await this.loadGroup(id);
    await this.loadMailTemplates();
    this.loaded = true;
  }

  public async loadGroup(id: string) {
    this.currentIg = await firstValueFrom(
      this.interestGroupService.getInterestGroup(id)
    );
  }

  public async loadMailTemplates() {
    this.templates = await firstValueFrom(
      this.emailService.getUserMailTemplates()
    );
    this.templates.unshift({ id: '0', name: '', subject: '', text: '' });
    this.selectedTemplateId = '0';
  }

  isEmailTab(): boolean {
    return this.selectedTab === 'email';
  }

  isUserTab(): boolean {
    return this.selectedTab === 'user';
  }

  isFileTab(): boolean {
    return this.selectedTab === 'file';
  }

  setTab(tab: string) {
    this.selectedTab = tab;
  }

  async sendEmail() {
    this.sending = true;

    const body: EmailDefinition = {};
    body.subject = this.emailForm.value.subject;
    body.content = this.emailForm.value.content;
    body.users = [];
    body.profiles = [];
    body.attachments = this.selectedNodes;

    for (const auth of this.membersForm.value.invitedUsersOrProfiles) {
      if (auth.userId && body.users) {
        body.users.push(auth);
      } else if (body.profiles) {
        body.profiles.push(auth);
      }
    }

    await firstValueFrom(
      this.emailService.postGroupEmail(this.currentIg.id as string, body)
    );
    this.cancel();

    this.sending = false;
  }

  cancel() {
    this.emailForm.reset();
    this.emailForm.controls.content.markAsUntouched();
    this.membersForm.reset();
    this.selectedNodes = [];
  }

  isSendable(): boolean {
    return (
      this.getNumberRecipients() > 0 &&
      this.emailForm.value.subject !== '' &&
      this.emailForm.value.content !== '' &&
      !this.sending
    );
  }

  getNumberRecipients(): number {
    if (this.membersForm.value.invitedUsersOrProfiles) {
      return this.membersForm.value.invitedUsersOrProfiles.length;
    } else {
      return 0;
    }
  }

  getNumberAttachments(): number {
    return this.selectedNodes.length;
  }

  get subjectControl(): AbstractControl {
    return this.emailForm.controls.subject;
  }

  get contentControl(): AbstractControl {
    return this.emailForm.controls.content;
  }

  isMember() {
    return this.alreadyMember;
  }

  public isDirAdmin(): boolean {
    return this.permEvalService.isDirAdmin(this.currentIg);
  }

  public isDirManageMembers(): boolean {
    return this.permEvalService.isDirManageMembers(this.currentIg);
  }

  public selectTemplate(value: string) {
    this.selectedTemplateId = value;
    this.updateTemplateFields(value);
  }

  private updateTemplateFields(value: string) {
    const retrievedTemplate = this.templates.find(
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

  public saveTemplateChecked(): boolean {
    return this.emailForm.value.checkSaveTemplate;
  }

  public async saveTemplate() {
    try {
      await firstValueFrom(
        this.emailService.saveUserMailTemplate(
          this.emailForm.value.templateName,
          this.emailForm.value.subject,
          this.emailForm.value.content,
          false
        )
      );

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

  public isRemovableTemplate(): boolean {
    return this.selectedTemplateId !== '0';
  }

  public async removeTemplate() {
    try {
      await firstValueFrom(
        this.emailService.deleteUserMailTemplates([this.selectedTemplateId])
      );
      this.updateTemplateFields('0');

      const res = this.translateService.translate('label.template.removed');
      this.uiMessageService.addSuccessMessage(res, true);
    } catch (error) {
      const res = this.translateService.translate(
        'label.template.removed.error'
      );
      this.uiMessageService.addErrorMessage(res);
    }
    await this.loadMailTemplates();
  }

  get templateNameControl(): AbstractControl {
    return this.emailForm.controls.templateName;
  }
}
