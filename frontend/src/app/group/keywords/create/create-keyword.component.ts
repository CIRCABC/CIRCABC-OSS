import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  OnChanges,
  OnInit,
  output,
  SimpleChanges,
  signal,
} from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
} from '@angular/forms';

import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { KeywordDefinition, KeywordsService } from 'app/core/generated/circabc';
import { nonEmptyTitle } from 'app/core/validation.service';
import { ControlMessageComponent } from 'app/shared/control-message/control-message.component';
import { MultilingualInputComponent } from 'app/shared/input/multilingual-input.component';
import { ModalComponent } from 'app/shared/modal/modal.component';

/**
 * Modal-based component for creating and editing keyword definitions within an
 * interest group.
 *
 * Renders a modal dialog (via {@link ModalComponent}) containing a reactive form
 * with a multilingual title input. The component operates in two modes:
 * - **Create mode**: when no `keyword` input is provided, submitting the form
 *   posts a new {@link KeywordDefinition} for the parent interest group.
 * - **Edit mode**: when a `keyword` input is supplied, the form is pre-populated
 *   with its title and submitting updates the existing definition.
 *
 * On completion or cancellation, the outcome is reported to the parent through
 * the {@link CreateKeywordComponent.modalHide} output as an
 * {@link ActionEmitterResult}.
 *
 * Key collaborators: {@link KeywordsService} (REST calls to persist keyword
 * definitions) and {@link FormBuilder} (reactive form construction).
 */
@Component({
  selector: 'cbc-create-keyword',
  templateUrl: './create-keyword.component.html',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ModalComponent,
    ReactiveFormsModule,
    MultilingualInputComponent,
    ControlMessageComponent,
  ],
})
export class CreateKeywordComponent implements OnInit, OnChanges {
  /** Form builder used to construct the reactive keyword form. */
  private readonly fb = inject(FormBuilder);
  /** Generated API service used to create and update keyword definitions. */
  private readonly keywordsService = inject(KeywordsService);

  /**
   * Required input controlling the visibility of the underlying modal dialog.
   * When `true` the modal is shown; when `false` it is hidden.
   */
  readonly showModal = input.required<boolean>();
  /**
   * Required input holding the identifier of the parent interest group that the
   * keyword definition is created under.
   */
  readonly parentIgId = input.required<string>();

  /**
   * Optional input carrying the keyword definition to edit. When provided, the
   * component operates in edit mode and pre-populates the form; when absent, the
   * component operates in create mode.
   */
  readonly keyword = input<KeywordDefinition>();
  /**
   * Output emitted when the modal should be dismissed, carrying the result of
   * the create, update or cancel action.
   */
  public readonly modalHide = output<ActionEmitterResult>();

  /** Reactive form backing the keyword title input. */
  public createKeywordForm!: FormGroup;
  /**
   * Indicates whether a create or update request is in progress, used to guard
   * the UI (e.g. disabling submit) while the operation runs.
   */
  public readonly creating = signal(false);

  /**
   * Angular lifecycle hook. Initializes the reactive form with a `title` control
   * validated by the {@link nonEmptyTitle} validator.
   */
  ngOnInit() {
    this.createKeywordForm = this.fb.group({
      title: ['', [nonEmptyTitle]],
    });
  }

  /**
   * Angular lifecycle hook. When the `keyword` input changes to a defined value,
   * updates the form's title control to reflect the incoming keyword's title.
   *
   * @param changes The set of changed input properties for this cycle.
   */
  ngOnChanges(changes: SimpleChanges) {
    const chng = changes.keyword;
    if (chng?.currentValue) {
      this.createKeywordForm.controls.title.setValue(chng.currentValue.title);
    }
  }

  /**
   * Creates a new keyword definition for the parent interest group using the
   * current form title, emits a `CREATE_KEYWORD` success result through
   * {@link CreateKeywordComponent.modalHide}, then resets the form.
   *
   * @returns A promise that resolves once the keyword has been created and the
   * result emitted.
   */
  public async create() {
    this.creating.set(true);

    const kTmp: KeywordDefinition = {
      title: this.createKeywordForm.value.title,
    };

    await this.keywordsService.postKeywordDefinitionAsync({
      id: this.parentIgId(),
      keywordDefinition: kTmp,
    });

    const result: ActionEmitterResult = {};

    result.result = ActionResult.SUCCEED;
    result.type = ActionType.CREATE_KEYWORD;

    this.modalHide.emit(result);

    this.resetForm();
    this.creating.set(false);
  }

  /**
   * Resets the reactive form to a pristine, untouched state with an empty title.
   */
  private resetForm() {
    this.createKeywordForm.reset({ title: '' });
    this.createKeywordForm.controls.title.markAsUntouched();
    this.createKeywordForm.controls.title.markAsPristine();
  }

  /**
   * Updates the keyword definition currently held in the `keyword` input using
   * the form title. Does nothing when no keyword is present. Emits an
   * `UPDATE_KEYWORD` result through {@link CreateKeywordComponent.modalHide},
   * flagged as succeeded when the update returns a value and failed otherwise
   * (including when the keyword has no identifier).
   *
   * @returns A promise that resolves once the update attempt has completed and
   * the result emitted.
   */
  public async update() {
    const keyword = this.keyword();
    if (keyword) {
      this.creating.set(true);

      const kTmp: KeywordDefinition = {
        id: keyword.id,
        title: this.createKeywordForm.value.title,
      };

      const result: ActionEmitterResult = {};
      if (kTmp.id) {
        const res = await this.keywordsService.putKeywordDefinitionAsync({
          keywordId: kTmp.id,
          keywordDefinition: kTmp,
        });
        if (res === undefined) {
          result.result = ActionResult.FAILED;
        } else {
          result.result = ActionResult.SUCCEED;
          this.resetForm();
        }
      } else {
        result.result = ActionResult.FAILED;
      }

      result.type = ActionType.UPDATE_KEYWORD;
      this.modalHide.emit(result);
      this.creating.set(false);
    }
  }

  /**
   * Cancels the create/update wizard, resets the form and emits a canceled
   * result through {@link CreateKeywordComponent.modalHide}. The emitted action
   * type reflects the current mode based on whether a `keyword` input is present.
   */
  public cancelWizard(): void {
    const result: ActionEmitterResult = {};
    result.result = ActionResult.CANCELED;
    if (this.keyword() === undefined) {
      result.type = ActionType.UPDATE_KEYWORD;
    } else {
      result.type = ActionType.CREATE_KEYWORD;
    }
    this.resetForm();
    this.modalHide.emit(result);
  }

  /**
   * Convenience accessor for the form's `title` control.
   *
   * @returns The {@link AbstractControl} backing the keyword title input.
   */
  get titleControl(): AbstractControl {
    return this.createKeywordForm.controls.title;
  }
}
