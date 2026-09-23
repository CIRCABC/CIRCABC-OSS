package io.swagger.api;

import io.swagger.model.Category;
import io.swagger.model.Header;
import java.util.List;

/**
 * Business service contract for managing CIRCABC {@link Header} entities.
 *
 * <p>In the CIRCABC domain hierarchy (Headers &rarr; Categories &rarr; Interest
 * Groups), a header is the top-level organizational unit that groups a set of
 * categories. This interface defines the operations exposed by the REST layer
 * for creating, reading, updating and deleting headers, as well as navigating
 * from a header to its categories.
 *
 * <p>Implementations (e.g. {@code HeadersApiImpl}) contain the actual business
 * logic and are injected into the corresponding Alfresco webscript endpoint
 * classes.
 */
public interface HeadersApi {
  /**
   * Deletes a single header identified by its id.
   *
   * @param id the identifier of the header to delete
   */
  void deleteHeader(String id);

  /**
   * Lists the categories belonging to a given header.
   *
   * <p>Backs {@code GET /headers/{id}/categories}.
   *
   * @param id the identifier of the header whose categories are requested
   * @param language the language code used to localize the returned categories
   * @param guest {@code true} to resolve the categories as an anonymous/guest
   *     user, {@code false} to use the authenticated user's context
   * @return the list of categories that belong to the specified header
   */
  List<Category> getCategoriesByHeaderId(
    String id,
    String language,
    Boolean guest
  );

  /**
   * Retrieves a single header by its id.
   *
   * <p>Backs {@code GET /headers/{id}}.
   *
   * @param id the identifier of the header to retrieve
   * @return the matching header
   */
  Header getHeader(String id);

  /**
   * Retrieves all headers defined in the application.
   *
   * <p>Backs {@code GET /headers}. This call does not require an authentication
   * token.
   *
   * @param language the language code used to localize the returned headers
   * @param guest {@code true} to resolve the headers as an anonymous/guest
   *     user, {@code false} to use the authenticated user's context
   * @return the list of all headers
   */
  List<Header> getHeaders(String language, Boolean guest);

  /**
   * Creates a new category under the given header.
   *
   * <p>Backs {@code POST /headers/{id}/categories}.
   *
   * @param id the identifier of the header the category will be created under
   * @param body the category to create
   * @return the newly created category
   */
  Category headersIdCategoriesPost(String id, Category body);

  /**
   * Creates a new header in the application.
   *
   * @param body the header to create
   * @return the newly created header
   */
  Header postHeader(Header body);

  /**
   * Updates an existing header in the application.
   *
   * @param id the identifier of the header to update
   * @param body the new header state to apply
   * @return the updated header
   */
  Header putHeader(String id, Header body);

  /**
   * Retrieves the header that owns the given category.
   *
   * @param id the identifier of the category whose parent header is requested
   * @return the header that contains the specified category
   */
  Header getHeaderByCategory(String id);
}
