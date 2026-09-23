import {
  ChangeDetectionStrategy,
  Component,
  effect,
  inject,
  input,
  model,
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
import { Share, SpaceService } from 'app/core/generated/circabc';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';

/**
 * Modal component that lets a user share a library space with other interest
 * groups (IGs) and manage the permission granted to an already-shared IG.
 *
 * Rendered as a dialog (visibility driven by {@link ShareSpaceComponent.showModal}),
 * it presents a reactive form to pick a target IG, a permission level and whether
 * the target group leaders should be notified. Depending on whether it is invoked
 * for a new share or an existing one, it either creates a new share
 * ({@link ShareSpaceComponent.share}) or updates the permission of an existing
 * share ({@link ShareSpaceComponent.change}).
 *
 * Key collaborators:
 * - {@link SpaceService} (generated CIRCABC API client) to fetch the available
 *   IGs/permissions and to persist share/update operations.
 * - {@link FormBuilder} to build the reactive sharing form.
 *
 * The outcome of each operation is reported to the parent via the
 * {@link ShareSpaceComponent.modalHide} output.
 */
@Component({
  selector: 'cbc-share-space',
  templateUrl: './share-space.component.html',
  styleUrl: './share-space.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule, SpinnerComponent, TranslocoModule],
})
export class ShareSpaceComponent {
  /** Generated CIRCABC API client used to read and persist space sharing data. */
  private readonly spaceService = inject(SpaceService);
  /** Builder used to construct the reactive space sharing form. */
  private readonly formBuilder = inject(FormBuilder);

  /**
   * Two-way bound flag controlling the visibility of the sharing modal.
   * Set to `false` by the component once an operation completes or is cancelled.
   */
  public showModal = model<boolean>(false);
  /** Required identifier of the space to be shared. */
  public readonly spaceId = input.required<string>();
  /**
   * Optional identifier of an interest group whose existing share is being
   * edited. When provided, the component operates in "change permission" mode.
   */
  public readonly igId = input<string>();
  /**
   * Optional current permission of the interest group identified by {@link igId}.
   * Used to detect whether the selected permission actually changed.
   */
  public readonly currentPermission = input<string>();
  /**
   * Emits the result (success/cancel and action type) whenever the modal is
   * closed after a share, permission change or cancellation.
   */
  public readonly modalHide = output<ActionEmitterResult>();

  /**
   * Fetches the interest groups the space can be shared with and the selectable
   * permissions from the backend whenever {@link spaceId} changes. There is no
   * dedicated error UI for this load, so failures are logged and a safe empty
   * default is returned instead of leaving the resource in the error state.
   */
  private readonly shareIGsAndPermissionsResource = resource({
    params: () => this.spaceId(),
    loader: async ({ params: id }) => {
      try {
        return await this.spaceService.getShareIGsAndPermissionsAsync({ id });
      } catch (e) {
        console.error(e);
        return {};
      }
    },
    defaultValue: {},
  });

  /** Available interest groups to share with and the selectable permissions. */
  public readonly shareIGsAndPermissions =
    this.shareIGsAndPermissionsResource.value;

  /** `true` while a share request is in flight (drives the spinner/disabled state). */
  public readonly sharing = signal(false);
  /** `true` while a permission-change request is in flight. */
  public readonly changing = signal(false);

  /** Reactive form holding the selected IG, permission and notify-leaders flag. */
  public spaceSharingForm: FormGroup = this.formBuilder.group(
    {
      selectedIg: [''],
      selectedPermission: [''],
      notifyLeaders: [true],
    },
    {
      updateOn: 'change',
    }
  );

  /**
   * Resets the sharing form to sensible defaults whenever new IGs/permissions
   * data is loaded. This syncs the loaded resource value into the imperative,
   * non-signal `FormGroup` API, which is the documented last-resort use case
   * for `effect`.
   */
  constructor() {
    effect(() => {
      this.shareIGsAndPermissionsResource.value();
      this.resetForm();
    });
  }

  /**
   * Indicates whether there are still interest groups available to share with.
   *
   * @returns `true` if at least one interest group can be selected, otherwise `false`.
   */
  public stillIGsToShare(): boolean {
    const igs = this.shareIGsAndPermissions().igs;
    return igs !== undefined && igs.length > 0;
  }

  /**
   * Checks that both an interest group and a permission have been selected in the form.
   *
   * @returns `true` when both selections are non-empty, otherwise `false`.
   */
  public allSelected() {
    return (
      this.spaceSharingForm.value.selectedIg !== '' &&
      this.spaceSharingForm.value.selectedPermission !== ''
    );
  }

  /**
   * Shares the space with the selected interest group using the selected
   * permission, optionally notifying the group leaders. On success the form is
   * reset, a {@link ActionType.SHARE_SPACE} success result is emitted through
   * {@link modalHide} and the modal is closed.
   *
   * @returns A promise that resolves once the share request completes and the
   * result has been emitted.
   */
  public async share() {
    this.sharing.set(true);

    const share: Share = {};

    share.igId = this.spaceSharingForm.controls.selectedIg.value;
    share.permission = this.spaceSharingForm.controls.selectedPermission.value;

    await this.spaceService.postShareSpaceAsync({
      id: this.spaceId(),
      notifyLeaders: this.spaceSharingForm.controls.notifyLeaders.value,
      share,
    });

    this.resetForm();

    const result: ActionEmitterResult = {};
    result.result = ActionResult.SUCCEED;
    result.type = ActionType.SHARE_SPACE;
    this.modalHide.emit(result);

    this.sharing.set(false);
    this.showModal.set(false);
  }

  /**
   * Updates the permission of the interest group identified by {@link igId} for
   * the current space. If no {@link igId} is set or the selected permission
   * equals the {@link currentPermission} (i.e. nothing changed), the operation
   * is treated as a cancellation via {@link cancel}. On a successful update the
   * form is reset, a {@link ActionType.SHARE_SPACE_CHANGE_PERMISSION} success
   * result is emitted through {@link modalHide} and the modal is closed.
   *
   * @returns A promise that resolves once the update (or cancellation) completes.
   */
  public async change() {
    const permission = this.spaceSharingForm.controls.selectedPermission.value;
    // don't do anything if the current permission is the same as the selected one
    // as this means that the user is not changing it
    const igId = this.igId();
    if (igId && this.currentPermission() !== permission) {
      this.changing.set(true);

      await this.spaceService.putShareSpacePermissionUpdateAsync({
        id: this.spaceId(),
        igId,
        permission,
        notifyLeaders: this.spaceSharingForm.controls.notifyLeaders.value,
      });

      this.resetForm();

      const result: ActionEmitterResult = {};
      result.result = ActionResult.SUCCEED;
      result.type = ActionType.SHARE_SPACE_CHANGE_PERMISSION;
      this.modalHide.emit(result);

      this.changing.set(false);
      this.showModal.set(false);
    } else {
      this.cancel('close');
    }
  }

  /**
   * Resets the sharing form to its default state: selects the first available
   * interest group and permission (or empty when none are available) and
   * re-enables the notify-leaders flag.
   */
  private resetForm() {
    const shareData = this.shareIGsAndPermissions();
    if (shareData.igs !== undefined && shareData.igs.length > 0) {
      this.spaceSharingForm.controls.selectedIg.patchValue(
        shareData.igs[0].value as string
      );
    } else {
      this.spaceSharingForm.controls.selectedIg.patchValue('');
    }

    if (
      shareData.permissions !== undefined &&
      shareData.permissions.length > 0
    ) {
      this.spaceSharingForm.controls.selectedPermission.patchValue(
        shareData.permissions[0]
      );
    } else {
      this.spaceSharingForm.controls.selectedPermission.patchValue('');
    }

    this.spaceSharingForm.controls.notifyLeaders.patchValue(true);
  }

  /**
   * Handles cancellation of the sharing dialog. When invoked with `'close'`,
   * it hides the modal, resets the form and emits a {@link ActionResult.CANCELED}
   * result of type {@link ActionType.SHARE_SPACE} through {@link modalHide}.
   *
   * @param backTo Cancellation mode; only the value `'close'` triggers closing
   * and emitting the cancellation result.
   */
  public cancel(backTo: string) {
    if (backTo === 'close') {
      this.showModal.set(false);
      this.resetForm();
      const result: ActionEmitterResult = {};
      result.result = ActionResult.CANCELED;
      result.type = ActionType.SHARE_SPACE;
      this.modalHide.emit(result);
    }
  }

  /**
   * Indicates whether the component operates in "change permission" mode, i.e.
   * it targets an existing share rather than creating a new one.
   *
   * @returns `true` when an {@link igId} is provided, otherwise `false`.
   */
  public toChangePermission(): boolean {
    return this.igId() !== undefined;
  }
}
