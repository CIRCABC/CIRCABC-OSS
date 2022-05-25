package io.swagger.api;

import eu.cec.digit.circabc.business.api.content.Attachement;
import eu.cec.digit.circabc.business.api.content.AttachementBusinessSrv;
import eu.cec.digit.circabc.business.api.user.UserDetails;
import eu.cec.digit.circabc.business.api.user.UserDetailsBusinessSrv;
import eu.cec.digit.circabc.config.CircabcConfiguration;
import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.model.DocumentModel;
import eu.cec.digit.circabc.service.newsgroup.ModerationService;
import io.swagger.exception.InvalidTopicException;
import io.swagger.model.Comment;
import io.swagger.model.Node;
import io.swagger.model.PagedNodes;
import io.swagger.util.ApiToolBox;
import io.swagger.util.Converter;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.io.IOUtils;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.errors.IntrusionException;
import org.owasp.esapi.errors.ValidationException;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author beaurpi
 */
public class TopicsApiImpl implements TopicsApi {

    private static final String CREATOR = "creator";
    private static final String MESSAGE = "message";
    private static final String AVATAR = "avatar";
    private static final String WAITING_FOR_APPROVAL = "waitingForApproval";
    private final Log logger = LogFactory.getLog(TopicsApiImpl.class);
    private NodeService secureNodeService;
    private ContentService contentService;
    private FileFolderService fileFolderService;
    private PersonService personService;
    private UserDetailsBusinessSrv userDetailsBusinessSrv;
    private NodesApi nodesApi;
    private ModerationService moderationService;
    private AttachementBusinessSrv attachementBusinessSrv;

    /**
     * Creates a file name for the message being posted
     *
     * @return The file name for the post
     */
    private static String createPostFileName() {
        // add a timestamp
        // add Universal Unique Identifier
        // fix bugs ETWOONE-196 and ETWOONE-203

        // add the HTML file extension
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss");

        return "posted-" + dateFormat.format(new Date()) + "-" + UUID.randomUUID() + ".html";
    }

    /*
     * (non-Javadoc)
     *
     * @see io.swagger.api.TopicsApi#getTopicReplies(java.lang.String)
     */
    @Override
    public List<Node> getTopicReplies(String id) {
        NodeRef topicRef = Converter.createNodeRefFromId(id);
        List<Node> result = new ArrayList<>();

        if (secureNodeService.hasAspect(topicRef, CircabcModel.ASPECT_NEWSGROUP)
                || secureNodeService.hasAspect(topicRef, CircabcModel.ASPECT_LIBRARY)) {
            List<ChildAssociationRef> children = secureNodeService.getChildAssocs(topicRef);

            for (ChildAssociationRef item : children) {

                if (secureNodeService.getType(item.getChildRef()).equals(ForumModel.TYPE_POST)) {
                    final NodeRef childRef = item.getChildRef();
                    Node postNode = getPost(childRef);
                    result.add(postNode);
                }
            }
        }

        return result;
    }

    /**
     *
     */
    private Node getPost(final NodeRef childRef) {
        PostNode postNode = (PostNode) nodesApi.getNode(childRef, new PostNode());
        postNode.getProperties().put(MESSAGE, resolvePostContent(childRef));
        String creator = postNode.getProperties().get(CREATOR);
        try {
            final NodeRef personNodeRef = personService.getPerson(creator);
            final UserDetails userDetails = userDetailsBusinessSrv.getUserDetails(personNodeRef);
            postNode.getProperties().put(AVATAR, userDetails.getAvatar().getId());
        } catch (Exception e) {
            logger.error("Error while getting user details for :" + creator, e);
        }
        postNode
                .getProperties()
                .put(
                        WAITING_FOR_APPROVAL,
                        moderationService.isWaitingForApproval(childRef) ? "true" : "false");
        boolean mlAware = MLPropertyInterceptor.isMLAware();
        try {
            MLPropertyInterceptor.setMLAware(false);
            List<Attachement> attachments = getAttachments(childRef.getId());
            postNode.setAttachments(attachments);
        } finally {
            MLPropertyInterceptor.setMLAware(mlAware);
        }

        return postNode;
    }

