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
package eu.cec.digit.circabc.business.helper;

import eu.cec.digit.circabc.business.api.BusinessStackError;
import eu.cec.digit.circabc.business.api.BusinessValidationError;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

import javax.mail.internet.AddressException;
import java.util.Calendar;
import java.util.Date;

/**
 * Business helper that manage fileds validation.
 *
 * @author Yanick Pignot
 */
public class ValidationManager {

    private static final String MSG_UNEXISTING_REF = "business_validation_unexisting_ref";
    private static final String MSG_NORIGHT_REF = "business_validation_noright_ref";
    private static final String MSG_NO_TITLE = "business_validation_no_title";
    private static final String MSG_NO_NAME = "business_validation_no_name";
    private static final String MSG_INVALID_NAME = "business_validation_invalid_name";
    private static final String MSG_NO_FIRSTNAME = "business_validation_no_firstname";
    private static final String MSG_NO_LASTNAME = "business_validation_no_lastname";
    private static final String MSG_NO_EMAIL = "business_validation_no_email";
    private static final String MSG_DUPLICATE_EMAIL = "business_validation_duplicate_email";
    private static final String MSG_INVALID_EMAIL = "business_validation_invalid_email";
    private static final String MSG_EXPIRATION = "business_validation_expiration_before_today";
    private static final String MSG_NOT_CONTENT = "business_validation_not_a_content";
    private static final String MSG_NOT_SPACE = "business_validation_not_a_space";
    private static final String MSG_NOT_SHARED_SPACE = "business_validation_not_a_shared_space";
    private static final String MSG_NOT_LOCKED = "business_validation_not_a_locked_content";
    private static final String MSG_NOT_WORKING_COPY = "business_validation_not_a_working_copy";
    private static final String MSG_NOT_IN_LIBRARY = "business_validation_not_in_library";
    private static final String MSG_NOT_A_IG = "business_validation_not_an_interestgroup";
    private static final String MSG_NOT_PERSON = "business_validation_not_a_person";
    private static final String MSG_GUEST = "business_validation_guest";
    private static final String MSG_NO_USERNAME = "business_validation_no_username";
    private static final String MSG_DUPLICATE_USERNAME = "business_validation_duplicate_username";
    private static final String MSG_NOT_IN_INFORMATION = "business_validation_not_in_information";
    private static final String MSG_NOT_IN_DIRECTROY = "business_validation_not_in_directory";
    private static final String MSG_NOT_IN_NEWSGROUP = "business_validation_not_in_newsgroup";
    private static final String MSG_NOT_IN_CALENDAR = "business_validation_not_in_calendar";
    private static final String MSG_NOT_IN_SURVEYS = "business_validation_not_in_survey";

    private static final String MSG_LOCKED = "business_validation_a_locked";
    private static final String MSG_WORKING_COPY = "business_validation_a_working_copy";

    private NodeService nodeService;

    private UserManager userManager;
    private MetadataManager metadataManager;
    private NodeTypeManager nodeTypeManager;
    private PermissionManager permissionManager;

    //--------------
    //-- public methods

    /**
     * Validate if the given date is valid to be setted on a node as Expiration property
     *
     * @param expirationdate The date to check, can be null but not earlier than today.
     * @see eu.cec.digit.circabc.model.DocumentModel#PROP_EXPIRATION_DATE
     */
    public void validateExpirationDate(final Date expirationdate, final BusinessStackError stack) {
        if (expirationdate != null) {
            if (expirationdate.before(getToday().getTime())) {
                append(stack, MSG_EXPIRATION);
            }
        }
    }

    /**
     * Validate if the given string is valid to be setted on a node as Title property
     *
     * @param title The title to test, can't be null and can't be empty
     * @see org.alfresco.model.ContentModel#PROP_TITLE
     */
    public void validateTitle(final String title, final BusinessStackError stack) {
        validateHasText(title, stack, MSG_NO_TITLE);
    }

