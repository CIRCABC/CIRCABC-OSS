import { Component, OnInit, inject } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { TranslocoModule } from '@jsverse/transloco';
import { CategoryService } from 'app/core/generated/circabc';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';
import { EditorModule } from 'primeng/editor';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-declne-form',
  templateUrl: './decline-form.component.html',
  styleUrl: './decline-form.component.scss',
  imports: [
    ReactiveFormsModule,
    EditorModule,
    SpinnerComponent,
    TranslocoModule,
  ],
})
export class DeclineFormComponent implements OnInit {
  fb = inject(FormBuilder);
  data = inject(MAT_DIALOG_DATA);
  dialogRef = inject(MatDialogRef<DeclineFormComponent>);
  categoryService = inject(CategoryService);

  request = this.data.request;
  categoryId = this.data.categoryId;
  decliningForm!: FormGroup;
  processing = false;

  ngOnInit() {
    this.decliningForm = this.fb.group({
      argument: ['', Validators.required],
      agreement: -1,
      id: this.request.id,
    });
  }

  public async reject() {
    this.processing = true;
    try {
      if (this.categoryId && this.request.id) {
        await firstValueFrom(
          this.categoryService.validateInterestGroupRequests(
            this.categoryId,
            this.request.id,
            this.decliningForm.value
          )
        );
      }
    } catch (error) {
      console.error(error);
    }
    this.processing = false;
    this.dialogRef.close(true);
  }
}
