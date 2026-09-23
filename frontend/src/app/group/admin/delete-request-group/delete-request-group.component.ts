import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  resource,
  signal,
} from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { TranslocoModule } from '@jsverse/transloco';
import {
  CategoryService,
  GroupDeletionRequestInput,
  InterestGroup,
} from 'app/core/generated/circabc';
import { User } from 'app/core/generated/circabc/model/user';
import { LoginService } from 'app/core/login.service';
import { ControlMessageComponent } from '../../../shared/control-message/control-message.component';
import { MultilingualInputComponent } from '../../../shared/input/multilingual-input.component';
import { SpinnerComponent } from '../../../shared/spinner/spinner.component';

/**
 * Data passed into the {@link DeleteRequestGroupComponent} dialog via the
 * Angular Material `MAT_DIALOG_DATA` injection token.
 */
export interface DialogData {
  /** The interest group that the deletion request targets. */
  group: InterestGroup;
}

/**
 * Dialog component that lets a user submit a request to delete an interest
 * group.
 *
 * Rendered inside an Angular Material dialog, it shows a reactive form with the
 * group's (read-only) name and title plus a multilingual justification field.
 * A `resource()` checks whether a deletion request is already pending for the
 * group and reflects that state in the UI. Submitting the form posts a
 * {@link GroupDeletionRequestInput} to the backend through the
 * {@link CategoryService} and closes the dialog.
 *
 * Key collaborators: {@link FormBuilder} (form construction),
 * {@link LoginService} (current user), {@link CategoryService} (pending-request
 * lookup and deletion-request submission) and {@link MatDialogRef} (dialog
 * lifecycle).
 */
@Component({
  selector: 'cbc-group-delete-request-post',
  templateUrl: './delete-request-group.component.html',
  styleUrl: './delete-request-group.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    TranslocoModule,
    MultilingualInputComponent,
    ReactiveFormsModule,
    SpinnerComponent,
    ControlMessageComponent,
    MatCardModule,
  ],
})
export class DeleteRequestGroupComponent {
  private readonly fb = inject(FormBuilder);
  private readonly loginService = inject(LoginService);
  private readonly categoryService = inject(CategoryService);
  /** Reference to the enclosing Material dialog, used to close it on submit. */
  dialogRef = inject<MatDialogRef<DeleteRequestGroupComponent>>(MatDialogRef);
  /** Data injected into the dialog, containing the target interest group. */
  data = inject<DialogData>(MAT_DIALOG_DATA);

  /** Reactive form holding the group name, title and justification controls. */
  public form: FormGroup = this.fb.group({
    name: [{ value: '', disabled: true }, Validators.required],
    title: [],
    justification: [],
  });
  /** Payload built from the form and sent to the backend on submission. */
  public groupDeletionRequestInput!: GroupDeletionRequestInput;
  /** The currently authenticated user, resolved during construction. */
  private readonly user: User = this.loginService.getUser();
  /** Whether a plain submit operation is in progress. */
  private readonly submitting = signal(false);

  /**
   * Queries the backend to determine whether a deletion request is already
   * pending for the target group. Stays idle (loader not called) when the
   * group has no id.
   */
  private readonly isPendingResource = resource({
    params: () => this.data.group.id || undefined,
    loader: ({ params: groupId }) =>
      this.categoryService.isDeleteRequestPendingAsync({ groupId }),
  });

  /** Whether an asynchronous operation (lookup or submit) is in progress. */
  public readonly processing = computed(
    () => this.isPendingResource.isLoading() || this.submitting()
  );
  /** Whether a deletion request is already pending for the target group. */
  public readonly isPending = computed(() =>
    this.isPendingResource.hasValue() ? this.isPendingResource.value() : true
  );
  /** Unused convenience copy of the injected dialog data. */
  public dialogData!: DialogData;

  constructor() {
    this.form.controls.name.patchValue(this.data.group.name);
    this.form.controls.title.patchValue(this.data.group.title);
  }

  /**
   * Submits the group deletion request.
   *
   * Ensures the justification control has a value (defaulting to an empty
   * English string when none is provided), builds the
   * {@link GroupDeletionRequestInput} from the English justification and posts
   * it to the backend for the target group. Closes the dialog once complete.
   *
   * @returns A promise that resolves after the request has been sent and the
   * dialog has been closed.
   */
  public async requestDeleteGroup() {
    this.submitting.set(true);
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
        await this.categoryService.postGroupDeletionRequestAsync({
          id: this.data.group.id,
          groupDeletionRequestInput: this.groupDeletionRequestInput,
        });
      }
    }
    this.submitting.set(false);
    this.dialogRef.close();
  }

  /**
   * Accessor for the multilingual justification form control.
   *
   * @returns The `justification` {@link AbstractControl} from the form.
   */
  get justificationControl(): AbstractControl {
    return this.form.controls.justification;
  }
}
