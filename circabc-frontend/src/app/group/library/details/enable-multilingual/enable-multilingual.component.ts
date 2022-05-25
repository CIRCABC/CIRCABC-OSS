import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  Validators,
} from '@angular/forms';

import { TranslocoService } from '@ngneat/transloco';

import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import {
  ContentService,
  MultilingualAspectMetadata,
  Node as ModelNode,
} from 'app/core/generated/circabc';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { getErrorTranslation } from 'app/core/util';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-enable-multilingual',
  templateUrl: './enable-multilingual.component.html',
  preserveWhitespaces: true,
})
export class EnableMultilingualComponent implements OnInit {
  @Input()
  showModal = false;
  @Input()
  targetNode!: ModelNode;
  @Output()
  readonly modalCanceled = new EventEmitter<ActionEmitterResult>();
  @Output()
  readonly mutlilingualEnabled = new EventEmitter<ActionEmitterResult>();

  public enableMultilingualForm!: FormGroup;
  public processing = false;

  constructor(
    private uiMessageService: UiMessageService,
    private translateService: TranslocoService,
    private fb: FormBuilder,
    private contentService: ContentService
  ) {}

  ngOnInit() {
    this.enableMultilingualForm = this.fb.group(
      {
        author: ['', Validators.required],
        lang: [this.translateService.getDefaultLang(), Validators.required],
      },
      {
        updateOn: 'change',
      }
    );
  }

  async enableMultilingual() {
    if (this.targetNode.id) {
      this.processing = true;
      const data: MultilingualAspectMetadata = {};
      data.pivotLang = this.enableMultilingualForm.value.lang;
      data.author = this.enableMultilingualForm.value.author;

      const result: ActionEmitterResult = {};
      result.type = ActionType.ENABLE_MULTILINGUAL;

      try {
        await firstValueFrom(
          this.contentService.postMultilingualAspect(this.targetNode.id, data)
        );
        result.result = ActionResult.SUCCEED;
      } catch (error) {
        result.result = ActionResult.FAILED;
        const txt = this.translateService.translate(
          getErrorTranslation(ActionType.ENABLE_MULTILINGUAL)
        );
        this.uiMessageService.addErrorMessage(txt, false);
      }

      this.mutlilingualEnabled.emit(result);

      this.processing = false;
    }
  }

  cancel() {
    this.processing = false;
    this.showModal = false;

    const result: ActionEmitterResult = {};
    result.result = ActionResult.CANCELED;
    result.type = ActionType.ENABLE_MULTILINGUAL;
    this.modalCanceled.emit(result);
  }

  get authorControl(): AbstractControl {
    return this.enableMultilingualForm.controls.author;
  }
}
