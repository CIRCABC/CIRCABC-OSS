import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import {
  NotificationService,
  PasteNotificationsState,
} from 'app/core/generated/circabc';

/**
 * Group administration component that renders a reactive form for configuring
 * the "paste" notification settings of an interest group.
 *
 * The form exposes two toggles:
 * - `notifyPaste`: whether members are notified when content is pasted into the group.
 * - `notifyPasteAll`: whether the notification is sent to all members.
 *
 * On initialization it resolves the interest group id from the active route and
 * loads the current notification state from the backend
 * ({@link NotificationService}), pre-populating the form controls. Users can
 * persist changes via {@link PasteNotificationsComponent.save} or revert unsaved
 * edits via {@link PasteNotificationsComponent.cancel}.
 *
 * Key collaborators: {@link ActivatedRoute} (to obtain the group id),
 * {@link NotificationService} (to read/write the paste notification state) and
 * {@link FormBuilder} (to build the reactive form).
 */
@Component({
  selector: 'cbc-paste-notifications',
  templateUrl: './paste-notifications.component.html',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule, TranslocoModule],
})
export class PasteNotificationsComponent implements OnInit {
  /** Active route used to read the interest group id (`id`) route parameter. */
  private readonly route = inject(ActivatedRoute);
  /** Generated API client used to read and update the paste notification state. */
  private readonly notificationService = inject(NotificationService);
  /** Builder used to construct the reactive notification form. */
  private readonly formBuilder = inject(FormBuilder);
  /**
   * Change detector used to mark the view dirty after the reactive form is
   * patched from the asynchronous state load (OnPush does not observe reactive
   * form value changes driven by `patchValue` outside a user event).
   */
  private readonly cdr = inject(ChangeDetectorRef);

  /** Identifier of the interest group whose notification settings are edited. */
  public igId!: string;
  /** Whether a save operation is currently in progress (used to disable UI). */
  public readonly saving = signal(false);
  /** Current paste notification state loaded from the backend. */
  public pasteNotificationsState!: PasteNotificationsState;

  /**
   * Reactive form backing the two notification toggles
   * (`notifyPaste` and `notifyPasteAll`).
   */
  public pasteNotificationsForm!: FormGroup;

  /**
   * Angular lifecycle hook. Builds the reactive form with the `notifyPaste` and
   * `notifyPasteAll` controls and subscribes to route parameter changes to load
   * the notification state for the current interest group.
   */
  ngOnInit() {
    this.pasteNotificationsForm = this.formBuilder.group(
      {
        notifyPaste: [false],
        notifyPasteAll: [false],
      },
      {
        updateOn: 'change',
      }
    );

    this.route.params.subscribe(
      async (params) => await this.getNotificationState(params)
    );
  }

  /**
   * Extracts the interest group id from the resolved route parameters and
   * triggers loading of the current notification state.
   *
   * @param params Resolved route parameters; the `id` entry is used as the
   * interest group id.
   * @returns A promise that resolves once the notification state has been loaded.
   */
  private async getNotificationState(params: { [key: string]: string }) {
    this.igId = params.id;
    await this.getState();
  }

  /**
   * Fetches the current paste notification state for the active interest group
   * and patches the form controls to reflect the retrieved values.
   *
   * @returns A promise that resolves once the state has been fetched and the
   * form has been updated.
   */
  private async getState() {
    this.pasteNotificationsState =
      await this.notificationService.getPasteNotificationsAsync({
        id: this.igId,
      });
    this.pasteNotificationsForm.controls.notifyPaste.patchValue(
      this.pasteNotificationsState.pasteEnabled
    );
    this.pasteNotificationsForm.controls.notifyPasteAll.patchValue(
      this.pasteNotificationsState.pasteAllEnabled
    );
    // Form is patched from an async continuation; mark the view dirty so the
    // OnPush template reflects the loaded values.
    this.cdr.markForCheck();
  }

  /**
   * Persists the current form values by posting the notification status to the
   * backend. Toggles {@link PasteNotificationsComponent.saving} while the
   * request is in flight.
   *
   * @returns A promise that resolves once the notification status has been saved.
   */
  public async save() {
    this.saving.set(true);

    await this.notificationService.postPasteNotificationsAsync({
      id: this.igId,
      pasteEnable: this.pasteNotificationsForm.value.notifyPaste,
      pasteAllEnable: this.pasteNotificationsForm.value.notifyPasteAll,
    });

    this.saving.set(false);
  }

  /**
   * Discards unsaved changes by reloading the notification state from the
   * backend and resetting the form controls to their persisted values.
   *
   * @returns A promise that resolves once the state has been reloaded.
   */
  public async cancel() {
    await this.getState();
  }
}
