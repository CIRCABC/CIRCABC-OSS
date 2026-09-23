package io.swagger.api;

import io.swagger.exception.CustomizationException;
import io.swagger.model.*;
import io.swagger.model.db.ActivityCountDAO;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Business operations for CIRCABC Interest Groups (IGs).
 *
 * <p>This interface defines the service contract consumed by the CIRCABC REST
 * webscript layer to manage Interest Groups and their related resources. It
 * covers a broad range of concerns, including:
 *
 * <ul>
 *   <li>Reading Interest Group definitions, dashboards and recent discussions;</li>
 *   <li>Creating, updating and deleting Interest Groups and their configuration;</li>
 *   <li>Managing members, membership requests (applicants) and membership
 *       expiration dates;</li>
 *   <li>Producing summary statistics, timelines and structure information;</li>
 *   <li>Exporting summaries and importing content from ZIP archives;</li>
 *   <li>Managing Interest Group logos.</li>
 * </ul>
 *
 * <p>Implementations contain the actual logic (typically backed by the Alfresco
 * repository), while webscript endpoint classes delegate to this interface.
 */
public interface GroupsApi {
  /**
   * Returns the details of the Interest Group identified by the given repository
   * node reference.
   *
   * @param igRef the {@link NodeRef} of the Interest Group node
   * @return the {@link InterestGroup} details
   */
  InterestGroup getInterestGroupDetails(final NodeRef igRef);

  /**
   * Returns the details of the Interest Group identified by the given repository
   * node reference, optionally in a reduced ("light") representation.
   *
   * @param igRef the {@link NodeRef} of the Interest Group node
   * @param lightMode {@code true} to return a lightweight representation with
   *     fewer properties, {@code false} for the full representation
   * @return the {@link InterestGroup} details
   */
  InterestGroup getInterestGroupDetails(final NodeRef igRef, boolean lightMode);

  /**
   * Gets the definition and properties of one Interest Group.
   *
   * @param id the node identifier of the Interest Group
   * @return the {@link InterestGroup} definition and properties
   */
  InterestGroup getInterestGroup(String id);

  /**
   * Gets the definition and properties of one Interest Group, optionally in a
   * reduced ("light") representation.
   *
   * @param interestGroupNodeId the node identifier of the Interest Group
   * @param lightMode {@code true} to return a lightweight representation with
   *     fewer properties, {@code false} for the full representation
   * @return the {@link InterestGroup} definition and properties
   */
  InterestGroup getInterestGroup(String interestGroupNodeId, boolean lightMode);

  /**
   * Gets the dashboard of an Interest Group, including its events and calendar.
   *
   * @param id the node identifier of the Interest Group
   * @return the {@link GroupDashboard} for the Interest Group
   */
  GroupDashboard getGroupDashboard(String id);

  /**
   * Returns the most recent discussions of an Interest Group.
   *
   * @param id the node identifier of the Interest Group
   * @return the list of {@link RecentDiscussion} items
   */
  List<RecentDiscussion> getGroupRecentDiscussions(String id);

  /**
   * Deletes one Interest Group.
   *
   * @param id the node identifier of the Interest Group to delete
   * @param purgeData {@code true} to permanently purge the group's data instead
   *     of moving it to the trash
   * @param purgeLogs {@code true} to also purge the group's logs
   */
  void groupsIdDelete(String id, Boolean purgeData, Boolean purgeLogs);

  /**
   * Gets the pending membership requests (applicants) of an Interest Group.
   *
   * @param id the node identifier of the Interest Group
   * @return the list of {@link Applicant} membership requests
   */
  List<Applicant> groupsIdMembersApplicantsGet(String id);

  /**
   * Gets the members of an Interest Group together with their respective
   * profiles, filtered by profile and matched against a free-text search query.
   *
   * @param id the node identifier of the Interest Group
   * @param profile the list of profile identifiers to filter members by
   * @param language the language used for localized profile labels
   * @param searchQuery a free-text query to match members against
   * @return the list of matching {@link UserProfile} members
   */
  List<UserProfile> groupsIdMembersGet(
    String id,
    List<String> profile,
    String language,
    String searchQuery
  );

