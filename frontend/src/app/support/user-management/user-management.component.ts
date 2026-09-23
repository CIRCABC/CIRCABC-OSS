import {
  ChangeDetectionStrategy,
  Component,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { TranslocoModule } from '@jsverse/transloco';
import { User, UserService } from 'app/core/generated/circabc';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';
import { InterestGroupProfileSelectable } from 'app/support/user-management/interest-group-profile-selectable';
import { UsersMembershipsModel } from 'app/support/user-management/users-memberships-model';
import { ExpirationSchedulerComponent } from './expiration-scheduler/expiration-scheduler.component';
import { FocusedUserBoxComponent } from './focused-user-box/focused-user-box.component';
import { FocusedUserMembershipBoxComponent } from './focused-user-membership-box/focused-user-membership-box.component';
import { RevocationSchedulerComponent } from './revocation-scheduler/revocation-scheduler.component';
import { UserResultBoxComponent } from './user-result-box/user-result-box.component';

/**
 * Support-area component that provides an administrative user-management
 * workspace.
 *
 * It renders a multi-step interface that lets support staff:
 * - search for users by a free-text query, or import a list of users from an
 *   uploaded file;
 * - select users into a working set and inspect the interest-group
 *   memberships (profiles) of each selected user;
 * - focus a single user to display detailed information;
 * - schedule bulk membership revocation or account expiration for the
 *   selected users via the revocation and expiration scheduler modals.
 *
 * The template is composed of several presentational sub-components
 * ({@link UserResultBoxComponent}, {@link FocusedUserBoxComponent},
 * {@link FocusedUserMembershipBoxComponent}, {@link RevocationSchedulerComponent}
 * and {@link ExpirationSchedulerComponent}) and drives them from the state
 * held on this component.
 *
 * Its key collaborator is the generated {@link UserService}, used to search
 * users, resolve users from an uploaded list, fetch a single user and load a
 * user's memberships. Reactive forms are built with {@link FormBuilder}.
 */
@Component({
  selector: 'cbc-user-management',
  templateUrl: './user-management.component.html',
  styleUrl: './user-management.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ReactiveFormsModule,
    SpinnerComponent,
    UserResultBoxComponent,
    FocusedUserBoxComponent,
    FocusedUserMembershipBoxComponent,
    RevocationSchedulerComponent,
    ExpirationSchedulerComponent,
    I18nPipe,
    TranslocoModule,
  ],
})
export class UserManagementComponent implements OnInit {
  /** Reactive-forms builder used to construct the search and file forms. */
  private readonly fb = inject(FormBuilder);
  /** Generated API client used for all user-related backend operations. */
  private readonly userService = inject(UserService);

  /** Reactive form holding the free-text `searchField` used to search users. */
  public searchForm!: FormGroup;
  /** Reactive form holding the `listFile` control used to import a user list. */
  public fileForm!: FormGroup;
  /**
   * Current step of the workflow: `'search'` for free-text search,
   * `'import'` for file-based import and `'selection'` for the selected-users
   * view.
   */
  public searchStep: 'search' | 'import' | 'selection' = 'search';
  /** Users returned by the last free-text search. */
  public readonly searchedUsers = signal<User[]>([]);
  /** Working set of users selected by the operator, with their memberships. */
  public readonly selectedUsers = signal<UsersMembershipsModel[]>([]);
  /** Users resolved from an uploaded list file. */
  public readonly retrievedUsers = signal<User[]>([]);
  /** Identifier of the currently focused user, or an empty string if none. */
  public readonly focusedUserId = signal('');
  /** The currently focused user detail, or `undefined` when none is focused. */
  public readonly focusedUser = signal<User | undefined>(undefined);
  /** Whether a single user is currently focused. */
  public readonly oneUserFocus = signal(false);
  /**
   * Flag set when focusing a user fails because no matching Alfresco user
   * exists and one could not be created.
   */
  public readonly expectionNoAlfrescoUser = signal(false);
  /** Whether a search request is currently in progress (drives the spinner). */
  public readonly searching = signal(false);
  /** Files selected through the file input for list-based import. */
  public uploadFiles!: FileList;
  /** Whether the bulk revocation scheduler modal is visible. */
  public readonly showRevocationModal = signal(false);
  /** Whether the bulk expiration scheduler modal is visible. */
  public readonly showExpirationModal = signal(false);
  /** Identifiers of the selected users, used to feed the scheduler modals. */
  public readonly selectedUserIds = signal<string[]>([]);

