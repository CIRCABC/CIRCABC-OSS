import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  Validators,
} from '@angular/forms';
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
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-add-shared-space-link',
  templateUrl: './add-shared-space-link.component.html',
  preserveWhitespaces: true,
})
export class AddSharedSpaceLinkComponent implements OnInit {
  @Input()
  showModal = false;
  @Input()
  public parentNode!: ModelNode;
  @Input()
  public sharedSpaceItems!: ShareSpaceItem[];
  @Output()
  public readonly modalHide = new EventEmitter<ActionEmitterResult>();

  public createSharedSpaceLinkForm!: FormGroup;
  public processing = false;

  constructor(private fb: FormBuilder, private spaceService: SpaceService) {}

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

    if (this.parentNode.id !== undefined) {
      const res: ActionEmitterResult = {};
      res.type = ActionType.ADD_SHARED_SPACE_LINK;

      try {
        await firstValueFrom(
          this.spaceService.postExportedSharedSpace(
            this.createSharedSpaceLinkForm.value.sharedSpaceId,
            this.parentNode.id,
            this.createSharedSpaceLinkForm.value.title,
            this.createSharedSpaceLinkForm.value.description
          )
        );
        res.result = ActionResult.SUCCEED;
        this.showModal = false;
        this.createSharedSpaceLinkForm.reset();
      } catch (error) {
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
