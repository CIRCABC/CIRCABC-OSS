import {
  ChangeDetectionStrategy,
  Component,
  computed,
  effect,
  inject,
  input,
  linkedSignal,
  OnInit,
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
import { HelpArticle, HelpService } from 'app/core/generated/circabc';
import { nonEmptyTitle } from 'app/core/validation.service';
import { MultilingualInputComponent } from 'app/shared/input/multilingual-input.component';
import {
  LanguageCodeName,
  supportedLanguages,
} from 'app/shared/langs/supported-langs';
import { ModalComponent } from 'app/shared/modal/modal.component';
import { RichTextEditorComponent } from 'app/shared/rich-text-editor/rich-text-editor.component';

/**
 * Modal-based form component for creating and editing multilingual help articles.
 *
 * Rendered as a `cbc-add-help-article` element, this component displays a modal
 * ({@link ModalComponent}) containing a reactive form with a multilingual title
 * input ({@link MultilingualInputComponent}) and a rich text editor
 * ({@link RichTextEditorComponent}) for the per-language article content.
 *
 * The component operates in two modes:
 * - Create mode: when only a {@link AddHelpArticleComponent.categoryId} is provided,
 *   a new article is created for that category via {@link HelpService.createCategoryArticle}.
 * - Edit mode: when an {@link AddHelpArticleComponent.articleId} is provided, the existing
 *   article is loaded and updated via {@link HelpService.updateHelpArticle}.
 *
 * Key collaborators: {@link FormBuilder} for building the reactive form and
 * {@link HelpService} for the backend help-article operations.
 */
@Component({
  selector: 'cbc-add-help-article',
  templateUrl: './add-help-article.component.html',
  styleUrl: './add-help-article.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ModalComponent,
    ReactiveFormsModule,
    MultilingualInputComponent,
    RichTextEditorComponent,
    TranslocoModule,
  ],
})
export class AddHelpArticleComponent implements OnInit {
  /** Angular reactive-forms builder used to construct the article form. */
  private readonly fb = inject(FormBuilder);
  /** Generated CIRCABC help API client used to read, create and update help articles. */
  private readonly helpService = inject(HelpService);

  /**
   * Input (aliased as `showModal`) controlling the initial visibility of the modal.
   * Its value seeds the writable {@link AddHelpArticleComponent.showModal} signal.
   */
  // eslint-disable-next-line @angular-eslint/no-input-rename
  readonly showModalInput = input(false, { alias: 'showModal' });
  /**
   * Writable signal driving the modal's visibility, initialised from
   * {@link AddHelpArticleComponent.showModalInput} and toggled locally as the user
   * saves or cancels.
   */
  readonly showModal = linkedSignal(this.showModalInput);
  /** Input holding the identifier of the category the new article belongs to (create mode). */
  readonly categoryId = input<string>();
  /** Input holding the identifier of the article to edit; when set, the component enters edit mode. */
  readonly articleId = input<string>();
  /** Output emitting the modal's visibility state whenever it changes (for two-way `showModal` binding). */
  readonly showModalChange = output<boolean>();
  /** Output emitted with the result of a create operation once an article has been created. */
  readonly articleCreated = output<ActionEmitterResult>();
  /** Output emitted with the result of an update operation once an article has been updated. */
  readonly articleUpdated = output<ActionEmitterResult>();

  /** Whether a create/update request is currently in progress (used to disable the form/actions). */
  public creating = signal(false);
  /** Reactive form holding the article `title`, the currently selected language `currentLang` and its `content`. */
  public newArticlForm!: FormGroup;
  /** Map of language code to the article's HTML content for that language. */
  public model!: { [key: string]: string };
  /** List of languages offered by the multilingual inputs. */
  public availableLangs!: LanguageCodeName[];
  /** Whether the form currently holds valid data (valid title and at least one non-empty content). */
  public isValid = signal(false);
  /** True when the component is editing an existing article rather than creating a new one. */
  public readonly editMode = computed(() => this.articleId() !== undefined);
  /** The article being edited, loaded reactively from the backend in edit mode. */
  public articleToEdit!: HelpArticle;

  /**
   * Resource that loads the help article to edit whenever the {@link articleId}
   * input is set. When no id is provided the loader does not run.
   */
  private readonly articleResource = resource({
    params: () => this.articleId(),
    loader: ({ params }) =>
      this.helpService.getHelpArticleAsync({ id: params }),
  });

  constructor() {
    // When the edited article resolves (initial load or reload), patch the
    // form title and content model and refresh the editor for the active
    // language. Guarded until the form has been built in ngOnInit.
    effect(() => {
      const article = this.articleResource.value();
      if (!(article && this.newArticlForm)) {
        return;
      }
      this.articleToEdit = article;
      this.newArticlForm.controls.title.patchValue(article.title);
      if (article.content) {
        this.model = article.content;
      }
      this.switchLang(this.newArticlForm.value.currentLang);
    });
  }

