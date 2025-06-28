import { DatePipe } from '@angular/common';
import {
  Component,
  Input,
  OnChanges,
  OnInit,
  SimpleChanges,
  OnDestroy,
  input,
  output,
  inject,
} from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { TranslocoModule } from '@jsverse/transloco';
import { ActionEmitterResult, ActionResult } from 'app/action-result';
import { ActionType } from 'app/action-result/action-type';
import { MembersService, UserProfile } from 'app/core/generated/circabc';
import { setupCalendarDateHandling } from 'app/core/util/date-calendar-util';
import { ModalComponent } from 'app/shared/modal/modal.component';
import { DatePicker } from 'primeng/datepicker';
import { firstValueFrom, Subscription } from 'rxjs';

@Component({
  selector: 'cbc-edit-expiration',
  templateUrl: './edit-expiration.component.html',
  styleUrl: './edit-expiration.component.scss',
  imports: [
    ModalComponent,
    ReactiveFormsModule,
    DatePicker,
    DatePipe,
    TranslocoModule,
  ],
})
export class EditExpirationComponent implements OnInit, OnChanges, OnDestroy {
  private fb = inject(FormBuilder);
  private membersService = inject(MembersService);

  @Input() showModal!: boolean;
  readonly members = input.required<UserProfile[]>();
  readonly groupId = input.required<string>();
  public readonly modalHide = output<ActionEmitterResult>();
  public membersDisplay: UserProfile[] = [];
  public processing = false;
  public expirationForm!: FormGroup;
  private dateSubscription!: Subscription;
  public minDate: Date = new Date(new Date().setHours(23, 59, 0, 0));

  ngOnInit() {
    this.expirationForm = this.fb.group({
      expiration: [false],
      expirationDateTime: [''],
      showOkButton: [false],
    });

    this.dateSubscription = setupCalendarDateHandling(
      this.expirationForm.controls.expirationDateTime
    );

    this.expirationForm.controls.expiration.valueChanges.subscribe((value) => {
      this.toggleExpiration(value);
      this.updateShowOkButton();
    });

    this.expirationForm.controls.expirationDateTime.valueChanges.subscribe(
      () => {
        this.updateShowOkButton();
      }
    );
  }

  ngOnDestroy() {
    this.dateSubscription?.unsubscribe();
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes.members) {
      this.updateMembersDisplay(changes.members.currentValue);
    }
    if (this.expirationForm) {
      this.updateShowOkButton();
    }
  }

  private updateMembersDisplay(members: UserProfile[]) {
    if (this.expirationForm) {
      this.membersDisplay = this.deepArrayCopy(members);

      const hasExpiration = members.some(
        (member) => member.expirationDate && member.expirationDate !== ''
      );

      this.expirationForm.controls.expiration.setValue(hasExpiration);

      if (members.length === 1 && members[0].expirationDate) {
        this.expirationForm.controls.expirationDateTime.setValue(
          new Date(members[0].expirationDate)
        );
        this.expirationForm.controls.expirationDateTime.enable();
      } else {
        this.expirationForm.controls.expirationDateTime.reset();
      }
    }
  }

  private toggleExpiration(value: boolean) {
    const expirationDateTimeControl =
      this.expirationForm.controls.expirationDateTime;

    if (value) {
      expirationDateTimeControl.enable();
      expirationDateTimeControl.setValue(this.minDate);
    } else {
      expirationDateTimeControl.disable();
      expirationDateTimeControl.setValue(null);
    }
  }

  private updateShowOkButton() {
    const expiration = this.expirationForm.controls.expiration.value;
    const expirationDateTime =
      this.expirationForm.controls.expirationDateTime.value;

    const showOkButtonValue = expiration
      ? expirationDateTime && expirationDateTime !== ''
      : true;
    this.expirationForm.controls.showOkButton.setValue(showOkButtonValue);
  }

  public async editExpiration() {
    this.processing = true;
    const res: ActionEmitterResult = { type: ActionType.EDIT_EXPIRATION };

    try {
      if (this.expirationForm.value.expiration) {
        await this.setExpirationDateTimes();
      } else {
        await this.deleteExpirationDateTimes();
      }
      res.result = ActionResult.SUCCEED;
    } catch {
      res.result = ActionResult.FAILED;
    }

    this.modalHide.emit(res);
    this.processing = false;
  }

  private async deleteExpirationDateTimes() {
    const deletePromises = this.membersDisplay
      .filter((member) => member.expirationDate && member.user?.userId)
      .map((member) => {
        if (member?.user?.userId) {
          firstValueFrom(
            this.membersService.deleteMemberExpiration(
              this.groupId(),
              member.user.userId
            )
          );
        }
      });

    await Promise.all(deletePromises);
  }

  private async setExpirationDateTimes(): Promise<void> {
    const expirationDateTime: string =
      this.expirationForm.value.expirationDateTime.toISOString();

    const updatePromises = this.membersDisplay.map((member) => {
      if (member.user?.userId) {
        return member.expirationDate
          ? this.updateMemberExpiration(member.user.userId, expirationDateTime)
          : this.createMemberExpiration(member, expirationDateTime);
      }
      return Promise.resolve();
    });

    try {
      await Promise.all(updatePromises);
    } catch (error) {
      console.error('Error setting expiration dates:', error);
      throw error;
    }
  }

  private updateMemberExpiration(userId: string, expirationDateTime: string) {
    return firstValueFrom(
      this.membersService.updateMemberExpiration(
        this.groupId(),
        userId,
        expirationDateTime
      )
    );
  }

  private createMemberExpiration(
    member: UserProfile,
    expirationDateTime: string
  ) {
    if (member.profile?.id && member.profile.groupName && member.user?.userId) {
      return firstValueFrom(
        this.membersService.createMemberExpiration(
          this.groupId(),
          member.user?.userId,
          expirationDateTime,
          member.profile.id,
          member.profile.groupName
        )
      );
    }
    return Promise.resolve();
  }

  public cancel() {
    this.showModal = false;
    this.modalHide.emit({
      type: ActionType.EDIT_EXPIRATION,
      result: ActionResult.CANCELED,
    });
  }

  private deepArrayCopy(arr: UserProfile[] = []): UserProfile[] {
    return arr.map((item) => ({
      user: item.user,
      profile: item.profile,
      expirationDate: item.expirationDate,
    }));
  }
}
