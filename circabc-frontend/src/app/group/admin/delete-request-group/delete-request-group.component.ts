import { Component, Inject, OnInit } from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';
import { LoginService } from 'app/core/login.service';
import { AbstractControl, ReactiveFormsModule } from '@angular/forms';
import { MultilingualInputComponent } from '../../../shared/input/multilingual-input.component';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { SpinnerComponent } from '../../../shared/spinner/spinner.component';
import { firstValueFrom } from 'rxjs';
import { ControlMessageComponent } from '../../../shared/control-message/control-message.component';
import { User } from 'app/core/generated/circabc/model/user';
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
  public form!: FormGroup;
  public groupDeletionRequestInput!: GroupDeletionRequestInput;
  private user!: User;
  public processing = false;
  public isPending = true;
  public dialogData!: DialogData;

  constructor(
    private fb: FormBuilder,
    private loginService: LoginService,
    private categoryService: CategoryService,
    public dialogRef: MatDialogRef<DeleteRequestGroupComponent>,
    @Inject(MAT_DIALOG_DATA) public data: DialogData
  ) {}

  async ngOnInit() {
    this.form = this.fb.group({
      name: [{ value: '', disabled: true }, Validators.required],
      title: [],
      justification: [],
    });
    this.form.controls.name.patchValue(this.data.group.name);
    this.form.controls.title.patchValue(this.data.group.title);

    this.user = this.loginService.getUser();

    if (this.data.group.id) {
      this.processing = true;
      this.isPending = await firstValueFrom(
        this.categoryService.isDeleteRequestPending(this.data.group.id)
      );
      this.processing = false;
    }
  }

  public async requestDeleteGroup() {
    this.processing = true;
    if (this.user) {
      if (this.form.value.justification) {
        this.form.controls.justification.patchValue(
          this.form.value.justification
        );
      } else {
        this.form.controls.justification.patchValue({ en: '' });
      }
      this.groupDeletionRequestInput = {
        justification: this.form.value.justification.en,
      };
      if (this.data.group.id) {
        await firstValueFrom(
          this.categoryService.postGroupDeletionRequest(
            this.data.group.id,
            this.groupDeletionRequestInput
          )
        );
      }
    }
    this.processing = false;
    this.dialogRef.close();
  }

  get justificationControl(): AbstractControl {
    return this.form.controls.justification;
  }
}
