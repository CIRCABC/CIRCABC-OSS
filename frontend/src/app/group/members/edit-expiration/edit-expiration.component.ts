import { DatePipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  OnChanges,
  OnDestroy,
  OnInit,
  output,
  SimpleChanges,
  signal,
} from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { TranslocoModule } from '@jsverse/transloco';
import { ActionEmitterResult, ActionResult } from 'app/action-result';
import { ActionType } from 'app/action-result/action-type';
import { MembersService, UserProfile } from 'app/core/generated/circabc';
import { setupCalendarDateHandling } from 'app/core/util/date-calendar-util';
import { ModalComponent } from 'app/shared/modal/modal.component';
import { Subscription } from 'rxjs';

/**
 * Modal component (`cbc-edit-expiration`) that lets an administrator set,
 * update or remove the membership expiration date/time for one or more group
 * members.
 *
 * It renders a modal dialog containing a reactive form with a toggle to enable
 * expiration and a Material date/time picker. Depending on the current
 * selection it creates, updates or deletes expiration dates for the selected
 * members through the {@link MembersService}, then emits the outcome via the
 * {@link modalHide} output so the parent can react and close the dialog.
 */
@Component({
  selector: 'cbc-edit-expiration',
  templateUrl: './edit-expiration.component.html',
  styleUrl: './edit-expiration.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ModalComponent,
    ReactiveFormsModule,
    MatDatepickerModule,
    MatInputModule,
    MatFormFieldModule,
    DatePipe,
    TranslocoModule,
  ],
})
export class EditExpirationComponent implements OnInit, OnChanges, OnDestroy {
  /** Reactive forms builder used to create the expiration form group. */
  private readonly fb = inject(FormBuilder);
  /** Generated CIRCABC API client used to create, update and delete member expirations. */
  private readonly membersService = inject(MembersService);

  /** Required input controlling whether the modal dialog is visible. */
  readonly showModal = input.required<boolean>();
  /** Required input with the members whose expiration date is being edited. */
  readonly members = input.required<UserProfile[]>();
  /** Required input with the identifier of the group the members belong to. */
  readonly groupId = input.required<string>();
  /**
   * Output emitted when the modal should be closed, carrying the result of the
   * edit operation (succeeded, failed or canceled).
   */
  public readonly modalHide = output<ActionEmitterResult>();
  /** Working copy of the input members used to compute and apply changes. */
  public membersDisplay: UserProfile[] = [];
  /** Whether an expiration edit request is currently in progress. */
  public processing = signal(false);
  /** Reactive form holding the expiration toggle, date/time value and OK-button state. */
  public expirationForm!: FormGroup;
  /** Subscription to the calendar date handling wired up for the date/time control. */
  private dateSubscription!: Subscription;
  /** Earliest selectable date/time (end of the current day), used to constrain the picker. */
  public minDate: Date = new Date(new Date().setHours(23, 59, 0, 0));

  /**
   * Angular lifecycle hook. Builds the reactive form, wires up calendar date
   * handling and subscribes to value changes so that enabling/disabling the
   * expiration and picking a date keep the OK-button state in sync.
   */
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

  /**
   * Angular lifecycle hook. Unsubscribes from the calendar date handling
   * subscription to avoid memory leaks.
   */
  ngOnDestroy() {
    this.dateSubscription?.unsubscribe();
  }

  /**
   * Angular lifecycle hook. Refreshes the working member list when the
   * `members` input changes and recomputes the OK-button state.
   *
   * @param changes The set of changed input properties for this cycle.
   */
  ngOnChanges(changes: SimpleChanges) {
    if (changes.members) {
      this.updateMembersDisplay(changes.members.currentValue);
    }
    if (this.expirationForm) {
      this.updateShowOkButton();
    }
  }

  /**
   * Synchronises the form with the provided members: creates a working copy,
   * sets the expiration toggle when any member already has an expiration date
   * and, for a single member, pre-fills and enables the date/time control.
   *
   * @param members The members to reflect in the form.
   */
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

