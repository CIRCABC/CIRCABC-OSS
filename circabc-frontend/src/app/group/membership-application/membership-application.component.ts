import { HttpErrorResponse } from '@angular/common/http';
import { Component, Input, OnInit, inject, output, input } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';

import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { MembersService } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { ReadOnlyStateService } from 'app/core/read-only-state.service';
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

  public readonly readOnlyState = inject(ReadOnlyStateService);
  private readonly uiMessageService = inject(UiMessageService);
  private readonly translateService = inject(TranslocoService);

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

    // Defensive guard: block the submit if the IG became read-only after the
    // modal was opened. Mirrors the pattern used in bulk-invite.
    if (this.readOnlyState.isReadOnly()) {
      this.uiMessageService.addWarningMessage(
        this.translateService.translate('text.member.request.readonly.warning')
      );
      const cancelled: ActionEmitterResult = {
        type: ActionType.APPLY_FOR_MEMBERSHIP,
        result: ActionResult.FAILED,
      };
      this.finished.emit(cancelled);
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
    } catch (error: unknown) {
      res.result = ActionResult.FAILED;
      this.handleSubmitError(error);
    }

    this.finished.emit(res);
    this.processing = false;
  }

  /**
   * Surfaces backend failures during membership application submission. When
   * the backend returns 403 with a read-only message (the IG got locked between
   * page load and submit), reflect that in the local state and show an
   * actionable warning instead of a silent failure.
   */
  private handleSubmitError(error: unknown): void {
    if (error instanceof HttpErrorResponse) {
      const backendMessage = error.error?.message ?? '';
      const isReadOnly =
        error.status === 403 &&
        typeof backendMessage === 'string' &&
        backendMessage.toLowerCase().includes('read-only');

      if (isReadOnly) {
        this.readOnlyState.setReadOnly(true);
        this.uiMessageService.addWarningMessage(
          this.translateService.translate(
            'text.member.request.readonly.warning'
          )
        );
      }
    }
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
