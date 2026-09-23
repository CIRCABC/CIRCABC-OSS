/* eslint-disable @typescript-eslint/no-explicit-any */
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  forwardRef,
  inject,
  input,
  output,
  resource,
  signal,
} from '@angular/core';
import {
  ControlValueAccessor,
  FormControl,
  FormGroup,
  NG_VALUE_ACCESSOR,
  ReactiveFormsModule,
} from '@angular/forms';
import { TranslocoModule } from '@jsverse/transloco';
import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import {
  InterestGroup,
  InterestGroupService,
  MembersService,
  PagedUserProfile,
  Profile,
  ProfileService,
  User,
} from 'app/core/generated/circabc';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';

/**
 * Union of the two concrete pickable entities: a full {@link User} or a
 * {@link Profile} (access profile).
 */
type UserOrProfile = User | Profile;

/**
 * Extension of {@link UserOrProfile} that also allows a bare `string`.
 *
 * A plain string typically represents an as-yet-unresolved reference such as
 * an internal user id or an external e-mail address that has not been
 * expanded into a full {@link User} object.
 */
type UserOrProfileOrString = User | Profile | string;

/**
 * A {@link UserOrProfile} paired with its current selection state, used to
 * back the multi-select list of search results.
 */
interface SelectableUserOrProfile {
  /** The user or profile represented by this list entry. */
  item: UserOrProfile;
  /** Whether this entry is currently selected in the results list. */
  selected: boolean;
}
/**
 * Describes an entry in the "search by type" toggle (user vs. profile).
 */
interface Types {
  /** Numeric type identifier (`0` = user, `1` = profile). */
  value: number;
  /** Translation key used to label the type option. */
  text: string;
}

@Component({
  selector: 'cbc-users-picker',
  templateUrl: './users-picker.component.html',
  styleUrl: './users-picker.component.scss',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      multi: true,

      useExisting: forwardRef(() => UsersPickerComponent),
    },
  ],
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule, TranslocoModule],
})
/**
 * Form control that lets the user search for and select interest-group members
 * (`User`s) and access `Profile`s within the context of a given interest group.
 *
 * The component renders a type toggle (users vs. profiles), a search box, a
 * multi-select list of matching results and, optionally, a list of the items
 * that have already been selected. Selected items can be added or removed and
 * the resulting collection is exposed to Angular's forms API.
 *
 * It implements {@link ControlValueAccessor}, so it can be used directly with a
 * `formControl`/`formControlName` binding; the control's value is the array of
 * selected {@link UserOrProfileOrString} entries.
 *
 * Key collaborators:
 * - {@link ProfileService} / {@link MembersService} — search for profiles and
 *   members.
 * - {@link InterestGroupService} — resolve the current interest group.
 * - {@link PermissionEvaluatorService} — decide whether privileged details
 *   (e.g. e-mail addresses) may be shown.
 * - {@link UiMessageService} — surface error messages to the user.
 * - {@link I18nPipe} — localise profile titles.
 *
 * @selector cbc-users-picker
 */
export class UsersPickerComponent implements ControlValueAccessor {
  private readonly profileService = inject(ProfileService);
  private readonly uiMessageService = inject(UiMessageService);
  private readonly membersService = inject(MembersService);
  private readonly i18nPipe = inject(I18nPipe);
  private readonly permEvalService = inject(PermissionEvaluatorService);
  private readonly groupService = inject(InterestGroupService);

  /**
   * When `true`, the special `guest` and `EVERYONE` profiles are kept in the
   * profile search results; otherwise they are filtered out.
   */
  public readonly displayGuestRegistered = input(false);
  /** Id of the interest group the search and membership lookups run against. */
  public readonly igId = input<string>();

  /** Optional tooltip/help text shown for the profiles option. */
  public profilesTip = input<string>();
  /** Optional tooltip/help text shown for the users option. */
  public usersTip = input<string>();

  /** When `true`, the list of already-selected items is displayed. */
  public readonly showSelectedList = input(true);
  /** Emits after items have been added to the selection. */
  public readonly afterSelectionMade = output();
  /** Emits when a user/profile search or resolution request fails. */
  public readonly userOrProfileQueryError = output();

