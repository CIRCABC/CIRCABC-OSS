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
package eu.cec.digit.circabc.web.wai.bean.navigation;

import eu.cec.digit.circabc.web.bean.navigation.NavigableNode;
import eu.cec.digit.circabc.web.bean.override.CircabcBrowseBean;
import eu.cec.digit.circabc.web.repository.IGServicesNode;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.web.bean.repository.Node;

/**
 * @author Yanick Pignot
 */
public abstract class ResolverHelper {

    public static final String ICON_FORUM_16 = "forum-16";
    public static final String SMALL_ICON = "smallIcon";
    public static final String DISPLAY_PATH = "displayPath";
    public static final String LANG = "lang";
    public static final String SIZE = "size";
    public static final String FILE_TYPE16 = "fileType16";
    public static final String FILE_TYPE32 = "fileType32";
    public static final String URL = "url";
    public static final String DOWNLOAD_URL = "downloadUrl";
    public static final String WEBDAV_URL = "webdavUrl";
    public static final String CIFS_PATH = "cifsPath";
    public static final String ICON = "icon";
    public static final String IS_CONTAINER = "isContainer";
    public static final String DISPLAY_LIGTH_PATH = "displayLigthPath";
    public static final String SHORT_NAME = "shortName";
    public static final String REPLIES_NUMBER = "replies_number";
    public static final String NAME_TRUNKED = "nameTrunked";
    public static final String FROM_TOPIC = "fromTopic";
    public static final String MESSAGE = "message";
    public static final String CREATOR_PROFILE = "creatorProfile";
    public static final String CREATOR_USER_NAME = "creatorUserName";
    public static final String IS_MODERATED = "isModerated";
    public static final String IS_WAITING_FOR_APPROVAL = "waitingApproval";
    public static final String NUMBER_WAITING_FOR_APPROVAL = "numberWaitingApproval";
    public static final String NUMBER_APPOVED = "numberApproved";
    public static final String NUMBER_REJECTED = "numberRejected";
    public static final String IS_REJECTED = "rejected";
    public static final String IS_APPROVED = "approved";
    public static final String IS_SIGNALED = "abuseSignaled";
    public static final String LAST_POST = "lastPost";
    public static final String REJECT_DISPLAY_MESSAGE = "rejectDetails";
    public static final String ABUSE_DISPLAY_MESSAGE = "abuseDetails";
    public static final String HAS_ATTACHEMENT = "hasAttachement";
    public static final String DOC_NUMBER = "docNumber";
    public static final String SPACE_SIZE = "spaceSize";
    public static final String CREATOR_SIGNATURE = "creatorSignature";
    public static final String CREATOR_AVATAR = "creatorAvatar";
    public static final String ON_CLICK = "onclick";

    /**
     *
     */
    private ResolverHelper() {
    }

    public static NavigableNode createContentRepresentation(final FileInfo fileInfo,
                                                            final NodeService nodeService, final CircabcBrowseBean browseBean) {
        //create our Node representation
        final NavigableNode node = buildNode(fileInfo, nodeService);
        addContentRepresentation(node, browseBean, false);
        return node;
    }

    public static NavigableNode createFileLinkRepresentation(final FileInfo fileInfo,
                                                             final NodeService nodeService, final CircabcBrowseBean browseBean) {
        //only display the user has the permissions to navigate to the target of the link
        final NodeRef destRef = (NodeRef) fileInfo.getProperties()
                .get(ContentModel.PROP_LINK_DESTINATION);
        if (destRef != null && new Node(destRef).hasPermission(PermissionService.READ) == true) {
            // create our File Link Node representation
            final NavigableNode node = buildNode(fileInfo, nodeService);
            addContentRepresentation(node, browseBean, true);
            return node;
        } else {
            return null;
        }
    }

    public static NavigableNode createFolderLinkRepresentation(final FileInfo fileInfo,
                                                               final NodeService nodeService, final CircabcBrowseBean browseBean) {
        //	only display the user has the permissions to navigate to the target of the link
        final NodeRef destRef = (NodeRef) fileInfo.getProperties()
                .get(ContentModel.PROP_LINK_DESTINATION);
        if (destRef != null && new Node(destRef).hasPermission(PermissionService.READ) == true) {
            // create our Folder Link Node representation
            final NavigableNode node = buildNode(fileInfo, nodeService);
            addFolderRepresentation(node, browseBean);
            return node;
        } else {
            return null;
        }
    }

