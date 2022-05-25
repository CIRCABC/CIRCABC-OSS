import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { KeywordsComponent } from 'app/group/keywords/keywords.component';

const keywordsRoutes: Routes = [{ path: '', component: KeywordsComponent }];

@NgModule({
  imports: [RouterModule.forChild(keywordsRoutes)],
  exports: [RouterModule],
})
export class KeywordsRoutingModule {}
