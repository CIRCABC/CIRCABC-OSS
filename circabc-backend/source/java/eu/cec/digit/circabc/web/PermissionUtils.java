/*******************************************************************************
 * Copyright 2006 European Community
 *
 *  Licensed under the EUPL, Version 1.1 or - as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  You may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  https://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 ******************************************************************************/
package eu.cec.digit.circabc.web;

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.model.UserModel;
import eu.cec.digit.circabc.service.CircabcServiceRegistry;
import eu.cec.digit.circabc.service.cmr.security.CircabcConstant;
import eu.cec.digit.circabc.service.profile.*;
import eu.cec.digit.circabc.service.profile.permissions.*;
import eu.cec.digit.circabc.service.struct.ManagementService;
import eu.cec.digit.circabc.service.user.SearchResultRecord;
import eu.cec.digit.circabc.web.bean.navigation.AspectResolver;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNodeType;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.*;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.MapNode;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.SortableSelectItem;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.repo.WebResources;
import org.apache.commons.logging.Log;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.*;

/**
 * Util methods for the Web part Persmission Management
 *
 * @author Yanick Pignot
 */
public abstract class PermissionUtils extends AbstractSearchUtils {

    public static final String ALLUSERS = "allusers";
    public static final String KEY_USER_FULL_NAME = "userFullName";
    public static final String KEY_USER_NAME = "userName";
    public static final String KEY_AUTHORITY = "authority";
    public static final String KEY_AUTHORITY_TYPE = "authType";
    public static final String KEY_ICON = "icon";
    public static final String KEY_DISPLAY_NAME = "displayName";
    public static final String KEY_PERMISSION = "permission";
    public static final String KEY_USER_PROFILE = "userProfile";
    public static final String KEY_EMAIL = "email";
    public static final String AUTH_TYPE_VALUE_USER = "user";
    public static final String AUTH_TYPE_VALUE_GROUP = "group";
    public static final String AUTH_TYPE_VALUE_UNKNOW = "unknow";
    public static final String FILTER_VALUE_PROFILES = "access profile";
    public static final String FILTER_VALUE_APPLICANT = "applicants";
    public static final String CIRCABC_CACHE_NAME = "eu.cec.digit.circabc.service.dynamic.authority.circabcDynamicAuthority";
    private static final String BLANK_DELIM = " ";
    private static final String MSG_ALL_USER = "search_user_all_user";
    private static final String VALUE_PERM_PREFIX = "permission_value_";
    private static final String TOOLTIP_PERM_PREFIX = "permission_tooltip_";
    private static final String NOTIFICATION_STATUS = "NotificationStatus";
    private static final List<String> PROFILES_LIST_TO_FILTER = new ArrayList<>(3);

    static {
        // fill the list of profiles to filter
        PROFILES_LIST_TO_FILTER.add(
                getProfileManagerServiceFactory().getCircabcRootProfileManagerService()
                        .getAllCircaUsersGroupName());
        PROFILES_LIST_TO_FILTER.add(CircabcConstant.GUEST_AUTHORITY);
    }


    public static Integer getApplicantFilterIndex() {
        return getUserService().getEcasUserDomains().size() + 1;
    }

