import { Location } from '@angular/common';
import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  Validators,
} from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import {
  Node as ModelNode,
  NodesService,
  TopicService,
  User,
  UserService,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { ValidationService } from 'app/core/validation.service';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-view-edit-details-topic',
  templateUrl: './view-edit-details-topic.component.html',
  preserveWhitespaces: true,
})
export class ViewEditDetailsTopicComponent implements OnInit {
  @Output()
  public readonly topicUpdated = new EventEmitter();

  public topicId!: string;
  public topic!: ModelNode;
  public creator!: User;
  public modifier!: User;

  // viewing variable has been disabled because of request https://webgate.ec.europa.eu/CITnet/jira/browse/DIGITCIRCABC-3489
  public viewing = false;
  public processing = false;

  public updateTopicForm!: FormGroup;

  constructor(
    private route: ActivatedRoute,
    private nodesService: NodesService,
    private userService: UserService,
    private topicService: TopicService,
    private formBuilder: FormBuilder,
    private location: Location,
    private permEvalService: PermissionEvaluatorService,
    private loginService: LoginService
  ) {}

  async ngOnInit() {
    this.updateTopicForm = this.formBuilder.group(
      {
        title: [
          '',
          [
            Validators.required,
            (control: AbstractControl) =>
              ValidationService.titleValidator(control),
            (control: AbstractControl) =>
              ValidationService.maxLengthTitleValidator(control, 50),
          ],
        ],
        description: [''],
        name: ['', [Validators.required, ValidationService.fileNameValidator]],
        securityRanking: [''],
        expirationDate: ['', ValidationService.pastDateValidator],
      },
      {
        updateOn: 'change',
      }
    );

    this.route.params.subscribe((params) => this.getParams(params));
    await this.fillForm();
  }

  private getParams(params: { [key: string]: string }) {
    this.topicId = params.topicId;
  }

  private async fillForm() {
    this.topic = await firstValueFrom(this.nodesService.getNode(this.topicId));

    // fill form fields
    this.updateTopicForm.controls.title.patchValue(this.topic.title);
    this.updateTopicForm.controls.description.patchValue(
      this.topic.description
    );
    this.updateTopicForm.controls.name.patchValue(this.topic.name);
    if (this.topic.properties !== undefined) {
      this.updateTopicForm.controls.securityRanking.patchValue(
        this.topic.properties.security_ranking
      );
      const expirationDate: string = this.topic.properties.expiration_date;
      if (expirationDate !== undefined && expirationDate.length >= 10) {
        this.updateTopicForm.controls.expirationDate.patchValue(
          this.adaptDateString(expirationDate)
        );
      }
    }

    // get creator and modifier users to retrieve their names for display
    if (this.topic.properties) {
      this.creator = await firstValueFrom(
        this.userService.getUser(this.topic.properties.creator)
      );
      this.modifier = await firstValueFrom(
        this.userService.getUser(this.topic.properties.modifier)
      );
    }
  }

  private cutDate(dateString: string) {
    return dateString !== undefined ? dateString.substring(0, 10) : '';
  }

  private adaptDateString(dateString: string) {
    if (dateString === undefined || dateString.length === 0) {
      return '';
    }
    const year = dateString.substring(0, 4);
    const month = dateString.substring(5, 7);
    const day = dateString.substring(8, 10);
    return `${day}/${month}/${year}`;
  }

  public goBack() {
    this.location.back();
  }

  public enableEdit() {
    // enable all form controls for edit
    Object.keys(this.updateTopicForm.controls).forEach((key) => {
      (this.updateTopicForm.get(key) as AbstractControl).enable();
    });

    this.viewing = false;
  }

  public clearExpirationDate() {
    this.updateTopicForm.controls.expirationDate.patchValue('');
  }

  public async cancel() {
    await this.fillForm();
    this.goBack();
  }

  public async update() {
    try {
      this.processing = true;

      if (this.topic !== undefined && this.topic.properties !== undefined) {
        this.topic.title = this.updateTopicForm.controls.title.value;
        this.topic.description =
          this.updateTopicForm.controls.description.value;
        this.topic.name = this.updateTopicForm.controls.name.value;
        this.topic.properties.security_ranking =
          this.updateTopicForm.controls.securityRanking.value;
        this.topic.properties.expiration_date =
          this.updateTopicForm.controls.expirationDate.value;

        await firstValueFrom(
          this.topicService.putTopic(this.topic.id as string, this.topic)
        );

        // emit an event to signal that a topic has been updated
        // will be used to redisplay the view
        this.topicUpdated.emit();

        this.goBack();
      } else {
        throw new Error('"topic" is undefined.');
      }
    } finally {
      this.processing = false;
    }
  }

  public isTopicAdmin(): boolean {
    return (
      this.permEvalService.isNewsgroupAdmin(this.topic) ||
      this.permEvalService.isOwner(
        this.topic,
        this.loginService.getCurrentUsername()
      )
    );
  }

  get expirationDateControl(): AbstractControl {
    return this.updateTopicForm.controls.expirationDate;
  }
  get nameControl(): AbstractControl {
    return this.updateTopicForm.controls.name;
  }

  get versionLabel(): string {
    if (this.topic.properties) {
      return this.topic.properties.versionLabel;
    } else {
      return '';
    }
  }

  get created(): string {
    if (this.topic.properties) {
      return this.cutDate(this.topic.properties.created);
    } else {
      return '';
    }
  }

  get modified(): string {
    if (this.topic.properties) {
      return this.cutDate(this.topic.properties.modified);
    } else {
      return '';
    }
  }

  get ismoderated(): string {
    if (this.topic.properties) {
      return this.cutDate(this.topic.properties.ismoderated);
    } else {
      return 'false';
    }
  }
}
