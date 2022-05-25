package eu.cec.digit.circabc.repo.app;

import eu.cec.digit.circabc.repo.customisation.logo.LogoPreferencesServiceImpl;
import eu.cec.digit.circabc.repo.profile.category.CategoryProfileManagerServiceImpl;
import eu.cec.digit.circabc.repo.profile.interestGroup.IGRootProfileManagerServiceImpl;
import eu.cec.digit.circabc.repo.user.UserServiceImpl;
import eu.cec.digit.circabc.service.app.CircabcService;
import eu.cec.digit.circabc.service.profile.CircabcRootProfileManagerService;
import eu.cec.digit.circabc.service.profile.Profile;
import io.swagger.api.CategoriesApiImpl;
import io.swagger.api.GroupsApiImpl;
import io.swagger.api.HeadersApiImpl;
import io.swagger.api.ProfilesApiImpl;
import io.swagger.model.Category;
import io.swagger.model.Header;
import io.swagger.model.MembershipPostDefinition;
import io.swagger.model.UserProfile;
import io.swagger.util.Converter;

import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CircabcMethodInterceptor implements MethodInterceptor {

    public static final String ADD_PERSON_TO_PROFILE = "addPersonToProfile";
    public static final String UNINVITE_PERSON = "uninvitePerson";
    private static final Log logger = LogFactory.getLog(CircabcMethodInterceptor.class);
    private String igServiceSimpleName;
    private String categoryServiceSimpleName;
    private String circabcServiceSimpleName;
    private String userServiceSimpleName;
    private String headersApiImplSimpleName;
    private String profilesApiImplSimpleName;
    private String groupsApiServiceSimpleName;
    private String categoriesApiServiceSimpleName;
    private String logoPreferencesServiceSimpleName;
    private CircabcService circabcService;
    private SimpleCache<String, Map<String, Profile>> profileMapCache;

    public void init() {
        circabcServiceSimpleName = CircabcRootProfileManagerService.class.getSimpleName();
        categoryServiceSimpleName = CategoryProfileManagerServiceImpl.class.getSimpleName();
        igServiceSimpleName = IGRootProfileManagerServiceImpl.class.getSimpleName();
        userServiceSimpleName = UserServiceImpl.class.getSimpleName();
        headersApiImplSimpleName = HeadersApiImpl.class.getSimpleName();
        profilesApiImplSimpleName = ProfilesApiImpl.class.getSimpleName();
        groupsApiServiceSimpleName = GroupsApiImpl.class.getSimpleName();
        categoriesApiServiceSimpleName = CategoriesApiImpl.class.getSimpleName();
        logoPreferencesServiceSimpleName = LogoPreferencesServiceImpl.class.getSimpleName();
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        final String targetName = invocation.getThis().getClass().getSimpleName();
        final String methodName = invocation.getMethod().getName();
        final Object[] arguments = invocation.getArguments();
        Object result = invocation.proceed();
        if (!circabcService.syncEnabled()) {
            return result;
        }
        final boolean infoEnabled = logger.isInfoEnabled();
        if (infoEnabled) {
            logger.info("targetName : " + targetName);
            logger.info("methodName : " + methodName);
            logger.info("arguments : " + Arrays.toString(arguments));
            if (result != null) {
                logger.info("result : " + result.toString());
            } else {
                logger.info("result : null ");
            }
        }
        if (targetName.equals(igServiceSimpleName)) {
            switch (methodName) {
                case ADD_PERSON_TO_PROFILE: {
                    NodeRef igNodeRef = (NodeRef) arguments[0];
                    String userName = (String) arguments[1];
                    String profileName = (String) arguments[2];
                    circabcService.addPersonToProfile(igNodeRef, userName, profileName);
                    break;
                }
                case UNINVITE_PERSON: {
                    NodeRef igNodeRef = (NodeRef) arguments[0];
                    String userName = (String) arguments[1];
                    circabcService.uninvitePerson(igNodeRef, userName);
                    break;
                }
                case "changePersonProfile": {
                    NodeRef igNodeRef = (NodeRef) arguments[0];
                    String userName = (String) arguments[1];
                    String profileName = (String) arguments[2];
                    circabcService.changePersonProfile(igNodeRef, userName, profileName);
                    break;
                }
                case "updateProfile": {
                    NodeRef igNodeRef = (NodeRef) arguments[0];
                    String profileName = (String) arguments[1];
                    @SuppressWarnings("unchecked")
                    Map<String, Set<String>> servicesPermissions = (Map<String, Set<String>>) arguments[2];
                    Profile profile = (Profile) result;
                    circabcService.updateProfile(igNodeRef, profileName, servicesPermissions, profile);
                    break;
                }
                case "exportProfile": {
                    NodeRef igNodeRef = (NodeRef) arguments[0];
                    String profileName = (String) arguments[1];
                    boolean export = (Boolean) arguments[2];
                    circabcService.exportProfile(igNodeRef, profileName, export);
                    break;
                }
                case "importProfile": {
                    NodeRef toIgNoderef = (NodeRef) arguments[0];
                    NodeRef fromIgNoderef = (NodeRef) arguments[1];
                    String fromProfileName = (String) arguments[2];
                    @SuppressWarnings("unchecked")
                    Map<String, Set<String>> servicesPermissions = (Map<String, Set<String>>) arguments[3];
                    circabcService.importProfile(
                            toIgNoderef, fromIgNoderef, fromProfileName, servicesPermissions);
                    circabcService.resyncInterestGroup(toIgNoderef);
                    break;
                }
                case "deleteProfile": {
                    NodeRef igNodeRef = (NodeRef) arguments[0];
                    String profileName = (String) arguments[1];
                    circabcService.deleteProfile(igNodeRef, profileName);
                    break;
                }
            }

        } else if (targetName.equals(categoryServiceSimpleName)) {
            if (methodName.equals(ADD_PERSON_TO_PROFILE)) {
                NodeRef catNodeRef = (NodeRef) arguments[0];
                String userName = (String) arguments[1];
                circabcService.addCategoryAdmin(catNodeRef, userName);
            } else if (methodName.equals(UNINVITE_PERSON)) {
                NodeRef catNodeRef = (NodeRef) arguments[0];
                String userName = (String) arguments[1];
                circabcService.removeCategoryAdmin(catNodeRef, userName);
            }
        } else if (targetName.equals(circabcServiceSimpleName)) {
            if (methodName.equals(ADD_PERSON_TO_PROFILE)) {
                String userName = (String) arguments[1];
                circabcService.addCircabcAdmin(userName);
            } else if (methodName.equals(UNINVITE_PERSON)) {
                String userName = (String) arguments[1];
                circabcService.removeCircabcAdmin(userName);
            }
        } else if (targetName.equals(userServiceSimpleName)) {
            if (methodName.equals("createLdapUser") || methodName.equals("createUser")) {
                NodeRef personNodeRef = (NodeRef) result;
                circabcService.addUser(personNodeRef);
            }
        } else if (targetName.equals(headersApiImplSimpleName)) {
            if (methodName.equals("postHeader")) {
                Header header = (Header) result;
                if (header != null) {
                    NodeRef headerNodeRef = getNodeRefFromID(header.getId());
                    circabcService.addHeaderNode(headerNodeRef);
                }
            } else if (methodName.equals("deleteHeader")) {
                String headerID = (String) arguments[0];
                NodeRef headerNodeRef = getNodeRefFromID(headerID);
                circabcService.deleteHeader(headerNodeRef);
            } else if (methodName.equals("putHeader")) {
                Header header = (Header) result;
                if (header != null) {
                    NodeRef headerNodeRef = getNodeRefFromID(header.getId());
                    circabcService.updateHeader(
                            headerNodeRef, header.getName(), header.getDescription().get("en"));
                }
            }
        } else if (targetName.equals(profilesApiImplSimpleName)) {
            if (methodName.equals("groupsIdProfilesPost")
                    || methodName.equals("profilesIdPut")
                    || methodName.equals("groupsIdImportedProfilesPost")) {
                io.swagger.model.Profile profile = (io.swagger.model.Profile) result;
                circabcService.postOrUpdateProfile(profile);
            }
            if (methodName.equals("profilesIdDelete")) {
                NodeRef igRef = (NodeRef) result;
                circabcService.resyncInterestGroup(igRef);
            }
        } else if (targetName.equals(groupsApiServiceSimpleName)) {
            if (methodName.equals("groupsIdPut")) {
                io.swagger.model.InterestGroup group = (io.swagger.model.InterestGroup) arguments[1];
                String id = String.valueOf(arguments[0]);
                NodeRef igNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, id);
                profileMapCache.remove(igNodeRef.toString());
                circabcService.updateIntestGroupProperties(igNodeRef);
                if (group != null) {
                    circabcService.updateInterestGroupPublic(igNodeRef, group.getIsPublic());
                    circabcService.updateInterestGroupRegistered(igNodeRef, group.getIsRegistered());
                    circabcService.updateInterestGroupApplication(igNodeRef, group.getAllowApply());
                }
            }
            if (methodName.equals("groupsIdMembersPost")) {

                NodeRef igNodeRef = (NodeRef) arguments[0];
                MembershipPostDefinition member = (MembershipPostDefinition) result;
                if (member != null) {
                    for (UserProfile uTmp : member.getMemberships()) {
                        String userName = uTmp.getUser().getUserId();
                        String profileName = uTmp.getProfile().getName();
                        if (!circabcService.isUserExists(userName)) {
                            circabcService.addUser(userName);
                        }
                        circabcService.addPersonToProfile(igNodeRef, userName, profileName);
                    }
                }
            }
            if (methodName.equals("groupsIdMembersPut")) {

                NodeRef igNodeRef = (NodeRef) arguments[0];
                MembershipPostDefinition member = (MembershipPostDefinition) result;
                if (member != null) {
                    for (UserProfile uTmp : member.getMemberships()) {
                        String userName = uTmp.getUser().getUserId();
                        String profileName = uTmp.getProfile().getName();
                        circabcService.deletePersonFromGroup(igNodeRef, userName);
                        circabcService.addPersonToProfile(igNodeRef, userName, profileName);
                    }
                }
            }
            if (methodName.equals("groupsIdMembersUserIdDelete")) {
                NodeRef igNodeRef = Converter.createNodeRefFromId((String) arguments[0]);
                String userId = (String) arguments[1];
                circabcService.deletePersonFromGroup(igNodeRef, userId);
            }
        } else if (targetName.equals(categoriesApiServiceSimpleName)) {
            if (methodName.equals("categoriesIdGroupsPost")) {

                io.swagger.model.InterestGroupPostModel groupPostModel = (io.swagger.model.InterestGroupPostModel) arguments[1];
                io.swagger.model.InterestGroup group = (io.swagger.model.InterestGroup) result;
                if (group != null) {
                    for (String leader : groupPostModel.getLeaders()) {
                        if (!circabcService.isUserExists(leader)) {
                            circabcService.addUser(leader);
                        }
                    } 
                    NodeRef igNodeRef = Converter.createNodeRefFromId(group.getId());
                    circabcService.resyncInterestGroup(igNodeRef);
                }
            }
            if (methodName.equals("selectCategoryLogoByLogoId")) {
                NodeRef categoryNodeRef = Converter.createNodeRefFromId((String) arguments[0]);
                circabcService.updateLogoRefProperty(categoryNodeRef);
            }
            if (methodName.equals("deleteCategoryLogoByLogoId")) {
                NodeRef categoryNodeRef = Converter.createNodeRefFromId((String) arguments[0]);
                circabcService.updateLogoRefProperty(categoryNodeRef);
            }
            if (methodName.equals("categoriesIdPut")) {
                NodeRef categoryNodeRef = Converter.createNodeRefFromId((String) arguments[0]);
                circabcService.updateCategoryProperties(categoryNodeRef);
            }
            if (methodName.equals("categoriesIdAdminsPost")) {
                NodeRef categoryNodeRef = Converter.createNodeRefFromId((String) arguments[0]);
                List<String> userIds = (List<String>) result;
                for (String uid : userIds) {
                    circabcService.addCategoryAdmin(categoryNodeRef, uid);
                }
            }
            if (methodName.equals("categoriesIdAdminsDelete")) {
                NodeRef categoryNodeRef = Converter.createNodeRefFromId((String) arguments[0]);
                String userId = (String) arguments[1];
                circabcService.removeCategoryAdmin(categoryNodeRef, userId);
            }
            if (methodName.equals("headersIdCategoryPost")) {
                NodeRef headerNodeRef = Converter.createNodeRefFromId((String) arguments[0]);
                Category category = (Category) result;
                if (category != null) {
                    NodeRef categoryNodeRef = Converter.createNodeRefFromId(category.getId());
                    circabcService.addCategoryNode(headerNodeRef, categoryNodeRef);
                }
            }
        } else if (targetName.equals(logoPreferencesServiceSimpleName)
                && methodName.equals("setDefault")) {
            NodeRef igNodeRef = (NodeRef) arguments[0];
            NodeRef logoNodeRef = (NodeRef) arguments[1];
            circabcService.setGroupLogoNode(igNodeRef, logoNodeRef);
        }
        return result;
    }

    private NodeRef getNodeRefFromID(String id) {
        return new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, id);
    }

    public CircabcService getCircabcService() {
        return circabcService;
    }

    public void setCircabcService(CircabcService circabcService) {
        this.circabcService = circabcService;
    }

    public void setProfileMapCache(
             SimpleCache<String, Map<String, Profile>> profileMapCache) {
        this.profileMapCache = profileMapCache;
    }
}
