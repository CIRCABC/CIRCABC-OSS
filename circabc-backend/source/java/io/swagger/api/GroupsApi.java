package io.swagger.api;

import eu.cec.digit.circabc.repo.customisation.CustomizationException;
import eu.cec.digit.circabc.repo.log.ActivityCountDAO;
import eu.cec.digit.circabc.repo.statistics.ig.StatData;
import io.swagger.model.*;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

// the groups API
public interface GroupsApi {

    InterestGroup getInterestGroupDetails(final NodeRef igRef);

    InterestGroup getInterestGroupDetails(final NodeRef igRef, boolean lightMode);

    // get the definition and properties of one Interest group
    // GET /groups/{id}
    InterestGroup getInterestGroup(String id);

    InterestGroup getInterestGroup(String interestGroupNodeId, boolean lightMode);

    // get the events and the calendar object of one Interest group if the
    // parameter month is selected, then it return the calendar object for the
    // specified month else it returns for the current month all items are
    // filtered depending on the user's permissions
    // !!! TODO !!! How to implement lazy loading with this object and the
    // methods behind
    // GET /groups/{id}/dashboard
    GroupDashboard getGroupDashboard(String id);

    List<RecentDiscussion> getGroupRecentDiscussions(String id);

    // get the list of all Interest groups to see if it makes sense at all
    // GET /groups",
    List<InterestGroup> groupsGet(String language);

    // delete one Interest group
    // DELETE /groups/{id}
    void groupsIdDelete(String id, Boolean purgeData, Boolean purgeLogs);

    // get the membership requests of an Interest
    // GET /groups/{id}/members/applicants",
    List<Applicant> groupsIdMembersApplicantsGet(String id);

    // get the members of an Interest group with their respective profile
    // GET /groups/{id}/members",
    List<UserProfile> groupsIdMembersGet(
            String id, List<String> profile, String language, String searchQuery);

    PagedUserProfile groupsIdMembersGet(
            String id,
            List<String> profile,
            String language,
            Integer limit,
            Integer page,
            String order,
            String searchQuery);

    // add a new member in the group
    // POST /groups/{id}/members",
    MembershipPostDefinition groupsIdMembersPost(NodeRef groupNodeRef, MembershipPostDefinition body);

    // updates members in the group
    // PUT /groups/{id}/members",
    MembershipPostDefinition groupsIdMembersPut(NodeRef groupNodeRef, MembershipPostDefinition body);

    // get the membership requests of an Interest the language parameter is used
    // to filter the profile title, if needed
    // ", response = Member.class, responseContainer = "List", authorizations =
    // {
    // GET/groups/{id}/members/search",
    List<Member> groupsIdMembersSearchGet(String id, String query, String language);

    // remove one user from the members of one Interest group
    // DELETE/groups/{id}/members/{userId}",
    void groupsIdMembersUserIdDelete(String id, String userId);

    // Update the properties of an new Interest Group
    // PUT /groups/{id}
    void groupsIdPut(String id, InterestGroup body);

    // Creates a new Interest Group
    // POST /groups
    void groupsPost(InterestGroup body);

    // used to remove a member applicant once it has been invited
    // or decline its applcation
    void groupsIdMembersApplicantsPut(String id, ApplicantAction body);

    // used to add a member applicant request
    void groupsIdMembersApplicantsPost(String id, ApplicantAction body);

    // get the statistics of the IG given its node id
    // GET /groups/{id}/summary/statistics
    List<StatData> getIGSummaryStatistics(String id, boolean calculate, boolean forExport);

    // get the timeline of the IG given its node id
    // GET /groups/{id}/summary/timeline
    List<ActivityCountDAO> getIGSummaryTimeline(String id);

    // get the structure of the IG given its node id
    // GET /groups/{id}/summary/structure
    String getIGSummaryStructure(String id);

    // export the summary of an Interest group as XML, XLS or CSV
    void exportSummary(String id, String format, String type, WebScriptResponse response);

    // generate the index file template for the import operation
    void generateImportIndexFileTemplate(WebScriptResponse response) throws IOException;

    // imports a zip file given as an input stream
    void importZipFile(
            String folderId,
            InputStream fileInputStream,
            String fileName,
            String mimeType,
            boolean notifyUser,
            boolean deleteFile,
            boolean disableNotification,
            String encoding);

    /**
     * used during the IG creation for no sync with CBC tables
     */
    MembershipPostDefinition groupsIdMembersPostNoSync(
            NodeRef groupNodeRef, MembershipPostDefinition body);

    List<InterestGroup> getVisitedGroups(int amount);

    List<InterestGroup> getVisitedGroups(String username, int amount);

    GroupConfiguration getInterestGroupConfiguration(String groupIp);

    GroupConfiguration putInterestGroupConfiguration(String groupIp, GroupConfiguration body);

    List<Node> getInterestGroupLogos(String groupId);

    Node postGroupLogoByGroupId(String groupId, InputStream inputStream, String filename);

    void putSelectedLogo(String groupId, String logoId) throws CustomizationException;

    void deleteLogo(String groupId, String logoId) throws CustomizationException;

    int countMembersInIg(String igId);

    void groupsIdMembersUserIdExpirationDelete(String id, String userId);

    void groupsIdMembersUserIdExpirationPut(String id, String userId, Date expirationDate);

    void groupsIdMembersUserIdExpirationPost(
            String id, String userId, Date expirationDate, String profileId, String alfrescoGroup);
}
