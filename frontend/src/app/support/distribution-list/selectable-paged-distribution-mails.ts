import { DistributionMail } from 'app/core/generated/circabc';

/**
 * View model representing a single page of distribution-list mail entries
 * that can be individually selected in the UI.
 *
 * It wraps a slice of {@link SelectableDistributionMail} items together with
 * the overall count of matching records, enabling paged rendering (e.g. in a
 * table with a pager) while retaining the total number of results for
 * pagination controls.
 */
export interface SelectablePagedDistributionMails {
  /** The distribution mails belonging to the current page, each carrying its own selection state. */
  data: SelectableDistributionMail[];
  /** Total number of distribution mails available across all pages, used to drive pagination. */
  total: number;
}

/**
 * A {@link DistributionMail} augmented with a UI selection flag.
 *
 * Extends the generated `DistributionMail` model with a `selected` property so
 * that individual mail entries can be checked/unchecked in the distribution-list
 * interface (for example to perform bulk actions on the chosen recipients).
 */
export interface SelectableDistributionMail extends DistributionMail {
  /** Whether this distribution mail is currently selected in the UI. */
  selected: boolean;
}