    /**
     * Validate if the given string is valid to be setted on a person as lastname property
     *
     * @param previousUserName The previous setted userName to check unicity
     * @param newUserName      The userName to test, can't be null, can't be empty, can't be already in use
     *                         (if new)
     * @see org.alfresco.model.ContentModel#PROP_USERNAME
     */
    public void validateUserName(final String previousUserName, final String newUserName,
                                 final BusinessStackError stack) {
        final boolean valid = validateHasText(newUserName, stack, MSG_NO_USERNAME);
        if (valid) {
            if (previousUserName == null || !previousUserName.equals(newUserName)) {
                if (getUserManager().personExists(newUserName)) {
                    append(stack, MSG_DUPLICATE_USERNAME);
                }
            }
        }
        //else{} don't continue validation
    }

    /**
     * Validate if the given string is valid to be setted on a person as firstName property
     *
     * @param firstName The firstName to test, can't be null and can't be empty
     * @see org.alfresco.model.ContentModel#PROP_FIRSTNAME
     */
    public void validateFirstName(final String firstName, final BusinessStackError stack) {
        validateHasText(firstName, stack, MSG_NO_FIRSTNAME);
    }

    /**
     * Validate if the given string is valid to be setted on a person as lastname property
     *
     * @param lastName The lastname to test, can't be null and can't be empty
     * @see org.alfresco.model.ContentModel#PROP_LASTNAME
     */
    public void validateLastName(final String lastName, final BusinessStackError stack) {
        validateHasText(lastName, stack, MSG_NO_LASTNAME);
    }

    /**
     * Validate if the given string is valid to be setted on a person as email property
     *
     * @param previousEmail The previous setted email to check unicity
     * @param newEmail      The email to test, can't be null, can't be empty, can't be already in use (if
     *                      new) and must be a valid Email
     * @see org.alfresco.model.ContentModel#PROP_EMAIL
     */
    public void validateEmail(final String previousEmail, final String newEmail,
                              final BusinessStackError stack) {
        final boolean valid = validateHasText(newEmail, stack, MSG_NO_EMAIL);

        if (valid) {
            if (previousEmail == null || previousEmail.equals(newEmail) == false) {
                try {
                    new javax.mail.internet.InternetAddress(newEmail.trim()).validate();
                } catch (final AddressException e) {
                    append(stack, MSG_INVALID_EMAIL);
                }

                if (getUserManager().isEmailExists(newEmail)) {
                    append(stack, MSG_DUPLICATE_EMAIL);
                }
            }
        }
        //else{} don't continue validation
    }

    /**
     * Validate any kind of node reference. It must be not null and exist.
     */
    public void validateNodeRef(final NodeRef nodeRef, final BusinessStackError stack) {
        validateNotNull(nodeRef, stack);
        if (getNodeService().exists(nodeRef) == false) {
            append(stack, MSG_UNEXISTING_REF);
        }
    }

    /**
     * Validate if the user have read permission on the given not null node reference.
     */
    public void validateCanRead(final NodeRef nodeRef, final BusinessStackError stack) {
        validateNotNull(nodeRef, stack);
        if (getPermissionManager().canRead(nodeRef) == false) {
            append(stack, MSG_NORIGHT_REF);
        }
    }

    /**
     * Validate if the user have edit properties permission on the given not null node reference.
     */
    public void validateCanEditProperties(final NodeRef nodeRef, final BusinessStackError stack) {
        validateNotNull(nodeRef, stack);
        if (getPermissionManager().canEditProperties(nodeRef) == false) {
            append(stack, MSG_NORIGHT_REF);
        }
    }

    /**
     * Validate if the user have edit content permission on the given not null node reference.
     */
    public void validateCanUpdateContent(final NodeRef nodeRef, final BusinessStackError stack) {
        validateNotNull(nodeRef, stack);
        if (getPermissionManager().canEditContent(nodeRef) == false) {
            append(stack, MSG_NORIGHT_REF);
        }
    }

    /**
     * Validate if the user have delete on the given not null node reference.
     */
    public void validateCanDelete(final NodeRef nodeRef, final BusinessStackError stack) {
        validateNotNull(nodeRef, stack);
        if (getPermissionManager().canDelete(nodeRef) == false) {
            append(stack, MSG_NORIGHT_REF);
        }
    }

    /**
     * Validate if the user have add child permission on the given not null node reference.
     */
    public void validateCanAddChild(final NodeRef nodeRef, final BusinessStackError stack) {
        validateNotNull(nodeRef, stack);
        if (getPermissionManager().canAddChild(nodeRef) == false) {
            append(stack, MSG_NORIGHT_REF);
        }
    }

