import { Component, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { ExternalRepositoryData } from 'app/core/generated/circabc';

export interface DialogData {
  title: string;
  message: string;
  message2: string;
  labelOK: string;
  labelCancel: string;
  layoutStyle: string;
  nodeLog: ExternalRepositoryData[];
}
@Component({
  selector: 'cbc-confirm-dialog',
  templateUrl: './confirm-dialog.component.html',
  styleUrls: ['./confirm-dialog.component.scss'],
})
export class ConfirmDialogComponent {
  dialogData!: DialogData;

  constructor(
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
}
