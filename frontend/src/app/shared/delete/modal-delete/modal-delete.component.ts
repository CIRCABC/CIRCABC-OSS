import {
  ChangeDetectionStrategy,
  Component,
  input,
  model,
  output,
} from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';
import { ModalComponent } from 'app/shared/modal/modal.component';

/**
 * Reusable confirmation modal used to ask the user to confirm or cancel a
 * deletion action.
 *
 * The component renders a modal dialog (via {@link ModalComponent}) that
 * displays a localized title and explanatory text along with confirm and
 * cancel controls. It does not perform the deletion itself; instead it
 * signals the parent component's intent through the {@link deletionConfirmed}
 * and {@link deletionCanceled} outputs, leaving the actual removal logic to
 * the host.
 *
 * The dialog visibility is controlled by the two-way bindable
 * {@link showModal} model, so parents can both open the dialog and be kept in
 * sync when it closes.
 */
@Component({
  selector: 'cbc-modal-delete',
  templateUrl: './modal-delete.component.html',
  styleUrl: './modal-delete.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ModalComponent, TranslocoModule],
})
export class ModalDeleteComponent {
  /**
   * Two-way bindable flag controlling the visibility of the modal.
   * `true` shows the dialog, `false` hides it. Reset to `false` whenever the
   * user confirms or cancels the deletion.
   */
  public showModal = model<boolean>(false);
  /** Localized heading displayed at the top of the modal. */
  public readonly title = input('');
  /** Localized body text describing what will be deleted. */
  public readonly text = input('');
  /** Emitted when the user dismisses the dialog without confirming. */
  public readonly deletionCanceled = output();
  /** Emitted when the user confirms the deletion. */
  public readonly deletionConfirmed = output();

  /**
   * Closes the modal and notifies the parent that the deletion was cancelled.
   *
   * Sets {@link showModal} to `false` and emits {@link deletionCanceled}.
   */
  public closePopupWindow(): void {
    this.showModal.set(false);
    this.deletionCanceled.emit();
  }

  /**
   * Closes the modal and notifies the parent that the deletion was confirmed.
   *
   * Sets {@link showModal} to `false` and emits {@link deletionConfirmed}.
   *
   * @returns A promise that resolves once the confirmation has been signalled.
   */
  public async delete() {
    this.showModal.set(false);
    this.deletionConfirmed.emit();
  }
}