    private String resolvePostContent(NodeRef childRef) {
        String result = "";
        if (contentService.getReader(childRef, ContentModel.PROP_CONTENT) != null) {
            result = contentService.getReader(childRef, ContentModel.PROP_CONTENT).getContentString();
        }
        return result;
    }

    /**
     * @return the secureNodeService
     */
    public NodeService getSecureNodeService() {
        return secureNodeService;
    }

    /**
     * @param secureNodeService the secureNodeService to set
     */
    public void setSecureNodeService(NodeService secureNodeService) {
        this.secureNodeService = secureNodeService;
    }

    /**
     * @return the contentService
     */
    public ContentService getContentService() {
        return contentService;
    }

    /**
     * @param contentService the contentService to set
     */
    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    /**
     * @return the nodesApi
     */
    public NodesApi getNodesApi() {
        return nodesApi;
    }

    /**
     * @param nodesApi the nodesApi to set
     */
    public void setNodesApi(NodesApi nodesApi) {
        this.nodesApi = nodesApi;
    }

    /**
     * @see io.swagger.api.TopicsApi#topicsIdRepliesPost(java.lang.String, io.swagger.model.Comment,
     * java.util.List, java.util.List)
     */
    @Override
    public Node topicsIdRepliesPost(
            String id, Comment body, List<FileAttachmentData> filesToAdd, List<String> linksToAdd)
            throws InvalidTopicException {

        NodeRef topicRef = Converter.createNodeRefFromId(id);

        String username = AuthenticationUtil.getRunAsUser();
        AuthenticationUtil.setRunAsUserSystem();

        if (!secureNodeService.getType(topicRef).equals(ForumModel.TYPE_TOPIC)) {
            throw new InvalidTopicException();
        }

        NodeRef postNodeRef = null;

        try {
            FileInfo postFile =
                    fileFolderService.create(topicRef, createPostFileName(), ForumModel.TYPE_POST);
            postNodeRef = postFile.getNodeRef();

            Map<QName, Serializable> editProps = new HashMap<>(1, 1.0f);
            editProps.put(ApplicationModel.PROP_EDITINLINE, true);
            secureNodeService.addAspect(postNodeRef, ApplicationModel.ASPECT_INLINEEDITABLE, editProps);

            final ContentWriter writer =
                    getContentService().getWriter(postNodeRef, ContentModel.PROP_CONTENT, true);
            writer.setMimetype(MimetypeMap.MIMETYPE_HTML);
            writer.setEncoding("UTF-8");
            writer.putContent(body.getText());

            // attach files
            for (FileAttachmentData fileToAdd : filesToAdd) {
                addFileAttachment(postNodeRef.getId(), fileToAdd.getName(), fileToAdd.getInputStream());
            }

            // attach links
            for (String linkToAdd : linksToAdd) {
                addLinkAttachment(postNodeRef.getId(), linkToAdd);
            }
        } finally {
            AuthenticationUtil.setRunAsUser(username);
        }

        if (postNodeRef != null) {
            return nodesApi.getNode(postNodeRef);
        } else {
            return null;
        }
    }

    /**
     * @return the fileFolderService
     */
    public FileFolderService getFileFolderService() {
        return fileFolderService;
    }

    /**
     * @param fileFolderService the fileFolderService to set
     */
    public void setFileFolderService(FileFolderService fileFolderService) {
        this.fileFolderService = fileFolderService;
    }

    @Override
    public void postsIdDelete(String id) {
        NodeRef postRef = Converter.createNodeRefFromId(id);

        if (secureNodeService.getType(postRef).equals(ForumModel.TYPE_POST)) {
            secureNodeService.deleteNode(postRef);
        }
    }

