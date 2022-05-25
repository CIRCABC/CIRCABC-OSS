import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared/shared.module';

import { AddKeywordComponent } from 'app/group/keywords/add/add-keyword.component';
import { CreateKeywordComponent } from 'app/group/keywords/create/create-keyword.component';
import { DeleteKeywordComponent } from 'app/group/keywords/delete/delete-keyword.component';
import { DeleteMultiKeywordsComponent } from 'app/group/keywords/delete/delete-multi-keywords.component';
import { ImportKeywordComponent } from 'app/group/keywords/import-keyword/import-keyword.component';
import { KeywordsRoutingModule } from 'app/group/keywords/keywords-routing.module';
import { KeywordsComponent } from 'app/group/keywords/keywords.component';
import { KeywordTagComponent } from 'app/group/keywords/tag/keyword-tag.component';

@NgModule({
  imports: [KeywordsRoutingModule, SharedModule],
  exports: [KeywordTagComponent, CreateKeywordComponent, AddKeywordComponent],
  declarations: [
    KeywordsComponent,
    KeywordTagComponent,
    DeleteKeywordComponent,
    DeleteMultiKeywordsComponent,
    CreateKeywordComponent,
    AddKeywordComponent,
    ImportKeywordComponent,
  ],
  providers: [],
})
export class KeywordsModule {}
