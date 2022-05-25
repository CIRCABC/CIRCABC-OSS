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
package eu.cec.digit.circabc.web.wai.dialog.ig;

import eu.cec.digit.circabc.business.api.link.LinksBusinessSrv;
import eu.cec.digit.circabc.business.api.link.ShareSpaceItem;
import eu.cec.digit.circabc.service.app.CircabcService;
import eu.cec.digit.circabc.service.profile.Profile;
import eu.cec.digit.circabc.service.profile.ProfileManagerService;
import eu.cec.digit.circabc.util.PathUtils;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.app.CircabcNavigationHandler;
import eu.cec.digit.circabc.web.bean.override.CircabcBrowseBean;
import eu.cec.digit.circabc.web.wai.dialog.generic.DeleteNodeDialog;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.archive.NodeArchiveService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.lock.NodeLockedException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.util.*;
import java.util.Map.Entry;


/**
 * Bean that backs the "Delete Interest Group" WAI Dialog.
 *
 * @author Stephane Clinckart
 */
public class DeleteIgNodeDialog extends DeleteNodeDialog {

    /**
     * Public JSF Bean name
     */

    public static final String BEAN_NAME = "DeleteIgNodeDialog";
    /**
     *
     */
    private static final long serialVersionUID = -3366182849266786067L;
    private static final Log logger = LogFactory.getLog(DeleteIgNodeDialog.class);
    private static final String MSG_IG_CONTAINS_LOCKED_DOCUMENTS = "ig_contains_locked_documents";
    private static final String MSG_SUCCESS = "delete_ig_node_dialog_success";

    final Map<Profile, List<NodeRef>> profilesExportedUsedByIG = new HashMap<>();

    private List<ShareSpaceItem> sharedSpaces = new ArrayList<>();

    private transient RuleService ruleService;

    /**
     * NodeArchiveService bean reference
     */
    transient private NodeArchiveService nodeArchiveService;

    private NodeRef archivedNode;
    private boolean deleteLog;
    private boolean purgeData;

    private NodeRef igNodeRef = null;
    private CircabcService circabcService;

    private long interestGroupID;


    @Override
    public void init(final Map<String, String> parameters) {
        try {
            super.init(parameters);
            deleteLog = true;
            purgeData = true;
            igNodeRef = getActionNode().getNodeRef();
            final ProfileManagerService profileManagerService = getProfileManagerServiceFactory()
                    .getProfileManagerService(igNodeRef);
            final List<Profile> profiles = profileManagerService.getProfiles(igNodeRef);
            profilesExportedUsedByIG.clear();
            List<NodeRef> igs;
            for (final Profile profile : profiles) {
                if (profile.isExported()) {
                    igs = new ArrayList<>(
                            profileManagerService.getRefInExportedProfile(igNodeRef, profile.getProfileName()));
                    if (igs.size() > 0) {
                        profilesExportedUsedByIG.put(profile, igs);
                    }
                }
            }

            sharedSpaces.clear();
            final List<ShareSpaceItem> spItems = getLinksBusinessSrv().findSharedSpaces(igNodeRef);
            if (spItems != null) {
                sharedSpaces.addAll(spItems);
            }

            logRecord.setIgID((Long) getNodeService()
                    .getProperty(getManagementService().getCurrentCategory(igNodeRef),
                            ContentModel.PROP_NODE_DBID));
            logRecord.setDate(new Date());
            logRecord
                    .setIgName(getNodeService().getProperty(igNodeRef, ContentModel.PROP_NAME).toString());
            logRecord.setUser(AuthenticationUtil.getFullyAuthenticatedUser());
            logRecord.setInfo(
                    "Ig Title: " + (getNodeService().getProperty(igNodeRef, ContentModel.PROP_TITLE) != null
                            ? getNodeService().getProperty(igNodeRef, ContentModel.PROP_TITLE).toString() : ""));
            Path path = getNodeService().getPath(igNodeRef);
            String displayPath = PathUtils.getCircabcPath(path, true);
            displayPath = displayPath.endsWith("contains") ? displayPath
                    .substring(0, displayPath.length() - "contains".length()) : displayPath;

            logRecord.setPath(displayPath);

        } catch (final Exception ex) {
            if (logger.isErrorEnabled()) {
                logger.error("init failed:" + ex.getMessage(), ex);
            }
        }


    }

    public boolean getDeletionAllowed() {
        return getProfilesExportedUsedByIGIsEmpty() && getSharedSpacesIsEmpty();
    }

    /**
     * @return
     */
    public boolean getSharedSpacesIsEmpty() {
        return sharedSpaces.isEmpty();
    }

    /**
     * @return
     */
    public boolean getProfilesExportedUsedByIGIsEmpty() {
        return (profilesExportedUsedByIG.size() == 0);
    }


    @Override
    protected String doPostCommitProcessing(FacesContext context, String outcome) {
        if (purgeData) {
            if (archivedNode != null) {
                if (this.getNodeService().exists(archivedNode)) {
                    this.getNodeArchiveService().purgeArchivedNode(archivedNode);
                } else {
                    if (logger.isWarnEnabled()) {
                        logger.warn("Unable to purge after deleting interest group:");
                        logger.warn("Archive node " + archivedNode.toString() + " does not exists");
                    }
                }
            }
        }
        if (interestGroupID > 0) {
            getCircabcService().deleteIntestGroupByID(interestGroupID);
        }

        return super.doPostCommitProcessing(context, outcome);

    }

