import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  resource,
  signal,
} from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import {
  AdminContactRequest,
  Category,
  CategoryService,
  HeaderService,
} from 'app/core/generated/circabc';
import { HeaderComponent } from 'app/shared/header/header.component';
import { NavigatorComponent } from 'app/shared/navigator/navigator.component';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { RichTextEditorComponent } from 'app/shared/rich-text-editor/rich-text-editor.component';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';

/**
 * Standalone page component (`cbc-contact-category`) that lets a user contact
 * the administrators of a category.
 *
 * It renders a reactive form allowing the user to pick a header, then a
 * category belonging to that header, compose a rich-text message and
 * optionally request a copy of the message. On submit the message is sent to
 * the selected category's administrators via {@link CategoryService}.
 *
 * The header and category selections can be pre-filled through the
 * `header` and `category` query parameters of the current route. Selecting a
 * header reactively (re)loads its categories via a {@link resource}, and the
 * list of headers is loaded once via another {@link resource}.
 *
 * Key collaborators:
 * - {@link HeaderService} to load headers and their categories.
 * - {@link CategoryService} to send the contact request.
 * - {@link I18nPipe} to resolve localized category titles.
 */
@Component({
  selector: 'cbc-contact-category',
  templateUrl: './contact-category.component.html',
  styleUrl: './contact-category.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    HeaderComponent,
    NavigatorComponent,
    ReactiveFormsModule,
    RichTextEditorComponent,
    SpinnerComponent,
    RouterLink,
    TranslocoModule,
  ],
})
export class ContactCategoryComponent {
  /** Angular reactive-forms builder used to construct {@link form}. */
  private readonly fb = inject(FormBuilder);
  /** API client used to send the contact request to the category admins. */
  private readonly categoryService = inject(CategoryService);
  /** API client used to load headers and their categories. */
  private readonly headerService = inject(HeaderService);
  /** Pipe used to resolve the localized title of a category. */
  private readonly i18nPipe = inject(I18nPipe);
  /** Current route, used to read `header`/`category` query parameters. */
  private readonly route = inject(ActivatedRoute);

  /**
   * Reactive form backing the contact page. Controls: `header`, `category`
   * (both required), `messageContent` (required rich text) and `sendCopy`.
   */
  public form: FormGroup = this.fb.group(
    {
      header: [undefined, Validators.required],
      category: [undefined, Validators.required],
      messageContent: ['', Validators.required],
      sendCopy: false,
    },
    {
      updateOn: 'change',
    }
  );
  /** True while a contact request is being submitted; drives the spinner. */
  public readonly processing = signal(false);

  /** Id of the header currently selected in the form, or `undefined`. */
  private readonly headerId = signal<string | undefined>(undefined);
  /** Id of the category currently selected in the form, or `undefined`. */
  private readonly categoryId = signal<string | undefined>(undefined);

  /** All headers available for selection, loaded once on creation. */
  private readonly headersResource = resource({
    loader: async () => {
      try {
        return await this.headerService.getHeadersAsync();
      } catch (error) {
        console.error(error);
        return [];
      }
    },
  });
  /** All headers available for selection. */
  public readonly headers = computed(() => this.headersResource.value() ?? []);

  /**
   * Categories belonging to the currently selected header. Idle (loader not
   * called) while no header is selected.
   */
  private readonly categoriesResource = resource({
    params: () => this.headerId() || undefined,
    loader: async ({ params: id }) => {
      try {
        return await this.headerService.getCategoriesByHeaderIdAsync({ id });
      } catch (error) {
        console.error(error);
        return [];
      }
    },
  });
  /** Categories belonging to the currently selected header. */
  public readonly categories = computed(
    () => this.categoriesResource.value() ?? []
  );

  /**
   * The category currently selected in the form, derived from
   * {@link categories} and the `category` control's value.
   */
  public readonly selectedCategory = computed<Category | undefined>(() =>
    this.categories().find((categ) => categ.id === this.categoryId())
  );

  constructor() {
    this.listenHeaderChange();
    this.listenCategoryChange();

    this.route.queryParams.subscribe((params) => {
      if (params.header) {
        this.form.controls.header.setValue(params.header);
      }

      if (params.category) {
        this.form.controls.category.setValue(params.category);
      }
    });
  }

  /**
   * Subscribes to changes of the `header` control and updates
   * {@link headerId}, which drives the reactive reload of
   * {@link categoriesResource}. No-op if the control is absent.
   */
  listenHeaderChange() {
    const headerControl = this.form.get('header');
    if (headerControl) {
      headerControl.valueChanges.subscribe((value: string) => {
        this.headerId.set(value);
      });
    }
  }

  /**
   * Subscribes to changes of the `category` control and updates
   * {@link categoryId}, which drives {@link selectedCategory}. No-op if the
   * control is absent.
   */
  listenCategoryChange() {
    const categoryControl = this.form.get('category');
    if (categoryControl) {
      categoryControl.valueChanges.subscribe((value: string) => {
        this.categoryId.set(value);
      });
    }
  }

  /**
   * Returns the display label for a category: its localized title when
   * available, otherwise its name.
   *
   * @param category The category to derive a label from.
   * @returns The localized title if present, otherwise the category name.
   */
  getNameOrTitle(category: Category): string {
    if (category.title && Object.keys(category.title).length > 0) {
      return this.i18nPipe.transform(category.title);
    }
    return category.name;
  }

  /**
   * Submits the contact request to the administrators of the selected
   * category, using the current form's message content and copy preference.
   *
   * Sets {@link processing} to `true` while the request is in flight and back
   * to `false` when it completes. Errors from the request are caught and
   * logged to the console rather than propagated.
   *
   * @returns A promise that resolves once the request has completed.
   */
  async contact() {
    this.processing.set(true);
    try {
      const body: AdminContactRequest = {
        content: this.form.value.messageContent,
        sendCopy: this.form.value.sendCopy,
      };

      await this.categoryService.contactCategoryAdminsAsync({
        id: this.form.value.category,
        adminContactRequest: body,
      });
    } catch (error) {
      console.error(error);
    }
    this.processing.set(false);
  }
}
