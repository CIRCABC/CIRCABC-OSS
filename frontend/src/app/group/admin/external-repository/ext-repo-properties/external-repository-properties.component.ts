import { DatePipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  effect,
  inject,
  resource,
  signal,
} from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import {
  FormArray,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
} from '@angular/forms';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { ActivatedRoute } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import {
  ExternalRepositoryService,
  RepositoryConfiguration,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';

/**
 * Standalone component that renders the external repository configuration
 * panel for the administration area of an interest group.
 *
 * It displays a reactive form containing one slide-toggle per available
 * external repository, letting administrators enable or disable the
 * registration of the current interest group against each repository.
 * For each repository the template also shows the date on which the group
 * was registered. When the current user belongs to the `external` domain
 * the toggles are rendered disabled (read-only).
 *
 * Key collaborators:
 * - {@link ExternalRepositoryService} to fetch the list of available and
 *   already-registered repositories and to add/remove registrations.
 * - {@link LoginService} to determine whether the current user is external.
 * - {@link ActivatedRoute} to read the interest group id from the route.
 * - {@link FormBuilder} to build the reactive form model.
 */
@Component({
  selector: 'cbc-external-repository-properties',
  templateUrl: './external-repository-properties.component.html',
  styleUrl: './external-repository-properties.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ReactiveFormsModule,
    MatSlideToggleModule,
    SpinnerComponent,
    DatePipe,
    TranslocoModule,
  ],
})
export class ExternalRepositoryPropertiesComponent {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly externalRepositoryService = inject(
    ExternalRepositoryService
  );
  private readonly loginService = inject(LoginService);

  /** The current route params, as a signal. */
  private readonly routeParams = toSignal(this.route.params);

  /** Identifier of the interest group being configured, read from the route. */
  public readonly igId = computed(() => this.routeParams()?.id ?? '');
  /** Whether a save operation is currently in progress (drives the spinner). */
  public readonly saving = signal(false);

  /**
   * Reactive form holding a single `repos` {@link FormArray}. Each entry is a
   * group with `name`, `requestedOn` and `enabled` controls describing one
   * external repository.
   */
  public readonly form: FormGroup = this.fb.group({
    repos: this.fb.array([]),
  });
  /**
   * Whether the current user belongs to the `external` domain. When `true`
   * the repository toggles are disabled so external users cannot change them.
   */
  public readonly isExternalUser =
    this.loginService.getUser().properties?.domain === 'external';

  /**
   * Resource that loads the available external repositories together with
   * the ones the current interest group is already registered against.
   * Reactively re-fetches whenever the route's `id` param changes.
   */
  private readonly reposResource = resource({
    params: () => this.igId(),
    loader: async ({ params: id }) => {
      try {
        const available =
          await this.externalRepositoryService.getAvailableExternalRepositoriesAsync();
        const registered =
          await this.externalRepositoryService.getExternalRepositoriesAsync({
            id,
          });
        return { available, registered };
      } catch (e) {
        console.error(e);
        return { available: [], registered: [] as RepositoryConfiguration[] };
      }
    },
    defaultValue: { available: [], registered: [] },
  });

  constructor() {
    // The `repos` FormArray is rebuilt from the loaded resource value; syncing
    // a signal into the imperative, non-signal FormArray API is a legitimate
    // use of `effect`.
    effect(() => {
      const { available, registered } = this.reposResource.value();
      const reposFormArray = this.form.controls.repos as FormArray;
      reposFormArray.clear();
      available.forEach((repo: string) => {
        const name = repo;
        let enabled = false;
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        let requestedOn: any;
        const externalRepo = registered.find((r) => r.name === name);
        if (externalRepo?.registrationDate) {
          enabled = true;
          requestedOn = new Date(externalRepo.registrationDate);
        }
        this.addRepo(name, enabled, requestedOn);
      });
    });
  }

  /**
   * Appends a repository entry to the `repos` form array. The `enabled`
   * control is disabled when the current user is external so it becomes
   * read-only.
   *
   * @param name The name of the external repository.
   * @param enabled Whether the interest group is currently registered against
   *   the repository.
   * @param requestedOn Optional date on which the registration was requested.
   */
  private addRepo(name: string, enabled: boolean, requestedOn?: Date) {
    const repos = this.form.controls.repos as FormArray;
    const enabledControl = this.fb.control(enabled);
    if (this.isExternalUser) {
      enabledControl.disable();
    }
    repos.push(
      this.fb.group({
        name: name,
        requestedOn: requestedOn,
        enabled: enabledControl,
      })
    );
  }

  /**
   * Persists the current form state. For each repository it compares the
   * toggled `enabled` value against the existing registrations and either
   * adds a new registration or deletes the existing one accordingly, then
   * reloads the data. Toggles the {@link saving} flag around the operation.
   *
   * @returns A promise that resolves once the save requests have been issued.
   */
  public async save() {
    this.saving.set(true);
    const registered = this.reposResource.hasValue()
      ? this.reposResource.value().registered
      : [];
    const reposModel = this.form.value.repos as {
      name: string;
      requestedOn?: Date;
      enabled: boolean;
    }[];
    reposModel.forEach(async (repo) => {
      const externalRepo = registered.find((r) => r.name === repo.name);
      if (repo.enabled && externalRepo === undefined) {
        await this.externalRepositoryService.addExternalRepositoriesAsync({
          id: this.igId(),
          repoId: repo.name,
        });
      }
      if (!repo.enabled && externalRepo !== undefined) {
        await this.externalRepositoryService.deleteExternalRepositoryAsync({
          id: this.igId(),
          repoId: repo.name,
        });
      }
      this.reposResource.reload();
    });
    this.saving.set(false);
  }

  /**
   * Discards any unsaved changes by reloading the repository data from the
   * server, resetting the form to its persisted state.
   */
  public cancel() {
    this.reposResource.reload();
  }

  /**
   * Convenience accessor for the `repos` {@link FormArray} within the form,
   * used by the template to iterate over repository controls.
   *
   * @returns The `repos` form array.
   */
  get reposFormArray() {
    return this.form.get('repos') as FormArray;
  }
}
