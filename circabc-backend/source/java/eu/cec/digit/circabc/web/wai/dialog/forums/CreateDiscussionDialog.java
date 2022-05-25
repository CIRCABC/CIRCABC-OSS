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

import eu.cec.digit.circabc.error.CircabcRuntimeException;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.service.cmr.dictionary.InvalidAspectException;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.InvalidQNameException;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Bean implementation of the "Create Discussion Dialog".
 *
 * @author Yanick Pignot
 */
public class CreateDiscussionDialog extends CreateTopicDialog {

    public static final String BEAN_NAME = "CreateDiscussionDialog";
    private static final long serialVersionUID = -4943386423005547018L;
    private static final Log logger = LogFactory.getLog(CreateDiscussionDialog.class);
    private static final String MSG_SAVED = "create_topic_successfullysaved";
    private static final String DIALOG_NAME = "createDiscussionWai";

    private boolean moderated;

    // ------------------------------------------------------------------------------
    // Wizard implementation

    @Override
    public void init(final Map<String, String> parameters) {
        super.init(parameters);

        logRecord.setService("Library");
        logRecord.setActivity("Create discusion");

        if (parameters != null) {
            this.moderated = false;
        }
    }

    @Override
    protected String finishImpl(final FacesContext context, String outcome) throws Exception {
        if (!isValid()) {
            this.isFinished = false;
            return null;
        } else {
            String content = getSelectedContent();
            if (MODE_TEXT.equals(getEditMode())) {
                // remove link breaks and replace with <br>
                content = Utils.replaceLineBreaks(content, false);
            }

            if (!isAlreadySaved()) {
                //	**  First part - Creation of the forum

                final NodeRef forumNodeRef = createForumNode();

                if (moderated) {
                    getModerationService().applyModeration(forumNodeRef, false);

                    if (logger.isDebugEnabled()) {
                        logger.debug("Moderation activated on forum and futur topics.");
                    }
                }

                // **  Second part - Create topic

                final NodeRef topicNodeRef = createTopicNode(forumNodeRef);

                if (getModerationService().isContainerModerated(topicNodeRef)) {
                    Utils.addStatusMessage(FacesMessage.SEVERITY_WARN, translate(MSG_WARN_MODERATION));
                }

                // **  Third Part - Create Post

                super.postRef = createPost(context, topicNodeRef, content);

                if (reopen()) {
                    Utils.addStatusMessage(FacesMessage.SEVERITY_INFO, translate(MSG_SAVED));
                }

                // Ensure that the node used by the previsous dialog is correctly rested.
                if (getBrowseBean().getActionSpace() != null) {
                    // should not longer be used !!!
                    getBrowseBean().getActionSpace().reset();
                }
                if (getBrowseBean().getDocument() != null) {
                    // should not longer be used !!!
                    getBrowseBean().getDocument().reset();
                }
                if (getNavigator().getCurrentNode() != null) {
                    // ONLY That should be used, all the dialogs must use ans set if needed this central node.
                    // But Alfresco dialogs use sometimes the previous ones.
                    getNavigator().getCurrentNode().reset();
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

    @Override
    protected String getDialogName() {
        return DIALOG_NAME;
    }

    /**
     * @return
     * @throws InvalidNodeRefException
     * @throws InvalidAspectException
     * @throws AlfrescoRuntimeException
     * @throws InvalidTypeException
     * @throws InvalidQNameException
     */
    private NodeRef createForumNode()
            throws InvalidNodeRefException, AlfrescoRuntimeException, InvalidQNameException {
        NodeRef forumNodeRef;
        final NodeRef discussingNodeRef = getActionNode().getNodeRef();

        if (getNodeService().hasAspect(discussingNodeRef, ForumModel.ASPECT_DISCUSSABLE)) {
            logger.error("createDiscussion called for an object that already has a discussion!"
                    + discussingNodeRef);
            throw new AlfrescoRuntimeException(
                    "createDiscussion called for an object that already has a discussion!");
        }

        // add the discussable aspect
        getNodeService().addAspect(discussingNodeRef, ForumModel.ASPECT_DISCUSSABLE, null);

        // alfresco version 3.4.6 changed when adding aspect we do not need to create node
        List<ChildAssociationRef> destChildren = getNodeService().getChildAssocs(
                discussingNodeRef,
                ForumModel.ASSOC_DISCUSSION,
                RegexQNamePattern.MATCH_ALL);
        if (destChildren.size() == 0) {
            throw new CircabcRuntimeException("The discussable aspect behaviour is not creating a topic");
        } else {
            ChildAssociationRef discussionAssoc = destChildren.get(0);
            forumNodeRef = discussionAssoc.getChildRef();
        }

        // apply the uifacets aspect
        final Map<QName, Serializable> uiFacetsPropForums = new HashMap<>(5);
        uiFacetsPropForums.put(ApplicationModel.PROP_ICON, "forum");
        getNodeService().addAspect(forumNodeRef, ApplicationModel.ASPECT_UIFACETS, uiFacetsPropForums);

        if (logger.isDebugEnabled()) {
            logger.debug("created forum " + forumNodeRef.toString() + " for content: " + discussingNodeRef
                    .toString());
        }
        return forumNodeRef;
    }

    @Override
    public String cancel() {
        // as we are cancelling the creation of a discussion we know we need to go back
        // to the browse screen, this also makes sure we don't end up in the forum that
        // just got deleted!
        return AlfrescoNavigationHandler.CLOSE_DIALOG_OUTCOME;
    }

    // ------------------------------------------------------------------------------
    // Service and Bean Injection

    public String getBrowserTitle() {
        return translate("create_discussion_title_wai");
    }

    public String getPageIconAltText() {
        return translate("create_discussion_icon_tooltip");
    }

    @Override
    public boolean getFinishButtonDisabled() {
        return false;
    }

    public final boolean isModerated() {
        return moderated;
    }

    public final void setModerated(boolean moderated) {
        this.moderated = moderated;
    }
}
