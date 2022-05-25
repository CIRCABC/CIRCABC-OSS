package io.swagger.api;

import eu.cec.digit.circabc.aspect.ContentNotifyAspect;
import eu.cec.digit.circabc.business.helper.MetadataManager;
import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.model.DocumentModel;
import eu.cec.digit.circabc.service.bulk.BulkService;
import eu.cec.digit.circabc.service.bulk.indexes.IndexRecord;
import eu.cec.digit.circabc.service.bulk.indexes.IndexService;
import eu.cec.digit.circabc.service.compress.ZipService;
import eu.cec.digit.circabc.service.log.LogRecord;
import eu.cec.digit.circabc.service.log.LogService;
import eu.cec.digit.circabc.service.rendition.CircabcRenditionHelper;
import eu.cec.digit.circabc.service.rendition.CircabcRenditionService;
import eu.cec.digit.circabc.service.rendition.RenditionDaoService;
import eu.cec.digit.circabc.service.rendition.Request;
import eu.cec.digit.circabc.service.struct.ManagementService;
import eu.cec.digit.circabc.service.translation.TranslationService;
import eu.cec.digit.circabc.web.ui.common.UtilsCircabc;
import io.swagger.model.*;
import io.swagger.util.Converter;
import io.swagger.util.CurrentUserPermissionCheckerService;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.repo.action.executer.ContentMetadataExtracter;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.node.integrity.IntegrityChecker;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.dictionary.*;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.openxml4j.exceptions.InvalidOperationException;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.surf.util.ISO8601DateFormat;
import org.springframework.extensions.surf.util.URLEncoder;

import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.*;
import java.util.Map.Entry;

/**
 * @author beaurpi
 */
public class ContentApiImpl implements ContentApi {

    public static final String THIS_NODE_IS_NOT_MULTILINGUAL = "this node is not multilingual";
    public static final String DYN_ATTR = "dynAttr";
    public static final String DISCUSSION = "discussion";
    /**
     * A logger for the class
     */
    private static final Log logger = LogFactory.getLog(ContentApiImpl.class);
    private static final List<String> officeMimetypes =
            new ArrayList<>(
                    Arrays.asList(
                            MimetypeMap.MIMETYPE_WORD,
                            MimetypeMap.MIMETYPE_EXCEL,
                            MimetypeMap.MIMETYPE_PPT,
                            MimetypeMap.MIMETYPE_OPENXML_WORDPROCESSING,
                            MimetypeMap.MIMETYPE_OPENXML_SPREADSHEET,
                            MimetypeMap.MIMETYPE_OPENXML_PRESENTATION));
    private static final String ISSUE_DATE = "issue_date";
    private static final String EXPIRATION_DATE = "expiration_date";
    private static final String REFERENCE = "reference";
    private static final String AUTHOR = "author";
    private static final String MIMETYPE = "mimetype";
    private static final String ENCODING = "encoding";
    private static final String STATUS = "status";
    private static final String SECURITY = "security";
    private static final String URL = "url";
    private VersionService versionService;
    private NodeService nodeService;
    private ContentService contentService;
    private TranslationService translationService;
    private MultilingualContentService multilingualContentService;
    private ManagementService managementService;
    private PermissionService permissionService;
    private DictionaryService dictionaryService;
    private LockService lockService;
    private CheckOutCheckInService checkOutCheckInService;
    private FileFolderService fileFolderService;
    private RenditionDaoService renditionDaoService;
    private CircabcRenditionHelper circabcRenditionHelper = null;
    private ZipService zipService = null;
    private IndexService indexService = null;
    private BulkService bulkService = null;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
    private NodesApi nodesApi;
    private BehaviourFilter policyBehaviourFilter;
    private ServiceRegistry serviceRegistry;
    private CircabcRenditionService circabcRenditionService;
    private LogService logService;
    private ActionService actionService;
    private MetadataManager metadataManager;

