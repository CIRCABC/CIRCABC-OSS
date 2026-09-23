import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  input,
  resource,
  signal,
} from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { TranslocoModule } from '@jsverse/transloco';
import {
  Category,
  CategoryService,
  GroupCreationRequest,
  HeaderService,
  User,
  UserService,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { ControlMessageComponent } from 'app/shared/control-message/control-message.component';
import { HeaderComponent } from 'app/shared/header/header.component';
import { NavigatorComponent } from 'app/shared/navigator/navigator.component';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';
import { LeaderCardComponent } from './leader-card/leader-card.component';

/**
 * Standalone component (`cbc-request-group`) that renders the multi-step form
 * used by an end user to request the creation of a new interest group.
 *
 * The view is organised as a two-step wizard:
 * - Step 1 collects the target header/category and the proposed group
 *   metadata (name, title, description and justification comment).
 * - Step 2 lets the requester search for existing users and designate the
 *   future group leaders before submitting the request.
 *
 * On submission it builds a {@link GroupCreationRequest} and posts it through
 * the {@link CategoryService}, then navigates back to the parent route.
 *
 * Key collaborators: {@link HeaderService} and {@link CategoryService} to load
 * headers/categories and post the request, {@link UserService} to search for
 * candidate leaders, {@link LoginService} to identify the requester, and
 * {@link I18nPipe} to resolve localised category titles.
 */
@Component({
  selector: 'cbc-request-group',
  templateUrl: './request-group.component.html',
  styleUrl: './request-group.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    HeaderComponent,
    NavigatorComponent,
    ReactiveFormsModule,
    ControlMessageComponent,
    SpinnerComponent,
    LeaderCardComponent,
    RouterLink,
    TranslocoModule,
  ],
})
export class RequestGroupComponent {
  private readonly fb = inject(FormBuilder);
  private readonly categoryService = inject(CategoryService);
  private readonly headerService = inject(HeaderService);
  private readonly i18nPipe = inject(I18nPipe);
  private readonly userService = inject(UserService);
  private readonly loginService = inject(LoginService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  /**
   * Signal input controlling whether the component is rendered inside a modal.
   * Defaults to `false` (rendered as a standalone page).
   */
  readonly showModal = input(false);

  /** Reactive form backing step 1 (header, category and group metadata). */
  public form: FormGroup = this.fb.group(
    {
      header: [undefined, Validators.required],
      category: [undefined, Validators.required],
      name: ['', Validators.required],
      title: [],
      description: [],
      comment: ['', Validators.required],
    },
    {
      updateOn: 'change',
    }
  );
  /** Flag set while the group creation request is being submitted. */
  public readonly processing = signal(false);
  /** The id of the header currently selected in the `header` control. */
  private readonly selectedHeaderId = signal<string | undefined>(undefined);
  /** The id of the category currently selected in the `category` control. */
  private readonly selectedCategoryId = signal<string | undefined>(undefined);

  /** Headers loaded on initialisation and offered for selection. */
  private readonly headersResource = resource({
    loader: () => this.headerService.getHeadersAsync(),
  });
  /** Headers loaded on initialisation and offered for selection. */
  public readonly headers = computed(() => this.headersResource.value() ?? []);

  /** Categories available under the currently selected header. */
  private readonly categoriesResource = resource({
    params: () => this.selectedHeaderId() || undefined,
    loader: ({ params: id }) =>
      this.headerService.getCategoriesByHeaderIdAsync({ id }),
  });
  /** Categories available under the currently selected header. */
  public readonly categories = computed(
    () => this.categoriesResource.value() ?? []
  );
  /** The category currently selected by the user in the form. */
  public readonly selectedCategory = computed<Category | undefined>(() =>
    this.categories().find((categ) => categ.id === this.selectedCategoryId())
  );

  /** Whether the first wizard step (group details) is displayed. */
  public step1 = true;
  /** Whether the second wizard step (leader selection) is displayed. */
  public step2 = false;

  /** Reactive form backing step 2 (user search and leader selection). */
  public groupLeadersForm: FormGroup = this.fb.group(
    {
      search: [],
      possibleUsers: [],
    },
    {
      updateOn: 'change',
    }
  );

  /** The last search term submitted via {@link searchUsers}. */
  private readonly searchQuery = signal<string | undefined>(undefined);
  /** Users returned by the last search, available to be picked as leaders. */
  private readonly usersResource = resource({
    params: () => this.searchQuery() || undefined,
    loader: ({ params: query }) => this.userService.getUsersAsync({ query }),
  });
  /** Users returned by the last search, available to be picked as leaders. */
  public readonly availableUsers = computed(
    () => this.usersResource.value() ?? []
  );
  /** Flag set while a user search request is in flight. */
  public readonly searchingUsers = this.usersResource.isLoading;
  /** Users selected to become the future leaders of the requested group. */
  public futureMembers: User[] = [];

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
   * Subscribes to changes of the `header` control and keeps
   * {@link selectedHeaderId} in sync so {@link categoriesResource} reloads.
   */
  listenHeaderChange() {
    const headerControl = this.form.get('header');
    if (headerControl) {
      headerControl.valueChanges.subscribe((value: string) => {
        this.selectedHeaderId.set(value);
      });
    }
  }

  /**
   * Subscribes to changes of the `category` control and keeps
   * {@link selectedCategoryId} in sync with the chosen category id.
   */
  listenCategoryChange() {
    const categoryControl = this.form.get('category');
    if (categoryControl) {
      categoryControl.valueChanges.subscribe((value: string) => {
        this.selectedCategoryId.set(value);
      });
    }
  }

  /**
   * Resolves a human-readable label for a category, preferring its localised
   * title when available and falling back to its raw name otherwise.
   *
   * @param category The category to derive the label from.
   * @returns The localised title if present, otherwise the category name.
   */
  getNameOrTitle(category: Category): string {
    if (category.title && Object.keys(category.title).length > 0) {
      return this.i18nPipe.transform(category.title);
    }
    return category.name;
  }

  /**
   * Builds a {@link GroupCreationRequest} from the current form values and the
   * selected leaders, then posts it via {@link CategoryService} for the
   * selected category. On success, navigates back to the parent route.
   *
   * Toggles {@link processing} for the duration of the request.
   *
   * @returns A promise that resolves once the request has been submitted (and
   * navigation triggered when a category is selected).
   */
  async requestGroup() {
    this.processing.set(true);

    const category = this.selectedCategory();

    const request: GroupCreationRequest = {
      from: this.loginService.getUser(),
      proposedName: this.form.value.name,
      proposedTitle: { en: this.form.value.title },
      proposedDescription: { en: this.form.value.description },
      justification: this.form.value.comment,
      categoryRef: category?.id,
      leaders: this.futureMembers,
    };

    if (category?.id) {
      await this.categoryService.postRequestInterestGroupAsync({
        id: category.id,
        groupCreationRequest: request,
      });

      this.router.navigate(['..', { relativeTo: this.route }]);
    }

    this.processing.set(false);
  }

  /** Clears the leader search form, resetting all its controls. */
  resetSearchForm() {
    this.groupLeadersForm.reset();
  }

  /**
   * Triggers a search for users matching the current value of the search
   * control by updating {@link searchQuery}, which reloads
   * {@link usersResource}. No-op when the search term is empty.
   */
  searchUsers() {
    const value = this.groupLeadersForm.controls.search.value;
    if (value && value !== '') {
      this.searchQuery.set(value);
    }
  }

  /**
   * Clears the current search results and resets the search and selection
   * controls of the leader search form.
   */
  public resetForm(): void {
    this.searchQuery.set(undefined);
    this.groupLeadersForm.controls.search.setValue('');
    this.groupLeadersForm.controls.possibleUsers.setValue('');
  }

  /**
   * Adds the users currently checked in the search results to
   * {@link futureMembers}, skipping any user already present to avoid
   * duplicates.
   */
  public selectUsers(): void {
    const membersTmp: User[] = [];
    this.groupLeadersForm.controls.possibleUsers.value.forEach(
      (userid: string) => {
        const memberTmp = this.availableUsers().find(
          (user) => user.userId === userid
        );
        if (memberTmp) {
          membersTmp.push(memberTmp);
        }
      }
    );

    this.futureMembers = this.futureMembers.concat(
      membersTmp.filter((memberTmp) => {
        return !this.futureMembers.some((member) => {
          if (member && memberTmp) {
            return member.userId === memberTmp.userId;
          }
          return true;
        });
      })
    );
  }

  /**
   * Removes the given user from the list of {@link futureMembers}.
   *
   * @param m The user to remove from the future leaders selection.
   */
  public removeFromFutureMember(m: User): void {
    const index: number = this.futureMembers.indexOf(m, 0);
    this.futureMembers.splice(index, 1);
  }

  /** Advances the wizard from step 1 to step 2 (leader selection). */
  confirm() {
    this.step1 = false;
    this.step2 = true;
  }

  /** Returns the wizard from step 2 back to step 1 (group details). */
  revert() {
    this.step2 = false;
    this.step1 = true;
  }

  /** Convenience accessor for the `header` form control. */
  get headerControl(): AbstractControl {
    return this.form.controls.header;
  }

  /** Convenience accessor for the `category` form control. */
  get categoryControl(): AbstractControl {
    return this.form.controls.category;
  }

  /** Convenience accessor for the `name` form control. */
  get nameControl(): AbstractControl {
    return this.form.controls.name;
  }

  /** Convenience accessor for the `comment` (justification) form control. */
  get commentControl(): AbstractControl {
    return this.form.controls.comment;
  }
}