    public static List<SelectItem> getPermissions(final Node actionNode, final Log logger) {
        if (actionNode.hasAspect(CircabcModel.ASPECT_LIBRARY)) {
            final List<String> libPermissions = LibraryPermissions.getOrderedLibraryPermissions();
            final List<SelectItem> itemsAsList = new ArrayList<>(libPermissions.size());

            for (final String libPerm : libPermissions) {
                /* No sens to keep LIB no access. To have this feature, only cut inheritance and don't invite the user */
                if (!libPerm.equals(LibraryPermissions.LIBNOACCESS.toString())) {
                    itemsAsList.add(
                            new SelectItem(libPerm, getPermissionLabel(libPerm))
                    );
                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug(
                        itemsAsList.size() + " Permission(s) found for the node " + actionNode.getName()
                                + " having aspect " + CircabcModel.ASPECT_LIBRARY + " . Permissions: "
                                + itemsAsList);
            }

            return itemsAsList;
        } else if (actionNode.hasAspect(CircabcModel.ASPECT_NEWSGROUP)) {
            final HashSet<NewsGroupPermissions> permissions = NewsGroupPermissions.getPermissions();
            final List<SelectItem> itemsAsList = new ArrayList<>(permissions.size());

            for (final NewsGroupPermissions perm : permissions) {
                /* No sens to keep LIB no access. To have this feature, only cut inheritance and don't invite the user */
                if (!perm.equals(NewsGroupPermissions.NWSNOACCESS)) {
                    itemsAsList.add(
                            new SelectItem(perm.toString(), getPermissionLabel(perm.toString()))
                    );
                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug(
                        itemsAsList.size() + " Permission(s) found for the node " + actionNode.getName()
                                + " having aspect " + CircabcModel.ASPECT_NEWSGROUP + " . Permissions: "
                                + itemsAsList);
            }

            return itemsAsList;
        } else if (actionNode.hasAspect(CircabcModel.ASPECT_SURVEY)) {
            final HashSet<SurveyPermissions> permissions = SurveyPermissions.getPermissions();
            final List<SelectItem> itemsAsList = new ArrayList<>(permissions.size());

            for (final SurveyPermissions perm : permissions) {
                /* No sens to keep LIB no access. To have this feature, only cut inheritance and don't invite the user */
                if (!perm.equals(SurveyPermissions.SURNOACCESS)) {
                    itemsAsList.add(
                            new SelectItem(perm.toString(), getPermissionLabel(perm.toString()))
                    );
                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug(
                        itemsAsList.size() + " Permission(s) found for the node " + actionNode.getName()
                                + " having aspect " + CircabcModel.ASPECT_SURVEY + " . Permissions: "
                                + itemsAsList);
            }

            return itemsAsList;
        } else if (actionNode.hasAspect(CircabcModel.ASPECT_INFORMATION)) {
            final HashSet<InformationPermissions> permissions = InformationPermissions.getPermissions();
            final List<SelectItem> itemsAsList = new ArrayList<>(permissions.size());

            for (final InformationPermissions perm : permissions) {
                /* No sens to keep INF no access. To have this feature, only cut inheritance and don't invite the user */
                if (!perm.equals(InformationPermissions.INFNOACCESS)) {
                    itemsAsList.add(
                            new SelectItem(perm.toString(), getPermissionLabel(perm.toString()))
                    );
                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug(
                        itemsAsList.size() + " Permission(s) found for the node " + actionNode.getName()
                                + " having aspect " + CircabcModel.ASPECT_INFORMATION + " . Permissions: "
                                + itemsAsList);
            }

            return itemsAsList;
        } else if (actionNode.hasAspect(CircabcModel.ASPECT_EVENT)) {
            final HashSet<EventPermissions> permissions = EventPermissions.getPermissions();
            final List<SelectItem> itemsAsList = new ArrayList<>(permissions.size());

            for (final EventPermissions perm : permissions) {
                /* No sens to keep EVENT no access. To have this feature, only cut inheritance and don't invite the user */
                if (!perm.equals(EventPermissions.EVENOACCESS)) {
                    itemsAsList.add(
                            new SelectItem(perm.toString(), getPermissionLabel(perm.toString()))
                    );
                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug(
                        itemsAsList.size() + " Permission(s) found for the node " + actionNode.getName()
                                + " having aspect " + CircabcModel.ASPECT_EVENT + " . Permissions: " + itemsAsList);
            }

            return itemsAsList;
        } else {
            logger.error(
                    "The list of permissions can't be returned beacause no logic found for the the node "
                            + actionNode + ". The node aspects are not taken in account yet.");

            return Collections.emptyList();
        }
    }

    public static void resetCache(SimpleCache<Serializable, Object> cache, final Log logger,
                                  final String name) {
        if (cache != null) {
            for (Serializable key : cache.getKeys()) {
                cache.remove(key);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Cache " + name + " successfuly reseted");
            }
        } else {
            logger.error("Cache " + name + " not found !!!");
        }
    }

    public static List<Map> getInterestGroupAuthorities(final NodeRef anyNode) {
        return getInterestGroupAuthorities(anyNode, null);
    }

    public static List<Map> getInterestGroupAuthorities(final NodeRef anyNode,
                                                        final List<String> withPermission) {
        final FacesContext context = FacesContext.getCurrentInstance();

        final NodeRef igRoot = getManagementService().getCurrentInterestGroup(anyNode);
        final ProfileManagerService igProfileManagerService = getProfileManagerServiceFactory()
                .getIGRootProfileManagerService();
        final ProfileManagerService catProfileManagerService = getProfileManagerServiceFactory()
                .getCategoryProfileManagerService();
        final ProfileManagerService rootProfileManagerService = getProfileManagerServiceFactory()
                .getCircabcRootProfileManagerService();

        final RetryingTransactionHelper txnHelper = Repository.getRetryingTransactionHelper(context);
        final RetryingTransactionCallback<List<Map>> callback = new RetryingTransactionCallback<List<Map>>() {
            public List<Map> execute() throws Throwable {
                List<Map> personNodes = null;

                try {
                    // Return all the permissions set against the current node for any authentication instance (user/group).
                    // Then combine them into a single list for each authentication found.
                    final Map<String, List<String>> permissionMap = new HashMap<>(8, 1.0f);
                    final Map<String, Boolean> inheritanceMap = new HashMap<>();
                    final Set<AccessPermission> permissions = getPermissionService()
                            .getAllSetPermissions(anyNode);

                    for (final AccessPermission permission : permissions) {

                        // we are only interested in Allow and not groups/owner etc.
                        if ((withPermission == null || withPermission.contains(permission.getPermission()))
                                && permission.getAccessStatus() == AccessStatus.ALLOWED
                                && (!permission.getPermission().equals(NOTIFICATION_STATUS))
                                && (permission.getAuthorityType() == AuthorityType.USER
                                || permission.getAuthorityType() == AuthorityType.GROUP
                                || permission.getAuthorityType() == AuthorityType.GUEST
                                || permission.getAuthorityType() == AuthorityType.EVERYONE)) {
                            final String authority = permission.getAuthority();

                            List<String> userPermissions = permissionMap.get(authority);
                            if (userPermissions == null) {
                                // create for first time
                                userPermissions = new ArrayList<>(4);
                                permissionMap.put(authority, userPermissions);
                            }
                            // add the permission name for this authority
                            userPermissions.add(permission.getPermission());
                            inheritanceMap.put(authority, permission.isInherited());
                        }
                    }

                    // for each authentication (username/group key) found we get the Person
                    // node represented by it and use that for our list databinding object
                    personNodes = new ArrayList<>(permissionMap.size());

                    for (final Map.Entry<String, List<String>> entry : permissionMap.entrySet()) {
                        if (entry.getKey().trim().length() < 1) {

                        }
                        // check if we are dealing with a person (User Authority)
                        else if (AuthorityType.getAuthorityType(entry.getKey()) == AuthorityType.GUEST
                                || getPersonService().personExists(entry.getKey())) {
                            final NodeRef nodeRef = getPersonService().getPerson(entry.getKey());
                            if (nodeRef != null) {
                                // create our Node representation
                                final MapNode node = new MapNode(nodeRef);

                                // set data binding properties this will also force initialisation of the props now during the transaction
                                // it is much better for performance to do this now rather than during page bind
                                final Map<String, Object> props = node.getProperties();

                                node.put(KEY_USER_FULL_NAME,
                                        ((String) props.get("firstName")) + ' ' + ((String) props.get("lastName")));
                                node.put(KEY_AUTHORITY, entry.getKey());
                                node.put(KEY_DISPLAY_NAME, computeUserLogin(props));
                                node.put(KEY_PERMISSION, permissionsToString(entry.getValue()));
                                node.put(KEY_ICON, WebResources.IMAGE_PERSON);
                                node.put(KEY_AUTHORITY_TYPE, AUTH_TYPE_VALUE_USER);
                                node.put("inheritedPermission", inheritanceMap.get(entry.getKey()));

                                personNodes.add(node);
                            }
                        } else {
                            // need a map (dummy node) to represent props for
                            // this Group Authority
                            final Map<String, Object> node = new HashMap<>(5, 1.0f);
                            boolean ignoreRow = false;
                            if (entry.getKey().startsWith(PermissionService.GROUP_PREFIX) == true) {
                                Profile profileFromGroup = igProfileManagerService
                                        .getProfileFromGroup(igRoot, entry.getKey());

                                if (profileFromGroup == null) {
                                    // perhaps the Cat Admin
                                    profileFromGroup = catProfileManagerService
                                            .getProfileFromGroup(getManagementService().getCurrentCategory(igRoot),
                                                    entry.getKey());
                                }

                                if (profileFromGroup == null) {
                                    // perhaps the CircabcAdmin Admin
                                    profileFromGroup = rootProfileManagerService
                                            .getProfileFromGroup(getManagementService().getCircabcNodeRef(),
                                                    entry.getKey());
                                }

                                ignoreRow = (profileFromGroup == null);
                                node.put(KEY_DISPLAY_NAME,
                                        ignoreRow ? "" : profileFromGroup.getProfileDisplayName());
                                node.put(KEY_AUTHORITY_TYPE, AUTH_TYPE_VALUE_GROUP);
                            } else {
                                node.put(KEY_DISPLAY_NAME, entry.getKey());
                                node.put(KEY_AUTHORITY_TYPE, AUTH_TYPE_VALUE_UNKNOW);
                            }

                            node.put(KEY_USER_FULL_NAME, "");
                            node.put(KEY_PERMISSION, permissionsToString(entry.getValue()));
                            node.put(KEY_ICON, WebResources.IMAGE_GROUP);
                            node.put(KEY_AUTHORITY, entry.getKey());
                            node.put("inheritedPermission", inheritanceMap.get(entry.getKey()));
                            if (!ignoreRow) {
                                personNodes.add(node);
                            }
                        }
                    }
                } catch (InvalidNodeRefException refErr) {

                    Utils.addErrorMessage(
                            MessageFormat.format(Application.getMessage(context, Repository.ERROR_NODEREF),
                                    refErr.getNodeRef()));
                    personNodes = Collections.emptyList();
                }

                return personNodes;
            }
        };

        return txnHelper.doInTransaction(callback, true);
    }

    public static SelectItem[] getDomainFilters(final boolean withFilter, final boolean accessProfile,
                                                final boolean applicant) {
        final List<SelectItem> filters = new ArrayList<>(11);

        final String allUsersMessage = Application
                .getMessage(FacesContext.getCurrentInstance(), MSG_ALL_USER);

        if (!withFilter) {
            filters.add(new SelectItem(PermissionUtils.ALLUSERS, allUsersMessage));

            for (final Map.Entry<String, String> item : getUserService().getEcasUserDomains()
                    .entrySet()) {
                filters.add(new SelectItem(item.getKey(), item.getValue()));
            }
        } else {
            int index = -1;

            filters.add(new SelectItem("" + ++index, allUsersMessage, PermissionUtils.ALLUSERS));
            for (final Map.Entry<String, String> item : getUserService().getEcasUserDomains()
                    .entrySet()) {
                filters.add(new SelectItem("" + ++index, item.getValue(), item.getKey()));
            }

            if (accessProfile) {
                filters.add(new SelectItem("" + ++index, "Access Profile", FILTER_VALUE_PROFILES));
            }
            if (applicant) {
                filters.add(new SelectItem("" + ++index, "Applicants", FILTER_VALUE_APPLICANT));
            }

        }

        return filters.toArray(new SelectItem[filters.size()]);
    }

    /**
     * @return true if the current user can apply to become a member of the current node
     */
    public static boolean isUserCanJoinNode(final String currentUser, final NodeRef nodeRef,
                                            final NodeService nodeService, final PermissionService permissionService) {
        final FacesContext fc = FacesContext.getCurrentInstance();
        final CircabcServiceRegistry services = Services.getCircabcServiceRegistry(fc);
        final ProfileManagerService profileManagerService = services.getProfileManagerServiceFactory()
                .getProfileManagerService(nodeRef);

        if (profileManagerService == null) {
            return false;
        }

        // if the user is already invited, he can't apply for membership
        boolean canJoin = !profileManagerService.getInvitedUsers(nodeRef).contains(currentUser);

        // the user is not yet invited, he only can join the current node if it is a IGRoot ...
        if (canJoin && nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_IGROOT)) {
            final AccessStatus accesStatus = permissionService
                    .hasPermission(nodeRef, JoinPermissions.JOIN.toString());

            // ...  AND if its access status == ALLOWED
            canJoin = (accesStatus == AccessStatus.ALLOWED);

            if (canJoin) {
                // test if the user isn't the Category Admin. The cat admin is not in the list of invited
                // user of the ig root but can't be invited
                final NodeRef categoryNodeRef = nodeService.getPrimaryParent(nodeRef).getParentRef();
                final ProfileManagerServiceFactory profileManagerServiceFactory = services
                        .getProfileManagerServiceFactory();
                final ProfileManagerService categoryProfileManagerService = profileManagerServiceFactory
                        .getProfileManagerService(categoryNodeRef);
                canJoin = !categoryProfileManagerService.getInvitedUsers(categoryNodeRef)
                        .contains(currentUser);
            }
        }
        return canJoin;
    }

    /**
     * Build a Jsf select item of all users available for an invitation
     */
    public static List<SortableSelectItem> buildInvitableUserItems(final String domain,
                                                                   final String match, final Log logger) {
        return buildInvitableUserItems(domain, match, null, logger);
    }

    /**
     * Build a Jsf select item of all users available for an invitation according given filters.
     *
     * @param match                   filter of the profile name
     * @param alreadyInvitedAuthority filter of some profile authority to remove
     */
    public static List<SortableSelectItem> buildInvitableUserItems(final String domain,
                                                                   final String match, final List<String> alreadyInvitedAuthority, final Log logger) {
        return buildInvitedUserItems(getAllAvailableUsers(domain, match, logger), MAX_ELEMENTS_IN_LIST,
                null, alreadyInvitedAuthority, logger);
    }

    /**
     * Build a Jsf select item of all users invited user in the given node
     */
    public static List<SortableSelectItem> buildInvitedUserItems(final Node node, final Log logger,
                                                                 final boolean onlineUsers) {
        return buildInvitedUserItems(node, null, onlineUsers, null, logger);
    }

    /**
     * Build a Jsf select item of all users invited user in the given node according given filters.
     *
     * @param match                   filter of the profile name
     * @param alreadyInvitedAuthority filter of some profile authority to remove
     */
    public static List<SortableSelectItem> buildCircabcUserItems(final Node node, final String match,
                                                                 final boolean onlineUsers, final List<String> alreadyInvitedAuthority, final Log logger) {
        return buildInvitedUserItems(getCircabcUsers(node, logger, onlineUsers, true),
                MAX_ELEMENTS_IN_LIST, match, alreadyInvitedAuthority, logger);
    }

    /**
     * Build a Jsf select item of all users invited user in the given node according given filters.
     *
     * @param match                   filter of the profile name
     * @param alreadyInvitedAuthority filter of some profile authority to remove
     */
    public static List<SortableSelectItem> buildCategoryUserItems(final Node node, final String match,
                                                                  final boolean onlineUsers, final List<String> alreadyInvitedAuthority, final Log logger) {
        return buildInvitedUserItems(getCategoryUsers(node, logger, onlineUsers, true),
                MAX_ELEMENTS_IN_LIST, match, alreadyInvitedAuthority, logger);
    }

    /**
     * Build a Jsf select item of all users invited user in the given node according given filters.
     *
     * @param match                   filter of the profile name
     * @param alreadyInvitedAuthority filter of some profile authority to remove
     */
    public static List<SortableSelectItem> buildInvitedUserItems(final Node node, final String match,
                                                                 final boolean onlineUsers, final List<String> alreadyInvitedAuthority, final Log logger) {
        return buildInvitedUserItems(getInvitedUsers(node, logger, onlineUsers, false),
                MAX_ELEMENTS_IN_LIST, match, alreadyInvitedAuthority, logger);
    }

    /**
     * Build a Jsf select item of all users invited user in the given node and its children according
     * given filters.
     *
     * @param match                   filter of the profile name
     * @param alreadyInvitedAuthority filter of some profile authority to remove
     */
    public static List<SortableSelectItem> buildInvitedUserItemsRecursivly(final Node node,
                                                                           final String match, final boolean onlineUsers, final List<String> alreadyInvitedAuthority,
                                                                           final Log logger) {
        return buildInvitedUserItems(getInvitedUsers(node, logger, onlineUsers, true),
                MAX_ELEMENTS_IN_LIST, match, alreadyInvitedAuthority, logger);
    }

    public static String getPermissionLabel(final String permission) {
        return Application.getMessage(FacesContext.getCurrentInstance(),
                VALUE_PERM_PREFIX + permission.toLowerCase());
    }

    public static String getPermissionTooltip(final String permission) {
        return Application.getMessage(FacesContext.getCurrentInstance(),
                TOOLTIP_PERM_PREFIX + permission.toLowerCase());
    }

    //-- Private helpers

    private static List<SortableSelectItem> buildInvitedUserItems(
            final Collection<SearchResultRecord> records, final int maxUserToReturn, final String match,
            final List<String> alreadyInvitedAuthority, final Log logger) {
        long startTime = 0;

        if (logger.isDebugEnabled()) {
            startTime = System.currentTimeMillis();
            logger.debug("Start to build the the user list items...");
        }

        final boolean needNameFilter;

        if (match == null || match.matches(STAR_WILDCARD_REGEX)) {
            needNameFilter = false;

            if (logger.isDebugEnabled()) {
                logger.debug("  --  No name filter activated");
            }
        } else {
            needNameFilter = true;

            if (logger.isDebugEnabled()) {
                logger.debug("  --  Name filter activated. Must match " + match);
            }
        }

        final Map<String, SortableSelectItem> items = new HashMap<>(records.size());

        String userText;
        String userId;
        for (final SearchResultRecord record : records) {
            userText = record.toString();
            userId = record.getId();

            if (needNameFilter && !userText.toLowerCase().contains(match.toLowerCase()) && !userId
                    .equals(match)) {
                // the search text is not contained in the users name or email, remove it from the list
                if (logger.isDebugEnabled()) {
                    logger.debug("  --  Profile " + userId
                            + " removed from the list because it doesn't matches filter. Search performed on text: "
                            + userText);
                }
            } else if (items.containsKey(userId)) {
                // the user has already been added to the list
                if (logger.isDebugEnabled()) {
                    logger.debug("  --  User " + userId + " already found in another location.");
                }
            } else {
                boolean disableItem = false;
                if (alreadyInvitedAuthority != null && alreadyInvitedAuthority.contains(userId)) {
                    // the user has already been invited
                    if (logger.isDebugEnabled()) {
                        logger.debug("  --  User " + userId + " mark in in the list.");
                    }
                    userText = userText + " - Already Invited";
                    disableItem = true;
                }

                SortableSelectItem newItem = new SortableSelectItem(record.getId(), userText, userText);
                newItem.setDisabled(disableItem);
                items.put(record.getId(), newItem);

                if (logger.isDebugEnabled()) {
                    logger.debug("  --  User " + userId + " successfully added to the list.");
                }
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug(
                    items.size() + " Users added in " + (System.currentTimeMillis() - startTime) + "ms");
        }

        final List<SortableSelectItem> listItems = new ArrayList<>(items.size());
        listItems.addAll(items.values());
        return listItems;
    }

    private static Collection<SearchResultRecord> getAllAvailableUsers(final String domain,
                                                                       final String criteria, final Log logger) {
        long startTime = 0;

        if (logger.isDebugEnabled()) {
            startTime = System.currentTimeMillis();
            logger.debug("Start to search all available users for an invitation ...");
        }

        List<SearchResultRecord> users;
        if (!criteria.contains(BLANK_DELIM)) {
            if (criteria.indexOf('@') == -1) {
                users = getUserService().getUsersByDomainFirstNameLastNameEmail(domain, criteria, true);
            } else {
                users = getUserService().getUsersByMailDomain(criteria, domain, true);
            }
        } else {
            users = getUserService().getUsersByDomainFirstNameLastNameEmail(domain, criteria, true);
        }

        if (logger.isDebugEnabled()) {
            logger.debug(
                    users.size() + " Users found in " + (System.currentTimeMillis() - startTime) + "ms");
        }

        return users;
    }

    private static Collection<SearchResultRecord> getCircabcUsers(final Node where, final Log logger,
                                                                  final boolean onlineUsers, final boolean recursive) {
        long startTime = 0;

        if (logger.isDebugEnabled()) {
            startTime = System.currentTimeMillis();
            logger.debug("Start to search all invited users users in " + where + " ...");
        }

        final NavigableNodeType type = AspectResolver.resolveType(where);
        if (type == null) {
            throw new IllegalStateException(
                    "Impossible to perform a Circabc invitation in an non Circabc node.");
        }

        if (!NavigableNodeType.CIRCABC_ROOT.equals(type)) {
            throw new IllegalStateException("The node is not a Circabc (root).");
        }

        final ProfileManagerService profileManager = getProfileManagerServiceFactory()
                .getCircabcRootProfileManagerService();

        NodeRef rootNode;

        if (type.isStrictlyUnder(NavigableNodeType.CIRCABC_ROOT)) {
            rootNode = profileManager.getCircaHome(where.getNodeRef());
        } else {
            rootNode = where.getNodeRef();
        }

        final Set<String> invitedUsers = profileManager.getInvitedUsers(rootNode);
        logger.debug("Found " + invitedUsers.size() + " invited users.");

        if (recursive) {
            final Set<String> subUsers = profileManager.getAllSubUsers(rootNode);
            invitedUsers.addAll(subUsers);
            logger.debug("Found " + subUsers.size() + " sub users.");
        }
        final List<SearchResultRecord> userRecords = buildUsersList(invitedUsers, onlineUsers);

        if (logger.isDebugEnabled()) {
            logger.debug(
                    userRecords.size() + " Users found in " + (System.currentTimeMillis() - startTime)
                            + "ms");
        }

        return userRecords;
    }

    private static Collection<SearchResultRecord> getCategoryUsers(final Node where, final Log logger,
                                                                   final boolean onlineUsers, final boolean recursive) {
        long startTime = 0;

        if (logger.isDebugEnabled()) {
            startTime = System.currentTimeMillis();
            logger.debug("Start to search all invited users users in " + where + " ...");
        }

        final NavigableNodeType type = AspectResolver.resolveType(where);
        if (type == null) {
            throw new IllegalStateException(
                    "Impossible to perform a Circabc invitation in an non Circabc node.");
        }

        if (!NavigableNodeType.CATEGORY.equals(type)) {
            throw new IllegalStateException("The node is not a Category.");
        }

        final ProfileManagerService profileManager = getProfileManagerServiceFactory()
                .getCategoryProfileManagerService();

        NodeRef rootNode;

        if (type.isStrictlyUnder(NavigableNodeType.CATEGORY)) {
            rootNode = profileManager.getCircaHome(where.getNodeRef());
        } else {
            rootNode = where.getNodeRef();
        }

        final Set<String> invitedUsers = profileManager.getInvitedUsers(rootNode);

        if (recursive) {
            invitedUsers.addAll(profileManager.getAllSubUsers(rootNode));
        }
        final List<SearchResultRecord> userRecords = buildUsersList(invitedUsers, onlineUsers);

        if (logger.isDebugEnabled()) {
            logger.debug(
                    userRecords.size() + " Users found in " + (System.currentTimeMillis() - startTime)
                            + "ms");
        }

        return userRecords;
    }

    private static Collection<SearchResultRecord> getInvitedUsers(final Node where, final Log logger,
                                                                  final boolean onlineUsers, final boolean recursive) {
        long startTime = 0;

        if (logger.isDebugEnabled()) {
            startTime = System.currentTimeMillis();
            logger.debug("Start to search all invited users users in " + where + " ...");
        }

        final ManagementService managementService = getManagementService();

        final NavigableNodeType type = AspectResolver.resolveType(where);
        final NodeRef rootNode;
        if (type == null) {
            throw new IllegalStateException(
                    "Impossible to perform a Circabc invitation in an non Circabc node.");
        }

        if (type.isStrictlyUnder(NavigableNodeType.IG_ROOT)) {
            rootNode = managementService.getCurrentInterestGroup(where.getNodeRef());
        } else {
            rootNode = where.getNodeRef();
        }

        final ProfileManagerService profileManager = getProfileManagerServiceFactory()
                .getProfileManagerService(rootNode);

        if (rootNode == null) {
            throw new IllegalStateException("The node received has not Interest Group");
        }

        if (logger.isDebugEnabled() && rootNode.equals(where.getNodeRef()) == false) {
            logger.debug("  --  The Interest Group of " + where + " successfully found: " + rootNode);
        }

        final Set<String> users = profileManager.getInvitedUsers(rootNode);

        final List<SearchResultRecord> userRecords = buildUsersList(users, onlineUsers);

        if (recursive) {

            if (NavigableNodeType.CIRCABC_ROOT.equals(type)) {
                final CircabcRootProfileManagerService circabcProfileManager = (CircabcRootProfileManagerService) profileManager;
                circabcProfileManager.getMasterUsers(rootNode);
                for (final NodeRef ref : managementService.getCategories()) {
                    userRecords.addAll(getInvitedUsers(new Node(ref), logger, onlineUsers, recursive));
                }
            } else if (NavigableNodeType.CATEGORY.equals(type)) {
                //Get the list of circabcAdmin's
                final NodeRef circabcNodeRef = managementService.getCircabcNodeRef();
                final ProfileManagerService profileManagerService = getProfileManagerServiceFactory()
                        .getProfileManagerService(circabcNodeRef);
                final Set<String> persons = profileManagerService.getPersonInProfile(circabcNodeRef,
                        CircabcRootProfileManagerService.Profiles.CIRCABC_ADMIN);
                userRecords.addAll(buildUsersList(persons, onlineUsers));

                //Get list of Admin on the IG
                for (final NodeRef ref : managementService.getInterestGroups(rootNode)) {
                    userRecords.addAll(getInvitedUsers(new Node(ref), logger, onlineUsers, recursive));
                }
            } else if (NavigableNodeType.IG_ROOT.equals(type)) {
                //Get the list of Admin on the Category
                final NodeRef categoryNodeRef = managementService.getCurrentCategory(where.getNodeRef());
                final ProfileManagerService profileManagerService = getProfileManagerServiceFactory()
                        .getProfileManagerService(categoryNodeRef);
                final Set<String> persons = profileManagerService.getPersonInProfile(categoryNodeRef,
                        CategoryProfileManagerService.Profiles.CIRCA_CATEGORY_ADMIN);
                userRecords.addAll(buildUsersList(persons, onlineUsers));
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug(
                    userRecords.size() + " Users found in " + (System.currentTimeMillis() - startTime)
                            + "ms");
        }

        return userRecords;
    }

    private static List<SearchResultRecord> buildUsersList(final Set<String> users,
                                                           final boolean onlineUsers) {
        final List<SearchResultRecord> userRecords = new ArrayList<>(users.size());

        NodeRef person;
        Map<QName, Serializable> properties;

        Set<String> allOnlineUsers = new HashSet<>();
        if (onlineUsers == true) {
            allOnlineUsers = getUserService().getAllOnlineUsers();
        }

        for (final String userId : users) {
            if (onlineUsers == true && !allOnlineUsers.contains(userId)) {
                continue;
            }
            person = getPersonService().getPerson(userId);
            properties = getNodeService().getProperties(person);
            userRecords.add(new SearchResultRecord(
                    userId,
                    (String) properties.get(ContentModel.PROP_FIRSTNAME),
                    (String) properties.get(ContentModel.PROP_LASTNAME),
                    (String) properties.get(ContentModel.PROP_EMAIL)
            ));
        }

        return userRecords;
    }


    private static String permissionsToString(final List<String> permissions) {
        final StringBuilder buff = new StringBuilder();

        if (permissions != null) {
            for (final String perm : permissions) {
                if (NOTIFICATION_STATUS.equals(perm) == false) {
                    if (buff.length() != 0) {
                        buff.append(", ");
                    }
                    buff.append(perm);
                }
            }
        }

        return buff.toString();
    }

    public static String computeUserLogin(final String username) {
        final FacesContext fc = FacesContext.getCurrentInstance();
        final ServiceRegistry registry = Services.getAlfrescoServiceRegistry(fc);

        return computeUserLogin(username,
                registry.getPersonService(),
                registry.getNodeService());
    }

    public static String computeUserLogin(final String username, final PersonService personService,
                                          final NodeService nodeService) {
        return computeUserLogin(
                personService.getPerson(username),
                nodeService);
    }

    public static String computeUserLogin(final NodeRef userRef, final NodeService nodeService) {
        return computeUserLogin(nodeService.getProperties(userRef));
    }

    public static String computeUserLogin(final Map<?, ?> properties) {
        String displayName = (String) properties.get(UserModel.PROP_ECAS_USER_NAME);
        if (displayName == null) {
            displayName = (String) properties.get(ContentModel.PROP_USERNAME);
        }
        return displayName;
    }


}
