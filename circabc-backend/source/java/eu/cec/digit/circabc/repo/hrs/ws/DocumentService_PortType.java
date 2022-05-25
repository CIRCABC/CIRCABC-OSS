/**
 * Copyright 2006 European Community
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
 *
 * <p>DocumentService_PortType.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 * <p>
 * DocumentService_PortType.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
/**
 * DocumentService_PortType.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package eu.cec.digit.circabc.repo.hrs.ws;

public interface DocumentService_PortType extends java.rmi.Remote {

    /** This operation performs the filing of a document into a file of the filing plan */
    public eu.cec.digit.circabc.repo.hrs.ws.OperationResponse fileDocument(
            eu.cec.digit.circabc.repo.hrs.ws.Header header,
            java.lang.String documentId,
            java.lang.String fileId)
            throws java.rmi.RemoteException;

    /** This operation performs the unfiling of a document from a file of the filing plan */
    public eu.cec.digit.circabc.repo.hrs.ws.OperationResponse unfileDocument(
            eu.cec.digit.circabc.repo.hrs.ws.Header header,
            java.lang.String documentId,
            java.lang.String fileId)
            throws java.rmi.RemoteException;

    /**
     * This operation creates a document in the common repository. It returns generated metadata, such
     * as the saveNumber. <br>
     * If the securityClassification is a value different than 'NORMAL', the document can have a
     * marking. Markings affect the visibility of the document but can also mandate other
     * restrictions. For now, the COMP and OLAF family of markings forbid having items on the
     * document. <br>
     * Specifying securityClassification 'EU_RESTRAINED' also forbids items on the document.
     */
    public eu.cec.digit.circabc.repo.hrs.ws.DocumentCreationSummary createDocument(
            eu.cec.digit.circabc.repo.hrs.ws.Header header,
            eu.cec.digit.circabc.repo.hrs.ws.DocumentCreationRequest request)
            throws java.rmi.RemoteException;

    /**
     * This operation updates an existing document from the common repository. <br>
     * If the document has checked out items or is otherwise locked, the operation fails.<br>
     * If the securityClassification is changed to 'EU_RESTRAINED', all existing items must be removed
     * as part of the update. </br>
     *
     * <p>The same is true for COMP and OLAF markings. Because they don't allow any items, existing
     * items must be removed as part of the update if any. <br>
     */
    public boolean updateDocument(
            eu.cec.digit.circabc.repo.hrs.ws.Header header,
            eu.cec.digit.circabc.repo.hrs.ws.DocumentUpdateRequest request)
            throws java.rmi.RemoteException;

    /**
     * This operation sends an unregistered document to trash. Registered documents can never be
     * trashed. <br>
     * Trashing a document will unfile it from any file where it is currently filed, and will link it
     * to a special trash folder. <br>
     * If a document sits more than 6 month in trash, it will be deleted from the system. If a
     * document is filed after being trashed, but before being deleted, it's going to be restored
     * (un-trashed). <br>
     * The user must have visibility on the document in order to trash it.
     */
    public boolean trashDocument(
            eu.cec.digit.circabc.repo.hrs.ws.Header header, java.lang.String documentId)
            throws java.rmi.RemoteException;

    /**
     * This operation registers an existing document. It returns generated metadata, such as the
     * registrationNumber. <br>
     * If the document has checked out items or is otherwise locked, the operation fails. <br>
     * If there are open eSignatory tasks, they will be automatically closed.
     */
    public eu.cec.digit.circabc.repo.hrs.ws.RegistrationByIdSummary registerDocumentById(
            eu.cec.digit.circabc.repo.hrs.ws.Header header, java.lang.String documentId)
            throws java.rmi.RemoteException;

    /**
     * This operation performs the creation and registration of a document in the common repository.
     * It returns generated metadata, such as the saveNumber and the registrationNumber. <br>
     * If the securityClassification has a value other than 'NORMAL', the document can have a marking.
     * Markings affect the visibility of the document but can also mandate other restrictions. For
     * now, the COMP and OLAF family of markings forbid having items on the document. Specifying the
     * securityClassification 'EU_RESTRAINED' also forbids items on the document. In all other cases,
     * it is compulsory to have at least one item on the document to be able to perform registration.
     */
    public eu.cec.digit.circabc.repo.hrs.ws.RegistrationSummary registerDocument(
            eu.cec.digit.circabc.repo.hrs.ws.Header header,
            eu.cec.digit.circabc.repo.hrs.ws.DocumentRegistrationRequest request)
            throws java.rmi.RemoteException;

    /**
     * This operation performs a checkout of the specified item. Only items with uploaded content
     * (i.e. items with attachment type different from ARES_SCANNED), belonging to unregistered
     * documents can be checked out. Translations cannot be checked out.<br>
     * A checkout will place a lock on the item, and the item will become readonly unless the checkout
     * operation is canceled. The document to which the item belongs will also be considered locked as
     * long as the item is checked out.<br>
     * To retrieve the content of the item after checkout, the normal HTTP data transfer service can
     * be used.
     */
    public boolean checkoutItem(
            eu.cec.digit.circabc.repo.hrs.ws.Header header, java.lang.String itemId)
            throws java.rmi.RemoteException;

    /**
     * This operation canceles the checkout of an item. The lock is removed from the item and
     * modifications are again possible on it.<br>
     * Note that the user that cancels the checkout must be the same as the one that has performed the
     * checkout.
     */
    public boolean cancelItemCheckout(
            eu.cec.digit.circabc.repo.hrs.ws.Header header, java.lang.String itemId)
            throws java.rmi.RemoteException;

    /**
     * This operation creates a new version of the item, with the specified content. The item content
     * should have previously been uploaded with the usual HTTP data transfer service. <br>
     * Note that the user that checks in the item must be the same as the one that has performed the
     * checkout.<br>
     * The new version of the item will receive a new repository id, which is returned by this
     * operation. The old version of the item will not be accessible anymore with the current HRS
     * services (i.e you will not be able to retrieve the item's previous version by using the old
     * item id); however, new web service operations may be added in the future to retrieve old item
     * versions if needed.
     */
    public java.lang.String checkinItem(
            eu.cec.digit.circabc.repo.hrs.ws.Header header,
            eu.cec.digit.circabc.repo.hrs.ws.ItemCheckinRequest request)
            throws java.rmi.RemoteException;

    /**
     * This operation adds translations to one of the items of a document (the document can be
     * registered or unregistered). <br>
     * For registered documents, only the document creator or DMO can add translations.
     */
    public boolean addTranslations(
            eu.cec.digit.circabc.repo.hrs.ws.Header header,
            java.lang.String itemId,
            eu.cec.digit.circabc.repo.hrs.ws.TranslationsToAddTranslation[] translations)
            throws java.rmi.RemoteException;

    /**
     * This method is deprecated and may be removed in future releases. Use the operation
     * searchDocumentsByExpression instead, with a search expression like documentId='...'. The
     * advantage of searchDocumentsByExpression is that it doesn't raise a failure if the document
     * does not exist and it provides fine grained control over what elements of the document should
     * be retrieved.
     */
    public eu.cec.digit.circabc.repo.hrs.ws.Document getDocument(
            eu.cec.digit.circabc.repo.hrs.ws.Header header, java.lang.String documentId)
            throws java.rmi.RemoteException;

    /**
     * This operation searches after documents that match the specified search expression.
     *
     * <p>A search can be performed using an expression for the meta-data (e.g.
     * <tt>encodingDate>=2010-01-01</tt>), an expression for fulltext search (e.g.
     * <tt>FULLTEXT('Report')</tt>) or both expressions for a combined search. When performing a
     * combined search (i.e. specifying both the meta-data search expression and the fulltext search
     * expression) the effect will be of a logical AND between those two expressions. For example,
     * specifying the meta-data search expression <tt>encodingDate>=2010-01-01</tt> and the fulltext
     * search expression <tt>FULLTEXT('Report')</tt> will return those documents that contain the word
     * 'Report' AND have been created after 01/01/2010.
     *
     * <p>For performance reasons, the search will retrieve a maximum of 2000 results, no matter how
     * many documents match the search criteria. Furthermore, if more than 300 documents are requested
     * (i.e value of <tt>max</tt> is greater than 300), then only basic metadata of documents can be
     * retrieved. Thus, specifying any value in <tt>DocumentRetrievalOptions</tt> when
     * <tt>max>300</tt> is not allowed.
     *
     * <p>Fields that can be used inside the search expression are:
     *
     * <ul>
     *   <li>documentId - the document's internal id
     *   <li>title - the document's title
     *   <li>registrationNumber - the document's registration number
     *   <li>saveNumber - the document's save number
     *   <li>registrationDate - the document's registration date
     *   <li>documentDate - the document's date
     *   <li>encodingDate - the date when the document was created in the repository
     *   <li>sendDate - the date when the document was sent
     *   <li>modificationDate - the date when the document was last modified. A modification is
     *       considered to be any change to the document's metadata, filing, items, senders and
     *       recipients.
     *   <li>comments - the document's comments
     *   <li>createdOnBehalfOfEcasId - User name of the user on whose behalf the document was created
     *       (e.g. if a secretary creates a document on behalf of the head of unit, this field refers
     *       to the ECAS id of the head of unit)
     *   <li>createdOnBehalfOfName - fullname of the person on behalf of which the document was
     *       created.
     *   <li>createdOnBehalfOfOrganization - organization name of the person on behalf of which the
     *       document was created.
     *   <li>creatorEcasId - ECAS id of the document's creator (e.g. ECAS id of the secretary that
     *       created the document, even if acting on behalf of somebody else). Use this field for
     *       documents created with delegations, otherwise use createdOnBehalfOfEcasId.
     *   <li>creatorName - full name of the document's creator. Use this field for documents created
     *       with delegations, otherwise use createdOnBehalfOfName.
     *   <li>creatorOrganization - organization name of the document's creator. Use this field for
     *       documents created with delegations, otherwise use createdOnBehalfOfOrganization.
     *   <li>registeredOnBehalfOfEcasId - ECAS id of the person on behalf of which the document was
     *       registered.
     *   <li>registeredOnBehalfOfName - full name of the person on behalf of which the document was
     *       registered.
     *   <li>registeredOnBehalfOfOrganization - organization name of the person on behalf of which the
     *       document was registered
     *   <li>registererEcasId - ECAS id of the person that registered the document. Use this field for
     *       documents registered with delegations, otherwise use registeredOnBehalfOf.
     *   <li>registererName - full name of the person that registered the document. Use this field for
     *       documents registered with delegations, otherwise use registeredOnBehalfOfName.
     *   <li>registererOrganization - organization name of the person that registered the document.
     *       Use this field for documents registered with delegations, otherwise use
     *       registeredOnBehalfOfOrganization.
     *   <li>isEncrypted - whether the document's items are stored encrypted or not. Note that
     *       regardless of how they are stored, items are always decrypted before being sent to the
     *       client.
     *   <li>itemName - search after documents that have at least one item with a name that satisfies
     *       the specified condition
     *   <li>itemScanWithoutContent - search after documents that have at least one item for which the
     *       scanning status matches the condition (e.g. true indicates a scanned item that has no
     *       content because it has not been scanned yet)
     *   <li>itemExternalReference - search after documents that have at least one item for which the
     *       external reference satisfies the condition
     *   <li>itemLanguage - search after documents that have at least one item for which the language
     *       matches the condition
     *   <li>senderId - search for documents that have at least one sender with an entity id that
     *       satisfies the specified condition
     *   <li>senderIsInternal - search for documents where the sender is internal (person or
     *       organization)
     *   <li>senderIsOrganization - search for documents where the sender is an organization
     *   <li>senderName - the full name of the person
     *   <li>senderOrganization - the name of the organization
     *   <li>recipientId - search for documents that have at least one recipient with an entity id
     *       that satisfies the specified condition
     *   <li>recipientIsInternal - search for documents where the recipient is internal
     *   <li>recipientIsOrganization - search for documents where the recipient is an organization
     *   <li>recipientName - the fullname of the person
     *   <li>recipientOrganization - the name of the organization
     *   <li>recipientCode - TO or CC
     *   <li>workflowCategory - the category of the workflow - ESIGNATORY, ASSIGNMENT or PAPER
     *   <li>actionCode - search documents that have at least one task with a task code that satisfies
     *       the condition
     *   <li>assignmentDate - the date when the task was assigned
     *   <li>taskComments - comments in the task
     *   <li>taskInstructions - instructions in the task
     *   <li>taskStatus - status of the task, such as ACTIVE, CLOSED, BYPASSED etc.
     *   <li>assigneeEcasId - ECAS id of the task's assigned person
     *   <li>assigneeName - the name of the assigned person
     *   <li>assigneeOrganization - the organization name the assignee belongs to
     *   <li>assignerEcasId - ECAS id of the person that assigned the task
     *   <li>assignerName - the name of the person that assigned the task
     *   <li>assignerOrganization - the organization code the assigner belongs to
     *   <li>fileId - search documents that are filed in the file with this fileId
     *   <li>fileName - search for documents filed in a file with this name. English, French and
     *       German fields are taken into account.
     *   <li>fileCode - search documents that are filed in the file with this fileCode
     *   <li>fileSpecificCode - search documents that are filed in the file where this specific file
     *       code satisfies the condition
     *   <li>chefDeFile - search documents that are filed in the file where the 'chef de file'
     *       satisfies the condition
     *   <li>filingDate - search documents that are filed in a file on a this date
     *   <li>link - search documents that have at least one link of this enum type Valid values are
     *       RESPONSE, REQUEST, GENERAL and DUPLICATE. These are enums (and not strings) and don't
     *       have to be surrounded with single quotes Examples: link = REQUEST link != GENERAL
     *   <li>linkTarget - search documents that have at least one link pointing to the specified
     *       document id. By including this search criteria, only documents with links will be taken
     *       into account when searching
     *   <li>securityClassification - search documents based on their security classification Valid
     *       values are NORMAL, LIMITED and EU_RESTRAINED. Example: securityClassification = LIMITED
     *   <li>marker - search documents that have a specific marker Valid values are enum values from
     *       MarkerType. Example: marker = PERSONAL
     *   <li>personConcernedEcasId - ECAS id of the person specified in the marking as person
     *       concerned.
     *   <li>personConcernedName - full name of the person specified in the marking as person
     *       concerned.
     *   <li>personConcernedOrganization - organization of the person specified in the marking as
     *       person concerned.
     *   <li>procedureName - search documents based on the name of their linked procedure. Both the
     *       English and the French names are take into account.
     * </ul>
     *
     * The following fields are deprecated:
     *
     * <ul>
     *   <li>assigneeOrganizationCode - use assigneeOrganization instead
     *   <li>assignerOrganizationCode - use assignerOrganization instead
     *   <li>createdOnBehalfOf - use createdOnBehalfOfEcasId instead
     *   <li>saverEcasId - use creatorEcasId instead
     *   <li>senderInternalPersonName - use senderName and senderIsInternal instead
     *   <li>senderInternalOrganizationCode - use senderOrganization and senderIsInternal instead
     *   <li>senderIsInternalOrganization - use senderIsOrganization and senderIsInternal instead
     *   <li>senderExternalPersonName - similar to above
     *   <li>senderExternalOrganizationName - similar to above
     *   <li>senderIsExternalOrganization - similar to above
     *   <li>recipientInternalPersonName - similar to above
     *   <li>recipientInternalOrganizationCode - similar to above
     *   <li>recipientIsInternalOrganization - similar to above
     *   <li>recipientExternalPersonName - similar to above
     *   <li>recipientExternalOrganizationName - similar to above
     *   <li>recipientIsExternalOrganization - similar to above
     *   <li>linkType - use link instead
     *   <li>securityClassificationType - use securityClassification instead
     * </ul>
     *
     * <p>Sample search expressions are:
     *
     * <ul style="list-style-type:none">
     *   <li>title CONTAINS 'Report'
     *   <li>registrationNumber = 'Ares(2008)2301'
     *   <li>documentDate BETWEEN (2008-12-23, 2009-01-14)
     *   <li>comments startsWith 'Updated'
     * </ul>
     *
     * <p>The fulltext search expression does not have any fields but always requires the word
     * <tt>FULLTEXT</tt>. Sample fulltext search expressions are:
     *
     * <ul style="list-style-type:none">
     *   <li>FULLTEXT('test')
     *   <li>FULLTEXT('"This is a test"')
     * </ul>
     *
     * <p>Sorting is only allowed if no fulltext expression is provided. In case a fulltext expression
     * is present, documents are implicitely sorted by matching relevance. If no fulltext expression
     * has been provided, the following fields can be used for sorting:
     *
     * <ul>
     *   <li>documentId
     *   <li>title
     *   <li>registrationNumber
     *   <li>saveNumber
     *   <li>registrationDate
     *   <li>documentDate
     *   <li>encodingDate
     *   <li>modificationDate
     *   <li>comments
     *   <li>securityClassification
     *   <li>itemName
     * </ul>
     */
    public eu.cec.digit.circabc.repo.hrs.ws.DocumentSearchResponse searchDocumentsByExpression(
            eu.cec.digit.circabc.repo.hrs.ws.Header header,
            eu.cec.digit.circabc.repo.hrs.ws.DocumentSearchByExpressionRequest request,
            eu.cec.digit.circabc.repo.hrs.ws.DocumentRetrievalOptions documentRetrievalOptions,
            eu.cec.digit.circabc.repo.hrs.ws.SortOptionsSortBy[] sortOptions)
            throws java.rmi.RemoteException;

    /**
     * Creates a link between 2 documents. The link is two-way (meaning if A is linked to B, then B is
     * also linked to A). Allowed link types for creation are: GENERAL, DUPLICATE and RESPONSE. In
     * case of the RESPONSE type, the reverse link will automatically have the REQUEST type assigned.
     *
     * <p>Any two documents can be linked together, unless a link of the same type already exists, in
     * which case a failure will be thrown.
     *
     * <p>The user needs to have permissions to "see" the documents (i.e. to be able to find them
     * using a search) in order to be able to create links.
     */
    public boolean linkDocuments(
            eu.cec.digit.circabc.repo.hrs.ws.Header header,
            eu.cec.digit.circabc.repo.hrs.ws.LinkDocumentsRequest request)
            throws java.rmi.RemoteException;

    /**
     * Removes a link of the specified type between 2 documents. If no such link exists, the operation
     * will return false, otherwise it will return true. After the successfull execution, neither A
     * will be linked to B, nor B will be linked to A with o link of the specified type.
     *
     * <p>In the case of the RESPONSE/REQUEST link type, the directionality of the link doesn't
     * matter, meaning that if the link A -> B is of type RESPONSE, but when removing the parameters
     * are (A, B, REQUEST) the link will still be removed.
     *
     * <p>The user needs to have permissions to "see" the documents (i.e. to be able to find them
     * using a search) in order to be able to remove links.
     */
    public boolean unlinkDocuments(
            eu.cec.digit.circabc.repo.hrs.ws.Header header,
            eu.cec.digit.circabc.repo.hrs.ws.UnlinkDocumentsRequest request)
            throws java.rmi.RemoteException;

    /**
     * Searches the repository for registered documents that might be a potential duplicate of a
     * document that has to be registered. This operation must be called prior to <i>
     * registerDocument</i>. For ease of use, it accepts the same input data as a registration
     * request.<br>
     * Currently following data is used when searching for duplicates:
     *
     * <ul>
     *   <li>title
     *   <li>document date
     *   <li>mail type
     *   <li>senders / recipients (depending on the mail type)
     * </ul>
     *
     * The purpose of this operation is to prevent double registrations.
     */
    public eu.cec.digit.circabc.repo.hrs.ws
            .FindPotentialRegisteredDuplicatesResponsePotentialDuplicateRegisteredDocumentsPotentialDuplicateRegisteredDocumentInfo
            [] findPotentialRegisteredDuplicates(
            eu.cec.digit.circabc.repo.hrs.ws.Header header,
            eu.cec.digit.circabc.repo.hrs.ws.DocumentRegistrationRequest request)
            throws java.rmi.RemoteException;

    /**
     * Searches the repository for registered documents that might be a potential duplicate of a
     * document that has to be registered. This operation must be called prior to <i>
     * registerDocumentById</i>. The document ID should correspond to a non-registered document.<br>
     * The purpose of this operation is to prevent double registrations.
     */
    public eu.cec.digit.circabc.repo.hrs.ws
            .FindPotentialRegisteredDuplicatesResponsePotentialDuplicateRegisteredDocumentsPotentialDuplicateRegisteredDocumentInfo
            [] findPotentialRegisteredDuplicatesById(
            eu.cec.digit.circabc.repo.hrs.ws.Header header, java.lang.String documentId)
            throws java.rmi.RemoteException;

    /**
     * Encrypts the content of all the items of a document. In order to encrypt items the user should
     * have a specific role that allows him/her to perform this operation.
     */
    public boolean encryptItems(
            eu.cec.digit.circabc.repo.hrs.ws.Header header,
            java.lang.String documentId,
            java.util.Date deadline)
            throws java.rmi.RemoteException;

    /**
     * Decrypts the content of all the items of a document. In order to decrypt items the user should
     * have a specific role that allows him/her to perform this operation.
     */
    public boolean decryptItems(
            eu.cec.digit.circabc.repo.hrs.ws.Header header, java.lang.String documentId)
            throws java.rmi.RemoteException;

    /**
     * Find received document notifications for the current user. A document is received at the moment
     * of registration if the user is one of the recipients, or if modify special is performed on a
     * registered document and the user is added to the list of recipients. Document notifications are
     * ordered descending by the document's registration date. The number of results is limited to
     * 300.
     */
    public eu.cec.digit.circabc.repo.hrs.ws.DocumentNotificationsResponse findDocumentNotifications(
            eu.cec.digit.circabc.repo.hrs.ws.Header header,
            eu.cec.digit.circabc.repo.hrs.ws.DocumentRetrievalOptions documentRetrievalOptions)
            throws java.rmi.RemoteException;

    /** Delete one or more document notifications, specified by their notification id. */
    public boolean deleteDocumentNotifications(
            eu.cec.digit.circabc.repo.hrs.ws.Header header, java.lang.String[] notificationId)
            throws java.rmi.RemoteException;
}
