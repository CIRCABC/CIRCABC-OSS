import { Component, Input, OnInit, output, input } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';

import { TranslocoModule } from '@jsverse/transloco';
import { ContentService } from 'app/core/generated/circabc';
import { ModalComponent } from 'app/shared/modal/modal.component';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-checkin',
  templateUrl: './checkin.component.html',
  styleUrl: './checkin.component.scss',
  preserveWhitespaces: true,
  imports: [ModalComponent, ReactiveFormsModule, TranslocoModule],
})
export class CheckinComponent implements OnInit {
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  public showModal = false;
  public readonly modalHide = output();
  public readonly checkedIn = output();
  public readonly nodeId = input.required<string>();
  public form!: FormGroup;

  // to enable/disable the spinner for lengthy operations
  public processing = false;

  public constructor(
    private contentService: ContentService,
    private formBuilder: FormBuilder
  ) {}

  public ngOnInit(): void {
    this.buildForm();
  }

  private buildForm() {
    this.form = this.formBuilder.group(
      {
        comment: [''],
        minorChange: [true],
        keepCheckedOut: [false],
      },
      {
        updateOn: 'change',
      }
    );
  }

  public closePopupWindow(): void {
    this.showModal = false;
    this.modalHide.emit();
    this.processing = false;
  }

  public async checkin() {
    this.processing = true;

    // checkin document
    await firstValueFrom(
      this.contentService.putCheckin(
        this.nodeId(),
        this.form.controls.comment.value,
        this.form.controls.minorChange.value,
        this.form.controls.keepCheckedOut.value
      )
    );

    this.form.reset();
    this.form.controls.minorChange.patchValue(true);

    // emit an event to signal that the document has been checked in
    // will be used by the library details to display the document
    this.checkedIn.emit();

    // close form/wizard
    this.closePopupWindow();
  }
}