  /** Available search types shown in the type toggle (user / profile). */
  public availableTypes: Types[] = [
    { value: 0, text: 'label.user' },
    { value: 1, text: 'label.profile' },
  ];
  /** Currently selected search type as a string (`'0'` user, `'1'` profile). */
  public selectedTypeValue = '0';
  /** Search results, each wrapped with its selection state. */
  public readonly availableUsersOrProfiles = signal<SelectableUserOrProfile[]>(
    []
  );
  /** Items currently selected; this is the value exposed via the form control. */
  public readonly selectedUsersOrProfiles = signal<UserOrProfileOrString[]>([]);
  /** Reactive form group backing the search text and selection controls. */
  public form!: FormGroup;
  /** Current free-text search term. */
  public searchText = '';

  /**
   * Loads the interest group identified by {@link igId}. Stays idle (loader
   * not called) while no id is set.
   */
  private readonly groupResource = resource({
    params: () => this.igId() || undefined,
    loader: ({ params: igId }) =>
      this.groupService.getInterestGroupAsync({ id: igId }),
  });

  /** The resolved interest group identified by {@link igId}. */
  public readonly currentGroup = computed(() =>
    this.groupResource.hasValue() ? this.groupResource.value() : undefined
  );

  /**
   * Type guard: determines whether the given entity is a {@link User}.
   *
   * @param userOrProfile - The entity to test.
   * @returns `true` if the entity carries a `userId` (i.e. is a user).
   */
  private static isUser(userOrProfile: UserOrProfile): userOrProfile is User {
    return (userOrProfile as User).userId !== undefined;
  }

  /**
   * Type guard: determines whether the given entity is a {@link Profile}.
   *
   * @param userOrProfile - The entity to test.
   * @returns `true` if the entity carries a `name` (i.e. is a profile).
   */
  private static isProfile(
    userOrProfile: UserOrProfile
  ): userOrProfile is Profile {
    return (userOrProfile as Profile).name !== undefined;
  }

  /**
   * Compares two entities for identity.
   *
   * Two users are equal when their `userId` matches; two profiles are equal
   * when their `name` matches. A user and a profile are never equal.
   *
   * @param a - First entity to compare.
   * @param b - Second entity to compare.
   * @returns `true` if both entities represent the same user or profile.
   */
  private static areEquals(a: UserOrProfile, b: UserOrProfile): boolean {
    if (UsersPickerComponent.isUser(a) && UsersPickerComponent.isUser(b)) {
      return a.userId === b.userId;
    }
    if (
      UsersPickerComponent.isProfile(a) &&
      UsersPickerComponent.isProfile(b)
    ) {
      return a.name === b.name;
    }
    return false;
  }

  // impement ControlValueAccessor interface

  /**
   * {@link ControlValueAccessor} callback invoked to propagate value changes to
   * the parent form; replaced via {@link registerOnChange}.
   */
  onChange = (_: any) => {};

  /**
   * Returns the machine-readable code of an entity: the `userId` for a user or
   * the `name` for a profile.
   *
   * @param userOrProfile - The entity to read.
   * @returns The entity's code, or an empty string if it is neither a user nor
   * a profile.
   */
  public getCode(userOrProfile: UserOrProfile): string {
    let result = '';
    if (UsersPickerComponent.isUser(userOrProfile)) {
      result = userOrProfile.userId as string;
    } else if (UsersPickerComponent.isProfile(userOrProfile)) {
      result = userOrProfile.name as string;
    }
    return result;
  }

  /**
   * Returns a human-friendly display name: "firstname lastname" for a user or
   * the `name` for a profile.
   *
   * @param userOrProfile - The entity to read.
   * @returns The display name, or an empty string if the entity is neither a
   * user nor a profile.
   */
  public getName(userOrProfile: UserOrProfile): string {
    let result = '';
    if (UsersPickerComponent.isUser(userOrProfile)) {
      result = `${userOrProfile.firstname}  ${userOrProfile.lastname}`;
    } else if (UsersPickerComponent.isProfile(userOrProfile)) {
      result = userOrProfile.name as string;
    }
    return result;
  }