    /**
     * Validate if the given node is not null and is a space
     */
    public void validateSpace(final NodeRef nodeRef, final BusinessStackError stack) {
        validateNotNull(nodeRef, stack);
        if (getNodeTypeManager().isFolder(nodeRef) == false) {
            append(stack, MSG_NOT_SPACE);
        }
    }

    /**
     * Validate if the given node is not null and is a person
     */
    public void validatePerson(final NodeRef nodeRef, final BusinessStackError stack) {
        validateNotNull(nodeRef, stack);
        if (getNodeTypeManager().isPerson(nodeRef) == false) {
            append(stack, MSG_NOT_PERSON);
        }
    }


    /**
     * Validate if the given node is not null and is a person
     */
    public void validateNotGuest(final String userName, final BusinessStackError stack) {
        validateUserName(userName, userName, stack);
        if (userName != null && getUserManager().isGuest(userName)) {
            append(stack, MSG_GUEST);
        }
    }

    /**
     * Validate if the given node is not null and is a content
     */
    public void validateContent(final NodeRef nodeRef, final BusinessStackError stack) {
        validateNotNull(nodeRef, stack);
        if (getNodeTypeManager().isContent(nodeRef) == false) {
            append(stack, MSG_NOT_CONTENT);
        }
    }

    /**
     * Validate if the given node is not null, is a content is ready for update.
     */
    public void validateUpdatableContent(final NodeRef nodeRef, final BusinessStackError stack) {
        validateContent(nodeRef, stack);
        if (getNodeTypeManager().isWorkingCopyDocument(nodeRef)) {
            if (getMetadataManager().isLockOwner(nodeRef) == false) {
                append(stack, MSG_WORKING_COPY);
            }
        } else if (getNodeTypeManager().isLockedDocument(nodeRef)) {
            append(stack, MSG_LOCKED);
        }
    }

    /**
     * Validate if the given node is not null, is a space and is shared.
     */
    public void validateSharedSpace(final NodeRef nodeRef, final BusinessStackError stack) {
        validateSpace(nodeRef, stack);
        if (getNodeTypeManager().isSharedSpace(nodeRef) == false) {
            append(stack, MSG_NOT_SHARED_SPACE);
        }
    }

    /**
     * Validate if the given node is not null, is a content and is locked
     */
    public void validateLockedContent(final NodeRef nodeRef, final BusinessStackError stack) {
        validateContent(nodeRef, stack);
        if (getNodeTypeManager().isLockedDocument(nodeRef) == false) {
            append(stack, MSG_NOT_LOCKED);
        }
    }

    /**
     * Validate if the given node is not null, is a content and is a working copy
     */
    public void validateWorkingCopyContent(final NodeRef nodeRef, final BusinessStackError stack) {
        validateContent(nodeRef, stack);
        if (getNodeTypeManager().isWorkingCopyDocument(nodeRef) == false) {
            append(stack, MSG_NOT_WORKING_COPY);
        }
    }

    /**
     * Validate if the given node is not null and is an Interest group root
     */
    public void validateInterstGroup(final NodeRef nodeRef, final BusinessStackError stack) {
        validateNotNull(nodeRef, stack);
        if (getNodeTypeManager().isInterestGroup(nodeRef) == false) {
            append(stack, MSG_NOT_A_IG);
        }
    }

    /**
     * Validate if the given node is not null and is a any child of the information interest group
     * service
     */
    public void validateInformationChild(final NodeRef nodeRef, final BusinessStackError stack) {
        validateNotNull(nodeRef, stack);
        if (getNodeTypeManager().isInformationChild(nodeRef) == false) {
            append(stack, MSG_NOT_IN_INFORMATION);
        }
    }

    /**
     * Validate if the given node is not null and is a any child of the library interest group
     * service
     */
    public void validateLibraryChild(final NodeRef nodeRef, final BusinessStackError stack) {
        validateNotNull(nodeRef, stack);
        if (getNodeTypeManager().isLibraryChild(nodeRef) == false) {
            append(stack, MSG_NOT_IN_LIBRARY);
        }
    }