    /*
     * (non-Javadoc)
     *
     * @see io.swagger.api.ContentApi#contentIdVersionsGet(java.lang.String, java.lang.String)
     */
    @Override
    public List<Version> contentIdVersionsGet(String id, String language) {

        List<Version> result = new ArrayList<>();
        NodeRef nodeRef = Converter.createNodeRefFromId(id);
        if (versionService.isVersioned(nodeRef)) {
            VersionHistory vhNode = versionService.getVersionHistory(nodeRef);
            for (org.alfresco.service.cmr.version.Version vTmp : vhNode.getAllVersions()) {
                Version modelVersion = new Version();
                modelVersion.setVersionLabel(vTmp.getVersionLabel());
                Node node = nodesApi.getNode(vTmp.getFrozenStateNodeRef());
                setAdditionalNodeProperties(node, vTmp);
                modelVersion.setNode(node);
                modelVersion.setNotes(vTmp.getDescription() != null ? vTmp.getDescription() : "");
                result.add(modelVersion);
            }
        }

        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see io.swagger.api.ContentApi#contentIdVersionsGet(java.lang.String, java.lang.String)
     */
    @Override
    public List<Version> contentIdFirstVersionsGet(String id) {

        List<Version> result = new ArrayList<>();
        NodeRef nodeRef = Converter.createNodeRefFromId(id);
        if (versionService.isVersioned(nodeRef)) {
            org.alfresco.service.cmr.version.Version vHead = versionService.getCurrentVersion(nodeRef);

            Version headVersion = new Version();
            headVersion.setVersionLabel(vHead.getVersionLabel());
            Node node = nodesApi.getNode(vHead.getFrozenStateNodeRef());
            setAdditionalNodeProperties(node, vHead);
            headVersion.setNode(node);
            headVersion.setNotes(vHead.getDescription() != null ? vHead.getDescription() : "");
            result.add(headVersion);

            NodeRef nextVersionRef = vHead.getFrozenStateNodeRef();
            VersionHistory history = versionService.getVersionHistory(nextVersionRef);
            org.alfresco.service.cmr.version.Version previousVersion = vHead;

            for (int i = 0; i < 9; i++) {
                previousVersion = history.getPredecessor(previousVersion);

                if (previousVersion != null) {

                    Version precedingVersion = new Version();
                    precedingVersion.setVersionLabel(previousVersion.getVersionLabel());
                    Node previousNode = nodesApi.getNode(previousVersion.getFrozenStateNodeRef());
                    setAdditionalNodeProperties(previousNode, previousVersion);
                    precedingVersion.setNode(previousNode);
                    precedingVersion.setNotes(
                            previousVersion.getDescription() != null ? previousVersion.getDescription() : "");
                    result.add(precedingVersion);
                } else {
                    break;
                }
            }
        }

        return result;
    }

    private void setAdditionalNodeProperties(
            Node node, org.alfresco.service.cmr.version.Version version) {
        // set additional version properties
        String dateString = ISO8601DateFormat.format((Date) version.getVersionProperty("modified"));
        node.getProperties().put("modified", dateString);
        node.getProperties()
                .put(
                        "originalContainerId",
                        nodeService.getPrimaryParent(version.getVersionedNodeRef()).getParentRef().getId());
    }

    public void setActionService(ActionService actionService)
	{
		this.actionService = actionService;
	}
    
    /**
     * @return the metadataManager
     */
    public MetadataManager getMetadataManager() {
        return metadataManager;
    }

    /**
     * @param metadataManager the metadataManager to set
     */
    public void setMetadataManager(MetadataManager metadataManager) {
        this.metadataManager = metadataManager;
    }

    /**
     * @return the versionService
     */
    public VersionService getVersionService() {
        return versionService;
    }

    /**
     * @param versionService the versionService to set
     */
    public void setVersionService(VersionService versionService) {
        this.versionService = versionService;
    }

    /**
     * @param permissionService the permissionService to set
     */
    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    /**
     * @param dictionaryService the dictionaryService to set
     */
    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    /**
     * @param nodeService the nodeService to set
     */
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
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

    @Override
    public Version contentIdVersionsVersionIdGet(String id, String versionId, String language) {

        Version result = null;
        NodeRef nodeRef = Converter.createNodeRefFromId(id);
        if (versionService.isVersioned(nodeRef)) {
            VersionHistory vhNode = versionService.getVersionHistory(nodeRef);
            for (org.alfresco.service.cmr.version.Version vTmp : vhNode.getAllVersions()) {

                if (vTmp.getFrozenStateNodeRef().getId().equals(versionId)) {
                    result = new Version();
                    result.setVersionLabel(vTmp.getVersionLabel());
                    result.setNode(nodesApi.getNode(vTmp.getFrozenStateNodeRef()));
                    result.setNotes(vTmp.getDescription() != null ? vTmp.getDescription() : "");
                }
            }
        }

        return result;
    }

    @Override
    public void contentIdDelete(String id) {
        NodeRef nodeRef = Converter.createNodeRefFromId(id);

        NodeRef igRoot = managementService.getCurrentInterestGroup(nodeRef);
        nodeService.setProperty(nodeRef, CircabcModel.PROP_IG_ROOT_NODE_ID_ARCHIVED, igRoot.getId());

        if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_MULTILINGUAL_DOCUMENT)) {
            NodeRef pivotRef = multilingualContentService.getPivotTranslation(nodeRef);
            if (pivotRef.getId().equals(nodeRef.getId())) {
                Map<Locale, NodeRef> modelTrans = multilingualContentService.getTranslations(nodeRef);
                for (Entry<Locale, NodeRef> entry : modelTrans.entrySet()) {
                    NodeRef tmpRef = entry.getValue();
                    if (!tmpRef.equals(nodeRef)) {
                        this.nodeService.deleteNode(tmpRef);
                    }
                }
            }
        }

