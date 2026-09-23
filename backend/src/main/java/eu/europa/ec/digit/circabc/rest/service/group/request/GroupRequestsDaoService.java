/**
 *
 */
package eu.europa.ec.digit.circabc.rest.service.group.request;

import io.swagger.model.GroupCreationRequest;
import io.swagger.model.GroupDeletionRequest;
import java.util.List;

/**
 * Data-access abstraction for group creation and group deletion requests.
 *
 * <p>Implementations of this interface are responsible for persisting, retrieving, counting and
 * updating the workflow requests raised by users who want a new group to be created under a
 * category or who want an existing group to be deleted. It isolates the persistence concerns (for
 * example database access) from the surrounding REST and service layers.
 *
 * @author beaurpi
 */
public interface GroupRequestsDaoService {
  /**
   * Counts the group creation requests attached to the given category.
   *
   * @param categRef the reference of the category whose creation requests are counted
   * @param filter an optional filter expression used to restrict the counted requests; may be
   *     {@code null} or empty to count all requests
   * @return the number of matching group creation requests
   */
  public Integer getCountCategoryGroupCreationRequests(
    String categRef,
    String filter
  );

  /**
   * Retrieves a paginated list of group creation requests for the given category.
   *
   * @param categRef the reference of the category whose creation requests are retrieved
   * @param limit the maximum number of requests to return in a page
   * @param page the zero- or one-based page index to retrieve (as defined by the implementation)
   * @param filter an optional filter expression used to restrict the returned requests; may be
   *     {@code null} or empty to return all requests
   * @return the matching group creation requests for the requested page, never {@code null}
   */
  public List<GroupCreationRequest> getCategoryGroupCreationRequests(
    String categRef,
    int limit,
    int page,
    String filter
  );

  /**
   * Persists a new group creation request.
   *
   * @param body the group creation request to save
   */
  public void saveRequest(GroupCreationRequest body);

  /**
   * Records a reviewer's approval decision on a group creation request.
   *
   * @param username the login name of the user who reviewed the request
   * @param id the identifier of the group creation request being reviewed
   * @param agreement the approval decision (for example approved or rejected) as an integer code
   * @param argument the free-text justification provided by the reviewer for the decision
   */
  public void updateGroupCreationRequestApproval(
    String username,
    long id,
    int agreement,
    String argument
  );

  /**
   * Retrieves a single group creation request by its identifier.
   *
   * @param requestId the identifier of the group creation request to retrieve
   * @return the matching group creation request, or {@code null} if none exists
   */
  public GroupCreationRequest getCategoryGroupCreationRequests(
    String requestId
  );

  /**
   * Updates an existing group creation request identified by its request id.
   *
   * @param requestId the identifier of the group creation request to update
   * @param body the new state of the group creation request
   */
  public void putCategoryGroupCreationRequest(
    String requestId,
    GroupCreationRequest body
  );

  /**
   * Persists a new group deletion request.
   *
   * @param body the group deletion request to save
   * @return the generated identifier of the saved deletion request
   */
  public int saveRequestDeletion(GroupDeletionRequest body);

  /**
   * Retrieves a paginated list of group deletion requests for the given category.
   *
   * @param categoryRef the reference of the category whose deletion requests are retrieved
   * @param limit the maximum number of requests to return in a page
   * @param page the zero- or one-based page index to retrieve (as defined by the implementation)
   * @param filter an optional filter expression used to restrict the returned requests; may be
   *     {@code null} or empty to return all requests
   * @return the matching group deletion requests for the requested page, never {@code null}
   */
  public List<GroupDeletionRequest> getCategoryGroupDeletionRequests(
    String categoryRef,
    Integer limit,
    Integer page,
    String filter
  );

  /**
   * Retrieves a single group deletion request by its identifier.
   *
   * @param requestId the identifier of the group deletion request to retrieve
   * @return the matching group deletion request, or {@code null} if none exists
   */
  public GroupDeletionRequest getCategoryGroupDeletionRequests(
    String requestId
  );

  /**
   * Counts the group deletion requests attached to the given category.
   *
   * @param categoryRef the reference of the category whose deletion requests are counted
   * @param filter an optional filter expression used to restrict the counted requests; may be
   *     {@code null} or empty to count all requests
   * @return the number of matching group deletion requests
   */
  public Long getCountCategoryGroupDeletionRequests(
    String categoryRef,
    String filter
  );

  /**
   * Updates an existing group deletion request, recording the reviewer that handled it.
   *
   * @param body the new state of the group deletion request
   * @param reviewer the login name of the user who reviewed the deletion request
   */
  public void updateRequestDeletion(GroupDeletionRequest body, String reviewer);

  /**
   * Deletes the deletion request associated with the given group.
   *
   * @param groupId the identifier of the group whose deletion request is removed
   */
  public void deleteRequestDeletion(String groupId);

  /**
   * Indicates whether a deletion request already exists for the given group.
   *
   * @param groupId the identifier of the group to check
   * @return {@code true} if a deletion request exists for the group, {@code false} otherwise
   */
  public boolean existsGroupDeleteRequest(String groupId);
}
