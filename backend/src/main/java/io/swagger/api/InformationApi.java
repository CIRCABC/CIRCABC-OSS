package io.swagger.api;

import io.swagger.model.InformationPage;
import io.swagger.model.News;
import io.swagger.model.PagedNews;

/**
 * Business operations backing the Information service of an Interest Group (IG).
 *
 * <p>The Information service exposes an editable information page for an IG together with a
 * collection of news items. Implementations of this interface contain the actual logic that the
 * REST webscript endpoints delegate to, translating between the Alfresco repository and the
 * {@link io.swagger.model} domain objects.
 *
 * @author beaurpi
 */
public interface InformationApi {
  /**
   * Retrieves the information page of an Interest Group.
   *
   * @param id the identifier of the Interest Group whose information page is requested
   * @return the {@link InformationPage} for the given group
   */
  InformationPage groupsIdInformationGet(String id);

  /**
   * Retrieves a paginated list of news items belonging to an Interest Group's information service.
   *
   * @param id the identifier of the Interest Group
   * @param limit the maximum number of news items to return per page
   * @param page the zero- or one-based page index to return (as defined by the implementation)
   * @return a {@link PagedNews} holding the requested page of news items along with paging metadata
   */
  PagedNews groupsIdInformationNewsGet(String id, Integer limit, Integer page);

  /**
   * Creates a new news item within an Interest Group's information service.
   *
   * @param id the identifier of the Interest Group the news item is added to
   * @param news the news item to create
   * @return the newly created {@link News} item, including any server-assigned properties
   */
  News groupsIdInformationNewsPost(String id, News news);

  /**
   * Deletes a news item.
   *
   * @param id the identifier of the news item to delete
   */
  void newsIdDelete(String id);

  /**
   * Retrieves a single news item by its identifier.
   *
   * @param id the identifier of the news item to retrieve
   * @return the matching {@link News} item
   */
  News newsIdGet(String id);

  /**
   * Updates an existing news item.
   *
   * @param id the identifier of the news item to update
   * @param news the new state to apply to the news item
   * @return the updated {@link News} item
   */
  News newsIdPut(String id, News news);

  /**
   * Updates the information page of an Interest Group.
   *
   * @param id the identifier of the Interest Group whose information page is updated
   * @param body the new content to apply to the information page
   */
  void groupsIdInformationPut(String id, InformationPage body);
}
