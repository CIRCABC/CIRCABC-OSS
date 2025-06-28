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
  UserMembershipsExpirationRequest,
} from 'app/core/generated/circabc';
import { setupCalendarDateHandling } from 'app/core/util/date-calendar-util';
import { ModalComponent } from 'app/shared/modal/modal.component';
import { UsersMembershipsModel } from 'app/support/user-management/users-memberships-model';
import { DatePicker } from 'primeng/datepicker';
import { firstValueFrom, Subscription } from 'rxjs';

@Component({
  selector: 'cbc-expiration-scheduler',
  templateUrl: './expiration-scheduler.component.html',
  styleUrl: './expiration-scheduler.component.scss',
  imports: [ModalComponent, ReactiveFormsModule, DatePicker, TranslocoModule],
})
export class ExpirationSchedulerComponent implements OnInit, OnDestroy {
  readonly showModal = input(false);
  readonly requests = input<UsersMembershipsModel[]>([]);
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

      await firstValueFrom(this.historyService.setMembershipsExpiration(body));
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
