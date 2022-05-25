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
package eu.cec.digit.circabc.service.external.repositories;

import eu.cec.digit.circabc.repo.external.repositories.RegistrationRequest;
import eu.cec.digit.circabc.repo.external.repositories.RepositoryConfiguration;
import eu.cec.digit.circabc.repo.external.repositories.UserNameResolver;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Interface for the service that manages the external repository publishing.
 *
 * @author schwerr
 */
public interface ExternalRepositoriesManagementService {

    String EXTERNAL_REPOSITORY_NAME = "Hermes";

    // Error messages (text is translated from the bundle)
    String ERROR_UPLOAD_CONTENT = "error_upload_content";
    String ERROR_REGISTER_DOCUMENT = "error_register_document";

    // Published document properties (text is translated from the bundle)
    String PROPERTY_DOCUMENT_ID = "property_document_id";
    String PROPERTY_REGISTRATION_NUMBER = "property_registration_number";
    String PROPERTY_SAVE_NUMBER = "property_save_number";
    String PROPERTY_REGISTRATION_DATE = "property_registration_date";
    String PROPERTY_ENCODING_DATE = "property_encoding_date";
    String PROPERTY_LAST_CHECKED = "property_last_checked";
    String PROPERTY_DATA_STATUS = "property_data_status";
    String PROPERTY_WORKFLOW_ID = "property_workflow_id";
    String PROPERTY_REGISTRATION_AUTHOR = "property_registration_author";
    String PROPERTY_DIRECT_ARES_LINK = "property_direct_ares_link";
    String PROPERTY_REQUEST_TYPE = "property_request_type";
    String PROPERTY_TRANSACTION_ID = "property_transaction_id";

    String PROPERTY_INTERNAL_SENDERS = "property_internal_senders";
    String PROPERTY_EXTERNAL_RECIPIENTS = "property_external_recipients";
    String PROPERTY_REGISTRATION_USER = "property_registration_user";
    String PROPERTY_TITLE = "publish_in_external_repositories_dialog_subject";
    String PROPERTY_COMMENTS = "publish_in_external_repositories_dialog_comment";
    String PROPERTY_MAIL_TYPE = "publish_in_external_repositories_dialog_mail_type";

    // Status
    String STATUS_REGISTERED = "Registered";
    String STATUS_FILED = "Filed";
    String PUBLISH_SUCCESS_WORKFLOW_FAILURE = "publish_success_workflow_failure";

    String STATUS_UNABLE = "Unable to check";
    String STATUS_OK = "OK";

    // General format for the dates
    String DATE_FORMAT = "dd MMMM yyyy";
    String EXTENDED_DATE_FORMAT = "dd MMMM yyyy HH:mm";

    String VERSION_STORE = "versionStore";

    /**
     * Returns true if the external repositories system is operational.
     */
    boolean isOperational();

    /**
     * Retrieves the list of the available repositories configured to publish to.
     */
    Collection<RepositoryConfiguration> getAvailableRepositories();

    /**
     * Adds a repository to the list of configured repositories.
     */
    void addRepository(String parentNodeId, RepositoryConfiguration configuration);

    /**
     * Removes a repository from the list of configured repositories.
     */
    void removeRepository(String parentNodeId, String repositoryName);

    /**
     * Returns the list of all configured repositories.
     */
    Collection<RepositoryConfiguration> getConfiguredRepositories(String parentNodeId);

    /**
     * Gets the properties of a document for the repositories.
     */
    Map<String, Map<String, String>> getRepositoryDataForDocument(String documentId);

    /**
     * Publishes the given document to the given repository.
     */
    PublishResponse publishDocument(
            String repositoryName, String nodeId, RegistrationRequest registrationRequest);

    /**
     * Publishes the given document to the given repository.
     */
    void saveExternalMetadata(
            String repositoryName,
            String nodeId,
            String documentId,
            String saveNumber,
            String registartionNumber,
            String requestType,
            String transactionId);

    /**
     * Checks if the given document was published to the given repository.
     */
    boolean wasPublishedTo(String repositoryName, String nodeId);

    /**
     * Gets a list of internal entities.
     */
    List<String> getInternalEntities(String firstName, String lastName, String ecasUserName);

    /**
     * Gets a list of external entities.
     */
    List<String> getExternalEntities(String firstName, String lastName, String organization);

    /**
     * Retrieves the user's profile and according to the response determines if the user is allowed to
     * publish.
     */
    boolean isUserAuthorizedToPublish(String ecasUserName);

    /**
     * Gets the value of the userNameResolver
     *
     * @return the userNameResolver
     */
    UserNameResolver getUserNameResolver();
}
