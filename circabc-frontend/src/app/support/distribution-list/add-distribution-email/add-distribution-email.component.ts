import { Component, Input, OnInit, output } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { TranslocoModule } from '@jsverse/transloco';
import {
  AppMessageService,
  DistributionMail,
} from 'app/core/generated/circabc';
import { ControlMessageComponent } from 'app/shared/control-message/control-message.component';
import { ModalComponent } from 'app/shared/modal/modal.component';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-add-distribution-email',
  templateUrl: './add-distribution-email.component.html',
  styleUrl: './add-distribution-email.component.scss',
  imports: [
    ModalComponent,
    ReactiveFormsModule,
    ControlMessageComponent,
    SpinnerComponent,
    TranslocoModule,
  ],
})
export class AddDistributionEmailComponent implements OnInit {
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input() showModal = false;
  readonly showModalChange = output<boolean>();
  readonly modalClosed = output();

  public emailForm!: FormGroup;
  public emailCreated: DistributionMail[] = [];
  public processing = false;

  constructor(
    private fb: FormBuilder,
    private appMessageService: AppMessageService
  ) {}

  ngOnInit() {
    this.emailForm = this.fb.group({
      emailAddress: ['', Validators.email],
    });
  }

  cancel() {
    this.showModal = false;
    this.showModalChange.emit(this.showModal);
    this.emailCreated = [];
    this.modalClosed.emit();
  }

  get emailAddressControl() {
    return this.emailForm?.controls.emailAddress;
  }

  public async addDistributionEmail() {
    if (
      this.emailForm.value.emailAddress &&
      this.emailForm.value.emailAddress !== 'null' &&
      this.emailForm.value.emailAddress !== '' &&
      this.emailForm.valid
    ) {
      try {
        this.processing = true;
        const body: DistributionMail[] = [];
        body.push(this.emailForm.value);
        await firstValueFrom(
          this.appMessageService.addDistributionEmails(body)
        );
        this.emailCreated.push(this.emailForm.value);
        this.emailForm.controls.emailAddress.reset();
      } catch (error) {
        console.error(error);
      } finally {
        this.processing = false;
      }
    }
  }
}
