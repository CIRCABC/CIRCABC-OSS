import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import {
  HistoryService,
  UserMembershipsExpirationRequest,
} from 'app/core/generated/circabc';
import { UsersMembershipsModel } from 'app/support/user-management/users-memberships-model';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-expiration-scheduler',
  templateUrl: './expiration-scheduler.component.html',
  styleUrls: ['./expiration-scheduler.component.scss'],
})
export class ExpirationSchedulerComponent implements OnInit {
  @Input() showModal = false;
  @Input() requests: UsersMembershipsModel[] = [];
  @Output() readonly scheduled = new EventEmitter();
  @Output() readonly canceled = new EventEmitter();

  public scheduleForm!: FormGroup;
  public processing = false;

  constructor(
    private historyService: HistoryService,
    private fb: FormBuilder
  ) {}

  ngOnInit() {
    this.scheduleForm = this.fb.group({
      scheduleDate: [new Date(), Validators.required],
    });
  }

  public async schedule() {
    this.processing = true;
    try {
      const body: UserMembershipsExpirationRequest[] = [];

      this.requests.forEach((item) => {
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
