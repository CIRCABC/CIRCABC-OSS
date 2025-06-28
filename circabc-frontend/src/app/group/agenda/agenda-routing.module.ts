import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

export const agendaRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('app/group/agenda/agenda.component').then(
        (m) => m.AgendaComponent
      ),
  },
  {
    path: ':eventId/details',
    loadComponent: () =>
      import(
        'app/group/agenda/view-edit-details-event/view-edit-details-event.component'
      ).then((m) => m.ViewEditDetailsEventComponent),
  },
  {
    path: 'list',
    loadComponent: () =>
      import('app/group/agenda/list/list.component').then(
        (m) => m.ListComponent
      ),
  },
];

@NgModule({
  imports: [RouterModule.forChild(agendaRoutes)],
  exports: [RouterModule],
})
export class AgendaRoutingModule {}
