import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  Validators,
} from '@angular/forms';

import { Header, HeaderService } from 'app/core/generated/circabc';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-add-header',
  templateUrl: './add-header.component.html',
  preserveWhitespaces: true,
})
export class AddHeaderComponent implements OnInit {
  @Input()
  public showModal = false;
  @Output()
  public readonly showModalChange = new EventEmitter();
  @Input()
  public headers!: Header[];
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
    if (!this.headers) {
      return null;
    }
    const name = control.value;

    const headerNames = this.headers.map((header: Header) => header.name);
    const no = headerNames.includes(name);
    return no ? { forbiddenNameArray: { name } } : null;
  }

  public cancel() {
    this.showModal = false;
    this.showModalChange.emit();
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
