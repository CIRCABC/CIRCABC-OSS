import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { CalendarModule } from 'primeng/calendar';
import { DropdownModule } from 'primeng/dropdown';
import { EditorModule } from 'primeng/editor';
import { InputSwitchModule } from 'primeng/inputswitch';
import { SelectButtonModule } from 'primeng/selectbutton';
import { TooltipModule } from 'primeng/tooltip';
import { AccordionModule } from 'primeng/accordion';

@NgModule({
  imports: [
    CommonModule,
    CalendarModule,
    EditorModule,
    InputSwitchModule,
    DropdownModule,
    SelectButtonModule,
    TooltipModule,
    AccordionModule,
  ],
  exports: [
    CalendarModule,
    EditorModule,
    InputSwitchModule,
    DropdownModule,
    SelectButtonModule,
    TooltipModule,
    AccordionModule,
  ],
  declarations: [],
})
export class PrimengComponentsModule {}
