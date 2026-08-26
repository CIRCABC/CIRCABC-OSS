import { Component, Input, OnInit, output, input } from '@angular/core';
import {
  AbstractControl,
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
  Node as ModelNode,
  ShareSpaceItem,
  SpaceService,
} from 'app/core/generated/circabc';
import { ControlMessageComponent } from 'app/shared/control-message/control-message.component';
import { ModalComponent } from 'app/shared/modal/modal.component';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-add-shared-space-link',
  templateUrl: './add-shared-space-link.component.html',
  preserveWhitespaces: true,
  imports: [
    ModalComponent,
    ReactiveFormsModule,
    ControlMessageComponent,
    TranslocoModule,
  ],
})
export class AddSharedSpaceLinkComponent implements OnInit {
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  showModal = false;
  public readonly parentNode = input.required<ModelNode>();
  public readonly sharedSpaceItems = input.required<ShareSpaceItem[]>();
  public readonly modalHide = output<ActionEmitterResult>();

  public createSharedSpaceLinkForm!: FormGroup;
  public processing = false;

  constructor(
    private fb: FormBuilder,
    private spaceService: SpaceService
  ) {}

  ngOnInit() {
    this.createSharedSpaceLinkForm = this.fb.group(
      {
        title: ['', Validators.required],
        description: [''],
        sharedSpaceId: ['', Validators.required],
      },
      {
        updateOn: 'change',
      }
    );
  }

  async createSharedSpaceLink() {
    this.processing = true;

    const parentNode = this.parentNode();
    if (parentNode.id !== undefined) {
      const res: ActionEmitterResult = {};
      res.type = ActionType.ADD_SHARED_SPACE_LINK;

      try {
        await firstValueFrom(
          this.spaceService.postExportedSharedSpace(
            this.createSharedSpaceLinkForm.value.sharedSpaceId,
            parentNode.id,
            this.createSharedSpaceLinkForm.value.title,
            this.createSharedSpaceLinkForm.value.description
          )
        );
        res.result = ActionResult.SUCCEED;
        this.showModal = false;
        this.createSharedSpaceLinkForm.reset();
      } catch (_error) {
        res.result = ActionResult.FAILED;
      }

      this.modalHide.emit(res);
    }

    this.processing = false;
  }

  cancel() {
    this.showModal = false;
    this.createSharedSpaceLinkForm.reset();
    const res: ActionEmitterResult = {};
    res.result = ActionResult.CANCELED;
    res.type = ActionType.ADD_SHARED_SPACE_LINK;
    this.modalHide.emit(res);
  }

  get titleControl(): AbstractControl {
    return this.createSharedSpaceLinkForm.controls.title;
  }

  get descriptionControl(): AbstractControl {
    return this.createSharedSpaceLinkForm.controls.description;
  }

  get sharedSpaceIdControl(): AbstractControl {
    return this.createSharedSpaceLinkForm.controls.sharedSpaceId;
  }
}
