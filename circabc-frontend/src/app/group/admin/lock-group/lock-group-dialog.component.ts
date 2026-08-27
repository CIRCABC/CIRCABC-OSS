import {
  Component,
  ChangeDetectionStrategy,
  inject,
  signal,
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef,
} from '@angular/material/dialog';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { TranslocoModule } from '@jsverse/transloco';
import { SharedModule } from 'primeng/api';
import { EditorModule } from 'primeng/editor';
import { GroupLockService } from 'app/core/generated/circabc';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';
import { firstValueFrom } from 'rxjs';

export interface LockGroupDialogData {
  groupId: string;
}

@Component({
  selector: 'cbc-lock-group-dialog',
  templateUrl: './lock-group-dialog.component.html',
  styleUrl: './lock-group-dialog.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatSlideToggleModule,
    TranslocoModule,
    EditorModule,
    SharedModule,
    SpinnerComponent,
  ],
})
export class LockGroupDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly dialogRef = inject(MatDialogRef<LockGroupDialogComponent>);
  private readonly data: LockGroupDialogData = inject(MAT_DIALOG_DATA);
  private readonly groupLockService = inject(GroupLockService);
  private readonly uiMessageService = inject(UiMessageService);

  readonly loading = signal(false);

  readonly lockForm = this.fb.group({
    message: [''],
    readOnly: [false],
  });

  onLock(): void {
    if (this.loading()) {
      return;
    }

    this.loading.set(true);

    const request = {
      message: this.lockForm.controls.message.value || undefined,
      readOnly: this.lockForm.controls.readOnly.value ?? false,
    };

    firstValueFrom(this.groupLockService.lockGroup(this.data.groupId, request))
      .then(() => {
        this.uiMessageService.addSuccessMessage(
          'successfully locked the interest group',
          true
        );
        this.dialogRef.close(true);
      })
      .catch((error) => {
        console.error('Failed to lock interest group', error);
        this.loading.set(false);
      });
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}
