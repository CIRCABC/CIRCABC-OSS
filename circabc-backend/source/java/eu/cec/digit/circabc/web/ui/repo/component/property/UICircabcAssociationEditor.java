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
package eu.cec.digit.circabc.web.ui.repo.component.property;

import eu.cec.digit.circabc.service.profile.IGRootProfileManagerService;
import eu.cec.digit.circabc.service.profile.Profile;
import eu.cec.digit.circabc.service.profile.ProfileManagerServiceFactory;
import eu.cec.digit.circabc.web.Beans;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNode;
import eu.cec.digit.circabc.web.bean.override.CircabcNavigationBean;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authority.AuthorityDAO;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.repository.User;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.repo.component.property.UIAssociationEditor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.jsf.FacesContextUtils;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.transaction.UserTransaction;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;

public class UICircabcAssociationEditor extends UIAssociationEditor {

    private static final Log logger = LogFactory
            .getLog(UICircabcAssociationEditor.class);
    private final static String FIELD_AVAILABLE = "_available";
    private static final String MSG_WARN_CANNOT_VIEW = "warn_cannot_view_target_details";
    private static final String MSG_REMOVE = "remove";
    private static final int ACTION_REMOVE = 0;
    private static final String ACTION_SEPARATOR = ";";
    private static final String MSG_CHANGE = "change";
    private static final int ACTION_CHANGE = 3;
    private NodeRef igNodeRef;
    private Map<NodeRef, String> allProfiles;
    private Map<NodeRef, String> allUsers;
    private IGRootProfileManagerService igRootProfileManagerService;

    public String getFamily() {
        return "eu.cec.digit.circabc.faces.CircabcAssociationEditor";
    }

    /**
     * @see javax.faces.component.StateHolder#restoreState(javax.faces.context.FacesContext,
     * java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    public void restoreState(FacesContext context, Object state) {
        Object values[] = (Object[]) state;
        // standard component attributes are restored by the super class
        super.restoreState(context, values[0]);
        this.igNodeRef = (NodeRef) values[1];
        this.allProfiles = (Map<NodeRef, String>) values[2];
        this.allUsers = (Map<NodeRef, String>) values[3];

    }

    /**
     * @see javax.faces.component.StateHolder#saveState(javax.faces.context.FacesContext)
     */
    public Object saveState(FacesContext context) {
        Object values[] = new Object[4];
        // standard component attributes are saved by the super class
        values[0] = super.saveState(context);
        values[1] = this.igNodeRef;
        values[2] = this.allProfiles;
        values[3] = this.allUsers;

        return (values);
    }

    @Override
    protected void getAvailableOptions(FacesContext context, String contains) {

        if (getIgNodeRef() == null) {
            super.getAvailableOptions(context, contains);
        } else {
            customGetAvailableOptions(context, contains);
        }

    }

    private IGRootProfileManagerService getProfileService() {
        if (igRootProfileManagerService == null) {
            final ProfileManagerServiceFactory factory = Services
                    .getCircabcServiceRegistry(
                            FacesContext.getCurrentInstance())
                    .getProfileManagerServiceFactory();
            igRootProfileManagerService = factory
                    .getIGRootProfileManagerService();
        }
        return igRootProfileManagerService;
    }

    private NodeRef getIgNodeRef() {
        if (igNodeRef == null) {
            CircabcNavigationBean navigator = Beans.getWaiNavigator();
            if (navigator != null) {
                NavigableNode currentIGRoot = navigator.getCurrentIGRoot();
                if (currentIGRoot != null) {
                    igNodeRef = currentIGRoot.getNodeRef();
                }
            }
        }
        return igNodeRef;
    }

    public void setIgNodeRef(NodeRef value) {
        igNodeRef = value;
    }

