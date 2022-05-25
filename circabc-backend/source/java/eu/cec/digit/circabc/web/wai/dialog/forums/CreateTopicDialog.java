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

import eu.cec.digit.circabc.model.DocumentModel;
import eu.cec.digit.circabc.service.newsgroup.ModerationService;
import eu.cec.digit.circabc.service.struct.ManagementService;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.WebClientHelper;
import eu.cec.digit.circabc.web.wai.dialog.content.edit.CreateContentBaseDialog;
import eu.cec.digit.circabc.web.wai.manager.ActionsListWrapper;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.service.cmr.dictionary.InvalidAspectException;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.forums.ForumsBean;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIListItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.config.Config;
import org.springframework.extensions.config.ConfigElement;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Bean implementation of the "Create Topic Dialog".
 *
 * @author Yanick Pignot
 * <p>
 * Migration 3.1 -> 3.4.6 - 02/12/2011 Config was moved to Spring. ConfigElement was moved to
 * Spring. This class seems to be developed for CircaBC
 */
public class CreateTopicDialog extends CreateContentBaseDialog {

    public static final String BEAN_NAME = "CircabcCreateTopicDialog";
    /*package*/ static final String MSG_EMPTY_MESSAGE = "newsgroups_forum_error_no_message";
    /*package*/ static final String MSG_WARN_MODERATION = "newsgroups_forum_create_topic_moderation_warning";
    private static final long serialVersionUID = -1648558671594657936L;
    private static final Log logger = LogFactory.getLog(CreateTopicDialog.class);
    private static final String MSG_EMPTY_NAME = "newsgroups_forum_error_no_name";
    private static final String MSG_SAVED = "create_topic_successfullysaved";

    private static final String DIALOG_NAME = "createTopicWai";


    /**
     * The name for the discussion
     */
    protected String name;

    protected NodeRef postRef;

    protected String icon;

    /**
     * The contentService reference
     */
    private transient ModerationService moderationService;

    // ------------------------------------------------------------------------------
    // Wizard implementation

    /**
     * Initialize the bean for the WAI version
     *
     * @param parameters Parameters Map
     */
    @Override
    public void init(final Map<String, String> parameters) {
        super.init(parameters);
        setDefaultMode(MODE_HTML);

        if (getActionNode() == null) {
            throw new IllegalArgumentException("The node id parameter is mandatory");
        }

        logRecord.setService(super.getServiceFromActionNode());
        logRecord.setActivity("Create topic");

        if (parameters != null) {
            this.name = null;
            this.icon = null;
            this.postRef = null;
        }

    }

    @Override
    protected String finishImpl(final FacesContext context, final String outcome) throws Exception {
        updateLogDocument(getActionNode().getNodeRef(), logRecord);

        if (isValid() == false) {
            this.isFinished = false;
            return null;
        } else {
            String content = getSelectedContent();
            if (MODE_TEXT.equals(getEditMode())) {
                // remove link breaks and replace with <br>
                content = Utils.replaceLineBreaks(content, false);
            }

            if (isAlreadySaved() == false) {
                // **  First part - Create topic

                final NodeRef topicNodeRef = createTopicNode(getActionNode().getNodeRef());

                if (getModerationService().isContainerModerated(topicNodeRef)) {
                    Utils.addStatusMessage(FacesMessage.SEVERITY_WARN, translate(MSG_WARN_MODERATION));
                }

                // **  Second Part - Create Post

                this.postRef = createPost(context, topicNodeRef, content);

                if (reopen()) {
                    Utils.addStatusMessage(FacesMessage.SEVERITY_INFO, translate(MSG_SAVED));
                }
            } else {
                final ContentWriter writer = getContentService()
                        .getWriter(this.postRef, ContentModel.PROP_CONTENT, true);
                if (writer != null) {
                    writer.putContent(content);
                }
            }

            attachFiles(this.postRef);

            return outcome;
        }
    }