    public List<ProfilesListWrapper> getProfiles() {
        final List<ProfilesListWrapper> profiles = new ArrayList<>();
        try {
            String profileName;
            for (final Entry<Profile, List<NodeRef>> entry : profilesExportedUsedByIG.entrySet()) {
                final Profile profile = entry.getKey();
                profileName = profile.getProfileDisplayName();
                if (logger.isErrorEnabled()) {
                    logger.error("Exported Profile:" + profileName + " used in:");
                }

                final List<NodeRef> igs = entry.getValue();
                final List<String> igsList = new ArrayList<>(igs.size());
                String title;
                for (final NodeRef nodeRef : igs) {
                    title = getBestTitle(nodeRef);
                    if (logger.isErrorEnabled()) {
                        logger.error("IG:" + title);
                    }
                    igsList.add(title);
                }

                profiles.add(new ProfilesListWrapper(profileName, igsList));
            }
        } catch (final Exception ex) {
            if (logger.isErrorEnabled()) {
                logger.error("getProfiles:" + ex.getMessage(), ex);
            }
        }
        return profiles;
    }

    @Override
    public boolean getFinishButtonDisabled() {
        return !getDeletionAllowed();
    }

    @Override
    protected String finishImpl(final FacesContext context, final String outcome) throws Exception {
        try {
            NodeRef parent = null;
            archivedNode = null;

            if (getActionNode() != null && actionNodeType != null) {
                final String currentName = getActionNode().getName();
                final NodeRef currentNode = getActionNode().getNodeRef();
                if (logger.isDebugEnabled()) {

                    final String name = (String) getNodeService()
                            .getProperty(currentNode, ContentModel.PROP_NAME);
                    logger.debug(
                            "Trying to delete Interest Group: " + name + " with ID:" + getActionNode().getId());
                }
                parent = getNodeService().getPrimaryParent(currentNode).getParentRef();
                interestGroupID = (Long) this.getNodeService()
                        .getProperty(currentNode, ContentModel.PROP_NODE_DBID);

                //delete the node
                try {
                    // A  rule was trying to add LibraryAspect in an archived node,
                    // where a non-owner hasn't right to perform AddAspect.
                    getRuleService().disableRules();
                    if (purgeData) {
                        this.getNodeService().addAspect(currentNode, ContentModel.ASPECT_TEMPORARY, null);
                    }
                    this.getNodeService().deleteNode(currentNode);
                    Utils.addStatusMessage(FacesMessage.SEVERITY_INFO, translate(MSG_SUCCESS, currentName));
                } catch (final NodeLockedException nle) {
                    interestGroupID = -1;
                    Utils.addErrorMessage(Application.getMessage(context, MSG_IG_CONTAINS_LOCKED_DOCUMENTS));
                    return null;
                } finally {
                    getRuleService().enableRules();
                }

                if (deleteLog && interestGroupID > 0) {
                    this.getLogService().deleteInterestgroupLog(interestGroupID);
                }

                archivedNode = this.getNodeArchiveService().getArchivedNode(currentNode);


            } else {
                if (logger.isWarnEnabled()) {
                    logger.warn("Delete called without a current ID!");
                }
            }

            if (parent != null) {
                getBrowseBean().clickWai(parent);
            }
        } catch (final Exception ex) {
            if (logger.isErrorEnabled()) {
                logger.error("finishImpl:" + ex.getMessage(), ex);
            }
        }

        return CircabcNavigationHandler.CLOSE_WAI_DIALOG_OUTCOME
                + CircabcNavigationHandler.OUTCOME_SEPARATOR
                + CircabcBrowseBean.PREFIXED_WAI_BROWSE_OUTCOME;
    }

    /**
     * @return the linksBusinessSrv
     */
    protected LinksBusinessSrv getLinksBusinessSrv() {
        return getBusinessRegistry().getLinksBusinessSrv();
    }


    /**
     * @return the sharedSpaces
     */
    public List<ShareSpaceItem> getSharedSpaces() {
        return sharedSpaces;
    }

    public final RuleService getRuleService() {
        if (ruleService == null) {
            ruleService = Services.getAlfrescoServiceRegistry(FacesContext.getCurrentInstance())
                    .getRuleService();
        }
        return ruleService;
    }

    public final void setRuleService(RuleService ruleService) {
        this.ruleService = ruleService;
    }

    /**
     * @return nodeArchiveService
     */
    public NodeArchiveService getNodeArchiveService() {
        //check for null for cluster environment
        if (nodeArchiveService == null) {
            nodeArchiveService = (NodeArchiveService) FacesHelper
                    .getManagedBean(FacesContext.getCurrentInstance(), "nodeArchiveService");
        }
        return nodeArchiveService;
    }

    public boolean isDeleteLog() {
        return deleteLog;
    }

    public void setDeleteLog(boolean deleteLog) {
        this.deleteLog = deleteLog;
    }

    public boolean isPurgeData() {
        return purgeData;
    }

    public void setPurgeData(boolean purgeData) {
        this.purgeData = purgeData;
    }

    public CircabcService getCircabcService() {
        if (circabcService == null) {
            circabcService = Services.getCircabcServiceRegistry(FacesContext.getCurrentInstance())
                    .getCircabcService();
        }
        return circabcService;
    }

    public void setCircabcService(CircabcService circabcService) {
        this.circabcService = circabcService;
    }


}
