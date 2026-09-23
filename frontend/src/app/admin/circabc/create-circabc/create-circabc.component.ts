import {
  ChangeDetectionStrategy,
  Component,
  inject,
  model,
  OnInit,
  signal,
} from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { TranslocoModule } from '@jsverse/transloco';
import { CiracbcAdminReloadListenerService } from 'app/core/circabc-admin-reload-listener.service';
import { CircabcService, User, UserService } from 'app/core/generated/circabc';
import { DataCyDirective } from 'app/shared/directives/data-cy.directive';
import { ModalComponent } from 'app/shared/modal/modal.component';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';

/**
 * Modal-based wizard component that lets a platform administrator promote one
 * or more users to CIRCABC administrators.
 *
 * The component renders a modal dialog (see {@link ModalComponent}) containing
 * a user search field, a list of matching users, and a list of the users that
 * will be granted the CIRCABC administrator role. Selected users are staged in
 * {@link CreateCircabcComponent.futureAdmins} and, once confirmed, submitted to
 * the backend.
 *
 * Key collaborators:
 * - {@link UserService} — searches for users matching a query.
 * - {@link CircabcService} — persists the new CIRCABC administrators.
 * - {@link CiracbcAdminReloadListenerService} — notifies other views so they
 *   refresh their CIRCABC administrator list after a successful invite.
 */
@Component({
  selector: 'cbc-create-circabc',
  templateUrl: './create-circabc.component.html',
  styleUrl: './create-circabc.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ModalComponent,
    DataCyDirective,
    ReactiveFormsModule,
    SpinnerComponent,
    TranslocoModule,
  ],
})
export class CreateCircabcComponent implements OnInit {
  /** Reactive forms factory used to build {@link addUserForm}. */
  private readonly fb = inject(FormBuilder);
  /** API client used to persist the selected CIRCABC administrators. */
  private readonly circabcService = inject(CircabcService);
  /** API client used to search for users by name. */
  private readonly userService = inject(UserService);
  /** Service used to broadcast a refresh so other views reload the admin list. */
  private readonly ciracbcAdminReloadListenerService = inject(
    CiracbcAdminReloadListenerService
  );

  /**
   * Two-way bindable model controlling the visibility of the modal dialog.
   * Set to `true` to open the wizard and `false` to close it.
   */
  showModal = model(false);

  /**
   * Reactive form backing the wizard, containing the `name` search field and
   * the `possibleUsers` selection control. Initialised in {@link ngOnInit}.
   */
  public addUserForm!: FormGroup;
  /** Identifier of the current wizard step (defaults to the admins step). */
  public step = 'admins';
  /** Users staged to be granted the CIRCABC administrator role. */
  public futureAdmins: User[] = [];
  /** Users returned by the most recent search that can be selected. */
  public readonly availableUsers = signal<User[]>([]);
  /** `true` while a user search request is in progress. */
  public readonly searchingUsers = signal(false);
  /** `true` while the invite request is being submitted to the backend. */
  public readonly processing = signal(false);

  /**
   * Angular lifecycle hook. Builds the reactive form used by the wizard.
   *
   * @returns A promise that resolves once initialisation completes.
   */
  ngOnInit() {
    this.addUserForm = this.fb.group({
      name: [''],
      possibleUsers: [''],
    });
  }

  /**
   * Closes the wizard and resets its state: clears the form, the available
   * users and the staged future administrators, then hides the modal.
   */
  public cancel() {
    this.addUserForm.reset();

    this.availableUsers.set([]);
    this.futureAdmins = [];

    this.showModal.set(false);
  }

  /**
   * Triggers a user search based on the current value of the `name` field.
   * Clears any previous selection before searching. Does nothing when the
   * search field is empty.
   *
   * @returns A promise that resolves once the search completes.
   */
  public async searchUsers() {
    if (this.addUserForm.controls.name.value !== '') {
      this.addUserForm.controls.possibleUsers.setValue('');
      await this.populateUsers(this.addUserForm.controls.name.value);
    }
  }

  /**
   * Queries the backend for users matching the given search string and
   * refreshes {@link availableUsers} with the results. Toggles
   * {@link searchingUsers} while the request is in flight.
   *
   * @param query The search string used to match users.
   * @returns A promise that resolves once the users have been populated.
   */
  public async populateUsers(query: string) {
    this.searchingUsers.set(true);
    const res = await this.userService.getUsersAsync({ query, filter: false });
    this.availableUsers.set([...res]);
    this.searchingUsers.set(false);
  }

  /**
   * Clears the search results and resets the `name` and `possibleUsers` form
   * controls, leaving the staged future administrators untouched.
   */
  public resetForm(): void {
    this.availableUsers.set([]);
    this.addUserForm.controls.name.setValue('');
    this.addUserForm.controls.possibleUsers.setValue('');
  }

  /**
   * Adds the users currently selected in the `possibleUsers` control to
   * {@link futureAdmins}, skipping any user already staged (de-duplicated by
   * user id).
   */
  public selectUsers(): void {
    const membersTmp: User[] = [];
    this.addUserForm.controls.possibleUsers.value.forEach((userid: string) => {
      const memberTmp = this.availableUsers().find(
        (user) => user.userId === userid
      );
      if (memberTmp) {
        membersTmp.push(memberTmp);
      }
    });

    this.futureAdmins = this.futureAdmins.concat(
      membersTmp.filter((memberTmp) => {
        return !this.futureAdmins.some((member) => {
          return member && memberTmp && member.userId === memberTmp.userId;
        });
      })
    );
  }

  /**
   * Indicates whether at least one user is currently selected in the
   * `possibleUsers` control.
   *
   * @returns `true` if a user is selected, otherwise `false`.
   */
  hasSelectedUser(): boolean {
    return this.addUserForm.controls.possibleUsers.value !== '';
  }

  /**
   * Removes the given user from the staged {@link futureAdmins} list.
   *
   * @param m The user to remove from the future administrators.
   */
  public removeFromFutureAdmin(m: User): void {
    const index: number = this.futureAdmins.indexOf(m, 0);
    this.futureAdmins.splice(index, 1);
  }

  /**
   * Determines whether the wizard can be completed, i.e. whether at least one
   * user has been staged as a future administrator.
   *
   * @returns `true` when there is at least one staged administrator.
   */
  public isWizardOk(): boolean {
    if (this.futureAdmins.length > 0) {
      return true;
    }

    return false;
  }

  /**
   * Submits the staged {@link futureAdmins} to the backend as CIRCABC
   * administrators. On success, broadcasts a refresh via
   * {@link CiracbcAdminReloadListenerService} and closes the wizard. Any error
   * is logged and does not propagate. Toggles {@link processing} for the
   * duration of the request.
   *
   * @returns A promise that resolves once the invite attempt has completed.
   */
  public async inviteCircabcAdmin() {
    this.processing.set(true);
    try {
      const userIds: string[] = [];
      this.futureAdmins.forEach((user: User) => {
        if (user.userId) {
          userIds.push(user.userId);
        }
      });
      await this.circabcService.postCircabcAdministratorsAsync({
        requestBody: userIds,
      });
      this.ciracbcAdminReloadListenerService.propagateCircabcAdminRefresh();
      this.cancel();
    } catch (error) {
      console.error('Error during the creation of the category', error);
    }
    this.processing.set(false);
  }
}
