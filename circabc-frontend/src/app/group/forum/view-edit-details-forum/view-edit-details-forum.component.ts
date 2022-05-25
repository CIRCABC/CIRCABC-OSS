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
  ForumService,
  Node as ModelNode,
  NodesService,
  User,
  UserService,
} from 'app/core/generated/circabc';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { ValidationService } from 'app/core/validation.service';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-view-edit-details-forum',
  templateUrl: './view-edit-details-forum.component.html',
  preserveWhitespaces: true,
})
export class ViewEditDetailsForumComponent implements OnInit {
  @Output()
  public readonly forumUpdated = new EventEmitter();

  public forumId!: string;
  public forum!: ModelNode;
  public creator!: User;
  public modifier!: User;

  // viewing variable has been disabled because of request https://webgate.ec.europa.eu/CITnet/jira/browse/DIGITCIRCABC-3489
  public viewing = false;
  public processing = false;

  public updateForumForm!: FormGroup;

  constructor(
    private route: ActivatedRoute,
    private nodesService: NodesService,
    private userService: UserService,
    private forumService: ForumService,
    private formBuilder: FormBuilder,
    private location: Location,
    private uiMessageService: UiMessageService,
    private permEvalService: PermissionEvaluatorService
  ) {}

  async ngOnInit() {
    this.updateForumForm = this.formBuilder.group(
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
      },
      {
        updateOn: 'change',
      }
    );

    this.route.params.subscribe((params) => this.getParams(params));
    await this.fillForm();
  }

  private getParams(params: { [key: string]: string }) {
    this.forumId = params.forumId;
  }

  private async fillForm() {
    try {
      this.forum = await firstValueFrom(
        this.nodesService.getNode(this.forumId)
      );

      // fill form fields
      this.updateForumForm.controls.title.patchValue(this.forum.title);
      this.updateForumForm.controls.description.patchValue(
        this.forum.description
      );
      this.updateForumForm.controls.name.patchValue(this.forum.name);

      // get creator and modifier users to retrieve their names for display
      if (this.forum.properties) {
        this.creator = await firstValueFrom(
          this.userService.getUser(this.forum.properties.creator)
        );
        this.modifier = await firstValueFrom(
          this.userService.getUser(this.forum.properties.modifier)
        );
      }
    } catch (error) {
      const jsonError = JSON.parse(error._body);
      if (jsonError) {
        this.uiMessageService.addErrorMessage(jsonError.message);
      }
    }
  }

  public cutDate(dateString: string) {
    return dateString !== undefined ? dateString.substring(0, 10) : '';
  }

  public goBack() {
    this.location.back();
  }

  public enableEdit() {
    // enable all form controls for edit
    Object.keys(this.updateForumForm.controls).forEach((key) => {
      (this.updateForumForm.get(key) as AbstractControl).enable();
    });

    this.viewing = false;
  }

  public async cancel() {
    await this.fillForm();
    this.goBack();
  }

  public async update() {
    try {
      this.processing = true;

      if (this.forum !== undefined) {
        this.forum.title = this.updateForumForm.controls.title.value;
        this.forum.description =
          this.updateForumForm.controls.description.value;
        this.forum.name = this.updateForumForm.controls.name.value;

        await firstValueFrom(
          this.forumService.putForum(this.forum.id as string, this.forum)
        );

        // emit an event to signal that a forum has been updated
        // will be used to redisplay the view
        this.forumUpdated.emit();
        this.goBack();
      } else {
        throw new Error('"forum" is undefined.');
      }
    } finally {
      this.processing = false;
    }
  }

  public isForumAdmin(): boolean {
    return this.permEvalService.isNewsgroupAdmin(this.forum);
  }

  get nameControl(): AbstractControl {
    return this.updateForumForm.controls.name;
  }

  get versionLabel(): string {
    if (this.forum.properties) {
      return this.forum.properties.versionLabel;
    } else {
      return '';
    }
  }

  get created(): string {
    if (this.forum.properties) {
      return this.cutDate(this.forum.properties.created);
    } else {
      return '';
    }
  }

  get modified(): string {
    if (this.forum.properties) {
      return this.cutDate(this.forum.properties.modified);
    } else {
      return '';
    }
  }

  get ismoderated(): string {
    if (this.forum.properties) {
      return this.forum.properties.ismoderated;
    } else {
      return 'false';
    }
  }
}
