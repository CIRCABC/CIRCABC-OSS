import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { canActivateService } from 'app/group/guards/service-guard.service';

export const informationRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('app/group/information/information.component').then(
        (m) => m.InformationComponent
      ),
    canActivate: [canActivateService],
  },
  {
    path: 'add/:infoId',
    loadComponent: () =>
      import('app/group/information/add-news/add-news.component').then(
        (m) => m.AddNewsComponent
      ),
  },
  {
    path: ':newsId/edit',
    loadComponent: () =>
      import('app/group/information/add-news/add-news.component').then(
        (m) => m.AddNewsComponent
      ),
  },
];

@NgModule({
  imports: [RouterModule.forChild(informationRoutes)],
  exports: [RouterModule],
})
export class InformationRoutingModule {}
