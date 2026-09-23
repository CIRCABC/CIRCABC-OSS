package io.swagger.api;

import io.swagger.model.PagedNodes;

/**
 * Business operations for retrieving expired documents within an Interest Group.
 *
 * <p>Implementations of this interface encapsulate the logic backing the REST
 * endpoint that lists documents whose expiration date has passed for a given
 * Interest Group. The results are returned in a paged form so that callers can
 * navigate large collections without loading every node at once.
 *
 * @author beaurpi
 */
public interface ExpiredApi {
  /**
   * Retrieves the expired documents belonging to the specified Interest Group.
   *
   * @param id the identifier of the Interest Group whose expired documents are
   *     requested
   * @param limit the maximum number of documents to include in a single page
   * @param page the zero- or one-based index of the page to return, depending on
   *     the implementation's pagination convention
   * @param order the sort order to apply to the returned documents (e.g. the
   *     property to sort on and/or the direction)
   * @return a {@link PagedNodes} instance containing the requested page of expired
   *     documents together with pagination metadata
   */
  PagedNodes groupsIdDocumentsExpiredGet(
    String id,
    Integer limit,
    Integer page,
    String order
  );
}
