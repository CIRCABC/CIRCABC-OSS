import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { MembersService } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-membership-application',
  templateUrl: './membership-application.component.html',
  preserveWhitespaces: true,
})
export class MembershipApplicationComponent implements OnInit {
  @Input()
  showModal = false;
  @Input()
  groupId: string | undefined;
  @Output()
  readonly canceled = new EventEmitter<ActionEmitterResult>();
  @Output()
  readonly finished = new EventEmitter<ActionEmitterResult>();

  public processing = false;
  public applicationForm!: FormGroup;

  constructor(
    private fb: FormBuilder,
    private loginService: LoginService,
    private membersService: MembersService
  ) {}

  ngOnInit() {
    this.applicationForm = this.fb.group(
      {
        username: [this.loginService.getCurrentUsername()],
        action: ['submitNew'],
        message: ['', Validators.required],
      },
      {
        updateOn: 'change',
      }
    );
  }

  async submitApplication() {
    if (this.groupId === undefined) {
      return;
    }
    this.processing = true;
    const res: ActionEmitterResult = {};
    res.type = ActionType.APPLY_FOR_MEMBERSHIP;

    try {
      await firstValueFrom(
        this.membersService.postApplicant(
          this.groupId,
          this.applicationForm.value
        )
      );
      res.result = ActionResult.SUCCEED;
      this.applicationForm.reset();
    } catch (error) {
      res.result = ActionResult.FAILED;
    }

    this.finished.emit(res);
    this.processing = false;
  }

  cancel() {
    this.applicationForm.controls.username.setValue(
      this.loginService.getCurrentUsername()
    );
    this.applicationForm.controls.action.setValue('submitNew');
    this.applicationForm.controls.message.setValue('');

    const res: ActionEmitterResult = {};
    res.type = ActionType.APPLY_FOR_MEMBERSHIP;
    res.result = ActionResult.CANCELED;

    this.showModal = false;
    this.canceled.emit(res);
  }
}
