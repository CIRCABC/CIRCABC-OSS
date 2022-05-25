import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  Validators,
} from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result/index';
import {
  CategoryService,
  InterestGroupPostModel,
  User,
  UserService,
} from 'app/core/generated/circabc';
import { ValidationService } from 'app/core/validation.service';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-create-group',
  templateUrl: './create-group.component.html',
  styleUrls: ['./create-group.component.scss'],
  preserveWhitespaces: true,
})
export class CreateGroupComponent implements OnInit {
  @Input()
  showModal = false;
  @Output()
  readonly modalClosed = new EventEmitter<ActionEmitterResult>();

  public showDetailsForm = true;
  public showLeadersForm = false;
  public showNotificationForm = false;
  public searchingUsers = false;
  public processing = false;

  public groupDetailsForm!: FormGroup;
  public groupLeadersForm!: FormGroup;
  public groupNotificationForm!: FormGroup;

  public categoryId!: string;

  public availableUsers: User[] = [];
  public futureMembers: User[] = [];

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private userService: UserService,
    private categoryService: CategoryService
  ) {}

  ngOnInit() {
    this.route.params.subscribe((params) => (this.categoryId = params.id));

    this.groupDetailsForm = this.fb.group(
      {
        name: ['', [Validators.required, ValidationService.fileNameValidator]],
        title: [{}, [Validators.required, ValidationService.titleValidator]],
        description: [{}, Validators.required],
        contact: [{}, Validators.required],
      },
      {
        updateOn: 'change',
      }
    );
    this.groupLeadersForm = this.fb.group(
      {
        search: [],
        possibleUsers: [],
      },
      {
        updateOn: 'change',
      }
    );
    this.groupNotificationForm = this.fb.group(
      {
        notify: [false],
        notificationText: [{}, Validators.required],
      },
      {
        updateOn: 'change',
      }
    );
  }

  async createGroup() {
    this.processing = true;
    const result: ActionEmitterResult = {};
    result.type = ActionType.CREATE_INTEREST_GROUP;

    if (this.categoryId) {
      try {
        const newGroup: InterestGroupPostModel = {
          ...this.groupDetailsForm.value,
        };
        newGroup.leaders = [];
        newGroup.notify = this.groupNotificationForm.value.notify;
        newGroup.notifyText = this.groupNotificationForm.value.notificationText;

        for (const leader of this.futureMembers) {
          if (leader.userId) {
            newGroup.leaders.push(leader.userId);
          }
        }
        const group = await firstValueFrom(
          this.categoryService.postInterestGroup(this.categoryId, newGroup)
        );
        result.type = ActionType.CREATE_INTEREST_GROUP;
        result.result = ActionResult.SUCCEED;
        result.node = { id: group.id };

        this.reset();
      } catch (error) {
        if (error.error.message.includes(' already exists.')) {
          result.type = ActionType.CREATE_INTEREST_GROUP_EXISTS;
        }
        result.result = ActionResult.FAILED;
      }
    }
    this.modalClosed.emit(result);
    this.processing = false;
  }

  resetSearchForm() {
    this.groupLeadersForm.reset();
  }

  cancel() {
    this.showModal = false;

    const res: ActionEmitterResult = {};
    res.result = ActionResult.CANCELED;
    res.type = ActionType.CREATE_INTEREST_GROUP;

    this.reset();

    this.modalClosed.emit(res);
  }

  setDetailsForm() {
    this.showDetailsForm = true;
    this.showLeadersForm = false;
    this.showNotificationForm = false;
  }

  setLeadersForm() {
    this.showDetailsForm = false;
    this.showLeadersForm = true;
    this.showNotificationForm = false;
  }

  setNotificationForm() {
    this.showDetailsForm = false;
    this.showLeadersForm = false;
    this.showNotificationForm = true;
  }

  async searchUsers() {
    if (!this.searchExpressionEmpty()) {
      this.searchingUsers = true;
      this.availableUsers = await firstValueFrom(
        this.userService.getUsers(this.groupLeadersForm.controls.search.value)
      );
      this.searchingUsers = false;
    }
  }

  public searchExpressionEmpty(): boolean {
    return (
      this.groupLeadersForm.controls.search.value === '' ||
      this.groupLeadersForm.controls.search.value === null
    );
  }

  public resetForm(): void {
    this.availableUsers = [];
    this.groupLeadersForm.controls.search.setValue('');
    this.groupLeadersForm.controls.possibleUsers.setValue('');
  }

  private reset() {
    this.setDetailsForm();

    if (this.groupDetailsForm !== undefined) {
      this.groupDetailsForm.reset({
        name: '',
        title: '',
        description: '',
        contact: '',
      });
      this.groupDetailsForm.controls.title.markAsPristine();
    }
    if (this.groupLeadersForm !== undefined) {
      this.groupLeadersForm.reset({
        search: '',
        possibleUsers: '',
      });
    }
    if (this.groupNotificationForm !== undefined) {
      this.groupNotificationForm.reset({
        notify: '',
      });
    }

    this.availableUsers = [];
    this.futureMembers = [];
  }

  public selectUsers(): void {
    const membersTmp: User[] = [];
    if (
      this.groupLeadersForm.controls.possibleUsers.value !== null &&
      this.groupLeadersForm.controls.possibleUsers.value !== undefined &&
      this.groupLeadersForm.controls.possibleUsers.value !== ''
    ) {
      this.groupLeadersForm.controls.possibleUsers.value.filter(
        (userid: string) => {
          const memberTmp = this.availableUsers.find((user) => {
            return user.userId === userid;
          });
          if (memberTmp) {
            membersTmp.push(memberTmp);
          }
        }
      );

      this.futureMembers = this.futureMembers.concat(
        membersTmp.filter((memberTmp) => {
          return (
            this.futureMembers.find((member) => {
              if (member && memberTmp) {
                return member.userId === memberTmp.userId;
              } else {
                return true;
              }
            }) === undefined
          );
        })
      );
    }
  }

  public removeFromFutureMember(m: User): void {
    const index: number = this.futureMembers.indexOf(m, 0);
    this.futureMembers.splice(index, 1);
  }

  get nameControl(): AbstractControl {
    return this.groupDetailsForm.controls.name;
  }

  get titleControl(): AbstractControl {
    return this.groupDetailsForm.controls.title;
  }

  public areFormsValid(): boolean {
    if (this.showDetailsForm || this.showLeadersForm) {
      return true;
    } else {
      return this.futureMembers.length > 0 && this.groupDetailsForm.valid;
    }
  }

  public getOkLabel() {
    if (this.showDetailsForm || this.showLeadersForm) {
      return 'label.next';
    } else {
      return 'label.create';
    }
  }

  public async okAction() {
    if (this.showDetailsForm) {
      this.setLeadersForm();
    } else if (this.showLeadersForm) {
      this.setNotificationForm();
    } else {
      await this.createGroup();
    }
  }
}
