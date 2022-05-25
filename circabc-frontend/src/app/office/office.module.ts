import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { OfficeComponent } from './office/office.component';
import { OfficeRoutingModule } from './office-routing.module';
import { MaterialComponentsModule } from 'app/material-components/material-components.module';
import { SharedModule } from 'app/shared/shared.module';

@NgModule({
  declarations: [OfficeComponent],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  imports: [
    CommonModule,
    MaterialComponentsModule,
    SharedModule,
    OfficeRoutingModule,
  ],
})
export class OfficeModule {}
