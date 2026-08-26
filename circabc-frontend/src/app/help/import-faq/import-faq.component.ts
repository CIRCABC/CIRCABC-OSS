import {
  Component,
  inject,
  signal,
  output,
  ChangeDetectionStrategy,
} from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { ModalComponent } from 'app/shared/modal/modal.component';
import { HelpService } from 'app/core/generated/circabc';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-import-faq',
  templateUrl: './import-faq.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [TranslocoModule, ModalComponent],
})
export class ImportFaqComponent {
  private readonly helpService = inject(HelpService);
  private readonly translocoService = inject(TranslocoService);

  readonly importSuccess = output<void>();

  readonly showConfirmDialog = signal(false);
  readonly showSuccessDialog = signal(false);
  readonly showErrorDialog = signal(false);
  readonly selectedFile = signal<File | null>(null);
  readonly isImporting = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly validationErrors = signal<string[]>([]);
  readonly successMessage = signal<string | null>(null);

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];

    if (!file) {
      return;
    }

    // Validate file is JSON
    if (!file.name.toLowerCase().endsWith('.json')) {
      this.errorMessage.set('Please select a valid JSON file.');
      this.selectedFile.set(null);
      return;
    }

    this.selectedFile.set(file);
    this.errorMessage.set(null);
    this.successMessage.set(null);
    this.showConfirmation();
  }

  showConfirmation(): void {
    this.showConfirmDialog.set(true);
  }

  cancelImport(): void {
    this.showConfirmDialog.set(false);
    this.selectedFile.set(null);
  }

  closeSuccessDialog(): void {
    this.showSuccessDialog.set(false);
    this.successMessage.set(null);
    this.importSuccess.emit();
  }

  closeErrorDialog(): void {
    this.showErrorDialog.set(false);
    this.errorMessage.set(null);
    this.validationErrors.set([]);
  }

  async confirmImport(): Promise<void> {
    const file = this.selectedFile();
    if (!file) {
      return;
    }

    this.isImporting.set(true);
    this.errorMessage.set(null);
    this.validationErrors.set([]);
    this.successMessage.set(null);
    this.showConfirmDialog.set(false);

    try {
      // Call import API via HelpService
      const result = await firstValueFrom(
        this.helpService.importFaqStructure(file)
      );

      // Display success message with statistics
      this.successMessage.set(
        this.translocoService.translate('help.import.success.message', {
          categoriesProcessed: result.categoriesProcessed,
          subcategoriesProcessed: result.subcategoriesProcessed,
          articlesProcessed: result.articlesProcessed,
        })
      );
      this.showSuccessDialog.set(true);
      this.selectedFile.set(null);
    } catch (error) {
      console.error('Import failed:', error);
      const errorInfo = this.getErrorMessage(error);
      this.errorMessage.set(errorInfo.message);
      this.validationErrors.set(errorInfo.validationErrors);
      this.showErrorDialog.set(true);
    } finally {
      this.isImporting.set(false);
    }
  }

  private getErrorMessage(error: unknown): {
    message: string;
    validationErrors: string[];
  } {
    if (error instanceof HttpErrorResponse) {
      switch (error.status) {
        case 400:
          return {
            message:
              'The selected file is not valid JSON. Please check the file format.',
            validationErrors: [],
          };
        case 401:
          return {
            message: 'You are not authenticated. Please log in and try again.',
            validationErrors: [],
          };
        case 403:
          return {
            message: 'You do not have permission to import FAQ content.',
            validationErrors: [],
          };
        case 422: {
          // Extract validation errors from response
          const validationErrors = error.error?.error?.errors || [];
          const message =
            error.error?.error?.message ||
            'Validation failed. Please check the file content.';
          return {
            message,
            validationErrors: Array.isArray(validationErrors)
              ? validationErrors.map(String)
              : [],
          };
        }
        case 500:
          return {
            message:
              'Import failed. Please try again later or contact support.',
            validationErrors: [],
          };
        default:
          return {
            message:
              'Unable to import FAQ. Please check your connection and try again.',
            validationErrors: [],
          };
      }
    }
    return {
      message: 'An unexpected error occurred. Please try again.',
      validationErrors: [],
    };
  }
}
