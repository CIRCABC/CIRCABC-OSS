import { Component, Input, OnInit, output, input } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';

import { TranslocoModule } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { MembersService } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';
import { SharedModule } from 'primeng/api';
import { EditorModule } from 'primeng/editor';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-membership-application',
  templateUrl: './membership-application.component.html',
  preserveWhitespaces: true,
  imports: [
    ReactiveFormsModule,
    EditorModule,
    SharedModule,
    SpinnerComponent,
    TranslocoModule,
  ],
})
export class MembershipApplicationComponent implements OnInit {
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  showModal = false;
  readonly groupId = input<string>();
  readonly canceled = output<ActionEmitterResult>();
  readonly finished = output<ActionEmitterResult>();

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
    const groupId = this.groupId();
    if (groupId === undefined) {
      return;
    }
    this.processing = true;
    const res: ActionEmitterResult = {};
    res.type = ActionType.APPLY_FOR_MEMBERSHIP;

    try {
      await firstValueFrom(
        this.membersService.postApplicant(groupId, this.applicationForm.value)
      );
      res.result = ActionResult.SUCCEED;
      this.applicationForm.reset();
    } catch (_error) {
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
