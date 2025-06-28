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
import { EmailService, type InterestGroup } from 'app/core/generated/circabc';
import { ModalComponent } from 'app/shared/modal/modal.component';
import { SharedModule } from 'primeng/api';
import { EditorModule } from 'primeng/editor';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-leader-contact',
  templateUrl: './leader-contact.component.html',
  preserveWhitespaces: true,
  imports: [
    ModalComponent,
    ReactiveFormsModule,
    EditorModule,
    SharedModule,
    TranslocoModule,
  ],
})
export class LeaderContactComponent implements OnInit {
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  showModal = false;
  readonly group = input.required<InterestGroup>();
  readonly modalHide = output<ActionEmitterResult>();

  public processing = false;
  public contactLeaderForm!: FormGroup;

  constructor(
    private fb: FormBuilder,
    private emailService: EmailService
  ) {}

  async ngOnInit() {
    this.contactLeaderForm = this.fb.group({
      message: ['', Validators.required],
    });
  }

  public async send() {
    this.processing = true;
    const res: ActionEmitterResult = {};
    res.type = ActionType.CONTACT_LEADERS;

    const group = this.group();
    if (group?.id) {
      try {
        await firstValueFrom(
          this.emailService.contactLeadersByEmail(
            group.id,
            this.contactLeaderForm.value.message
          )
        );

        res.result = ActionResult.SUCCEED;
      } catch (_error) {
        res.result = ActionResult.FAILED;
      }
    }

    this.processing = false;
    this.modalHide.emit(res);
  }

  public cancel() {
    this.showModal = false;
    const res: ActionEmitterResult = {};
    res.type = ActionType.CONTACT_LEADERS;
    res.result = ActionResult.CANCELED;
    this.modalHide.emit(res);
  }
}
