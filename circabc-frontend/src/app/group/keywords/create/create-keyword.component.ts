import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges,
} from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup } from '@angular/forms';

import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { KeywordDefinition, KeywordsService } from 'app/core/generated/circabc';
import { ValidationService } from 'app/core/validation.service';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-create-keyword',
  templateUrl: './create-keyword.component.html',
  preserveWhitespaces: true,
})
export class CreateKeywordComponent implements OnInit, OnChanges {
  @Input()
  showModal!: boolean;
  @Input()
  parentIgId!: string;
  @Input()
  keyword: KeywordDefinition | undefined;
  @Output()
  public readonly modalHide = new EventEmitter<ActionEmitterResult>();

  public createKeywordForm!: FormGroup;
  public creating = false;

  constructor(
    private fb: FormBuilder,
    private keywordsService: KeywordsService
  ) {}

  ngOnInit() {
    this.createKeywordForm = this.fb.group({
      title: ['', [ValidationService.nonEmptyTitle]],
    });
  }

  ngOnChanges(changes: SimpleChanges) {
    const chng = changes.keyword;
    if (chng && chng.currentValue) {
      this.createKeywordForm.controls.title.setValue(chng.currentValue.title);
    }
  }

  public async create() {
    this.creating = true;

    const kTmp: KeywordDefinition = {
      title: this.createKeywordForm.value.title,
    };

    await firstValueFrom(
      this.keywordsService.postKeywordDefinition(this.parentIgId, kTmp)
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
