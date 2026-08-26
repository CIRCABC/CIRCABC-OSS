import { Component, OnInit, inject } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { TranslocoModule } from '@jsverse/transloco';
import {
  CategoryService,
  GroupDeletionRequestApproval,
} from 'app/core/generated/circabc';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';
import { EditorModule } from 'primeng/editor';
import { firstValueFrom } from 'rxjs';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'cbc-decline-delete-form',
  templateUrl: './decline-delete-form.component.html',
  styleUrl: './decline-delete-form.component.scss',
  imports: [
    ReactiveFormsModule,
    EditorModule,
    SpinnerComponent,
    TranslocoModule,
    CommonModule,
  ],
})
export class DeclineDeleteFormComponent implements OnInit {
  fb = inject(FormBuilder);
  data = inject(MAT_DIALOG_DATA);
  dialogRef = inject(MatDialogRef<DeclineDeleteFormComponent>);
  categoryService = inject(CategoryService);
  decliningForm!: FormGroup;
  processing = false;
  groupDeletionRequestApproval!: GroupDeletionRequestApproval;

  ngOnInit() {
    this.decliningForm = this.fb.group({
      argument: ['', Validators.required],
    });

    this.groupDeletionRequestApproval = {
      id: this.data.request.id,
      argument: '',
      agreement: -1,
    };
  }

  async reject() {
    this.groupDeletionRequestApproval.argument =
      this.decliningForm.controls.argument.value;

    this.processing = true;
    await firstValueFrom(
      this.categoryService.validateInterestGroupDeleteRequests(
        this.data.categoryId,
        this.groupDeletionRequestApproval
      )
    );
    this.processing = false;
    this.dialogRef.close(true);
  }
}