  /**
   * Gets the members of an Interest Group together with their respective
   * profiles, filtered by profile and by individual identity fields.
   *
   * @param id the node identifier of the Interest Group
   * @param profile the list of profile identifiers to filter members by
   * @param language the language used for localized profile labels
   * @param firstName the first name to filter members by
   * @param lastName the last name to filter members by
   * @param email the email address to filter members by
   * @return the list of matching {@link UserProfile} members
   */
  List<UserProfile> groupsIdMembersGet(
    String id,
    List<String> profile,
    String language,
    String firstName,
    String lastName,
    String email
  );

  /**
   * Gets a paginated view of the members of an Interest Group, filtered by
   * profile and matched against a free-text search query.
   *
   * @param id the node identifier of the Interest Group
   * @param profile the list of profile identifiers to filter members by
   * @param language the language used for localized profile labels
   * @param limit the maximum number of members to return per page
   * @param page the zero- or one-based page index (implementation defined)
   * @param order the sort order to apply to the results
   * @param searchQuery a free-text query to match members against
   * @return a {@link PagedUserProfile} containing the requested page of members
   */
  PagedUserProfile groupsIdMembersGet(
    String id,
    List<String> profile,
    String language,
    Integer limit,
    Integer page,
    String order,
    String searchQuery
  );

  /**
   * Gets a paginated view of the members of an Interest Group, filtered by
   * profile and by individual identity fields.
   *
   * @param id the node identifier of the Interest Group
   * @param profile the list of profile identifiers to filter members by
   * @param language the language used for localized profile labels
   * @param limit the maximum number of members to return per page
   * @param page the zero- or one-based page index (implementation defined)
   * @param order the sort order to apply to the results
   * @param firstName the first name to filter members by
   * @param lastName the last name to filter members by
   * @param email the email address to filter members by
   * @return a {@link PagedUserProfile} containing the requested page of members
   */
  @SuppressWarnings("java:S107") // REST endpoint query parameters
  PagedUserProfile groupsIdMembersGet(
    String id,
    List<String> profile,
    String language,
    Integer limit,
    Integer page,
    String order,
    String firstName,
    String lastName,
    String email
  );

  /**
   * Adds a new member to the Interest Group.
   *
   * <p>Backs the {@code POST /groups/{id}/members} endpoint.
   *
   * @param groupNodeRef the {@link NodeRef} of the Interest Group
   * @param body the membership definition describing the member(s) and profile
   *     to add
   * @return the resulting {@link MembershipPostDefinition}
   */
  MembershipPostDefinition groupsIdMembersPost(
    NodeRef groupNodeRef,
    MembershipPostDefinition body
  );

  /**
   * Updates existing members of the Interest Group.
   *
   * <p>Backs the {@code PUT /groups/{id}/members} endpoint.
   *
   * @param groupNodeRef the {@link NodeRef} of the Interest Group
   * @param body the membership definition describing the member(s) and profile
   *     to update
   * @return the resulting {@link MembershipPostDefinition}
   */
  MembershipPostDefinition groupsIdMembersPut(
    NodeRef groupNodeRef,
    MembershipPostDefinition body
  );

  /**
   * Removes one user from the members of one Interest Group.
   *
   * @param id the node identifier of the Interest Group
   * @param userId the identifier of the user to remove
   */
  void groupsIdMembersUserIdDelete(String id, String userId);

  /**
   * Updates the properties of an Interest Group.
   *
   * @param id the node identifier of the Interest Group
   * @param body the {@link InterestGroup} carrying the updated properties
   */
  void groupsIdPut(String id, InterestGroup body);

