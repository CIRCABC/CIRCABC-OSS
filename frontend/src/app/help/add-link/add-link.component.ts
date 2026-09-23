import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  linkedSignal,
  OnChanges,
  OnInit,
  output,
  SimpleChanges,
  signal,
} from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { TranslocoModule } from '@jsverse/transloco';
import { ActionEmitterResult, ActionResult } from 'app/action-result';
import { HelpLink, HelpService } from 'app/core/generated/circabc';
import { urlValidator } from 'app/core/validation.service';
import { ControlMessageComponent } from 'app/shared/control-message/control-message.component';
import { MultilingualInputComponent } from 'app/shared/input/multilingual-input.component';
import { ModalComponent } from 'app/shared/modal/modal.component';

/**
 * Modal-based component that renders a reactive form for creating a new help
 * link or editing an existing one.
 *
 * The component displays a {@link ModalComponent} containing a multilingual
 * title input and an href input (validated as a URL). Depending on whether a
 * `linkId` is provided it operates in one of two modes:
 * - **create mode** — submits a brand new {@link HelpLink} via
 *   {@link HelpService.createHelpLink}.
 * - **edit mode** — loads the existing link through
 *   {@link HelpService.getHelpLink}, pre-fills the form and persists changes
 *   via {@link HelpService.updateHelpLink}.
 *
 * Key collaborators are the Angular {@link FormBuilder} (for building the
 * reactive form) and the generated {@link HelpService} (for the backend CRUD
 * operations). Outcomes are communicated back to the parent through the
 * component's outputs.
 */
@Component({
  selector: 'cbc-add-link',
  templateUrl: './add-link.component.html',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ModalComponent,
    ReactiveFormsModule,
    MultilingualInputComponent,
    ControlMessageComponent,
    TranslocoModule,
  ],
})
export class AddLinkComponent implements OnInit, OnChanges {
  /** Reactive forms builder used to construct {@link newLinkForm}. */
  private readonly fb = inject(FormBuilder);
  /** Generated backend client for help link CRUD operations. */
  private readonly helpService = inject(HelpService);

  /**
   * Input (aliased as `showModal`) controlling whether the modal is displayed.
   * Feeds the writable {@link showModal} signal.
   */
  // eslint-disable-next-line @angular-eslint/no-input-rename
  readonly showModalInput = input(false, { alias: 'showModal' });
  /**
   * Writable signal mirroring {@link showModalInput}; used internally to close
   * the modal (e.g. from {@link cancel}) while staying in sync with the input.
   */
  readonly showModal = linkedSignal(this.showModalInput);
  /**
   * Required input (aliased as `linkId`) holding the identifier of the help
   * link to edit, or `undefined` when creating a new link. Feeds the writable
   * {@link linkId} signal.
   */
  readonly linkIdInput = input.required<string | undefined>({
    // eslint-disable-next-line @angular-eslint/no-input-rename
    alias: 'linkId',
  });
  /**
   * Writable signal mirroring {@link linkIdInput}; reset to `undefined` after a
   * successful update or cancellation.
   */
  readonly linkId = linkedSignal(this.linkIdInput);
  /** Emits the modal visibility state when it changes (two-way `showModal`). */
  readonly showModalChange = output<boolean>();
  /** Emits when the edited link id is cleared (two-way `linkId`). */
  readonly linkIdChange = output();
  /** Emits the result once a link has been created or updated. */
  readonly linkCreated = output<ActionEmitterResult>();
  /** Emits the result once a link has been updated. */
  readonly linkUpdated = output<ActionEmitterResult>();

  /** `true` while a create/update request is in flight, used to disable UI. */
  public readonly creating = signal(false);
  /** Reactive form holding the `title` and `href` controls. */
  public newLinkForm!: FormGroup;
  /** `true` when the component is editing an existing link. */
  public readonly editMode = signal(false);
  /** The link currently being edited, or `undefined` in create mode. */
  public linkToEdit!: HelpLink | undefined;
  /** Cached validity of {@link newLinkForm}, recomputed on value changes. */
  public readonly isValid = signal(false);

