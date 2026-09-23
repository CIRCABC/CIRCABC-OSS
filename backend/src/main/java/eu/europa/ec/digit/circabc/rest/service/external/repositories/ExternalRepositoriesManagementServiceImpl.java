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
package eu.europa.ec.digit.circabc.rest.service.external.repositories;

import io.swagger.model.RepositoryConfiguration;
import io.swagger.model.alfresco.CircabcModel;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Default implementation of {@link ExternalRepositoriesManagementService}.
 *
 * <p>This service manages the concerns related to publishing CIRCABC documents to external
 * repositories (such as Hermes document publishing). It performs two distinct groups of
 * operations against the Alfresco repository:
 *
 * <ul>
 *   <li>Recording the fact that a node was published to a named external repository, by attaching
 *       the {@link CircabcModel#ASPECT_EXTERNALLY_PUBLISHED} aspect and maintaining the
 *       {@link CircabcModel#PROP_REPOSITORIES_INFO} property (a map keyed by repository name).
 *   <li>Managing the configuration of the available external repositories, which are stored as
 *       child nodes under a dedicated {@code ExternalRepositoryConfigurations} folder.
 * </ul>
 *
 * @author schwerr
 */
public class ExternalRepositoriesManagementServiceImpl
  implements ExternalRepositoriesManagementService
{

  // Published document properties (text is translated from the bundle)

  /** Key, within the per-repository info map, holding the external document identifier. */
  private static final String PROPERTY_DOCUMENT_ID = "property_document_id";

  /** Key, within the per-repository info map, holding the registration number. */
  private static final String PROPERTY_REGISTRATION_NUMBER =
    "property_registration_number";

  /** Key, within the per-repository info map, holding the save number. */
  private static final String PROPERTY_SAVE_NUMBER = "property_save_number";

  /** Key, within the per-repository info map, holding the request type. */
  private static final String PROPERTY_REQUEST_TYPE = "property_request_type";

  /** Key, within the per-repository info map, holding the transaction identifier. */
  private static final String PROPERTY_TRANSACTION_ID =
    "property_transaction_id";

  /** Alfresco node service used to read and mutate node aspects, properties and associations. */
  @Autowired
  private NodeService nodeService;

  /** Alfresco transaction service used to run mutating operations in retrying transactions. */
  @Autowired
  private TransactionService transactionService;

  /**
   * Records the metadata returned by an external repository after a node has been published to it.
   *
   * <p>If the node does not yet carry the {@link CircabcModel#ASPECT_EXTERNALLY_PUBLISHED} aspect it
   * is added first. Any previously stored information for the same {@code repositoryName} is
   * replaced. Only the non-{@code null} metadata values are stored.
   *
   * @param repositoryName the name of the external repository the node was published to; used as
   *     the key under which the metadata is stored
   * @param nodeId the string reference of the published node
   * @param documentId the external document identifier, or {@code null} if not provided
   * @param saveNumber the external save number, or {@code null} if not provided
   * @param registartionNumber the external registration number, or {@code null} if not provided
   * @param requestType the external request type, or {@code null} if not provided
   * @param transactionId the external transaction identifier, or {@code null} if not provided
   */
  @SuppressWarnings("unchecked")
  @Override
  public void saveExternalMetadata(
    String repositoryName,
    String nodeId,
    String documentId,
    String saveNumber,
    String registartionNumber,
    String requestType,
    String transactionId
  ) {
    final NodeRef nodeRef = new NodeRef(nodeId);
    // Add to Alfresco
    HashMap<String, HashMap<String, String>> repositoriesInfo = null;

    if (
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_EXTERNALLY_PUBLISHED)
    ) {
      repositoriesInfo = (HashMap<
        String,
        HashMap<String, String>
      >) nodeService.getProperty(nodeRef, CircabcModel.PROP_REPOSITORIES_INFO);
    } else {
      repositoriesInfo = new HashMap<>();

      Map<QName, Serializable> aspectProperties = new HashMap<>();

      nodeService.addAspect(
        nodeRef,
        CircabcModel.ASPECT_EXTERNALLY_PUBLISHED,
        aspectProperties
      );
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

    nodeService.setProperty(
      nodeRef,
      CircabcModel.PROP_REPOSITORIES_INFO,
      repositoriesInfo
    );
  }

  /**
   * Determines whether a node has already been published to an external repository.
   *
   * @param repositoryName the name of the external repository to check for; when {@code null} the
   *     method only checks whether the node has been published to any repository at all
   * @param nodeId the string reference of the node to inspect
   * @return {@code true} if the node carries the {@link CircabcModel#ASPECT_EXTERNALLY_PUBLISHED}
   *     aspect and (when {@code repositoryName} is provided) has stored info for that repository;
   *     {@code false} otherwise
   */
  @Override
  @SuppressWarnings("unchecked")
  public boolean wasPublishedTo(String repositoryName, String nodeId) {
    NodeRef nodeRef = new NodeRef(nodeId);

    if (
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_EXTERNALLY_PUBLISHED)
    ) {
      if (repositoryName == null) {
        return true;
      }

      HashMap<String, HashMap<String, String>> repositoriesInfo = (HashMap<
        String,
        HashMap<String, String>
      >) nodeService.getProperty(nodeRef, CircabcModel.PROP_REPOSITORIES_INFO);

      return (
        repositoriesInfo != null && repositoriesInfo.containsKey(repositoryName)
      );
    }

    return false;
  }

  /**
   * Returns the external repository configurations registered under the given parent node.
   *
   * <p>The configurations are read from the {@code ExternalRepositoryConfigurations} folder that
   * lives beneath the parent node. If the folder does not exist an empty collection is returned.
   *
   * @param parentNodeId the string reference of the parent node holding the configuration folder
   * @return a collection of {@link RepositoryConfiguration} entries, each populated with the
   *     repository name and registration (creation) date; never {@code null}
   */
  @Override
  public Collection<RepositoryConfiguration> getConfiguredRepositories(
    String parentNodeId
  ) {
    NodeRef parentNodeRef = createOrGetRepositoryConfigurationFolder(
      parentNodeId,
      false
    );

    Collection<RepositoryConfiguration> repositories = new ArrayList<>();

    if (parentNodeRef == null) {
      return repositories;
    }

    List<ChildAssociationRef> children = nodeService.getChildAssocs(
      parentNodeRef
    );

    for (ChildAssociationRef child : children) {
      RepositoryConfiguration configuration = new RepositoryConfiguration();

      NodeRef childNodeRef = child.getChildRef();

      String name = (String) nodeService.getProperty(
        childNodeRef,
        ContentModel.PROP_NAME
      );
      Date registrationDate = (Date) nodeService.getProperty(
        childNodeRef,
        ContentModel.PROP_CREATED
      );

      configuration.setName(name);
      configuration.setRegistrationDate(registrationDate);

      repositories.add(configuration);
    }

    return repositories;
  }

  /**
   * Registers a new external repository configuration under the given parent node.
   *
   * <p>The configuration is stored as a child node inside the {@code ExternalRepositoryConfigurations}
   * folder, which is created on demand if it does not already exist. The operation runs as the
   * system user within a retrying transaction.
   *
   * @param parentNodeId the string reference of the parent node under which the configuration folder
   *     resides (or is created)
   * @param configuration the repository configuration to persist; its name is used as the node name
   */
  @Override
  public void addRepository(
    final String parentNodeId,
    final RepositoryConfiguration configuration
  ) {
    final RetryingTransactionHelper txnHelper =
      transactionService.getRetryingTransactionHelper();

    final RetryingTransactionCallback<Object> callback =
      new RetryingTransactionCallback<Object>() {
        public Object execute() throws Throwable {
          NodeRef parentNodeRef = createOrGetRepositoryConfigurationFolder(
            parentNodeId,
            true
          );

          Map<QName, Serializable> properties = new HashMap<>();

          properties.put(ContentModel.PROP_NAME, configuration.getName());

          nodeService
            .createNode(
              parentNodeRef,
              CircabcModel.ASSOC_CONTAINSCON_FIGURATIONS,
              QName.createQName(
                ContentModel.PROP_NAME.getNamespaceURI(),
                QName.createValidLocalName(configuration.getName())
              ),
              CircabcModel.TYPE_EXTERNAL_REPOSITORY_CONFIGURATION,
              properties
            )
            .getChildRef();
          return null;
        }
      };

    AuthenticationUtil.runAs(
      () -> txnHelper.doInTransaction(callback, false, true),
      AuthenticationUtil.getSystemUserName()
    );
  }

  /**
   * Removes a previously registered external repository configuration.
   *
   * <p>Looks up the configuration node by name inside the {@code ExternalRepositoryConfigurations}
   * folder and, if found, removes it. The removal runs as the system user within a retrying
   * transaction. The call is a no-op when either the configuration folder or the named
   * configuration does not exist.
   *
   * @param parentNodeId the string reference of the parent node holding the configuration folder
   * @param repositoryName the name of the repository configuration to remove
   */
  @Override
  public void removeRepository(String parentNodeId, String repositoryName) {
    final NodeRef parentNodeRef = createOrGetRepositoryConfigurationFolder(
      parentNodeId,
      false
    );

    if (parentNodeRef == null) {
      return;
    }

    final NodeRef configuredRepositoryNodeRef = nodeService.getChildByName(
      parentNodeRef,
      CircabcModel.ASSOC_CONTAINSCON_FIGURATIONS,
      repositoryName
    );

    if (configuredRepositoryNodeRef == null) {
      return;
    }

    final RetryingTransactionHelper txnHelper =
      transactionService.getRetryingTransactionHelper();

    final RetryingTransactionCallback<Object> callback =
      new RetryingTransactionCallback<Object>() {
        public Object execute() throws Throwable {
          nodeService.removeChild(parentNodeRef, configuredRepositoryNodeRef);

          return null;
        }
      };

    AuthenticationUtil.runAs(
      () -> txnHelper.doInTransaction(callback, false, true),
      AuthenticationUtil.getSystemUserName()
    );
  }

  /**
   * Resolves the {@code ExternalRepositoryConfigurations} folder beneath the given parent node,
   * optionally creating it when absent.
   *
   * @param parentNodeId the string reference of the parent node
   * @param create when {@code true} the folder is created if it does not already exist; when
   *     {@code false} the method returns {@code null} for a missing folder
   * @return the {@link NodeRef} of the existing or newly created configuration folder, or
   *     {@code null} if the folder is absent and {@code create} is {@code false}
   */
  private NodeRef createOrGetRepositoryConfigurationFolder(
    String parentNodeId,
    boolean create
  ) {
    NodeRef parentNodeRef = new NodeRef(parentNodeId);

    String localName = "ExternalRepositoryConfigurations";

    NodeRef externalRepositoryNodeRef = nodeService.getChildByName(
      parentNodeRef,
      ContentModel.ASSOC_CONTAINS,
      localName
    );

    if (externalRepositoryNodeRef != null) {
      return externalRepositoryNodeRef;
    }

    if (!create) {
      return null;
    }

    Map<QName, Serializable> properties = new HashMap<>();

    properties.put(ContentModel.PROP_NAME, localName);

    externalRepositoryNodeRef = nodeService
      .createNode(
        parentNodeRef,
        ContentModel.ASSOC_CONTAINS,
        QName.createQName(
          ContentModel.PROP_NAME.getNamespaceURI(),
          QName.createValidLocalName(localName)
        ),
        CircabcModel.TYPE_EXTERNAL_REPOSITORY_CONFIGURATION_FOLDER,
        properties
      )
      .getChildRef();

    return externalRepositoryNodeRef;
  }
}