    private void customGetAvailableOptions(FacesContext context, String contains) {

        AssociationDefinition assocDef = getAssociationDefinition(context);
        if (assocDef != null) {
            // find and show all the available options for the current
            // association
            String type = assocDef.getTargetClass().getName().toString();

            if (type.equals(ContentModel.TYPE_AUTHORITY_CONTAINER.toString())) {
                getAvailableProfileOptions(context, contains);
            } else {
                getAvailableUserOptions(context, contains, assocDef, type);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Found " + this.availableOptions.size()
                        + " available options");
            }
        }

    }

    private void getAvailableUserOptions(FacesContext context, String contains,
                                         AssociationDefinition assocDef, String type) {

        if (!type.equals(ContentModel.TYPE_PERSON.toString())) {
            if (logger.isErrorEnabled()) {
                logger.error("Invalid type expected : " + type);
            }
            this.availableOptions = Collections.emptyList();
        }

        String safeContains = null;
        if (contains != null && contains.length() > 0) {
            safeContains = Utils.remove(contains.trim(), "\"");
            safeContains = safeContains.toLowerCase();
        }
        this.availableOptions = new ArrayList<>();

        for (Map.Entry<NodeRef, String> entry : this.getAllUsers(context)
                .entrySet()) {

            if (safeContains != null) {
                if (entry.getValue().toLowerCase().contains(safeContains)) {

                    availableOptions.add(entry.getKey());
                }
            } else {
                availableOptions.add(entry.getKey());

            }

        }

    }

    private void getAvailableProfileOptions(FacesContext context,
                                            String contains) {

        String safeContains = null;
        if (contains != null && contains.length() > 0) {
            safeContains = Utils.remove(contains.trim(), "\"");
            safeContains = safeContains.toLowerCase();
        }

        List<Profile> profilesList = getProfileService()
                .getProfiles(getIgNodeRef());

        this.availableOptions = new ArrayList<>(profilesList.size());
        for (Map.Entry<NodeRef, String> entry : this.getAllProfiles(context)
                .entrySet()) {

            if (safeContains != null) {
                if (entry.getValue().toLowerCase().contains(safeContains)) {

                    availableOptions.add(entry.getKey());
                }
            } else {
                availableOptions.add(entry.getKey());

            }
        }

    }

    protected void renderAvailableOptions(FacesContext context,
                                          ResponseWriter out, NodeService nodeService, String targetType,
                                          boolean allowMany) throws IOException {

        if (getIgNodeRef() == null) {
            super.renderAvailableOptions(context, out, nodeService, targetType,
                    allowMany);

        } else {

            customRenderAvailableOptions(context, out, nodeService, targetType,
                    allowMany);

        }

    }

    private void customRenderAvailableOptions(FacesContext context,
                                              ResponseWriter out, NodeService nodeService, String targetType,
                                              boolean allowMany) throws IOException {

        boolean itemsPresent = (this.availableOptions != null && this.availableOptions
                .size() > 0);

        out.write("<tr><td colspan='2'><select ");
        if (!itemsPresent) {
            // rather than having a very slim select box set the width if there
            // are no results
            out.write("style='width:240px;' ");
        }
        out.write("name='");
        out.write(getClientId(context) + FIELD_AVAILABLE);
        out.write("' size='");
        out.write(getAvailableOptionsSize());
        out.write("'");
        if (allowMany) {
            out.write(" multiple");
        }
        out.write(">");

        if (itemsPresent) {
            Node currentNode = (Node) getValue();

            for (NodeRef item : this.availableOptions) {
                // show all the available options apart from the current node as
                // we don't
                // want to create recursive associations!!
                if (!item.toString().equals(currentNode.getNodeRef().toString())) {

                    if (ContentModel.TYPE_PERSON.equals(nodeService
                            .getType(item))) {
                        String fullNameAndUserId = getAllUsers(context).get(
                                item);
                        out.write("<option value='");
                        out.write(item.toString());
                        out.write("'>");
                        out.write(Utils.encode(fullNameAndUserId));
                        out.write("</option>");

                    } else if (ContentModel.TYPE_AUTHORITY_CONTAINER
                            .equals(nodeService.getType(item))) {
                        String profileDisplayName = getAllProfiles(context)
                                .get(item);
                        // if the node represents a group, show the authority
                        // display name instead of the name

                        out.write("<option value='");
                        out.write(item.toString());
                        out.write("'>");
                        out.write(Utils.encode(profileDisplayName));
                        out.write("</option>");
                    } else {
                        out.write("<option value='");
                        out.write(item.toString());
                        out.write("'>");
                        out.write(Utils.encode(Repository
                                .getDisplayPath(nodeService.getPath(item))));
                        out.write("/");
                        out.write(Utils.encode(Repository.getNameForNode(
                                nodeService, item)));
                        out.write("</option>");
                    }
                }
            }
        }

        out.write("</select></td></tr>");

    }

    @Override
    protected void renderExistingAssociation(FacesContext context,
                                             ResponseWriter out, NodeService nodeService, NodeRef targetRef,
                                             boolean allowMany) throws IOException {
        boolean accessDenied = false;
        out.write("<tr><td class='");
        if (this.highlightedRow) {
            out.write("selectedItemsRowAlt");
        } else {
            out.write("selectedItemsRow");
        }
        out.write("'>");

        if (ContentModel.TYPE_PERSON.equals(nodeService.getType(targetRef))) {
            if (getIgNodeRef() != null) {

                out.write(getAllUsers(context).get(targetRef));
            } else // alfresco stuff
            {
                out.write(Utils.encode(User.getFullNameAndUserId(nodeService,
                        targetRef)));

            }
        } else if (ContentModel.TYPE_AUTHORITY_CONTAINER.equals(nodeService
                .getType(targetRef))) {

            if (getIgNodeRef() != null) {

                out.write(getAllProfiles(context).get(targetRef));
            } else // alfresco stuff
            {

                // get display name, if not present strip prefix from group id
                String groupDisplayName = (String) nodeService.getProperty(
                        targetRef, ContentModel.PROP_AUTHORITY_DISPLAY_NAME);
                if (groupDisplayName == null || groupDisplayName.length() == 0) {
                    String group = (String) nodeService.getProperty(targetRef,
                            ContentModel.PROP_AUTHORITY_NAME);
                    groupDisplayName = group
                            .substring(PermissionService.GROUP_PREFIX.length());
                }

                out.write(groupDisplayName);

            }

        } else {
            PermissionService permissionService = Repository
                    .getServiceRegistry(context).getPermissionService();
            if (permissionService.hasPermission(targetRef,
                    PermissionService.READ) == AccessStatus.ALLOWED) {
                out.write(Utils.encode(Repository.getDisplayPath(nodeService
                        .getPath(targetRef))));
                out.write("/");
                out.write(Utils.encode(Repository.getNameForNode(nodeService,
                        targetRef)));
            } else {
                accessDenied = true;
                out.write(Application.getMessage(context, MSG_WARN_CANNOT_VIEW));
            }
        }
        if (accessDenied) {
            out.write("</td><td>&nbsp;");
        } else {
            out.write("</td><td class='");
            if (this.highlightedRow) {
                out.write("selectedItemsRowAlt");
            } else {
                out.write("selectedItemsRow");
            }
            out.write("'><a href='#' title='");
            out.write(Application.getMessage(context, MSG_REMOVE));
            out.write("' onclick=\"");
            out.write(generateFormSubmit(context, ACTION_REMOVE
                    + ACTION_SEPARATOR + targetRef.toString()));
            out.write("\"><img src='");
            out.write(context.getExternalContext().getRequestContextPath());
            out.write("/images/icons/delete.gif' border='0' width='13' height='16'/></a>");

            if (!allowMany) {
                out.write("&nbsp;<a href='#' title='");
                out.write(Application.getMessage(context, MSG_CHANGE));
                out.write("' onclick=\"");
                out.write(generateFormSubmit(context, ACTION_CHANGE
                        + ACTION_SEPARATOR + targetRef.toString()));
                out.write("\"><img src='");
                out.write(context.getExternalContext().getRequestContextPath());
                out.write("/images/icons/edit_icon.gif' border='0' width='12' height='16'/></a>");
            }
        }
        out.write("</td></tr>");

        this.highlightedRow = !this.highlightedRow;
    }

    private Map<NodeRef, String> getAllProfiles(FacesContext context) {
        if (allProfiles == null) {
            UserTransaction tx = null;
            try {
                tx = Repository.getUserTransaction(context, true);
                tx.begin();

                List<Profile> profilesList = getProfileService().getProfiles(
                        getIgNodeRef());
                this.allProfiles = new HashMap<>(
                        profilesList.size());

                // get the NodeRef for each matching group
                AuthorityDAO authorityDAO = (AuthorityDAO) FacesContextUtils
                        .getRequiredWebApplicationContext(context).getBean(
                                "authorityDAO");
                if (authorityDAO != null) {

                    for (Profile profile : profilesList) {
                        if (profile == null) {
                            continue;
                        }
                        NodeRef groupRef = authorityDAO
                                .getAuthorityNodeRefOrNull(profile
                                        .getPrefixedAlfrescoGroupName());
                        String profileDisplayName = profile.getProfileDisplayName();
                        if (groupRef != null && !profileDisplayName.isEmpty() && !profileDisplayName
                                .equalsIgnoreCase(AuthenticationUtil.getGuestUserName())) {
                            allProfiles.put(groupRef,
                                    profileDisplayName);
                        }
                    }
                }

                // commit the transaction
                tx.commit();
            } catch (Throwable err) {
                Utils.addErrorMessage(MessageFormat.format(Application
                        .getMessage(context, Repository.ERROR_GENERIC), err
                        .getMessage()), err);
                this.allProfiles = Collections.emptyMap();
                try {
                    if (tx != null) {
                        tx.rollback();
                    }
                } catch (Exception tex) {
                    if (logger.isErrorEnabled()) {
                        logger.error("Can not rollback transaction", tex);
                    }
                }
            }
        }

        return allProfiles;
    }

    private Map<NodeRef, String> getAllUsers(FacesContext context) {
        if (allUsers == null) {

            UserTransaction tx = null;
            try {

                tx = Repository.getUserTransaction(context, true);
                tx.begin();

                Set<String> invitedUsers = getProfileService().getInvitedUsersProfiles(igNodeRef).keySet();
                PersonService personService = (PersonService) FacesContextUtils
                        .getRequiredWebApplicationContext(context).getBean(
                                "PersonService");

                NodeService nodeService = (NodeService) FacesContextUtils
                        .getRequiredWebApplicationContext(context).getBean(
                                "NodeService");
                allUsers = new HashMap<>(invitedUsers.size());
                for (String user : invitedUsers) {
                    NodeRef person = personService.getPerson(user);
                    String fullNameAndUserId = User.getFullNameAndUserId(
                            nodeService, person);
                    allUsers.put(person, fullNameAndUserId);
                }

                // commit the transaction
                tx.commit();
            } catch (Throwable err) {
                Utils.addErrorMessage(MessageFormat.format(Application
                        .getMessage(context, Repository.ERROR_GENERIC), err
                        .getMessage()), err);
                this.availableOptions = Collections.emptyList();
                this.allUsers = Collections.emptyMap();
                try {
                    if (tx != null) {
                        tx.rollback();
                    }
                } catch (Exception tex) {
                    if (logger.isErrorEnabled()) {
                        logger.error("Can not rollback transaction", tex);
                    }
                }
            }
        }
        return allUsers;

    }


    @Override
    protected void renderReadOnlyAssociations(FacesContext context, ResponseWriter out,
                                              NodeService nodeService) throws IOException {
        if (this.originalAssocs.size() > 0) {
            out.write("<table cellspacing='0' cellpadding='2' border='0'>");

            for (Object o : this.originalAssocs.values()) {
                out.write("<tr><td>");
                AssociationRef assoc = (AssociationRef) o;
                NodeRef targetNode = assoc.getTargetRef();

                if (nodeService.exists(targetNode)) {
                    if (ContentModel.TYPE_PERSON.equals(nodeService.getType(targetNode))) {
                        if (getIgNodeRef() != null) {
                            out.write(getAllUsers(context).get(targetNode));
                        } else {
                            // if the node represents a person, show the username instead of the name
                            out.write(Utils.encode(User.getFullNameAndUserId(nodeService, targetNode)));
                        }
                    } else if (ContentModel.TYPE_AUTHORITY_CONTAINER
                            .equals(nodeService.getType(targetNode))) {
                        if (getIgNodeRef() != null) {
                            out.write(getAllProfiles(context).get(targetNode));
                        } else // alfresco stuff
                        {
                            // if the node represents a group, show the group display name instead of the name
                            String groupDisplayName = (String) nodeService.getProperty(targetNode,
                                    ContentModel.PROP_AUTHORITY_DISPLAY_NAME);
                            if (groupDisplayName == null || groupDisplayName.length() == 0) {
                                String group = (String) nodeService.getProperty(targetNode,
                                        ContentModel.PROP_AUTHORITY_NAME);
                                groupDisplayName = group.substring(PermissionService.GROUP_PREFIX.length());
                            }

                            out.write(groupDisplayName);
                        }
                    } else {
                        // use the standard cm:name property

                        // Fix AWC-1301
                        String displayString;
                        try {
                            displayString =
                                    Repository.getDisplayPath(nodeService.getPath(targetNode)) + "/" + Repository
                                            .getNameForNode(nodeService, targetNode);
                        } catch (AccessDeniedException ade) {
                            displayString = Application.getMessage(context, MSG_WARN_CANNOT_VIEW_TARGET_DETAILS);
                        }

                        out.write(Utils.encode(displayString));
                    }
                } else {
                    String message = Application.getMessage(context, MSG_WARN_USER_WAS_DELETED);
                    out.write(message);
                }
                out.write("</td></tr>");
            }

            out.write("</table>");
        }
    }

}
