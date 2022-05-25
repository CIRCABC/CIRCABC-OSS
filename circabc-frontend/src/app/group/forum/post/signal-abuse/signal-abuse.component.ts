import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  Output,
} from '@angular/core';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';

import { FormBuilder, FormGroup } from '@angular/forms';
import { Node as ModelNode, PostService } from 'app/core/generated/circabc';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-signal-abuse',
  templateUrl: './signal-abuse.component.html',
  preserveWhitespaces: true,
})
export class SignalAbuseComponent implements OnChanges {
  @Input()
  post!: ModelNode;
  @Input()
  showModal = false;
  @Output()
  readonly modalHide = new EventEmitter<ActionEmitterResult>();

  public executing = false;

  public signalPostForm!: FormGroup;

  constructor(
    private postService: PostService,
    private formBuilder: FormBuilder
  ) {}

  ngOnChanges() {
    this.signalPostForm = this.formBuilder.group(
      {
        abuseText: [''],
      },
      {
        updateOn: 'change',
      }
    );
  }

  public async accept() {
    this.executing = true;

    await firstValueFrom(
      this.postService.postAbuse(
        this.post.id as string,
        this.signalPostForm.controls.abuseText.value
      )
    );

    this.showModal = false;

    const result: ActionEmitterResult = {};
    result.result = ActionResult.SUCCEED;
    result.type = ActionType.SIGNAL_ABUSE_POST;
    this.modalHide.emit(result);

    this.executing = false;
  }

  public cancel(_action: string): void {
    this.showModal = false;
    const result: ActionEmitterResult = {};
    result.result = ActionResult.CANCELED;
    result.type = ActionType.SIGNAL_ABUSE_POST;
    this.modalHide.emit(result);
  }

  // post.properties.message
  get message(): string {
    if (this.post.properties) {
      return this.post.properties.message;
    } else {
      return '';
    }
  }
}
