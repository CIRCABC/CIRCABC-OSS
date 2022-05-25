import { Component, EventEmitter, Input, Output } from '@angular/core';

import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { KeywordsService, Node as ModelNode } from 'app/core/generated/circabc';
import { SelectableKeyword } from 'app/core/ui-model/index';
import { arrayDiff } from 'app/core/util';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-add-keyword',
  templateUrl: './add-keyword.component.html',
  styleUrls: ['./add-keyword.component.scss'],
  preserveWhitespaces: true,
})
export class AddKeywordComponent {
  @Input()
  documentNode!: ModelNode;
  @Input()
  groupNode!: ModelNode;
  @Output()
  readonly addedKeyword = new EventEmitter<ActionEmitterResult>();

  public showModal = false;
  public adding = false;
  public availableKeywords: SelectableKeyword[] = [];
  public usedKeywords: SelectableKeyword[] = [];

  constructor(private keywordsService: KeywordsService) {}

  public async openModal() {
    await this.loadAvailableKeywords();
    this.showModal = true;
  }

  public async loadAvailableKeywords() {
    this.availableKeywords = [];
    if (this.groupNode.id && this.documentNode.id) {
      this.usedKeywords = await firstValueFrom(
        this.keywordsService.getKeywords(this.documentNode.id)
      );
      const sourceKeywords = await firstValueFrom(
        this.keywordsService.getKeywordDefinitions(this.groupNode.id)
      );
      this.availableKeywords = arrayDiff(
        sourceKeywords,
        this.usedKeywords,
        'id'
      );
    }
  }

  public toggleSelected(keyword: SelectableKeyword) {
    this.availableKeywords.forEach((keywordTmp) => {
      if (keywordTmp.id === keyword.id) {
        keywordTmp.selected = !keywordTmp.selected;
      }
    });
  }

  public hasKeywords(): boolean {
    return (
      this.availableKeywords !== undefined && this.availableKeywords !== []
    );
  }

  public cancelWizard(_action: string): void {
    this.showModal = false;
  }

  public async add() {
    let keywordsAdded = false;
    this.adding = true;
    for (const keyword of this.availableKeywords) {
      if (keyword.selected && this.documentNode.id) {
        await firstValueFrom(
          this.keywordsService.postKeyword(this.documentNode.id, keyword)
        );
        keywordsAdded = true;
      }
    }

    if (keywordsAdded) {
      await this.loadAvailableKeywords();
      const result: ActionEmitterResult = {};
      result.type = ActionType.ADD_KEYWORD;
      result.result = ActionResult.SUCCEED;
      this.addedKeyword.emit(result);
    }
    this.adding = false;
    this.showModal = false;
  }
}
