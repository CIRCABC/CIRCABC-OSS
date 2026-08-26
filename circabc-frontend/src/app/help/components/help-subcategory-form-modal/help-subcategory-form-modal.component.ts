import {
  Component,
  OnInit,
  OnChanges,
  SimpleChanges,
  output,
  input,
  model,
  signal,
  computed,
  inject,
  ChangeDetectionStrategy,
} from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { TranslocoModule } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { HelpSubcategory, HelpService } from 'app/core/generated/circabc';
import { nonEmptyTitle } from 'app/core/validation.service';
import { MultilingualInputComponent } from 'app/shared/input/multilingual-input.component';
import { ModalComponent } from 'app/shared/modal/modal.component';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-help-subcategory-form-modal',
  templateUrl: './help-subcategory-form-modal.component.html',
  styleUrls: ['./help-subcategory-form-modal.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ModalComponent,
    ReactiveFormsModule,
    MultilingualInputComponent,
    TranslocoModule,
  ],
})
export class HelpSubcategoryFormModalComponent implements OnInit, OnChanges {
  private readonly fb = inject(FormBuilder);
  private readonly helpService = inject(HelpService);

  // Inputs and outputs using signals API
  readonly visible = model.required<boolean>();
  readonly categoryId = input.required<string>(); // Parent category ID (required)
  readonly subcategoryId = input<string | undefined>(); // Subcategory ID for edit mode

  readonly saved = output<ActionEmitterResult>();
  readonly cancelled = output<void>();

  // Internal state using signals
  readonly isLoading = signal<boolean>(false);
  readonly isSaving = signal<boolean>(false);
  readonly errorMessage = signal<string | null>(null);
  readonly editMode = computed(() => this.subcategoryId() !== undefined);

  // Form
  protected subcategoryForm!: FormGroup;
  protected titleModel: { [key: string]: string } = {};

  ngOnInit(): void {
    this.initializeForm();
  }

  async ngOnChanges(changes: SimpleChanges): Promise<void> {
    // Load subcategory data when subcategoryId changes and has a value (edit mode)
    if (changes['subcategoryId']) {
      const subcategoryId = this.subcategoryId();
      if (subcategoryId) {
        await this.loadSubcategory();
      } else {
        // Reset form when switching to create mode
        this.resetForm();
      }
    }
  }

  private initializeForm(): void {
    this.subcategoryForm = this.fb.group(
      {
        title: ['', nonEmptyTitle],
      },
      {
        updateOn: 'change',
      }
    );
  }

  private async loadSubcategory(): Promise<void> {
    const id = this.subcategoryId();
    if (!id) return;

    this.isLoading.set(true);
    this.errorMessage.set(null);

    try {
      const subcategory = await firstValueFrom(
        this.helpService.getHelpSubcategory(id)
      );
      this.titleModel = subcategory.title || {};
      this.subcategoryForm.patchValue({ title: subcategory.title });
    } catch (error) {
      console.error('Failed to load subcategory', error);

      // Determine error type and set appropriate message
      if (error instanceof HttpErrorResponse) {
        switch (error.status) {
          case 403:
            this.errorMessage.set('help.errors.permission');
            break;
          case 404:
            this.errorMessage.set('help.errors.not_found');
            break;
          case 500:
            this.errorMessage.set('help.errors.server');
            break;
          default:
            this.errorMessage.set('help.errors.unknown');
        }
      } else {
        this.errorMessage.set('help.errors.network');
      }
    } finally {
      this.isLoading.set(false);
    }
  }

  protected async onSave(): Promise<void> {
    if (this.subcategoryForm.invalid) return;

    this.isSaving.set(true);
    this.errorMessage.set(null);

    const result: ActionEmitterResult = {
      type: this.editMode()
        ? ActionType.UPDATE_HELP_SUBCATEGORY
        : ActionType.ADD_HELP_SUBCATEGORY,
      result: ActionResult.FAILED,
    };

    try {
      const body: HelpSubcategory = {
        title: this.subcategoryForm.value.title,
      };

      if (this.editMode()) {
        const id = this.subcategoryId();
        if (id) {
          await firstValueFrom(this.helpService.putHelpSubcategory(id, body));
        }
      } else {
        // Create mode: use parent categoryId
        const categoryId = this.categoryId();
        await firstValueFrom(
          this.helpService.createCategorySubcategory(categoryId, body)
        );
      }

      result.result = ActionResult.SUCCEED;
      this.visible.set(false);
      this.resetForm();
      this.saved.emit(result);
    } catch (error) {
      console.error('Failed to save subcategory', error);

      // Determine error type and set appropriate message
      if (error instanceof HttpErrorResponse) {
        switch (error.status) {
          case 400:
            this.errorMessage.set('help.errors.validation');
            break;
          case 403:
            this.errorMessage.set('help.errors.permission');
            break;
          case 404:
            this.errorMessage.set('help.errors.not_found');
            break;
          case 500:
            this.errorMessage.set('help.errors.server');
            break;
          default:
            this.errorMessage.set('help.errors.unknown');
        }
      } else {
        this.errorMessage.set('help.errors.network');
      }

      // Modal stays open so user can retry
    } finally {
      this.isSaving.set(false);
    }
  }

  protected onCancel(): void {
    this.visible.set(false);
    this.resetForm();
    this.cancelled.emit();
  }

  private resetForm(): void {
    this.subcategoryForm.reset();
    this.titleModel = {};
    this.errorMessage.set(null);
  }

  protected get isFormValid(): boolean {
    return this.subcategoryForm.valid;
  }
}