    public static NavigableNode createFolderRepresentation(final FileInfo fileInfo,
                                                           final NodeService nodeService, final CircabcBrowseBean browseBean) {
        // create our Node representation
        final NavigableNode node = buildNode(fileInfo, nodeService);
        addFolderRepresentation(node, browseBean);
        return node;
    }

    public static NavigableNode createForumRepresentation(final FileInfo fileInfo,
                                                          final NodeService nodeService, final CircabcBrowseBean browseBean) {
        //create our Node representation
        final NavigableNode node = buildNode(fileInfo, nodeService);
        addForumRepresentation(node, browseBean);
        return node;
    }

    public static NavigableNode createTopicRepresentation(final FileInfo fileInfo,
                                                          final NodeService nodeService, final CircabcBrowseBean browseBean) {
        //create our Node representation
        final NavigableNode node = buildNode(fileInfo, nodeService);
        addTopicRepresentation(node, browseBean);
        return node;
    }

    public static NavigableNode createPostRepresentation(final FileInfo fileInfo,
                                                         final NodeService nodeService, final CircabcBrowseBean browseBean) {
        //create our Node representation
        final NavigableNode node = buildNode(fileInfo, nodeService);
        addPostRepresentation(node, browseBean);
        return node;
    }


    private static void addFolderRepresentation(final NavigableNode node,
                                                final CircabcBrowseBean browseBean) {
        node.addPropertyResolver(SMALL_ICON, browseBean.resolverSmallIcon);
        node.addPropertyResolver(ICON, browseBean.resolverSpaceIcon);
        node.addPropertyResolver(DISPLAY_PATH, browseBean.resolverSimpleDisplayPath);
        node.addPropertyResolver(DISPLAY_LIGTH_PATH, browseBean.resolverSimpleDisplayPathLight);
        node.addPropertyResolver(SHORT_NAME, browseBean.resolverShortName);
        node.addPropertyResolver(DOC_NUMBER, browseBean.resolverDocumentNumber);
        node.addPropertyResolver(SPACE_SIZE, browseBean.resolverSpaceSize);

        node.put(IS_CONTAINER, true);
    }

    private static void addContentRepresentation(final NavigableNode node,
                                                 final CircabcBrowseBean browseBean, final boolean isLink) {
        node.addPropertyResolver(FILE_TYPE16, browseBean.resolverFileType16);
        node.addPropertyResolver(FILE_TYPE32, browseBean.resolverFileType32);
        node.addPropertyResolver(SIZE, browseBean.resolverSize);
        node.addPropertyResolver(LANG, browseBean.resolverLang);
        node.addPropertyResolver(DISPLAY_PATH, browseBean.resolverSimpleDisplayPath);
        node.addPropertyResolver(DISPLAY_LIGTH_PATH, browseBean.resolverSimpleDisplayPathLight);
        node.addPropertyResolver(SHORT_NAME, browseBean.resolverShortName);
        node.put(IS_CONTAINER, false);
        if (isLink) {
            node.addPropertyResolver(DOWNLOAD_URL, browseBean.resolverLinkDownload);
            node.addPropertyResolver(URL, browseBean.resolverLinkUrl);
            node.addPropertyResolver(WEBDAV_URL, browseBean.resolverLinkWebdavUrl);
            node.addPropertyResolver(CIFS_PATH, browseBean.resolverLinkCifsPath);
        } else {
            node.addPropertyResolver(DOWNLOAD_URL, browseBean.resolverDownload);
            node.addPropertyResolver(URL, browseBean.resolverUrl);
            node.addPropertyResolver(WEBDAV_URL, browseBean.resolverWebdavUrl);
            node.addPropertyResolver(CIFS_PATH, browseBean.resolverCifsPath);
        }
    }

