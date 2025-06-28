import { Component, Inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef,
} from '@angular/material/dialog';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { TranslocoModule } from '@jsverse/transloco';
import { ExternalRepositoryData } from 'app/core/generated/circabc';

export interface DialogData {
  title: string;
  message: string;
  message2: string;
  messageTranslated?: string;
  labelOK: string;
  labelCancel: string;
  layoutStyle: string;
  nodeLog: ExternalRepositoryData[];
  notify: boolean | null;
}
@Component({
  selector: 'cbc-confirm-dialog',
  templateUrl: './confirm-dialog.component.html',
  styleUrl: './confirm-dialog.component.scss',
  imports: [
    MatCardModule,
    MatDialogModule,
    ReactiveFormsModule,
    MatSlideToggleModule,
    TranslocoModule,
  ],
})
export class ConfirmDialogComponent {
  dialogData!: DialogData;
  notifyFormGroup = this.notifyFormBuilder.group({
    notify: true,
  });
  constructor(
    private notifyFormBuilder: FormBuilder,
    public dialogRef: MatDialogRef<ConfirmDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: DialogData
  ) {}

  onOk(): void {
    // Close the dialog, return true
    this.dialogRef.close(true);
  }

  onCancel(): void {
    // Close the dialog, return false
    this.dialogRef.close(false);
  }

  closeNotification(): void {
    this.dialogRef.close(this.notifyFormGroup.controls.notify.value);
  }
}
