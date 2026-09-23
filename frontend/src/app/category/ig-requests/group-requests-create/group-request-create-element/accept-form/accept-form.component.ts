import {
  ChangeDetectionStrategy,
  Component,
  inject,
  OnInit,
  signal,
} from '@angular/core';
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
import { RichTextEditorComponent } from 'app/shared/rich-text-editor/rich-text-editor.component';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';

/**
 * Dialog component that renders the form used to accept an interest group
 * creation request.
 *
 * The template presents editable fields for the future interest group (name,
 * title, rich-text description) pre-filled from the pending request, together
 * with a required justification (`argument`) and a spinner shown while the
 * acceptance is being processed.
 *
 * On submission it edits and validates the underlying interest group request
 * and creates the actual interest group through the {@link CategoryService},
 * then closes the surrounding Material dialog reporting success.
 *
 * The component is opened as a Material dialog and receives its inputs through
 * the injected `MAT_DIALOG_DATA` payload (see {@link request} and
 * {@link categoryId}).
 */
@Component({
  selector: 'cbc-accept-form',
  templateUrl: './accept-form.component.html',
  styleUrl: './accept-form.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ReactiveFormsModule,
    RichTextEditorComponent,
    ControlMessageComponent,
    SpinnerComponent,
    TranslocoModule,
  ],
})
export class AcceptFormComponent implements OnInit {
  /** Reactive forms builder used to construct {@link acceptingForm}. */
  fb = inject(FormBuilder);
  /** Generated API client used to edit, validate the request and create the group. */
  categoryService = inject(CategoryService);
  /** Material dialog payload providing the pending request and category id. */
  data = inject(MAT_DIALOG_DATA);
  /** Reference to the hosting Material dialog, used to close it with a result. */
  dialogRef = inject(MatDialogRef<AcceptFormComponent>);

  /** The pending interest group request being accepted, taken from the dialog data. */
  request = this.data.request;
  /** Identifier of the category the request (and future group) belongs to. */
  categoryId = this.data.categoryId;
  /** Reactive form holding the editable acceptance fields; initialized in {@link ngOnInit}. */
  acceptingForm!: FormGroup;
  /** Whether an acceptance operation is currently in progress (drives the spinner/disabled state). */
  processing = signal(false);

  /**
   * Angular lifecycle hook. Builds {@link acceptingForm} with its controls and
   * validators, then pre-populates it from the incoming request via
   * `initAcceptingForm`.
   */
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

  /**
   * Pre-fills {@link acceptingForm} from the current {@link request},
   * mapping the proposed name and the English variants of the proposed title
   * and description onto the corresponding form controls. No-op when the form
   * or request is not available.
   */
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

  /**
   * Accepts the request: sets the processing flag, and when a category id and
   * request id are present, applies form edits to the request, saves and
   * validates it, then creates the interest group. Any error is logged to the
   * console. Regardless of outcome, clears the processing flag and closes the
   * dialog with `true`.
   *
   * @returns A promise that resolves once processing has completed and the
   * dialog has been closed.
   */
  public async accept() {
    this.processing.set(true);
    try {
      const categoryId = this.categoryId;
      if (categoryId && this.request.id) {
        this.updateRequestFromForm();
        await this.saveAndValidateRequest(categoryId);
        await this.createInterestGroup(categoryId);
      }
    } catch (error) {
      console.error(error);
    }
    this.processing.set(false);
    this.dialogRef.close(true);
  }

  /**
   * Copies the non-empty name, title and description values from
   * {@link acceptingForm} onto the {@link request}, wrapping the title and
   * description as English (`en`) localized values.
   */
  private updateRequestFromForm() {
    const { name, title, description } = this.acceptingForm.value;

    if (name && name !== '') {
      this.request.proposedName = name;
    }
    if (title && title !== '') {
      this.request.proposedTitle = { en: title };
    }
    if (description && description !== '') {
      this.request.proposedDescription = { en: description };
    }
  }

  /**
   * Persists the current {@link request} and then validates it against the
   * form values via the {@link CategoryService}. Returns early when the request
   * has no id.
   *
   * @param categoryId Identifier of the category owning the request.
   * @returns A promise that resolves once both the edit and validation calls
   * have completed.
   */
  private async saveAndValidateRequest(categoryId: string) {
    if (!this.request.id) return;

    await this.categoryService.editInterestGroupRequestAsync({
      id: categoryId,
      idRequest: this.request.id,
      groupCreationRequest: this.request,
    });

    await this.categoryService.validateInterestGroupRequestsAsync({
      id: categoryId,
      idRequest: this.request.id,
      groupCreationRequestApproval: this.acceptingForm.value,
    });
  }

  /**
   * Builds an {@link InterestGroupPostModel} from the form values (with English
   * title/description), the request's leaders and notification enabled, then
   * creates the interest group through the {@link CategoryService}.
   *
   * @param categoryId Identifier of the category in which to create the group.
   * @returns A promise that resolves once the group has been created.
   */
  private async createInterestGroup(categoryId: string) {
    const { name, title, description } = this.acceptingForm.value;
    const newGroup: InterestGroupPostModel = {
      name,
      title: { en: title },
      description: { en: description },
      leaders: this.getLeaderIds(),
      notify: true,
    };

    await this.categoryService.postInterestGroupAsync({
      id: categoryId,
      interestGroupPostModel: newGroup,
    });
  }

  /**
   * Extracts the user ids of the request's leaders, skipping any leader without
   * a `userId`.
   *
   * @returns An array of leader user ids, or an empty array when the request
   * has no leaders.
   */
  private getLeaderIds(): string[] {
    if (!this.request.leaders) return [];
    return this.request.leaders
      .filter((leader: { userId?: string }) => leader.userId)
      .map((leader: { userId?: string }) => leader.userId as string);
  }

  /** Convenience accessor for the `name` form control (used for validation messages). */
  get nameControl() {
    return this.acceptingForm.controls.name;
  }

  /** Convenience accessor for the `title` form control. */
  get titleControl() {
    return this.acceptingForm.controls.title;
  }

  /** Convenience accessor for the `description` form control. */
  get descriptionControl() {
    return this.acceptingForm.controls.description;
  }
}
