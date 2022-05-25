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
package eu.cec.digit.circabc.repo.external.repositories;

import eu.cec.digit.circabc.action.evaluator.ExternalRepositoryActionEvaluator;
import eu.cec.digit.circabc.action.evaluator.ManageExternalRepositoriesActionEvaluator;
import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.repo.hrs.ws.*;
import eu.cec.digit.circabc.service.external.repositories.ExternalRepositoriesManagementService;
import eu.cec.digit.circabc.service.external.repositories.PublishResponse;
import eu.cec.digit.circabc.web.WebClientHelper;
import io.swagger.api.AresBridgeApiImpl;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Implementation of the service that manages all the concerns about Hermes document publishing.
 *
 * @author schwerr
 */
public class ExternalRepositoriesManagementServiceImpl
        implements ExternalRepositoriesManagementService {

    private static final Log logger =
            LogFactory.getLog(ExternalRepositoriesManagementServiceImpl.class);

    private NodeService nodeService = null;

    private TransactionService transactionService = null;

    private ContentService contentService = null;

    // HRS
    private String applicationId = "CIRCAmN67t";
    private UserNameResolver userNameResolver = null;
    private ProxyTicketResolver proxyTicketResolver = null;
    private String uploadUrl = "http://myserver:1234/hrs-dts/DataTransferService";
    private String endpointBaseAddress = "http://dighbust.cc.cec.eu.int:11031/hermes/Proxy";
    private String aresLinkPrefix =
            "http://www.development.cc.cec/Ares_pg/documentDirectAccess.do?documentId=";

    private EntityWebService entityWebService = null;
    private DocumentWebService documentWebService = null;
    private WorkflowWebService workflowWebService = null;
    private UserWebService userWebService = null;

    private long minutesToCheck = 60 * 12;

    // Is the system operational? By default, not
    private boolean operational = false;

    private SimpleCache<String, Object> hrsInternalEntitiesCache = null;

    /**
     * Initializes the web services and endpoints.
     */
    public void init() {

        // Init evaluators
        ExternalRepositoryActionEvaluator.setExternalRepositoriesManagementService(this);
        ManageExternalRepositoriesActionEvaluator.setExternalRepositoriesManagementService(this);

        // Init web services
        entityWebService = new EntityWebServiceLocator();
        documentWebService = new DocumentWebServiceLocator();
        workflowWebService = new WorkflowWebServiceLocator();
        userWebService = new UserWebServiceLocator();

        ((EntityWebServiceLocator) entityWebService)
                .setEntityServiceEndpointAddress(endpointBaseAddress + "/1.16/EntityWebServicePS");
        ((DocumentWebServiceLocator) documentWebService)
                .setDocumentServiceEndpointAddress(endpointBaseAddress + "/1.16/DocumentWebServicePS");
        ((WorkflowWebServiceLocator) workflowWebService)
                .setWorkflowServiceEndpointAddress(endpointBaseAddress + "/1.16/WorkflowWebServicePS");
        ((UserWebServiceLocator) userWebService)
                .setUserServiceEndpointAddress(endpointBaseAddress + "/1.16/UserWebServicePS");
    }

    /**
     * @see eu.cec.digit.circabc.service.external.repositories.ExternalRepositoriesManagementService#isOperational()
     */
    @Override
    public boolean isOperational() {
        return operational;
    }

    /**
     * Sets the value of the operational
     *
     * @param operational the operational to set.
     */
    public void setOperational(boolean operational) {
        this.operational = operational;
    }

    /**
     * @see eu.cec.digit.circabc.service.external.repositories.ExternalRepositoriesManagementService#getAvailableRepositories()
     */
    @Override
    public Collection<RepositoryConfiguration> getAvailableRepositories() {

        List<RepositoryConfiguration> availableRepositories = new ArrayList<>();

        RepositoryConfiguration data = new RepositoryConfiguration();
        data.setName(ExternalRepositoriesManagementService.EXTERNAL_REPOSITORY_NAME);
        availableRepositories.add(data);

        return availableRepositories;
    }

    /**
     * @see eu.cec.digit.circabc.service.external.repositories.ExternalRepositoriesManagementService#addRepository(java.lang.String,
     * eu.cec.digit.circabc.repo.external.repositories.RepositoryConfiguration)
     */
    @Override
    public void addRepository(
            final String parentNodeId, final RepositoryConfiguration configuration) {

        final RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();

        final RetryingTransactionCallback<Object> callback =
                new RetryingTransactionCallback<Object>() {

                    public Object execute() throws Throwable {

                        NodeRef parentNodeRef = createOrGetRepositoryConfigurationFolder(parentNodeId, true);

                        Map<QName, Serializable> properties = new HashMap<>();

                        properties.put(ContentModel.PROP_NAME, configuration.getName());

                        nodeService
                                .createNode(
                                        parentNodeRef,
                                        CircabcModel.ASSOC_CONTAINSCON_FIGURATIONS,
                                        QName.createQName(
                                                ContentModel.PROP_NAME.getNamespaceURI(),
                                                QName.createValidLocalName(configuration.getName())),
                                        CircabcModel.TYPE_EXTERNAL_REPOSITORY_CONFIGURATION,
                                        properties)
                                .getChildRef();
                        return null;
                    }
                };

        AuthenticationUtil.runAs(
                new AuthenticationUtil.RunAsWork<Object>() {

                    public Object doWork() {
                        return txnHelper.doInTransaction(callback, false, true);
                    }
                },
                AuthenticationUtil.getSystemUserName());
    }

    /**
     * Gets or creates the parent folder to store the configurations of the external repositories in
     * the current IG
     *
     * @param parentNodeId the node id of the current IG
     * @param create       should the parent folder be created?
     */
    public NodeRef createOrGetRepositoryConfigurationFolder(String parentNodeId, boolean create) {

        NodeRef parentNodeRef = new NodeRef(parentNodeId);

        String localName = "ExternalRepositoryConfigurations";

        NodeRef externalRepositoryNodeRef =
                nodeService.getChildByName(parentNodeRef, ContentModel.ASSOC_CONTAINS, localName);

        if (externalRepositoryNodeRef != null) {
            return externalRepositoryNodeRef;
        }

        if (!create) {
            return null;
        }

        Map<QName, Serializable> properties = new HashMap<>();

        properties.put(ContentModel.PROP_NAME, localName);

        externalRepositoryNodeRef =
                nodeService
                        .createNode(
                                parentNodeRef,
                                ContentModel.ASSOC_CONTAINS,
                                QName.createQName(
                                        ContentModel.PROP_NAME.getNamespaceURI(),
                                        QName.createValidLocalName(localName)),
                                CircabcModel.TYPE_EXTERNAL_REPOSITORY_CONFIGURATION_FOLDER,
                                properties)
                        .getChildRef();

        return externalRepositoryNodeRef;
    }

    /**
     * @see eu.cec.digit.circabc.service.external.repositories.ExternalRepositoriesManagementService#removeRepository(java.lang.String,
     * java.lang.String)
     */
    @Override
    public void removeRepository(String parentNodeId, String repositoryName) {

        final NodeRef parentNodeRef = createOrGetRepositoryConfigurationFolder(parentNodeId, false);

        if (parentNodeRef == null) {
            return;
        }

        final NodeRef configuredRepositoryNodeRef =
                nodeService.getChildByName(
                        parentNodeRef, CircabcModel.ASSOC_CONTAINSCON_FIGURATIONS, repositoryName);

        if (configuredRepositoryNodeRef == null) {
            return;
        }

        final RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();

        final RetryingTransactionCallback<Object> callback =
                new RetryingTransactionCallback<Object>() {

                    public Object execute() throws Throwable {

                        nodeService.removeChild(parentNodeRef, configuredRepositoryNodeRef);

                        return null;
                    }
                };

        AuthenticationUtil.runAs(
                new AuthenticationUtil.RunAsWork<Object>() {

                    public Object doWork() {
                        return txnHelper.doInTransaction(callback, false, true);
                    }
                },
                AuthenticationUtil.getSystemUserName());
    }

    /**
     * @see eu.cec.digit.circabc.service.external.repositories.ExternalRepositoriesManagementService#getConfiguredRepositories(java.lang.String)
     */
    @Override
    public Collection<RepositoryConfiguration> getConfiguredRepositories(String parentNodeId) {

        NodeRef parentNodeRef = createOrGetRepositoryConfigurationFolder(parentNodeId, false);

        Collection<RepositoryConfiguration> repositories = new ArrayList<>();

        if (parentNodeRef == null) {
            return repositories;
        }

        List<ChildAssociationRef> children = nodeService.getChildAssocs(parentNodeRef);

        for (ChildAssociationRef child : children) {

            RepositoryConfiguration configuration = new RepositoryConfiguration();

            NodeRef childNodeRef = child.getChildRef();

            String name = (String) nodeService.getProperty(childNodeRef, ContentModel.PROP_NAME);
            Date registrationDate =
                    (Date) nodeService.getProperty(childNodeRef, ContentModel.PROP_CREATED);

            configuration.setName(name);
            configuration.setRegistrationDate(registrationDate);

            repositories.add(configuration);
        }

        return repositories;
    }

    /**
     * Tries to get the data for the given document from Hermes
     *
     * @param data
     */
    //	private boolean getDataFromExternalRepository(HashMap<String, String> data) {
    //
    //		if (!mustCheck(data.get(PROPERTY_LAST_CHECKED))) {
    //			return false;
    //		}
    //
    //		// At this point document data can be retrieved again from the
    //		// repository
    //		try {
    //			DocumentService_PortType documentService =
    //									documentWebService.getDocumentService();
    //
    //			eu.cec.digit.circabc.repo.hrs.ws.Document document =
    //								documentService.getDocument(createHeader(
    //									userNameResolver.getUserName()),
    //										data.get(PROPERTY_DOCUMENT_ID));
    //
    //			data.put(PROPERTY_TITLE, document.getTitle());
    //			data.put(PROPERTY_COMMENTS, document.getComments());
    //			data.put(PROPERTY_MAIL_TYPE, document.getMailType().toString());
    //
    //			SimpleDateFormat simpleDateFormat =	new SimpleDateFormat(EXTENDED_DATE_FORMAT);
    //			data.put(PROPERTY_LAST_CHECKED, simpleDateFormat.format(new Date()));
    //
    //			data.put(PROPERTY_DATA_STATUS, STATUS_OK);
    //
    //			return true;
    //		}
    //		catch (Exception e) {
    //			logger.warn("Unable to retrieve document data from Hermes. " +
    //					"Showing old data. Data could have changed from the " +
    //					"last view.", e);
    //			data.put(PROPERTY_DATA_STATUS, STATUS_UNABLE);
    //			return false;
    //		}
    //	}

    /**
     * Is it necessary to check for updates to the document from Hermes?
     *
     * @param data
     * @return
     */
    //	private boolean mustCheck(String lastCheckedString) {
    //
    //		SimpleDateFormat simpleDateFormat =	new SimpleDateFormat(EXTENDED_DATE_FORMAT);
    //
    //		Date currentDate = new Date();
    //
    //		if (lastCheckedString != null) {
    //
    //			Date lastChecked = null;
    //
    //			try {
    //				lastChecked = simpleDateFormat.parse(lastCheckedString);
    //			}
    //			catch (ParseException e) {
    //				logger.error("Error parsing the last check date.", e);
    //				return false;
    //			}
    //
    //			// 12 hs
    //			if (currentDate.getTime() - lastChecked.getTime() <
    //								1000 * 60 * minutesToCheck) {
    //				return false;
    //			}
    //		}
    //
    //		return true;
    //	}

    /**
     * @see eu.cec.digit.circabc.service.external.repositories.ExternalRepositoriesManagementService#getRepositoryDataForDocument(java.lang.String)
     */
    @SuppressWarnings("all")
    @Override
    public Map<String, Map<String, String>> getRepositoryDataForDocument(String documentId) {

        final NodeRef nodeRef = new NodeRef(documentId);

        if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_EXTERNALLY_PUBLISHED)) {

            final HashMap<String, HashMap<String, String>> repositoriesInfo =
                    (HashMap<String, HashMap<String, String>>)
                            nodeService.getProperty(nodeRef, CircabcModel.PROP_REPOSITORIES_INFO);

            HashMap<String, String> data = repositoriesInfo.get(AresBridgeApiImpl.ARES_BRIDGE);

            if (data == null) {
                data = repositoriesInfo.get(EXTERNAL_REPOSITORY_NAME);
                if (data == null) {
                    return new HashMap<String, Map<String, String>>();
                }
            }

            //			// Check if it's a version in which case, the properties cannot
            //			// change
            //			StoreRef storeRef = nodeRef.getStoreRef();
            //
            //			if (!VERSION_STORE.equals(storeRef.getProtocol())) {
            //
            //				// First try to get the document data from Hermes and
            //				// update Alfreso entries, and if it does not respond,
            //				// get it from Alfresco
            //				boolean checked = getDataFromExternalRepository(data);
            //
            //				// Update properties
            //				final RetryingTransactionHelper txnHelper = transactionService
            //						.getRetryingTransactionHelper();
            //
            //				final RetryingTransactionCallback<Object> callback =
            //									new RetryingTransactionCallback<Object>() {
            //
            //					public Object execute() throws Throwable {
            //
            //						nodeService.setProperty(nodeRef, CircabcModel.
            //									PROP_REPOSITORIES_INFO, repositoriesInfo);
            //
            //						return null;
            //					}
            //				};
            //
            //				if (checked) {
            //
            //					AuthenticationUtil.runAs(
            //								new AuthenticationUtil.RunAsWork<Object>() {
            //
            //						public Object doWork() {
            //							return txnHelper.doInTransaction(callback, false,
            //																true);
            //						}
            //					}, AuthenticationUtil.getSystemUserName());
            //				}
            //			}

            // After updating properties, return the data to display
            // Translate properties according to active language
            HashMap<String, String> displayData = new HashMap<String, String>();
            for (Map.Entry<String, String> entry : data.entrySet()) {
                displayData.put(translate(entry.getKey()), entry.getValue());
            }

            HashMap<String, Map<String, String>> displayRepositoriesInfo =
                    new HashMap<String, Map<String, String>>();

            displayRepositoriesInfo.put(EXTERNAL_REPOSITORY_NAME, displayData);

            return (Map) displayRepositoriesInfo;
        }

        return new HashMap<String, Map<String, String>>();
    }

    /**
     * @see eu.cec.digit.circabc.service.external.repositories.ExternalRepositoriesManagementService#publishDocument(java.lang.String,
     * java.lang.String, eu.cec.digit.circabc.repo.external.repositories.RegistrationRequest)
     */
    @SuppressWarnings("unchecked")
    @Override
    public PublishResponse publishDocument(
            final String repositoryName,
            final String nodeId,
            final RegistrationRequest registrationRequest) {

        final NodeRef nodeRef = new NodeRef(nodeId);

        registrationRequest.setRegistrationUserName(userNameResolver.getUserName());

        final RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();

        final RetryingTransactionCallback<PublishResponse> callback =
                new RetryingTransactionCallback<PublishResponse>() {

                    public PublishResponse execute() throws Throwable {

                        PublishResponse publishResponse = new PublishResponse();

                        // Publish
                        RegistrationSummary registrationSummary = publishToHermes(nodeRef, registrationRequest);
                        // Error
                        if (registrationSummary.getRegistrationNumber() == null) {
                            publishResponse.setSuccess(false);
                            publishResponse.setMessage(registrationSummary.getDocumentId());
                            return publishResponse;
                        }

                        // Add workflow for recipients
                        AssignmentTaskRequest request = new AssignmentTaskRequest();
                        request.setAssignmentUserName(registrationRequest.getRegistrationUserName());
                        request.setDocumentId(registrationSummary.getDocumentId());
                        request.setAssigneeIds(
                                Collections.singletonList(
                                        getInternalEntityId(registrationRequest.getRegistrationUserName())));

                        AssignmentWorkflow workflow = addAssignmentTask(request);

                        String workflowId = null;

                        // TODO Retry workflow?
                        if (workflow.getWorkflowId() != 0) {
                            workflowId = String.valueOf(workflow.getWorkflowId());
                        } else {
                            workflowId = workflow.getDocumentId();
                        }

                        // Add to Alfresco
                        HashMap<String, HashMap<String, String>> repositoriesInfo = null;

                        if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_EXTERNALLY_PUBLISHED)) {

                            repositoriesInfo =
                                    (HashMap<String, HashMap<String, String>>)
                                            nodeService.getProperty(nodeRef, CircabcModel.PROP_REPOSITORIES_INFO);
                        } else {
                            repositoriesInfo = new HashMap<>();

                            Map<QName, Serializable> aspectProperties = new HashMap<>();

                            nodeService.addAspect(
                                    nodeRef, CircabcModel.ASPECT_EXTERNALLY_PUBLISHED, aspectProperties);
                        }

                        // Add the new published info
                        if (wasPublishedTo(repositoryName, nodeId)) {
                            repositoriesInfo.remove(repositoryName);
                        }

                        HashMap<String, String> data = new HashMap<>();

                        data.put(PROPERTY_TITLE, registrationRequest.getSubject());
                        data.put(PROPERTY_REGISTRATION_NUMBER, registrationSummary.getRegistrationNumber());
                        data.put(PROPERTY_SAVE_NUMBER, registrationSummary.getSaveNumber());
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT);
                        data.put(
                                PROPERTY_REGISTRATION_DATE,
                                simpleDateFormat.format(registrationSummary.getRegistrationDate()));
                        data.put(
                                PROPERTY_REGISTRATION_AUTHOR,
                                getInternalEntityName(registrationRequest.getRegistrationUserName()));
                        data.put(PROPERTY_MAIL_TYPE, registrationRequest.getMailType());
                        data.put(PROPERTY_COMMENTS, registrationRequest.getComments());
                        data.put(
                                PROPERTY_DIRECT_ARES_LINK, aresLinkPrefix + registrationSummary.getDocumentId());

                        //				getDataFromExternalRepository(data);

                        repositoriesInfo.put(repositoryName, data);

                        nodeService.setProperty(nodeRef, CircabcModel.PROP_REPOSITORIES_INFO, repositoriesInfo);

                        // In case the workflow failed
                        if (workflow.getWorkflowId() == 0) {
                            publishResponse.setSuccess(false);
                            publishResponse.setMessage(PUBLISH_SUCCESS_WORKFLOW_FAILURE + " " + workflowId);
                            return publishResponse;
                        }

                        publishResponse.setMessage(
                                "Registration - Workflow "
                                        + "succeeded. Document Id: "
                                        + registrationSummary.getDocumentId()
                                        + ", Workflow Id: "
                                        + workflowId
                                        + ", Access Link: "
                                        + aresLinkPrefix
                                        + registrationSummary.getDocumentId());

                        return publishResponse;
                    }
                };

        PublishResponse publishResponse =
                AuthenticationUtil.runAs(
                        new AuthenticationUtil.RunAsWork<PublishResponse>() {

                            public PublishResponse doWork() {
                                return txnHelper.doInTransaction(callback, false, true);
                            }
                        },
                        AuthenticationUtil.getSystemUserName());

        return publishResponse;
    }

    @Override
    public void saveExternalMetadata(
            String repositoryName,
            String nodeId,
            String documentId,
            String saveNumber,
            String registartionNumber,
            String requestType,
            String transactionId) {
        final NodeRef nodeRef = new NodeRef(nodeId);
        // Add to Alfresco
        HashMap<String, HashMap<String, String>> repositoriesInfo = null;

        if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_EXTERNALLY_PUBLISHED)) {

            repositoriesInfo =
                    (HashMap<String, HashMap<String, String>>)
                            nodeService.getProperty(nodeRef, CircabcModel.PROP_REPOSITORIES_INFO);
        } else {
            repositoriesInfo = new HashMap<>();

            Map<QName, Serializable> aspectProperties = new HashMap<>();

            nodeService.addAspect(nodeRef, CircabcModel.ASPECT_EXTERNALLY_PUBLISHED, aspectProperties);
        }

        // Add the new published info
        if (wasPublishedTo(repositoryName, nodeId)) {
            repositoriesInfo.remove(repositoryName);
        }

        HashMap<String, String> data = new HashMap<>();
        if (documentId != null) {
            data.put(PROPERTY_DOCUMENT_ID, documentId);
        }
        if (saveNumber != null) {
            data.put(PROPERTY_SAVE_NUMBER, saveNumber);
        }
        if (registartionNumber != null) {
            data.put(PROPERTY_REGISTRATION_NUMBER, registartionNumber);
        }
        if (requestType != null) {
            data.put(PROPERTY_REQUEST_TYPE, requestType);
        }

        if (transactionId != null) {
            data.put(PROPERTY_TRANSACTION_ID, transactionId);
        }

        repositoriesInfo.put(repositoryName, data);

        nodeService.setProperty(nodeRef, CircabcModel.PROP_REPOSITORIES_INFO, repositoriesInfo);
    }

    /**
     * Translates locale messages from bundles.
     */
    protected final String translate(final String key, final Object... params) {
        return WebClientHelper.translate(key, params);
    }

    /**
     * @see eu.cec.digit.circabc.service.external.repositories.ExternalRepositoriesManagementService#wasPublishedTo(java.lang.String,
     * java.lang.String)
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean wasPublishedTo(String repositoryName, String nodeId) {

        NodeRef nodeRef = new NodeRef(nodeId);

        if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_EXTERNALLY_PUBLISHED)) {

            if (repositoryName == null) {
                return true;
            }

            HashMap<String, HashMap<String, String>> repositoriesInfo =
                    (HashMap<String, HashMap<String, String>>)
                            nodeService.getProperty(nodeRef, CircabcModel.PROP_REPOSITORIES_INFO);

            return repositoriesInfo != null && repositoriesInfo.containsKey(repositoryName);
        }

        return false;
    }

    /**
     * Gets the list of the internal persons
     *
     * @see eu.cec.digit.circabc.service.external.repositories.ExternalRepositoriesManagementService#getInternalEntities(java.lang.String,
     * java.lang.String, java.lang.String)
     */
    private CurrentInternalEntity[] getInternalEntitiesIntern(
            String firstName, String lastName, String ecasUserName) {

        if ((firstName == null || firstName.isEmpty())
                && (lastName == null || lastName.isEmpty())
                && (ecasUserName == null || ecasUserName.isEmpty())) {
            return null;
        }

        // Build search expression
        String searchExpression = buildSearchExpression(firstName, lastName, null);

        try {
            EntityService_PortType entityService = entityWebService.getEntityService();

            EntitySearchByExpressionRequest searchByExpressionRequest = null;
            FindCurrentInternalEntityRequest entityRequest = null;

            // In case we have a search expression
            if (ecasUserName == null || ecasUserName.isEmpty()) {

                SortOptionsSortBy[] sortOptions = {
                        new SortOptionsSortBy("personFullName", SortOptionsSortByOrder.ASC)
                };

                searchByExpressionRequest =
                        new EntitySearchByExpressionRequest(searchExpression, sortOptions);
            }
            // In case we have an ECAS user name
            else {
                entityRequest = new FindCurrentInternalEntityRequest();

                entityRequest.setSearchForPerson(
                        new FindCurrentInternalEntityRequestSearchForPerson(null, null, ecasUserName, null));
            }

            CurrentInternalEntity[] currentInternalEntities =
                    entityService.findCurrentInternalEntity(
                            createHeader(userNameResolver.getUserName()),
                            entityRequest,
                            searchByExpressionRequest);

            return currentInternalEntities;
        } catch (Exception e) {
            logger.error("Error getting the internal entities.", e);
        }

        return null;
    }

    /**
     * Gets the list of the internal person names
     *
     * @see eu.cec.digit.circabc.service.external.repositories.ExternalRepositoriesManagementService#getInternalEntities(java.lang.String,
     * java.lang.String, java.lang.String)
     */
    @Override
    public List<String> getInternalEntities(String firstName, String lastName, String ecasUserName) {

        CurrentInternalEntity[] currentInternalEntities =
                getInternalEntitiesIntern(firstName, lastName, ecasUserName);

        List<String> internalEntities = new ArrayList<>();

        if (currentInternalEntities != null) {
            for (CurrentInternalEntity entity : currentInternalEntities) {

                CurrentInternalPerson person = entity.getCurrentInternalPerson();

                internalEntities.add(
                        person.getFirstName()
                                + " "
                                + person.getLastName()
                                + " - "
                                + entity.getCurrentEntityId());
            }
        }

        return internalEntities;
    }

    /**
     * Gets the name of the internal entity given its ECAS user name
     */
    public String getInternalEntityName(String ecasUserName) {

        CurrentInternalEntity[] currentInternalEntities =
                getInternalEntitiesIntern(null, null, ecasUserName);

        if (currentInternalEntities != null && currentInternalEntities.length > 0) {

            CurrentInternalPerson person = currentInternalEntities[0].getCurrentInternalPerson();

            return person.getFirstName() + " " + person.getLastName();
        }

        return null;
    }

    /**
     * Gets the id of the internal entity given its ECAS user name
     */
    public String getInternalEntityId(String ecasUserName) {

        CurrentInternalEntity[] currentInternalEntities =
                getInternalEntitiesIntern(null, null, ecasUserName);

        if (currentInternalEntities != null && currentInternalEntities.length > 0) {
            return currentInternalEntities[0].getCurrentEntityId();
        }

        return null;
    }

    /**
     * Gets the list of the external persons
     *
     * @see eu.cec.digit.circabc.service.external.repositories.ExternalRepositoriesManagementService#getExternalEntities(java.lang.String,
     * java.lang.String, java.lang.String)
     */
    @Override
    public List<String> getExternalEntities(String firstName, String lastName, String organization) {

        List<String> externalEntities = new ArrayList<>();

        if ((firstName == null || firstName.isEmpty()) && (lastName == null || lastName.isEmpty())) {
            return externalEntities;
        }

        // Build search expression
        String searchExpression = buildSearchExpression(firstName, lastName, organization);
        try {
            EntityService_PortType entityService = entityWebService.getEntityService();

            SortOptionsSortBy[] sortOptions = {
                    new SortOptionsSortBy("personFullName", SortOptionsSortByOrder.ASC)
            };

            EntitySearchByExpressionRequest searchByExpressionRequest =
                    new EntitySearchByExpressionRequest(searchExpression, sortOptions);

            CurrentExternalEntityRetrievalOptions retrievalOptions =
                    new CurrentExternalEntityRetrievalOptions(false, false);

            CurrentExternalEntity[] currentExternalEntities =
                    entityService.findCurrentExternalEntity(
                            createHeader(userNameResolver.getUserName()),
                            null,
                            searchByExpressionRequest,
                            retrievalOptions);

            if (currentExternalEntities == null) {
                return externalEntities;
            }

            for (CurrentExternalEntity entity : currentExternalEntities) {

                CurrentExternalPerson person = entity.getCurrentExternalPerson();

                externalEntities.add(
                        person.getFirstName()
                                + " "
                                + person.getLastName()
                                + " - "
                                + entity.getCurrentEntityId());
            }
        } catch (Exception e) {
            logger.error("Error getting the external entities.", e);
        }

        return externalEntities;
    }

    /**
     * Builds the search expression based on the fields.
     */
    private String buildSearchExpression(String firstName, String lastName, String organization) {

        StringBuilder searchExpression = new StringBuilder("(");

        if (firstName != null) {
            searchExpression.append("personFirstName startswith '").append(firstName).append("'");
        }
        if (lastName != null) {
            if (searchExpression.length() > 1) {
                searchExpression.append(" or ");
            }
            searchExpression.append("personLastName startswith '").append(lastName).append("'");
        }
        if (organization != null) {
            if (searchExpression.length() > 1) {
                searchExpression.append(" or ");
            }
            searchExpression.append("organization startswith '").append(organization).append("'");
        }

        searchExpression.append(") and isOrganisation=false");

        if (logger.isDebugEnabled()) {
            logger.debug("Search Expression: " + searchExpression.toString());
        }

        return searchExpression.toString();
    }

    /**
     * Asks if the user is authorized to publish according to its profile.
     *
     * @see eu.cec.digit.circabc.service.external.repositories.ExternalRepositoriesManagementService#isUserAuthorizedToPublish(java.lang.String)
     */
    @Override
    public boolean isUserAuthorizedToPublish(String ecasUserName) {

        // Check if the user is already cached
        //		if (hrsInternalEntitiesCache.contains(ecasUserName)) {
        //			return (Boolean) hrsInternalEntitiesCache.get(ecasUserName);
        //		}

        UserProfile userProfile = null;

        Boolean isAuthorized = false;

        try {
            UserService_PortType userService = userWebService.getUserService();

            userProfile = userService.getUserProfile(createHeader(ecasUserName));
            if (logger.isDebugEnabled()) {
                logger.debug("Got profile " + userProfile.getValue() + " for user " + ecasUserName);
            }

            // If we get BASE_USER the user is not allowed to publish, any
            // other profile is ok
            if (UserProfile._BASE_USER.equals(userProfile.getValue())) {
                return isAuthorized;
            }

            isAuthorized = true;
        } catch (Exception e) {
            // If an exception is thrown, the user is not registered or there
            // was an error in Hermes, so don't allow to publish this time
            if (logger.isDebugEnabled()) {
                logger.debug("Exception getting profile for user: " + ecasUserName, e);
            }
            return isAuthorized;
        }
        //		finally {
        //			// Add user to the cache
        //			hrsInternalEntitiesCache.put(ecasUserName, isAuthorized);
        //		}

        return isAuthorized;
    }

    /**
     * Does the publication into Hermes (registration)
     */
    private RegistrationSummary publishToHermes(
            NodeRef nodeRef, RegistrationRequest registrationRequest) throws Exception {

        InputStream content = null;

        try {
            ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);

            content = reader.getContentInputStream();

            String uploadResponse =
                    uploadDocument(registrationRequest.getRegistrationUserName(), content);

            boolean success =
                    Boolean.parseBoolean(
                            (String)
                                    evaluateXPath(
                                            uploadResponse, "/hrs:response/hrs:success/text()", XPathConstants.STRING));
            if (!success) {
                String error =
                        (String)
                                evaluateXPath(
                                        uploadResponse, "/hrs:response/hrs:error/text()", XPathConstants.STRING);
                return new RegistrationSummary(ERROR_UPLOAD_CONTENT + " " + error, null, null, null, null);
            }

            String uploadedContentId =
                    (String)
                            evaluateXPath(
                                    uploadResponse, "/hrs:response/hrs:content-id/text()", XPathConstants.STRING);

            String fileName = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);

            registrationRequest.setUploadedContentId(uploadedContentId);
            registrationRequest.setFileName(fileName);

            return registerDocument(registrationRequest);
        } finally {
            if (content != null) {
                content.close();
            }
        }
    }

    /**
     * Builds and evaluates an XPath expression given an XML
     *
     * @return The result of evaluating the expression.
     */
    private Object evaluateXPath(String xml, String xPathExpression, javax.xml.namespace.QName qName)
            throws Exception {

        InputStream xmlInputStream = null;

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            factory.setNamespaceAware(true);

            // This is the PRIMARY defense. If DTDs (doctypes) are disallowed, almost all XML entity
            // attacks are prevented
            // Xerces 2 only - http://xerces.apache.org/xerces2-j/features.html#disallow-doctype-decl
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

            // If you can't completely disable DTDs, then at least do the following:
            // Xerces 1 - http://xerces.apache.org/xerces-j/features.html#external-general-entities
            // Xerces 2 - http://xerces.apache.org/xerces2-j/features.html#external-general-entities
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);

            // Xerces 1 - http://xerces.apache.org/xerces-j/features.html#external-parameter-entities
            // Xerces 2 - http://xerces.apache.org/xerces2-j/features.html#external-parameter-entities
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

            // and these as well, per Timothy Morgan's 2014 paper: "XML Schema, DTD, and Entity Attacks"
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);

            DocumentBuilder builder = factory.newDocumentBuilder();

            xmlInputStream = new ByteArrayInputStream(xml.getBytes("UTF-8"));
            Document document = builder.parse(xmlInputStream);

            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xPath = xPathFactory.newXPath();
            xPath.setNamespaceContext(new EUNamespaceContext());

            XPathExpression expr = xPath.compile(xPathExpression);

            return expr.evaluate(document, qName);
        } finally {
            if (xmlInputStream != null) {
                try {
                    xmlInputStream.close();
                } catch (IOException e) {
                    logger.error(e);
                }
            }
        }
    }

    /**
     * Uploads a document to the Hermes temp space and returns its id embedded in an XML, if
     * successful This id has to be extracted to be used to link the uploaded content when registering
     * the document.
     */
    private String uploadDocument(String userName, InputStream content) throws Exception {

        PostMethod postMethod = new PostMethod(uploadUrl);

        RequestEntity requestEntity =
                new InputStreamRequestEntity(content, "application/octet-stream; charset=UTF-8");
        try {
            postMethod.setRequestHeader(new Header("X-HRS-USER", userName));
            postMethod.setRequestHeader(new Header("X-HRS-TICKET", proxyTicketResolver.getProxyTicket()));
            postMethod.setRequestHeader(new Header("X-HRS-APPLICATION", applicationId));

            postMethod.setRequestEntity(requestEntity);

            HttpClient httpClient = new HttpClient();
            httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(5000);

            int status = httpClient.executeMethod(postMethod);

            if (status == HttpStatus.SC_OK) {
                return postMethod.getResponseBodyAsString();
            } else {
                return buildError(
                        HttpStatus.getStatusText(status) + " Response=" + postMethod.getResponseBodyAsString());
            }
        } catch (Exception e) {
            logger.error("Error uploading the given document.", e);
            return buildError(e.getMessage());
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Builds an Hermes-like XML error message to keep a standard response in case there is another
     * kind of error.
     */
    private String buildError(String message) {
        return "<response xmlns=\"http://ec.europa.eu/sg/hrs/dts/1.0\" "
                + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
                + "xsi:schemaLocation=\"http://ec.europa.eu/sg/hrs/dts/1.0 "
                + "http://www.cc.cec/hrs-dts/DataTransferService?XSD\">"
                + "<success>false</success>"
                + "<error>Status="
                + message
                + "</error>"
                + "</response>";
    }

    /**
     * Registers a document in Hermes.
     */
    private RegistrationSummary registerDocument(RegistrationRequest registrationRequest) {

        try {
            DocumentService_PortType documentService = documentWebService.getDocumentService();

            // Add sender id
            List<String> senderIds = registrationRequest.getSenderIds();
            SendersToAddSender[] senders = new SendersToAddSender[senderIds.size()];
            for (int idx = 0; idx < senderIds.size(); idx++) {
                SendersToAddSender sender = new SendersToAddSender();
                sender.setCurrentEntityId(senderIds.get(idx));
                senders[idx] = sender;
            }

            // Add recipient ids
            List<String> recipientIds = registrationRequest.getRecipientIds();
            RecipientsToAddRecipient[] recipients = new RecipientsToAddRecipient[recipientIds.size()];
            for (int idx = 0; idx < recipientIds.size(); idx++) {
                RecipientsToAddRecipient recipient = new RecipientsToAddRecipient();
                recipient.setCurrentEntityId(recipientIds.get(idx));
                recipient.setCode(RecipientCode.TO);
                recipients[idx] = recipient;
            }

            // Add items
            UploadedItemToAdd item =
                    new UploadedItemToAdd(
                            registrationRequest.getFileName(),
                            registrationRequest.getUploadedContentId(),
                            AttachmentTypeToAdd.NATIVE_ELECTRONIC,
                            "EN",
                            ItemKindToAdd.MAIN,
                            null,
                            null);

            DocumentRegistrationRequest request =
                    new DocumentRegistrationRequest(
                            registrationRequest.getSubject(),
                            new Date(),
                            new Date(),
                            SecurityClassification.NORMAL,
                            false,
                            null,
                            null,
                            registrationRequest.getComments(),
                            MailType.fromString(registrationRequest.getMailType()),
                            senders,
                            recipients,
                            new UploadedItemToAdd[]{item});

            return documentService.registerDocument(
                    createHeader(registrationRequest.getRegistrationUserName()), request);
        } catch (Exception e) {
            logger.error("Error registering document.", e);
            return new RegistrationSummary(
                    ERROR_REGISTER_DOCUMENT + " " + e.getMessage(), null, null, null, null);
        }
    }

    /**
     * Starts an assignment task workflow.
     */
    private AssignmentWorkflow addAssignmentTask(AssignmentTaskRequest request) {

        AssignmentWorkflow workflow = null;

        try {
            WorkflowService_PortType workflowService = workflowWebService.getWorkflowService();

            // Add all the assignment tasks
            List<String> assigneeIds = request.getAssigneeIds();
            AssignmentTaskToAdd[] tasks = new AssignmentTaskToAdd[assigneeIds.size()];
            for (int idx = 0; idx < assigneeIds.size(); idx++) {
                AssignmentTaskToAdd task = new AssignmentTaskToAdd();
                task.setAssigneeId(assigneeIds.get(idx));
                task.setCode(AssignmentTaskToAddCode.CLASS);
                tasks[idx] = task;
            }

            AddAssignmentsRequest assignmentsRequest =
                    new AddAssignmentsRequest(request.getDocumentId(), tasks);

            workflow =
                    workflowService.addAssignments(
                            createHeader(request.getAssignmentUserName()), assignmentsRequest);
        } catch (Exception e) {
            logger.error("Error while assigning workflow.", e);
            workflow =
                    new AssignmentWorkflow(
                            0,
                            "Error assigning workflow to document with Id: "
                                    + request.getDocumentId()
                                    + " - "
                                    + e.getMessage(),
                            null,
                            null);
        }

        return workflow;
    }

    /**
     * Builds the header for the HRS requests.
     */
    private eu.cec.digit.circabc.repo.hrs.ws.Header createHeader(String userName) {
        return new eu.cec.digit.circabc.repo.hrs.ws.Header(
                userName, null, proxyTicketResolver.getProxyTicket(), applicationId);
    }

    /**
     * Sets the value of the nodeService
     *
     * @param nodeService the nodeService to set.
     */
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /**
     * Sets the value of the transactionService
     *
     * @param transactionService the transactionService to set.
     */
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * Sets the value of the contentService
     *
     * @param contentService the contentService to set.
     */
    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    /**
     * Sets the value of the applicationId
     *
     * @param applicationId the applicationId to set.
     */
    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    /**
     * Gets the value of the userNameResolver
     *
     * @return the userNameResolver
     */
    @Override
    public UserNameResolver getUserNameResolver() {
        return userNameResolver;
    }

    /**
     * Sets the value of the userNameResolver
     *
     * @param userNameResolver the userNameResolver to set.
     */
    public void setUserNameResolver(UserNameResolver userNameResolver) {
        this.userNameResolver = userNameResolver;
    }

    /**
     * Sets the value of the proxyTicketResolver
     *
     * @param proxyTicketResolver the proxyTicketResolver to set.
     */
    public void setProxyTicketResolver(ProxyTicketResolver proxyTicketResolver) {
        this.proxyTicketResolver = proxyTicketResolver;
    }

    /**
     * Sets the value of the uploadUrl
     *
     * @param uploadUrl the uploadUrl to set.
     */
    public void setUploadUrl(String uploadUrl) {
        this.uploadUrl = uploadUrl;
    }

    /**
     * Sets the value of the endpointBaseAddress
     *
     * @param endpointBaseAddress the endpointBaseAddress to set.
     */
    public void setEndpointBaseAddress(String endpointBaseAddress) {
        this.endpointBaseAddress = endpointBaseAddress;
    }

    /**
     * Sets the value of the aresLinkPrefix
     *
     * @param aresLinkPrefix the aresLinkPrefix to set.
     */
    public void setAresLinkPrefix(String aresLinkPrefix) {
        this.aresLinkPrefix = aresLinkPrefix;
    }

    /**
     * Sets the value of the minutesToCheck
     *
     * @param minutesToCheck the minutesToCheck to set.
     */
    public void setMinutesToCheck(long minutesToCheck) {
        this.minutesToCheck = minutesToCheck;
    }

    /**
     * Sets the value of the hrsInternalEntitiesCache
     *
     * @param hrsInternalEntitiesCache the hrsInternalEntitiesCache to set.
     */
    public void setHrsInternalEntitiesCache(SimpleCache<String, Object> hrsInternalEntitiesCache) {
        this.hrsInternalEntitiesCache = hrsInternalEntitiesCache;
    }

    /**
     * Class to manage the default namespaces of Hermes XML answers.
     *
     * @author schwerr
     */
    private static class EUNamespaceContext implements NamespaceContext {

        public String getNamespaceURI(String prefix) {
            if ("hrs".equals(prefix)) {
                return "http://ec.europa.eu/sg/hrs/dts/1.0";
            }
            return null;
        }

        public String getPrefix(String namespaceURI) {
            return null;
        }

        public Iterator<String> getPrefixes(String namespaceURI) {
            return null;
        }
    }
}
