import {
  ChangeDetectionStrategy,
  Component,
  input,
  model,
} from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { TranslocoModule } from '@jsverse/transloco';

/**
 * Standalone pagination component (`cbc-pager`).
 *
 * Renders pagination controls that let the user move between pages of a
 * collection: previous/next navigation and a list of selectable page numbers.
 * The component computes the total number of pages from the provided item
 * count (`length`) and page size (`pageSize`), and exposes the currently
 * selected page through the two-way bindable `page` model so the host can
 * react to page changes.
 *
 * @example
 * ```html
 * <cbc-pager [(page)]="currentPage" [length]="totalItems" [pageSize]="20" />
 * ```
 */
@Component({
  selector: 'cbc-pager',
  templateUrl: './pager.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule, TranslocoModule],
})
export class PagerComponent {
  /**
   * Two-way bindable model holding the currently selected page number
   * (1-based). Defaults to the first page.
   */
  page = model<number>(1);
  /** Total number of items across all pages. */
  readonly length = input(0);
  /** Number of items displayed per page, used to compute the page count. */
  readonly pageSize = input(0);
  /**
   * Moves the selection to the previous page, if one exists.
   * No-op when already on the first page.
   */
  public previousPage() {
    if (this.canPreviousPage()) {
      this.page.update((value) => value - 1);
    }
  }
  /**
   * Indicates whether a previous page is available.
   *
   * @returns `true` if the current page is greater than the first page,
   * otherwise `false`.
   */
  public canPreviousPage() {
    return this.page() > 1;
  }

  /**
   * Moves the selection to the next page, if one exists.
   * No-op when already on the last page.
   */
  public nextPage() {
    if (this.canNextPage()) {
      this.page.update((value) => value + 1);
    }
  }
  /**
   * Indicates whether a next page is available.
   *
   * @returns `true` if the current page is before the last computed page,
   * otherwise `false`.
   */
  public canNextPage() {
    return this.page() < Math.ceil(this.length() / this.pageSize());
  }

  /**
   * Sets the current page to the given value.
   *
   * @param page - The target page number as a string (e.g. from a
   * template control); it is coerced to a number before being applied.
   */
  public goToPage(page: string) {
    this.page.set(Number(page));
  }

  /**
   * Builds the list of available page numbers.
   *
   * @returns An array of 1-based page numbers derived from `length` and
   * `pageSize`, suitable for rendering the page selector.
   */
  public getPages(): number[] {
    const result: number[] = [];

    for (let i = 1; i < this.length() / this.pageSize() + 1; i += 1) {
      result.push(i);
    }

    return result;
  }
}
