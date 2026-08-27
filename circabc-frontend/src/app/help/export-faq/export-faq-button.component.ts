import {
  Component,
  inject,
  signal,
  ChangeDetectionStrategy,
} from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { TranslocoModule } from '@jsverse/transloco';
import { firstValueFrom } from 'rxjs';
import { HelpService } from 'app/core/generated/circabc';

@Component({
  selector: 'cbc-export-faq-button',
  templateUrl: './export-faq-button.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [TranslocoModule],
})
export class ExportFaqButtonComponent {
  private readonly helpService = inject(HelpService);

  readonly isExporting = signal(false);
  readonly errorMessage = signal<string | null>(null);

  async exportFaq(): Promise<void> {
    this.isExporting.set(true);
    this.errorMessage.set(null);

    try {
      // Call the export API endpoint via HelpService
      const faqData = await firstValueFrom(
        this.helpService.exportFaqStructure()
      );

      // Convert JSON data to Blob
      const jsonString = JSON.stringify(faqData, null, 2);
      const blob = new Blob([jsonString], { type: 'application/json' });

      // Generate filename with timestamp
      const timestamp = this.formatTimestamp(new Date());
      const filename = `faq-export-${timestamp}.json`;

      // Trigger browser download
      this.downloadFile(blob, filename);
    } catch (error) {
      console.error('Export failed:', error);
      this.errorMessage.set(this.getErrorMessage(error));
    } finally {
      this.isExporting.set(false);
    }
  }

  private getErrorMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      switch (error.status) {
        case 401:
          return 'You are not authenticated. Please log in and try again.';
        case 403:
          return 'You do not have permission to export FAQ content.';
        case 500:
          return 'Export failed. Please try again later or contact support.';
        default:
          return 'Unable to export FAQ. Please check your connection and try again.';
      }
    }
    return 'An unexpected error occurred. Please try again.';
  }

  private formatTimestamp(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    const seconds = String(date.getSeconds()).padStart(2, '0');

    return `${year}-${month}-${day}-${hours}${minutes}${seconds}`;
  }

  private downloadFile(blob: Blob, filename: string): void {
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    link.click();

    // Clean up the URL object
    URL.revokeObjectURL(url);
  }
}
