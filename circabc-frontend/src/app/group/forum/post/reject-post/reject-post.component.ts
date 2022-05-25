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
  selector: 'cbc-reject-post',
  templateUrl: './reject-post.component.html',
  preserveWhitespaces: true,
})
export class RejectPostComponent implements OnChanges {
  @Input()
  post!: ModelNode;
  @Input()
  showModal = false;
  @Output()
  readonly modalHide = new EventEmitter<ActionEmitterResult>();

  public executing = false;

  public rejectPostForm!: FormGroup;

  constructor(
    private postService: PostService,
    private formBuilder: FormBuilder
  ) {}

  ngOnChanges() {
    this.rejectPostForm = this.formBuilder.group(
      {
        rejectReason: [''],
      },
      {
        updateOn: 'change',
      }
    );
  }

  public async accept() {
    this.executing = true;

    await firstValueFrom(
      this.postService.putVerify(
        this.post.id as string,
        false,
        this.rejectPostForm.controls.rejectReason.value
      )
    );

    this.showModal = false;

    const result: ActionEmitterResult = {};
    result.result = ActionResult.SUCCEED;
    result.type = ActionType.REJECT_POST;
    this.modalHide.emit(result);

    this.executing = false;
  }

  public cancel(_action: string): void {
    this.showModal = false;
    const result: ActionEmitterResult = {};
    result.result = ActionResult.CANCELED;
    result.type = ActionType.REJECT_POST;
    this.modalHide.emit(result);
  }
}
