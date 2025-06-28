import { Component, output, input } from '@angular/core';

import { TranslocoService } from '@jsverse/transloco';
import {
  type KeywordDefinition,
  KeywordsService,
  Node as ModelNode,
} from 'app/core/generated/circabc';
import { supportedLanguages } from 'app/shared/langs/supported-langs';
import { CapitalizePipe } from 'app/shared/pipes/capitalize.pipe';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-keyword-tag',
  templateUrl: './keyword-tag.component.html',
  styleUrl: './keyword-tag.component.scss',
  preserveWhitespaces: true,
  imports: [SpinnerComponent, CapitalizePipe],
})
export class KeywordTagComponent {
  readonly keyword = input.required<KeywordDefinition>();
  readonly lang = input<string>();
  readonly documentNode = input<ModelNode>();
  readonly removable = input(false);
  readonly showLang = input(true);
  readonly removed = output<void>();

  public deleting = false;

  constructor(
    private translateService: TranslocoService,
    private keywordsService: KeywordsService
  ) {}

  public getTitleKeys(keyword: KeywordDefinition): string[] {
    const keywordValue = this.keyword();
    const lang = this.lang();
    if (keywordValue.title && lang) {
      if (Object.keys(keywordValue.title).indexOf(lang) !== -1) {
        return [lang];
      }
      if (
        Object.keys(keywordValue.title).indexOf(
          this.translateService.getActiveLang()
        ) !== -1
      ) {
        return [this.translateService.getActiveLang()];
      }
      return [Object.keys(keywordValue.title)[0]];
    }
    return Object.keys(keyword.title);
  }

  public async removeKeyword(keyword: KeywordDefinition) {
    const documentNode = this.documentNode();
    if (documentNode === undefined) {
      return;
    }
    if (this.removable() && keyword.id && documentNode.id) {
      this.deleting = true;
      await firstValueFrom(
        this.keywordsService.deleteKeyword(documentNode.id, keyword.id)
      );
      this.removed.emit();
      this.deleting = false;
    }
  }

  getLanguageName(langCode: string): string {
    for (const lang of supportedLanguages) {
      if (lang.code === langCode) {
        return lang.name;
      }
    }

    return '';
  }
}
