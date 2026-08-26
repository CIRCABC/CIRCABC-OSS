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
import { Node as ModelNode, SpaceService } from 'app/core/generated/circabc';
import { fileNameValidator } from 'app/core/validation.service';
import { ControlMessageComponent } from 'app/shared/control-message/control-message.component';
import { ModalComponent } from 'app/shared/modal/modal.component';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-add-url',
  templateUrl: './add-url.component.html',
  preserveWhitespaces: true,
  imports: [
    ModalComponent,
    ReactiveFormsModule,
    ControlMessageComponent,
    TranslocoModule,
  ],
})
export class AddUrlComponent implements OnInit {
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  showModal = false;
  public readonly parentNode = input.required<ModelNode>();
  public readonly modalHide = output<ActionEmitterResult>();

  public createUrlForm!: FormGroup;
  public processing = false;

  constructor(
    private fb: FormBuilder,
    private spaceService: SpaceService
  ) {}

  ngOnInit() {
    this.createUrlForm = this.fb.group(
      {
        name: ['', [Validators.required, fileNameValidator]],
        url: ['', Validators.required],
      },
      {
        updateOn: 'change',
      }
    );
  }

  async createUrl() {
    this.processing = true;
    const parentNode = this.parentNode();
    if (parentNode.id !== undefined) {
      const res: ActionEmitterResult = {};
      res.type = ActionType.ADD_URL;

      const body: ModelNode = {
        name: this.createUrlForm.value.name,
        properties: {
          url: this.createUrlForm.value.url,
        },
      };

      try {
        await firstValueFrom(this.spaceService.postURL(parentNode.id, body));
        res.result = ActionResult.SUCCEED;
        this.showModal = false;
        this.createUrlForm.reset();
      } catch (_error) {
        res.result = ActionResult.FAILED;
      }

      this.modalHide.emit(res);
    }
    this.processing = false;
  }

  cancel() {
    this.showModal = false;
    this.createUrlForm.reset({ name: '', url: '' });
    const res: ActionEmitterResult = {};
    res.result = ActionResult.CANCELED;
    res.type = ActionType.ADD_URL;
    this.modalHide.emit(res);
  }

  get nameControl(): AbstractControl {
    return this.createUrlForm.controls.name;
  }

  get urlControl(): AbstractControl {
    return this.createUrlForm.controls.url;
  }
}
