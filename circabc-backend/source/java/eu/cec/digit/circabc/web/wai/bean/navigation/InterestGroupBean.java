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

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.service.log.LogRecord;
import eu.cec.digit.circabc.service.log.LogService;
import eu.cec.digit.circabc.util.PathUtils;
import eu.cec.digit.circabc.web.PermissionUtils;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNode;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNodeType;
import eu.cec.digit.circabc.web.bean.override.CircabcBrowseBean;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.model.RenditionModel;
import org.alfresco.repo.cache.MemoryCache;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.CachingDateFormat;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.repository.User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.owasp.esapi.ESAPI;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

import static eu.cec.digit.circabc.web.WebClientHelper.getPathFromSpaceRef;

/**
 * Bean that backs the navigation inside an Interest Group
 *
 * @author yanick pignot
 */
public class InterestGroupBean extends BaseWaiNavigator {

    public static final String IG = "Interest Group";
    public static final String IG_ACCESS = "IG Access";
    public static final String IG_ACCESS_DESCRIPTION = "IG has been accessed.";
    public static final String JSP_NAME = "ig-home.jsp";
    public static final String BEAN_NAME = "InterestGroupBean";
    public static final String MSG_PAGE_DESCRIPTION = "igroot_home_title_desc";
    protected static final String GIF = ".gif";
    protected static final String IMAGES_ICONS = "/images/icons/";
    protected static final String ICON = "icon";
    protected static final String CREATE_CHILDREN = "CreateChildren";
    protected static final String ADD_CONTENT_TOOLTIP = "library_add_content_tooltip";
    protected static final String ADD_CONTENT_BEAN_START = "CircabcAddContentBean.start";
    protected static final String ADD_CONTENT = "library_add_content";
    protected static final String CREATE_SPACE_ACTION = "library_create_space_action";
    protected static final String CREATE_SPACE_TOOLTIP = "library_create_space_tooltip";
    protected static final String DIALOG_MANAGER_SETUP_PARAMETERS = "WaiDialogManager.setupParameters";
    protected static final String WAI_DIALOG_CREATE_SPACE_WAI = "wai:dialog:createSpaceWai";
    protected static final String BULK_UPLOAD_BEAN_START = "BulkUploadBean.start";
    protected static final String BULK_UPLOAD_ACTION = "bulk_upload_action";
    protected static final String ID = "id";
    private static final String ESCAPE_QUOTES = "\" ";
    private static final String START_PROP_SEARCH = "@";
    private static final String CLOSE_QUERY = " )";
    private static final String OPEN_QUERY = "( ";
    private static final String SEARCH_RESULTS_RETURNED = "Search results returned: ";
    private static final String TYPE = ", type = ";
    private static final String FOUND_INVALID_OBJECT_IN_DATABASE_ID = "Found invalid object in database: id = ";
    private static final String SEARCH_FAILED_FOR = "Search failed for: ";
    private static final String MODIFIED = "modified";
    private static final String ESCAPE4 = "\\\\:";
    private static final String ESCAPE3 = "\\:";
    private static final String TO = " TO ";
    private static final String OPEN_BRACKETS = ":[";
    private static final String CLOSE_BRACKETS = "] ";
    private static final String AND = " AND ";
    private static final String PATH = "PATH:";
    private static final String MS = "ms";
    private static final String TIME_TO_QUERY_AND_BUILD_MAP_NODES = "Time to query and build map nodes: ";
    private static final String CREATE_FILE_LINK_REPRESENTATION_NOT_OPTIMIZED_FOR_TYPE = "createFileLinkRepresentation not optimized for type:";
    private static final String CREATE_FOLDER_LINK_REPRESENTATION_NOT_OPTIMIZED_FOR_TYPE = "createFolderLinkRepresentation not optimized for type:";
    private static final String CREATE_FOLDER_REPRESENTATION_NOT_OPTIMIZED_FOR_TYPE = "createFolderRepresentation not optimized for type:";
    private static final String CREATE_NODE_CONTENT_REPRESENTATION_NOT_OPTIMIZED_FOR_TYPE = "createNodeContentRepresentation not optimized for type:";
    private static final String ESCAPE1 = "\\-";
    private static final String ESCAPE2 = "\\\\-";
    /**
     *
     */
    private static final long serialVersionUID = -6967164595499663893L;
    private static final Log logger = LogFactory.getLog(InterestGroupBean.class);
    //	Code Optimisation - Constants
    final private static String PROP_MODIFIED_ESCAPED = ContentModel.PROP_MODIFIED.toString()
            .replaceAll(":", ESCAPE4).replaceAll("\\{", "\\\\{").replaceAll("\\}", "\\\\}");
    final private static String PROP_CREATED_ESCAPED = ContentModel.PROP_CREATED.toString()
            .replaceAll(":", ESCAPE4).replaceAll("\\{", "\\\\{").replaceAll("\\}", "\\\\}");
    final private static String TYPE_FILE_ESCAPED = ContentModel.TYPE_CONTENT.toString()
            .replaceAll(":", ESCAPE4).replaceAll("\\{", "\\\\{").replaceAll("\\}", "\\\\}");
    final private static String TYPE_FOLDER_ESCAPED = ContentModel.TYPE_FOLDER.toString()
            .replaceAll(":", ESCAPE4).replaceAll("\\{", "\\\\{").replaceAll("\\}", "\\\\}");
    final private Map<QName, TypeDefinition> validTypeMap = new HashMap<>();
    /**
     * The number of days in which the what's new sarch should be performed
     */
    //public static final int WHAT_NEW_INTERVAL_DATE_DAYS = 7;
    public int dateInterval = 1;
    public int maxEntries = 10;
    private SimpleCache<NodeRef, WhatsNewResult> whatsNewNodesCache;

