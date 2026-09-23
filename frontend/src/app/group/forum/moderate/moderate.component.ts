import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  model,
  OnChanges,
  output,
  signal,
} from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { ForumService, Node as ModelNode } from 'app/core/generated/circabc';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';

/**
 * Modal component that lets a moderator enable or disable moderation for a
 * forum node and decide whether existing pending posts should be accepted.
 *
 * Rendered inside a modal dialog (with a {@link SpinnerComponent} shown while
 * the change is being persisted), it exposes toggles for the moderation state
 * and post-validation option, and delegates persistence to the
 * {@link ForumService}. When the user confirms or cancels, it closes the modal
 * and notifies the parent through the {@link ModerateComponent.modalHide}
 * output.
 *
 * @implements OnChanges — recomputes the local moderation flag whenever the
 * bound {@link ModerateComponent.forum} changes.
 */
@Component({
  selector: 'cbc-moderate-forum',
  templateUrl: './moderate.component.html',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [SpinnerComponent, TranslocoModule],
})
export class ModerateComponent implements OnChanges {
  /** Client used to persist the forum moderation settings on the backend. */
  private readonly forumService = inject(ForumService);

  /**
   * Required input: the forum node whose moderation settings are being edited.
   * Its `properties.ismoderated` value seeds the initial moderation state.
   */
  readonly forum = input.required<ModelNode>();
  /**
   * Two-way bindable flag controlling the visibility of the moderation modal.
   * Set to `false` when the action is accepted or cancelled to close the modal.
   */
  readonly showModal = model<boolean>(false);
  /**
   * Emits the outcome of the moderation action (succeeded or cancelled) so the
   * parent component can react once the modal is dismissed.
   */
  readonly modalHide = output<ActionEmitterResult>();

  /** True while the moderation change is being submitted to the backend. */
  public readonly executing = signal(false);

  /** Current (possibly unsaved) state of whether the forum is moderated. */
  public moderationEnabled = false;
  /** Whether already-pending posts should be validated/accepted on save. */
  public acceptPostValidation = false;

  /**
   * Angular lifecycle hook. Re-initialises {@link moderationEnabled} from the
   * current forum node whenever its bound inputs change.
   */
  ngOnChanges() {
    this.moderationEnabled = this.forumModerated();
  }

  /**
   * Reads the moderation state from the bound forum node's properties.
   *
   * @returns `true` if the forum's `ismoderated` property equals the string
   * `'true'`; otherwise `false` (including when properties are undefined).
   */
  public forumModerated(): boolean {
    const forum = this.forum();
    if (forum.properties !== undefined) {
      return forum.properties.ismoderated === 'true';
    }
    return false;
  }

  /** Toggles the local moderation-enabled flag between on and off. */
  public toggleModeration() {
    this.moderationEnabled = !this.moderationEnabled;
  }

  /** Toggles whether pending posts should be validated/accepted on save. */
  public togglePostValidation() {
    this.acceptPostValidation = !this.acceptPostValidation;
  }

  /**
   * Persists the current moderation settings for the forum, updates the local
   * node's `ismoderated` property, closes the modal and emits a successful
   * {@link ActionEmitterResult}.
   *
   * @returns A promise that resolves once the moderation update has been sent
   * and the modal has been closed.
   */
  public async accept() {
    this.executing.set(true);
    const forum = this.forum();
    if (forum.id && forum.properties) {
      await this.forumService.putModerationAsync({
        id: forum.id,
        enable: this.moderationEnabled,
        acceptAll: this.acceptPostValidation,
      });
      forum.properties.ismoderated = this.moderationEnabled ? 'true' : 'false';
    }

    this.showModal.set(false);

    const result: ActionEmitterResult = {};
    result.result = ActionResult.SUCCEED;
    result.type = ActionType.MODERATE_FORUM;
    this.modalHide.emit(result);

    this.executing.set(false);
  }

  /**
   * Dismisses the modal without saving and emits a cancelled
   * {@link ActionEmitterResult}.
   *
   * @param _action The originating action identifier (unused).
   */
  public cancel(_action: string): void {
    this.showModal.set(false);
    const result: ActionEmitterResult = {};
    result.result = ActionResult.CANCELED;
    result.type = ActionType.MODERATE_FORUM;
    this.modalHide.emit(result);
  }
}
