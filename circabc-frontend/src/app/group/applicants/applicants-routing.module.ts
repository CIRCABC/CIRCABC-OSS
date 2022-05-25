import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { ApplicantsComponent } from 'app/group/applicants/applicants.component';
import { ServiceAccessGuard } from 'app/group/guards/service-guard.service';

const applicantsRoutes: Routes = [
  {
    path: '',
    component: ApplicantsComponent,
    canActivate: [ServiceAccessGuard],
  },
];

@NgModule({
  imports: [RouterModule.forChild(applicantsRoutes)],
  exports: [RouterModule],
})
export class ApplicantsRoutingModule {}