    private static void addForumRepresentation(final NavigableNode node,
                                               final CircabcBrowseBean browseBean) {
        node.addPropertyResolver(DISPLAY_PATH, browseBean.resolverSimpleDisplayPath);
        node.addPropertyResolver(DISPLAY_LIGTH_PATH, browseBean.resolverSimpleDisplayPathLight);
        node.addPropertyResolver(IS_MODERATED, browseBean.resolverIsModerated);
        node.addPropertyResolver(NUMBER_WAITING_FOR_APPROVAL, browseBean.resolverNumberWaitingPost);
        node.addPropertyResolver(NUMBER_APPOVED, browseBean.resolverNumberApprovedPost);
        node.addPropertyResolver(NUMBER_REJECTED, browseBean.resolverNumberRejectedPost);
        node.addPropertyResolver(NAME_TRUNKED, browseBean.resolverTrunckedFourHundredName);
        node.addPropertyResolver(LAST_POST, browseBean.resolverLastPost);
        node.put(SMALL_ICON, ICON_FORUM_16);

        node.put(IS_CONTAINER, true);
    }

    private static void addTopicRepresentation(final NavigableNode node,
                                               final CircabcBrowseBean browseBean) {
        node.addPropertyResolver(DISPLAY_PATH, browseBean.resolverSimpleDisplayPath);
        node.addPropertyResolver(DISPLAY_LIGTH_PATH, browseBean.resolverSimpleDisplayPathLight);

        node.addPropertyResolver(NAME_TRUNKED, browseBean.resolverTrunckedFourHundredName);
        node.addPropertyResolver(SMALL_ICON, browseBean.resolverSmallIcon);
        node.addPropertyResolver(REPLIES_NUMBER, browseBean.resolverReplies);
        node.addPropertyResolver(IS_MODERATED, browseBean.resolverIsModerated);
        node.addPropertyResolver(NUMBER_WAITING_FOR_APPROVAL, browseBean.resolverNumberWaitingPost);
        node.addPropertyResolver(NUMBER_APPOVED, browseBean.resolverNumberApprovedPost);
        node.addPropertyResolver(NUMBER_REJECTED, browseBean.resolverNumberRejectedPost);
        node.addPropertyResolver(LAST_POST, browseBean.resolverLastPost);
        node.put(IS_CONTAINER, true);
    }

    private static void addPostRepresentation(final NavigableNode node,
                                              final CircabcBrowseBean browseBean) {
        addContentRepresentation(node, browseBean, false);
        node.addPropertyResolver(URL, browseBean.resolverBrowseWaiUrl);
        node.addPropertyResolver(NAME_TRUNKED, browseBean.resolverContentFourHundred);
        node.addPropertyResolver(FROM_TOPIC, browseBean.resolverFromTopic);
        node.addPropertyResolver(MESSAGE, browseBean.resolverContent);
        node.addPropertyResolver(CREATOR_PROFILE, browseBean.resolverCreatorProfile);
        node.addPropertyResolver(CREATOR_USER_NAME, browseBean.resolverCreatorUserName);

        node.addPropertyResolver(NavigableNode.BEST_TITLE_RESOLVER_NAME, browseBean.resolverPost);

        node.addPropertyResolver(IS_MODERATED, browseBean.resolverIsModerated);
        node.addPropertyResolver(IS_WAITING_FOR_APPROVAL, browseBean.resolverWaitingForApproval);
        node.addPropertyResolver(IS_APPROVED, browseBean.resolverIsApproved);
        node.addPropertyResolver(IS_REJECTED, browseBean.resolverIsRejected);
        node.addPropertyResolver(IS_SIGNALED, browseBean.resolverIsAbuseSignaled);

        node.addPropertyResolver(REJECT_DISPLAY_MESSAGE, browseBean.resolverRejectMessages);
        node.addPropertyResolver(ABUSE_DISPLAY_MESSAGE, browseBean.resolverAbuseMessages);

        node.addPropertyResolver(HAS_ATTACHEMENT, browseBean.resolverHasAttachement);

        node.addPropertyResolver(CREATOR_AVATAR, browseBean.resolverCreatorAvatar);
        node.addPropertyResolver(CREATOR_SIGNATURE, browseBean.resolverCreatorSignature);
    }

    private static IGServicesNode buildNode(final FileInfo fileInfo, final NodeService nodeService) {
        return new IGServicesNode(fileInfo.getNodeRef(), nodeService, fileInfo.getProperties());
    }
}
