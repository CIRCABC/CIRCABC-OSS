import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges,
} from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { ActionEmitterResult, ActionResult } from 'app/action-result';
import { ActionType } from 'app/action-result/action-type';
import { MembersService, UserProfile } from 'app/core/generated/circabc';
import { ValidationService } from 'app/core/validation.service';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-edit-expiration',
  templateUrl: './edit-expiration.component.html',
  styleUrls: ['./edit-expiration.component.scss'],
})
export class EditExpirationComponent implements OnInit, OnChanges {
  @Input()
  members!: UserProfile[];
  @Input()
  groupId!: string;
  @Input()
  showModal!: boolean;
  @Output()
  public readonly modalHide = new EventEmitter<ActionEmitterResult>();

  public membersDisplay: UserProfile[] = [];
  public processing = false;
  public expirationForm!: FormGroup;

  constructor(
    private fb: FormBuilder,
    private membersService: MembersService
  ) {}

  ngOnInit() {
    this.expirationForm = this.fb.group({
      expiration: [false],
      expirationDateTime: ['', ValidationService.pastDateTimeValidator],
    });
  }

  public expirationChanged() {
    this.changeExpiration(this.expirationForm.value.expiration);
  }

  public changeExpiration(value: boolean) {
    if (value) {
      this.expirationForm.controls.expirationDateTime.enable();
      this.dateSelected(new Date());
    } else {
      this.expirationForm.controls.expirationDateTime.disable();
      this.dateUnselected();
    }
  }

  ngOnChanges(changes: SimpleChanges) {
    if (
      this.expirationForm !== undefined &&
      !this.expirationForm.value.expiration
    ) {
      this.dateUnselected();
    }
    this.membersDisplay = this.deepArrayCopy(this.members);
    if (changes.members && !changes.members.firstChange) {
      if (
        changes.members.currentValue.length === 1 &&
        changes.members.currentValue[0].expirationDate
      ) {
        this.expirationForm.controls.expirationDateTime.setValue(
          new Date(changes.members.currentValue[0].expirationDate)
        );
        if (changes.members.currentValue[0].expirationDate) {
          this.expirationForm.controls.expiration.setValue(true);
        }
      } else {
        this.expirationForm.controls.expirationDateTime.setValue(new Date());
      }
    }
    if (changes.members) {
      // set the slider to 'Yes' if at least one member has an expiration date
      for (const member of changes.members.currentValue) {
        if (member.expirationDate !== '') {
          this.expirationForm.controls.expiration.setValue(true);
          this.changeExpiration(true);
          break;
        } else {
          this.expirationForm.controls.expiration.setValue(false);
          this.changeExpiration(false);
        }
      }
    }
  }

  public setExpiration() {
    if (this.expirationForm.value.expiration === false) {
      this.expirationForm.controls.expirationDateTime.setValue('');
    }
  }

  public async editExpiration() {
    this.processing = true;
    const res: ActionEmitterResult = {};
    res.type = ActionType.EDIT_EXPIRATION;
    try {
      if (this.expirationForm.value.expiration === false) {
        await this.deleteExpirationDateTimes();
      } else {
        await this.setEpirationDateTimes();
      }
      res.result = ActionResult.SUCCEED;
    } catch (error) {
      res.result = ActionResult.FAILED;
    }
    this.modalHide.emit(res);
    this.processing = false;
  }

  public async deleteExpirationDateTimes() {
    this.members.forEach(async (member) => {
      if (member.expirationDate && member.user && member.user.userId) {
        await firstValueFrom(
          this.membersService.deleteMemberExpiration(
            this.groupId,
            member.user.userId
          )
        );
      }
    });
    this.membersDisplay = this.deepArrayCopy(this.members);
  }

  public async setEpirationDateTimes() {
    const expirationDateTime: string =
      this.expirationForm.value.expirationDateTime.toISOString();
    this.members.forEach(async (member) => {
      if (member.expirationDate) {
        if (member.user && member.user.userId) {
          await firstValueFrom(
            this.membersService.updateMemberExpiration(
              this.groupId,
              member.user.userId,
              expirationDateTime
            )
          );
        }
      } else {
        if (
          member.user &&
          member.user.userId &&
          member.profile &&
          member.profile.id &&
          member.profile.groupName
        ) {
          await firstValueFrom(
            this.membersService.createMemberExpiration(
              this.groupId,
              member.user.userId,
              expirationDateTime,
              member.profile.id,
              member.profile.groupName
            )
          );
        }
      }
    });
    this.membersDisplay = this.deepArrayCopy(this.members);
  }

  public cancel() {
    this.showModal = false;
    const res: ActionEmitterResult = {};
    res.type = ActionType.EDIT_EXPIRATION;
    res.result = ActionResult.CANCELED;
    this.modalHide.emit(res);
  }

  public dateSelected(selectedDate: Date) {
    this.expirationForm.controls.expirationDateTime.setValue(selectedDate);
    this.membersDisplay = this.deepArrayCopy(this.members);
    this.membersDisplay.forEach((member) => {
      if (member.user && member.user.userId) {
        member.expirationDate = selectedDate.toISOString();
      }
    });
  }

  private dateUnselected() {
    this.membersDisplay.forEach((member) => {
      if (member.user && member.user.userId) {
        member.expirationDate = '';
      }
    });
  }

  get minDate(): Date {
    const result: Date = new Date();
    result.setDate(result.getDate() + 1);
    return result;
  }

  private deepArrayCopy(arr: UserProfile[]): UserProfile[] {
    const result: UserProfile[] = [];
    if (!arr) {
      return result;
    }
    const arrayLength = arr.length;
    for (let i = 0; i <= arrayLength; i++) {
      const item = arr[i];
      if (item) {
        result.push({
          user: item.user,
          profile: item.profile,
          expirationDate: item.expirationDate,
        });
      }
    }
    return result;
  }
}
