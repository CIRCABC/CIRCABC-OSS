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
  CategoryService,
  HeaderService,
  InterestGroupService,
} from 'app/core/generated/circabc';
import { saveAs } from 'file-saver';
import { EMPTY } from 'rxjs';

/**
 * Support screen that lets an administrator export a single interest group as
 * an XML document.
 *
 * The component renders a set of cascading Material select dropdowns
 * (header → category → interest group). Selecting a header loads its
 * categories, selecting a category loads its interest groups, and once an
 * interest group is chosen the user can trigger the export. A progress bar is
 * shown while the export request is in flight, and any error message is
 * surfaced to the template.
 *
 * Data is fetched reactively via `rxResource` from the generated CIRCABC API
 * services ({@link HeaderService}, {@link CategoryService} and
 * {@link InterestGroupService}). The resulting XML is downloaded on the client
 * using `file-saver`.
 */
@Component({
  selector: 'cbc-ig-export',
  templateUrl: './ig-export.component.html',
  styleUrl: './ig-export.component.scss',
  imports: [
    TranslocoModule,
    MatFormFieldModule,
    MatSelectModule,
    MatProgressBarModule,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class IgExportComponent {
  /** Generated API client used to perform the interest group XML export. */
  private readonly igService = inject(InterestGroupService);
  /** Generated API client used to load headers and their categories. */
  private readonly headerService = inject(HeaderService);
  /** Generated API client used to load interest groups for a category. */
  private readonly categoryService = inject(CategoryService);

  /** Id of the header currently selected in the first dropdown. */
  readonly selectedHeaderId = signal('');
  /** Id of the category currently selected in the second dropdown. */
  readonly selectedCategoryId = signal('');
  /** Id of the interest group currently selected in the third dropdown. */
  readonly selectedIgId = signal('');
  /** Whether an export request is currently in progress. */
  readonly loading = signal(false);
  /** Latest export error message, or an empty string when there is no error. */
  readonly error = signal('');

  /** Reactive resource providing the full list of available headers. */
  readonly headersResource = rxResource({
    stream: () => this.headerService.getHeaders(),
  });

  /**
   * Reactive resource providing the categories of the currently selected
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
   * Reactive resource providing the interest groups of the currently selected
   * category. Re-fetches whenever {@link selectedCategoryId} changes.
   */
  readonly igsResource = rxResource({
    params: () => this.selectedCategoryId(),
    stream: ({ params: categoryId }: { params: string }) =>
      categoryId
        ? this.categoryService.getInterestGroupsByCategoryId({
            id: categoryId,
          })
        : EMPTY,
  });

  /**
   * Computed flag indicating whether the export action is currently allowed,
   * i.e. an interest group is selected and no export is already running.
   */
  readonly canExport = computed(() => this.selectedIgId() && !this.loading());

  /**
   * Handles selection of a header. Records the new header id and resets the
   * dependent category and interest group selections.
   *
   * @param headerId - Id of the header selected by the user.
   */
  onHeaderChange(headerId: string) {
    this.selectedHeaderId.set(headerId);
    this.selectedCategoryId.set('');
    this.selectedIgId.set('');
  }

  /**
   * Handles selection of a category. Records the new category id and resets
   * the dependent interest group selection.
   *
   * @param categoryId - Id of the category selected by the user.
   */
  onCategoryChange(categoryId: string) {
    this.selectedCategoryId.set(categoryId);
    this.selectedIgId.set('');
  }

  /**
   * Handles selection of an interest group.
   *
   * @param igId - Id of the interest group selected by the user.
   */
  onIgChange(igId: string) {
    this.selectedIgId.set(igId);
  }

  /**
   * Exports the currently selected interest group as an XML file.
   *
   * Does nothing when {@link canExport} is `false`. While the request is in
   * flight {@link loading} is set to `true`. On success the returned XML is
   * saved locally as `ig-export-<igId>.xml`; on failure the error message is
   * stored in {@link error}. Errors from the underlying request are caught and
   * reported via {@link error} rather than propagated.
   *
   * @returns A promise that resolves once the export attempt has completed.
   */
  async export() {
    if (!this.canExport()) return;
    this.loading.set(true);
    this.error.set('');
    try {
      const xml = await this.igService.igExportAsync({
        id: this.selectedIgId(),
      });
      saveAs(
        new Blob([xml], { type: 'application/xml' }),
        `ig-export-${this.selectedIgId()}.xml`
      );
    } catch (e: unknown) {
      this.error.set(e instanceof Error ? e.message : 'Export failed');
    }
    this.loading.set(false);
  }
}
