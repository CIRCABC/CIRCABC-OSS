import { NgModule } from '@angular/core';

import { AgendaRoutingModule } from 'app/group/agenda/agenda-routing.module';
import { AgendaComponent } from 'app/group/agenda/agenda.component';
import { CalendarComponent } from 'app/group/agenda/calendar/calendar.component';
import { CreateEventComponent } from 'app/group/agenda/create-event/create-event.component';
import { DayComponent } from 'app/group/agenda/day/day.component';
import { DeleteEventComponent } from 'app/group/agenda/delete-event/delete-event.component';
import { ListComponent } from 'app/group/agenda/list/list.component';
import { TimezoneSelectorComponent } from 'app/group/agenda/timezone/timezone-selector.component';
import { ViewEditDetailsEventComponent } from 'app/group/agenda/view-edit-details-event/view-edit-details-event.component';
import { WeekComponent } from 'app/group/agenda/week/week.component';
import { MaterialComponentsModule } from 'app/material-components/material-components.module';
import { PrimengComponentsModule } from 'app/primeng-components/primeng-components.module';
import { SharedModule } from 'app/shared/shared.module';

@NgModule({
  imports: [
    AgendaRoutingModule,
    PrimengComponentsModule,
    MaterialComponentsModule,
    SharedModule,
  ],
  exports: [DayComponent, WeekComponent],
  declarations: [
    AgendaComponent,
    CalendarComponent,
    CreateEventComponent,
    DayComponent,
    DeleteEventComponent,
    ListComponent,
    TimezoneSelectorComponent,
    ViewEditDetailsEventComponent,
    WeekComponent,
  ],
  providers: [],
})
export class AgendaModule {}