    // Caches locally (igId++sessionId, username)
    private SimpleCache<String, String> sessionIdsCache = new MemoryCache<>();

    /**
     * internalSearchService bean reference
     */
    transient private SearchService internalSearchService;
    transient private LogService logService;

    public NavigableNodeType getManagedNodeType() {
        return NavigableNodeType.IG_ROOT;
    }

    public String getRelatedJsp() {
        return NAVIGATION_JSP_FOLDER + JSP_NAME;
    }


    public String getPageDescription() {
        logIGAccess();

        return translate(MSG_PAGE_DESCRIPTION, getPageTitle());
    }

    /**
     * Logs the access to an IG in the current session for the current user
     * <p>
     * https://webgate.ec.europa.eu/CITnet/jira/browse/DIGITCIRCABC-1873
     */
    private void logIGAccess() {

        NodeRef igNodeRef = getCurrentNode().getNodeRef();

        Long igId = (Long) getNodeService().getProperty(igNodeRef,
                ContentModel.PROP_NODE_DBID);

        HttpSession session = ((HttpServletRequest)
                FacesContext.getCurrentInstance().getExternalContext().
                        getRequest()).getSession();

        String sessionId = session.getId();

        // Check if this session was already saved to avoid logging duplicate
        // information
        if (sessionIdsCache.contains(String.valueOf(igId) + sessionId)) {
            return;
        }

        LogRecord logRecord = new LogRecord();

        logRecord.setIgID(igId);
        logRecord.setIgName((String) getNodeService().getProperty(igNodeRef,
                ContentModel.PROP_NAME));
        String username = AuthenticationUtil.getFullyAuthenticatedUser();
        logRecord.setUser(username);
        Path path = getNodeService().getPath(igNodeRef);
        String displayPath = PathUtils.getCircabcPath(path, true);
        displayPath = displayPath.endsWith("contains") ?
                displayPath.substring(0, displayPath.length() -
                        "contains".length()) : displayPath;
        logRecord.setPath(displayPath);
        logRecord.setActivity(IG_ACCESS);
        logRecord.setService(IG);
        logRecord.addInfo(IG_ACCESS_DESCRIPTION);
        logRecord.setOK(true);
        logRecord.setDate(new Date());

        logService.log(logRecord);

        sessionIdsCache.put(String.valueOf(igId) + sessionId, username);
    }

