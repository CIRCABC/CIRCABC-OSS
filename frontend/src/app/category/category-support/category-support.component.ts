import {
  ChangeDetectionStrategy,
  Component,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { Category, CategoryService, User } from 'app/core/generated/circabc';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';

/**
 * Standalone Angular component that renders the "support" configuration page
 * for a CIRCABC category.
 *
 * The component displays a reactive form that lets a category administrator
 * define how support/contact requests are routed for the category. It supports
 * two mutually exclusive modes:
 * - a single contact email address, or
 * - multiple contact email addresses (typically chosen from the category
 *   administrators).
 *
 * On initialisation it loads the current {@link Category} and its list of
 * administrators from the {@link CategoryService} using the `id` route
 * parameter, and pre-populates the form with the persisted contact settings.
 * Saving the form persists the updated contact configuration back through the
 * {@link CategoryService}.
 *
 * Key collaborators: {@link ActivatedRoute} (to read the category id from the
 * URL), {@link CategoryService} (to load and persist category data) and
 * {@link FormBuilder} (to build the reactive form).
 */
@Component({
  selector: 'cbc-category-support',
  templateUrl: './category-support.component.html',
  styleUrl: './category-support.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule, SpinnerComponent, TranslocoModule],
})
export class CategorySupportComponent implements OnInit {
  /** Provides access to the current route parameters (used to read the category id). */
  private readonly route = inject(ActivatedRoute);
  /** API client used to load and persist the category and its administrators. */
  private readonly categoryService = inject(CategoryService);
  /** Factory used to build the reactive contact configuration form. */
  private readonly fb = inject(FormBuilder);

  /** Whether an update request is currently in progress; used to drive the spinner/UI state. */
  public readonly updating = signal(false);
  /** The category currently being configured, loaded from the backend. */
  public category!: Category;
  /**
   * Reactive form holding the contact configuration.
   * Controls: `useSingleContact` (boolean toggle), `contactEmail` (single email,
   * required) and `contactEmails` (comma-separated list of emails).
   */
  public categoryForm!: FormGroup;
  /** Administrators of the category, offered as selectable contact candidates. */
  public readonly administrators = signal<User[]>([]);

  /**
   * Angular lifecycle hook. Builds the reactive form and subscribes to route
   * parameter changes to (re)load the category identified by the `id` route
   * parameter.
   */
  ngOnInit() {
    this.categoryForm = this.fb.group({
      useSingleContact: [false],
      contactEmail: ['', Validators.required],
      contactEmails: [''],
    });

    this.route.params.subscribe(async (params) => {
      await this.initCategory(params.id);
    });
  }

  /**
   * Loads the category and its administrators for the given id and
   * pre-populates the form controls with the persisted contact settings.
   *
   * Depending on `useSingleContact`, either the first contact email is placed
   * in the `contactEmail` control or the full list is placed (comma-separated)
   * in the `contactEmails` control. Any failure is caught and logged.
   *
   * @param id The identifier of the category to load.
   * @returns A promise that resolves once loading and form population complete.
   */
  private async initCategory(id: string) {
    try {
      this.category = await this.categoryService.getCategoryAsync({ id });
      this.administrators.set(
        await this.categoryService.getCategoryAdministratorsAsync({ id })
      );

      if (this.categoryForm) {
        this.categoryForm.controls.useSingleContact.setValue(
          this.category.useSingleContact
        );
        if (this.category.useSingleContact && this.category.contactEmails) {
          this.categoryForm.controls.contactEmail.setValue(
            this.category.contactEmails[0]
          );
        } else if (
          !this.category.useSingleContact &&
          this.category.contactEmails
        ) {
          const emails = this.arrayToString(this.category.contactEmails);
          this.categoryForm.controls.contactEmails.setValue(emails);
        }
      }
    } catch (error) {
      console.error(error);
      console.error('impossible to get the category');
    }
  }

  /**
   * Discards unsaved changes by reloading the category from the backend,
   * resetting the form to the persisted state.
   *
   * @returns A promise that resolves once the category has been reloaded.
   */
  public async cancel() {
    if (this.category.id) {
      await this.initCategory(this.category.id);
    }
  }

  /**
   * Persists the current contact configuration to the backend.
   *
   * Copies the form values onto the category (either a single contact email or
   * a parsed list of emails, depending on `useSingleContact`), saves it via
   * {@link CategoryService.putCategory} and then reloads the category. Does
   * nothing if the category has no id or the form is invalid. Toggles the
   * `updating` flag around the operation and logs any error.
   *
   * @returns A promise that resolves once the update (and reload) completes.
   */
  public async update() {
    this.updating.set(true);
    if (this.category?.id && this.isFormValid()) {
      try {
        this.category.useSingleContact =
          this.categoryForm.value.useSingleContact;
        if (this.categoryForm.value.useSingleContact) {
          this.category.contactEmails = [this.categoryForm.value.contactEmail];
        } else {
          this.category.contactEmails = this.stringToArray(
            this.categoryForm.value.contactEmails
          );
        }

        await this.categoryService.putCategoryAsync({
          id: this.category.id,
          category: this.category,
        });
        await this.initCategory(this.category.id);
      } catch (error) {
        console.error(error);
      }
    }
    this.updating.set(false);
  }

  /**
   * Toggles the membership of an email in the multi-contact list.
   *
   * If the email is not yet present it is added; if it is already present it is
   * removed. The updated comma-separated list is written back to the
   * `contactEmails` form control. A `undefined` email is ignored.
   *
   * @param email The email address to add or remove, or `undefined` to no-op.
   */
  public toggleSelect(email: string | undefined) {
    if (email === undefined) {
      return;
    }
    const formValue = `${this.categoryForm.value.contactEmails}`;
    const emails: string[] = formValue.split(',');

    const indexEmail = emails.indexOf(email);
    if (indexEmail === -1) {
      emails.push(email);
    } else {
      emails.splice(indexEmail, 1);
    }

    const stringEmails = this.arrayToString(emails);
    this.categoryForm.controls.contactEmails.setValue(stringEmails);
  }

  /**
   * Determines whether the given email is currently part of the multi-contact
   * list held in the `contactEmails` form control.
   *
   * @param email The email address to check, or `undefined`.
   * @returns `true` if the email is selected, otherwise `false` (including when
   * `email` is `undefined`).
   */
  public isEmailSelected(email: string | undefined): boolean {
    if (email === undefined) {
      return false;
    }
    const formValue = `${this.categoryForm.value.contactEmails}`;
    const emails: string[] = formValue.split(',');
    return emails.includes(email);
  }

  /**
   * Serialises an array of emails into a trailing-comma-separated string,
   * skipping empty entries. Example: `['a', 'b']` becomes `'a,b,'`.
   *
   * @param array The list of email strings to join.
   * @returns The comma-separated representation.
   */
  private arrayToString(array: string[]): string {
    let result = '';

    for (const str of array) {
      if (str !== '') {
        result = `${result}${str},`;
      }
    }
    return result;
  }

  /**
   * Parses a comma-separated string into an array of emails, discarding empty
   * segments.
   *
   * @param str The comma-separated string to parse.
   * @returns The array of non-empty email strings.
   */
  private stringToArray(str: string): string[] {
    const result = [];

    for (const part of str.split(',')) {
      if (part !== '') {
        result.push(part);
      }
    }
    return result;
  }

  /**
   * Validates the form according to the selected contact mode.
   *
   * In single-contact mode the `contactEmail` control must be valid. In
   * multi-contact mode there must be at least one email in the list (the split
   * on the trailing comma yields more than one segment).
   *
   * @returns `true` if the current form state is considered valid, otherwise `false`.
   */
  public isFormValid() {
    let result: boolean;
    if (this.categoryForm.value.useSingleContact) {
      result = this.categoryForm.controls.contactEmail.valid;
    } else {
      const formValue = `${this.categoryForm.value.contactEmails}`;
      // > 1, because split returns at least 1 empty string cell
      result = formValue.split(',').length > 1;
    }

    return result;
  }
}