        this.nodeService.deleteNode(nodeRef);
    }

    @Override
    public Translations contentIdTranslationsGet(String id) {
        NodeRef nodeRef = Converter.createNodeRefFromId(id);
        Map<Locale, NodeRef> modelTrans = multilingualContentService.getTranslations(nodeRef);
        Translations result = new Translations();
        for (Entry<Locale, NodeRef> entry : modelTrans.entrySet()) {
            NodeRef tmpRef = entry.getValue();
            Node tmpNode = nodesApi.getNode(tmpRef);
            result.getTranslations().add(tmpNode);
        }

        result.setPivot(nodesApi.getNode(multilingualContentService.getPivotTranslation(nodeRef)));

        return result;
    }

    /**
     * @return the translationService
     */
    public TranslationService getTranslationService() {
        return translationService;
    }

    /**
     * @param translationService the translationService to set
     */
    public void setTranslationService(TranslationService translationService) {
        this.translationService = translationService;
    }

    /**
     * @return the multilingualContentService
     */
    public MultilingualContentService getMultilingualContentService() {
        return multilingualContentService;
    }

    /**
     * @param multilingualContentService the multilingualContentService to set
     */
    public void setMultilingualContentService(MultilingualContentService multilingualContentService) {
        this.multilingualContentService = multilingualContentService;
    }

    @Override
    public void contentIdPut(String id, Node body) {
        NodeRef nodeRef = Converter.createNodeRefFromId(id);

        this.nodeService.setProperty(nodeRef, ContentModel.PROP_NAME, body.getName().trim());
        this.nodeService.setProperty(
                nodeRef, ContentModel.PROP_TITLE, Converter.toMLText(body.getTitle()));
        this.nodeService.setProperty(
                nodeRef, ContentModel.PROP_DESCRIPTION, Converter.toMLText(body.getDescription()));

        setDateProperty(body, nodeRef, ISSUE_DATE, DocumentModel.PROP_ISSUE_DATE);
        setDateProperty(body, nodeRef, EXPIRATION_DATE, DocumentModel.PROP_EXPIRATION_DATE);

        this.nodeService.setProperty(
                nodeRef, DocumentModel.PROP_REFERENCE, body.getProperties().get(REFERENCE));
        this.nodeService.setProperty(
                nodeRef, DocumentModel.PROP_STATUS, body.getProperties().get(STATUS));
        String security = body.getProperties().get(SECURITY);
        if (security != null && security.length() > 0) {
            this.nodeService.setProperty(nodeRef, DocumentModel.PROP_SECURITY_RANKING, security);
        }
        this.nodeService.setProperty(
                nodeRef, ContentModel.PROP_AUTHOR, body.getProperties().get(AUTHOR));

        for (int i = 1; i < 21; i++) {
            if (body.getProperties().get(DYN_ATTR + i) != null) {
                QName q = QName.createQName(DocumentModel.CIRCABC_DOCUMENT_MODEL_1_0_URI, DYN_ATTR + i);
                this.nodeService.setProperty(nodeRef, q, body.getProperties().get(DYN_ATTR + i));
            }
        }

        // in case it's a URL
        if (nodeService.hasAspect(nodeRef, DocumentModel.ASPECT_URLABLE)
                && body.getProperties().get(URL) != null) {
            String url = body.getProperties().get(URL);
            this.nodeService.setProperty(nodeRef, DocumentModel.PROP_URL, url);
        } else {
            ContentData cData = (ContentData) nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT);
            
            //FIX BUG DIGITCIRACB-4844 - ContentData.setEncoding and ContentData.setMimetype methods return a new object          
            cData = ContentData.setEncoding(cData, body.getProperties().get(ENCODING));
            cData = ContentData.setMimetype(cData, body.getProperties().get(MIMETYPE));
            
            //FIX BUG DIGITCIRACB-4844 - Save the updated cData
            this.nodeService.setProperty(nodeRef, ContentModel.PROP_CONTENT,cData);
        }
    }

    private void setDateProperty(
            Node body, NodeRef nodeRef, String propertyName, QName propertyQName) {
        try {
            if (!body.getProperties().get(propertyName).equals("")) {
                if (body.getProperties().get(propertyName).equals("null")
                        || body.getProperties().get(propertyName) == null) {
                    this.nodeService.setProperty(nodeRef, propertyQName, null);
                } else {
                    this.nodeService.setProperty(
                            nodeRef,
                            propertyQName,
                            Converter.convertStringToDate(body.getProperties().get(propertyName)));
                }
            }
        } catch (ParseException e) {
            logger.error("Invalid issue date:" + body.getProperties().get(propertyName), e);
        }
    }

    @Override
    public List<Node> contentIdTopicsGet(String id) {
        NodeRef docRef = Converter.createNodeRefFromId(id);
        List<Node> result = new ArrayList<>();

        if (nodeService.exists(docRef)) {

            List<ChildAssociationRef> children = nodeService.getChildAssocs(docRef);
            NodeRef discussionRef = null;
            for (ChildAssociationRef childAssoc : children) {
                if (childAssoc
                        .getQName()
                        .equals(QName.createQName(NamespaceService.FORUMS_MODEL_1_0_URI, DISCUSSION))) {
                    discussionRef = childAssoc.getChildRef();
                }
            }

            if (discussionRef != null) {
                List<ChildAssociationRef> topics = nodeService.getChildAssocs(discussionRef);
                for (ChildAssociationRef item : topics) {

                    if (nodeService.getType(item.getChildRef()).equals(ForumModel.TYPE_TOPIC)) {
                        final NodeRef childRef = item.getChildRef();
                        result.add(nodesApi.getNode(childRef));
                    }
                }
            }
        }

        return result;
    }

    @Override
    public Node contentIdTopicsPost(String id, Node body) {
        NodeRef docRef = Converter.createNodeRefFromId(id);
        
        if (!(nodeService.getType(docRef).equals(ContentModel.TYPE_CONTENT)
                || (nodeService.getType(docRef).equals(ContentModel.TYPE_FOLDER)))) {
            throw new InvalidTypeException("The node " + docRef + " does not have type content",
                    nodeService.getType(docRef));
        }

        List<ChildAssociationRef> children = nodeService.getChildAssocs(docRef);
        NodeRef discussionRef = null;
        for (ChildAssociationRef childAssoc : children) {
            if (childAssoc
                    .getQName()
                    .equals(QName.createQName(NamespaceService.FORUMS_MODEL_1_0_URI, DISCUSSION))) {
                discussionRef = childAssoc.getChildRef();
            }
        }

        // not any discussion node yet created
        if (discussionRef == null) {
            discussionRef =
                    nodeService
                            .createNode(
                                    docRef,
                                    ForumModel.ASSOC_DISCUSSION,
                                    QName.createQName(NamespaceService.FORUMS_MODEL_1_0_URI, DISCUSSION),
                                    ForumModel.TYPE_FORUM)
                            .getChildRef();
        }

        Map<QName, Serializable> props = new HashMap<>();
        props.put(ContentModel.PROP_NAME, body.getName());
        NodeRef topicRef =
                nodeService
                        .createNode(
                                discussionRef,
                                ContentModel.ASSOC_CONTAINS,
                                QName.createQName(NamespaceService.FORUMS_MODEL_1_0_URI, body.getName()),
                                ForumModel.TYPE_TOPIC,
                                props)
                        .getChildRef();

        return nodesApi.getNode(topicRef);
    }

    @Override
    public Node contentIdTranslationsPost(
            String id, String lang, InputStream file, String mimeType, String fileName) {

        NodeRef nodeRef = Converter.createNodeRefFromId(id);

        if (!nodeService.hasAspect(nodeRef, ContentModel.ASPECT_MULTILINGUAL_DOCUMENT)) {
            throw new InvalidAspectException(
                    THIS_NODE_IS_NOT_MULTILINGUAL, ContentModel.ASPECT_MULTILINGUAL_DOCUMENT);
        }

        NodeRef parentRef = nodeService.getPrimaryParent(nodeRef).getParentRef();

        String uniqueName = generateUniqueFilename(parentRef, fileName);

        NodeRef createdRef = createContent(parentRef, file, mimeType, uniqueName);
        Map<QName, Serializable> props = new HashMap<>();
        props.put(ContentModel.PROP_AUTHOR, "");
        nodeService.addAspect(createdRef, ContentModel.ASPECT_AUTHOR, props);
        props = new HashMap<>();
        nodeService.addAspect(createdRef, ContentModel.ASPECT_OWNABLE, props);

        multilingualContentService.addTranslation(createdRef, nodeRef, I18NUtil.parseLocale(lang));

        return null;
    }

    @Override
    public Node requestMachineTranslation(String id, String lang, boolean notify) {
        NodeRef nodeRef = Converter.createNodeRefFromId(id);

        String sourceLanguage = nodeService.getProperty(nodeRef, ContentModel.PROP_LOCALE).toString();

        if (!nodeService.hasAspect(nodeRef, ContentModel.ASPECT_MULTILINGUAL_DOCUMENT)) {
            throw new InvalidAspectException(
                    THIS_NODE_IS_NOT_MULTILINGUAL, ContentModel.ASPECT_MULTILINGUAL_DOCUMENT);
        }

        NodeRef copyOfDocument;
        {
            UserTransaction trx =
                    serviceRegistry.getTransactionService().getNonPropagatingUserTransaction(false);
            try {

                trx.begin();
                copyOfDocument = getTranslationService().copyDocumentToBeTranslated(nodeRef);
                Long dbId = (Long) nodeService.getProperty(nodeRef, ContentModel.PROP_NODE_DBID);
                String name = (String) nodeService.getProperty(copyOfDocument, ContentModel.PROP_NAME);
                int lastIndexOf = name.lastIndexOf('.');
                String newName = String.valueOf(dbId);
                if (lastIndexOf > -1) {
                    String extension = name.substring(lastIndexOf + 1);
                    newName = newName + "." + extension;
                }
                if (!getTranslationService().canBeTranslated(name)) {
                    throw new InvalidOperationException("File type extension: " + name + " is not supported");
                }

                if (!getTranslationService().getAvailableLanguages().contains(lang.toUpperCase())) {
                    throw new InvalidOperationException("Language: " + lang + " is not supported");
                }

                nodeService.setProperty(copyOfDocument, ContentModel.PROP_NAME, newName);
                trx.commit();
            } catch (Throwable e) {
                if (logger.isErrorEnabled()) {
                    logger.error("Try to rollback transaction" + e);
                }
                try {
                    trx.rollback();
                } catch (SystemException e1) {
                    if (logger.isErrorEnabled()) {
                        logger.error("Can not rollback transaction" + e1);
                    }
                }
                throw new InvalidOperationException("Can not copy document");
            }
        }

        Set<String> languages = new HashSet<>(1);
        languages.add(lang.toUpperCase());
        if (copyOfDocument != null) {
            {
                UserTransaction trx =
                        serviceRegistry.getTransactionService().getNonPropagatingUserTransaction(false);
                try {
                    trx.begin();
                    getTranslationService()
                            .translateDocument(
                                    nodeRef, copyOfDocument, sourceLanguage.toUpperCase(), languages, notify);
                    trx.commit();
                } catch (Throwable e) {
                    if (logger.isErrorEnabled()) {
                        logger.error("Try to rollback  transaction" + e);
                    }
                    try {
                        trx.rollback();
                    } catch (SystemException e1) {
                        if (logger.isErrorEnabled()) {
                            logger.error("Can not rollback transaction" + e1);
                        }
                    }
                    throw new InvalidOperationException("Can request translation of document");
                }
            }
        }
        return null;
    }

    private NodeRef createContent(
            NodeRef parentRef, InputStream file, String mimeType, String fileName) {
        NodeRef nodeRef;

        try {
            QName associationNameQName =
                    QName.createQName(ContentModel.PROP_NAME.getNamespaceURI(), fileName);
            policyBehaviourFilter.disableBehaviour(parentRef, ContentNotifyAspect.ASPECT_CONTENT_NOTIFY);
            nodeRef =
                    nodeService
                            .createNode(
                                    parentRef,
                                    ContentModel.ASSOC_CONTAINS,
                                    associationNameQName,
                                    ContentModel.TYPE_CONTENT)
                            .getChildRef();
            nodeService.setProperty(nodeRef, ContentModel.PROP_NAME, fileName);

            QName propContent = UtilsCircabc.getPropContent(nodeService.getType(nodeRef));

            final ContentWriter writer = contentService.getWriter(nodeRef, propContent, true);
            writer.setMimetype(mimeType);
            writer.putContent(file);

        } finally {
            policyBehaviourFilter.enableBehaviour(parentRef, ContentNotifyAspect.ASPECT_CONTENT_NOTIFY);
        }

        return nodeRef;
    }

    private String generateUniqueFilename(NodeRef parentRef, String filename) {
        Integer counter = 1;
        while (nodeService.getChildByName(parentRef, ContentModel.ASSOC_CONTAINS, filename) != null) {
            String cleanName = filename.trim();

            if (cleanName.contains(".")) {
                String leftSideName = cleanName.substring(0, cleanName.lastIndexOf('.'));
                String rightSideName = cleanName.substring(cleanName.lastIndexOf('.'));

                if (cleanName.matches(".*\\([0-9]*\\)\\..*")) {
                    leftSideName = cleanName.substring(0, cleanName.lastIndexOf('('));
                    filename = MessageFormat.format("{0}({1}){2}", leftSideName, counter, rightSideName);
                } else {
                    filename = MessageFormat.format("{0}({1}){2}", leftSideName, counter, rightSideName);
                }

            } else {
                filename = MessageFormat.format("{0}({1})", cleanName, counter);
            }

            counter += 1;
        }

        return filename;
    }

    public ContentService getContentService() {
        return contentService;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    public BehaviourFilter getPolicyBehaviourFilter() {
        return policyBehaviourFilter;
    }

    public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
        this.policyBehaviourFilter = policyBehaviourFilter;
    }

    @Override
    public void contentIdMultilingualAspectPost(String id, MultilingualAspectMetadata body) {

        NodeRef nodeRef = Converter.createNodeRefFromId(id);

        Map<QName, Serializable> props = nodeService.getProperties(nodeRef);
        props.remove(ContentModel.PROP_LOCALE);

        // Added to warn about integrity errors instead of throwing the
        // exception (Alfresco 4)
        if (!IntegrityChecker.isWarnInTransaction()) {
            IntegrityChecker.setWarnInTransaction();
        }

        // Do the jobs
        final Locale locale = I18NUtil.parseLocale(body.getPivotLang());

        // https://webgate.ec.europa.eu/CITnet/jira/browse/DIGIT-CIRCABC-2290
        nodeService.removeAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE);

        // make this node multilingual
        multilingualContentService.makeTranslation(nodeRef, locale);
        final NodeRef mlContainer = getMultilingualContentService().getTranslationContainer(nodeRef);

        // backup and reaply properties of non i18n node
        nodeService.addProperties(nodeRef, props);

        // if the author of the node is not set, set it with the default author
        // name of
        // the new ML Container
        String nodeAuthor = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_AUTHOR);

        if (nodeAuthor == null || nodeAuthor.length() < 1 && body.getAuthor() != null) {
            nodeService.setProperty(nodeRef, ContentModel.PROP_AUTHOR, body.getAuthor());
        }

        // set properties of the ml container
        nodeService.setProperty(mlContainer, ContentModel.PROP_AUTHOR, body.getAuthor());

        // https://webgate.ec.europa.eu/CITnet/jira/browse/DIGIT-CIRCABC-2290
        nodeService.addAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE, null);
    }

    /**
     * @see ContentApi#checkEditInOffice(String[])
     */
    @Override
    public List<OfficeEditResult> checkEditInOffice(String[] nodeIds) {

        List<OfficeEditResult> results = new ArrayList<>();

        for (String nodeId : nodeIds) {
            NodeRef nodeRef = Converter.createNodeRefFromId(nodeId);
            if (!nodeService.exists(nodeRef)) {
                // if the item does not exist or has been deleted, return
                return results;
            }
            results.add(checkEditInOffice(nodeRef));
        }

        return results;
    }

    private OfficeEditResult checkEditInOffice(NodeRef nodeRef) {

        OfficeEditResult result = new OfficeEditResult();

        result.setId(nodeRef.getId());

        // check if the current user has write permission on the node
        if (permissionService.hasPermission(nodeRef, PermissionService.WRITE) == AccessStatus.DENIED) {
            result.setErrorDescription("1: Write permission denied. Cannot edit in Office.");
            return result;
        }

        // get all content definitions
        Collection<QName> contentQNames =
                dictionaryService.getAllProperties(DataTypeDefinition.CONTENT);

        QName nodeType = nodeService.getType(nodeRef);

        // container check
        if (isContainer(nodeType)) {
            result.setErrorDescription("2: Node is a container (folder).");
            return result;
        }

        TypeDefinition typeDefinition = dictionaryService.getType(nodeType);

        Map<QName, PropertyDefinition> propertyDefinitions = typeDefinition.getProperties();
        boolean contentItem = false;

        // Iterate through all content definitions until we find the actual
        // content property to extract. This step is necessary because CIRCABC
        // uses default and custom content properties, which means that the
        // actual content could be in a different property than cm:content
        // (example: newsgroup attachments)
        for (QName contentQName : contentQNames) {

            // check if the property exists for the nodeRef's type
            if (propertyDefinitions.containsKey(contentQName)) {

                // check if the document is an Office document
                ContentReader reader = contentService.getReader(nodeRef, contentQName);

                if (reader != null && !officeMimetypes.contains(reader.getMimetype())) {
                    result.setErrorDescription(
                            "3: Not an Office document. Mimetype: " + reader.getMimetype());
                    return result;
                }

                if (isLocked(nodeRef)) {
                    result.setErrorDescription("4: Document is locked.");
                    return result;
                }

                if (checkOutCheckInService.getWorkingCopy(nodeRef) != null) {
                    result.setErrorDescription("5: Document has working copies.");
                    return result;
                }

                if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY)) {
                    result.setErrorDescription("6: Document is a working copy.");
                    return result;
                }

                contentItem = true;
                break;
            }
        }

        if (!contentItem) {
            result.setErrorDescription("7: Document's content property could not be found.");
            return result;
        }

        String documentLocation = getDocumentLocation(nodeRef);

        if (documentLocation.isEmpty()) {
            result.setErrorDescription("8: Document's content could not be retrieved.");
            return result;
        }

        // finally provide document to open
        result.setCanEdit(true);
        result.setDocumentLocation(documentLocation);

        return result;
    }

    private boolean isContainer(QName nodeType) {
        return dictionaryService.isSubClass(nodeType, ContentModel.TYPE_FOLDER)
                && !dictionaryService.isSubClass(nodeType, ContentModel.TYPE_SYSTEM_FOLDER);
    }

    private boolean isLocked(NodeRef nodeRef) {
        LockStatus status = lockService.getLockStatus(nodeRef);
        return status == LockStatus.LOCKED || status == LockStatus.LOCK_OWNER;
    }

    private String getDocumentLocation(NodeRef nodeRef) {

        String url = "";

        try {
            List<String> paths = fileFolderService.getNameOnlyPath(null, nodeRef);

            // build up the webdav url
            StringBuilder path = new StringBuilder();

            // build up the path skipping the first path as it is the root
            // folder
            for (int i = 1; i < paths.size(); i++) {
                path.append("/").append(URLEncoder.encode(paths.get(i)));
            }

            url = path.toString();
        } catch (Exception e) {
            // return empty url
        }

        return url;
    }

    /**
     * @see io.swagger.api.ContentApi#checkPreview(java.lang.String)
     */
    @Override
    public PreviewResult checkPreview(String documentId) {

        // first check if the document is already a PDF
        NodeRef documentNodeRef = Converter.createNodeRefFromId(documentId);

        ContentReader reader = contentService.getReader(documentNodeRef, ContentModel.PROP_CONTENT);

        if (reader != null && MimetypeMap.MIMETYPE_PDF.equals(reader.getMimetype())) {
            return new PreviewResult(true, "", "", reader.getSize());
        }

        // else check if it has been converted
        NodeRef renditionNodeRef = circabcRenditionHelper.getRenditionNodeRef(documentNodeRef);

        if (renditionNodeRef != null) {

            reader = contentService.getReader(renditionNodeRef, ContentModel.PROP_CONTENT);

            return new PreviewResult(true, "", "", reader.getSize());
        }

        // finally check if it has been submitted for conversion
        List<Request> requests = renditionDaoService.getRequestsForDocument(documentId);

        if (requests == null || requests.isEmpty()) {
            return new PreviewResult(false, "Send document to convert.", "send.to.convert", 0);
        }

        for (Request request : requests) {
            if (request.isSuccess()) {
                // will it come to this point???
                return new PreviewResult(true, "", "", 0);
            } else if (request.getEndProcessingDate() == null
                    || request.getRemainingRenderRetries() > 0) {
                return new PreviewResult(false, "Document being processed.", "processing", 0);
            }
        }

        return new PreviewResult(false, "No preview available.", "not.available", 0);
    }

    public void buildZip(String[] nodeIds, OutputStream outputStream)
            throws IOException, XMLStreamException {

        FileInputStream inputStream = null;

        try {

            List<NodeRef> nodeRefs = new ArrayList<>();

            for (String nodeId : nodeIds) {
                NodeRef nodeRef = Converter.createNodeRefFromId(nodeId);

                if (!nodeService.exists(nodeRef)) {
                    throw new IllegalArgumentException(nodeId + " is not a valid node id.");
                }

                if (currentUserPermissionCheckerService.hasAlfrescoReadPermission(nodeId)) {
                    nodeRefs.add(nodeRef);
                } else {
                    logger.warn(
                            "The node "
                                    + nodeId
                                    + " has been ignored for the bulk download. User does not have read permission on node");
                }
            }

            File tempZipFile =
                    TempFileProvider.createTempFile("bulk", ".zip", TempFileProvider.getTempDir());
            File tempIndexFile =
                    TempFileProvider.createTempFile("bulk", ".txt", TempFileProvider.getTempDir());

            final List<IndexRecord> indexRecords = bulkService.getMetaData(nodeRefs);

            indexService.generateIndexRecords(tempIndexFile, indexRecords);

            zipService.addingFileIntoArchive(nodeRefs, tempZipFile, tempIndexFile);

            inputStream = new FileInputStream(tempZipFile);
            IOUtils.copy(inputStream, outputStream);

        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    /**
     * @see ContentApi#createDownloadableZip(String[], String)
     */
    @Override
    public NodeRef createDownloadableZip(String[] nodeIds, String parentId)
            throws IOException, XMLStreamException {

        if (!currentUserPermissionCheckerService.hasAlfrescoWritePermission(parentId)) {
            throw new AccessDeniedException(
                    "You don't have permission to write the content ZIP file in this space.");
        }

        NodeRef parentRef = Converter.createNodeRefFromId(parentId);

        File tempFile = null;

        FileOutputStream fileOutputStream = null;

        try {

            tempFile = File.createTempFile("contents-", GUID.generate());

            fileOutputStream = new FileOutputStream(tempFile);

            buildZip(nodeIds, fileOutputStream);

            try (FileInputStream fileInputStream = new FileInputStream(tempFile)) {

                NodeRef contentNodeRef =
                        createContent(
                                parentRef,
                                fileInputStream,
                                "application/zip;charset=UTF-8",
                                "contents-" + GUID.generate() + ".zip");

                nodeService.addAspect(contentNodeRef, CircabcModel.ASPECT_LIBRARY, null);

                return contentNodeRef;
            }
        } finally {
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            if (tempFile != null) {
                tempFile.deleteOnExit();
            }
        }
    }

    /**
     * @return the managementService
     */
    public ManagementService getManagementService() {
        return managementService;
    }

    /**
     * @param managementService the managementService to set
     */
    public void setManagementService(ManagementService managementService) {
        this.managementService = managementService;
    }

    /**
     * @param lockService the lockService to set
     */
    public void setLockService(LockService lockService) {
        this.lockService = lockService;
    }

    /**
     * @param checkOutCheckInService the checkOutCheckInService to set
     */
    public void setCheckOutCheckInService(CheckOutCheckInService checkOutCheckInService) {
        this.checkOutCheckInService = checkOutCheckInService;
    }

    /**
     * @param fileFolderService the fileFolderService to set
     */
    public void setFileFolderService(FileFolderService fileFolderService) {
        this.fileFolderService = fileFolderService;
    }

    /**
     * @param renditionDaoService the renditionDaoService to set
     */
    public void setRenditionDaoService(RenditionDaoService renditionDaoService) {
        this.renditionDaoService = renditionDaoService;
    }

    /**
     * @param circabcRenditionHelper the circabcRenditionHelper to set
     */
    public void setCircabcRenditionHelper(CircabcRenditionHelper circabcRenditionHelper) {
        this.circabcRenditionHelper = circabcRenditionHelper;
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    /**
     * @param zipService the zipService to set
     */
    public void setZipService(ZipService zipService) {
        this.zipService = zipService;
    }

    /**
     * @param indexService the indexService to set
     */
    public void setIndexService(IndexService indexService) {
        this.indexService = indexService;
    }

    /**
     * @param bulkService the bulkService to set
     */
    public void setBulkService(BulkService bulkService) {
        this.bulkService = bulkService;
    }

    /**
     * @param currentUserPermissionCheckerService the currentUserPermissionCheckerService to set
     */
    public void setCurrentUserPermissionCheckerService(
            CurrentUserPermissionCheckerService currentUserPermissionCheckerService) {
        this.currentUserPermissionCheckerService = currentUserPermissionCheckerService;
    }

    public void setCircabcRenditionService(CircabcRenditionService circabcRenditionService) {
        this.circabcRenditionService = circabcRenditionService;
    }

    /**
     * @param logService the logService to set
     */
    public void setLogService(LogService logService) {
        this.logService = logService;
    }

    @Override
    public NodeRef createContent(
            String parentId,
            String name,
            I18nProperty title,
            I18nProperty description,
            String author,
            String reference,
            String securityRanking,
            String status,
            String[] keywords,
            Date expiration,
            String mimetype,
            InputStream file,
            Boolean isPivot,
            String lang,
            Map<String, Object> dynProps) {
    	
    	//BUG DIGITCIRCABC-4849
    	//If mime type returned by browser is binary, check if it is the right type!
    	if(MimetypeMap.MIMETYPE_BINARY.equals(mimetype)) {
    		String guessMimeType = metadataManager.guessMimetype(name);
    		//check if the guessed mime type is not binary
    		if(!guessMimeType.equals(mimetype)) {
    			logger.warn("Wrong mime type for file "+name+"! change it from "+mimetype+" to "+guessMimeType);
    			mimetype=guessMimeType;
    		}
    	}

        NodeRef parentRef = Converter.createNodeRefFromId(parentId);
        if (!nodeService.exists(parentRef)) {
            throw new InvalidNodeRefException("parent does not exist", parentRef);
        }
        if (!(nodeService.getType(parentRef).equals(ContentModel.TYPE_FOLDER)
                || nodeService.getType(parentRef).equals(CircabcModel.TYPE_INFORMATION_NEWS))) {
            throw new InvalidTypeException(
                    "parent does not have a correct type" + parentRef, ContentModel.TYPE_FOLDER);
        }

        boolean wasEnabled = !policyBehaviourFilter.isEnabled();
        policyBehaviourFilter.disableBehaviour(ContentNotifyAspect.ASPECT_CONTENT_NOTIFY);
        String newfilename = generateUniqueFilename(parentRef, name);
        NodeRef nodeRef =
                createContentInternal(
                        parentRef,
                        newfilename,
                        title,
                        description,
                        author,
                        reference,
                        securityRanking,
                        status,
                        keywords,
                        expiration,
                        mimetype,
                        file,
                        dynProps);

        if (Boolean.TRUE.equals(isPivot)) {
            MultilingualAspectMetadata body = new MultilingualAspectMetadata();
            if (!isNullEmptyString(author)) {
                body.setAuthor(author);
            } else {
                body.setAuthor("");
            }
            body.setPivotLang(lang);
            contentIdMultilingualAspectPost(nodeRef.getId(), body);
            // reaplyexpiration
        }

        final LogRecord logRecord = logService.prepareLogUploadRequest(nodeRef, name);
        logRecord.setOK(true);
        logService.log(logRecord);

        if (wasEnabled) {
            policyBehaviourFilter.enableBehaviour(ContentNotifyAspect.ASPECT_CONTENT_NOTIFY);
        }

        if (nodeRef != null) {
            circabcRenditionService.addRequest(nodeRef);
        }

        return nodeRef;
    }

    private NodeRef createContentInternal(
            NodeRef parentRef,
            String newfilename,
            I18nProperty title,
            I18nProperty description,
            String author,
            String reference,
            String securityRanking,
            String status,
            String[] keywords,
            Date expiration,
            String mimetype,
            InputStream file,
            Map<String, Object> dynProps) {

        QName associationNameQName =
                QName.createQName(ContentModel.PROP_NAME.getNamespaceURI(), newfilename);

        Map<QName, Serializable> props = new HashMap<>();
        props.put(ContentModel.PROP_NAME, newfilename);

        if (title != null) {
            props.put(ContentModel.PROP_TITLE, Converter.toMLText(title));
        }

        if (description != null) {
            props.put(ContentModel.PROP_DESCRIPTION, Converter.toMLText(description));
        }

        boolean isAuthorSet = false;
        if (!isNullEmptyString(author)) {
            props.put(ContentModel.PROP_AUTHOR, author);
            isAuthorSet = true;
        }

        NodeRef nodeRef =
                nodeService
                        .createNode(
                                parentRef,
                                ContentModel.ASSOC_CONTAINS,
                                associationNameQName,
                                ContentModel.TYPE_CONTENT,
                                props)
                        .getChildRef();

        Map<QName, Serializable> bprops = new HashMap<>();

        if (expiration != null) {
            bprops.put(DocumentModel.PROP_EXPIRATION_DATE, expiration);
        }

        if (!isNullEmptyString(securityRanking)) {
            bprops.put(DocumentModel.PROP_SECURITY_RANKING, securityRanking);
        } else {
            bprops.put(DocumentModel.PROP_SECURITY_RANKING, DocumentModel.SECURITY_RANKINGS_NORMAL);
        }

        nodeService.addAspect(nodeRef, DocumentModel.ASPECT_BPROPERTIES, bprops);

        Map<QName, Serializable> cprops = new HashMap<>();

        if (!isNullEmptyString(reference)) {
            cprops.put(DocumentModel.PROP_REFERENCE, reference);
        }

        if (!isNullEmptyString(status)) {
            cprops.put(DocumentModel.PROP_STATUS, status);
        } else {
            cprops.put(DocumentModel.PROP_STATUS, DocumentModel.STATUS_VALUE_DRAFT);
        }

        if (dynProps != null) {
            for (Entry<String, Object> entry : dynProps.entrySet()) {
                if (entry.getValue() != null && entry.getValue() != "null") {
                    QName q = QName.createQName(DocumentModel.CIRCABC_DOCUMENT_MODEL_1_0_URI, entry.getKey());

                    if (entry.getValue() instanceof org.json.simple.JSONArray) {
                        Serializable value = entry
                            .getValue()
                            .toString()
                            .replace("[\"", "")
                            .replaceAll("\"]", "")
                            .replace("\",\"", ",");
                        cprops.put(
                                q,value
                                );
                    } else {
                        cprops.put(q, (Serializable) entry.getValue());
                    }
                }
            }
        }

        if (keywords != null) {
            Set<NodeRef> keywordsToAdd = new HashSet<>();
            for (String keywordId : keywords) {
                keywordsToAdd.add(Converter.createNodeRefFromId(keywordId));
            }
            cprops.put(DocumentModel.PROP_KEYWORD, (Serializable) keywordsToAdd);
        }

        nodeService.addAspect(nodeRef, DocumentModel.ASPECT_BPROPERTIES, cprops);

        ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);

        writer.setMimetype(mimetype);
        writer.putContent(file);

        if (!isAuthorSet) {
            Action action = actionService
                    .createAction(ContentMetadataExtracter.EXECUTOR_NAME);
            action.setExecuteAsynchronously(true);
            actionService.executeAction(action, nodeRef, true, false);

        }

        return nodeRef;
    }

    private boolean isNullEmptyString(String string) {
        return string == null || "".equals(string) || "null".equals(string);
    }

    @Override
    public NodeRef createContentTranslation(
            String id,
            String defaultName,
            I18nProperty title,
            I18nProperty description,
            String author,
            String reference,
            String securityRanking,
            String statusProp,
            String[] keywords,
            Date expirationDate,
            String mimeType,
            InputStream file,
            String lang,
            Map<String, Object> dynProps) {

        NodeRef nodeRef = Converter.createNodeRefFromId(id);

        if (!nodeService.hasAspect(nodeRef, ContentModel.ASPECT_MULTILINGUAL_DOCUMENT)) {
            throw new InvalidAspectException(
                    THIS_NODE_IS_NOT_MULTILINGUAL, ContentModel.ASPECT_MULTILINGUAL_DOCUMENT);
        }

        NodeRef parentRef = nodeService.getPrimaryParent(nodeRef).getParentRef();

        boolean wasEnabled = !policyBehaviourFilter.isEnabled();
        policyBehaviourFilter.disableBehaviour(ContentNotifyAspect.ASPECT_CONTENT_NOTIFY);
        String newfilename = generateUniqueFilename(parentRef, defaultName);
        NodeRef createdRef =
                createContentInternal(
                        parentRef,
                        newfilename,
                        title,
                        description,
                        author,
                        reference,
                        securityRanking,
                        statusProp,
                        keywords,
                        expirationDate,
                        mimeType,
                        file,
                        dynProps);
        Map<QName, Serializable> props = new HashMap<>();
        props.put(ContentModel.PROP_AUTHOR, author);
        nodeService.addAspect(createdRef, ContentModel.ASPECT_AUTHOR, props);
        props = new HashMap<>();
        nodeService.addAspect(createdRef, ContentModel.ASPECT_OWNABLE, props);

        multilingualContentService.addTranslation(createdRef, nodeRef, I18NUtil.parseLocale(lang));

        if (wasEnabled) {
            policyBehaviourFilter.enableBehaviour(ContentNotifyAspect.ASPECT_CONTENT_NOTIFY);
        }

        if (nodeRef != null) {
            circabcRenditionService.addRequest(nodeRef);
        }

        return createdRef;
    }
}
