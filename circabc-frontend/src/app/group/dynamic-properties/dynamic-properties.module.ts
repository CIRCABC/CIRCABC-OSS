import { NgModule } from '@angular/core';

import { DynamicPropertyBoxComponent } from 'app/group/dynamic-properties/box/dynamic-property-box.component';
import { CreateDynamicPropertyComponent } from 'app/group/dynamic-properties/create/create-dynamic-property.component';
import { DynamicPropertyDeleteComponent } from 'app/group/dynamic-properties/delete/dynamic-property-delete.component';
import { DynamicPropertiesRoutingModule } from 'app/group/dynamic-properties/dynamic-properties-routing.module';
import { DynamicPropertiesComponent } from 'app/group/dynamic-properties/dynamic-properties.component';
import { EditDynamicPropertyComponent } from 'app/group/dynamic-properties/edit-dynamic-property/edit-dynamic-property.component';
import { TitleTagComponent } from 'app/group/dynamic-properties/title/title-tag.component';
import { SharedModule } from 'app/shared/shared.module';

@NgModule({
  imports: [DynamicPropertiesRoutingModule, SharedModule],
  exports: [],
  declarations: [
    DynamicPropertiesComponent,
    DynamicPropertyBoxComponent,
    TitleTagComponent,
    DynamicPropertyDeleteComponent,
    CreateDynamicPropertyComponent,
    EditDynamicPropertyComponent,
  ],
  providers: [],
})
export class DynamicPropertiesModule {}