    /**
     * @see io.swagger.api.TopicsApi#postsIdPut(java.lang.String, io.swagger.model.Node,
     * java.util.List, java.util.List, java.util.List)
     */
    @Override
    public Node postsIdPut(
            String id,
            Node body,
            List<FileAttachmentData> filesToAdd,
            List<String> linksToAdd,
            List<String> attachmentsToDelete) {

        NodeRef postRef = Converter.createNodeRefFromId(id);

        if (secureNodeService.getType(postRef).equals(ForumModel.TYPE_POST)) {
            final ContentWriter writer =
                    getContentService().getWriter(postRef, ContentModel.PROP_CONTENT, true);
            writer.setMimetype(MimetypeMap.MIMETYPE_HTML);
            writer.setEncoding("UTF-8");
            writer.putContent(body.getProperties().get(MESSAGE));
        }

        if (moderationService.isContainerModerated(postRef)) {
            // in case this forum is moderated, when content is updated, resubmit for
            // approval
            moderationService.waitForApproval(postRef);
        }

        // delete attachments
        for (String attachmentToDelete : attachmentsToDelete) {
            removeAttachment(id, attachmentToDelete);
        }

        // attach files
        for (FileAttachmentData fileToAdd : filesToAdd) {
            addFileAttachment(id, fileToAdd.getName(), fileToAdd.getInputStream());
        }

        // attach links
        for (String linkToAdd : linksToAdd) {
            addLinkAttachment(id, linkToAdd);
        }

        return getPost(postRef);
    }

    @Override
    public void topicsIdDelete(String id) {

        NodeRef topicRef = Converter.createNodeRefFromId(id);

        secureNodeService.deleteNode(topicRef);
    }

    @Override
    public PagedNodes getTopicReplies(String id, Integer nbPage, Integer nbLimit, String sort) {
        PagedNodes pagedResult = new PagedNodes();

        NodeRef topicRef = Converter.createNodeRefFromId(id);

        List<Node> result = new ArrayList<>();

        if (secureNodeService.hasAspect(topicRef, CircabcModel.ASPECT_NEWSGROUP)
                || secureNodeService.hasAspect(topicRef, CircabcModel.ASPECT_LIBRARY)) {

            PagingRequest pr = new PagingRequest(nbPage * nbLimit, nbLimit);

            if (nbLimit != -1) {
                final PagingResults<FileInfo> list =
                        getFileFolderService().list(topicRef, true, false, new HashSet<QName>(), null, pr);

                for (FileInfo item : list.getPage()) {

                    if (secureNodeService.getType(item.getNodeRef()).equals(ForumModel.TYPE_POST)) {
                        final NodeRef childRef = item.getNodeRef();
                        Node postNode = getPost(childRef);
                        result.add(postNode);
                    }
                }
            } else {
                result = getTopicReplies(id);
            }

            pagedResult.setTotal((long) getFileFolderService().list(topicRef).size());
            pagedResult.setData(result);
        }

        return pagedResult;
    }

