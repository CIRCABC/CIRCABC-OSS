import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { EmailService, InterestGroup } from 'app/core/generated/circabc';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-leader-contact',
  templateUrl: './leader-contact.component.html',
  preserveWhitespaces: true,
})
export class LeaderContactComponent implements OnInit {
  @Input()
  showModal = false;
  @Input()
  group!: InterestGroup;
  @Output()
  readonly modalHide = new EventEmitter<ActionEmitterResult>();

  public processing = false;
  public contactLeaderForm!: FormGroup;

  constructor(private fb: FormBuilder, private emailService: EmailService) {}

  async ngOnInit() {
    this.contactLeaderForm = this.fb.group({
      message: ['', Validators.required],
    });
  }

  public async send() {
    this.processing = true;
    const res: ActionEmitterResult = {};
    res.type = ActionType.CONTACT_LEADERS;

    if (this.group && this.group.id) {
      try {
        await firstValueFrom(
          this.emailService.contactLeadersByEmail(
            this.group.id,
            this.contactLeaderForm.value.message
          )
        );

        res.result = ActionResult.SUCCEED;
      } catch (error) {
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