    public String getPageTitle() {
        return ESAPI.encoder().encodeForHTML((String) getCurrentInterstGroup().getProperties()
                .get(NavigableNode.BEST_TITLE_RESOLVER_NAME));
    }

    public String getPageIcon() {
        return IMAGES_ICONS + getCurrentNode().getProperties().get(ICON) + GIF;
    }

    public String getPageIconAltText() {
        return getCurrentNode().getName();
    }

    public void init(final Map<String, String> parameters) {

    }


    public boolean isJoinAllowed() {
        final User currentUser = getNavigator().getCurrentUser();
        final NavigableNode currentIGRoot = getNavigator().getCurrentIGRoot();
        if (currentUser != null && currentUser.getUserName() != null && currentIGRoot != null) {
            Boolean canJoin = PermissionUtils
                    .isUserCanJoinNode(currentUser.getUserName(), currentIGRoot.getNodeRef(),
                            getNodeService(), getPermissionService());
            final Boolean canApply = (Boolean) getNodeService()
                    .getProperty(currentIGRoot.getNodeRef(), CircabcModel.PROP_CAN_REGISTERED_APPLY);
            if (canApply != null) {
                canJoin = canJoin && canApply;
            }
            return canJoin;
        } else {
            return false;
        }

    }

    /**
     * Function to get the elements for the "What's New" panel in IG home
     *
     * @param searchNodeRef The nodeRef to use for the base path search
     * @return List<Node> The node list of the What's New element
     */
    public List<WhatsNewResult> getWhatsNewNodes() {
        long startTime = 0;
        if (logger.isDebugEnabled()) {
            startTime = System.currentTimeMillis();
        }

        final FacesContext context = FacesContext.getCurrentInstance();
        final NodeService nodeService = getNodeService();
        final CircabcBrowseBean browseBean = getBrowseBean();
        final DictionaryService dictionaryService = getDictionaryService();
        final PermissionService permissionService = getPermissionService();

        final String query = buildWhatNewQuery(getCurrentNode().getNodeRef(), getDateInterval());

        if (logger.isDebugEnabled()) {
            logger.debug("Search for Lucene : " + query);
        }

        final List<String> errorMessages = new ArrayList<>();

        // create a list of items from the results

        final RetryingTransactionHelper txnHelper = Repository.getRetryingTransactionHelper(context);
        final RetryingTransactionCallback<List<WhatsNewResult>> callback = new RetryingTransactionCallback<List<WhatsNewResult>>() {
            public List<WhatsNewResult> execute() throws Throwable {
                // perform the search against the repo

                List<WhatsNewResult> resultNodes = null;

                ResultSet results = null;
                try {
                    results = getWhatsNewNodes(getInternalSearchService(), query);

                    if (logger.isDebugEnabled()) {
                        logger.debug(SEARCH_RESULTS_RETURNED + results.length());
                    }

                    if (results.length() != 0) {
                        NodeRef nodeRef;
                        FileInfo fileInfo;
                        QName type;
                        TypeDefinition typeDef;
                        NavigableNode node;
                        WhatsNewResult whatsNewResult;
                        final int maxEntries = getMaxEntries();
                        resultNodes = new ArrayList<>(
                                getMaxEntries() > results.length() ? results.length() : getMaxEntries());

                        for (final ResultSetRow row : results) {
                            try {
                                if (resultNodes.size() < maxEntries) {
                                    nodeRef = row.getNodeRef();
                                    final boolean exist = nodeService.exists(nodeRef);
                                    if (exist) {
                                        final boolean hasReadPermission = AccessStatus.ALLOWED
                                                .equals(permissionService.hasPermission(nodeRef, PermissionService.READ));
                                        if (hasReadPermission) {
                                            fileInfo = getFileFolderService().getFileInfo(nodeRef);

                                            if (nodeService.hasAspect(nodeRef, RenditionModel.ASPECT_HIDDEN_RENDITION)) {
                                                continue;
                                            }
                                            whatsNewResult = whatsNewNodesCache.get(nodeRef);
                                            if (whatsNewResult != null) {
                                                resultNodes.add(whatsNewResult);
                                            } else {
                                                type = nodeService.getType(nodeRef);

                                                // Trick to optimise the
                                                // efficiency of the code
                                                if (validTypeMap.containsKey(type)) {
                                                    typeDef = validTypeMap.get(type);
                                                } else {
                                                    // make sure the type is
                                                    // defined in the data
                                                    // dictionary
                                                    typeDef = dictionaryService.getType(type);
                                                    validTypeMap.put(type, typeDef);
                                                }

                                                if (typeDef != null) {
                                                    node = null;

                                                    if (ContentModel.TYPE_CONTENT.equals(type)) {
                                                        node = ResolverHelper
                                                                .createContentRepresentation(fileInfo, nodeService, browseBean);
                                                    } else if (ContentModel.TYPE_FOLDER.equals(type)) {
                                                        node = ResolverHelper
                                                                .createFolderRepresentation(fileInfo, nodeService, browseBean);
                                                    } else if (ApplicationModel.TYPE_FOLDERLINK.equals(type)) {
                                                        node = ResolverHelper
                                                                .createFolderLinkRepresentation(fileInfo, nodeService, browseBean);
                                                    } else if (ApplicationModel.TYPE_FILELINK.equals(type)) {
                                                        node = ResolverHelper
                                                                .createFileLinkRepresentation(fileInfo, nodeService, browseBean);
                                                    } else if (ForumModel.TYPE_POST.equals(type)) {
                                                        node = ResolverHelper
                                                                .createPostRepresentation(fileInfo, nodeService, browseBean);
                                                    } else if (dictionaryService
                                                            .isSubClass(type, ContentModel.TYPE_CONTENT)) {
                                                        if (logger.isWarnEnabled()) {
                                                            logger.debug(
                                                                    CREATE_NODE_CONTENT_REPRESENTATION_NOT_OPTIMIZED_FOR_TYPE + type);
                                                        }
                                                        node = ResolverHelper
                                                                .createContentRepresentation(fileInfo, nodeService, browseBean);
                                                    } else if (
                                                            dictionaryService.isSubClass(type, ContentModel.TYPE_FOLDER) == true
                                                                    && dictionaryService
                                                                    .isSubClass(type, ContentModel.TYPE_SYSTEM_FOLDER) == false) {
                                                        if (logger.isWarnEnabled()) {
                                                            logger.debug(
                                                                    CREATE_FOLDER_REPRESENTATION_NOT_OPTIMIZED_FOR_TYPE + type);
                                                        }
                                                        node = ResolverHelper
                                                                .createFolderRepresentation(fileInfo, nodeService, browseBean);
                                                    } else if (dictionaryService
                                                            .isSubClass(type, ApplicationModel.TYPE_FOLDERLINK)) {
                                                        if (logger.isWarnEnabled()) {
                                                            logger.debug(
                                                                    CREATE_FOLDER_LINK_REPRESENTATION_NOT_OPTIMIZED_FOR_TYPE + type);
                                                        }
                                                        node = ResolverHelper
                                                                .createFolderLinkRepresentation(fileInfo, nodeService, browseBean);
                                                    } else if (dictionaryService
                                                            .isSubClass(type, ApplicationModel.TYPE_FILELINK)) {
                                                        if (logger.isWarnEnabled()) {
                                                            logger.debug(
                                                                    CREATE_FILE_LINK_REPRESENTATION_NOT_OPTIMIZED_FOR_TYPE + type);
                                                        }
                                                        node = ResolverHelper
                                                                .createFileLinkRepresentation(fileInfo, nodeService, browseBean);
                                                    }

                                                    if (node != null) {
                                                        whatsNewResult = new WhatsNewResult(node);
                                                        resultNodes.add(whatsNewResult);
                                                        whatsNewNodesCache.put(nodeRef, whatsNewResult);
                                                    }
                                                } else {
                                                    if (logger.isWarnEnabled()) {
                                                        logger
                                                                .warn(FOUND_INVALID_OBJECT_IN_DATABASE_ID + nodeRef + TYPE + type);
                                                    }
                                                }

                                            }
                                        } else {
                                            if (logger.isDebugEnabled()) {
                                                logger.debug("No read Permission on node:" + nodeRef
                                                        + ". This means that the current user has no acces to this node.");
                                            }
                                        }
                                    } else {
                                        if (logger.isErrorEnabled()) {
                                            logger.debug("Lucene indexes corrupted. Node:" + nodeRef
                                                    + " don't exist but is referenced in the indexes!");
                                        }
                                    }

                                } else {
                                    if (logger.isDebugEnabled()) {
                                        logger.debug(
                                                "Skip node: Max entries to display in the what's new page is reached.");
                                    }
                                }
                            } catch (final InvalidNodeRefException refErr) {
                                errorMessages.add(translate(Repository.ERROR_NODEREF, refErr.getNodeRef()));
                            } catch (final Throwable err) {
                                if (logger.isInfoEnabled()) {
                                    logger.info(SEARCH_FAILED_FOR + query);
                                }

                                errorMessages.add(translate(Repository.ERROR_SEARCH, err.getMessage()));
                            }
                        }
                    } else {
                        if (logger.isInfoEnabled()) {
                            logger.info("Nothing new to display in the what's new page of this IG.");
                        }
                        resultNodes = Collections.emptyList();
                    }

                } finally {
                    if (results != null) {
                        results.close();
                    }
                }
                return resultNodes;
            }

        };
        final List<WhatsNewResult> resultNodes = txnHelper.doInTransaction(callback, true);

        if (logger.isDebugEnabled()) {
            long endTime = System.currentTimeMillis();
            logger.debug(TIME_TO_QUERY_AND_BUILD_MAP_NODES + (endTime - startTime) + MS);
        }

        if (logger.isErrorEnabled()) {
            for (final String error : errorMessages) {
                logger.error(error);
            }
        }

        return resultNodes;
    }