  /**
   * Angular lifecycle hook that initialises the reactive form, wires up
   * value-change subscriptions (switching the editor language and recomputing
   * validity) and seeds the content model and available languages. In edit
   * mode the article is loaded reactively by {@link articleResource} and the
   * form is patched by the constructor effect once it resolves.
   */
  ngOnInit(): void {
    this.newArticlForm = this.fb.group(
      {
        title: ['', nonEmptyTitle],
        currentLang: '',
        content: [''],
      },
      {
        updateOn: 'change',
      }
    );

    this.newArticlForm.controls.currentLang.valueChanges.subscribe((value) => {
      this.switchLang(value);
    });

    this.newArticlForm.controls.title.valueChanges.subscribe((_value) => {
      this.computeValidity();
    });

    this.model = {};

    this.availableLangs = supportedLanguages;
  }

  /**
   * Creates a new help article for the current `categoryId` from the form title and
   * multilingual content model. On success the modal is closed, the form and model are
   * reset and the `showModalChange` output is emitted. Regardless of outcome, the result
   * of the operation is emitted via the `articleCreated` output. Does nothing if no
   * `categoryId` is set.
   *
   * @returns A promise that resolves once the create attempt has completed and the result emitted.
   */
  public async createArticle() {
    const categoryId = this.categoryId();
    if (categoryId === undefined) {
      return;
    }
    this.creating.set(true);
    const result: ActionEmitterResult = {};
    result.type = ActionType.ADD_HELP_ARTICLE;
    result.result = ActionResult.FAILED;

    try {
      const body: HelpArticle = {
        title: this.newArticlForm.value.title,
        content: this.model,
      };
      await this.helpService.createCategoryArticleAsync({
        id: categoryId,
        helpArticle: body,
      });
      result.result = ActionResult.SUCCEED;
      this.showModal.set(false);
      this.model = {};
      this.newArticlForm.reset();

      this.showModalChange.emit(this.showModal());
    } catch (error) {
      console.error(error);
    }
    this.creating.set(false);
    this.articleCreated.emit(result);
  }

  /**
   * Updates the existing help article identified by the `articleId` input using the current
   * form title and content model. On success the form is reset, the modal is closed, the form
   * is re-prepared from the persisted article and the `showModalChange` output is emitted.
   * Regardless of outcome, the result of the operation is emitted via the `articleUpdated`
   * output. Does nothing if no `articleId` is set.
   *
   * @returns A promise that resolves once the update attempt has completed and the result emitted.
   */
  public async updateArticle() {
    this.creating.set(true);
    const result: ActionEmitterResult = {};
    result.type = ActionType.UPDATE_HELP_ARTICLE;
    result.result = ActionResult.FAILED;

    try {
      const body: HelpArticle = {
        title: this.newArticlForm.value.title,
        content: this.model,
      };

      const articleId = this.articleId();
      if (articleId) {
        await this.helpService.updateHelpArticleAsync({
          id: articleId,
          helpArticle: body,
        });
        result.result = ActionResult.SUCCEED;
        this.newArticlForm.reset();
        this.showModal.set(false);
        this.articleResource.reload();
        this.showModalChange.emit(this.showModal());
      }
    } catch (error) {
      console.error(error);
    }
    this.creating.set(false);
    this.articleUpdated.emit(result);
  }

  /**
   * Cancels the current create/edit interaction by closing the modal and clearing the content
   * model. When not in edit mode the form is also reset and validity recomputed. Emits the
   * updated visibility state via the `showModalChange` output.
   */
  public cancel() {
    this.showModal.set(false);
    this.model = {};
    if (!this.editMode()) {
      this.newArticlForm.reset();
      this.computeValidity();
    }

    this.showModalChange.emit(this.showModal());
  }

  /**
   * Handler for rich text editor changes: stores the edited HTML content for the currently
   * selected language in the content model and recomputes form validity.
   *
   * @param event - The rich text editor change event; its `htmlValue` holds the edited HTML content.
   */
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  public syncText(event: any) {
    this.model[this.newArticlForm.value.currentLang] = event.htmlValue;
    this.computeValidity();
  }

  /**
   * Switches the editor to the given language by patching the form `content` control with the
   * stored content for that language (or an empty string if none exists) and recomputing validity.
   *
   * @param value - The language code to display in the editor.
   */
  public switchLang(value: string) {
    if (this.model[value]) {
      this.newArticlForm.patchValue({ content: this.model[value] });
    } else {
      this.newArticlForm.patchValue({ content: '' });
    }

    this.computeValidity();
  }

  /**
   * Recomputes {@link AddHelpArticleComponent.isValid}: the form is considered valid when the
   * reactive form is valid and the content model contains at least one non-empty language entry.
   */
  private computeValidity() {
    if (this.newArticlForm) {
      let isValidModel = false;
      for (const key of Object.keys(this.model)) {
        if (
          this.model[key] !== '' &&
          this.model[key] !== undefined &&
          this.model[key] !== null
        ) {
          isValidModel = true;
        }
      }

      this.isValid.set(this.newArticlForm.valid && isValidModel);
    } else {
      this.isValid.set(false);
    }
  }
}
