import { Component, Input, OnChanges, output, input } from '@angular/core';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';

import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { TranslocoModule } from '@jsverse/transloco';
import { Node as ModelNode, PostService } from 'app/core/generated/circabc';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-signal-abuse',
  templateUrl: './signal-abuse.component.html',
  preserveWhitespaces: true,
  imports: [ReactiveFormsModule, SpinnerComponent, TranslocoModule],
})
export class SignalAbuseComponent implements OnChanges {
  readonly post = input.required<ModelNode>();
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  showModal = false;
  readonly modalHide = output<ActionEmitterResult>();

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
        this.post().id as string,
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
    const post = this.post();
    if (post.properties) {
      return post.properties.message;
    }
    return '';
  }
}
