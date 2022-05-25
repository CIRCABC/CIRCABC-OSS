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
import { Node as ModelNode, SpaceService } from 'app/core/generated/circabc';
import { ValidationService } from 'app/core/validation.service';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-add-url',
  templateUrl: './add-url.component.html',
  preserveWhitespaces: true,
})
export class AddUrlComponent implements OnInit {
  @Input()
  showModal = false;
  @Input()
  public parentNode!: ModelNode;
  @Output()
  public readonly modalHide = new EventEmitter<ActionEmitterResult>();

  public createUrlForm!: FormGroup;
  public processing = false;

  constructor(private fb: FormBuilder, private spaceService: SpaceService) {}

  ngOnInit() {
    this.createUrlForm = this.fb.group(
      {
        name: ['', [Validators.required, ValidationService.fileNameValidator]],
        url: ['', Validators.required],
      },
      {
        updateOn: 'change',
      }
    );
  }

  async createUrl() {
    this.processing = true;
    if (this.parentNode.id !== undefined) {
      const res: ActionEmitterResult = {};
      res.type = ActionType.ADD_URL;

      const body: ModelNode = {
        name: this.createUrlForm.value.name,
        properties: {
          url: this.createUrlForm.value.url,
        },
      };

      try {
        await firstValueFrom(
          this.spaceService.postURL(this.parentNode.id, body)
        );
        res.result = ActionResult.SUCCEED;
        this.showModal = false;
        this.createUrlForm.reset();
      } catch (error) {
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
