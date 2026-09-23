import {
  ChangeDetectionStrategy,
  Component,
  input,
  linkedSignal,
  output,
} from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';

/**
 * Standalone component that renders an inline delete affordance with a
 * built-in confirmation step.
 *
 * It displays a delete trigger (as text and/or an icon/image) and, when
 * activated, switches into a confirmation state asking the user to confirm
 * or cancel the deletion. The trigger, confirmation prompt and states are
 * driven by the component's signal inputs, while consumers react to the
 * outcome through the emitted outputs.
 *
 * The component manages two pieces of internal UI state — whether a
 * confirmation is currently being requested (`mustConfirm`) and whether a
 * deletion is in progress (`deleting`) — each backed by a `linkedSignal`
 * seeded from the corresponding input so the parent can both provide an
 * initial value and observe subsequent changes.
 *
 * Labels are localized through Transloco.
 */
@Component({
  selector: 'cbc-inline-delete',
  templateUrl: './inline-delete.component.html',
  styleUrl: './inline-delete.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [TranslocoModule],
})
export class InlineDeleteComponent {
  /**
   * Emitted when the user confirms the deletion. Consumers should perform
   * the actual delete operation in response to this event.
   */
  readonly deletionConfirmed = output();

  /**
   * Input (aliased as `deleting`) providing the initial "deletion in
   * progress" state. Used to seed the `deleting` linked signal.
   */
  // eslint-disable-next-line @angular-eslint/no-input-rename
  readonly deletingInput = input(false, { alias: 'deleting' });
  /**
   * Reactive state indicating whether a deletion is currently in progress.
   * Linked to {@link deletingInput} so it starts from the supplied value
   * and can be updated internally (e.g. after confirmation).
   */
  readonly deleting = linkedSignal(this.deletingInput);
  /** Emitted whenever the internal {@link deleting} state changes. */
  readonly deletingChange = output<boolean>();

  /**
   * Input (aliased as `mustConfirm`) providing the initial "awaiting
   * confirmation" state. Used to seed the `mustConfirm` linked signal.
   */
  // eslint-disable-next-line @angular-eslint/no-input-rename
  readonly mustConfirmInput = input(false, { alias: 'mustConfirm' });
  /**
   * Reactive state indicating whether the confirmation prompt is currently
   * shown. Linked to {@link mustConfirmInput} so it starts from the
   * supplied value and can be toggled internally.
   */
  readonly mustConfirm = linkedSignal(this.mustConfirmInput);
  /** Emitted whenever the internal {@link mustConfirm} state changes. */
  readonly mustConfirmChange = output<boolean>();

  /** When `true`, render an icon/image as part of the delete trigger. */
  readonly useIcon = input(false);

  /** When `true`, render the textual label as part of the delete trigger. */
  readonly useText = input(true);

  /** URL/path of the image to display when {@link useIcon} is enabled. */
  readonly image = input('');
  /** Height and width, in pixels, applied to the {@link image}. */
  readonly imageHeightWidth = input(12);

  /**
   * When `true`, confirming a deletion switches the component into the
   * in-progress ({@link deleting}) state inline; when `false`, confirming
   * simply dismisses the confirmation prompt.
   */
  readonly deleteInline = input(true);

  /** Transloco translation key for the delete trigger label. */
  readonly deleteLabel = input('label.delete');

  /**
   * Enters the confirmation state, requesting the user to confirm the
   * deletion, and notifies listeners of the change.
   */
  public deleteAskConfirmation() {
    this.mustConfirm.set(true);
    this.mustConfirmChange.emit(this.mustConfirm());
  }

  /**
   * Confirms the deletion.
   *
   * If {@link deleteInline} is enabled the component moves into the
   * in-progress ({@link deleting}) state; otherwise it dismisses the
   * confirmation prompt. In both cases {@link deletionConfirmed} is emitted
   * so the parent can carry out the deletion, followed by a
   * {@link deletingChange} notification with the current progress state.
   */
  public confirm() {
    if (this.deleteInline()) {
      this.deleting.set(true);
    } else {
      this.mustConfirm.set(false);
    }
    this.deletionConfirmed.emit();
    this.deletingChange.emit(this.deleting());
  }

  /**
   * Cancels the pending deletion, leaving the confirmation state and
   * notifying listeners of the change.
   */
  public cancel() {
    this.mustConfirm.set(false);
    this.mustConfirmChange.emit(this.mustConfirm());
  }
}
