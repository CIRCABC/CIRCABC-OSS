import { Component, Input, OnInit, output } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { TranslocoModule } from '@jsverse/transloco';
import { EventItemDefinition, EventsService } from 'app/core/generated/circabc';
import { ModalComponent } from 'app/shared/modal/modal.component';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-delete-event',
  templateUrl: './delete-event.component.html',
  styleUrl: './delete-event.component.scss',
  preserveWhitespaces: true,
  imports: [ModalComponent, ReactiveFormsModule, TranslocoModule],
})
export class DeleteEventComponent implements OnInit {
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  public showModal = false;
  public readonly modalHide = output();
  public readonly eventMeetingDeleted = output();
  // TODO: Skipped for migration because:
  //  This input is used in a control flow expression (e.g. `@if` or `*ngIf`)
  //  and migrating would break narrowing currently.
  @Input()
  public event!: EventItemDefinition;
  public form!: FormGroup;

  // to enable/disable the spinner for lengthy operations
  public processing = false;

  public constructor(private eventsService: EventsService) {}

  public ngOnInit(): void {
    this.form = new FormGroup(
      {
        occurrenceSelection: new FormControl('Single'),
      },
      {
        updateOn: 'change',
      }
    );
  }

  public closePopupWindow(): void {
    this.showModal = false;
    this.modalHide.emit();
    this.form.patchValue({ occurrenceSelection: 'Single' });
    this.processing = false;
  }

  public isSingleEvent() {
    return this.event?.occurrenceRate?.startsWith('OnlyOnce|');
  }

  public async delete() {
    this.processing = true;

    // delete event/meeting
    await firstValueFrom(
      this.eventsService.deleteEvent(
        this.event.id as string,
        this.form.value.occurrenceSelection
      )
    );

    // emit an event to signal that an event/meeting has been deleted
    // will be used by the agenda->calendar to redisplay the view
    this.eventMeetingDeleted.emit();

    // close form/wizard
    this.closePopupWindow();
  }
}