    /**
     * @see io.swagger.api.TopicsApi#updateTopic(java.lang.String, io.swagger.model.Node)
     */
    @Override
    public void updateTopic(String id, Node topicNode) {

        NodeRef topicRef = getTopicNodeRef(id);

        // for a topic only update these properties and ignore the rest
        secureNodeService.setProperty(topicRef, ContentModel.PROP_TITLE, topicNode.getTitle());
        secureNodeService.setProperty(
                topicRef, ContentModel.PROP_DESCRIPTION, topicNode.getDescription());
        secureNodeService.setProperty(topicRef, ContentModel.PROP_NAME, topicNode.getName());

        String securityRanking = topicNode.getProperties().get("security_ranking");

        if (securityRanking != null && !securityRanking.isEmpty()) {

            if (!DocumentModel.SECURITY_RANKINGS.contains(securityRanking)) {
                throw new IllegalArgumentException(
                        "The 'security_ranking' is invalid:" + securityRanking);
            }

            secureNodeService.setProperty(topicRef, DocumentModel.PROP_SECURITY_RANKING, securityRanking);
        }

        String expirationDateString = topicNode.getProperties().get("expiration_date");

        if (expirationDateString == null || expirationDateString.isEmpty()) {
            secureNodeService.setProperty(topicRef, DocumentModel.PROP_EXPIRATION_DATE, null);
            return;
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        Date expirationDate;

        try {
            expirationDate = simpleDateFormat.parse(expirationDateString);
            secureNodeService.setProperty(topicRef, DocumentModel.PROP_EXPIRATION_DATE, expirationDate);
        } catch (ParseException e) {
            throw new IllegalArgumentException(
                    "The 'expiration_date' has a wrong format. Must be yyyy-MM-dd", e);
        }
    }

    private NodeRef getTopicNodeRef(String id) {

        NodeRef topicRef = Converter.createNodeRefFromId(id);

        if (!secureNodeService.exists(topicRef)) {
            throw new IllegalArgumentException("The topic with id " + id + " could not be found.");
        }

        if (!ForumModel.TYPE_TOPIC.equals(secureNodeService.getType(topicRef))) {
            throw new InvalidTypeException(
                    "The given id does not correspond to a node of type topic", ForumModel.TYPE_TOPIC);
        }

        return topicRef;
    }

    /**
     * @see io.swagger.api.TopicsApi#getAttachments(java.lang.String)
     */
    @Override
    public List<Attachement> getAttachments(String id) {
        NodeRef postNodeRef = Converter.createNodeRefFromId(id);

        List<Attachement> attachments = attachementBusinessSrv.getAttachements(postNodeRef);

        ContentReader reader;

        for (Attachement attachment : attachments) {
            if (attachment.geType() == Attachement.AttachementType.HIDDEN_FILE) {
                reader = contentService.getReader(attachment.getNodeRef(), DocumentModel.PROP_CONTENT);
                attachment.setSize(reader.getSize());
                attachment.setEncoding(reader.getEncoding());
                attachment.setMimetype(reader.getMimetype());
            }
        }

        return attachments;
    }

    /**
     * @see io.swagger.api.TopicsApi#addFileAttachment(java.lang.String, java.lang.String,
     * java.io.File)
     */
    @Override
    public void addFileAttachment(String id, String name, File file) {
        NodeRef postNodeRef = Converter.createNodeRefFromId(id);
        attachementBusinessSrv.addAttachement(postNodeRef, name, file);
    }

    /**
     * @see io.swagger.api.TopicsApi#addFileAttachment(java.lang.String, java.lang.String,
     * java.io.InputStream)
     */
    @Override
    public void addFileAttachment(String id, String name, InputStream inputStream) {

        // esapi validation
        final List<String> allowedFileExtensions =
                ESAPI.securityConfiguration().getAllowedFileExtensions();

        try {
            ESAPI.validator().getValidFileName("submitted file", name, allowedFileExtensions, false);
        } catch (ValidationException | IntrusionException vex) {
            throw new IllegalArgumentException("Invalid file type: " + name);
        }

        // file size validation and transfer to temp file for upload
        NodeRef postNodeRef = Converter.createNodeRefFromId(id);

        File tempFile = null;

        try {

            tempFile = File.createTempFile("attachment", ".tmp");

            long attachmentTotalSize =
                    Long.parseLong(
                            CircabcConfiguration.getProperty(
                                    CircabcConfiguration.POST_ALLOWED_ATTACHMENT_SIZE_BYTES));

            // validate the file being uploaded
            long totalBytesRead =
                    ApiToolBox.inputStreamToFile(inputStream, tempFile, attachmentTotalSize);

            if (totalBytesRead > attachmentTotalSize) {
                throw new IllegalArgumentException(
                        "Size of attachments exceeds the allowed limit for this post: "
                                + attachmentTotalSize
                                + "bytes, Given: "
                                + totalBytesRead);
            }

            // size validation with already attachments
            List<Attachement> attachments = getAttachments(id);

            long totalSize = totalBytesRead * -1;

            for (Attachement attachment : attachments) {
                if (attachment.geType() == Attachement.AttachementType.HIDDEN_FILE) {
                    totalSize += attachment.getSize();
                }
            }

            if (totalSize > attachmentTotalSize) {
                throw new IllegalArgumentException(
                        "Size of attachments exceeds the allowed limit for this post: "
                                + attachmentTotalSize
                                + "bytes, Given: "
                                + totalSize);
            }

            // addition of the new file
            attachementBusinessSrv.addAttachement(postNodeRef, name, tempFile);
        } catch (Exception e) {
            logger.error("Could not create temp file.", e);
            throw new RuntimeException("Could not create temp file.", e);
        } finally {
            if (tempFile != null) {
                boolean isDeleted = tempFile.delete();
                if (!isDeleted) {
                    if (logger.isErrorEnabled()) {
                        logger.error("Can not delete filee" + tempFile.toString());
                    }
                }
            }
            if (inputStream != null) {
                IOUtils.closeQuietly(inputStream);
            }
        }
    }

    /**
     * @see io.swagger.api.TopicsApi#addLinkAttachment(java.lang.String, java.lang.String)
     */
    @Override
    public void addLinkAttachment(String id, String destinationId) {
        NodeRef postNodeRef = Converter.createNodeRefFromId(id);
        NodeRef destinationNodeRef = Converter.createNodeRefFromId(destinationId);
        if (!secureNodeService.exists(destinationNodeRef)) {
            throw new IllegalArgumentException("Node to be linked does not exist.");
        }
        try {
            attachementBusinessSrv.addAttachement(postNodeRef, destinationNodeRef);
        } catch (DuplicateChildNodeNameException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Duplicate child  node name ", e);
            }
        }
    }