    /**
     * @return
     */
    public boolean isAlreadySaved() {
        return this.postRef != null;
    }

    @Override
    protected String getDialogName() {
        return DIALOG_NAME;
    }


    @Override
    protected NodeRef getEditablePost() {
        return this.postRef;
    }

    @Override
    public boolean isAttachementAllowed() {
        return true;
    }

    @Override
    public String getFinishButtonLabel() {
        return translate("create_post_label");
    }

    /**
     * @return Returns the icon.
     */
    public String getIcon() {
        return this.icon;
    }

    public final void setIcon(final String icon) {
        this.icon = icon;
    }

    public List<UIListItem> getIcons() {
        // NOTE: we can't cache this list as it depends on the space type
        //       which the user can change during the advanced space wizard

        List<UIListItem> icons = null;
        final List<String> iconNames = new ArrayList<>(8);
        final QName type = ForumModel.TYPE_TOPIC;
        final String typePrefixForm = type.toPrefixString(getNamespaceService());
        final Config config = Application.getConfigService(FacesContext.getCurrentInstance())
                .getConfig(typePrefixForm + " icons");

        if (config != null) {
            final ConfigElement iconsCfg = config.getConfigElement("icons");
            if (iconsCfg != null) {
                boolean first = true;
                for (final ConfigElement iconConfigElement : iconsCfg.getChildren()) {
                    final String iconName = iconConfigElement.getAttribute("name");
                    final String iconPath = iconConfigElement.getAttribute("path");

                    if (iconName != null && iconPath != null) {
                        if (first) {
                            // if this is the first icon create the list and make
                            // the first icon in the list the default

                            icons = new ArrayList<>(iconsCfg.getChildCount());
                            if (this.icon == null) {
                                // set the default if it is not already
                                this.icon = iconName;
                            }
                            first = false;
                        }

                        final UIListItem item = new UIListItem();
                        item.setValue(iconName);
                        item.setImage(iconPath);
                        icons.add(item);
                        iconNames.add(iconName);
                    }
                }
            }
        }
        // make sure the current value for the icon is valid for the current list of icons about to be displayed
        if (iconNames.contains(this.icon) == false) {
            this.icon = iconNames.get(0);
        }

        return icons;
    }

    /**
     * @param forumNodeRef
     * @return
     * @throws FileExistsException
     * @throws InvalidNodeRefException
     * @throws InvalidAspectException
     */
    /*package*/ NodeRef createTopicNode(final NodeRef forumNodeRef)
            throws FileExistsException, InvalidNodeRefException, InvalidAspectException {
        NodeRef topicNodeRef;
        final String validName = WebClientHelper.toValidFileName(this.name);
        final FileInfo topicFile = getFileFolderService()
                .create(forumNodeRef, validName, ForumModel.TYPE_TOPIC);
        topicNodeRef = topicFile.getNodeRef();

        if (logger.isDebugEnabled()) {
            logger.debug("Created topic node with name: " + validName);
        }
        if (this.icon == null || this.icon.length() < 1) {
            this.icon = "topic";
        }

        //apply the uifacets aspect - icon, title and description props
        final Map<QName, Serializable> uiFacetsPropTopics = new HashMap<>(5);

        String validIcon = getIcon();
        if (icon == null || icon.length() < 1) {
            validIcon = ManagementService.DEFAULT_TOPIC_ICON_NAME;
        }

        uiFacetsPropTopics.put(ApplicationModel.PROP_ICON, validIcon);
        uiFacetsPropTopics.put(ContentModel.PROP_TITLE, name);
        uiFacetsPropTopics.put(ContentModel.PROP_DESCRIPTION, null);
        getNodeService().addAspect(topicNodeRef, ApplicationModel.ASPECT_UIFACETS, uiFacetsPropTopics);

        if (logger.isDebugEnabled()) {
            logger.debug("Added uifacets aspect with properties: " + uiFacetsPropTopics);
        }
        return topicNodeRef;
    }