  /**
   * Builds the label shown for a selected entry.
   *
   * For strings, the value is returned as-is. For users, the full name is
   * returned, augmented with the e-mail address when the current user has
   * directory admin or manage-members rights, or otherwise with the EC moniker
   * when available. For profiles, the localised title is preferred over the
   * raw `name`.
   *
   * @param userOrProfile - The entity (or raw string) to render.
   * @returns The display label.
   */
  public getNameEmail(userOrProfile: UserOrProfileOrString): string {
    let result = '';
    if (typeof userOrProfile === 'string') {
      return userOrProfile;
    }
    if (UsersPickerComponent.isUser(userOrProfile)) {
      if (this.isDirAdmin() || this.isDirManageMembers()) {
        result = `${userOrProfile.firstname} ${userOrProfile.lastname} (${userOrProfile.email})`;
      } else {
        result = `${userOrProfile.firstname} ${userOrProfile.lastname}`;
        if (userOrProfile.properties?.ecMoniker) {
          result = `${result} (${userOrProfile.properties.ecMoniker})`;
        }
      }
    } else if (UsersPickerComponent.isProfile(userOrProfile)) {
      result = userOrProfile.name as string;
      const profileTitle = this.i18nPipe.transform(userOrProfile.title);
      if (profileTitle !== '' && profileTitle !== undefined) {
        result = profileTitle;
      }
    }
    return result;
  }

  /**
   * {@link ControlValueAccessor} callback invoked when the control is touched;
   * replaced via {@link registerOnTouched}.
   */
  onTouched = () => {};

  /**
   * {@link ControlValueAccessor} entry point that writes a value from the parent
   * form into the component.
   *
   * A `null` value clears the current selection, results and form; any other
   * value is resolved into a complete list of selected entries.
   *
   * @param value - The value pushed by the form, expected to be an array of
   * {@link UserOrProfileOrString}, or `null` to reset.
   */
  writeValue(value: any) {
    void this.handleWriteValue(value);
  }

  private async handleWriteValue(value: any) {
    if (value === null) {
      this.selectedUsersOrProfiles.set([]);
      this.form.reset();
      this.availableUsersOrProfiles.set([]);
    } else if (value) {
      await this.buildCompleteUsersArray(value);
    }
  }

  /**
   * {@link ControlValueAccessor} hook that registers the change-propagation
   * callback.
   *
   * @param fn - Callback invoked with the new value whenever the selection
   * changes.
   */
  registerOnChange(fn: (_: any) => void) {
    this.onChange = fn;
  }

  /**
   * {@link ControlValueAccessor} hook that registers the touched callback.
   *
   * @param fn - Callback invoked when the control is touched.
   */
  registerOnTouched(fn: () => {}): void {
    this.onTouched = fn;
  }

  /**
   * Initialises the reactive form (search text and selection controls) and
   * wires up value-change subscriptions to keep the component state and the
   * form control value in sync.
   */
  constructor() {
    const selectedUsersOrProfilesFormControl = new FormControl(
      this.selectedUsersOrProfiles(),
      { nonNullable: true }
    );
    const searchTextFormControl = new FormControl(this.searchText, {
      nonNullable: true,
    });
    this.form = new FormGroup(
      {
        selectedUsersOrProfiles: selectedUsersOrProfilesFormControl,
        searchText: searchTextFormControl,
      },
      {
        updateOn: 'change',
      }
    );
    selectedUsersOrProfilesFormControl.valueChanges.subscribe((value) => {
      this.selectedUsersOrProfiles.set(value);
      if (this.onChange) {
        this.onChange(value);
      }
    });
    searchTextFormControl.valueChanges.subscribe((value) => {
      this.searchText = value;
    });
  }

  /**
   * @returns `true` if the current user is a directory administrator of the
   * current group.
   */
  public isDirAdmin(): boolean {
    return this.permEvalService.isDirAdmin(
      this.currentGroup() as InterestGroup
    );
  }

  /**
   * @returns `true` if the current user may manage members of the current
   * group.
   */
  public isDirManageMembers(): boolean {
    return this.permEvalService.isDirManageMembers(
      this.currentGroup() as InterestGroup
    );
  }

