/**
 * ***************************************************************************** Copyright 2006
 * European Community
 *
 * <p>Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 *
 * <p>https://joinup.ec.europa.eu/software/page/eupl
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * ****************************************************************************
 */
package eu.cec.digit.circabc.repo.struct;

import eu.cec.digit.circabc.aspect.ContentNotifyAspect;
import eu.cec.digit.circabc.config.CircabcConfiguration;
import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.model.DocumentModel;
import eu.cec.digit.circabc.model.EventModel;
import eu.cec.digit.circabc.model.SurveyModel;
import eu.cec.digit.circabc.repo.customisation.CustomizationException;
import eu.cec.digit.circabc.service.cmr.security.CircabcConstant;
import eu.cec.digit.circabc.service.customisation.NodePreferencesService;
import eu.cec.digit.circabc.service.customisation.nav.ColumnConfig;
import eu.cec.digit.circabc.service.customisation.nav.NavigationPreference;
import eu.cec.digit.circabc.service.customisation.nav.NavigationPreferencesService;
import eu.cec.digit.circabc.service.customisation.nav.ServiceConfig;
import eu.cec.digit.circabc.service.profile.*;
import eu.cec.digit.circabc.service.profile.permissions.*;
import eu.cec.digit.circabc.service.struct.ManagementService;
import eu.cec.digit.circabc.web.wai.dialog.customization.EditNavigationPreference;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.action.evaluator.IsSubTypeEvaluator;
import org.alfresco.repo.action.evaluator.NoConditionEvaluator;
import org.alfresco.repo.action.executer.AddFeaturesActionExecuter;
import org.alfresco.repo.search.QueryParameterDefImpl;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.NotAuditable;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.CompositeAction;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.rule.RuleType;
import org.alfresco.service.cmr.search.*;
import org.alfresco.service.cmr.search.CategoryService.Depth;
import org.alfresco.service.cmr.search.CategoryService.Mode;
import org.alfresco.service.cmr.security.*;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jcr.PathNotFoundException;
import java.io.Serializable;
import java.util.*;

/**
 * Implementation of the Main circabc management service. It serves to manage the particular
 * structure of primary Circabc Nodes
 *
 * <p>Company Home (The Alfresco Company Home) |_ Circabc [1.1] |_ Category (linked to a Category
 * Header) [1.n] |_ Interest Group Home (alias IgHome) [1.n] |_ Library (location of spaces and
 * documents) [1.1] |_ Newsgroups (location of forums) [1.1] |_ Surveys (Location of the IPM
 * surveys) [1.1] |_ Information (Location of the information about IG (IG Mini site)) [1.1] |_
 * Event (Location of the Circabc events (calendar)) [1.1] |_ <i>And other futur circabc services
 * (as Wiki? Blog?)</i>
 *
 * @author clincst
 * @author Yanick Pignot
 */
public class ManagementServiceImpl implements ManagementService {

