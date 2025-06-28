import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

export const profilesRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('app/group/profiles/profiles.component').then(
        (m) => m.ProfilesComponent
      ),
  },
];

@NgModule({
  imports: [RouterModule.forChild(profilesRoutes)],
  exports: [RouterModule],
})
export class ProfilesRoutingModule {}
