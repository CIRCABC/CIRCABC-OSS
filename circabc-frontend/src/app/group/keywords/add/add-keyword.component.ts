import { Component, OnInit, output, input } from '@angular/core';

import { TranslocoModule } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { KeywordsService, Node as ModelNode } from 'app/core/generated/circabc';
import { SelectableKeyword } from 'app/core/ui-model/index';
import { arrayDiff } from 'app/core/util';
import { KeywordTagComponent } from 'app/group/keywords/tag/keyword-tag.component';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-add-keyword',
  templateUrl: './add-keyword.component.html',
  styleUrl: './add-keyword.component.scss',
  preserveWhitespaces: true,
  imports: [KeywordTagComponent, SpinnerComponent, TranslocoModule],
})
export class AddKeywordComponent implements OnInit {
  readonly documentNode = input.required<ModelNode>();
  readonly groupNode = input.required<ModelNode>();
  readonly addedKeyword = output<ActionEmitterResult>();

  public showModal = false;
  public adding = false;
  public availableKeywords: SelectableKeyword[] = [];
  public usedKeywords: SelectableKeyword[] = [];

  constructor(private keywordsService: KeywordsService) {}

  ngOnInit(): void {
    this.loadAvailableKeywords();
  }

  public async openModal() {
    await this.loadAvailableKeywords();
    this.showModal = true;
  }

  public async loadAvailableKeywords() {
    this.availableKeywords = [];
    const groupNode = this.groupNode();
    const documentNode = this.documentNode();
    if (groupNode.id && documentNode.id) {
      this.usedKeywords = await firstValueFrom(
        this.keywordsService.getKeywords(documentNode.id)
      );
      const sourceKeywords = await firstValueFrom(
        this.keywordsService.getKeywordDefinitions(groupNode.id)
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
      this.availableKeywords !== undefined && this.availableKeywords.length > 0
    );
  }

  public cancelWizard(_action: string): void {
    this.showModal = false;
  }

  public async add() {
    let keywordsAdded = false;
    this.adding = true;
    for (const keyword of this.availableKeywords) {
      const documentNode = this.documentNode();
      if (keyword.selected && documentNode.id) {
        await firstValueFrom(
          this.keywordsService.postKeyword(documentNode.id, keyword)
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
