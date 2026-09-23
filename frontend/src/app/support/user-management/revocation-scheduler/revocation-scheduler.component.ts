import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  OnDestroy,
  OnInit,
  output,
} from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { TranslocoModule } from '@jsverse/transloco';
import {
  HistoryService,
  UserRevocationRequest,
} from 'app/core/generated/circabc';
import { setupCalendarDateHandling } from 'app/core/util/date-calendar-util';
import { ModalComponent } from 'app/shared/modal/modal.component';
import { Subscription } from 'rxjs';

/**
 * Modal component that lets support users schedule the revocation of one or
 * more users' group memberships on a chosen date.
 *
 * The component renders a modal dialog (via {@link ModalComponent}) containing
 * a reactive form with a single date picker. When confirmed, it submits a
 * {@link UserRevocationRequest} through the {@link HistoryService} to revoke the
 * memberships of the selected users on the picked date, then notifies the
 * parent through the `scheduled` output. Cancelling the dialog emits the
 * `canceled` output instead.
 *
 * Key collaborators:
 * - {@link HistoryService}: performs the actual revocation request.
 * - {@link FormBuilder}: builds the reactive schedule form.
 * - {@link setupCalendarDateHandling}: normalizes the date-picker value.
 */
@Component({
  selector: 'cbc-revocation-scheduler',
  templateUrl: './revocation-scheduler.component.html',
  styleUrl: './revocation-scheduler.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ModalComponent,
    ReactiveFormsModule,
    MatDatepickerModule,
    MatInputModule,
    MatFormFieldModule,
    TranslocoModule,
  ],
})
export class RevocationSchedulerComponent implements OnInit, OnDestroy {
  /** API service used to submit the membership revocation request. */
  private readonly historyService = inject(HistoryService);
  /** Factory used to build the reactive schedule form. */
  private readonly fb = inject(FormBuilder);

  /** Input controlling whether the scheduler modal is visible. */
  readonly showModal = input(false);
  /** Required input holding the identifiers of the users whose memberships will be revoked. */
  readonly userIds = input.required<string[]>();
  /** Emitted once the revocation has been successfully scheduled. */
  readonly scheduled = output();
  /** Emitted when the user dismisses the scheduler without scheduling. */
  readonly canceled = output();

  /** Reactive form holding the `scheduleDate` control for the revocation date. */
  public scheduleForm!: FormGroup;
  /** Flag indicating that a revocation request is currently in flight. */
  public processing = false;
  /** Subscription managing calendar date handling for the schedule date control. */
  private dateSubscription!: Subscription;

  /**
   * Angular lifecycle hook. Initializes the reactive form with a required
   * `scheduleDate` defaulting to the current date and wires up calendar date
   * handling for that control.
   */
  ngOnInit() {
    this.scheduleForm = this.fb.group({
      scheduleDate: [new Date(), Validators.required],
    });

    this.dateSubscription = setupCalendarDateHandling(
      this.scheduleForm.controls.scheduleDate
    );
  }

  /**
   * Angular lifecycle hook. Unsubscribes from the calendar date handling
   * subscription to prevent memory leaks.
   */
  ngOnDestroy() {
    if (this.dateSubscription) {
      this.dateSubscription.unsubscribe();
    }
  }

  /**
   * Builds a {@link UserRevocationRequest} from the current user ids and the
   * selected schedule date and submits it via the {@link HistoryService} to
   * revoke the users' memberships. On success emits the `scheduled` output.
   *
   * Any error raised by the request is caught and logged to the console so the
   * component does not reject; the `processing` flag is reset in all cases.
   *
   * @returns A promise that resolves once the request completes (successfully
   * or after handling an error).
   */
  public async schedule() {
    this.processing = true;
    try {
      const body: UserRevocationRequest = {};
      body.userIds = this.userIds();
      body.revocationDate = this.scheduleForm.value.scheduleDate;
      body.action = 'revoke';
      await this.historyService.revokeUserMembershipsAsync({
        userRevocationRequest: body,
      });
      this.scheduled.emit();
    } catch (error) {
      console.error(error);
    }
    this.processing = false;
  }

  /**
   * Dismisses the scheduler without performing any revocation by emitting the
   * `canceled` output.
   */
  public cancel() {
    this.canceled.emit();
  }
}
