package io.swagger.api;

import io.swagger.model.*;
import java.io.InputStream;
import java.util.List;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Business operations for CIRCABC Categories.
 *
 * <p>A Category groups together {@link InterestGroup Interest Groups} and lives under a Header in
 * the CIRCABC hierarchy (Headers &rarr; Categories &rarr; Interest Groups). This interface defines
 * the service contract backing the {@code /categories} REST endpoints: listing categories,
 * reading and updating a single category, managing category administrators and logos, creating
 * Interest Groups, and handling Interest Group creation/deletion requests and their approvals.
 *
 * <p>Implementations perform the actual work against the Alfresco repository; the REST webscript
 * classes delegate to this interface.
 *
 * @author beaurpi
 */
public interface CategoriesApi {
  /**
   * Gets all the Categories belonging to all Headers in the application ({@code GET /categories}).
   *
   * @return the list of every Category available in CIRCABC
   */
  List<Category> getCategories();

  /**
   * Lists the node references of the Interest Groups contained in the given category.
   *
   * @param category the {@link NodeRef} of the category
   * @return the list of {@link NodeRef}s pointing to the category's Interest Groups
   */
  List<NodeRef> getInterestGroups(final NodeRef category);

  /**
   * Gets the list of Interest Groups belonging to one category
   * ({@code GET /categories/{id}/groups}).
   *
   * @param id the Category ID
   * @return the list of all Interest Groups in the given Category
   */
  List<InterestGroup> getInterestGroupByCategoryId(String id);

  /**
   * Exports the profiles defined in the given category
   * ({@code GET /categories/{id}/exportedProfiles}).
   *
   * @param id the Category ID
   * @param ignoreIgId identifier of an Interest Group whose profiles should be excluded from the
   *     export; may be {@code null} to export all
   * @return the list of exported {@link Profile}s for the category
   */
  List<Profile> categoriesIdExportedProfilesGet(String id, String ignoreIgId);

  /**
   * Creates a new Interest Group inside the category
   * ({@code POST /categories/{id}/groups}).
   *
   * @param id the Category ID the new Interest Group is created under
   * @param ig the model describing the Interest Group to create
   * @return the newly created {@link InterestGroup}
   */
  InterestGroup categoriesIdGroupsPost(String id, InterestGroupPostModel ig);

  /**
   * Retrieves a paged view of the content statistics for the Interest Groups of a category.
   *
   * @param id the Category ID
   * @param startItem the zero-based index of the first item to return
   * @param amount the maximum number of items to return
   * @return the paged content statistics for the category
   */
  PagedStatisticsContents getIGStatisticsContents(
    String id,
    int startItem,
    int amount
  );

  /**
   * Triggers (re)calculation of the Interest Group statistics for the given category.
   *
   * @param id the Category ID whose statistics should be recalculated
   */
  void calculateIGStatistics(String id);

  /**
   * Submits a request to create a new Interest Group within the given category
   * ({@code POST /categories/{categoryId}/groupRequest}).
   *
   * @param categoryId the Category ID the request targets
   * @param body the details of the Interest Group creation request
   */
  void categoriesIdGroupRequestPost(
    String categoryId,
    GroupCreationRequest body
  );

  /**
   * Lists the administrators of the given category
   * ({@code GET /categories/{categoryId}/admins}).
   *
   * @param categoryId the Category ID
   * @return the list of {@link User}s who are administrators of the category
   */
  List<User> categoriesIdAdminsGet(String categoryId);

  /**
   * Gets the list of logos uploaded in the category.
   *
   * @param categoryId the Category ID
   * @return the list of logo {@link Node}s stored in the category's logos folder
   */
  List<Node> getCategoryLogoByCategoryId(String categoryId);

  /**
   * Uploads a new logo into the category's logos folder.
   *
   * @param categoryId the Category ID the logo is uploaded to
   * @param inputStream the stream providing the logo file content
   * @param fileName the name to give the uploaded logo file
   */
  void postCategoryLogoByCategoryId(
    String categoryId,
    InputStream inputStream,
    String fileName
  );

  /**
   * Marks one logo as the selected logo for the category.
   *
   * @param categoryId the Category ID
   * @param logoId the identifier of the logo to mark as selected
   */
  void selectCategoryLogoByLogoId(String categoryId, String logoId);

  /**
   * Deletes a logo from the category's logos folder.
   *
   * @param categoryId the Category ID
   * @param logoId the identifier of the logo to delete
   * @return the list of remaining logo {@link Node}s after the deletion
   */
  List<Node> deleteCategoryLogoByLogoId(String categoryId, String logoId);

