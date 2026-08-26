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
  InterestGroupPostModel,
} from 'app/core/generated/circabc';
import { nameValidator } from 'app/core/validation.service';
import { ControlMessageComponent } from 'app/shared/control-message/control-message.component';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';
import { SharedModule } from 'primeng/api';
import { EditorModule } from 'primeng/editor';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-accept-form',
  templateUrl: './accept-form.component.html',
  styleUrl: './accept-form.component.scss',
  imports: [
    ReactiveFormsModule,
    EditorModule,
    SharedModule,
    ControlMessageComponent,
    SpinnerComponent,
    TranslocoModule,
  ],
})
export class AcceptFormComponent implements OnInit {
  fb = inject(FormBuilder);
  categoryService = inject(CategoryService);
  data = inject(MAT_DIALOG_DATA);
  dialogRef = inject(MatDialogRef<AcceptFormComponent>);

  request = this.data.request;
  categoryId = this.data.categoryId;
  acceptingForm!: FormGroup;
  processing = false;

  ngOnInit() {
    this.acceptingForm = this.fb.group({
      argument: ['', Validators.required],
      agreement: 1,
      id: this.request.id,
      name: ['', [Validators.required, nameValidator]],
      title: '',
      description: '',
    });

    this.initAcceptingForm();
  }

  private initAcceptingForm() {
    if (this.acceptingForm && this.request) {
      this.acceptingForm.patchValue({
        name: this.request.proposedName,
        title: this.request.proposedTitle?.en
          ? this.request.proposedTitle.en
          : '',
        description: this.request.proposedDescription?.en
          ? this.request.proposedDescription.en
          : '',
      });
    }
  }

  public async accept() {
    this.processing = true;
    try {
      const categoryId = this.categoryId;
      if (categoryId && this.request.id) {
        if (
          this.acceptingForm.value.name &&
          this.acceptingForm.value.name !== ''
        ) {
          this.request.proposedName = this.acceptingForm.value.name;
        }

        if (
          this.acceptingForm.value.title &&
          this.acceptingForm.value.title !== ''
        ) {
          this.request.proposedTitle = { en: this.acceptingForm.value.title };
        }

        if (
          this.acceptingForm.value.description &&
          this.acceptingForm.value.description !== ''
        ) {
          this.request.proposedDescription = {
            en: this.acceptingForm.value.description,
          };
        }

        await firstValueFrom(
          this.categoryService.editInterestGroupRequest(
            categoryId,
            this.request.id,
            this.request
          )
        );

        await firstValueFrom(
          this.categoryService.validateInterestGroupRequests(
            categoryId,
            this.request.id,
            this.acceptingForm.value
          )
        );

        const newGroup: InterestGroupPostModel = {
          name: this.acceptingForm.value.name,
          title: { en: this.acceptingForm.value.title },
          description: { en: this.acceptingForm.value.description },
        };
        newGroup.leaders = [];
        newGroup.notify = true;

        if (this.request.leaders) {
          for (const leader of this.request.leaders) {
            if (leader.userId) {
              newGroup.leaders.push(leader.userId);
            }
          }
        }

        await firstValueFrom(
          this.categoryService.postInterestGroup(categoryId, newGroup)
        );
      }
    } catch (error) {
      console.error(error);
    }
    this.processing = false;
    this.dialogRef.close(true);
  }

  get nameControl() {
    return this.acceptingForm.controls.name;
  }

  get titleControl() {
    return this.acceptingForm.controls.title;
  }

  get descriptionControl() {
    return this.acceptingForm.controls.description;
  }
}