    /**
     * @see io.swagger.api.TopicsApi#removeAttachment(java.lang.String, java.lang.String)
     */
    @Override
    public void removeAttachment(String id, String attachmentId) {
        NodeRef postNodeRef = Converter.createNodeRefFromId(id);
        NodeRef attachmentNodeRef = Converter.createNodeRefFromId(attachmentId);
        attachementBusinessSrv.removeAttachement(postNodeRef, attachmentNodeRef);
    }

    /**
     * @see io.swagger.api.TopicsApi#getAttachmentsRemainingSize(java.lang.String)
     */
    @Override
    public long getAttachmentsRemainingSize(String id) {

        NodeRef postNodeRef = Converter.createNodeRefFromId(id);

        long attachmentTotalSize =
                Long.parseLong(
                        CircabcConfiguration.getProperty(
                                CircabcConfiguration.POST_ALLOWED_ATTACHMENT_SIZE_BYTES));

        if (!secureNodeService.exists(postNodeRef)) {
            return attachmentTotalSize;
        }

        List<Attachement> attachments = getAttachments(id);

        long totalSize = 0;

        for (Attachement attachment : attachments) {
            if (attachment.geType() == Attachement.AttachementType.HIDDEN_FILE) {
                totalSize += attachment.getSize();
            }
        }

        long finalSize = attachmentTotalSize - totalSize;

        return finalSize <= 0 ? 0 : finalSize;
    }

    /**
     * @see io.swagger.api.TopicsApi#getAttachment(java.lang.String, java.io.OutputStream)
     */
    @Override
    public void getAttachment(String attachmentId, OutputStream outputStream) {

        NodeRef nodeRef = Converter.createNodeRefFromId(attachmentId);

        final QName type = secureNodeService.getType(nodeRef);

        ContentReader reader;

        if (type.equals(DocumentModel.TYPE_HIDDEN_ATTACHEMENT_CONTENT)) {
            reader = contentService.getReader(nodeRef, DocumentModel.PROP_CONTENT);
        } else {
            throw new IllegalArgumentException("Attachment type is not hidden content.");
        }

        reader.getContent(outputStream);
    }

    /**
     * @param attachementBusinessSrv the attachementBusinessSrv to set
     */
    public void setAttachementBusinessSrv(AttachementBusinessSrv attachementBusinessSrv) {
        this.attachementBusinessSrv = attachementBusinessSrv;
    }

    /**
     * @return the personService
     */
    public PersonService getPersonService() {
        return personService;
    }

    /**
     * @param personService the personService to set
     */
    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    /**
     * @return the userDetailsBusinessSrv
     */
    public UserDetailsBusinessSrv getUserDetailsBusinessSrv() {
        return userDetailsBusinessSrv;
    }

    /**
     * @param userDetailsBusinessSrv the userDetailsBusinessSrv to set
     */
    public void setUserDetailsBusinessSrv(UserDetailsBusinessSrv userDetailsBusinessSrv) {
        this.userDetailsBusinessSrv = userDetailsBusinessSrv;
    }

    /**
     * @param moderationService the moderationService to set
     */
    public void setModerationService(ModerationService moderationService) {
        this.moderationService = moderationService;
    }
}
