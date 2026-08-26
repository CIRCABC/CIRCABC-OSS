import { Component, OnInit, output, input, OnDestroy } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { TranslocoModule } from '@jsverse/transloco';
import {
  HistoryService,
  UserRevocationRequest,
} from 'app/core/generated/circabc';
import { setupCalendarDateHandling } from 'app/core/util/date-calendar-util';
import { ModalComponent } from 'app/shared/modal/modal.component';
import { DatePicker } from 'primeng/datepicker';
import { firstValueFrom, Subscription } from 'rxjs';

@Component({
  selector: 'cbc-revocation-scheduler',
  templateUrl: './revocation-scheduler.component.html',
  styleUrl: './revocation-scheduler.component.scss',
  imports: [ModalComponent, ReactiveFormsModule, DatePicker, TranslocoModule],
})
export class RevocationSchedulerComponent implements OnInit, OnDestroy {
  readonly showModal = input(false);
  readonly userIds = input.required<string[]>();
  readonly scheduled = output();
  readonly canceled = output();

  public scheduleForm!: FormGroup;
  public processing = false;
  private dateSubscription!: Subscription;

  constructor(
    private historyService: HistoryService,
    private fb: FormBuilder
  ) {}

  ngOnInit() {
    this.scheduleForm = this.fb.group({
      scheduleDate: [new Date(), Validators.required],
    });

    this.dateSubscription = setupCalendarDateHandling(
      this.scheduleForm.controls.scheduleDate
    );
  }

  ngOnDestroy() {
    if (this.dateSubscription) {
      this.dateSubscription.unsubscribe();
    }
  }

  public async schedule() {
    this.processing = true;
    try {
      const body: UserRevocationRequest = {};
      body.userIds = this.userIds();
      body.revocationDate = this.scheduleForm.value.scheduleDate;
      body.action = 'revoke';
      await firstValueFrom(this.historyService.revokeUserMemberships(body));
      this.scheduled.emit();
    } catch (error) {
      console.error(error);
    }
    this.processing = false;
  }

  public cancel() {
    this.canceled.emit();
  }
}