    /**
     * Function to get the elements for the "Visible document" panel in IG home
     *
     * @return List<Node> The node list of the that current user can access
     */
    public List<NavigableNode> getVisibleDocuments() {
        long startTime = 0;
        if (logger.isDebugEnabled()) {
            startTime = System.currentTimeMillis();
        }

        final FacesContext context = FacesContext.getCurrentInstance();
        final NodeService nodeService = getNodeService();
        final CircabcBrowseBean browseBean = getBrowseBean();
        final DictionaryService dictionaryService = getDictionaryService();
        final PermissionService permissionService = getPermissionService();

        final String query = buildvisibleDocumentQuery();

        if (logger.isDebugEnabled()) {
            logger.debug("Search for Lucene : " + query);
        }

        final List<String> errorMessages = new ArrayList<>();

        // create a list of items from the results

        final RetryingTransactionHelper txnHelper = Repository.getRetryingTransactionHelper(context);
        final RetryingTransactionCallback<List<NavigableNode>> callback = new RetryingTransactionCallback<List<NavigableNode>>() {
            public List<NavigableNode> execute() throws Throwable {
                // perform the search against the repo

                List<NavigableNode> resultNodes = null;

                ResultSet results = null;
                try {
                    results = getWhatsNewNodes(getSearchService(), query);

                    if (logger.isDebugEnabled()) {
                        logger.debug(SEARCH_RESULTS_RETURNED + results.length());
                    }

                    if (results.length() != 0) {
                        NodeRef nodeRef;
                        FileInfo fileInfo;
                        QName type;
                        TypeDefinition typeDef;
                        NavigableNode node;
                        final int maxEntries = getMaxEntries();
                        resultNodes = new ArrayList<>(
                                getMaxEntries() > results.length() ? results.length() : getMaxEntries());

                        for (final ResultSetRow row : results) {
                            try {
                                if (resultNodes.size() < maxEntries) {
                                    nodeRef = row.getNodeRef();
                                    final boolean exist = nodeService.exists(nodeRef);
                                    if (exist) {
                                        final boolean hasReadPermission = AccessStatus.ALLOWED
                                                .equals(permissionService.hasPermission(nodeRef, PermissionService.READ));
                                        if (hasReadPermission) {
                                            fileInfo = getFileFolderService().getFileInfo(nodeRef);

                                            {
                                                type = nodeService.getType(nodeRef);

                                                if (validTypeMap.containsKey(type)) {
                                                    typeDef = validTypeMap.get(type);
                                                } else {
                                                    typeDef = dictionaryService.getType(type);
                                                    validTypeMap.put(type, typeDef);
                                                }

                                                if (typeDef != null) {
                                                    node = null;

                                                    if (ContentModel.TYPE_CONTENT.equals(type)) {
                                                        node = ResolverHelper
                                                                .createContentRepresentation(fileInfo, nodeService, browseBean);
                                                    } else if (ContentModel.TYPE_FOLDER.equals(type)) {
                                                        node = ResolverHelper
                                                                .createFolderRepresentation(fileInfo, nodeService, browseBean);
                                                    } else if (ApplicationModel.TYPE_FOLDERLINK.equals(type)) {
                                                        node = ResolverHelper
                                                                .createFolderLinkRepresentation(fileInfo, nodeService, browseBean);
                                                    } else if (ApplicationModel.TYPE_FILELINK.equals(type)) {
                                                        node = ResolverHelper
                                                                .createFileLinkRepresentation(fileInfo, nodeService, browseBean);
                                                    } else if (ForumModel.TYPE_POST.equals(type)) {
                                                        node = ResolverHelper
                                                                .createPostRepresentation(fileInfo, nodeService, browseBean);
                                                    } else if (dictionaryService
                                                            .isSubClass(type, ContentModel.TYPE_CONTENT)) {
                                                        if (logger.isWarnEnabled()) {
                                                            logger.debug(
                                                                    CREATE_NODE_CONTENT_REPRESENTATION_NOT_OPTIMIZED_FOR_TYPE + type);
                                                        }
                                                        node = ResolverHelper
                                                                .createContentRepresentation(fileInfo, nodeService, browseBean);
                                                    } else if (
                                                            dictionaryService.isSubClass(type, ContentModel.TYPE_FOLDER) == true
                                                                    && dictionaryService
                                                                    .isSubClass(type, ContentModel.TYPE_SYSTEM_FOLDER) == false) {
                                                        if (logger.isWarnEnabled()) {
                                                            logger.debug(
                                                                    CREATE_FOLDER_REPRESENTATION_NOT_OPTIMIZED_FOR_TYPE + type);
                                                        }
                                                        node = ResolverHelper
                                                                .createFolderRepresentation(fileInfo, nodeService, browseBean);
                                                    } else if (dictionaryService
                                                            .isSubClass(type, ApplicationModel.TYPE_FOLDERLINK)) {
                                                        if (logger.isWarnEnabled()) {
                                                            logger.debug(
                                                                    CREATE_FOLDER_LINK_REPRESENTATION_NOT_OPTIMIZED_FOR_TYPE + type);
                                                        }
                                                        node = ResolverHelper
                                                                .createFolderLinkRepresentation(fileInfo, nodeService, browseBean);
                                                    } else if (dictionaryService
                                                            .isSubClass(type, ApplicationModel.TYPE_FILELINK)) {
                                                        if (logger.isWarnEnabled()) {
                                                            logger.debug(
                                                                    CREATE_FILE_LINK_REPRESENTATION_NOT_OPTIMIZED_FOR_TYPE + type);
                                                        }
                                                        node = ResolverHelper
                                                                .createFileLinkRepresentation(fileInfo, nodeService, browseBean);
                                                    }

                                                    if (node != null) {
                                                        resultNodes.add(node);
                                                    }
                                                } else {
                                                    if (logger.isWarnEnabled()) {
                                                        logger
                                                                .warn(FOUND_INVALID_OBJECT_IN_DATABASE_ID + nodeRef + TYPE + type);
                                                    }
                                                }

                                            }
                                        } else {
                                            if (logger.isDebugEnabled()) {
                                                logger.debug("No read Permission on node:" + nodeRef
                                                        + ". This means that the current user has no acces to this node.");
                                            }
                                        }
                                    } else {
                                        if (logger.isErrorEnabled()) {
                                            logger.debug("Lucene indexes corrupted. Node:" + nodeRef
                                                    + " don't exist but is referenced in the indexes!");
                                        }
                                    }

                                } else {
                                    if (logger.isDebugEnabled()) {
                                        logger.debug(
                                                "Skip node: Max entries to display in the what's new page is reached.");
                                    }
                                }
                            } catch (final InvalidNodeRefException refErr) {
                                errorMessages.add(translate(Repository.ERROR_NODEREF, refErr.getNodeRef()));
                            } catch (final Throwable err) {
                                if (logger.isInfoEnabled()) {
                                    logger.info(SEARCH_FAILED_FOR + query);
                                }

                                errorMessages.add(translate(Repository.ERROR_SEARCH, err.getMessage()));
                            }
                        }
                    } else {
                        if (logger.isWarnEnabled()) {
                            logger.warn("Nothing new to display in the what's new page of this IG.");
                        }
                        resultNodes = Collections.emptyList();
                    }

                } finally {
                    if (results != null) {
                        results.close();
                    }
                }
                return resultNodes;
            }

        };
        final List<NavigableNode> resultNodes = txnHelper.doInTransaction(callback, true);

        if (logger.isDebugEnabled()) {
            long endTime = System.currentTimeMillis();
            logger.debug(TIME_TO_QUERY_AND_BUILD_MAP_NODES + (endTime - startTime) + MS);
        }

        if (logger.isErrorEnabled()) {
            for (final String error : errorMessages) {
                logger.error(error);
            }
        }

        return resultNodes;
    }

