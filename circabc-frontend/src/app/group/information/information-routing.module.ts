import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { ServiceAccessGuard } from 'app/group/guards/service-guard.service';
import { AddNewsComponent } from 'app/group/information/add-news/add-news.component';
import { InformationComponent } from 'app/group/information/information.component';

const informationRoutes: Routes = [
  {
    path: '',
    component: InformationComponent,
    canActivate: [ServiceAccessGuard],
  },
  { path: 'add/:infoId', component: AddNewsComponent },
  { path: ':newsId/edit', component: AddNewsComponent },
];

@NgModule({
  imports: [RouterModule.forChild(informationRoutes)],
  exports: [RouterModule],
})
export class InformationRoutingModule {}