  /**
   * Enables or disables the date/time control according to the expiration
   * toggle, defaulting the value to {@link minDate} when enabled and clearing
   * it when disabled.
   *
   * @param value `true` to enable expiration, `false` to disable it.
   */
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

  /**
   * Recomputes the hidden `showOkButton` control: the OK button is enabled when
   * expiration is disabled, or when a date/time value has been selected.
   */
  private updateShowOkButton() {
    const expiration = this.expirationForm.controls.expiration.value;
    const expirationDateTime =
      this.expirationForm.controls.expirationDateTime.value;

    const showOkButtonValue = expiration
      ? expirationDateTime && expirationDateTime !== ''
      : true;
    this.expirationForm.controls.showOkButton.setValue(showOkButtonValue);
  }

  /**
   * Applies the expiration changes for all selected members. Depending on the
   * expiration toggle it either sets/updates or deletes expiration dates, then
   * emits the outcome through {@link modalHide}.
   *
   * @returns A promise that resolves once the operation completes and the
   *          result has been emitted.
   */
  public async editExpiration() {
    this.processing.set(true);
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
    this.processing.set(false);
  }

  /**
   * Removes the expiration date for every member that currently has one, in
   * parallel, via {@link MembersService.deleteMemberExpiration}.
   *
   * @returns A promise that resolves once all deletions have completed.
   */
  private async deleteExpirationDateTimes() {
    const deletePromises = this.membersDisplay
      .filter((member) => member.expirationDate && member.user?.userId)
      .map((member) =>
        this.membersService.deleteMemberExpirationAsync({
          id: this.groupId(),
          userId: member.user?.userId ?? '',
        })
      );

    await Promise.all(deletePromises);
  }

  /**
   * Sets the selected expiration date/time on all members, in parallel:
   * updating members that already have an expiration and creating one for the
   * others.
   *
   * @returns A promise that resolves once all create/update calls succeed.
   * @throws Re-throws any error raised while applying the expiration dates.
   */
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

  /**
   * Updates the expiration date/time of an existing member expiration.
   *
   * @param userId The identifier of the user whose expiration is updated.
   * @param expirationDateTime The new expiration date/time as an ISO string.
   * @returns A promise resolving when the update request completes.
   */
  private updateMemberExpiration(userId: string, expirationDateTime: string) {
    return this.membersService.updateMemberExpirationAsync({
      id: this.groupId(),
      userId,
      expirationDate: expirationDateTime,
    });
  }

  /**
   * Creates a new member expiration for a member that does not yet have one.
   * No request is made if the member is missing the required profile or user
   * identifiers.
   *
   * @param member The member for which to create an expiration.
   * @param expirationDateTime The expiration date/time as an ISO string.
   * @returns A promise resolving when the create request completes, or an
   *          already-resolved promise when required data is missing.
   */
  private createMemberExpiration(
    member: UserProfile,
    expirationDateTime: string
  ) {
    if (member.profile?.id && member.profile.groupName && member.user?.userId) {
      return this.membersService.createMemberExpirationAsync({
        id: this.groupId(),
        userId: member.user?.userId,
        expirationDate: expirationDateTime,
        profileId: member.profile.id,
        alfrescoGroup: member.profile.groupName,
      });
    }
    return Promise.resolve();
  }

  /**
   * Cancels the edit without applying any change and emits a canceled result
   * through {@link modalHide}.
   */
  public cancel() {
    this.modalHide.emit({
      type: ActionType.EDIT_EXPIRATION,
      result: ActionResult.CANCELED,
    });
  }

  /**
   * Produces a shallow-per-item copy of the given member array, cloning each
   * entry's `user`, `profile` and `expirationDate` so edits to the working
   * copy do not mutate the original input members.
   *
   * @param arr The members to copy (defaults to an empty array).
   * @returns A new array of copied {@link UserProfile} entries.
   */
  private deepArrayCopy(arr: UserProfile[] = []): UserProfile[] {
    return arr.map((item) => ({
      user: item.user,
      profile: item.profile,
      expirationDate: item.expirationDate,
    }));
  }
}
