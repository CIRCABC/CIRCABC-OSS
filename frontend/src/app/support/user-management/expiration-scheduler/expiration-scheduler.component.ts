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
  UserMembershipsExpirationRequest,
} from 'app/core/generated/circabc';
import { setupCalendarDateHandling } from 'app/core/util/date-calendar-util';
import { ModalComponent } from 'app/shared/modal/modal.component';
import { UsersMembershipsModel } from 'app/support/user-management/users-memberships-model';
import { Subscription } from 'rxjs';

/**
 * Modal component that lets a support administrator schedule a future
 * expiration date for a set of selected user memberships.
 *
 * It renders a modal dialog (via {@link ModalComponent}) containing a reactive
 * form with a single date picker. When confirmed, the component collects the
 * memberships that were flagged as `selected` for each incoming request and
 * submits a batch expiration request to the backend through
 * {@link HistoryService}. Successful submission and cancellation are surfaced
 * to the parent component via the `scheduled` and `canceled` outputs.
 *
 * @see UsersMembershipsModel
 * @see UserMembershipsExpirationRequest
 */
@Component({
  selector: 'cbc-expiration-scheduler',
  templateUrl: './expiration-scheduler.component.html',
  styleUrl: './expiration-scheduler.component.scss',
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
export class ExpirationSchedulerComponent implements OnInit, OnDestroy {
  /** Backend client used to persist the batch memberships expiration request. */
  private readonly historyService = inject(HistoryService);
  /** Factory used to build the reactive scheduling form. */
  private readonly fb = inject(FormBuilder);

  /** Input controlling whether the scheduler modal is displayed. */
  readonly showModal = input(false);
  /**
   * Input carrying the users and their memberships to be considered for
   * expiration. Only memberships flagged as `selected` are submitted.
   */
  readonly requests = input<UsersMembershipsModel[]>([]);
  /** Emitted after the expiration request has been successfully submitted. */
  readonly scheduled = output();
  /** Emitted when the user dismisses the scheduler without submitting. */
  readonly canceled = output();

  /** Reactive form holding the `scheduleDate` control (required). */
  public scheduleForm!: FormGroup;
  /** Indicates whether a submission is currently in progress. */
  public processing = false;
  /** Subscription managing calendar date normalisation for the date control. */
  private dateSubscription!: Subscription;

  /**
   * Angular lifecycle hook. Initialises the scheduling form with a required
   * `scheduleDate` control defaulting to the current date, and wires up
   * calendar date handling for that control.
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
   * subscription to avoid memory leaks when the component is destroyed.
   */
  ngOnDestroy() {
    if (this.dateSubscription) {
      this.dateSubscription.unsubscribe();
    }
  }

  /**
   * Builds and submits the batch memberships expiration request.
   *
   * Iterates over the {@link requests} input, keeping only the memberships
   * flagged as `selected`, and posts a {@link UserMembershipsExpirationRequest}
   * per user (using the form's `scheduleDate`) via {@link HistoryService}.
   * On success the `scheduled` output is emitted; any error is logged to the
   * console. The `processing` flag is toggled around the operation.
   *
   * @returns A promise that resolves once the request completes (whether it
   * succeeded or failed).
   */
  public async schedule() {
    this.processing = true;
    try {
      const body: UserMembershipsExpirationRequest[] = [];

      this.requests().forEach((item) => {
        const futureExpirations = item.memberships.filter((membership) => {
          return membership.selected;
        });

        if (futureExpirations.length > 0) {
          body.push({
            userId: item.userid,
            expirationDate: this.scheduleForm.value.scheduleDate,
            memberships: futureExpirations,
          });
        }
      });

      await this.historyService.setMembershipsExpirationAsync({
        userMembershipsExpirationRequest: body,
      });
      this.scheduled.emit();
    } catch (error) {
      console.error(error);
    }
    this.processing = false;
  }

  /**
   * Dismisses the scheduler by emitting the `canceled` output, allowing the
   * parent component to close the modal without scheduling any expiration.
   */
  public cancel() {
    this.canceled.emit();
  }
}