  /**
   * Handles an existing membership applicant: either invites the applicant or
   * declines the application.
   *
   * @param id the node identifier of the Interest Group
   * @param body the {@link ApplicantAction} describing the action to perform
   */
  void groupsIdMembersApplicantsPut(String id, ApplicantAction body);

  /**
   * Adds a new membership applicant request.
   *
   * @param id the node identifier of the Interest Group
   * @param body the {@link ApplicantAction} describing the application request
   */
  void groupsIdMembersApplicantsPost(String id, ApplicantAction body);

  /**
   * Gets the statistics of the Interest Group given its node identifier.
   *
   * <p>Backs the {@code GET /groups/{id}/summary/statistics} endpoint.
   *
   * @param id the node identifier of the Interest Group
   * @param calculate {@code true} to (re)compute the statistics, {@code false}
   *     to return cached/stored values
   * @param forExport {@code true} when the statistics are requested for export
   *     purposes
   * @return the list of {@link StatData} statistics entries
   */
  List<StatData> getIGSummaryStatistics(
    String id,
    boolean calculate,
    boolean forExport
  );

  /**
   * Gets the activity timeline of the Interest Group given its node identifier.
   *
   * <p>Backs the {@code GET /groups/{id}/summary/timeline} endpoint.
   *
   * @param id the node identifier of the Interest Group
   * @return the list of {@link ActivityCountDAO} activity counts over time
   */
  List<ActivityCountDAO> getIGSummaryTimeline(String id);

  /**
   * Gets the structure of the Interest Group given its node identifier.
   *
   * <p>Backs the {@code GET /groups/{id}/summary/structure} endpoint.
   *
   * @param id the node identifier of the Interest Group
   * @return a serialized representation of the Interest Group structure
   */
  String getIGSummaryStructure(String id);

  /**
   * Exports the summary of an Interest Group as XML, XLS or CSV, writing the
   * result directly to the webscript response.
   *
   * @param id the node identifier of the Interest Group
   * @param format the export format (e.g. XML, XLS or CSV)
   * @param type the type of summary to export
   * @param response the {@link WebScriptResponse} to write the export to
   */
  void exportSummary(
    String id,
    String format,
    String type,
    WebScriptResponse response
  );

  /**
   * Generates the index file template used by the import operation and writes it
   * to the webscript response.
   *
   * @param response the {@link WebScriptResponse} to write the template to
   * @throws IOException if writing the template to the response fails
   */
  void generateImportIndexFileTemplate(WebScriptResponse response)
    throws IOException;

  /**
   * Imports content from a ZIP file provided as an input stream into the target
   * folder.
   *
   * @param folderId the node identifier of the destination folder
   * @param fileInputStream the input stream of the ZIP file to import
   * @param fileName the name of the uploaded file
   * @param mimeType the MIME type of the uploaded file
   * @param notifyUser {@code true} to notify the user once the import completes
   * @param deleteFile {@code true} to delete the source file after import
   * @param disableNotification {@code true} to disable notifications triggered
   *     by the imported content
   * @param encoding the character encoding used for the import
   */
  @SuppressWarnings("java:S107") // Import operation parameters
  void importZipFile(
    String folderId,
    InputStream fileInputStream,
    String fileName,
    String mimeType,
    boolean notifyUser,
    boolean deleteFile,
    boolean disableNotification,
    String encoding
  );

  /**
   * Adds a new member during Interest Group creation without synchronizing with
   * the CBC tables.
   *
   * @param groupNodeRef the {@link NodeRef} of the Interest Group
   * @param body the membership definition describing the member(s) and profile
   *     to add
   * @return the resulting {@link MembershipPostDefinition}
   */
  MembershipPostDefinition groupsIdMembersPostNoSync(
    NodeRef groupNodeRef,
    MembershipPostDefinition body
  );

  /**
   * Returns the Interest Groups most recently visited by the current user.
   *
   * @param amount the maximum number of Interest Groups to return
   * @return the list of recently visited {@link InterestGroup}s
   */
  List<InterestGroup> getVisitedGroups(int amount);

