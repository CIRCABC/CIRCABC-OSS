import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AgendaComponent } from 'app/group/agenda/agenda.component';
import { ListComponent } from 'app/group/agenda/list/list.component';
import { ViewEditDetailsEventComponent } from 'app/group/agenda/view-edit-details-event/view-edit-details-event.component';

const agendaRoutes: Routes = [
  { path: '', component: AgendaComponent },
  { path: ':eventId/details', component: ViewEditDetailsEventComponent },
  { path: 'list', component: ListComponent },
];

@NgModule({
  imports: [RouterModule.forChild(agendaRoutes)],
  exports: [RouterModule],
})
export class AgendaRoutingModule {}