    /**
     * The reference to the space store
     */
    public static final StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
    private static final MLText CATEGORY_ADMIN = new MLText(Locale.ENGLISH, "Category Administrator");
    private static final MLText CIRCABC_ADMIN_TITLE =
            new MLText(Locale.ENGLISH, "Circabc Administrator");
    private static final MLText ACCESS_TITLE = new MLText(Locale.ENGLISH, "Access");
    private static final MLText AUTHOR_TITLE = new MLText(Locale.ENGLISH, "Author");
    private static final MLText CONTRIB_TITLE = new MLText(Locale.ENGLISH, "Contributor");
    private static final MLText LEADER_TITLE = new MLText(Locale.ENGLISH, "Leader");
    private static final MLText REVIEWER_TITLE = new MLText(Locale.ENGLISH, "Reviewer");
    private static final MLText SECRETARY_TITLE = new MLText(Locale.ENGLISH, "Secretary");
    private static final MLText REGISTRED_TITLE = new MLText(Locale.ENGLISH, "Registered");
    private static final MLText ANONYMOUS_TITLE = new MLText(Locale.ENGLISH, "Guest");
    private static final String CIRCABC_DD_FOLDER = "CircaBC";
    private static final String ADMIN = "admin";
    /**
     * A logger for the class
     */
    private static final Log logger = LogFactory.getLog(ManagementServiceImpl.class);
    /**
     * The path to the company home
     */
    private static final String APP_COMPANY_HOME_PATH = "/app:company_home";
    /**
     * The path to the guest user home
     */
    private static final String GUEST_USER_HOME_PATH = "/app:company_home/app:guest_home";
    private static final String APP_ALFRESCO_DICTIONARY_LUCENE_PATH = "/app:dictionary";
    private static final QName PARAM_QNAME_PARENT =
            QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "parent");
    /**
     * Shallow search for all files and folders
     */
    private static final String LUCENE_QUERY_SHALLOW_ALL =
            "+PARENT:\"${cm:parent}\""
                    + "-TYPE:\""
                    + ContentModel.TYPE_SYSTEM_FOLDER
                    + "\" "
                    + "+("
                    + "TYPE:\""
                    + ContentModel.TYPE_CONTENT
                    + "\" "
                    + "TYPE:\""
                    + ContentModel.TYPE_FOLDER
                    + "\" "
                    + "TYPE:\""
                    + ContentModel.TYPE_LINK
                    + "\" "
                    + ")";
    /**
     * Shallow search for all files and folders
     */
    private static final String LUCENE_QUERY_SHALLOW_FOLDERS =
            "+PARENT:\"${cm:parent}\""
                    + "-TYPE:\""
                    + ContentModel.TYPE_SYSTEM_FOLDER
                    + "\" "
                    + "+TYPE:\""
                    + ContentModel.TYPE_FOLDER
                    + "\" ";
    /**
     * Shallow search for all files and folders
     */
    private static final String LUCENE_QUERY_SHALLOW_FILES =
            "+PARENT:\"${cm:parent}\""
                    + "-TYPE:\""
                    + ContentModel.TYPE_SYSTEM_FOLDER
                    + "\" "
                    + "+TYPE:\""
                    + ContentModel.TYPE_CONTENT
                    + "\" ";
    private static NodeRef companyHomeNodeRef = null;
    private static NodeRef guestUserHomeNodeRef = null;
    protected NodeService nodeService;
    protected PermissionService permissionService;
    protected OwnableService ownableService;
    protected AuthenticationService authenticationService;
    protected AuthorityService authorityService;
    protected FileFolderService fileFolderService;
    protected SearchService searchService;
    protected DictionaryService dictionaryService;
    protected MultilingualContentService multilingualContentService;
    protected ProfileManagerServiceFactory profileManagerServiceFactory;
    protected TenantService tenantService;
    protected ServiceRegistry serviceRegistry = null;
    protected NodePreferencesService nodePreferencesService = null;
    private ActionService actionService = null;
    private RuleService ruleService = null;
    private ContentService contentService = null;
    private CategoryService categoryService;
    private DataTypeDefinition dataTypeNodeRef;
    private NavigationPreferencesService navigationPreferencesService;
    private NamespacePrefixResolver namespacePrefixResolver = null;
    private NodeRef rootNode = null;
    /**
     * The cached value of the circabc root node xpath value
     */
    private String circabcLucenePath = null;
    /**
     * The cached value of the circabc root node xpath value
     */
    private String mtLucenePath = null;
    private String alfrescoDictionaryPath = null;
    /**
     * the cached value of the category header root node xpath vaule
     */
    private String categoryHeaderLucenePath = null;
    /**
     * The cached reference of the circabc root node
     */
    private NodeRef circabcNodeRef = null;
    /**
     * The cached reference of the machine translation root node
     */
    private NodeRef mtNodeRef = null;
    /**
     * The cached reference of the alfresco Dictonary node
     */
    private NodeRef alfrescoDictionary = null;
    /**
     * The cached reference of the Circabc Dictonary node
     */
    private NodeRef circabcDictionary = null;
    private NodeRef rootCategoryHeader = null;
    private SimplePath rootSimplePath;

    public void init() {
        dataTypeNodeRef = dictionaryService.getDataType(DataTypeDefinition.NODE_REF);
    }

    public SimplePath getRootSimplePath() throws PathNotFoundException {
        if (this.rootSimplePath == null) {
            this.rootSimplePath = new SimplePath(nodeService, getCircabcNodeRef());
        }

        return this.rootSimplePath;
    }

    public SimplePath getNodePath(final NodeRef nodeRef) throws PathNotFoundException {
        // TODO move this logic in SimplePath ...
        // TODO make SimplePath contructor accpeting only CircabcServiceRegistry
        // TODO make SimplePath constructor visibility as PACKAGE
        // TODO make all services and bean that construct SimplePath using this servive !!!!

        final QName type = nodeService.getType(nodeRef);

        if (type.equals(CircabcModel.TYPE_CATEGORY_HEADER)) {
            return new SimplePath(nodeService, getCircabcNodeRef());
        } else if (type.equals(ContentModel.TYPE_MULTILINGUAL_CONTAINER)) {
            final NodeRef pivotTranslation = multilingualContentService.getPivotTranslation(nodeRef);
            final ChildAssociationRef pivotFolder = nodeService.getPrimaryParent(pivotTranslation);
            return new SimplePath(nodeService, pivotFolder.getParentRef(), nodeRef);
        } else if (EventModel.TYPE_EVENT.equals(type)) {
            final ChildAssociationRef parent = nodeService.getPrimaryParent(nodeRef);
            final ChildAssociationRef parentparent = nodeService.getPrimaryParent(parent.getParentRef());
            final ChildAssociationRef calendarService =
                    nodeService.getPrimaryParent(parentparent.getParentRef());
            return new SimplePath(nodeService, calendarService.getParentRef(), nodeRef);
        } else {
            return new SimplePath(nodeService, nodeRef);
        }
    }

    public List<SimplePath> getChildsPath(final NodeRef nodeRef) throws PathNotFoundException {
        final List<SimplePath> list = new ArrayList<>();
        final List<ChildAssociationRef> childs = nodeService.getChildAssocs(nodeRef);
        for (final ChildAssociationRef child : childs) {
            list.add(new SimplePath(nodeService, child.getChildRef()));
        }
        return list;
    }

    public SimplePath getNodePath(final String pathString) throws PathNotFoundException {
        if (pathString == null) {
            throw new PathNotFoundException("The path as String is a mandatory parameter");
        } else if (!pathString.toLowerCase().startsWith(getRootSimplePath().toString().toLowerCase())) {
            throw new PathNotFoundException(
                    "The given path is not managed by circabc. The path must start with "
                            + getRootSimplePath().toString());
        }

        final StringTokenizer tokens =
                new StringTokenizer(pathString, String.valueOf(SimplePath.PATH_SEPARATOR), false);

        // The first element is known: circabc
        tokens.nextToken();

        int iterationConter = 1;
        NodeRef iterNodeRef = getCircabcNodeRef();
        String iterName = null;
        while (tokens.hasMoreTokens()) {
            iterName = tokens.nextToken();
            iterationConter++;

            iterNodeRef = nodeService.getChildByName(iterNodeRef, ContentModel.ASSOC_CONTAINS, iterName);

            if (iterNodeRef == null) {
                throw new PathNotFoundException(
                        "The path is invalid. Impossible to found the path element '"
                                + iterName
                                + "' found at the position: "
                                + iterationConter);
            }
        }

        return new SimplePath(nodeService, iterNodeRef);
    }

    public List<NodeRef> getAllParents(final NodeRef initialNodeRef, final boolean includeContents) {
        return getAllParents(initialNodeRef, includeContents, CircabcModel.ASPECT_CIRCABC_ROOT, true);
    }

    public List<NodeRef> getAllParents(
            final NodeRef initialNodeRef,
            final boolean includeContets,
            QName untilAspect,
            final boolean includeUntilAspect) {
        if (initialNodeRef == null) {
            throw new NullPointerException("The nodeRef is a mandatory parameter");
        }

        if (!getNodeService().exists(initialNodeRef)) {
            throw new InvalidNodeRefException("The nodeRef should exist", initialNodeRef);
        }
        NodeRef nodeRef = initialNodeRef;
        if (untilAspect == null) {
            untilAspect = CircabcModel.ASPECT_CIRCABC_ROOT;
        }

        final List<NodeRef> result = new LinkedList<>();

        Set<QName> aspects;
        QName type;

        for (; ; ) {
            if (nodeRef == null) {
                // we are probably at the root of the store
                break;
            }

            type = getNodeService().getType(nodeRef);

            if (!includeContets) {
                final TypeDefinition typeDef = this.getDictionaryService().getType(type);

                if (typeDef != null) {
                    // look for File content node
                    if (ContentModel.TYPE_CONTENT.equals(type)
                            || this.getDictionaryService().isSubClass(type, ContentModel.TYPE_CONTENT)) {
                        nodeRef = getNodeService().getPrimaryParent(nodeRef).getParentRef();
                        continue;
                    }
                }
            }

            if (ContentModel.TYPE_MULTILINGUAL_CONTAINER.equals(type)) {
                // if we receive a ml continer, get its pivot translation
                nodeRef = getMultilingualContentService().getPivotTranslation(nodeRef);
                continue;
            }

            aspects = getNodeService().getAspects(nodeRef);

            if (!aspects.contains(CircabcModel.ASPECT_CIRCABC_MANAGEMENT)) {
                // we are outside circabc ...
                break;
            }

            if (aspects.contains(untilAspect)) {
                if (includeUntilAspect
                        && getPermissionService()
                        .hasPermission(nodeRef, PermissionService.READ)
                        .equals(AccessStatus.ALLOWED)) {
                    ((LinkedList<NodeRef>) result).addFirst(nodeRef);
                }

                // the last iteration
                break;
            } else {
                if (getPermissionService()
                        .hasPermission(nodeRef, PermissionService.READ)
                        .equals(AccessStatus.ALLOWED)) {
                    ((LinkedList<NodeRef>) result).addFirst(nodeRef);
                }

                nodeRef = getNodeService().getPrimaryParent(nodeRef).getParentRef();
            }
        }

        return result;
    }

    /**
     * Create circabc root level
     */
    public NodeRef createCircabc(final NodeRef parentNodeRef) {
        // force the re initialisation of this value at the next call of it
        circabcNodeRef = null;
        circabcLucenePath = null;

        // Create circaBCNode
        final NodeRef circabcNodeRef = createFolder(parentNodeRef, getCircaBCFolderName());

        // add CircaBC aspect
        final Map<QName, Serializable> circaBCProps = new HashMap<>();

        nodeService.addAspect(circabcNodeRef, CircabcModel.ASPECT_CIRCABC_ROOT, circaBCProps);

        if (logger.isDebugEnabled()) {
            logger.debug("Add:" + CircabcModel.ASPECT_CIRCABC_ROOT + " to the node:" + circabcNodeRef);
        }
        setCircabcDefaultSettings(circabcNodeRef);
        setCircabcDefaultRules(circabcNodeRef);

        return circabcNodeRef;
    }

    /**
     * Create machine translation root level
     */
    @Override
    public NodeRef createMTSpace(final NodeRef parentNodeRef) {
        // force the re initialisation of this value at the next call of it
        mtNodeRef = null;
        mtLucenePath = null;
        String mtRootSpaceName =
                CircabcConfiguration.getProperty(CircabcConfiguration.MT_ROOT_NODE_NAME_PROPERTIES);

        final NodeRef mtNodeRef = createFolder(parentNodeRef, mtRootSpaceName);

        return mtNodeRef;
    }

    private String getCircaBCFolderName() {
        return CircabcConfiguration.getProperty(CircabcConfiguration.CIRCABC_ROOT_NODE_NAME_PROPERTIES);
    }

    /**
     * Create CircabcCategory level
     */
    public NodeRef createCategory(
            final NodeRef parentNodeRef, final String folderName, final NodeRef alfrescoCategoryNodeRef) {
        final NodeRef circabcCategoryNodeRef = createFolder(parentNodeRef, folderName);

        // add CircaBC aspect
        final Map<QName, Serializable> circabcCategoryProps = new HashMap<>();

        nodeService.addAspect(
                circabcCategoryNodeRef, CircabcModel.ASPECT_CATEGORY, circabcCategoryProps);

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Add:" + CircabcModel.ASPECT_CATEGORY + " to the node:" + circabcCategoryNodeRef);
        }
        setCircabcCategoryDefaultSettings(circabcCategoryNodeRef);

        /** Link to SuperCategoryAlfresco */
        nodeService.addAspect(circabcCategoryNodeRef, ContentModel.ASPECT_GEN_CLASSIFIABLE, null);

        final ArrayList<NodeRef> categories = new ArrayList<>();
        categories.add(alfrescoCategoryNodeRef);

        // firstly retrieve all the properties for the current node
        final Map<QName, Serializable> updateProps = nodeService.getProperties(circabcCategoryNodeRef);
        updateProps.put(ContentModel.PROP_CATEGORIES, (Serializable) categories);

        // set the properties on the node
        nodeService.setProperties(circabcCategoryNodeRef, updateProps);

        return circabcCategoryNodeRef;
    }

    public NodeRef createIGRoot(
            final NodeRef parentNodeRef, final String folderName, final String circabcIGRootContact) {

        return createIGRoot(parentNodeRef, folderName, circabcIGRootContact, true);
    }

    public NodeRef createIGRoot(
            final NodeRef parentNodeRef,
            final String folderName,
            final String circabcIGRootContact,
            final boolean createOptionalProfiles) {
        final NodeRef circabcIGRootNodeRef = createFolder(parentNodeRef, folderName);
        // add CircaBC aspect
        final Map<QName, Serializable> circabcIGRootProps = new HashMap<>();

        /** Circabc contact for interest group leader */
        circabcIGRootProps.put(
                getIGRootProfileManagerService().getContactParamName(), circabcIGRootContact);
        nodeService.addAspect(circabcIGRootNodeRef, CircabcModel.ASPECT_IGROOT, circabcIGRootProps);

        if (logger.isDebugEnabled()) {
            logger.debug("Add:" + CircabcModel.ASPECT_IGROOT + " to the node:" + circabcIGRootNodeRef);
        }
        setCircabcIGRootDefaultSettings(parentNodeRef, circabcIGRootNodeRef, createOptionalProfiles);

        return circabcIGRootNodeRef;
    }

    public NodeRef createLibrary(final NodeRef parentNodeRef) {

        final String circabcLibraryDescription =
                CircabcConfiguration.getProperty(CircabcConfiguration.LIBRARY_NODE_DESCRIPTION_PROPERTIES);
        final NodeRef circabcLibraryNodeRef =
                createFolder(
                        parentNodeRef,
                        CircabcConfiguration.getProperty(CircabcConfiguration.LIBRARY_NODE_NAME_PROPERTIES));
        // add CircaBC aspect
        final Map<QName, Serializable> circabcLibraryRootProps = new HashMap<>();

        circabcLibraryRootProps.put(ContentModel.PROP_DESCRIPTION, circabcLibraryDescription);
        nodeService.addAspect(
                circabcLibraryNodeRef, CircabcModel.ASPECT_LIBRARY_ROOT, circabcLibraryRootProps);

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Add:" + CircabcModel.ASPECT_LIBRARY_ROOT + " to the node:" + circabcLibraryNodeRef);
        }
        setCircabcLibraryDefaultSettings(parentNodeRef, circabcLibraryNodeRef);

        setDefaultLibraryRules(circabcLibraryNodeRef);
        setCircabcNotifyRules(circabcLibraryNodeRef);

        return circabcLibraryNodeRef;
    }

    public NodeRef createNewsGroup(final NodeRef parentNodeRef) {
        final String circabcNewsGroupDescription =
                CircabcConfiguration.getProperty(
                        CircabcConfiguration.NEWSGROUP_NODE_DESCRIPTION_PROPERTIES);
        final NodeRef circabcNewsGroupNodeRef =
                createNewsGroup(
                        parentNodeRef,
                        CircabcConfiguration.getProperty(CircabcConfiguration.NEWSGROUP_NODE_NAME_PROPERTIES));
        // add CircaBC aspect
        final Map<QName, Serializable> circabcNewsGroupProps = new HashMap<>();

        circabcNewsGroupProps.put(ContentModel.PROP_DESCRIPTION, circabcNewsGroupDescription);
        nodeService.addAspect(
                circabcNewsGroupNodeRef, CircabcModel.ASPECT_NEWSGROUP_ROOT, circabcNewsGroupProps);

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Add:" + CircabcModel.ASPECT_NEWSGROUP_ROOT + " to the node:" + circabcNewsGroupNodeRef);
        }
        setDefaultNewsGroupRules(circabcNewsGroupNodeRef);
        setCircabcNewsGroupDefaultSettings(parentNodeRef, circabcNewsGroupNodeRef);
        setCircabcNotifyRules(circabcNewsGroupNodeRef);
        return circabcNewsGroupNodeRef;
    }

    public NodeRef createSurvey(final NodeRef parentNodeRef) {
        final String circabcSurveyDescription =
                CircabcConfiguration.getProperty(CircabcConfiguration.SURVEY_NODE_DESCRIPTION_PROPERTIES);
        final NodeRef circabcSurveyNodeRef =
                createSurvey(
                        parentNodeRef,
                        CircabcConfiguration.getProperty(CircabcConfiguration.SURVEY_NODE_NAME_PROPERTIES));
        // add CircaBC aspect
        final Map<QName, Serializable> circabcSurveyProps = new HashMap<>();

        circabcSurveyProps.put(ContentModel.PROP_DESCRIPTION, circabcSurveyDescription);

        nodeService.addAspect(
                circabcSurveyNodeRef, CircabcModel.ASPECT_SURVEY_ROOT, circabcSurveyProps);

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Add:" + CircabcModel.ASPECT_SURVEY_ROOT + " to the node:" + circabcSurveyNodeRef);
        }
        setDefaultSurveyRules(circabcSurveyNodeRef);
        setCircabcSurveyDefaultSettings(parentNodeRef, circabcSurveyNodeRef);

        return circabcSurveyNodeRef;
    }

    public NodeRef createDirectory(final NodeRef parentNodeRef) {
        final ChildAssociationRef assocRef =
                nodeService.createNode(
                        parentNodeRef,
                        CircabcModel.ASSOC_IG_DIRECTORY_CONTAINER,
                        CircabcModel.TYPE_DIRECTORY_SERVICE,
                        CircabcModel.TYPE_DIRECTORY_SERVICE,
                        new PropertyMap());

        final NodeRef dirContainerNodeRef = assocRef.getChildRef();

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Add:"
                            + CircabcModel.TYPE_DIRECTORY_SERVICE
                            + " to the node:"
                            + dirContainerNodeRef
                            + " with name "
                            + nodeService.getProperty(parentNodeRef, ContentModel.PROP_NAME));
        }

        return dirContainerNodeRef;
    }

    public NodeRef createEventService(NodeRef parentNodeRef) {
        final String circabcEventDescription =
                CircabcConfiguration.getProperty(CircabcConfiguration.EVENT_NODE_DESCRIPTION_PROPERTIES);
        final NodeRef circabcEventNodeRef =
                createFolder(
                        parentNodeRef,
                        CircabcConfiguration.getProperty(CircabcConfiguration.EVENT_NODE_NAME_PROPERTIES));
        // add Event aspect
        final Map<QName, Serializable> circabcEventProps = new HashMap<>();

        circabcEventProps.put(ContentModel.PROP_DESCRIPTION, circabcEventDescription);
        nodeService.addAspect(circabcEventNodeRef, CircabcModel.ASPECT_EVENT_ROOT, circabcEventProps);

        if (logger.isDebugEnabled()) {
            logger.debug("Add:" + CircabcModel.ASPECT_EVENT_ROOT + " to the node:" + circabcEventNodeRef);
        }
        setDefaultEventRules(circabcEventNodeRef);
        setEventDefaultSettings(parentNodeRef, circabcEventNodeRef);
        return circabcEventNodeRef;
    }

    public NodeRef createInformationService(NodeRef parentNodeRef) {
        final String circabcInformationDescription =
                CircabcConfiguration.getProperty(
                        CircabcConfiguration.INFORMATION_NODE_DESCRIPTION_PROPERTIES);
        final NodeRef circabcInformationNodeRef =
                createFolder(
                        parentNodeRef,
                        CircabcConfiguration.getProperty(
                                CircabcConfiguration.INFORMATION_NODE_NAME_PROPERTIES));
        // add CircaBC aspect
        final Map<QName, Serializable> circabcInformationProps = new HashMap<>();

        circabcInformationProps.put(ContentModel.PROP_DESCRIPTION, circabcInformationDescription);
        nodeService.addAspect(
                circabcInformationNodeRef, CircabcModel.ASPECT_INFORMATION_ROOT, circabcInformationProps);

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Add:"
                            + CircabcModel.ASPECT_INFORMATION_ROOT
                            + " to the node:"
                            + circabcInformationNodeRef);
        }
        setDefaultInformationRules(circabcInformationNodeRef);
        setCircabcNotifyRules(circabcInformationNodeRef);
        setInformationDefaultSettings(parentNodeRef, circabcInformationNodeRef);

        return circabcInformationNodeRef;
    }

    /**
     * Create a new folder with specified name in a given parent node
     *
     * @return the FileInfo of the new Folder
     */
    private NodeRef createFolder(final NodeRef parentNodeRef, final String folderName) {
        NodeRef newNodeRef = null;
        if (logger.isDebugEnabled()) {
            logger.debug("create new Folder:" + folderName + " on parentNode: " + parentNodeRef);
        }
        try {
            final FileInfo fileInfo =
                    fileFolderService.create(parentNodeRef, folderName, ContentModel.TYPE_FOLDER);
            newNodeRef = fileInfo.getNodeRef();
        } catch (final RuntimeException e) {
            if (logger.isErrorEnabled()) {
                logger.error(
                        "Impossible to create the folder '" + folderName + "' under the node " + parentNodeRef,
                        e);
            }

            throw e;
        }
        return newNodeRef;
    }

    /**
     * Create a new folder with specified name in a given parent node
     *
     * @return the FileInfo of the new Folder
     */
    private NodeRef createNewsGroup(final NodeRef parentNodeRef, final String newsGroupName) {
        NodeRef newNodeRef = null;
        if (logger.isDebugEnabled()) {
            logger.debug("create new Folder:" + newsGroupName + " on parentNode: " + parentNodeRef);
        }
        try {
            final FileInfo fileInfo =
                    fileFolderService.create(parentNodeRef, newsGroupName, ForumModel.TYPE_FORUMS);
            newNodeRef = fileInfo.getNodeRef();
        } catch (final RuntimeException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Impossible to create a newsgroup service under the node " + parentNodeRef, e);
            }

            throw e;
        }
        return newNodeRef;
    }

    /**
     * Create a new folder with specified name in a given parent node
     *
     * @return the FileInfo of the new Folder
     */
    private NodeRef createSurvey(final NodeRef parentNodeRef, final String surveyName) {
        NodeRef newNodeRef = null;
        if (logger.isDebugEnabled()) {
            logger.debug("create new Folder:" + surveyName + " on parentNode: " + parentNodeRef);
        }
        try {
            final FileInfo fileInfo =
                    fileFolderService.create(parentNodeRef, surveyName, SurveyModel.TYPE_SURVEY_SPACE);
            newNodeRef = fileInfo.getNodeRef();
        } catch (final RuntimeException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Impossible to create a survey service under the node " + parentNodeRef, e);
            }
            throw e;
        }
        return newNodeRef;
    }

    private void setCircabcDefaultSettings(final NodeRef nodeRef) {
        Map<String, Set<String>> servicesPermissions;

        getCircabcRootProfileManagerService().createMasterGroup(nodeRef);
        getCircabcRootProfileManagerService().createInvitedUsersGroup(nodeRef);
        getCircabcRootProfileManagerService().createSubsGroup(nodeRef);

        /*final String supportGroup = */
        getCircabcRootProfileManagerService()
                .createCircaSupportGroup();

        Set<String> permissionsSet;

        // Default Profiles
        servicesPermissions = new HashMap<>();
        permissionsSet = new HashSet<>();
        permissionsSet.add(CircabcRootPermissions.CIRCABCADMIN.toString());
        servicesPermissions.put(CircabcServices.CIRCABC.toString(), permissionsSet);
        getCircabcRootProfileManagerService()
                .addProfile(
                        nodeRef, CircabcRootProfileManagerService.Profiles.CIRCABC_ADMIN, servicesPermissions);
        getCircabcRootProfileManagerService()
                .addProfileTitles(
                        nodeRef, CircabcRootProfileManagerService.Profiles.CIRCABC_ADMIN, CIRCABC_ADMIN_TITLE);

        // Cut inheritance
        permissionService.setInheritParentPermissions(nodeRef, false);

        String profileName;
        // Set Public Permission
        servicesPermissions = new HashMap<>();
        permissionsSet = new HashSet<>();
        profileName = getCircabcRootProfileManagerService().getAllCircaUsersGroupName();

        permissionsSet.add(CircabcRootPermissions.CIRCABCACCESS.toString());
        servicesPermissions.put(CircabcServices.CIRCABC.toString(), permissionsSet);
        getCircabcRootProfileManagerService().addProfile(nodeRef, profileName, servicesPermissions);
        getCircabcRootProfileManagerService().addProfileTitles(nodeRef, profileName, REGISTRED_TITLE);

        // Set Guest Permission
        servicesPermissions = new HashMap<>();
        permissionsSet = new HashSet<>();
        permissionsSet.add(CircabcRootPermissions.CIRCABCACCESS.toString());
        profileName = CircabcConstant.GUEST_AUTHORITY;
        servicesPermissions.put(CircabcServices.CIRCABC.toString(), permissionsSet);
        getCircabcRootProfileManagerService().addProfile(nodeRef, profileName, servicesPermissions);
        getCircabcRootProfileManagerService().addProfileTitles(nodeRef, profileName, ANONYMOUS_TITLE);
        /*
         * final HashMap<QName, Serializable> properties = new HashMap<QName,
         * Serializable>(1, 1.0f); properties.put(ContentModel.PROP_OWNER,
         * ADMIN); nodeService.addAspect(nodeRef, ContentModel.ASPECT_OWNABLE,
         * properties);
         */
        ownableService.setOwner(nodeRef, ADMIN);

        nodeService.addAspect(nodeRef, CircabcModel.ASPECT_CIRCABC_MANAGEMENT, null);

        // Set AlfrescoAdmin Permission
        final HashSet<String> permissions = new HashSet<>();
        permissions.add(CircabcRootPermissions.CIRCABCADMIN.toString());
        getCircabcRootProfileManagerService().setAlfrescoAdminPermissions(nodeRef, permissions, true);
    }

    /*
     * Use a businesss service that call
     * eu.cec.digit.circabc.business.helper.RulesManager#addAspectToAllChilds(circabcNodeRef, CircabcModel.ASPECT_CIRCABC_MANAGEMENT);
     */
    @Deprecated
    private void setCircabcDefaultRules(final NodeRef circabcNodeRef) {
        // Add CircaManagement
        final CompositeAction compositeActionManagement = actionService.createCompositeAction();

        // Create Action
        final Action actionManagement = actionService.createAction(AddFeaturesActionExecuter.NAME);
        actionManagement.setParameterValue(
                AddFeaturesActionExecuter.PARAM_ASPECT_NAME, CircabcModel.ASPECT_CIRCABC_MANAGEMENT);
        compositeActionManagement.addAction(actionManagement);
        compositeActionManagement.setTitle("Add CircaManagement Aspect");
        compositeActionManagement.setDescription("Add CircaManagement Aspect Description");

        // Create Condition
        final ActionCondition actionConditionLibrary =
                actionService.createActionCondition(NoConditionEvaluator.NAME);
        compositeActionManagement.addActionCondition(actionConditionLibrary);

        // Create a rule
        final Rule rule = new Rule();
        rule.setRuleType(RuleType.INBOUND);

        rule.applyToChildren(true);
        rule.setExecuteAsynchronously(false);
        rule.setAction(compositeActionManagement);
        rule.setTitle(compositeActionManagement.getTitle());
        rule.setDescription(compositeActionManagement.getDescription());
        ruleService.saveRule(circabcNodeRef, rule);
    }

    private void setCircabcCategoryDefaultSettings(final NodeRef nodeRef) {
        Map<String, Set<String>> servicesPermissions;

        getCategoryProfileManagerService().createMasterGroup(nodeRef);
        getCategoryProfileManagerService().createInvitedUsersGroup(nodeRef);
        getCategoryProfileManagerService().createSubsGroup(nodeRef);
        Set<String> permissionsSet;

        // Default Profiles
        servicesPermissions = new HashMap<>();
        permissionsSet = new HashSet<>();
        permissionsSet.add(CategoryPermissions.CIRCACATEGORYADMIN.toString());
        servicesPermissions.put(CircabcServices.CATEGORY.toString(), permissionsSet);

        getCategoryProfileManagerService()
                .addProfile(
                        nodeRef,
                        CategoryProfileManagerService.Profiles.CIRCA_CATEGORY_ADMIN,
                        servicesPermissions);

        getCategoryProfileManagerService()
                .addProfileTitles(
                        nodeRef, CategoryProfileManagerService.Profiles.CIRCA_CATEGORY_ADMIN, CATEGORY_ADMIN);

        // Cut inheritance
        permissionService.setInheritParentPermissions(nodeRef, false);

        String profileName;
        // Set Public Permission
        servicesPermissions = new HashMap<>();
        permissionsSet = new HashSet<>();

        profileName = getCircabcRootProfileManagerService().getAllCircaUsersGroupName();

        permissionsSet.add(CategoryPermissions.CIRCACATEGORYACCESS.toString());
        servicesPermissions.put(CircabcServices.CATEGORY.toString(), permissionsSet);
        getCategoryProfileManagerService().addProfile(nodeRef, profileName, servicesPermissions);
        getCategoryProfileManagerService().addProfileTitles(nodeRef, profileName, REGISTRED_TITLE);

        // Set Guest Permission
        servicesPermissions = new HashMap<>();
        permissionsSet = new HashSet<>();
        permissionsSet.add(CategoryPermissions.CIRCACATEGORYACCESS.toString());
        servicesPermissions.put(CircabcServices.CATEGORY.toString(), permissionsSet);
        profileName = CircabcConstant.GUEST_AUTHORITY;
        getCategoryProfileManagerService().addProfile(nodeRef, profileName, servicesPermissions);
        getCategoryProfileManagerService().addProfileTitles(nodeRef, profileName, ANONYMOUS_TITLE);

        // CircaBCAdmin
        final NodeRef circabcNodeRef = nodeService.getPrimaryParent(nodeRef).getParentRef();
        final String circabcAdminGroup =
                getCircabcRootProfileManagerService()
                        .getProfile(circabcNodeRef, CircabcRootProfileManagerService.Profiles.CIRCABC_ADMIN)
                        .getPrefixedAlfrescoGroupName();

        ownableService.setOwner(nodeRef, ADMIN);

        // Add defaults Profiles permissions
        permissionService.setPermission(
                nodeRef, circabcAdminGroup, CategoryPermissions.CIRCACATEGORYADMIN.toString(), true);
    }

    private void setCircabcIGRootDefaultSettings(
            final NodeRef circabcCategoryNodeRef, final NodeRef nodeRef, boolean createOptionalProfiles) {
        getIGRootProfileManagerService().createMasterGroup(nodeRef);
        getIGRootProfileManagerService().createInvitedUsersGroup(nodeRef);

        Map<String, Set<String>> servicesPermissions;
        Set<String> permissionsSet;

        if (createOptionalProfiles) {
            // Default Profiles

            // Access
            permissionsSet = new HashSet<>();
            permissionsSet.add(DirectoryPermissions.DIRACCESS.toString());
            servicesPermissions = new HashMap<>();
            servicesPermissions.put(CircabcServices.DIRECTORY.toString(), permissionsSet);
            permissionsSet = new HashSet<>();
            permissionsSet.add(VisibilityPermissions.VISIBILITY.toString());
            servicesPermissions.put(CircabcServices.VISIBILITY.toString(), permissionsSet);
            getIGRootProfileManagerService()
                    .addProfile(nodeRef, IGRootProfileManagerService.Profiles.ACCESS, servicesPermissions);
            getIGRootProfileManagerService()
                    .addProfileTitles(nodeRef, IGRootProfileManagerService.Profiles.ACCESS, ACCESS_TITLE);

            // Author
            permissionsSet = new HashSet<>();
            permissionsSet.add(DirectoryPermissions.DIRACCESS.toString());
            servicesPermissions = new HashMap<>();
            servicesPermissions.put(CircabcServices.DIRECTORY.toString(), permissionsSet);
            permissionsSet = new HashSet<>();
            permissionsSet.add(VisibilityPermissions.VISIBILITY.toString());
            servicesPermissions.put(CircabcServices.VISIBILITY.toString(), permissionsSet);
            getIGRootProfileManagerService()
                    .addProfile(nodeRef, IGRootProfileManagerService.Profiles.AUTHOR, servicesPermissions);
            getIGRootProfileManagerService()
                    .addProfileTitles(nodeRef, IGRootProfileManagerService.Profiles.AUTHOR, AUTHOR_TITLE);

            // Contributor
            permissionsSet = new HashSet<>();
            permissionsSet.add(DirectoryPermissions.DIRACCESS.toString());
            servicesPermissions = new HashMap<>();
            servicesPermissions.put(CircabcServices.DIRECTORY.toString(), permissionsSet);
            permissionsSet = new HashSet<>();
            permissionsSet.add(VisibilityPermissions.VISIBILITY.toString());
            servicesPermissions.put(CircabcServices.VISIBILITY.toString(), permissionsSet);
            getIGRootProfileManagerService()
                    .addProfile(
                            nodeRef, IGRootProfileManagerService.Profiles.CONTRIBUTOR, servicesPermissions);
            getIGRootProfileManagerService()
                    .addProfileTitles(
                            nodeRef, IGRootProfileManagerService.Profiles.CONTRIBUTOR, CONTRIB_TITLE);

            // IGLeader
            permissionsSet = new HashSet<>();
            permissionsSet.add(DirectoryPermissions.DIRADMIN.toString());
            servicesPermissions = new HashMap<>();
            servicesPermissions.put(CircabcServices.DIRECTORY.toString(), permissionsSet);
            permissionsSet = new HashSet<>();
            permissionsSet.add(VisibilityPermissions.VISIBILITY.toString());
            servicesPermissions.put(CircabcServices.VISIBILITY.toString(), permissionsSet);
            getIGRootProfileManagerService()
                    .addProfile(nodeRef, IGRootProfileManagerService.Profiles.IGLEADER, servicesPermissions);
            getIGRootProfileManagerService()
                    .addProfileTitles(nodeRef, IGRootProfileManagerService.Profiles.IGLEADER, LEADER_TITLE);

            // Reviewer
            permissionsSet = new HashSet<>();
            permissionsSet.add(DirectoryPermissions.DIRACCESS.toString());
            servicesPermissions = new HashMap<>();
            servicesPermissions.put(CircabcServices.DIRECTORY.toString(), permissionsSet);
            permissionsSet = new HashSet<>();
            permissionsSet.add(VisibilityPermissions.VISIBILITY.toString());
            servicesPermissions.put(CircabcServices.VISIBILITY.toString(), permissionsSet);
            getIGRootProfileManagerService()
                    .addProfile(nodeRef, IGRootProfileManagerService.Profiles.REVIEWER, servicesPermissions);
            getIGRootProfileManagerService()
                    .addProfileTitles(nodeRef, IGRootProfileManagerService.Profiles.REVIEWER, REVIEWER_TITLE);

            // Secretary
            permissionsSet = new HashSet<>();
            permissionsSet.add(DirectoryPermissions.DIRMANAGEMEMBERS.toString());
            servicesPermissions = new HashMap<>();
            servicesPermissions.put(CircabcServices.DIRECTORY.toString(), permissionsSet);
            permissionsSet = new HashSet<>();
            permissionsSet.add(VisibilityPermissions.VISIBILITY.toString());
            servicesPermissions.put(CircabcServices.VISIBILITY.toString(), permissionsSet);
            getIGRootProfileManagerService()
                    .addProfile(nodeRef, IGRootProfileManagerService.Profiles.SECRETARY, servicesPermissions);
            getIGRootProfileManagerService()
                    .addProfileTitles(
                            nodeRef, IGRootProfileManagerService.Profiles.SECRETARY, SECRETARY_TITLE);
        }

        final String registredProfile =
                getCircabcRootProfileManagerService().getAllCircaUsersGroupName();
        final String guestProfile = CircabcConstant.GUEST_AUTHORITY;

        // Set Public Permission
        servicesPermissions = new HashMap<>();
        permissionsSet = new HashSet<>();
        permissionsSet.add(DirectoryPermissions.DIRNOACCESS.toString());
        servicesPermissions.put(CircabcServices.DIRECTORY.toString(), permissionsSet);

        permissionsSet = new HashSet<>();
        permissionsSet.add(VisibilityPermissions.NOVISIBILITY.toString());
        servicesPermissions.put(CircabcServices.VISIBILITY.toString(), permissionsSet);

        getIGRootProfileManagerService().addProfile(nodeRef, registredProfile, servicesPermissions);
        getIGRootProfileManagerService().addProfile(nodeRef, guestProfile, servicesPermissions);

        getIGRootProfileManagerService().addProfileTitles(nodeRef, registredProfile, REGISTRED_TITLE);

        getIGRootProfileManagerService().addProfileTitles(nodeRef, guestProfile, ANONYMOUS_TITLE);

        // CategoryAdmin
        final String circabcCategoryAdminGroup =
                getCategoryProfileManagerService()
                        .getProfile(
                                circabcCategoryNodeRef, CategoryProfileManagerService.Profiles.CIRCA_CATEGORY_ADMIN)
                        .getPrefixedAlfrescoGroupName();

        // Add defaults Profiles permissions
        permissionService.setPermission(
                nodeRef, circabcCategoryAdminGroup, DirectoryPermissions.DIRADMIN.toString(), true);
        permissionService.setPermission(
                nodeRef, circabcCategoryAdminGroup, IgPermissions.IGDELETE.toString(), true);
        permissionService.setPermission(
                nodeRef, circabcCategoryAdminGroup, VisibilityPermissions.VISIBILITY.toString(), true);

        ownableService.setOwner(nodeRef, ADMIN);

        // Default permissions
        // Cut inheritance
        permissionService.setInheritParentPermissions(nodeRef, false);

        // https://webgate.ec.europa.eu/CITnet/jira/browse/DIGIT-CIRCABC-2282
        setNameAsDefaultRendition(nodeRef);
    }

    private void setNameAsDefaultRendition(final NodeRef nodeRef) {
        NavigationPreference navPref =
                navigationPreferencesService.getServicePreference(
                        nodeRef,
                        NavigationPreferencesService.LIBRARY_SERVICE,
                        NavigationPreferencesService.CONTENT_TYPE);

        ServiceConfig globalPreference = navPref.getService();

        ColumnConfig name = null;

        for (ColumnConfig cf : globalPreference.getColumns()) {
            if (cf.getName().equals(EditNavigationPreference.COLUMN_NAME)) {
                name = cf;
            }
        }

        navPref.getColumns().set(0, name);

        try {

            navigationPreferencesService.addServicePreference(
                    nodeRef,
                    NavigationPreferencesService.LIBRARY_SERVICE,
                    NavigationPreferencesService.CONTENT_TYPE,
                    navPref);

        } catch (CustomizationException e) {

            if (logger.isErrorEnabled()) {
                logger.error(
                        "Error during setting default 'name' value as rendered property in content list for group:"
                                + nodeRef,
                        e);
            }
        }
    }

    private void setCircabcLibraryDefaultSettings(
            final NodeRef circabcIGRootNodeRef, final NodeRef nodeRef) {

        final Map<String, Profile> profiles =
                getIGRootProfileManagerService().getProfileMap(circabcIGRootNodeRef);

        Map<String, Set<String>> servicesPermissions;

        // Default Profiles
        Set<String> permissionsSet;

        if (profiles.containsKey(IGRootProfileManagerService.Profiles.ACCESS)) {
            // Access
            permissionsSet = new HashSet<>();
            permissionsSet.add(LibraryPermissions.LIBACCESS.toString());
            servicesPermissions = new HashMap<>();
            servicesPermissions.put(CircabcServices.LIBRARY.toString(), permissionsSet);
            getLibraryProfileManagerService()
                    .updateProfile(
                            nodeRef, IGRootProfileManagerService.Profiles.ACCESS, servicesPermissions, true);
        }

        if (profiles.containsKey(IGRootProfileManagerService.Profiles.AUTHOR)) {
            //	Author
            permissionsSet = new HashSet<>();
            permissionsSet.add(LibraryPermissions.LIBFULLEDIT.toString());
            servicesPermissions = new HashMap<>();
            servicesPermissions.put(CircabcServices.LIBRARY.toString(), permissionsSet);
            getLibraryProfileManagerService()
                    .updateProfile(
                            nodeRef, IGRootProfileManagerService.Profiles.AUTHOR, servicesPermissions, true);
        }
        if (profiles.containsKey(IGRootProfileManagerService.Profiles.CONTRIBUTOR)) {
            // Contributor
            permissionsSet = new HashSet<>();
            permissionsSet.add(LibraryPermissions.LIBACCESS.toString());
            servicesPermissions = new HashMap<>();
            servicesPermissions.put(CircabcServices.LIBRARY.toString(), permissionsSet);
            getLibraryProfileManagerService()
                    .updateProfile(
                            nodeRef, IGRootProfileManagerService.Profiles.CONTRIBUTOR, servicesPermissions, true);
        }
        if (profiles.containsKey(IGRootProfileManagerService.Profiles.IGLEADER)) {
            // IGLeader
            permissionsSet = new HashSet<>();
            permissionsSet.add(LibraryPermissions.LIBADMIN.toString());
            servicesPermissions = new HashMap<>();
            servicesPermissions.put(CircabcServices.LIBRARY.toString(), permissionsSet);
            getLibraryProfileManagerService()
                    .updateProfile(
                            nodeRef, IGRootProfileManagerService.Profiles.IGLEADER, servicesPermissions, true);
        }
        if (profiles.containsKey(IGRootProfileManagerService.Profiles.REVIEWER)) {
            // Reviewer
            permissionsSet = new HashSet<>();
            permissionsSet.add(LibraryPermissions.LIBEDITONLY.toString());
            servicesPermissions = new HashMap<>();
            servicesPermissions.put(CircabcServices.LIBRARY.toString(), permissionsSet);
            getLibraryProfileManagerService()
                    .updateProfile(
                            nodeRef, IGRootProfileManagerService.Profiles.REVIEWER, servicesPermissions, true);
        }
        if (profiles.containsKey(IGRootProfileManagerService.Profiles.SECRETARY)) {
            // Secretary
            permissionsSet = new HashSet<>();
            permissionsSet.add(LibraryPermissions.LIBNOACCESS.toString());
            servicesPermissions = new HashMap<>();
            servicesPermissions.put(CircabcServices.LIBRARY.toString(), permissionsSet);
            getLibraryProfileManagerService()
                    .updateProfile(
                            nodeRef, IGRootProfileManagerService.Profiles.SECRETARY, servicesPermissions, true);
        }

        String profileName;
        // Set Public Permission
        servicesPermissions = new HashMap<>();
        permissionsSet = new HashSet<>();
        profileName = getCircabcRootProfileManagerService().getAllCircaUsersGroupName();

        permissionsSet.add(LibraryPermissions.LIBNOACCESS.toString());
        servicesPermissions.put(CircabcServices.LIBRARY.toString(), permissionsSet);
        getLibraryProfileManagerService()
                .updateProfile(nodeRef, profileName, servicesPermissions, true);

        // Set Guest Permission
        servicesPermissions = new HashMap<>();
        permissionsSet = new HashSet<>();
        permissionsSet.add(LibraryPermissions.LIBNOACCESS.toString());
        servicesPermissions.put(CircabcServices.LIBRARY.toString(), permissionsSet);
        profileName = CircabcConstant.GUEST_AUTHORITY;
        getLibraryProfileManagerService()
                .updateProfile(nodeRef, profileName, servicesPermissions, true);

        // Registered permissions
        servicesPermissions = new HashMap<>();
        permissionsSet = new HashSet<>();
        permissionsSet.add(LibraryPermissions.LIBNOACCESS.toString());
        servicesPermissions.put(CircabcServices.LIBRARY.toString(), permissionsSet);
        getLibraryProfileManagerService()
                .updateProfile(
                        nodeRef,
                        getCircabcRootProfileManagerService().getAllCircaUsersGroupName(),
                        servicesPermissions,
                        true);

        final NodeRef circabcCategoryNodeRef =
                nodeService.getPrimaryParent(circabcIGRootNodeRef).getParentRef();

        final String circabcCategoryAdminGroup =
                getCategoryProfileManagerService()
                        .getProfile(
                                circabcCategoryNodeRef, CategoryProfileManagerService.Profiles.CIRCA_CATEGORY_ADMIN)
                        .getPrefixedAlfrescoGroupName();

        // Add defaults Profiles permissions
        permissionService.setPermission(
                nodeRef, circabcCategoryAdminGroup, LibraryPermissions.LIBADMIN.toString(), true);

        /*
         * final HashMap<QName, Serializable> properties = new HashMap<QName,
         * Serializable>(1, 1.0f); properties.put(ContentModel.PROP_OWNER,
         * ADMIN); nodeService.addAspect(nodeRef, ContentModel.ASPECT_OWNABLE,
         * properties);
         *
         */

        ownableService.setOwner(nodeRef, ADMIN);

        // Default permissions
        // Cut inheritance
        permissionService.setInheritParentPermissions(nodeRef, false);
    }

    /*
     * Use a businesss service that call
     * eu.cec.digit.circabc.business.helper.RulesManager#addAspectToAllChilds(libraryNodeRef, CircabcModel.ASPECT_LIBRARY);
     * eu.cec.digit.circabc.business.helper.RulesManager#addAspectToAllChilds(libraryNodeRef, DocumentModel.ASPECT_CIRCABC_DOCUMENT, ContentModel.TYPE_CONTENT);
     */
    @Deprecated
    private void setDefaultLibraryRules(final NodeRef libraryNodeRef) {
        {
            // Add CircaDocument
            final CompositeAction compositeAction = actionService.createCompositeAction();

            // Create Action
            final Action action = actionService.createAction(AddFeaturesActionExecuter.NAME);
            action.setParameterValue(
                    AddFeaturesActionExecuter.PARAM_ASPECT_NAME, DocumentModel.ASPECT_CIRCABC_DOCUMENT);
            compositeAction.addAction(action);
            compositeAction.setTitle("Add CircaDocument Aspect");
            compositeAction.setDescription("Add CircaDocument Aspect Description");

            // Create Condition
            final ActionCondition actionConditionCircaDocument =
                    actionService.createActionCondition(IsSubTypeEvaluator.NAME);

            actionConditionCircaDocument.setParameterValue(
                    IsSubTypeEvaluator.PARAM_TYPE, ContentModel.TYPE_CONTENT);

            compositeAction.addActionCondition(actionConditionCircaDocument);

            // Create a rule
            final Rule rule = new Rule();
            rule.setRuleType(RuleType.INBOUND);

            rule.applyToChildren(true);
            rule.setExecuteAsynchronously(false);
            rule.setAction(compositeAction);
            rule.setTitle(compositeAction.getTitle());
            rule.setDescription(compositeAction.getDescription());
            ruleService.saveRule(libraryNodeRef, rule);
        }

        {
            // Add CircaLibrary
            final CompositeAction compositeActionLibrary = actionService.createCompositeAction();

            // Create Action
            final Action actionLibrary = actionService.createAction(AddFeaturesActionExecuter.NAME);
            actionLibrary.setParameterValue(
                    AddFeaturesActionExecuter.PARAM_ASPECT_NAME, CircabcModel.ASPECT_LIBRARY);
            compositeActionLibrary.addAction(actionLibrary);
            compositeActionLibrary.setTitle("Add CircaLibrary Aspect");
            compositeActionLibrary.setDescription("Add CircaLibrary Aspect Description");

            // Create Condition
            final ActionCondition actionConditionLibrary =
                    actionService.createActionCondition(NoConditionEvaluator.NAME);
            // final ActionCondition actionConditionLibrary =
            // actionService.createActionCondition(IsSubTypeEvaluator.NAME);
            // actionConditionLibrary.setParameterValue(IsSubTypeEvaluator.PARAM_TYPE,
            // ContentModel.TYPE_FOLDER);

            compositeActionLibrary.addActionCondition(actionConditionLibrary);

            // Create a rule
            final Rule rule = new Rule();
            rule.setRuleType(RuleType.INBOUND);

            rule.applyToChildren(true);
            rule.setExecuteAsynchronously(false);
            rule.setAction(compositeActionLibrary);
            rule.setTitle(compositeActionLibrary.getTitle());
            rule.setDescription(compositeActionLibrary.getDescription());
            ruleService.saveRule(libraryNodeRef, rule);
        }
    }

    /*
     * Use a businesss service that call
     * eu.cec.digit.circabc.business.helper.RulesManager#addAspectToAllChilds(libraryNodeRef, ContentNotifyAspect.ASPECT_CONTENT_NOTIFY);
     */
    @Deprecated
    private void setCircabcNotifyRules(final NodeRef libraryNodeRef) {
        // Add CircaManagement
        final CompositeAction compositeActionNotify = actionService.createCompositeAction();

        // Create Action
        final Action actionNotify = actionService.createAction(AddFeaturesActionExecuter.NAME);
        actionNotify.setParameterValue(
                AddFeaturesActionExecuter.PARAM_ASPECT_NAME, ContentNotifyAspect.ASPECT_CONTENT_NOTIFY);
        compositeActionNotify.addAction(actionNotify);
        compositeActionNotify.setTitle("Add CircabcNotify Aspect");
        compositeActionNotify.setDescription("Add CircabcNotify Aspect Description");

        // Create Condition
        final ActionCondition actionConditionNotify =
                actionService.createActionCondition(IsSubTypeEvaluator.NAME);
        actionConditionNotify.setParameterValue(
                IsSubTypeEvaluator.PARAM_TYPE, ContentModel.TYPE_CONTENT);
        compositeActionNotify.addActionCondition(actionConditionNotify);

        // Create a rule
        final Rule rule = new Rule();
        rule.setRuleType(RuleType.INBOUND);

        rule.applyToChildren(true);
        rule.setExecuteAsynchronously(false);
        rule.setAction(compositeActionNotify);
        rule.setTitle(compositeActionNotify.getTitle());
        rule.setDescription(compositeActionNotify.getDescription());
        ruleService.saveRule(libraryNodeRef, rule);
    }

    private void setCircabcNewsGroupDefaultSettings(
            final NodeRef circabcIGRootNodeRef, final NodeRef nodeRef) {

        final Map<String, Profile> profiles =
                getIGRootProfileManagerService().getProfileMap(circabcIGRootNodeRef);

        HashMap<String, Set<String>> servicesPermissions;
        Set<String> permissionsSet;
        // Default Profiles

        if (profiles.containsKey(IGRootProfileManagerService.Profiles.ACCESS)) {
            // Access
            permissionsSet = new HashSet<>();
            permissionsSet.add(NewsGroupPermissions.NWSACCESS.toString());
            servicesPermissions = new HashMap<>();
            servicesPermissions.put(CircabcServices.NEWSGROUP.toString(), permissionsSet);
            getNewsGroupProfileManagerService()
                    .updateProfile(
                            nodeRef, IGRootProfileManagerService.Profiles.ACCESS, servicesPermissions, true);
        }
        if (profiles.containsKey(IGRootProfileManagerService.Profiles.AUTHOR)) {
            // Author
            permissionsSet = new HashSet<>();
            permissionsSet.add(NewsGroupPermissions.NWSPOST.toString());
            servicesPermissions = new HashMap<>();
            servicesPermissions.put(CircabcServices.NEWSGROUP.toString(), permissionsSet);
            getNewsGroupProfileManagerService()
                    .updateProfile(
                            nodeRef, IGRootProfileManagerService.Profiles.AUTHOR, servicesPermissions, true);
        }
        if (profiles.containsKey(IGRootProfileManagerService.Profiles.CONTRIBUTOR)) {
            // Contributor
            permissionsSet = new HashSet<>();
            permissionsSet.add(NewsGroupPermissions.NWSPOST.toString());
            servicesPermissions = new HashMap<>();
            servicesPermissions.put(CircabcServices.NEWSGROUP.toString(), permissionsSet);
            getNewsGroupProfileManagerService()
                    .updateProfile(
                            nodeRef, IGRootProfileManagerService.Profiles.CONTRIBUTOR, servicesPermissions, true);
        }
        if (profiles.containsKey(IGRootProfileManagerService.Profiles.IGLEADER)) {
            // IGLeader
            permissionsSet = new HashSet<>();
            permissionsSet.add(NewsGroupPermissions.NWSADMIN.toString());
            servicesPermissions = new HashMap<>();
            servicesPermissions.put(CircabcServices.NEWSGROUP.toString(), permissionsSet);
            getNewsGroupProfileManagerService()
                    .updateProfile(
                            nodeRef, IGRootProfileManagerService.Profiles.IGLEADER, servicesPermissions, true);
        }
        if (profiles.containsKey(IGRootProfileManagerService.Profiles.REVIEWER)) {
            // Reviewer
            permissionsSet = new HashSet<>();
            permissionsSet.add(NewsGroupPermissions.NWSPOST.toString());
            servicesPermissions = new HashMap<>();
            servicesPermissions.put(CircabcServices.NEWSGROUP.toString(), permissionsSet);
            getNewsGroupProfileManagerService()
                    .updateProfile(
                            nodeRef, IGRootProfileManagerService.Profiles.REVIEWER, servicesPermissions, true);
        }
        if (profiles.containsKey(IGRootProfileManagerService.Profiles.SECRETARY)) {
            // Secretary
            permissionsSet = new HashSet<>();
            permissionsSet.add(NewsGroupPermissions.NWSNOACCESS.toString());
            servicesPermissions = new HashMap<>();
            servicesPermissions.put(CircabcServices.NEWSGROUP.toString(), permissionsSet);
            getNewsGroupProfileManagerService()
                    .updateProfile(
                            nodeRef, IGRootProfileManagerService.Profiles.SECRETARY, servicesPermissions, true);
        }

        String profileName;
        // Set Public Permission
        servicesPermissions = new HashMap<>();
        permissionsSet = new HashSet<>();
        profileName = getCircabcRootProfileManagerService().getAllCircaUsersGroupName();

        permissionsSet.add(NewsGroupPermissions.NWSNOACCESS.toString());
        servicesPermissions.put(CircabcServices.NEWSGROUP.toString(), permissionsSet);
        getNewsGroupProfileManagerService()
                .updateProfile(nodeRef, profileName, servicesPermissions, true);

        // Set Guest Permission
        servicesPermissions = new HashMap<>();
        permissionsSet = new HashSet<>();
        permissionsSet.add(NewsGroupPermissions.NWSNOACCESS.toString());
        servicesPermissions.put(CircabcServices.NEWSGROUP.toString(), permissionsSet);
        profileName = CircabcConstant.GUEST_AUTHORITY;
        getNewsGroupProfileManagerService()
                .updateProfile(nodeRef, profileName, servicesPermissions, true);

        // Registred permissions
        servicesPermissions = new HashMap<>();
        permissionsSet = new HashSet<>();
        permissionsSet.add(NewsGroupPermissions.NWSNOACCESS.toString());
        servicesPermissions.put(CircabcServices.NEWSGROUP.toString(), permissionsSet);
        getNewsGroupProfileManagerService()
                .updateProfile(
                        nodeRef,
                        getCircabcRootProfileManagerService().getAllCircaUsersGroupName(),
                        servicesPermissions,
                        true);

        final NodeRef circabcCategoryNodeRef =
                nodeService.getPrimaryParent(circabcIGRootNodeRef).getParentRef();

        final String circabcCategoryAdminGroup =
                getCategoryProfileManagerService()
                        .getProfile(
                                circabcCategoryNodeRef, CategoryProfileManagerService.Profiles.CIRCA_CATEGORY_ADMIN)
                        .getPrefixedAlfrescoGroupName();

        // Add defaults Profiles permissions
        permissionService.setPermission(
                nodeRef, circabcCategoryAdminGroup, NewsGroupPermissions.NWSADMIN.toString(), true);

        /*
         * final HashMap<QName, Serializable> properties = new HashMap<QName,
         * Serializable>(1, 1.0f); properties.put(ContentModel.PROP_OWNER,
         * ADMIN); nodeService.addAspect(nodeRef, ContentModel.ASPECT_OWNABLE,
         * properties);
         *
         */

        // ownableService.setOwner(nodeRef, ADMIN);
        // Default permissions
        // Cut inheritance
        permissionService.setInheritParentPermissions(nodeRef, false);
    }

    /*
     * Use a businesss service that call
     * eu.cec.digit.circabc.business.helper.RulesManager#addAspectToAllChilds(newsGroupNodeRef, CircabcModel.ASPECT_NEWSGROUP);
     */
    @Deprecated
    private void setDefaultNewsGroupRules(final NodeRef newsGroupNodeRef) {
        {
            // Add CircaNewsGroup
            final CompositeAction compositeActionNewsGroup = actionService.createCompositeAction();

            // Create Action
            final Action actionNewsGroup = actionService.createAction(AddFeaturesActionExecuter.NAME);
            actionNewsGroup.setParameterValue(
                    AddFeaturesActionExecuter.PARAM_ASPECT_NAME, CircabcModel.ASPECT_NEWSGROUP);
            compositeActionNewsGroup.addAction(actionNewsGroup);
            compositeActionNewsGroup.setTitle("Add CircaNewsGroup Aspect");
            compositeActionNewsGroup.setDescription("Add CircaNewsGroup Aspect Description");

            // Create Condition
            final ActionCondition actionConditionNewsGroup =
                    actionService.createActionCondition(IsSubTypeEvaluator.NAME);
            actionConditionNewsGroup.setParameterValue(
                    IsSubTypeEvaluator.PARAM_TYPE, ContentModel.TYPE_CMOBJECT);

            compositeActionNewsGroup.addActionCondition(actionConditionNewsGroup);

            // Create a rule
            final Rule rule = new Rule();
            rule.setRuleType(RuleType.INBOUND);

            rule.applyToChildren(true);
            rule.setExecuteAsynchronously(false);
            rule.setAction(compositeActionNewsGroup);
            rule.setTitle(compositeActionNewsGroup.getTitle());
            rule.setDescription(compositeActionNewsGroup.getDescription());
            ruleService.saveRule(newsGroupNodeRef, rule);
        }
    }

    private void setCircabcSurveyDefaultSettings(
            final NodeRef circabcIGRootNodeRef, final NodeRef nodeRef) {

        final Map<String, Profile> profiles =
                getIGRootProfileManagerService().getProfileMap(circabcIGRootNodeRef);

        HashMap<String, Set<String>> servicesPermissions;

        // Default Profiles
        Set<String> permissionsSet;
        if (profiles.containsKey(IGRootProfileManagerService.Profiles.ACCESS)) {
            // Access
            permissionsSet = new HashSet<>();
            permissionsSet.add(SurveyPermissions.SURACCESS.toString());
            servicesPermissions = new HashMap<>();
            servicesPermissions.put(CircabcServices.SURVEY.toString(), permissionsSet);
            getSurveyProfileManagerService()
                    .updateProfile(
                            nodeRef, IGRootProfileManagerService.Profiles.ACCESS, servicesPermissions, true);
        }
        if (profiles.containsKey(IGRootProfileManagerService.Profiles.AUTHOR)) {
            // Author
            permissionsSet = new HashSet<>();
            permissionsSet.add(SurveyPermissions.SURENCODE.toString());
            servicesPermissions = new HashMap<>();
            servicesPermissions.put(CircabcServices.SURVEY.toString(), permissionsSet);
            getSurveyProfileManagerService()
                    .updateProfile(
                            nodeRef, IGRootProfileManagerService.Profiles.AUTHOR, servicesPermissions, true);
        }
        if (profiles.containsKey(IGRootProfileManagerService.Profiles.CONTRIBUTOR)) {
            // Contributor
            permissionsSet = new HashSet<>();
            permissionsSet.add(SurveyPermissions.SURENCODE.toString());
            servicesPermissions = new HashMap<>();
            servicesPermissions.put(CircabcServices.SURVEY.toString(), permissionsSet);
            getSurveyProfileManagerService()
                    .updateProfile(
                            nodeRef, IGRootProfileManagerService.Profiles.CONTRIBUTOR, servicesPermissions, true);
        }
        if (profiles.containsKey(IGRootProfileManagerService.Profiles.IGLEADER)) {
            // IGLeader
            permissionsSet = new HashSet<>();
            permissionsSet.add(SurveyPermissions.SURADMIN.toString());
            servicesPermissions = new HashMap<>();
            servicesPermissions.put(CircabcServices.SURVEY.toString(), permissionsSet);
            getSurveyProfileManagerService()
                    .updateProfile(
                            nodeRef, IGRootProfileManagerService.Profiles.IGLEADER, servicesPermissions, true);
        }
        if (profiles.containsKey(IGRootProfileManagerService.Profiles.REVIEWER)) {
            // Reviewer
            permissionsSet = new HashSet<>();
            permissionsSet.add(SurveyPermissions.SURENCODE.toString());
            servicesPermissions = new HashMap<>();
            servicesPermissions.put(CircabcServices.SURVEY.toString(), permissionsSet);
            getSurveyProfileManagerService()
                    .updateProfile(
                            nodeRef, IGRootProfileManagerService.Profiles.REVIEWER, servicesPermissions, true);
        }
        if (profiles.containsKey(IGRootProfileManagerService.Profiles.SECRETARY)) {
            // Secretary
            permissionsSet = new HashSet<>();
            permissionsSet.add(SurveyPermissions.SURACCESS.toString());
            servicesPermissions = new HashMap<>();
            servicesPermissions.put(CircabcServices.SURVEY.toString(), permissionsSet);
            getSurveyProfileManagerService()
                    .updateProfile(
                            nodeRef, IGRootProfileManagerService.Profiles.SECRETARY, servicesPermissions, true);
        }

        String profileName;
        // Set Public Permission
        servicesPermissions = new HashMap<>();
        permissionsSet = new HashSet<>();
        profileName = getCircabcRootProfileManagerService().getAllCircaUsersGroupName();

        permissionsSet.add(SurveyPermissions.SURNOACCESS.toString());
        servicesPermissions.put(CircabcServices.SURVEY.toString(), permissionsSet);
        getSurveyProfileManagerService().updateProfile(nodeRef, profileName, servicesPermissions, true);

        // Set Guest Permission
        servicesPermissions = new HashMap<>();
        permissionsSet = new HashSet<>();
        permissionsSet.add(SurveyPermissions.SURNOACCESS.toString());
        servicesPermissions.put(CircabcServices.SURVEY.toString(), permissionsSet);
        profileName = CircabcConstant.GUEST_AUTHORITY;
        getSurveyProfileManagerService().updateProfile(nodeRef, profileName, servicesPermissions, true);

        // Registred permissions
        servicesPermissions = new HashMap<>();
        permissionsSet = new HashSet<>();
        permissionsSet.add(SurveyPermissions.SURNOACCESS.toString());
        servicesPermissions.put(CircabcServices.SURVEY.toString(), permissionsSet);
        getSurveyProfileManagerService()
                .updateProfile(
                        nodeRef,
                        getCircabcRootProfileManagerService().getAllCircaUsersGroupName(),
                        servicesPermissions,
                        true);

        final NodeRef circabcCategoryNodeRef =
                nodeService.getPrimaryParent(circabcIGRootNodeRef).getParentRef();

        final String circabcCategoryAdminGroup =
                getCategoryProfileManagerService()
                        .getProfile(
                                circabcCategoryNodeRef, CategoryProfileManagerService.Profiles.CIRCA_CATEGORY_ADMIN)
                        .getPrefixedAlfrescoGroupName();

        // Add defaults Profiles permissions
        permissionService.setPermission(
                nodeRef, circabcCategoryAdminGroup, SurveyPermissions.SURADMIN.toString(), true);

        /*
         * final HashMap<QName, Serializable> properties = new HashMap<QName,
         * Serializable>(1, 1.0f); properties.put(ContentModel.PROP_OWNER,
         * ADMIN); nodeService.addAspect(nodeRef, ContentModel.ASPECT_OWNABLE,
         * properties);
         *
         */

        // ownableService.setOwner(nodeRef, ADMIN);

        // Default permissions
        // Cut inheritance
        permissionService.setInheritParentPermissions(nodeRef, false);
    }

    /*
     * Use a businesss service that call
     * eu.cec.digit.circabc.business.helper.RulesManager#addAspectToAllChilds(surveyNodeRef, CircabcModel.ASPECT_SURVEY);
     */
    @Deprecated
    private void setDefaultSurveyRules(final NodeRef surveyNodeRef) {
        {
            // Add CircaNewsGroup
            final CompositeAction compositeActionSurvey = actionService.createCompositeAction();

            // Create Action
            final Action actionSurvey = actionService.createAction(AddFeaturesActionExecuter.NAME);
            actionSurvey.setParameterValue(
                    AddFeaturesActionExecuter.PARAM_ASPECT_NAME, CircabcModel.ASPECT_SURVEY);

            compositeActionSurvey.addAction(actionSurvey);
            compositeActionSurvey.setTitle("Add CircaSurvey Aspect");
            compositeActionSurvey.setDescription("Add CircaSurvey Aspect Description");

            // Create Condition
            final ActionCondition actionConditionSurvey =
                    actionService.createActionCondition(IsSubTypeEvaluator.NAME);
            actionConditionSurvey.setParameterValue(
                    IsSubTypeEvaluator.PARAM_TYPE, ContentModel.TYPE_CMOBJECT);

            compositeActionSurvey.addActionCondition(actionConditionSurvey);

            // Create a rule
            final Rule rule = new Rule();
            rule.setRuleType(RuleType.INBOUND);

            rule.applyToChildren(true);
            rule.setExecuteAsynchronously(false);
            rule.setAction(compositeActionSurvey);
            rule.setTitle(compositeActionSurvey.getTitle());
            rule.setDescription(compositeActionSurvey.getDescription());
            ruleService.saveRule(surveyNodeRef, rule);
        }
    }

    private void setEventDefaultSettings(final NodeRef circabcIGRootNodeRef, final NodeRef nodeRef) {

        final Map<String, Profile> profiles =
                getIGRootProfileManagerService().getProfileMap(circabcIGRootNodeRef);

        HashMap<String, Set<String>> servicesPermissions;

        // Default Profiles

        Set<String> permissionsSet;
        if (profiles.containsKey(IGRootProfileManagerService.Profiles.ACCESS)) {
            // Access
            permissionsSet = new HashSet<>();
            permissionsSet.add(EventPermissions.EVEACCESS.toString());
            servicesPermissions = new HashMap<>();
            servicesPermissions.put(CircabcServices.EVENT.toString(), permissionsSet);
            getEventProfileManagerService()
                    .updateProfile(
                            nodeRef, IGRootProfileManagerService.Profiles.ACCESS, servicesPermissions, true);
        }
        if (profiles.containsKey(IGRootProfileManagerService.Profiles.AUTHOR)) {
            // Author
            permissionsSet = new HashSet<>();
            permissionsSet.add(EventPermissions.EVEACCESS.toString());
            servicesPermissions = new HashMap<>();
            servicesPermissions.put(CircabcServices.EVENT.toString(), permissionsSet);
            getEventProfileManagerService()
                    .updateProfile(
                            nodeRef, IGRootProfileManagerService.Profiles.AUTHOR, servicesPermissions, true);
        }
        if (profiles.containsKey(IGRootProfileManagerService.Profiles.CONTRIBUTOR)) {
            // Contributor
            permissionsSet = new HashSet<>();
            permissionsSet.add(EventPermissions.EVEACCESS.toString());
            servicesPermissions = new HashMap<>();
            servicesPermissions.put(CircabcServices.EVENT.toString(), permissionsSet);
            getEventProfileManagerService()
                    .updateProfile(
                            nodeRef, IGRootProfileManagerService.Profiles.CONTRIBUTOR, servicesPermissions, true);
        }
        if (profiles.containsKey(IGRootProfileManagerService.Profiles.IGLEADER)) {
            // IGLeader
            permissionsSet = new HashSet<>();
            permissionsSet.add(EventPermissions.EVEADMIN.toString());
            servicesPermissions = new HashMap<>();
            servicesPermissions.put(CircabcServices.EVENT.toString(), permissionsSet);
            getEventProfileManagerService()
                    .updateProfile(
                            nodeRef, IGRootProfileManagerService.Profiles.IGLEADER, servicesPermissions, true);
        }
        if (profiles.containsKey(IGRootProfileManagerService.Profiles.REVIEWER)) {
            // Reviewer
            permissionsSet = new HashSet<>();
            permissionsSet.add(EventPermissions.EVEACCESS.toString());
            servicesPermissions = new HashMap<>();
            servicesPermissions.put(CircabcServices.EVENT.toString(), permissionsSet);
            getEventProfileManagerService()
                    .updateProfile(
                            nodeRef, IGRootProfileManagerService.Profiles.REVIEWER, servicesPermissions, true);
        }
        if (profiles.containsKey(IGRootProfileManagerService.Profiles.SECRETARY)) {
            // Secretary
            permissionsSet = new HashSet<>();
            permissionsSet.add(EventPermissions.EVENOACCESS.toString());
            servicesPermissions = new HashMap<>();
            servicesPermissions.put(CircabcServices.EVENT.toString(), permissionsSet);
            getEventProfileManagerService()
                    .updateProfile(
                            nodeRef, IGRootProfileManagerService.Profiles.SECRETARY, servicesPermissions, true);
        }

        String profileName;
        // Set Public Permission
        servicesPermissions = new HashMap<>();
        permissionsSet = new HashSet<>();
        profileName = getCircabcRootProfileManagerService().getAllCircaUsersGroupName();

        permissionsSet.add(EventPermissions.EVENOACCESS.toString());
        servicesPermissions.put(CircabcServices.EVENT.toString(), permissionsSet);
        getEventProfileManagerService().updateProfile(nodeRef, profileName, servicesPermissions, true);

        // Set Guest Permission
        servicesPermissions = new HashMap<>();
        permissionsSet = new HashSet<>();
        permissionsSet.add(EventPermissions.EVENOACCESS.toString());
        servicesPermissions.put(CircabcServices.EVENT.toString(), permissionsSet);
        profileName = CircabcConstant.GUEST_AUTHORITY;
        getEventProfileManagerService().updateProfile(nodeRef, profileName, servicesPermissions, true);

        // Registred permissions
        servicesPermissions = new HashMap<>();
        permissionsSet = new HashSet<>();
        permissionsSet.add(EventPermissions.EVENOACCESS.toString());
        servicesPermissions.put(CircabcServices.EVENT.toString(), permissionsSet);
        getEventProfileManagerService()
                .updateProfile(
                        nodeRef,
                        getCircabcRootProfileManagerService().getAllCircaUsersGroupName(),
                        servicesPermissions,
                        true);

        final NodeRef circabcCategoryNodeRef =
                nodeService.getPrimaryParent(circabcIGRootNodeRef).getParentRef();

        final String circabcCategoryAdminGroup =
                getCategoryProfileManagerService()
                        .getProfile(
                                circabcCategoryNodeRef, CategoryProfileManagerService.Profiles.CIRCA_CATEGORY_ADMIN)
                        .getPrefixedAlfrescoGroupName();

        // Add defaults Profiles permissions
        permissionService.setPermission(
                nodeRef, circabcCategoryAdminGroup, EventPermissions.EVEADMIN.toString(), true);

        ownableService.setOwner(nodeRef, ADMIN);
        // Default permissions
        // Cut inheritance
        permissionService.setInheritParentPermissions(nodeRef, false);
    }

    /*
     * Use a businesss service that call
     * eu.cec.digit.circabc.business.helper.RulesManager#addAspectToAllChilds(surveyNodeRef, CircabcModel.ASPECT_EVENT);
     */
    @Deprecated
    private void setDefaultEventRules(final NodeRef eventGroupNodeRef) {
        // Add CircaNewsGroup
        final CompositeAction compositeActionEvent = actionService.createCompositeAction();

        // Create Action
        final Action actionEvent = actionService.createAction(AddFeaturesActionExecuter.NAME);
        actionEvent.setParameterValue(
                AddFeaturesActionExecuter.PARAM_ASPECT_NAME, CircabcModel.ASPECT_EVENT);
        compositeActionEvent.addAction(actionEvent);
        compositeActionEvent.setTitle("Add Event Aspect");
        compositeActionEvent.setDescription("Add Event Aspect Description");

        // Create Condition
        final ActionCondition actionConditionEvent =
                actionService.createActionCondition(IsSubTypeEvaluator.NAME);
        actionConditionEvent.setParameterValue(
                IsSubTypeEvaluator.PARAM_TYPE, ContentModel.TYPE_CMOBJECT);

        compositeActionEvent.addActionCondition(actionConditionEvent);

        // Create a rule
        final Rule rule = new Rule();
        rule.setRuleType(RuleType.INBOUND);

        rule.applyToChildren(true);
        rule.setExecuteAsynchronously(false);
        rule.setAction(compositeActionEvent);
        rule.setTitle(compositeActionEvent.getTitle());
        rule.setDescription(compositeActionEvent.getDescription());
        ruleService.saveRule(eventGroupNodeRef, rule);
    }

    private void setInformationDefaultSettings(
            final NodeRef circabcIGRootNodeRef, final NodeRef nodeRef) {
        final Map<String, Profile> profiles =
                getIGRootProfileManagerService().getProfileMap(circabcIGRootNodeRef);

        HashMap<String, Set<String>> servicesPermissions;

        // Default Profiles

        Set<String> permissionsSet;
        if (profiles.containsKey(IGRootProfileManagerService.Profiles.ACCESS)) {
            // Access
            permissionsSet = new HashSet<>();
            permissionsSet.add(InformationPermissions.INFACCESS.toString());
            servicesPermissions = new HashMap<>();
            servicesPermissions.put(CircabcServices.INFORMATION.toString(), permissionsSet);
            getInformationProfileManagerService()
                    .updateProfile(
                            nodeRef, IGRootProfileManagerService.Profiles.ACCESS, servicesPermissions, true);
        }
        if (profiles.containsKey(IGRootProfileManagerService.Profiles.AUTHOR)) {
            // Author
            permissionsSet = new HashSet<>();
            permissionsSet.add(InformationPermissions.INFACCESS.toString());
            servicesPermissions = new HashMap<>();
            servicesPermissions.put(CircabcServices.INFORMATION.toString(), permissionsSet);
            getInformationProfileManagerService()
                    .updateProfile(
                            nodeRef, IGRootProfileManagerService.Profiles.AUTHOR, servicesPermissions, true);
        }
        if (profiles.containsKey(IGRootProfileManagerService.Profiles.CONTRIBUTOR)) {
            // Contributor
            permissionsSet = new HashSet<>();
            permissionsSet.add(InformationPermissions.INFACCESS.toString());
            servicesPermissions = new HashMap<>();
            servicesPermissions.put(CircabcServices.INFORMATION.toString(), permissionsSet);
            getInformationProfileManagerService()
                    .updateProfile(
                            nodeRef, IGRootProfileManagerService.Profiles.CONTRIBUTOR, servicesPermissions, true);
        }
        if (profiles.containsKey(IGRootProfileManagerService.Profiles.IGLEADER)) {
            // IGLeader
            permissionsSet = new HashSet<>();
            permissionsSet.add(InformationPermissions.INFADMIN.toString());
            servicesPermissions = new HashMap<>();
            servicesPermissions.put(CircabcServices.INFORMATION.toString(), permissionsSet);
            getInformationProfileManagerService()
                    .updateProfile(
                            nodeRef, IGRootProfileManagerService.Profiles.IGLEADER, servicesPermissions, true);
        }
        if (profiles.containsKey(IGRootProfileManagerService.Profiles.REVIEWER)) {
            // Reviewer
            permissionsSet = new HashSet<>();
            permissionsSet.add(InformationPermissions.INFACCESS.toString());
            servicesPermissions = new HashMap<>();
            servicesPermissions.put(CircabcServices.INFORMATION.toString(), permissionsSet);
            getInformationProfileManagerService()
                    .updateProfile(
                            nodeRef, IGRootProfileManagerService.Profiles.REVIEWER, servicesPermissions, true);
        }
        if (profiles.containsKey(IGRootProfileManagerService.Profiles.SECRETARY)) {
            // Secretary
            permissionsSet = new HashSet<>();
            permissionsSet.add(InformationPermissions.INFNOACCESS.toString());
            servicesPermissions = new HashMap<>();
            servicesPermissions.put(CircabcServices.INFORMATION.toString(), permissionsSet);
            getInformationProfileManagerService()
                    .updateProfile(
                            nodeRef, IGRootProfileManagerService.Profiles.SECRETARY, servicesPermissions, true);
        }

        String profileName;
        // Set Public Permission
        servicesPermissions = new HashMap<>();
        permissionsSet = new HashSet<>();
        profileName = getCircabcRootProfileManagerService().getAllCircaUsersGroupName();

        permissionsSet.add(InformationPermissions.INFNOACCESS.toString());
        servicesPermissions.put(CircabcServices.INFORMATION.toString(), permissionsSet);
        getInformationProfileManagerService()
                .updateProfile(nodeRef, profileName, servicesPermissions, true);

        // Set Guest Permission
        servicesPermissions = new HashMap<>();
        permissionsSet = new HashSet<>();
        permissionsSet.add(InformationPermissions.INFNOACCESS.toString());
        servicesPermissions.put(CircabcServices.INFORMATION.toString(), permissionsSet);
        profileName = CircabcConstant.GUEST_AUTHORITY;
        getInformationProfileManagerService()
                .updateProfile(nodeRef, profileName, servicesPermissions, true);

        // Registred permissions
        servicesPermissions = new HashMap<>();
        permissionsSet = new HashSet<>();
        permissionsSet.add(InformationPermissions.INFNOACCESS.toString());
        servicesPermissions.put(CircabcServices.INFORMATION.toString(), permissionsSet);
        getInformationProfileManagerService()
                .updateProfile(
                        nodeRef,
                        getCircabcRootProfileManagerService().getAllCircaUsersGroupName(),
                        servicesPermissions,
                        true);

        final NodeRef circabcCategoryNodeRef =
                nodeService.getPrimaryParent(circabcIGRootNodeRef).getParentRef();

        final String circabcCategoryAdminGroup =
                getCategoryProfileManagerService()
                        .getProfile(
                                circabcCategoryNodeRef, CategoryProfileManagerService.Profiles.CIRCA_CATEGORY_ADMIN)
                        .getPrefixedAlfrescoGroupName();

        // Add defaults Profiles permissions
        permissionService.setPermission(
                nodeRef, circabcCategoryAdminGroup, InformationPermissions.INFADMIN.toString(), true);

        ownableService.setOwner(nodeRef, ADMIN);

        // Default permissions
        // Cut inheritance
        permissionService.setInheritParentPermissions(nodeRef, false);
    }

    /*
     * Use a businesss service that call
     * eu.cec.digit.circabc.business.helper.RulesManager#addAspectToAllChilds(informationNodeRef, CircabcModel.ASPECT_INFORMATION);
     */
    @Deprecated
    private void setDefaultInformationRules(final NodeRef informationNodeRef) {
        // Add Information
        final CompositeAction compositeActionInformation = actionService.createCompositeAction();

        // Create Action
        final Action actionInformation = actionService.createAction(AddFeaturesActionExecuter.NAME);
        actionInformation.setParameterValue(
                AddFeaturesActionExecuter.PARAM_ASPECT_NAME, CircabcModel.ASPECT_INFORMATION);
        compositeActionInformation.addAction(actionInformation);
        compositeActionInformation.setTitle("Add Information Aspect");
        compositeActionInformation.setDescription("Add Information Aspect Description");

        // Create Condition
        final ActionCondition actionConditionInformation =
                actionService.createActionCondition(IsSubTypeEvaluator.NAME);
        actionConditionInformation.setParameterValue(
                IsSubTypeEvaluator.PARAM_TYPE, ContentModel.TYPE_CMOBJECT);

        compositeActionInformation.addActionCondition(actionConditionInformation);

        // Create a rule
        final Rule rule = new Rule();
        rule.setRuleType(RuleType.INBOUND);

        rule.applyToChildren(true);
        rule.setExecuteAsynchronously(false);
        rule.setAction(compositeActionInformation);
        rule.setTitle(compositeActionInformation.getTitle());
        rule.setDescription(compositeActionInformation.getDescription());
        ruleService.saveRule(informationNodeRef, rule);
    }

    /**
     * Get the company home node reference
     */
    public final NodeRef getCompanyHomeNodeRef() {
        if (companyHomeNodeRef == null) {
            // Get the company home node
            companyHomeNodeRef = getNodeRefFromPath(APP_COMPANY_HOME_PATH);
        }
        return companyHomeNodeRef;
    }

    private NodeRef getNodeRefFromPath(String path) {
        return getNodesFromPath(path).get(0);
    }

    public List<NodeRef> getCategoryHeaders(final NodeRef nodeRef) {
        if (!nodeService.hasAspect(nodeRef, ContentModel.ASPECT_GEN_CLASSIFIABLE)) {
            if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_CATEGORY)) {

                logger.error("A category is found without being linked to a Category Header.");

                throw new IllegalStateException("All the Categories MUST be linked to a Category Header.");
            }

            return Collections.emptyList();
        }

        final List<NodeRef> cats =
                (List<NodeRef>) nodeService.getProperty(nodeRef, ContentModel.PROP_CATEGORIES);

        if (cats == null) {
            if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_CATEGORY)) {

                logger.error("A category is found without being linked to a Category Header.");

                throw new IllegalStateException("All the Categories MUST be linked to a Category Header.");
            }

            return Collections.emptyList();
        } else {
            return cats;
        }
    }

    public Map<NodeRef, List<NodeRef>> getCategoryHeadersMap() {
        // get all Categories
        final List<NodeRef> categories = getCategories();

        if (categories == null || categories.isEmpty()) {
            return Collections.emptyMap();
        }

        // construct the map to return
        final Map<NodeRef, List<NodeRef>> categoryHeaderMap = new HashMap<>(categories.size());
        List<NodeRef> categoryHeaders;
        List<NodeRef> values;
        for (final NodeRef category : categories) {
            // get the Category Headers of the Category
            categoryHeaders = getCategoryHeaders(category);
            for (final NodeRef categoryHeader : categoryHeaders) {
                // get the categories for the current Category Header
                values = categoryHeaderMap.get(categoryHeader);

                // if the values is null, instatiate them
                if (values == null) {
                    values = new ArrayList<>();
                }

                categoryHeaderMap.put(categoryHeader, values);
            }
        }

        return categoryHeaderMap;
    }

    public Map<NodeRef, List<NodeRef>> getCategoryMap() {
        // get all Categories
        final List<NodeRef> categories = getCategories();

        if (categories == null || categories.isEmpty()) {
            return Collections.emptyMap();
        }

        // construct the map to return
        final Map<NodeRef, List<NodeRef>> categoryMap = new HashMap<>(categories.size());
        List<NodeRef> categoryHeaders;
        for (final NodeRef ref : categories) {
            // get the Category Headers of the Category
            categoryHeaders = getCategoryHeaders(ref);
            categoryMap.put(ref, categoryHeaders);
        }

        return categoryMap;
    }

    public List<NodeRef> getExistingCategoryHeaders() {
        final long start = System.currentTimeMillis();
        List<NodeRef> headerNodes = null;

        final NodeRef circaBCHeadersNodeRef = getRootCategoryHeader();

        if (circaBCHeadersNodeRef == null) {
            if (logger.isWarnEnabled()) {
                logger.warn(
                        "No circabc Category Headers found under the known Category Root Location : "
                                + getCategoryHeaderRootLucenePath());
            }

            // There's not CircaBCHeader Category
            return Collections.emptyList();
        }

        final Collection<ChildAssociationRef> childRefs =
                categoryService.getChildren(circaBCHeadersNodeRef, Mode.SUB_CATEGORIES, Depth.IMMEDIATE);

        headerNodes = new ArrayList<>(childRefs.size());

        NodeRef nodeRef;
        for (final ChildAssociationRef ref : childRefs) {
            // create our Node representation from the NodeRef
            nodeRef = ref.getChildRef();

            headerNodes.add(nodeRef);
        }

        final long duration = System.currentTimeMillis() - start;

        if (logger.isDebugEnabled()) {
            logger.debug(
                    headerNodes.size()
                            + " Category Headers found: "
                            + headerNodes
                            + " in "
                            + duration
                            + " milliseconds.");
        }

        return headerNodes;
    }

    public NodeRef getRootCategoryHeader() {
        //
        if (rootCategoryHeader == null) {
            final String path = getCategoryHeaderRootLucenePath();

            final List<NodeRef> nodeRefList = getNodesFromPath(path);

            if (nodeRefList != null && nodeRefList.size() > 0) {
                rootCategoryHeader = nodeRefList.get(0);
            }

            if (rootCategoryHeader != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Found Node: " + rootCategoryHeader);
                    logger.debug("CircaBCHeader found at:" + nodeService.getPath(rootCategoryHeader));
                }
            } else if (logger.isDebugEnabled()) {
                logger.debug("CircaBCHeader not found");
            }
        }
        return rootCategoryHeader;
    }

    public List<NodeRef> getCategories() {
        final List<FileInfo> childsAssoc = fileFolderService.listFolders(getCircabcNodeRef());

        final List<NodeRef> toReturn = new ArrayList<>(childsAssoc.size());
        for (final FileInfo info : childsAssoc) {
            toReturn.add(info.getNodeRef());
        }

        return toReturn;
    }

    public List<NodeRef> getInterestGroups(final NodeRef category) {
        final List<ChildAssociationRef> assocs =
                getServiceRegistry().getNodeService().getChildAssocs(category);

        List<NodeRef> interestGroupsNodes = new ArrayList<>(assocs.size());

        NodeRef ref = null;

        for (ChildAssociationRef assoc : assocs) {
            ref = assoc.getChildRef();

            // Secure the list of ig. No other kind of spaces can be returned
            if (nodeService.hasAspect(ref, CircabcModel.ASPECT_IGROOT)) {
                interestGroupsNodes.add(ref);
            }
        }

        return interestGroupsNodes;
    }

    public NodeRef getCategory(String categoryName) {
        return nodeService.getChildByName(
                getCircabcNodeRef(), ContentModel.ASSOC_CONTAINS, categoryName);
    }

    public NodeRef getCircabcNodeRef() {
        if (circabcNodeRef == null) {
            final List<NodeRef> circabcNodeRefs = getNodesFromPath(getCircabcLucenePath());

            if (circabcNodeRefs != null && circabcNodeRefs.size() > 0) {
                circabcNodeRef = circabcNodeRefs.get(0);
            }

            if (circabcNodeRef != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Found Node: " + circabcNodeRef);
                    logger.debug("CircaBC found at:" + nodeService.getPath(circabcNodeRef));
                }

                if (nodeService.hasAspect(circabcNodeRef, CircabcModel.ASPECT_CIRCABC_ROOT)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("CircaBC has CIRCABC Aspect");
                    }
                } else {
                    logger.warn("CircaBC has no CIRCABC Aspect");
                }
            }
        }
        return circabcNodeRef;
    }

    public NodeRef getAlfrescoDictionaryNodeRef() {
        final List<NodeRef> nodeRefs = getNodesFromPath(getAlfrescoDictionaryLucenePath());

        if (nodeRefs != null && nodeRefs.size() > 0) {
            alfrescoDictionary = nodeRefs.get(0);
        }

        if (alfrescoDictionary != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Found Node: " + alfrescoDictionary);
                logger.debug("alfrescoDictionary found at:" + nodeService.getPath(alfrescoDictionary));
            }
        }
        return alfrescoDictionary;
    }

    public NodeRef getCircabcDictionaryNodeRef() {
        if (circabcDictionary == null) {
            final NodeRef dictionaryNodeRef = getAlfrescoDictionaryNodeRef();
            circabcDictionary =
                    nodeService.getChildByName(
                            dictionaryNodeRef, ContentModel.ASSOC_CONTAINS, CIRCABC_DD_FOLDER);

            if (circabcDictionary == null) {
                circabcDictionary =
                        fileFolderService
                                .create(dictionaryNodeRef, CIRCABC_DD_FOLDER, ContentModel.TYPE_FOLDER)
                                .getNodeRef();

                logger.info("Circabc data dictionanry successfully created: " + circabcDictionary);
            }
        }

        return circabcDictionary;
    }

    public String getCircabcLucenePath() {
        if (circabcLucenePath == null) {
            circabcLucenePath =
                    APP_COMPANY_HOME_PATH
                            + "/cm:"
                            + CircabcConfiguration.getProperty(
                            CircabcConfiguration.CIRCABC_ROOT_NODE_NAME_PROPERTIES);
        }
        return this.circabcLucenePath;
    }

    public String getMTLucenePath() {
        if (mtLucenePath == null) {
            mtLucenePath =
                    APP_COMPANY_HOME_PATH
                            + "/cm:"
                            + CircabcConfiguration.getProperty(CircabcConfiguration.MT_ROOT_NODE_NAME_PROPERTIES);
        }
        return this.mtLucenePath;
    }

    public String getAlfrescoDictionaryLucenePath() {
        if (alfrescoDictionaryPath == null) {
            alfrescoDictionaryPath = APP_COMPANY_HOME_PATH + APP_ALFRESCO_DICTIONARY_LUCENE_PATH;
        }
        return this.alfrescoDictionaryPath;
    }

    private String getCategoryHeaderRootLucenePath() {
        if (categoryHeaderLucenePath == null) {
            categoryHeaderLucenePath =
                    "/cm:categoryRoot/cm:generalclassifiable/cm:"
                            + CircabcConfiguration.getProperty(CircabcConfiguration.ROOT_HEADER_NAME_PROPERTIES);
        }
        return this.categoryHeaderLucenePath;
    }

    public boolean isCircabcUser(final String userName) {
        final String userGroupName =
                getCircabcRootProfileManagerService().getMasterInvitedGroupName(getCircabcNodeRef());

        final String prefixedUserGroupName =
                authorityService.getName(AuthorityType.GROUP, userGroupName);

        final Set<String> containingAuthorithies =
                authorityService.getContainingAuthorities(AuthorityType.GROUP, userName, false);
        return containingAuthorithies.contains(prefixedUserGroupName);
    }

    public boolean hasGuestVisibility(final NodeRef igRootNodeRef) {
        final Profile profile =
                getIGRootProfileManagerService().getProfile(igRootNodeRef, CircabcConstant.GUEST_AUTHORITY);
        final Set<String> permissions =
                profile.getServicePermissions(CircabcServices.VISIBILITY.toString());
        if (permissions.contains(VisibilityPermissions.VISIBILITY.toString())) {
            return true;
        } else {
            return false;
        }
    }

    public boolean hasAllCircabcUsersVisibility(final NodeRef igRootNodeRef) {
        final String allUserGroupName =
                getCircabcRootProfileManagerService().getAllCircaUsersGroupName();
        final Profile profile =
                getIGRootProfileManagerService().getProfile(igRootNodeRef, allUserGroupName);
        final Set<String> permissions =
                profile.getServicePermissions(CircabcServices.VISIBILITY.toString());
        if (permissions.contains(VisibilityPermissions.VISIBILITY.toString())) {
            return true;
        } else {
            return false;
        }
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.service.struct.ManagementService#getCurrentInterestGroup(org.alfresco.service.cmr.repository.NodeRef)
     */
    public NodeRef getCurrentInterestGroup(final NodeRef currentNodeRef) {
        if (currentNodeRef == null) {
            throw new NullPointerException("NodeRef is a mandatory parameter.");
        }

        NodeRef tempNodeRef = currentNodeRef;
        // go up to the root of the IG
        for (; ; ) {
            if (tempNodeRef == null) {
                break;
            }

            if (getNodeService().getType(tempNodeRef).equals(ContentModel.TYPE_MULTILINGUAL_CONTAINER)) {
                tempNodeRef = getMultilingualContentService().getPivotTranslation(tempNodeRef);
            }

            if (!getNodeService().hasAspect(tempNodeRef, CircabcModel.ASPECT_IGROOT)) {
                tempNodeRef = getNodeService().getPrimaryParent(tempNodeRef).getParentRef();
            } else {
                break;
            }
        }

        return tempNodeRef;
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.service.struct.ManagementService#getCurrentLibrary(org.alfresco.service.cmr.repository.NodeRef)
     */
    public NodeRef getCurrentLibrary(final NodeRef currentNodeRef) {
        if (currentNodeRef == null) {
            throw new NullPointerException("NodeRef is a mandatory parameter.");
        }

        NodeRef tempNodeRef = currentNodeRef;
        if (getNodeService().hasAspect(tempNodeRef, CircabcModel.ASPECT_IGROOT)) {
            tempNodeRef = getFirstChildByAspect(tempNodeRef, CircabcModel.ASPECT_LIBRARY_ROOT);
        } else {

            for (; ; ) {
                if (tempNodeRef == null) {
                    break;
                }

                if (getNodeService()
                        .getType(tempNodeRef)
                        .equals(ContentModel.TYPE_MULTILINGUAL_CONTAINER)) {
                    tempNodeRef = getMultilingualContentService().getPivotTranslation(tempNodeRef);
                }

                if (getNodeService().hasAspect(tempNodeRef, CircabcModel.ASPECT_IGROOT)) {
                    tempNodeRef = getFirstChildByAspect(tempNodeRef, CircabcModel.ASPECT_LIBRARY_ROOT);
                }

                if (!getNodeService().hasAspect(tempNodeRef, CircabcModel.ASPECT_LIBRARY_ROOT)) {
                    tempNodeRef = getNodeService().getPrimaryParent(tempNodeRef).getParentRef();
                } else {
                    break;
                }
            }
        }
        return tempNodeRef;
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.service.struct.ManagementService#getCurrentNewsGroup(org.alfresco.service.cmr.repository.NodeRef)
     */
    public NodeRef getCurrentNewsGroup(final NodeRef currentNodeRef) {
        if (currentNodeRef == null) {
            throw new NullPointerException("NodeRef is a mandatory parameter.");
        }

        NodeRef tempNodeRef = currentNodeRef;

        if (getNodeService().hasAspect(tempNodeRef, CircabcModel.ASPECT_IGROOT)) {
            tempNodeRef = getFirstChildByAspect(tempNodeRef, CircabcModel.ASPECT_NEWSGROUP_ROOT);
        } else {
            // go up to the root of the IG
            // TODO : Change ASPECT_CIRCA_NEWSGROUP to ASPECT_CIRCA_NEWSGROUP_ROOT
            while (tempNodeRef != null
                    && !getNodeService().hasAspect(tempNodeRef, CircabcModel.ASPECT_NEWSGROUP)) {
                tempNodeRef = getNodeService().getPrimaryParent(tempNodeRef).getParentRef();
            }
        }
        return tempNodeRef;
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.service.struct.ManagementService#getCurrentSurvey(org.alfresco.service.cmr.repository.NodeRef)
     */
    public NodeRef getCurrentSurvey(final NodeRef currentNodeRef) {
        if (currentNodeRef == null) {
            throw new NullPointerException("NodeRef is a mandatory parameter.");
        }

        NodeRef tempNodeRef = currentNodeRef;

        if (getNodeService().hasAspect(tempNodeRef, CircabcModel.ASPECT_IGROOT)) {
            tempNodeRef = getFirstChildByAspect(tempNodeRef, CircabcModel.ASPECT_SURVEY_ROOT);
        } else {
            // go up to the root of the IG
            while (tempNodeRef != null
                    && !getNodeService().hasAspect(tempNodeRef, CircabcModel.ASPECT_SURVEY)) {
                tempNodeRef = getNodeService().getPrimaryParent(tempNodeRef).getParentRef();
            }
        }
        return tempNodeRef;
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.service.struct.ManagementService#getCurrentCategory(org.alfresco.service.cmr.repository.NodeRef)
     */
    public NodeRef getCurrentCategory(final NodeRef currentNodeRef) {
        if (currentNodeRef == null) {
            throw new NullPointerException("NodeRef is a mandatory parameter.");
        }

        NodeRef tempNodeRef = currentNodeRef;
        // go up to the root of the Categorie
        while (tempNodeRef != null
                && !getNodeService().hasAspect(tempNodeRef, CircabcModel.ASPECT_CATEGORY)) {
            tempNodeRef = getNodeService().getPrimaryParent(tempNodeRef).getParentRef();
        }

        return tempNodeRef;
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.service.struct.ManagementService#getCurrentForum(org.alfresco.service.cmr.repository.NodeRef)
     */
    public NodeRef getCurrentForum(final NodeRef currentNodeRef) {
        if (currentNodeRef == null) {
            throw new NullPointerException("NodeRef is a mandatory parameter.");
        }

        NodeRef tempNodeRef = currentNodeRef;
        // @TODO to check if it works ....
        // QName type = nodeService.getType(tempNodeRef);
        QName type = getNodeService().getType(tempNodeRef);

        // make sure the type is defined in the data dictionary
        TypeDefinition typeDef = getDictionaryService().getType(type);
        while (tempNodeRef != null
                && typeDef != null
                && !(ForumModel.TYPE_FORUM.equals(type)
                || getDictionaryService().isSubClass(type, ForumModel.TYPE_FORUM))) {
            tempNodeRef = getNodeService().getPrimaryParent(tempNodeRef).getParentRef();
            type = getNodeService().getType(tempNodeRef);
            typeDef = getDictionaryService().getType(type);
        }
        if (tempNodeRef != null && typeDef != null) {
            // Found it - Simply return it below
        } else {
            // Should never be here
            tempNodeRef = null;
        }

        return tempNodeRef;
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.service.struct.ManagementService#getCurrentTopic(org.alfresco.service.cmr.repository.NodeRef)
     */
    public NodeRef getCurrentTopic(final NodeRef currentNodeRef) {
        if (currentNodeRef == null) {
            throw new NullPointerException("NodeRef is a mandatory parameter.");
        }

        NodeRef tempNodeRef = currentNodeRef;

        final QName type = nodeService.getType(tempNodeRef);
        // make sure the type is defined in the data dictionary
        final TypeDefinition typeDef = getDictionaryService().getType(type);
        if (typeDef != null) {
            // look for Topic
            if (ForumModel.TYPE_TOPIC.equals(type)) {
                // Nothing to do
            } else if (ForumModel.TYPE_POST.equals(type)
                    || getDictionaryService().isSubClass(type, ForumModel.TYPE_POST)) {
                // Get the primary parent to start the list
                tempNodeRef = nodeService.getPrimaryParent(tempNodeRef).getParentRef();
            } else if (getDictionaryService().isSubClass(type, ForumModel.TYPE_TOPIC)) {
                // Nothing to do
                // Optimisation of code
            } else {
                // Should never be here
                tempNodeRef = null;
            }
        } else {
            // Should never be here
            tempNodeRef = null;
        }
        return tempNodeRef;
    }

    public List<NodeRef> luceneSearch(
            final NodeRef contextNodeRef, final boolean folders, final boolean files) {
        final NodeRef contextNodeRefName = tenantService.getName(contextNodeRef);

        final SearchParameters params = new SearchParameters();
        params.setLanguage(SearchService.LANGUAGE_LUCENE);
        params.addStore(contextNodeRefName.getStoreRef());
        // set the parent parameter
        final QueryParameterDefinition parentParamDef =
                new QueryParameterDefImpl(
                        PARAM_QNAME_PARENT, dataTypeNodeRef, true, contextNodeRefName.toString());
        params.addQueryParameterDefinition(parentParamDef);
        if (folders && files) // search for both files and folders
        {
            params.setQuery(LUCENE_QUERY_SHALLOW_ALL);
        } else if (folders) // search for folders only
        {
            params.setQuery(LUCENE_QUERY_SHALLOW_FOLDERS);
        } else if (files) // search for files only
        {
            params.setQuery(LUCENE_QUERY_SHALLOW_FILES);
        } else {
            throw new IllegalArgumentException("Must search for either files or folders or both");
        }

        ResultSet rs = null;
        final List<NodeRef> nodeRefs = new ArrayList<>();
        try {
            rs = searchService.query(params);
            NodeRef nodeRef = null;
            for (final ResultSetRow row : rs) {
                nodeRef = row.getNodeRef();
                if (permissionService
                        .hasPermission(nodeRef, PermissionService.READ)
                        .equals(AccessStatus.ALLOWED)) {
                    nodeRefs.add(nodeRef);
                }
            }
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
        // done
        return nodeRefs;
    }

    /**
     * Util method that return the first node contained in a parent that match the given aspect.
     *
     * @param parentNodeRef the node in which perform the search
     * @param aspectQName   the aspect that the child must match
     * @return the node matched by the aspect or nul if not found
     */
    private NodeRef getFirstChildByAspect(NodeRef parentNodeRef, QName aspectQName) {
        NodeRef matchedChild = null;

        List<ChildAssociationRef> childAssocs = getNodeService().getChildAssocs(parentNodeRef);
        for (ChildAssociationRef assoc : childAssocs) {
            if (getNodeService().hasAspect(assoc.getChildRef(), aspectQName)) {
                matchedChild = assoc.getChildRef();
                break;
            }
        }

        return matchedChild;
    }

    // ///////// Helpers

    /**
     * Helper to return the nodeRefs from a Path
     *
     * @return NodeRef
     */
    private List<NodeRef> getNodesFromPath(final String path) {

        if (rootNode == null) {
            rootNode = nodeService.getRootNode(storeRef);
        }

        return searchService.selectNodes(rootNode, path, null, namespacePrefixResolver, false);
    }

    // / Inject the depedencies

    /**
     * @return the actionService
     */
    public ActionService getActionService() {
        return actionService;
    }

    /**
     * @param actionService the actionService to set
     */
    public void setActionService(ActionService actionService) {
        this.actionService = actionService;
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

    /**
     * @return the nodeService
     */
    public NodeService getNodeService() {
        return nodeService;
    }

    /**
     * @param nodeService the nodeService to set
     */
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /**
     * @return the permissionService
     */
    public PermissionService getPermissionService() {
        return permissionService;
    }

    /**
     * @param permissionService the permissionService to set
     */
    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    /**
     * @return the ruleService
     */
    public RuleService getRuleService() {
        return ruleService;
    }

    /**
     * @param ruleService the ruleService to set
     */
    public void setRuleService(RuleService ruleService) {
        this.ruleService = ruleService;
    }

    /**
     * @return the searchService
     */
    public SearchService getSearchService() {
        return searchService;
    }

    /**
     * @param searchService the searchService to set
     */
    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    /**
     * @return the authorityService
     */
    public final AuthorityService getAuthorityService() {
        return authorityService;
    }

    /**
     * @param authorityService the authorityService to set
     */
    public final void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    /**
     * @return the authenticationService
     */
    public final AuthenticationService getAuthenticationService() {
        return authenticationService;
    }

    /**
     * @param authenticationService the authenticationService to set
     */
    public final void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * @return the ownableService
     */
    public final OwnableService getOwnableService() {
        return ownableService;
    }

    /**
     * @param ownableService the ownableService to set
     */
    public final void setOwnableService(OwnableService ownableService) {
        this.ownableService = ownableService;
    }

    public ServiceRegistry getServiceRegistry() {
        return serviceRegistry;
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public ContentService getContentService() {
        return contentService;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    /**
     * @return the dictionaryService
     */
    public DictionaryService getDictionaryService() {
        return dictionaryService;
    }

    /**
     * @param dictionaryService the dictionaryService to set
     */
    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
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

    /**
     * @return the profileManagerServiceFactory
     */
    public final ProfileManagerServiceFactory getProfileManagerServiceFactory() {
        return profileManagerServiceFactory;
    }

    /**
     * @param profileManagerServiceFactory the profileManagerServiceFactory to set
     */
    public final void setProfileManagerServiceFactory(
            ProfileManagerServiceFactory profileManagerServiceFactory) {
        this.profileManagerServiceFactory = profileManagerServiceFactory;
    }

    /**
     * @return the NewsGroupProfileManagerService
     */
    protected final NewsGroupProfileManagerService getNewsGroupProfileManagerService() {
        return getProfileManagerServiceFactory().getNewsGroupProfileManagerService();
    }

    /**
     * @return the circabcRootProfileManagerService()
     */
    protected final CircabcRootProfileManagerService getCircabcRootProfileManagerService() {
        return getProfileManagerServiceFactory().getCircabcRootProfileManagerService();
    }

    /**
     * @return the categoryProfileManagerService
     */
    protected final CategoryProfileManagerService getCategoryProfileManagerService() {
        return getProfileManagerServiceFactory().getCategoryProfileManagerService();
    }

    /**
     * @return the IGRootProfileManagerService
     */
    protected final IGRootProfileManagerService getIGRootProfileManagerService() {
        return getProfileManagerServiceFactory().getIGRootProfileManagerService();
    }

    /**
     * @return the LibraryProfileManagerService
     */
    protected final LibraryProfileManagerService getLibraryProfileManagerService() {
        return getProfileManagerServiceFactory().getLibraryProfileManagerService();
    }

    /**
     * @return the SurveyProfileManagerService
     */
    protected final SurveyProfileManagerService getSurveyProfileManagerService() {
        return getProfileManagerServiceFactory().getSurveyProfileManagerService();
    }

    /**
     * @return the EventProfileManagerService
     */
    protected final EventProfileManagerService getEventProfileManagerService() {
        return getProfileManagerServiceFactory().getEventProfileManagerService();
    }

    /**
     * @return the InformationProfileManagerService
     */
    protected final InformationProfileManagerService getInformationProfileManagerService() {
        return getProfileManagerServiceFactory().getInformationProfileManagerService();
    }

    public TenantService getTenantService() {
        return tenantService;
    }

    public void setTenantService(final TenantService tenantService) {
        this.tenantService = tenantService;
    }

    /**
     * @param nodePreferencesService the nodePreferencesService to set
     */
    public final void setNodePreferencesService(NodePreferencesService nodePreferencesService) {
        this.nodePreferencesService = nodePreferencesService;
    }

    /**
     * @param categoryService the categoryService to set
     */
    public final void setCategoryService(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    public NodeRef getGuestHomeNodeRef() {
        if (guestUserHomeNodeRef == null) {
            // Get the company home node
            guestUserHomeNodeRef = getNodeRefFromPath(GUEST_USER_HOME_PATH);
        }
        return guestUserHomeNodeRef;
    }

    /**
     * @return the navigationPreferences
     */
    public NavigationPreferencesService getNavigationPreferencesService() {
        return navigationPreferencesService;
    }

    /**
     * @param navigationPreferences the navigationPreferences to set
     */
    public void setNavigationPreferencesService(NavigationPreferencesService navigationPreferences) {
        this.navigationPreferencesService = navigationPreferences;
    }

    @Override
    @NotAuditable
    public NodeRef getMTNodeRef() {
        if (mtNodeRef == null) {
            final List<NodeRef> mtNodeRefs = getNodesFromPath(getMTLucenePath());
            if (mtNodeRefs != null && mtNodeRefs.size() > 0) {
                mtNodeRef = mtNodeRefs.get(0);
            }
        }
        return mtNodeRef;
    }

    /**
     * @param namespacePrefixResolver the namespacePrefixResolver to set
     */
    public void setNamespacePrefixResolver(NamespacePrefixResolver namespacePrefixResolver) {
        this.namespacePrefixResolver = namespacePrefixResolver;
    }
}
