import {
  ChangeDetectionStrategy,
  Component,
  effect,
  inject,
  input,
  output,
  resource,
} from '@angular/core';

import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { TranslocoModule } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import {
  InformationPage,
  InformationService,
} from 'app/core/generated/circabc';
import { ModalComponent } from 'app/shared/modal/modal.component';

/**
 * Modal component that lets an interest group administrator configure the
 * settings of the group's Information service.
 *
 * It renders a modal dialog (via {@link ModalComponent}) containing a reactive
 * form with the information page configuration options — currently the
 * `displayOldInformation` toggle. It reactively loads the existing
 * {@link InformationPage} definition for the current group through the
 * {@link InformationService} whenever `groupId` is set, and on save it
 * persists the updated definition back to the backend. Opening/closing of the
 * modal is driven by the host component through the `showModal` input and the
 * `modalHide` output.
 *
 * Key collaborators:
 * - {@link InformationService}: fetches and persists the information page
 *   definitions.
 * - {@link FormBuilder}: builds the reactive configuration form.
 */
@Component({
  selector: 'cbc-configure-information',
  templateUrl: './configure-information.component.html',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ModalComponent, ReactiveFormsModule, TranslocoModule],
})
export class ConfigureInformationComponent {
  /** Service used to read and write the group's information page definitions. */
  private readonly informationService = inject(InformationService);
  /** Factory used to build the reactive configuration form. */
  private readonly fb = inject(FormBuilder);

  /** Controls the visibility of the configuration modal dialog. */
  readonly showModal = input(false);
  /** Identifier of the interest group whose information settings are configured. */
  readonly groupId = input.required<string>();
  /**
   * Emitted when the modal should be closed, carrying the outcome of the
   * cancel or save action (success, failure or cancellation).
   */
  readonly modalHide = output<ActionEmitterResult>();

  /**
   * Loads the {@link InformationPage} definition for the current group. Idle
   * (loader not called) while `groupId` is empty, mirroring the previous
   * `if (groupId)` guard.
   */
  private readonly informationDefinitionsResource = resource<
    InformationPage,
    string | undefined
  >({
    params: () => this.groupId() || undefined,
    loader: ({ params: id }) =>
      this.informationService.getInformationDefinitionsAsync({ id }),
  });

  /** The information page definition loaded from and saved to the backend. */
  public readonly infPage = this.informationDefinitionsResource.value;

  /** Indicates whether an asynchronous operation is currently in progress. */
  public processing = false;
  /** Reactive form holding the editable information configuration values. */
  public configurationForm: FormGroup = this.fb.group({
    displayOldInformation: [false],
  });

  constructor() {
    // Syncs the form's `displayOldInformation` control to the imperative
    // FormGroup API once the InformationPage definition has loaded.
    effect(() => {
      const infPage = this.infPage();
      if (infPage) {
        this.configurationForm.patchValue({
          displayOldInformation: infPage.displayOldInformation ?? false,
        });
      }
    });
  }

  /**
   * Cancels the configuration: resets the form back to the currently persisted
   * `displayOldInformation` value and emits a {@link ActionResult.CANCELED}
   * result through {@link modalHide} to close the modal without saving.
   */
  public cancel() {
    const res: ActionEmitterResult = {};
    res.result = ActionResult.CANCELED;
    res.type = ActionType.UPDATE_INFORMATION_CONFIGURATION;
    this.configurationForm.patchValue({
      displayOldInformation: this.infPage()?.displayOldInformation ?? false,
    });
    this.modalHide.emit(res);
  }

  /**
   * Persists the current form values to the backend. Applies the form's
   * `displayOldInformation` value onto the loaded {@link InformationPage} and
   * saves it via {@link InformationService}. Emits a
   * {@link ActionResult.SUCCEED} or {@link ActionResult.FAILED} result through
   * {@link modalHide} depending on the outcome.
   *
   * @returns A promise that resolves once the save attempt has completed and
   * the result has been emitted.
   */
  public async save() {
    const res: ActionEmitterResult = {};
    res.type = ActionType.UPDATE_INFORMATION_CONFIGURATION;
    try {
      const body = this.infPage();
      if (!body) {
        throw new Error('information definitions not loaded');
      }
      body.displayOldInformation =
        this.configurationForm.value.displayOldInformation;
      await this.informationService.putInformationDefinitionsAsync({
        id: this.groupId(),
        informationPage: body,
      });
      res.result = ActionResult.SUCCEED;
    } catch (error) {
      console.error(error);
      console.error('impossible to save the configuration');
      res.result = ActionResult.FAILED;
    }

    this.modalHide.emit(res);
  }

  /**
   * Returns the current value of the `displayOldInformation` control.
   *
   * @returns The form's `displayOldInformation` value, or `false` when the
   * form has not yet been initialized.
   */
  public getDisplayValue(): boolean {
    if (this.configurationForm) {
      return this.configurationForm.value.displayOldInformation;
    }
    return false;
  }
}
