import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { canActivateService } from 'app/group/guards/service-guard.service';

export const applicantsRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('app/group/applicants/applicants.component').then(
        (m) => m.ApplicantsComponent
      ),
    canActivate: [canActivateService],
  },
];

@NgModule({
  imports: [RouterModule.forChild(applicantsRoutes)],
  exports: [RouterModule],
})
export class ApplicantsRoutingModule {}