    /*package*/ NodeRef createPost(final FacesContext context, final NodeRef topicNodeRef,
                                   final String _message)
            throws FileExistsException, InvalidNodeRefException, InvalidAspectException, InvalidTypeException, ContentIOException {
        NodeRef postNodeRef;

        // create a unique file name for the message content
        final String fileName = ForumsBean.createPostFileName();

        final FileInfo postFile = getFileFolderService()
                .create(topicNodeRef, fileName, ForumModel.TYPE_POST);
        postNodeRef = postFile.getNodeRef();

        if (logger.isDebugEnabled()) {
            logger.debug("Created post node with filename: " + fileName);
        }

        // apply the titled aspect - title and description
        final Map<QName, Serializable> titledProps = new HashMap<>(3, 1.0f);
        titledProps.put(ContentModel.PROP_TITLE, fileName);
        getNodeService().addAspect(postNodeRef, ContentModel.ASPECT_TITLED, titledProps);

        if (logger.isDebugEnabled()) {
            logger.debug("Added titled aspect with properties: " + titledProps);
        }

        final Map<QName, Serializable> editProps = new HashMap<>(1, 1.0f);
        editProps.put(ApplicationModel.PROP_EDITINLINE, true);
        getNodeService().addAspect(postNodeRef, ApplicationModel.ASPECT_INLINEEDITABLE, editProps);

        if (logger.isDebugEnabled()) {
            logger.debug("Added inlineeditable aspect with properties: " + editProps);
        }

        // get a writer for the content and put the file
        final ContentWriter writer = getContentService()
                .getWriter(postNodeRef, ContentModel.PROP_CONTENT, true);
        // set the mimetype and encoding
        writer.setMimetype(Repository.getMimeTypeForFileName(context, fileName));
        writer.setEncoding("UTF-8");

        writer.putContent(getCleanHTML(_message, true));

        return postNodeRef;
    }

    protected boolean isValid() {
        final String message = getSelectedContent();
        boolean valid = true;
        if (message == null || message.trim().length() < 1) {
            valid = false;
            Utils.addErrorMessage(translate(MSG_EMPTY_MESSAGE));
        }
        if (this.name == null || this.name.trim().length() < 1) {
            valid = false;
            Utils.addErrorMessage(translate(MSG_EMPTY_NAME));
        }

        if (!isCleanHTML(name, false)) {
            valid = false;
            Utils
                    .addErrorMessage(translate(MSG_ERR_INVALID_VALUE_FOR, translate("create_topic_subject")));
        }

        return valid;
    }

    public String getBrowserTitle() {
        return Application.getMessage(FacesContext.getCurrentInstance(), "create_topic_title_wai");
    }

    public String getPageIconAltText() {
        return Application.getMessage(FacesContext.getCurrentInstance(), "create_topic_icon_tooltip");
    }

    public ActionsListWrapper getActionList() {
        return null;
    }

    public boolean isCancelButtonVisible() {
        return true;
    }

    @Override
    public boolean getFinishButtonDisabled() {
        return false;
    }

    public boolean isFormProvided() {
        return false;
    }

    /**
     * @return the securityRanking
     */
    public final List<SelectItem> getSecurityRankings() {
        final FacesContext fc = FacesContext.getCurrentInstance();
        final List<String> rankings = DocumentModel.SECURITY_RANKINGS;
        final List<SelectItem> items = new ArrayList<>(rankings.size());
        for (final String ranking : rankings) {
            items.add(new SelectItem(ranking, Application.getMessage(fc, ranking)));
        }

        return items;
    }

    public final String getName() {
        return name;
    }

    public final void setName(final String name) {
        this.name = name;
    }

    public final ModerationService getModerationService() {
        if (moderationService == null) {
            moderationService = Services.getCircabcServiceRegistry(FacesContext.getCurrentInstance())
                    .getModerationService();
        }
        return moderationService;
    }

    public final void setModerationService(final ModerationService moderationService) {
        this.moderationService = moderationService;
    }
}
