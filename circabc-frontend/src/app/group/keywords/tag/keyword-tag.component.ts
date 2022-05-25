import { Component, EventEmitter, Input, Output } from '@angular/core';

import { TranslocoService } from '@ngneat/transloco';
import {
  KeywordDefinition,
  KeywordsService,
  Node as ModelNode,
} from 'app/core/generated/circabc';
import { SupportedLangs } from 'app/shared/langs/supported-langs';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-keyword-tag',
  templateUrl: './keyword-tag.component.html',
  styleUrls: ['./keyword-tag.component.scss'],
  preserveWhitespaces: true,
})
export class KeywordTagComponent {
  @Input()
  keyword!: KeywordDefinition;
  @Input()
  lang: string | undefined;
  @Input()
  documentNode!: ModelNode;
  @Input()
  removable = false;
  @Input()
  showLang = true;
  @Output()
  readonly removed = new EventEmitter<string>();

  public deleting = false;

  constructor(
    private translateService: TranslocoService,
    private keywordsService: KeywordsService
  ) {}

  public getTitleKeys(keyword: KeywordDefinition): string[] {
    if (this.keyword.title && this.lang) {
      if (Object.keys(this.keyword.title).indexOf(this.lang) !== -1) {
        return [this.lang];
      } else if (
        Object.keys(this.keyword.title).indexOf(
          this.translateService.getActiveLang()
        ) !== -1
      ) {
        return [this.translateService.getActiveLang()];
      } else {
        return [Object.keys(this.keyword.title)[0]];
      }
    } else {
      return Object.keys(keyword.title);
    }
  }

  public async removeKeyword(keyword: KeywordDefinition) {
    if (this.removable && keyword.id && this.documentNode.id) {
      this.deleting = true;
      await firstValueFrom(
        this.keywordsService.deleteKeyword(this.documentNode.id, keyword.id)
      );
      this.removed.emit();
      this.deleting = false;
    }
  }

  getLanguageName(langCode: string): string {
    for (const lang of SupportedLangs.availableLangs) {
      if (lang.code === langCode) {
        return lang.name;
      }
    }

    return '';
  }
}
