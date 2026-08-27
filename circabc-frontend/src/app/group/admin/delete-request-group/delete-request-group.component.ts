import { Component, OnInit, inject } from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';
import { LoginService } from 'app/core/login.service';
import { AbstractControl, ReactiveFormsModule } from '@angular/forms';
import { MultilingualInputComponent } from '../../../shared/input/multilingual-input.component';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { SpinnerComponent } from '../../../shared/spinner/spinner.component';
import { firstValueFrom } from 'rxjs';
import { ControlMessageComponent } from '../../../shared/control-message/control-message.component';
import {
  InterestGroup,
  CategoryService,
  GroupDeletionRequestInput,
} from 'app/core/generated/circabc';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatCardModule } from '@angular/material/card';

export interface DialogData {
  group: InterestGroup;
}

type ComponentState = 'checking' | 'form' | 'pending' | 'submitting' | 'error';

@Component({
  selector: 'cbc-group-delete-request-post',
  templateUrl: './delete-request-group.component.html',
  styleUrl: './delete-request-group.component.scss',
  imports: [
    TranslocoModule,
    MultilingualInputComponent,
    ReactiveFormsModule,
    SpinnerComponent,
    ControlMessageComponent,
    MatCardModule,
  ],
})
export class DeleteRequestGroupComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly loginService = inject(LoginService);
  private readonly categoryService = inject(CategoryService);
  readonly dialogRef = inject(MatDialogRef<DeleteRequestGroupComponent>);
  readonly data = inject<DialogData>(MAT_DIALOG_DATA);

  public form!: FormGroup;
  public state: ComponentState = 'checking';

  async ngOnInit() {
    this.form = this.fb.group({
      name: [{ value: '', disabled: true }, Validators.required],
      title: [],
      justification: [],
    });
    this.form.controls['name'].patchValue(this.data.group.name);
    this.form.controls['title'].patchValue(this.data.group.title);

    await this.checkPendingRequest();
  }

  private async checkPendingRequest(): Promise<void> {
    if (!this.data.group.id) {
      this.state = 'form';
      return;
    }

    this.state = 'checking';
    try {
      const isPending = await firstValueFrom(
        this.categoryService.isDeleteRequestPending(this.data.group.id)
      );
      this.state = isPending ? 'pending' : 'form';
    } catch {
      this.state = 'error';
    }
  }

  public async requestDeleteGroup(): Promise<void> {
    const user = this.loginService.getUser();
    if (!(user && this.data.group.id)) {
      return;
    }

    this.state = 'submitting';
    try {
      const justificationValue = this.form.value.justification;
      const groupDeletionRequestInput: GroupDeletionRequestInput = {
        justification: justificationValue?.en ?? '',
      };

      await firstValueFrom(
        this.categoryService.postGroupDeletionRequest(
          this.data.group.id,
          groupDeletionRequestInput
        )
      );
      this.dialogRef.close(true);
    } catch {
      this.state = 'error';
    }
  }

  get justificationControl(): AbstractControl {
    return this.form.controls['justification'];
  }
}
