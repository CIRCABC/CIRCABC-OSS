import { NgModule } from '@angular/core';

import { ApplicantsRoutingModule } from 'app/group/applicants/applicants-routing.module';
import { ApplicantsComponent } from 'app/group/applicants/applicants.component';
import { RequestComponent } from 'app/group/applicants/request/request.component';
import { SharedModule } from 'app/shared/shared.module';

@NgModule({
  imports: [ApplicantsRoutingModule, SharedModule],
  exports: [],
  declarations: [ApplicantsComponent, RequestComponent],
  providers: [],
})
export class ApplicantsModule {}
