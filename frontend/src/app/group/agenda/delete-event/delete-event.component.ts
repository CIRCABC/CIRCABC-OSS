import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  model,
  OnInit,
  output,
  signal,
} from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { TranslocoModule } from '@jsverse/transloco';
import { EventItemDefinition, EventsService } from 'app/core/generated/circabc';
import { ModalComponent } from 'app/shared/modal/modal.component';

/**
 * Modal component that confirms and performs the deletion of an agenda
 * event or meeting.
 *
 * It renders a confirmation dialog (via {@link ModalComponent}) containing a
 * reactive form that lets the user choose, for recurring events, whether to
 * delete only a single occurrence or the whole series. On confirmation it
 * delegates the actual deletion to the generated {@link EventsService} and
 * notifies its parent (typically the agenda/calendar view) so the view can be
 * refreshed.
 */
@Component({
  selector: 'cbc-delete-event',
  templateUrl: './delete-event.component.html',
  styleUrl: './delete-event.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ModalComponent, ReactiveFormsModule, TranslocoModule],
})
export class DeleteEventComponent implements OnInit {
  /** Generated API client used to perform the event/meeting deletion. */
  private readonly eventsService = inject(EventsService);

  /**
   * Two-way bound flag controlling the visibility of the confirmation modal.
   * `true` shows the dialog, `false` hides it.
   */
  public showModal = model(false);

  /** Emitted when the modal is closed/dismissed without further action. */
  public readonly modalHide = output();

  /**
   * Emitted after an event or meeting has been successfully deleted, so the
   * parent agenda/calendar view can redisplay itself.
   */
  public readonly eventMeetingDeleted = output();

  /** Required input carrying the event/meeting definition to be deleted. */
  public event = input.required<EventItemDefinition>();

  /**
   * Reactive form backing the dialog. Holds the `occurrenceSelection` control
   * used to decide, for recurring events, the scope of the deletion.
   * Initialised in {@link ngOnInit}.
   */
  public form!: FormGroup;

  /** Whether a lengthy operation is in progress; drives the spinner state. */
  // to enable/disable the spinner for lengthy operations
  public readonly processing = signal(false);

  /**
   * Angular lifecycle hook. Builds the reactive {@link form} with a default
   * `occurrenceSelection` of `'Single'`.
   */
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

  /**
   * Closes the confirmation dialog, resets the form to its default state and
   * emits {@link modalHide}.
   */
  public closePopupWindow(): void {
    this.showModal.set(false);
    this.modalHide.emit();
    this.form.patchValue({ occurrenceSelection: 'Single' });
    this.processing.set(false);
  }

  /**
   * Determines whether the current event is a one-off (non-recurring) event.
   *
   * @returns `true` if the event's occurrence rate indicates a single
   * occurrence, `false` if it recurs, or `undefined` when the event or its
   * occurrence rate is not available.
   */
  public isSingleEvent() {
    return this.event()?.occurrenceRate?.startsWith('OnlyOnce|');
  }

  /**
   * Deletes the current event/meeting via {@link EventsService}, using the
   * selected occurrence scope from the form. On success emits
   * {@link eventMeetingDeleted} and closes the dialog.
   *
   * @returns A promise that resolves once the deletion completes and the
   * dialog has been closed.
   */
  public async delete() {
    this.processing.set(true);

    // delete event/meeting
    await this.eventsService.deleteEventAsync({
      id: this.event().id as string,
      updateMode: this.form.value.occurrenceSelection,
    });

    // emit an event to signal that an event/meeting has been deleted
    // will be used by the agenda->calendar to redisplay the view
    this.eventMeetingDeleted.emit();

    // close form/wizard
    this.closePopupWindow();
  }
}
