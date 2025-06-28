import { Component, OnInit, input } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { TranslocoModule } from '@jsverse/transloco';
import { KeywordDefinition, KeywordsService } from 'app/core/generated/circabc';
import { SelectableKeyword } from 'app/core/ui-model/index';
import { KeywordTagComponent } from 'app/group/keywords/tag/keyword-tag.component';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-form-keyword-finder',
  templateUrl: './form-keyword-finder.component.html',
  styleUrl: './form-keyword-finder.component.scss',
  preserveWhitespaces: true,
  imports: [KeywordTagComponent, TranslocoModule],
})
export class FormKeywordFinderComponent implements OnInit {
  public readonly groupId = input.required<string | undefined>();

  public readonly label = input('label.keywords');

  public readonly searchAdvancedForm = input.required<FormGroup>();

  public availableKeywords: SelectableKeyword[] = [];
  public selectedKeywords: KeywordDefinition[] = [];

  constructor(private keywordsService: KeywordsService) {}

  ngOnInit(): void {
    this.loadAvailableKeywords();
    this.searchAdvancedForm().valueChanges.subscribe((dataForm) => {
      const loadKeywordsSelected: KeywordDefinition[] = dataForm.keywords;
      this.availableKeywords.forEach((keywordTmp, index) => {
        if (loadKeywordsSelected) {
          const keyword = loadKeywordsSelected.filter(
            (keyword) => keyword.id === keywordTmp.id
          );

          if (keyword && keywordTmp.id === keyword[0]?.id) {
            this.availableKeywords[index].selected = true;
          } else {
            this.availableKeywords[index].selected = false;
          }
        } else {
          this.availableKeywords[index].selected = false;
        }
      });
    });
  }

  public async loadAvailableKeywords() {
    this.availableKeywords = [];
    const groupId = this.groupId();
    if (groupId) {
      this.availableKeywords = await firstValueFrom(
        this.keywordsService.getKeywordDefinitions(groupId)
      );
    }
  }

  public toggleSelected(keyword: SelectableKeyword) {
    this.availableKeywords.forEach((keywordTmp) => {
      if (keywordTmp.id === keyword.id) {
        keywordTmp.selected = !keywordTmp.selected;
      }
    });

    this.searchAdvancedForm().controls.keywords.setValue(
      this.availableKeywords.filter((keyword) => keyword.selected)
    );
  }

  public hasKeywords(): boolean {
    return (
      this.availableKeywords !== undefined && this.availableKeywords.length > 0
    );
  }
}
