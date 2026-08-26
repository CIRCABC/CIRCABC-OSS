import {
  Component,
  Input,
  OnChanges,
  OnInit,
  SimpleChanges,
  output,
} from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { TranslocoModule } from '@jsverse/transloco';
import { ActionEmitterResult, ActionResult } from 'app/action-result';
import { HelpLink, HelpService } from 'app/core/generated/circabc';
import { urlValidator } from 'app/core/validation.service';
import { ControlMessageComponent } from 'app/shared/control-message/control-message.component';
import { MultilingualInputComponent } from 'app/shared/input/multilingual-input.component';
import { ModalComponent } from 'app/shared/modal/modal.component';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-add-link',
  templateUrl: './add-link.component.html',
  preserveWhitespaces: true,
  imports: [
    ModalComponent,
    ReactiveFormsModule,
    MultilingualInputComponent,
    ControlMessageComponent,
    TranslocoModule,
  ],
})
export class AddLinkComponent implements OnInit, OnChanges {
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  showModal = false;
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  linkId!: string | undefined;
  readonly showModalChange = output<boolean>();
  readonly linkIdChange = output();
  readonly linkCreated = output<ActionEmitterResult>();
  readonly linkUpdated = output<ActionEmitterResult>();

  public creating = false;
  public newLinkForm!: FormGroup;
  public editMode = false;
  public linkToEdit!: HelpLink | undefined;
  public isValid = false;

  constructor(
    private fb: FormBuilder,
    private helpService: HelpService
  ) {}

  ngOnInit() {
    this.newLinkForm = this.fb.group({
      title: ['', Validators.required],
      href: ['', [Validators.required, urlValidator]],
    });

    this.newLinkForm.valueChanges.subscribe((_value) => {
      this.computeValidity();
    });
  }

  async ngOnChanges(changes: SimpleChanges) {
    if (changes.linkId) {
      if (changes.linkId.currentValue) {
        this.linkToEdit = await firstValueFrom(
          this.helpService.getHelpLink(changes.linkId.currentValue)
        );
        this.newLinkForm.patchValue(this.linkToEdit);
        this.editMode = true;
      }
    }
  }

  public async updateLink() {
    this.creating = true;

    try {
      if (this.linkId) {
        const body: HelpLink = this.newLinkForm.value;
        body.id = this.linkId;

        await firstValueFrom(this.helpService.updateHelpLink(body.id, body));

        this.linkCreated.emit({ result: ActionResult.SUCCEED });
        this.newLinkForm.reset();
        this.editMode = false;
        this.linkId = undefined;
      }
    } catch (error) {
      console.error(error);
    }

    this.creating = false;
  }

  public async createLink() {
    this.creating = true;

    try {
      await firstValueFrom(
        this.helpService.createHelpLink(this.newLinkForm.value)
      );
      this.linkCreated.emit({ result: ActionResult.SUCCEED });
      this.newLinkForm.reset();
    } catch (error) {
      console.error(error);
    }

    this.creating = false;
  }

  public cancel() {
    this.showModal = false;
    this.newLinkForm.reset();
    this.linkToEdit = undefined;
    this.editMode = false;
    this.linkId = undefined;
    this.linkIdChange.emit(this.linkId);
    this.showModalChange.emit(this.showModal);
  }

  get hrefControl() {
    return this.newLinkForm.controls.href;
  }

  private computeValidity() {
    if (this.newLinkForm) {
      this.isValid = this.newLinkForm.valid;
    } else {
      this.isValid = false;
    }
  }
}
