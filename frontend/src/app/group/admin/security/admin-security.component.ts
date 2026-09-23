import {
  ChangeDetectionStrategy,
  Component,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import {
  InterestGroup,
  InterestGroupService,
} from 'app/core/generated/circabc';
import { SetTitlePipe } from 'app/shared/pipes/set-title.pipe';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';

/**
 * Interest group administration screen (`cbc-admin-security`) that lets an
 * administrator configure the security settings of an interest group.
 *
 * The component renders a reactive form allowing the administrator to choose
 * the group's visibility level (private, visible to registered users, or
 * publicly visible to guests) and whether users are allowed to apply for
 * membership. The current interest group is resolved from the `id` route
 * parameter, its persisted flags are mapped onto the form controls, and any
 * changes are saved back through the {@link InterestGroupService}.
 *
 * The visibility control uses the following numeric encoding:
 * - `0` — private (neither public nor registered).
 * - `1` — visible to registered users (`isRegistered` only).
 * - `2` — visible to guests / public (`isPublic` and `isRegistered`).
 *
 * Key collaborators: {@link ActivatedRoute} (route parameters),
 * {@link FormBuilder} (reactive form construction) and
 * {@link InterestGroupService} (loading and persisting the group).
 */
@Component({
  selector: 'cbc-admin-security',
  templateUrl: './admin-security.component.html',
  styleUrl: './admin-security.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ReactiveFormsModule,
    SpinnerComponent,
    SetTitlePipe,
    TranslocoModule,
  ],
})
export class AdminSecurityComponent implements OnInit {
  /** Route accessor used to read the interest group `id` parameter. */
  private readonly route = inject(ActivatedRoute);
  /** Builder used to construct the reactive security form. */
  private readonly fb = inject(FormBuilder);
  /** Service used to load and persist the interest group. */
  private readonly interestGroupService = inject(InterestGroupService);

  /**
   * Reactive form backing the view. Contains a `visibility` control (numeric
   * encoding, see the class description) and an `applicants` boolean control
   * indicating whether membership applications are allowed.
   */
  public securityForm!: FormGroup;
  /** The interest group currently being edited, once loaded. */
  public readonly ig = signal<InterestGroup | undefined>(undefined);
  /** Whether a save operation is in progress; used to drive UI state. */
  public readonly saving = signal(false);

  /**
   * Angular lifecycle hook. Initialises the reactive form with default values
   * and subscribes to route parameter changes to load the matching interest
   * group.
   */
  ngOnInit() {
    this.securityForm = this.fb.group(
      {
        visibility: [0],
        applicants: [false],
      },
      {
        updateOn: 'change',
      }
    );
    this.route.params.subscribe(async (params) => await this.loadIg(params));
  }

  /**
   * Loads the interest group identified by the route parameters and maps its
   * persisted visibility and application flags onto the form controls.
   *
   * @param params Route parameters; the `id` entry identifies the interest
   * group to load. If no `id` is present the method does nothing.
   * @returns A promise that resolves once the group has been loaded and the
   * form controls have been populated.
   */
  private async loadIg(params: { [key: string]: string }) {
    const id = params.id;
    if (id) {
      const ig = await this.interestGroupService.getInterestGroupAsync({ id });
      this.ig.set(ig);

      if (!(ig.isPublic || ig.isRegistered)) {
        this.securityForm.controls.visibility.setValue(0);
      } else if (ig.isPublic) {
        // guest
        this.securityForm.controls.visibility.setValue(2);
      } else if (!ig.isPublic && ig.isRegistered) {
        // registered
        this.securityForm.controls.visibility.setValue(1);
      }

      this.securityForm.controls.applicants.setValue(ig.allowApply);
    }
  }

  /**
   * Discards any unsaved changes by clearing the saving flag and reloading the
   * current interest group, restoring the form to its persisted state.
   *
   * @returns A promise that resolves once the group has been reloaded.
   */
  public async cancel() {
    this.saving.set(false);
    const ig = this.ig();
    if (ig) {
      await this.loadIg({ id: ig.id as string });
    }
  }

  /**
   * Persists the current form values to the interest group. Maps the numeric
   * `visibility` selection back onto the `isPublic` / `isRegistered` flags and
   * updates `allowApply`, then saves the group via the
   * {@link InterestGroupService} and reloads it to reflect the stored state.
   *
   * @returns A promise that resolves once the group has been saved and
   * reloaded.
   */
  public async save() {
    this.saving.set(true);
    const ig = this.ig();
    if (ig) {
      if (this.securityForm.value.visibility === 2) {
        ig.isPublic = true;
        ig.isRegistered = true;
      } else if (this.securityForm.value.visibility === 1) {
        ig.isPublic = false;
        ig.isRegistered = true;
      } else {
        ig.isPublic = false;
        ig.isRegistered = false;
      }

      ig.allowApply = this.securityForm.value.applicants;

      await this.interestGroupService.putInterestGroupAsync({
        id: ig.id as string,
        interestGroup: ig,
      });
      await this.loadIg({ id: ig.id as string });
    }

    this.saving.set(false);
  }

  /**
   * @returns `true` when the selected visibility is guest/public (`2`).
   */
  public isGuestVisible(): boolean {
    return this.securityForm.value.visibility === 2;
  }

  /**
   * @returns `true` when the selected visibility is registered users (`1`).
   */
  public isRegisteredVisible(): boolean {
    return this.securityForm.value.visibility === 1;
  }

  /**
   * @returns `true` when the selected visibility is private (`0`).
   */
  public isPrivateVisible(): boolean {
    return this.securityForm.value.visibility === 0;
  }

  /**
   * @returns `true` when membership applications are currently allowed.
   */
  public isApplicationAllowed(): boolean {
    return this.securityForm.value.applicants === true;
  }

  /**
   * Resolves the translation key describing the currently selected visibility.
   *
   * @returns `'label.public'` for guest visibility, `'label.users'` for
   * registered visibility, or `'label.private'` otherwise.
   */
  public getVisibilityLabel(): string {
    if (this.isGuestVisible()) {
      return 'label.public';
    }
    if (this.isRegisteredVisible()) {
      return 'label.users';
    }
    return 'label.private';
  }

  /**
   * Resolves the yes/no translation key reflecting whether applications are
   * allowed.
   *
   * @returns `'label.yes'` when applications are allowed, `'label.no'`
   * otherwise.
   */
  public getApplicantsYesNoLabel(): string {
    return this.isApplicationAllowed() ? 'label.yes' : 'label.no';
  }
}
