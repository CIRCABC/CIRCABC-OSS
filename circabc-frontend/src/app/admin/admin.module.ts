import { NgModule } from '@angular/core';

import { AdminRoutingModule } from 'app/admin/admin-routing.module';
import { AdminComponent } from 'app/admin/admin.component';
import { CreateCategoryComponent } from 'app/admin/create-category/create-category.component';
import { AddHeaderComponent } from 'app/admin/headers/add-header/add-header.component';
import { HeadersComponent } from 'app/admin/headers/headers.component';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { SharedModule } from 'app/shared/shared.module';

@NgModule({
  imports: [AdminRoutingModule, SharedModule],
  exports: [],
  declarations: [
    AddHeaderComponent,
    AdminComponent,
    CreateCategoryComponent,
    HeadersComponent,
  ],
  providers: [I18nPipe],
})
export class AdminModule {}