  /**
   * Returns the Interest Groups most recently visited by the given user.
   *
   * @param username the user whose visited Interest Groups are requested
   * @param amount the maximum number of Interest Groups to return
   * @return the list of recently visited {@link InterestGroup}s
   */
  List<InterestGroup> getVisitedGroups(String username, int amount);

  /**
   * Gets the configuration of an Interest Group.
   *
   * @param groupIp the identifier of the Interest Group
   * @return the {@link GroupConfiguration} for the Interest Group
   */
  GroupConfiguration getInterestGroupConfiguration(String groupIp);

  /**
   * Updates the configuration of an Interest Group.
   *
   * @param groupIp the identifier of the Interest Group
   * @param body the {@link GroupConfiguration} carrying the updated configuration
   * @return the updated {@link GroupConfiguration}
   */
  GroupConfiguration putInterestGroupConfiguration(
    String groupIp,
    GroupConfiguration body
  );

  /**
   * Returns the available logos of an Interest Group.
   *
   * @param groupId the identifier of the Interest Group
   * @return the list of logo {@link Node}s
   */
  List<Node> getInterestGroupLogos(String groupId);

  /**
   * Uploads a new logo for an Interest Group.
   *
   * @param groupId the identifier of the Interest Group
   * @param inputStream the input stream of the logo image content
   * @param filename the name of the uploaded logo file
   * @return the created logo {@link Node}
   */
  Node postGroupLogoByGroupId(
    String groupId,
    InputStream inputStream,
    String filename
  );

  /**
   * Selects which of an Interest Group's logos is the active one.
   *
   * @param groupId the identifier of the Interest Group
   * @param logoId the identifier of the logo to select
   * @throws CustomizationException if the logo cannot be selected
   */
  void putSelectedLogo(String groupId, String logoId)
    throws CustomizationException;

  /**
   * Deletes a logo from an Interest Group.
   *
   * @param groupId the identifier of the Interest Group
   * @param logoId the identifier of the logo to delete
   * @throws CustomizationException if the logo cannot be deleted
   */
  void deleteLogo(String groupId, String logoId) throws CustomizationException;

  /**
   * Counts the number of members in an Interest Group.
   *
   * @param igId the identifier of the Interest Group
   * @return the number of members in the Interest Group
   */
  int countMembersInIg(String igId);

  /**
   * Removes the membership expiration date for a user in an Interest Group.
   *
   * @param id the node identifier of the Interest Group
   * @param userId the identifier of the user whose expiration is removed
   */
  void groupsIdMembersUserIdExpirationDelete(String id, String userId);

  /**
   * Updates the membership expiration date for a user in an Interest Group.
   *
   * @param id the node identifier of the Interest Group
   * @param userId the identifier of the user whose expiration is updated
   * @param expirationDate the new expiration date for the membership
   */
  void groupsIdMembersUserIdExpirationPut(
    String id,
    String userId,
    Date expirationDate
  );

  /**
   * Sets the membership expiration date for a user in an Interest Group,
   * targeting a specific profile and Alfresco group.
   *
   * @param id the node identifier of the Interest Group
   * @param userId the identifier of the user whose expiration is set
   * @param expirationDate the expiration date for the membership
   * @param profileId the identifier of the profile the expiration applies to
   * @param alfrescoGroup the Alfresco group the expiration applies to
   */
  void groupsIdMembersUserIdExpirationPost(
    String id,
    String userId,
    Date expirationDate,
    String profileId,
    String alfrescoGroup
  );

  /**
   * Flags an Interest Group as pending deletion (or clears that flag).
   *
   * @param interestGroupID the database identifier of the Interest Group
   * @param b {@code true} to mark the Interest Group to be deleted,
   *     {@code false} to clear the flag
   */
  void updateIgToBeDeleted(long interestGroupID, boolean b);
}