    private String buildvisibleDocumentQuery() {
        String query =
                "( PATH:\"" + getPathFromSpaceRef(getNavigator().getCurrentIGRoot().getNodeRef(), true)
                        + "\"" + " )";
        query = query + "AND (( TYPE:\"" + ForumModel.TYPE_POST + "\" OR TYPE:\"" +
                ForumModel.TYPE_FORUMS + "\") OR (TYPE:\"" +
                ContentModel.TYPE_CONTENT + "\" OR TYPE:\"" +
                ContentModel.TYPE_FOLDER + "\")) )";
        return query;
    }

    /**
     * Helper that build the query to perform the what's ne search
     */
    protected String buildWhatNewQuery(final NodeRef searchNodeRef, final int interval) {
        final StringBuilder query = new StringBuilder();

        //	set the creation and modification date
        final GregorianCalendar calendarNow = new GregorianCalendar();
        calendarNow.set(Calendar.HOUR, 0);
        calendarNow.set(Calendar.MINUTE, 0);
        calendarNow.set(Calendar.SECOND, 1);

        // From: subtract the interval
        calendarNow.add(GregorianCalendar.DATE, -interval);

        final String strDateFrom = escape(
                CachingDateFormat.getDateFormat().format(calendarNow.getTime()));

        // To: add the interval plus one day to be compatible with Lucene & SOLR
        calendarNow.add(Calendar.DATE, interval);
        calendarNow.add(Calendar.DAY_OF_MONTH, 1);

        final String strDateTo = escape(
                CachingDateFormat.getDateFormat().format(calendarNow.getTime()));

        query
                .append(OPEN_QUERY)

                .append(PATH)
                .append(ESCAPE_QUOTES)
                .append(getPathFromSpaceRef(searchNodeRef, true))
                .append(ESCAPE_QUOTES)
                .append(CLOSE_QUERY)

                .append(AND)
                .append(OPEN_QUERY)
                .append(START_PROP_SEARCH)
                .append(PROP_MODIFIED_ESCAPED)
                .append(OPEN_BRACKETS)
                .append(strDateFrom)
                .append(TO)
                .append(strDateTo)
                .append(CLOSE_BRACKETS)
                .append(" OR ")
                .append(START_PROP_SEARCH)
                .append(PROP_CREATED_ESCAPED)
                .append(OPEN_BRACKETS)
                .append(strDateFrom)
                .append(TO)
                .append(strDateTo)
                .append(CLOSE_BRACKETS)
                .append(CLOSE_QUERY)

                .append(AND)
                .append(OPEN_QUERY)
                .append(OPEN_QUERY)
                .append("TYPE:")
                .append(TYPE_FILE_ESCAPED)
                .append(" OR ")
                .append("TYPE:")
                .append(TYPE_FOLDER_ESCAPED)

                .append(CLOSE_QUERY)
                .append(CLOSE_QUERY)
        ;
        return query.toString();
    }

