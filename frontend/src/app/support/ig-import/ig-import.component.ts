import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  signal,
} from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { TranslocoModule } from '@jsverse/transloco';
import {
  HeaderService,
  IgImportResponse,
  InterestGroupService,
} from 'app/core/generated/circabc';
import { EMPTY } from 'rxjs';

/**
 * Support tool component (`cbc-ig-import`) that lets an administrator import an
 * interest group from an uploaded file into a chosen category.
 *
 * The component renders a small form with two cascading dropdowns and a file
 * input:
 * - a header selector, populated from {@link HeaderService.getHeaders};
 * - a category selector, populated from
 *   {@link HeaderService.getCategoriesByHeaderId} based on the selected header;
 * - a file picker for the interest group archive to import.
 *
 * Once a category and a file are chosen it triggers the import via
 * {@link InterestGroupService.igImport}, showing a progress bar while the
 * request is in flight and surfacing either the resulting
 * {@link IgImportResponse} or an error message.
 *
 * State is managed with Angular signals and the data-loading dropdowns rely on
 * `rxResource` to stream their options reactively.
 *
 * Key collaborators: {@link InterestGroupService} (performs the import) and
 * {@link HeaderService} (provides headers and their categories).
 */
@Component({
  selector: 'cbc-ig-import',
  templateUrl: './ig-import.component.html',
  styleUrl: './ig-import.component.scss',
  imports: [
    TranslocoModule,
    MatFormFieldModule,
    MatSelectModule,
    MatProgressBarModule,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class IgImportComponent {
  /** Client used to perform the interest group import request. */
  private readonly igService = inject(InterestGroupService);
  /** Client used to load headers and their associated categories. */
  private readonly headerService = inject(HeaderService);

  /** Identifier of the header currently selected in the header dropdown. */
  readonly selectedHeaderId = signal('');
  /** Identifier of the category currently selected as the import target. */
  readonly selectedCategoryId = signal('');
  /** The file chosen for import, or `null` when none is selected. */
  readonly file = signal<File | null>(null);
  /** Whether an import request is currently in progress. */
  readonly loading = signal(false);
  /** Error message from the last failed import, or empty string when none. */
  readonly error = signal('');
  /** Response from the last successful import, or `null` when none. */
  readonly result = signal<IgImportResponse | null>(null);

  /** Reactive resource streaming the list of available headers. */
  readonly headersResource = rxResource({
    stream: () => this.headerService.getHeaders(),
  });

  /**
   * Reactive resource streaming the categories of the currently selected
   * header. Re-fetches whenever {@link selectedHeaderId} changes.
   */
  readonly categoriesResource = rxResource({
    params: () => this.selectedHeaderId(),
    stream: ({ params: headerId }: { params: string }) =>
      headerId
        ? this.headerService.getCategoriesByHeaderId({ id: headerId })
        : EMPTY,
  });

  /**
   * Whether the import action can be triggered: a category and a file must be
   * selected and no import may already be running.
   */
  readonly canImport = computed(
    () => this.selectedCategoryId() && this.file() && !this.loading()
  );

  /**
   * Handles selection of a new header, updating {@link selectedHeaderId} and
   * clearing any previously selected category.
   *
   * @param headerId - Identifier of the newly selected header.
   */
  onHeaderChange(headerId: string) {
    this.selectedHeaderId.set(headerId);
    this.selectedCategoryId.set('');
  }

  /**
   * Handles selection of a new target category.
   *
   * @param categoryId - Identifier of the newly selected category.
   */
  onCategoryChange(categoryId: string) {
    this.selectedCategoryId.set(categoryId);
  }

  /**
   * Handles a change on the file input, storing the first selected file (or
   * `null` if the selection was cleared) in {@link file}.
   *
   * @param event - The DOM `change` event emitted by the file input element.
   */
  onFileChange(event: Event) {
    const input = event.target as HTMLInputElement;
    this.file.set(input.files?.[0] ?? null);
  }

  /**
   * Performs the interest group import for the selected category and file.
   *
   * Does nothing when {@link canImport} is false or no file is set. While
   * running it toggles {@link loading}, resets previous {@link error} and
   * {@link result} state, and finally stores either the successful
   * {@link IgImportResponse} in {@link result} or an error message in
   * {@link error}. Errors are captured internally and never rethrown.
   *
   * @returns A promise that resolves once the import attempt has completed.
   */
  async import() {
    const file = this.file();
    if (!(this.canImport() && file)) return;
    this.loading.set(true);
    this.error.set('');
    this.result.set(null);
    try {
      this.result.set(
        await this.igService.igImportAsync({
          categoryId: this.selectedCategoryId(),
          file,
        })
      );
    } catch (e: unknown) {
      this.error.set(e instanceof Error ? e.message : 'Import failed');
    }
    this.loading.set(false);
  }
}
