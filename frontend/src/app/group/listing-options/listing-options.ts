/**
 * Describes the pagination and sorting parameters used when requesting a
 * paginated listing of resources (e.g. library nodes, forum topics, members).
 *
 * Instances of this interface are typically passed to API service calls and
 * pager components to control which slice of a collection is fetched and how
 * it is ordered.
 */
export interface ListingOptions {
  /** One-based index of the page to retrieve. */
  page: number;
  /** Maximum number of items to return per page. */
  limit: number;
  /**
   * Sort specification for the listing, expressed as a field name optionally
   * combined with a direction (as understood by the backend API).
   */
  sort: string;
}
