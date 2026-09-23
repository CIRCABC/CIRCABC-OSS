import {
  ChangeDetectionStrategy,
  Component,
  computed,
  effect,
  inject,
  input,
  linkedSignal,
  output,
  resource,
  signal,
} from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { TranslocoModule } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { HelpCategory, HelpService } from 'app/core/generated/circabc';
import { nonEmptyTitle } from 'app/core/validation.service';
import { MultilingualInputComponent } from 'app/shared/input/multilingual-input.component';
import { ModalComponent } from 'app/shared/modal/modal.component';

/**
 * Modal component (`cbc-add-help-category`) used to create a new help
 * category or edit an existing one.
 *
 * The component renders a modal dialog (via {@link ModalComponent})
 * containing a reactive form with a single multilingual title field
 * (rendered through {@link MultilingualInputComponent}). When a
 * `categoryId` is supplied the component switches to edit mode, loads the
 * existing {@link HelpCategory} and pre-fills the form; otherwise it
 * operates in creation mode.
 *
 * It collaborates with the generated {@link HelpService} to fetch, create
 * and update help categories, and notifies the parent component of
 * visibility changes and the outcome of create/update operations through
 * its outputs.
 */
@Component({
  selector: 'cbc-add-help-category',
  templateUrl: './add-help-category.component.html',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ModalComponent,
    ReactiveFormsModule,
    MultilingualInputComponent,
    TranslocoModule,
  ],
})
export class AddHelpCategoryComponent {
  /** Angular reactive-forms builder used to construct the category form. */
  private readonly fb = inject(FormBuilder);
  /** Generated API client used to read, create and update help categories. */
  private readonly helpService = inject(HelpService);

  /**
   * Input (aliased as `showModal`) controlling the initial visibility of
   * the modal dialog. Seeds the writable {@link showModal} signal.
   */
  // eslint-disable-next-line @angular-eslint/no-input-rename
  readonly showModalInput = input(false, { alias: 'showModal' });
  /**
   * Writable signal driving the modal visibility. Initialised from
   * {@link showModalInput} and updated internally when the dialog is
   * confirmed or cancelled.
   */
  readonly showModal = linkedSignal(this.showModalInput);
  /**
   * Optional identifier of the help category to edit. When provided the
   * component runs in edit mode and loads the corresponding category.
   */
  readonly categoryId = input<string>();
  /** Emits the current modal visibility whenever it changes, for two-way `showModal` binding. */
  readonly showModalChange = output<boolean>();
  /** Emits the result of a category creation attempt. */
  readonly categoryCreated = output<ActionEmitterResult>();
  /** Emits the result of a category update attempt. */
  readonly categoryUpdated = output<ActionEmitterResult>();

  /** Whether a create/update request is currently in progress. */
  public readonly creating = signal(false);
  /**
   * Reactive form holding the category title. Built eagerly (rather than in
   * `ngOnInit`) since it is bound in the template from the first render.
   */
  public readonly newCategoryForm: FormGroup = this.fb.group(
    {
      title: ['', nonEmptyTitle],
    },
    {
      updateOn: 'change',
    }
  );
  /**
   * Loads the existing {@link HelpCategory} identified by {@link categoryId}.
   * Stays `idle` (loader not called) while no category id is set, i.e. in
   * creation mode.
   */
  private readonly categoryResource = resource({
    params: () => this.categoryId() || undefined,
    loader: ({ params: id }) => this.helpService.getHelpCategoryAsync({ id }),
  });
  /** Whether the component is editing an existing category (`true`) or creating a new one (`false`). */
  public readonly editMode = computed(() => !!this.categoryId());
  /** The existing help category being edited, populated in edit mode. */
  public readonly categoryToEdit = computed(() =>
    this.categoryResource.hasValue() ? this.categoryResource.value() : undefined
  );
  /** Whether the current form state is valid. */
  public readonly isValid = signal(false);

  constructor() {
    this.newCategoryForm.controls.title.valueChanges.subscribe((_value) => {
      this.computeValidity();
    });

    // Pre-fill the form with the loaded category's title once it is
    // available. Syncs the resource's signal state into the (non-signal)
    // reactive form API, so an `effect` is the appropriate tool here.
    effect(() => {
      const categoryToEdit = this.categoryToEdit();
      if (categoryToEdit) {
        this.newCategoryForm.controls.title.patchValue(categoryToEdit.title);
      }
    });
  }

  /**
   * Creates a new help category from the current form value.
   *
   * Sends the title to {@link HelpService.createHelpCategory}. On success
   * the modal is closed, the form is reset and the visibility change is
   * emitted. Failures are caught and logged; in all cases a
   * {@link categoryCreated} event carrying the {@link ActionEmitterResult}
   * (with the appropriate success/failure state) is emitted.
   *
   * @returns A promise that resolves once the creation attempt has finished.
   */
  public async createCategory() {
    this.creating.set(true);
    const result: ActionEmitterResult = {};
    result.type = ActionType.ADD_HELP_SECTION;
    result.result = ActionResult.FAILED;

    try {
      const body: HelpCategory = {
        title: this.newCategoryForm.value.title,
      };

      await this.helpService.createHelpCategoryAsync({ helpCategory: body });
      result.result = ActionResult.SUCCEED;
      this.showModal.set(false);
      this.newCategoryForm.reset();

      this.showModalChange.emit(this.showModal());
    } catch (error) {
      console.error(error);
    }
    this.creating.set(false);
    this.categoryCreated.emit(result);
  }

  /**
   * Updates the existing help category (identified by {@link categoryId})
   * with the current form value.
   *
   * Sends the title to {@link HelpService.updateHelpCategory}. On success
   * the modal is closed, the form is reset and the visibility change is
   * emitted. When no `categoryId` is set the update is skipped. Failures are
   * caught and logged; in all cases a {@link categoryUpdated} event carrying
   * the {@link ActionEmitterResult} is emitted.
   *
   * @returns A promise that resolves once the update attempt has finished.
   */
  public async updateCategory() {
    this.creating.set(true);
    const result: ActionEmitterResult = {};
    result.type = ActionType.UPDATE_HELP_SECTION;
    result.result = ActionResult.FAILED;

    try {
      const body: HelpCategory = {
        title: this.newCategoryForm.value.title,
      };

      const categoryId = this.categoryId();
      if (categoryId) {
        await this.helpService.updateHelpCategoryAsync({
          id: categoryId,
          helpCategory: body,
        });
        result.result = ActionResult.SUCCEED;
        this.showModal.set(false);
        this.newCategoryForm.reset();

        this.showModalChange.emit(this.showModal());
      }
    } catch (error) {
      console.error(error);
    }
    this.creating.set(false);
    this.categoryUpdated.emit(result);
  }

  /**
   * Closes the modal without persisting changes. Resets the form when not
   * in edit mode and emits the resulting visibility change.
   */
  public cancel() {
    this.showModal.set(false);
    if (!this.editMode()) {
      this.newCategoryForm.reset();
    }

    this.showModalChange.emit(this.showModal());
  }

  /**
   * Recomputes {@link isValid} from the reactive form's validity state.
   */
  private computeValidity() {
    this.isValid.set(this.newCategoryForm.valid);
  }
}
