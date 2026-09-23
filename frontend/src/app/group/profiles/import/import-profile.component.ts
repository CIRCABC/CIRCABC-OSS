import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  input,
  model,
  output,
  resource,
  signal,
} from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';

import { TranslocoModule } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import {
  CategoryService,
  NodesService,
  Profile,
  ProfileService,
} from 'app/core/generated/circabc';
import { ModalComponent } from 'app/shared/modal/modal.component';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';

/**
 * Modal wizard component that lets a user import an access profile into an
 * interest group.
 *
 * Rendered inside a {@link ModalComponent}, it presents a reactive form with a
 * single required selector populated from the profiles that were previously
 * exported at the group's parent category level. On confirmation the chosen
 * profile is imported into the current interest group via the CIRCABC API.
 *
 * Key collaborators:
 * - {@link ProfileService} — performs the actual profile import.
 * - {@link NodesService} — resolves the interest group node (to obtain its parent).
 * - {@link CategoryService} — lists the profiles exported at the parent category.
 *
 * The outcome of the import (success, failure or cancellation) is reported to
 * the parent component through the {@link ImportProfileComponent.profileImported}
 * output.
 */
@Component({
  selector: 'cbc-import-profile',
  templateUrl: './import-profile.component.html',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ModalComponent, ReactiveFormsModule, I18nPipe, TranslocoModule],
})
export class ImportProfileComponent {
  /** Service used to import the selected profile into the interest group. */
  private readonly profileService = inject(ProfileService);
  /** Service used to fetch the interest group node and resolve its parent. */
  private readonly nodesService = inject(NodesService);
  /** Service used to retrieve the profiles exported at the parent category. */
  private readonly categoryService = inject(CategoryService);
  /** Factory used to build the reactive import form. */
  private readonly fb = inject(FormBuilder);

  /**
   * Required input holding the node id of the interest group into which a
   * profile should be imported.
   */
  readonly igNodeId = input.required<string>();

  /**
   * Two-way bound flag controlling the visibility of the import modal.
   * Set to `false` to close the wizard.
   */
  readonly showModal = model(false);
  /**
   * Emits the result of the import operation (success, failure or
   * cancellation) so the parent component can react accordingly.
   */
  readonly profileImported = output<ActionEmitterResult>();

  /** Whether an import request is currently in progress. */
  public readonly importing = signal(false);

  /**
   * Resource that resolves the interest group node from {@link igNodeId} and,
   * when the group has a parent, fetches the list of exported profiles
   * available for import at the parent category.
   *
   * Errors are caught internally (no dedicated error UI existed in the
   * original implementation) and result in an empty node/profiles pair.
   */
  private readonly importDataResource = resource({
    params: () => this.igNodeId() || undefined,
    loader: async ({ params: igNodeId }) => {
      try {
        const ig = await this.nodesService.getNodeAsync({ id: igNodeId });

        const exportedProfiles = ig.parentId
          ? await this.categoryService.getExportedProfilesAsync({
              id: ig.parentId,
              ignoreIgId: ig.id,
            })
          : [];

        return { ig, exportedProfiles };
      } catch (error) {
        console.error(error);
        return { ig: undefined, exportedProfiles: [] };
      }
    },
  });

  /** The resolved interest group node the profile is imported into. */
  public readonly ig = computed(() => this.importDataResource.value()?.ig);
  /** Profiles exported at the parent category, offered as import candidates. */
  public readonly exportedProfiles = computed(
    () => this.importDataResource.value()?.exportedProfiles ?? []
  );

  /** Reactive form backing the profile selection. */
  public readonly importProfileForm: FormGroup = this.fb.group(
    {
      selectedProfile: ['', Validators.required],
    },
    {
      updateOn: 'change',
    }
  );

  /**
   * Cancels the import wizard.
   *
   * Closes the modal, resets the form and emits a
   * {@link ActionResult.CANCELED} result of type
   * {@link ActionType.IMPORT_PROFILE} through {@link profileImported}.
   */
  cancelWizard() {
    this.showModal.set(false);

    const result: ActionEmitterResult = {};
    result.type = ActionType.IMPORT_PROFILE;
    result.result = ActionResult.CANCELED;

    this.importProfileForm.reset();
    this.profileImported.emit(result);
  }

  /**
   * Imports the profile currently selected in the form into the interest group.
   *
   * Matches the selected profile against {@link exportedProfiles} and posts it
   * via {@link ProfileService.postImportedProfile}. On success the form is
   * reset; any failure is captured and reflected in the emitted result. The
   * {@link importing} flag is toggled around the request, and the outcome
   * ({@link ActionResult.SUCCEED} or {@link ActionResult.FAILED}) is emitted
   * through {@link profileImported}.
   *
   * @returns A promise that resolves once the import attempt has completed and
   * the result has been emitted.
   */
  async import() {
    this.importing.set(true);
    const result: ActionEmitterResult = {};
    result.type = ActionType.IMPORT_PROFILE;

    for (const profile of this.exportedProfiles()) {
      if (this.importProfileForm.value.selectedProfile === profile.id) {
        const body: Profile = profile;
        try {
          await this.profileService.postImportedProfileAsync({
            id: this.igNodeId(),
            profile: body,
          });
          result.result = ActionResult.SUCCEED;
          this.importProfileForm.reset();
        } catch (error) {
          console.error(error);
          result.result = ActionResult.FAILED;
        }
      }
    }
    this.profileImported.emit(result);
    this.importing.set(false);
  }
}