  /**
   * Expands an incoming array of selection references into fully-populated
   * entries stored in {@link selectedUsersOrProfiles}.
   *
   * Already-complete users are kept as-is; bare internal user ids are resolved
   * against the group's membership; everything else is pushed unchanged. On
   * failure, {@link userOrProfileQueryError} is emitted.
   *
   * @param usersOrProfilesArray - The references to expand.
   * @returns A promise that resolves once the array has been processed.
   */
  // prettier-ignore
  private async buildCompleteUsersArray(usersOrProfilesArray: UserOrProfileOrString[]) { // NOSONAR
    const igId = this.igId();
    if (!igId) return;

    try {
      const pagedUserProfile = await this.fetchMembers(igId);

      for (const userOrProfile of usersOrProfilesArray) {
        if (this.isCompleteUser(userOrProfile)) {
          this.selectedUsersOrProfiles.update((arr) => [...arr, userOrProfile]);
        } else if (this.isInternalUserId(userOrProfile)) {
          this.addInternalUser(userOrProfile, pagedUserProfile);
        } else {
          this.selectedUsersOrProfiles.update((arr) => [...arr, userOrProfile]);
        }
      }
    } catch (error) {
      console.error(error);
      this.userOrProfileQueryError.emit();
    }
  }

  /**
   * Fetches the full (unpaged) list of members for a group.
   *
   * @param igId - Id of the interest group.
   * @returns A promise resolving to the paged user profiles for the group.
   */
  private async fetchMembers(igId: string): Promise<PagedUserProfile> {
    return await this.membersService.getMembersAsync({
      id: igId,
      searchQuery: '',
    });
  }

  /**
   * Type guard for an already fully-populated user reference.
   *
   * @param userOrProfile - The reference to test.
   * @returns `true` when the reference carries a `firstname` and is therefore a
   * complete {@link User}.
   */
  private isCompleteUser(
    userOrProfile: UserOrProfileOrString
  ): userOrProfile is User {
    return !!(userOrProfile as User).firstname;
  }

  /**
   * Determines whether a reference denotes an internal user that still needs to
   * be resolved from a bare id (or is a raw string).
   *
   * @param userOrProfile - The reference to test.
   * @returns `true` for a `userId` without an `@` (internal) or a raw string.
   */
  private isInternalUserId(userOrProfile: UserOrProfileOrString): boolean {
    const userId = (userOrProfile as User).userId;
    return (
      (userId && !userId.includes('@')) || typeof userOrProfile === 'string'
    );
  }

  /**
   * Resolves an internal user id against the group's membership and appends the
   * matching {@link User} to {@link selectedUsersOrProfiles} (avoiding
   * duplicates).
   *
   * @param userOrProfile - The internal user id (or string) to resolve.
   * @param pagedUserProfile - The group membership to search within.
   */
  private addInternalUser(
    userOrProfile: UserOrProfileOrString,
    pagedUserProfile: PagedUserProfile
  ) {
    if (!pagedUserProfile.data) return;

    for (const userProfile of pagedUserProfile.data) {
      const user = userProfile.user;
      if (
        userOrProfile === user?.userId &&
        !this.selectedUsersOrProfiles().includes(user)
      ) {
        this.selectedUsersOrProfiles.update((arr) => [...arr, user]);
        break;
      }
    }
  }

  /**
   * Updates {@link selectedTypeValue} in response to the search-type toggle.
   *
   * @param value - The newly selected type (`'0'` user, `'1'` profile).
   */
  public onTypeChange(value: string): void {
    this.selectedTypeValue = value;
  }

  /**
   * Runs a search for users or profiles (depending on the selected type) using
   * the current {@link searchText}, and populates
   * {@link availableUsersOrProfiles}. Errors are routed to
   * {@link handleSearchError}.
   *
   * @returns A promise that resolves once the search has completed.
   */
  // searches for the users/profiles to display
  public async doSearch() {
    try {
      const igId = this.igId();
      if (!igId) return;

      this.searchText = this.searchText || '';

      if (this.selectedTypeValue === '0') {
        await this.searchUsers(igId);
      } else {
        await this.searchProfiles(igId);
      }
    } catch (error) {
      this.handleSearchError(error);
    }
  }

  /**
   * Searches the group's members matching {@link searchText} and stores the
   * results in {@link availableUsersOrProfiles}.
   *
   * @param igId - Id of the interest group to search within.
   * @returns A promise that resolves once the results are loaded.
   */
  private async searchUsers(igId: string) {
    const pagedUserProfile = await this.membersService.getMembersAsync({
      id: igId,
      searchQuery: this.searchText,
    });

    if (pagedUserProfile.data) {
      this.availableUsersOrProfiles.set(
        pagedUserProfile.data.map(
          (userProfile) =>
            ({
              item: userProfile.user,
              selected: false,
            }) as SelectableUserOrProfile
        )
      );
    }
  }

