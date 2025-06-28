import {
  Component,
  Input,
  OnChanges,
  OnInit,
  SimpleChanges,
  output,
  input,
} from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
} from '@angular/forms';

import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { KeywordDefinition, KeywordsService } from 'app/core/generated/circabc';
import { nonEmptyTitle } from 'app/core/validation.service';
import { ControlMessageComponent } from 'app/shared/control-message/control-message.component';
import { MultilingualInputComponent } from 'app/shared/input/multilingual-input.component';
import { ModalComponent } from 'app/shared/modal/modal.component';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-create-keyword',
  templateUrl: './create-keyword.component.html',
  preserveWhitespaces: true,
  imports: [
    ModalComponent,
    ReactiveFormsModule,
    MultilingualInputComponent,
    ControlMessageComponent,
  ],
})
export class CreateKeywordComponent implements OnInit, OnChanges {
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  showModal!: boolean;
  readonly parentIgId = input.required<string>();
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  keyword: KeywordDefinition | undefined;
  public readonly modalHide = output<ActionEmitterResult>();

  public createKeywordForm!: FormGroup;
  public creating = false;

  constructor(
    private fb: FormBuilder,
    private keywordsService: KeywordsService
  ) {}

  ngOnInit() {
    this.createKeywordForm = this.fb.group({
      title: ['', [nonEmptyTitle]],
    });
  }

  ngOnChanges(changes: SimpleChanges) {
    const chng = changes.keyword;
    if (chng?.currentValue) {
      this.createKeywordForm.controls.title.setValue(chng.currentValue.title);
    }
  }

  public async create() {
    this.creating = true;

    const kTmp: KeywordDefinition = {
      title: this.createKeywordForm.value.title,
    };

    await firstValueFrom(
      this.keywordsService.postKeywordDefinition(this.parentIgId(), kTmp)
    );

    const result: ActionEmitterResult = {};

    result.result = ActionResult.SUCCEED;
    result.type = ActionType.CREATE_KEYWORD;

    this.modalHide.emit(result);

    this.resetForm();
    this.creating = false;
    this.showModal = false;
  }

  private resetForm() {
    this.createKeywordForm.reset({ title: '' });
    this.createKeywordForm.controls.title.markAsUntouched();
    this.createKeywordForm.controls.title.markAsPristine();
  }

  public async update() {
    if (this.keyword) {
      this.creating = true;

      const kTmp: KeywordDefinition = {
        id: this.keyword.id,
        title: this.createKeywordForm.value.title,
      };

      const result: ActionEmitterResult = {};
      if (kTmp.id) {
        const res = await firstValueFrom(
          this.keywordsService.putKeywordDefinition(kTmp.id, kTmp)
        );
        if (res !== undefined) {
          result.result = ActionResult.SUCCEED;
          this.resetForm();
        } else {
          result.result = ActionResult.FAILED;
        }
      } else {
        result.result = ActionResult.FAILED;
      }

      result.type = ActionType.UPDATE_KEYWORD;
      this.modalHide.emit(result);
      this.creating = false;
      this.showModal = false;
      this.keyword = undefined;
    }
  }

  public cancelWizard(): void {
    this.showModal = false;
    const result: ActionEmitterResult = {};
    result.result = ActionResult.CANCELED;
    if (this.keyword !== undefined) {
      result.type = ActionType.CREATE_KEYWORD;
    } else {
      result.type = ActionType.UPDATE_KEYWORD;
    }
    this.keyword = undefined;
    this.resetForm();
    this.modalHide.emit(result);
  }

  get titleControl(): AbstractControl {
    return this.createKeywordForm.controls.title;
  }
}
