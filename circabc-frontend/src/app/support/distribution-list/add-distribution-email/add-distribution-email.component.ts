import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import {
  AppMessageService,
  DistributionMail,
} from 'app/core/generated/circabc';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-add-distribution-email',
  templateUrl: './add-distribution-email.component.html',
  styleUrls: ['./add-distribution-email.component.scss'],
})
export class AddDistributionEmailComponent implements OnInit {
  @Input() showModal = false;
  @Output() readonly showModalChange = new EventEmitter<boolean>();
  @Output() readonly modalClosed = new EventEmitter();

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
