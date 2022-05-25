import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import {
  CategoryService,
  GroupCreationRequest,
  InterestGroupPostModel,
} from 'app/core/generated/circabc';
import { ValidationService } from 'app/core/validation.service';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-group-request-element',
  templateUrl: './group-request-element.component.html',
  styleUrls: ['./group-request-element.component.scss'],
})
export class GroupRequestElementComponent implements OnInit {
  @Input() request!: GroupCreationRequest;
  @Input() categoryId!: string;
  @Output() readonly mustRefresh = new EventEmitter();

  public showMode = 'show';
  public decliningForm!: FormGroup;
  public acceptingForm!: FormGroup;
  public processing = false;

  constructor(
    private fb: FormBuilder,
    private categoryService: CategoryService
  ) {}

  ngOnInit() {
    this.decliningForm = this.fb.group({
      argument: ['', Validators.required],
      agreement: -1,
      id: this.request.id,
    });

    this.acceptingForm = this.fb.group({
      argument: ['', Validators.required],
      agreement: 1,
      id: this.request.id,
      name: ['', [Validators.required, ValidationService.nameValidator]],
      title: '',
      description: '',
    });

    this.initAcceptingForm();
  }

  private initAcceptingForm() {
    if (this.acceptingForm && this.request) {
      this.acceptingForm.patchValue({
        name: this.request.proposedName,
        title:
          this.request.proposedTitle && this.request.proposedTitle.en
            ? this.request.proposedTitle.en
            : '',
        description:
          this.request.proposedDescription &&
          this.request.proposedDescription.en
            ? this.request.proposedDescription.en
            : '',
      });
    }
  }

  public async accept() {
    this.processing = true;
    try {
      if (this.categoryId && this.request.id) {
        if (
          this.acceptingForm.value.name &&
          this.acceptingForm.value.name !== ''
        ) {
          this.request.proposedName = this.acceptingForm.value.name;
        }

        if (
          this.acceptingForm.value.title &&
          this.acceptingForm.value.title !== ''
        ) {
          this.request.proposedTitle = { en: this.acceptingForm.value.title };
        }

        if (
          this.acceptingForm.value.description &&
          this.acceptingForm.value.description !== ''
        ) {
          this.request.proposedDescription = {
            en: this.acceptingForm.value.description,
          };
        }

        await firstValueFrom(
          this.categoryService.editInterestGroupRequest(
            this.categoryId,
            this.request.id,
            this.request
          )
        );

        await firstValueFrom(
          this.categoryService.validateInterestGroupRequests(
            this.categoryId,
            this.request.id,
            this.acceptingForm.value
          )
        );

        const newGroup: InterestGroupPostModel = {
          name: this.acceptingForm.value.name,
          title: { en: this.acceptingForm.value.title },
          description: { en: this.acceptingForm.value.description },
        };
        newGroup.leaders = [];
        newGroup.notify = true;

        if (this.request.leaders) {
          for (const leader of this.request.leaders) {
            if (leader.userId) {
              newGroup.leaders.push(leader.userId);
            }
          }
        }

        await firstValueFrom(
          this.categoryService.postInterestGroup(this.categoryId, newGroup)
        );
      }
      this.mustRefresh.emit();
    } catch (error) {
      console.error(error);
    }
    this.processing = false;
  }

  public async reject() {
    this.processing = true;
    try {
      if (this.categoryId && this.request.id) {
        await firstValueFrom(
          this.categoryService.validateInterestGroupRequests(
            this.categoryId,
            this.request.id,
            this.decliningForm.value
          )
        );
      }
      this.mustRefresh.emit();
    } catch (error) {
      console.error(error);
    }
    this.processing = false;
  }

  public cancelAccept() {
    this.showMode = 'show';
    this.acceptingForm.patchValue({
      argument: '',
      name: this.request.proposedName,
      title:
        this.request.proposedTitle && this.request.proposedTitle.en
          ? this.request.proposedTitle.en
          : '',
      description:
        this.request.proposedDescription && this.request.proposedDescription.en
          ? this.request.proposedDescription.en
          : '',
    });
  }

  public cancelDecline() {
    this.showMode = 'show';
    this.decliningForm.reset({
      argument: '',
    });
  }

  get nameControl() {
    return this.acceptingForm.controls.name;
  }

  get titleControl() {
    return this.acceptingForm.controls.title;
  }

  get descriptionControl() {
    return this.acceptingForm.controls.description;
  }
}