  /**
   * Gets the details of a category ({@code GET /categories/{categoryId}}).
   *
   * @param categoryId the Category ID
   * @return the {@link Category} details
   */
  Category categoriesIdGet(String categoryId);

  /**
   * Updates the details of a category ({@code PUT /categories/{categoryId}}).
   *
   * @param categoryId the Category ID to update
   * @param category the new category details to apply
   * @return the updated {@link Category}
   */
  Category categoriesIdPut(String categoryId, Category category);

  /**
   * Adds new users as administrators of a category
   * ({@code POST /categories/{categoryId}/admins}).
   *
   * @param categoryId the Category ID
   * @param userIds the identifiers of the users to grant administrator rights
   * @return the list of user identifiers that were added as administrators
   */
  List<String> categoriesIdAdminsPost(String categoryId, List<String> userIds);

  /**
   * Removes an administrator from a category
   * ({@code DELETE /categories/{categoryId}/admins/{userId}}).
   *
   * @param categoryId the Category ID
   * @param userId the identifier of the user to remove as administrator
   */
  void categoriesIdAdminsDelete(String categoryId, String userId);

  /**
   * Creates a new category under the given header
   * ({@code POST /headers/{headerId}/category}).
   *
   * @param headerId the Header ID the category is created under
   * @param categoryBody the details of the category to create
   * @return the newly created {@link Category}
   */
  Category headersIdCategoryPost(String headerId, Category categoryBody);

  /**
   * Contacts the administrators of a category
   * ({@code POST /categories/{categoryId}/adminContact}).
   *
   * @param categoryId the Category ID whose administrators are contacted
   * @param body the contact request, including the message to send
   */
  void categoriesIdAdminContactPost(
    String categoryId,
    AdminContactRequest body
  );

  /**
   * Retrieves a paged, optionally filtered list of Interest Group creation requests for a category
   * ({@code GET /categories/{categoryRef}/groupRequests}).
   *
   * @param categoryRef the reference of the category
   * @param limit the maximum number of requests per page
   * @param page the page number to return
   * @param filter an optional filter expression to narrow the results; may be {@code null}
   * @return the paged Interest Group creation requests
   */
  PagedGroupCreationRequests categoriesIdGroupRequestsGet(
    String categoryRef,
    Integer limit,
    Integer page,
    String filter
  );

  /**
   * Approves or rejects an Interest Group creation request for a category
   * ({@code POST /categories/{categoryId}/groupRequestApproval}).
   *
   * @param categoryId the Category ID the request belongs to
   * @param body the approval decision and related details
   * @param username the name of the user performing the approval
   */
  void categoriesIdGroupRequestApprovalPost(
    String categoryId,
    GroupCreationRequestApproval body,
    String username
  );

  /**
   * Updates an existing Interest Group creation request
   * ({@code PUT /categories/groupRequest/{requestId}}).
   *
   * @param requestId the identifier of the group creation request to update
   * @param body the new details for the request
   */
  void categoriesGroupRequestPut(
    String categoryId,
    String requestId,
    GroupCreationRequest body
  );

  /**
   * Submits a request to delete an Interest Group.
   *
   * @param body the details of the Interest Group deletion request
   */
  void groupIdDeleteRequestPost(GroupDeletionRequest body);

  /**
   * Checks whether a pending deletion request already exists for the given Interest Group.
   *
   * @param groupId the identifier of the Interest Group
   * @return {@code true} if a deletion request exists for the group, {@code false} otherwise
   */
  boolean existsGroupDeleteRequest(String groupId);

  /**
   * Retrieves a paged, optionally filtered list of Interest Group deletion requests for a category
   * ({@code GET /categories/{categoryRef}/groupDeleteRequests}).
   *
   * @param categoryRef the reference of the category
   * @param limit the maximum number of requests per page
   * @param page the page number to return
   * @param filter an optional filter expression to narrow the results; may be {@code null}
   * @return the paged Interest Group deletion requests
   */
  PagedGroupDeletionRequests categoriesIdGroupDeleteRequestsGet(
    String categoryRef,
    Integer limit,
    Integer page,
    String filter
  );

  /**
   * Approves or rejects an Interest Group deletion request for a category
   * ({@code POST /categories/{categoryId}/groupRequestDeleteApproval}).
   *
   * @param categoryId the Category ID the request belongs to
   * @param body the deletion approval decision and related details
   * @param currentUserName the name of the user performing the approval
   */
  void categoriesIdGroupRequestDeleteApprovalPost(
    String categoryId,
    GroupDeletionRequestApproval body,
    String currentUserName
  );
}