    /**
     * Return the string escaped
     */
    protected String escape(final String str) {
        return str.replaceAll(ESCAPE1, ESCAPE2).replaceAll(ESCAPE3, ESCAPE4);
    }

    /**
     * Search for the what's new thinks - with or without security evaluation (use the
     * internalSearchService)
     *
     * @param searchService the proxied or not search Service (internal recommended)
     * @return the list of node
     */
    protected ResultSet getWhatsNewNodes(final SearchService searchService, final String query) {
        final SearchParameters sp = new SearchParameters();
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery(query);
        sp.addStore(Repository.getStoreRef());
        sp.addSort(MODIFIED, false);

        // limit the number of result to 5
        // sp.setLimitBy(LimitBy.FINAL_SIZE); s
        // p.setLimit(50);

        return searchService.query(sp);
    }

    protected final SearchService getInternalSearchService() {
        if (this.internalSearchService == null) {
            this.internalSearchService = Services
                    .getCircabcServiceRegistry(FacesContext.getCurrentInstance())
                    .getNonSecuredSearchService();
        }
        return this.internalSearchService;
    }

    /**
     * @param searchService the service used to find nodes
     */
    public final void setInternalSearchService(final SearchService internalSearchService) {
        this.internalSearchService = internalSearchService;
    }

    public void setWhatsNewNodesCache(final SimpleCache<NodeRef, WhatsNewResult> whatsNewNodesCache) {
        this.whatsNewNodesCache = whatsNewNodesCache;
    }

    public int getDateInterval() {
        return dateInterval;
    }

    public void setDateInterval(final int dateInterval) {
        this.dateInterval = dateInterval;
    }

    public int getMaxEntries() {
        return maxEntries;
    }

    public void setMaxEntries(final int maxEntries) {
        this.maxEntries = maxEntries;
    }

    /**
     * Sets the value of the logService
     *
     * @param logService the logService to set.
     */
    public void setLogService(LogService logService) {
        this.logService = logService;
    }
}
