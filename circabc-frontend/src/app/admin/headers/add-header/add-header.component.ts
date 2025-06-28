import { Component, OnInit, output, input, model } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';

import { TranslocoModule } from '@jsverse/transloco';
import { Header, HeaderService } from 'app/core/generated/circabc';
import { ControlMessageComponent } from 'app/shared/control-message/control-message.component';
import { DataCyDirective } from 'app/shared/directives/data-cy.directive';
import { MultilingualInputComponent } from 'app/shared/input/multilingual-input.component';
import { ModalComponent } from 'app/shared/modal/modal.component';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-add-header',
  templateUrl: './add-header.component.html',
  preserveWhitespaces: true,
  imports: [
    ModalComponent,
    ReactiveFormsModule,
    DataCyDirective,
    ControlMessageComponent,
    MultilingualInputComponent,
    SpinnerComponent,
    TranslocoModule,
  ],
})
export class AddHeaderComponent implements OnInit {
  public showModal = model<boolean>(false);
  public readonly showModalChange = output();
  public readonly headers = input<Header[]>();
  public processing = false;
  public headerForm!: FormGroup;

  constructor(
    private headerService: HeaderService,
    private formBuilder: FormBuilder
  ) {}

  ngOnInit() {
    this.headerForm = this.formBuilder.group(
      {
        // the fat arrow function captures "this" from the surrounding context and passes it on
        name: [
          '',
          [
            Validators.required,
            (control: AbstractControl) =>
              this.forbiddenNameArrayValidator(control),
          ],
        ],
        description: ['', [Validators.required]],
      },
      {
        updateOn: 'change',
      }
    );
  }

  // validates that the given name does exist
  public forbiddenNameArrayValidator(
    control: AbstractControl
  ): { [key: string]: {} } | null {
    const headers = this.headers();
    if (!headers) {
      return null;
    }
    const name = control.value;

    const headerNames = headers.map((header: Header) => header.name);
    const no = headerNames.includes(name);
    return no ? { forbiddenNameArray: { name } } : null;
  }

  public cancel() {
    this.showModal.set(false);
  }

  public async addHeader() {
    if (this.headerForm.status !== 'VALID') {
      return;
    }

    try {
      this.processing = true;

      const header: Header = { name: '' };

      header.name = this.headerForm.controls.name.value;
      header.description = this.headerForm.controls.description.value;

      await firstValueFrom(this.headerService.postHeader(header));
      this.showModalChange.emit();
    } finally {
      this.processing = false;
    }
  }

  get nameControl(): AbstractControl {
    return this.headerForm.controls.name;
  }

  get descriptionControl(): AbstractControl {
    return this.headerForm.controls.description;
  }
}