  /**
   * Searches the group's profiles matching {@link searchText} and stores the
   * results in {@link availableUsersOrProfiles}. When
   * {@link displayGuestRegistered} is `false`, the `guest` and `EVERYONE`
   * profiles are excluded.
   *
   * @param igId - Id of the interest group to search within.
   * @returns A promise that resolves once the results are loaded.
   */
  private async searchProfiles(igId: string) {
    let profiles = await this.profileService.getProfilesAsync({
      id: igId,
      searchQuery: this.searchText,
      nonEmptyProfiles: false,
    });

    if (!this.displayGuestRegistered()) {
      profiles = profiles.filter(
        (p) => p.name !== 'guest' && p.name !== 'EVERYONE'
      );
    }

    this.availableUsersOrProfiles.set(
      profiles.map(
        (profile) =>
          ({
            item: profile,
            selected: false,
          }) as SelectableUserOrProfile
      )
    );
  }

  /**
   * Handles a search failure by emitting {@link userOrProfileQueryError} and,
   * when the error body carries a message, displaying it via
   * {@link UiMessageService}.
   *
   * @param error - The error thrown during the search; its `_body` is parsed as
   * JSON to extract an optional `message`.
   */
  private handleSearchError(error: any) {
    this.userOrProfileQueryError.emit();
    const jsonError = JSON.parse(error._body) as Record<string, string>;
    if (jsonError?.message) {
      this.uiMessageService.addErrorMessage(jsonError.message);
    }
  }

  /**
   * Synchronises the `selected` flag of each entry in
   * {@link availableUsersOrProfiles} with the DOM selection state of the
   * corresponding option in the native multi-select element.
   *
   * @param multiSelectElement - The `<select multiple>` element whose `options`
   * selection state is read.
   */
  public setSelected(multiSelectElement: any): void {
    const items = this.availableUsersOrProfiles();
    let i = 0;
    for (const optionElement of multiSelectElement.options) {
      if (optionElement.selected === true) {
        items[i].selected = true;
      } else {
        items[i].selected = false;
      }
      i += 1;
    }
  }

  /**
   * Appends the currently-selected search results to
   * {@link selectedUsersOrProfiles}, skipping any items already present, then
   * propagates the change to the form ({@link onChange}) and emits
   * {@link afterSelectionMade}.
   */
  public addToSelectedUsersOrProfiles(): void {
    // concat selectedUsersOrProfiles with elements that have been selected from availableUsersOrProfiles
    // and are also not found in selectedUsersOrProfiles (not already added to selectedUsersOrProfiles)
    const current = this.selectedUsersOrProfiles();

    const updated = current.concat(
      this.availableUsersOrProfiles()
        .filter((availableItem) => availableItem.selected)
        .map((selectableItem) => selectableItem.item)
        .filter((selectedItem) => {
          return !current.some((currentItem) => {
            return UsersPickerComponent.areEquals(
              currentItem as User | Profile,
              selectedItem
            );
          });
        })
    );
    this.selectedUsersOrProfiles.set(updated);
    // propagate the changes to be catched by the for component to be updated
    this.onChange(updated);
    this.afterSelectionMade.emit();
  }

  /**
   * Removes an item from {@link selectedUsersOrProfiles} and propagates the
   * change to the form ({@link onChange}). Typically fired when the user clicks
   * the "X" next to a selected entry.
   *
   * @param selectedItem - The entry to remove from the selection.
   */
  // unselect items (fired when X is clicked)
  public removeFromSelectedUsersOrProfiles(
    selectedItem: UserOrProfileOrString
  ): void {
    const updated = [...this.selectedUsersOrProfiles()];
    const index: number = updated.indexOf(selectedItem as UserOrProfile, 0);
    updated.splice(index, 1);
    this.selectedUsersOrProfiles.set(updated);
    // propagate the changes to be catched by the for component to be updated
    this.onChange(updated);
  }

  /**
   * Clears the current search results ({@link availableUsersOrProfiles}).
   */
  public clearAvailableUsersOrProfiles(): void {
    this.availableUsersOrProfiles.set([]);
  }
}
