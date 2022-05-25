import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { EventItemDefinition, EventsService } from 'app/core/generated/circabc';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-delete-event',
  templateUrl: './delete-event.component.html',
  styleUrls: ['./delete-event.component.scss'],
  preserveWhitespaces: true,
})
export class DeleteEventComponent implements OnInit {
  @Input()
  public showModal = false;
  @Output()
  public readonly modalHide = new EventEmitter();
  @Output()
  public readonly eventMeetingDeleted = new EventEmitter();
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
    return (
      this.event !== undefined &&
      this.event.occurrenceRate !== undefined &&
      this.event.occurrenceRate.startsWith('OnlyOnce|')
    );
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