    /**
     * Validate if the given node is not null and is a any child of the directory interest group
     * service
     */
    public void validateDirectoryChild(final NodeRef nodeRef, final BusinessStackError stack) {
        validateNotNull(nodeRef, stack);
        if (getNodeTypeManager().isDirectoryChild(nodeRef) == false) {
            append(stack, MSG_NOT_IN_DIRECTROY);
        }
    }

    /**
     * Validate if the given node is not null and is a any child of the newsgroup interest group
     * service
     */
    public void validateNewsgroupChild(final NodeRef nodeRef, final BusinessStackError stack) {
        validateNotNull(nodeRef, stack);
        if (getNodeTypeManager().isNewsgroupChild(nodeRef) == false) {
            append(stack, MSG_NOT_IN_NEWSGROUP);
        }
    }

    /**
     * Validate if the given node is not null and is a any child of the calendar interest group
     * service
     */
    public void validateCalendarChild(final NodeRef nodeRef, final BusinessStackError stack) {
        validateNotNull(nodeRef, stack);
        if (getNodeTypeManager().isCalendarChild(nodeRef) == false) {
            append(stack, MSG_NOT_IN_CALENDAR);
        }
    }

    /**
     * Validate if the given node is not null and is a any child of the survey interest group service
     */
    public void validateSurveyChild(final NodeRef nodeRef, final BusinessStackError stack) {
        validateNotNull(nodeRef, stack);
        if (getNodeTypeManager().isSurveyChild(nodeRef) == false) {
            append(stack, MSG_NOT_IN_SURVEYS);
        }
    }

    //--------------
    //-- private helpers

    private Calendar getToday() {
        final Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        return today;
    }

    /*
     * @return	if the text is valid.
     */
    private boolean validateHasText(final String mandatoryString, final BusinessStackError stack,
                                    String message) {
        if (mandatoryString == null || mandatoryString.trim().length() < 1) {
            append(stack, message);
            return false;
        } else {
            return true;
        }
    }

    private BusinessStackError append(final BusinessStackError stack, String message) {
        stack.append(new BusinessValidationError(message));
        return stack;
    }

    /*
     * @return	if the object is not null.
     */
    private boolean validateNotNull(final NodeRef nodeRef, final BusinessStackError stack) {
        // if the nodeRef is null, throw now the excpetion beacause no other method call will be success
        if (nodeRef == null) {
            append(stack, MSG_UNEXISTING_REF).finish();
            return false;
        } else {
            return true;
        }
    }

    //--------------
    //-- IOC

    /**
     * @return the nodeService
     */
    protected final NodeService getNodeService() {
        return nodeService;
    }

    /**
     * @param nodeService the nodeService to set
     */
    public final void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /**
     * @return the metadataManager
     */
    protected final MetadataManager getMetadataManager() {
        return metadataManager;
    }

    /**
     * @param metadataManager the metadataManager to set
     */
    public final void setMetadataManager(MetadataManager metadataManager) {
        this.metadataManager = metadataManager;
    }

    /**
     * @return the nodeTypeManager
     */
    protected final NodeTypeManager getNodeTypeManager() {
        return nodeTypeManager;
    }

    /**
     * @param nodeTypeManager the nodeTypeManager to set
     */
    public final void setNodeTypeManager(NodeTypeManager nodeTypeManager) {
        this.nodeTypeManager = nodeTypeManager;
    }

    /**
     * @return the permissionManager
     */
    protected final PermissionManager getPermissionManager() {
        return permissionManager;
    }

    /**
     * @param permissionManager the permissionManager to set
     */
    public final void setPermissionManager(PermissionManager permissionManager) {
        this.permissionManager = permissionManager;
    }

    /**
     * @return the userManager
     */
    protected final UserManager getUserManager() {
        return userManager;
    }

    /**
     * @param userManager the userManager to set
     */
    public final void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    public void validateName(String name, BusinessStackError stack) {

        if (name == null || name.trim().length() < 1) {
            append(stack, MSG_NO_NAME);
        } else {
            if (name.charAt(name.length() - 1) == '.') {
                append(stack, MSG_INVALID_NAME);
            }
        }
    }
}
