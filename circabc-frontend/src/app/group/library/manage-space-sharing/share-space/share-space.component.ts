import {
  Component,
  Input,
  OnChanges,
  OnInit,
  output,
  input,
} from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { TranslocoModule } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import {
  Share,
  ShareIGsAndPermissions,
  SpaceService,
} from 'app/core/generated/circabc';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-share-space',
  templateUrl: './share-space.component.html',
  styleUrl: './share-space.component.scss',
  preserveWhitespaces: true,
  imports: [ReactiveFormsModule, SpinnerComponent, TranslocoModule],
})
export class ShareSpaceComponent implements OnInit, OnChanges {
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  public showModal = false;
  public readonly spaceId = input.required<string>();
  public readonly igId = input<string>();
  public readonly currentPermission = input<string>();
  public readonly modalHide = output<ActionEmitterResult>();

  public shareIGsAndPermissions!: ShareIGsAndPermissions;

  public sharing = false;
  public changing = false;

  public spaceSharingForm!: FormGroup;

  constructor(
    private spaceService: SpaceService,
    private formBuilder: FormBuilder
  ) {}

  async ngOnInit() {
    this.spaceSharingForm = this.formBuilder.group(
      {
        selectedIg: [''],
        selectedPermission: [''],
        notifyLeaders: [true],
      },
      {
        updateOn: 'change',
      }
    );
  }

  async ngOnChanges() {
    await this.loadShareIGsAndPermissions();
  }

  public async loadShareIGsAndPermissions() {
    this.shareIGsAndPermissions = await firstValueFrom(
      this.spaceService.getShareIGsAndPermissions(this.spaceId())
    );
    this.resetForm();
  }

  public stillIGsToShare(): boolean {
    return (
      this.shareIGsAndPermissions.igs !== undefined &&
      this.shareIGsAndPermissions.igs.length > 0
    );
  }

  public allSelected() {
    return (
      this.spaceSharingForm.value.selectedIg !== '' &&
      this.spaceSharingForm.value.selectedPermission !== ''
    );
  }

  public async share() {
    this.sharing = true;

    const share: Share = {};

    share.igId = this.spaceSharingForm.controls.selectedIg.value;
    share.permission = this.spaceSharingForm.controls.selectedPermission.value;

    await firstValueFrom(
      this.spaceService.postShareSpace(
        this.spaceId(),
        this.spaceSharingForm.controls.notifyLeaders.value,
        share
      )
    );

    this.resetForm();

    const result: ActionEmitterResult = {};
    result.result = ActionResult.SUCCEED;
    result.type = ActionType.SHARE_SPACE;
    this.modalHide.emit(result);

    this.sharing = false;
    this.showModal = false;
  }

  public async change() {
    const permission = this.spaceSharingForm.controls.selectedPermission.value;
    // don't do anything if the current permission is the same as the selected one
    // as this means that the user is not changing it
    const igId = this.igId();
    if (igId && this.currentPermission() !== permission) {
      this.changing = true;

      await firstValueFrom(
        this.spaceService.putShareSpacePermissionUpdate(
          this.spaceId(),
          igId,
          permission,
          this.spaceSharingForm.controls.notifyLeaders.value
        )
      );

      this.resetForm();

      const result: ActionEmitterResult = {};
      result.result = ActionResult.SUCCEED;
      result.type = ActionType.SHARE_SPACE_CHANGE_PERMISSION;
      this.modalHide.emit(result);

      this.changing = false;
      this.showModal = false;
    } else {
      this.cancel('close');
    }
  }

  private resetForm() {
    if (
      this.shareIGsAndPermissions?.igs !== undefined &&
      this.shareIGsAndPermissions.igs.length > 0
    ) {
      this.spaceSharingForm.controls.selectedIg.patchValue(
        this.shareIGsAndPermissions.igs[0].value as string
      );
    } else {
      this.spaceSharingForm.controls.selectedIg.patchValue('');
    }

    if (
      this.shareIGsAndPermissions?.permissions !== undefined &&
      this.shareIGsAndPermissions.permissions.length > 0
    ) {
      this.spaceSharingForm.controls.selectedPermission.patchValue(
        this.shareIGsAndPermissions.permissions[0]
      );
    } else {
      this.spaceSharingForm.controls.selectedPermission.patchValue('');
    }

    this.spaceSharingForm.controls.notifyLeaders.patchValue(true);
  }

  public cancel(backTo: string) {
    if (backTo === 'close') {
      this.showModal = false;
      this.resetForm();
      const result: ActionEmitterResult = {};
      result.result = ActionResult.CANCELED;
      result.type = ActionType.SHARE_SPACE;
      this.modalHide.emit(result);
    }
  }

  public toChangePermission(): boolean {
    return this.igId() !== undefined;
  }
}