  /**
   * Angular lifecycle hook that initialises the `searchForm` and `fileForm`
   * reactive forms when the component is created.
   */
  ngOnInit() {
    this.searchForm = this.fb.group({
      searchField: [''],
    });

    this.fileForm = this.fb.group({
      listFile: [''],
    });
  }

  /**
   * Captures the file(s) chosen through the list-import file input and stores
   * them in {@link uploadFiles}.
   *
   * @param event The DOM change event fired by the `<input type="file">`.
   */
  public fileChangeEvent(event: Event) {
    const input = event.target as HTMLInputElement;
    this.uploadFiles = input.files as FileList;
  }

  /** Clears the search field and discards the current search results. */
  public resetSearch() {
    this.searchForm.controls.searchField.setValue('');
    this.searchedUsers.set([]);
  }

  /** Clears the list-file control and discards the retrieved users. */
  public resetUpload() {
    this.fileForm.controls.listFile.setValue('');
    this.retrievedUsers.set([]);
  }

  /**
   * Runs a free-text user search using the current `searchField` value and
   * populates {@link searchedUsers}. Resets any focused user and toggles the
   * {@link searching} flag around the request. Errors are logged and swallowed
   * so the UI can recover.
   *
   * @returns A promise that resolves once the search has completed.
   */
  public async searchUsers() {
    this.searching.set(true);
    if (
      this.searchForm.value.searchField &&
      this.searchForm.value.searchField !== ''
    ) {
      try {
        this.focusedUserId.set('');
        this.oneUserFocus.set(false);
        this.searchedUsers.set(
          await this.userService.getUsersAsync({
            query: this.searchForm.value.searchField,
            filter: false,
          })
        );
      } catch (error) {
        console.error(error);
      }
    }
    this.searching.set(false);
  }

  /**
   * Resolves users from the first uploaded list file via
   * {@link UserService.getUsersFromList} and stores them in
   * {@link retrievedUsers}. Errors are logged and swallowed.
   *
   * @returns A promise that resolves once the users have been retrieved.
   */
  public async retrieveUsers() {
    try {
      if (this.uploadFiles.length > 0) {
        const file = this.uploadFiles.item(0);
        if (file) {
          this.retrievedUsers.set(
            await this.userService.getUsersFromListAsync({ body: file })
          );
        }
      }
    } catch (error) {
      console.error(error);
    }
  }

  /**
   * Focuses a single user and loads their detail. Passing an empty string
   * clears the current focus. When the backend reports that the user does not
   * exist and could not be created, {@link expectionNoAlfrescoUser} is set.
   *
   * @param userId The identifier of the user to focus, or `''` to clear focus.
   * @returns A promise that resolves once the focused user has been resolved.
   */
  public async focusUser(userId: string) {
    this.expectionNoAlfrescoUser.set(false);
    if (userId === '') {
      this.focusedUserId.set('');
      this.oneUserFocus.set(false);
      this.focusedUser.set(undefined);
    } else {
      this.focusedUserId.set(userId);
      try {
        this.focusedUser.set(
          await this.userService.getUserAsync({ userId: this.focusedUserId() })
        );
      } catch (error) {
        if (
          error?.error?.message?.includes(
            'User does not exist and could not be created'
          )
        ) {
          this.expectionNoAlfrescoUser.set(true);
        }
      }
      this.oneUserFocus.set(true);
    }
  }

