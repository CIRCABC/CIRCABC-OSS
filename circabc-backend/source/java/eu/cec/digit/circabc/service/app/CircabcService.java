package eu.cec.digit.circabc.service.app;

import eu.cec.digit.circabc.repo.app.model.*;
import eu.cec.digit.circabc.service.profile.Profile;
import eu.cec.digit.circabc.service.user.UserCategoryMembershipRecord;
import eu.cec.digit.circabc.service.user.UserIGMembershipRecord;
import eu.cec.digit.circabc.web.wai.bean.navigation.CategoryHeader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface CircabcService {

    void loadModel();

    void addCategoryAdmin(NodeRef categoryNodeRef, String userName);

    void removeCategoryAdmin(NodeRef categoryNodeRef, String userName);

    void addPersonToProfile(NodeRef igNodeRef, String userName, String profileName);

    void uninvitePerson(NodeRef igNodeRef, String userName);

    void changePersonProfile(NodeRef igNodeRef, String userName, String profileName);

    void exportProfile(NodeRef igNodeRef, String profileName, boolean export);

    void importProfile(
            NodeRef toIgNoderef,
            NodeRef fromIgNoderef,
            String fromProfileName,
            Map<String, Set<String>> servicesPermissions);

    void deleteProfile(NodeRef igNodeRef, String profileName);

    void updateCanApplyForMemberhip(NodeRef igNodeRef);

    void updateProfile(
            NodeRef igNodeRef,
            String profileName,
            Map<String, Set<String>> servicesPermissions,
            Profile profile);

    void updateProfileTitles(long igID, String profileName);

    void setUILangauge(String currentUser, String language);

    void addCategoryNode(NodeRef headerNodeRef, NodeRef categoryNodeRef);

    void addInterestGroupNode(NodeRef catNodeRef, NodeRef igRootNodeRef);

    void addUser(NodeRef userNodeRef);

    void updateIntestGroupProperties(NodeRef igNodeRef);

    void updateCategoryProperties(NodeRef catNodeRef);

    void deleteCategory(NodeRef catNodeRef);

    void deleteIntestGroup(NodeRef igNodeRef);

    void deleteIntestGroupByID(Long interestGroupID);

    void moveIG(NodeRef oldCategoryRef, NodeRef newCategoryRef, NodeRef igRef);

    void addHeaderNode(NodeRef headerNodeRef);

    void deleteHeader(NodeRef nodeRef);

    List<InterestGroupItem> getInterestGroupByCategoryUser(NodeRef categoryRef, String userName);

    /**
     * @return list of profile that could be imported in interest group
     */
    List<ExportedProfileItem> getExportedProfiles(NodeRef categoryRef, NodeRef igRef);

    Set<String> getCategoryAlfrescoGroupsExceptCurrentIG(NodeRef category, NodeRef igNodeRef);

    List<UserIGMembershipRecord> getInterestGroups(String userName);

    List<UserCategoryMembershipRecord> getCategories(String userName);

    void updateUser(NodeRef userNodeRef);

    boolean syncEnabled();

    boolean readFromDatabase();

    void updateHeader(NodeRef headerNodeRef, String name, String description);

    void resyncInterestGroup(NodeRef nodeRef);

    void resyncCategory(NodeRef nodeRef);

    void resyncUsers();

    List<CategoryHeader> getHeadersAndCategories();

    List<ProfileWithUsersCount> getProfiles(NodeRef nodeRef, long localeID);

    Long getUserLocaleID(String userName);

    List<UserWithProfile> getFilteredUsers(
            NodeRef igNodeRef, long localeID, String alfrescoGroup, String searchText);

    List<UserWithProfile> getFilteredUsers(
            NodeRef igNodeRef, long localeID, String alfrescoGroup, String searchText, String order);

    List<eu.cec.digit.circabc.repo.app.model.Profile> getProfilesForIg(
            NodeRef igNodeRef, long localeID, String searchQuery);

    Integer getNumberOfAdmintrators(NodeRef igNodeRef);

    void postOrUpdateProfile(io.swagger.model.Profile profile);

    NodeService getNodeService();

    boolean isUserMember(NodeRef igNodeRef, String userName);

    boolean isCategoryAdmin(NodeRef categoryNodeRef, String userName);

    boolean isCategoryAdminOfInterestGroup(NodeRef interestGroupNodeRef, String userName);

    boolean isCircabcAdmin(String userName);

    void addCircabcAdmin(String userName);

    void removeCircabcAdmin(String userName);

    void resyncCircabcAdmins();

    InterestGroupResult getInterestGroup(NodeRef interestGroupNodeRef);

    List<String> getCategoryAdmins(NodeRef categoryNodeRef);

    void deletePersonFromGroup(NodeRef igNodeRef, String userName);

    void updateLogoRefProperty(NodeRef categoryNodeRef);

    void setGroupLogoNode(NodeRef igNodeRef, NodeRef logoNodeRef);

    Map<String, String> getInterestGroupTitle(NodeRef igRef);

    Map<String, String> getProfileTitle(NodeRef profileId);

    Map<String, String> getCategoryTitle(NodeRef categRef);

    boolean isUserExists(String userName);

    void updateInterestGroupPublic(NodeRef igRef, Boolean isGuest);

    void updateInterestGroupRegistered(NodeRef igRef, Boolean isRegistered);

    void updateInterestGroupApplication(NodeRef igRef, Boolean applicationAllowed);

    void deleteAll();

    void resyncAll();

    int countMembersInIg(String id);

    Set<String> getUserIds(String interestGroupId);

    void addUser(String userName);

    void syncGroupLogos();

    boolean isUserDirAdminOrCategoryAdminOrCircabcAdmin(String userName);
}
