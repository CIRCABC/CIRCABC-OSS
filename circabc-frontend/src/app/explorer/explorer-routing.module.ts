import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { ContactCategoryComponent } from 'app/explorer/contact-category/contact-category.component';
import { ExplorerComponent } from 'app/explorer/explorer.component';
import { RequestGroupComponent } from 'app/explorer/request-group/request-group.component';

const explorerRoutes: Routes = [
  { path: '', component: ExplorerComponent },
  { path: 'group-request', component: RequestGroupComponent },
  { path: 'contact-category', component: ContactCategoryComponent },
];

@NgModule({
  imports: [RouterModule.forChild(explorerRoutes)],
  exports: [RouterModule],
})
export class ExplorerRoutingModule {}
