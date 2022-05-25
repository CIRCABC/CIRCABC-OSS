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
package eu.cec.digit.circabc.web.wai.dialog.forums;

import eu.cec.digit.circabc.service.newsgroup.ModerationService;
import eu.cec.digit.circabc.service.struct.ManagementService;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.WebClientHelper;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


/**
 * Bean implementation of the "Create Forum Dialog".
 *
 * @author yanick Pignot
 */
public class CreateForumDialog extends BaseWaiDialog {

    public static final String BEAN_NAME = "CircabcCreateForumDialog";
    private static final String MSG_EMPTY_NAME = "newsgroups_forum_create_forum_name_mandatory";
    private static final long serialVersionUID = 8637487953006931563L;
    private static final Log logger = LogFactory.getLog(CreateForumDialog.class);
    private transient ModerationService moderationService;
    private String name;
    private String description;
    private boolean moderated;

    // ------------------------------------------------------------------------------
    // Wizard implementation

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);
        logRecord.setService("Newsgroup");
        logRecord.setActivity("Create forum");

        if (getActionNode() == null) {
            throw new IllegalArgumentException("The node id parameter is mandatory");
        }

        if (parameters != null) {
            this.name = null;
            this.description = null;
            this.moderated = getModerationService().isContainerModerated(getActionNode().getNodeRef());
        }
    }

    @Override
    protected String finishImpl(final FacesContext context, final String outcome) throws Exception {
        if (name == null || name.trim().length() < 1) {
            Utils.addErrorMessage(translate(MSG_EMPTY_NAME));
            this.isFinished = false;
            return null;
        }

        if (!isCleanHTML(name, false)) {

            Utils.addErrorMessage(translate(MSG_ERR_INVALID_VALUE_FOR, translate("name")));
            this.isFinished = false;
            return null;
        }

        if (!isCleanHTML(description, true)) {

            Utils.addErrorMessage(translate(MSG_ERR_INVALID_VALUE_FOR, translate("description")));
            this.isFinished = false;
            return null;
        }

        final String cleanName = WebClientHelper.toValidFileName(this.name);
        final String title = this.name;

        updateLogDocument(getActionNode().getNodeRef(), logRecord);

        final FileInfo fileInfo = getFileFolderService().create(
                getActionNode().getNodeRef(), cleanName, ForumModel.TYPE_FORUM);

        final NodeRef nodeRef = fileInfo.getNodeRef();

        if (logger.isDebugEnabled()) {
            logger.debug("Created forum node with name: " + cleanName);
        }

        // apply the uifacets aspect - icon, title and description props
        final Map<QName, Serializable> uiFacetsProps = new HashMap<>(5);
        uiFacetsProps.put(ApplicationModel.PROP_ICON, ManagementService.DEFAULT_FORUM_ICON_NAME);
        uiFacetsProps.put(ContentModel.PROP_TITLE, title);
        uiFacetsProps.put(ContentModel.PROP_DESCRIPTION, this.description);
        this.getNodeService().addAspect(nodeRef, ApplicationModel.ASPECT_UIFACETS, uiFacetsProps);

        if (logger.isDebugEnabled()) {
            logger.debug("Added uifacets aspect with properties: " + uiFacetsProps);
        }

        if (moderated) {
            getModerationService().applyModeration(nodeRef, false);

            if (logger.isDebugEnabled()) {
                logger.debug("Moderation activated on forum and furtu topics.");
            }
        }

        return outcome;
    }

    @Override
    public String getFinishButtonLabel() {
        return Application
                .getMessage(FacesContext.getCurrentInstance(), "newsgroups_forum_create_forum");
    }

    public String getBrowserTitle() {
        return Application.getMessage(FacesContext.getCurrentInstance(),
                "newsgroups_forum_create_forum_browser_title");
    }

    public String getPageIconAltText() {
        return Application.getMessage(FacesContext.getCurrentInstance(),
                "newsgroups_forum_create_forum_icon_tooltip");
    }

    public final boolean isModerated() {
        return moderated;
    }

    public final void setModerated(boolean moderated) {
        this.moderated = moderated;
    }

    public final ModerationService getModerationService() {
        if (moderationService == null) {
            moderationService = Services.getCircabcServiceRegistry(FacesContext.getCurrentInstance())
                    .getModerationService();
        }
        return moderationService;
    }

    public final void setModerationService(ModerationService moderationService) {
        this.moderationService = moderationService;
    }

    public final String getDescription() {
        return description;
    }

    public final void setDescription(String description) {
        this.description = description;
    }

    public final String getName() {
        return name;
    }

    public final void setName(String name) {
        this.name = name;
    }
}