  /**
   * Angular lifecycle hook. Builds {@link newLinkForm} with required `title`
   * and URL-validated `href` controls and subscribes to value changes to keep
   * {@link isValid} up to date.
   */
  ngOnInit() {
    this.newLinkForm = this.fb.group({
      title: ['', Validators.required],
      href: ['', [Validators.required, urlValidator]],
    });

    this.newLinkForm.valueChanges.subscribe((_value) => {
      this.computeValidity();
    });
  }

  /**
   * Angular lifecycle hook. When the `linkId` input changes to a defined value
   * it fetches the corresponding {@link HelpLink}, patches the form with its
   * values and switches the component into edit mode.
   *
   * @param changes - Map of input changes provided by Angular.
   * @returns A promise that resolves once the link (if any) has been loaded.
   */
  ngOnChanges(changes: SimpleChanges) {
    void this.handleChanges(changes);
  }

  private async handleChanges(changes: SimpleChanges) {
    if (changes.linkId) {
      if (changes.linkId.currentValue) {
        this.linkToEdit = await this.helpService.getHelpLinkAsync(
          changes.linkId.currentValue
        );
        this.newLinkForm.patchValue(this.linkToEdit);
        this.editMode.set(true);
      }
    }
  }

  /**
   * Persists changes to the currently edited link. Sends the updated
   * {@link HelpLink} to {@link HelpService.updateHelpLink}, emits
   * {@link linkCreated} on success, then resets the form and clears edit state.
   * Does nothing if no {@link linkId} is set. Errors are caught and logged.
   *
   * @returns A promise that resolves once the update attempt has completed.
   */
  public async updateLink() {
    this.creating.set(true);

    try {
      const linkId = this.linkId();
      if (linkId) {
        const body: HelpLink = this.newLinkForm.value;
        body.id = linkId;

        await this.helpService.updateHelpLinkAsync({
          id: body.id,
          helpLink: body,
        });

        this.linkCreated.emit({ result: ActionResult.SUCCEED });
        this.newLinkForm.reset();
        this.editMode.set(false);
        this.linkId.set(undefined);
      }
    } catch (error) {
      console.error(error);
    }

    this.creating.set(false);
  }

  /**
   * Creates a new help link from the current form value via
   * {@link HelpService.createHelpLink}, emits {@link linkCreated} on success
   * and resets the form. Errors are caught and logged.
   *
   * @returns A promise that resolves once the creation attempt has completed.
   */
  public async createLink() {
    this.creating.set(true);

    try {
      await this.helpService.createHelpLinkAsync({
        helpLink: this.newLinkForm.value,
      });
      this.linkCreated.emit({ result: ActionResult.SUCCEED });
      this.newLinkForm.reset();
    } catch (error) {
      console.error(error);
    }

    this.creating.set(false);
  }

  /**
   * Cancels the current create/edit operation: hides the modal, resets the
   * form and edit state, clears the selected {@link linkId} and notifies the
   * parent via {@link linkIdChange} and {@link showModalChange}.
   */
  public cancel() {
    this.showModal.set(false);
    this.newLinkForm.reset();
    this.linkToEdit = undefined;
    this.editMode.set(false);
    this.linkId.set(undefined);
    this.linkIdChange.emit();
    this.showModalChange.emit(this.showModal());
  }

  /**
   * Convenience accessor for the `href` form control.
   *
   * @returns The `href` {@link AbstractControl} of {@link newLinkForm}.
   */
  get hrefControl() {
    return this.newLinkForm.controls.href;
  }

  /**
   * Recomputes {@link isValid} from the current validity of
   * {@link newLinkForm}, defaulting to `false` when the form is not yet built.
   */
  private computeValidity() {
    if (this.newLinkForm) {
      this.isValid.set(this.newLinkForm.valid);
    } else {
      this.isValid.set(false);
    }
  }
}