  /**
   * Adds a user to the {@link selectedUsers} working set if not already
   * present, then asynchronously loads that user's interest-group memberships
   * and stores them on the corresponding entry.
   *
   * @param user The user to add to the selection.
   */
  public selection(user: User) {
    const found = this.selectedUsers().find((item) => {
      return item.userid === user.userId;
    });

    if (found === undefined && user.userId) {
      const userId = user.userId;
      this.selectedUsers.update((users) => [
        ...users,
        {
          userid: userId,
          user: user,
          memberships: [],
          loadingMemberships: true,
        },
      ]);

      this.userService
        .getUserMembership({ userId, language: '', lightMode: false })
        .subscribe((data) => {
          const memberships = data.filter((item) => {
            return { ...item, selected: false };
          }) as InterestGroupProfileSelectable[];
          this.selectedUsers.update((users) =>
            users.map((item) =>
              item.userid === userId
                ? { ...item, loadingMemberships: false, memberships }
                : item
            )
          );
        });
    }
  }

  /** Adds every user from the current search results to the selection. */
  public selectAllSearch() {
    for (const searchedUser of this.searchedUsers()) {
      this.selection(searchedUser);
    }
  }

  /** Adds every user retrieved from the imported list to the selection. */
  public selectAllList() {
    for (const retrievedUser of this.retrievedUsers()) {
      this.selection(retrievedUser);
    }
  }

  /**
   * Removes a user from the {@link selectedUsers} working set, if present.
   *
   * @param user The user to remove from the selection.
   */
  public remove(user: User) {
    this.selectedUsers.update((users) =>
      users.filter((item) => item.userid !== user.userId)
    );
  }

  /**
   * Resets the selection state after a scheduling operation completes,
   * clearing the selected user identifiers and users and hiding the
   * revocation modal.
   */
  public refreshAfterSchedule() {
    this.selectedUserIds.set([]);
    this.selectedUsers.set([]);
    this.showRevocationModal.set(false);
  }

  /** Hides the revocation scheduler modal without applying any change. */
  public cancelSchedule() {
    this.showRevocationModal.set(false);
  }

  /** Hides the expiration scheduler modal without applying any change. */
  public cancelExpirationSchedule() {
    this.showExpirationModal.set(false);
  }

  /**
   * Collects the identifiers of all selected users into
   * {@link selectedUserIds} and opens the revocation scheduler modal.
   */
  public prepareRevocation() {
    const ids: string[] = [];
    for (const item of this.selectedUsers()) {
      if (item.userid) {
        ids.push(item.userid);
      }
    }
    this.selectedUserIds.set(ids);
    this.showRevocationModal.set(true);
  }

  /** Opens the expiration scheduler modal. */
  public prepareExpiration() {
    this.showExpirationModal.set(true);
  }

  /**
   * Marks a single interest-group profile membership as selected for the given
   * user. Does nothing if either the profile name or interest-group name is
   * undefined.
   *
   * @param userid The identifier of the selected user whose membership to toggle.
   * @param profileName The name of the profile to select, or `undefined`.
   * @param interestGroupName The name of the interest group to match, or
   * `undefined`.
   */
  public toggleProfile(
    userid: string,
    profileName: string | undefined,
    interestGroupName: string | undefined
  ) {
    if (profileName === undefined || interestGroupName === undefined) {
      return;
    }
    this.selectedUsers.update((users) =>
      users.map((item) => {
        if (item.userid !== userid) {
          return item;
        }
        const newMemberships = item.memberships.map((membership) =>
          membership.interestGroup?.name === interestGroupName &&
          membership.profile?.name === profileName
            ? { ...membership, selected: true }
            : membership
        );
        return { ...item, memberships: newMemberships };
      })
    );
  }

  /**
   * Selects or resets all membership profiles for the given user in one
   * operation.
   *
   * @param userid The identifier of the selected user whose memberships to
   * toggle.
   * @param type When `'reset'` all memberships are deselected; any other value
   * selects them all.
   */
  public toggleProfiles(userid: string, type: string) {
    this.selectedUsers.update((users) =>
      users.map((item) =>
        item.userid === userid
          ? {
              ...item,
              memberships: item.memberships.map((membership) => ({
                ...membership,
                selected: type !== 'reset',
              })),
            }
          : item
      )
    );
  }
}
