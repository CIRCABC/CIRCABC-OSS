import { Component, Input, OnInit, output, input } from '@angular/core';
import {
  AbstractControl,
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
import {
  ContentService,
  MultilingualAspectMetadata,
  Node as ModelNode,
} from 'app/core/generated/circabc';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { getErrorTranslation } from 'app/core/util';
import { ControlMessageComponent } from 'app/shared/control-message/control-message.component';
import { LangSelectorComponent } from 'app/shared/lang/lang-selector.component';
import { ModalComponent } from 'app/shared/modal/modal.component';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-enable-multilingual',
  templateUrl: './enable-multilingual.component.html',
  preserveWhitespaces: true,
  imports: [
    ModalComponent,
    ReactiveFormsModule,
    ControlMessageComponent,
    LangSelectorComponent,
    TranslocoModule,
  ],
})
export class EnableMultilingualComponent implements OnInit {
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  showModal = false;
  readonly targetNode = input.required<ModelNode>();
  readonly modalCanceled = output<ActionEmitterResult>();
  readonly mutlilingualEnabled = output<ActionEmitterResult>();

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
    const targetNode = this.targetNode();
    if (targetNode.id) {
      this.processing = true;
      const data: MultilingualAspectMetadata = {};
      data.pivotLang = this.enableMultilingualForm.value.lang;
      data.author = this.enableMultilingualForm.value.author;

      const result: ActionEmitterResult = {};
      result.type = ActionType.ENABLE_MULTILINGUAL;

      try {
        await firstValueFrom(
          this.contentService.postMultilingualAspect(targetNode.id, data)
        );
        result.result = ActionResult.SUCCEED;
      } catch (_error) {
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
