import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

export const keywordsRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('app/group/keywords/keywords.component').then(
        (m) => m.KeywordsComponent
      ),
  },
];

@NgModule({
  imports: [RouterModule.forChild(keywordsRoutes)],
  exports: [RouterModule],
})
export class KeywordsRoutingModule {}
