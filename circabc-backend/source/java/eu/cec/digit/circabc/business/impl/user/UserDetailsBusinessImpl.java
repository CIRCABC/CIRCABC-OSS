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
package eu.cec.digit.circabc.business.impl.user;

import eu.cec.digit.circabc.business.api.BusinessRuntimeExpection;
import eu.cec.digit.circabc.business.api.BusinessStackError;
import eu.cec.digit.circabc.business.api.BusinessValidationError;
import eu.cec.digit.circabc.business.api.user.RemoteUserBusinessSrv;
import eu.cec.digit.circabc.business.api.user.UserDetails;
import eu.cec.digit.circabc.business.api.user.UserDetailsBusinessSrv;
import eu.cec.digit.circabc.business.helper.ContentManager;
import eu.cec.digit.circabc.business.helper.UserManager;
import eu.cec.digit.circabc.business.helper.ValidationManager;
import eu.cec.digit.circabc.business.impl.AssertUtils;
import eu.cec.digit.circabc.business.impl.ValidationUtils;
import eu.cec.digit.circabc.business.impl.content.ContentBusinessImpl;
import eu.cec.digit.circabc.repo.customisation.CustomizationException;
import eu.cec.digit.circabc.service.cmr.security.CircabcConstant;
import eu.cec.digit.circabc.service.customisation.NodePreferencesService;
import eu.cec.digit.circabc.service.struct.ManagementService;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Business service to manage user details (properties and preferences).
 *
 * @author Yanick Pignot
 */
public class UserDetailsBusinessImpl implements UserDetailsBusinessSrv {

    private static final String MSG_NOT_EXISTS_USER = "business_validation_not_a_existing_user";
    private static final String MSG_NO_USERNAME = "business_validation_no_username";
    private static final String MSG_CANNOT_LOAD = "business_error_cannot_load_user_details";
    private static final String AVATAR_CONFIG_ROOT = "users";
    private static final String AVATAR_CONFIG_TYPE = "preferences";
    private static final String AVATAR_CONFIG_ELEMENT = "avatar";
    private final Log logger = LogFactory.getLog(ContentBusinessImpl.class);
    private NodeRef defaultAvatar;

    private PermissionService permissionService;
    private NodeService nodeService;
    private NodePreferencesService nodePreferencesService;
    private ManagementService managementService;
    private TransactionService transactionService;

    private RemoteUserBusinessSrv remoteUserBusinessSrv;
    private UserManager userManager;
    private ContentManager contentManager;
    private ValidationManager validationManager;

    //--------------
    //-- public methods

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.user.UserDetailsBusinessSrv#getMyDetails()
     */
    public UserDetails getMyDetails() {
        return getUserDetails(AuthenticationUtil.getFullyAuthenticatedUser());
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.user.UserDetailsBusinessSrv#getUserDetails(java.lang.String)
     */
    public UserDetails getUserDetails(String userName) {
        final BusinessStackError stack = new BusinessStackError();

        boolean userInRepository = false;
        if (userName == null || userName.length() < 1) {
            stack.append(new BusinessValidationError(MSG_NO_USERNAME));
        } else if ((userInRepository = userManager.personExists(userName)) == false
                &&
                (remoteUserBusinessSrv.isRemoteManagementAvailable() == false
                        || remoteUserBusinessSrv.userExists(userName) == false)) {
            stack.append(new BusinessValidationError(MSG_NOT_EXISTS_USER));
        }

        stack.finish(logger);

        if (userInRepository) {
            return getUserDetails(userManager.getPerson(userName));
        } else {
            try {
                final UserDetails details = new UserDetailsImpl(userName, this);

                remoteUserBusinessSrv.reloadDetails(details);

                return details;
            } catch (Throwable t) {
                throw new BusinessRuntimeExpection(MSG_CANNOT_LOAD, t, userName);
            }
        }
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.user.UserDetailsBusinessSrv#getUserDetails(org.alfresco.service.cmr.repository.NodeRef)
     */
    public UserDetails getUserDetails(NodeRef person) {
        final BusinessStackError stack = new BusinessStackError();

        validationManager.validateNodeRef(person, stack);
        validationManager.validateCanRead(person, stack);
        validationManager.validatePerson(person, stack);

        stack.finish(logger);

        return new UserDetailsImpl(person, this);
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.user.UserDetailsBusinessSrv#updateAvatar(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef)
     */
    public void updateAvatar(final NodeRef person, final NodeRef imageRef) {
        final BusinessStackError stack = new BusinessStackError();
        validationManager.validateNodeRef(person, stack);
        validationManager.validatePerson(person, stack);
        validationManager.validateNodeRef(person, stack);
        validationManager.validateContent(imageRef, stack);
        stack.finish(logger);

        updateAvatar(person, (String) nodeService.getProperty(imageRef, ContentModel.PROP_NAME), null,
                imageRef);
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.user.UserDetailsBusinessSrv#updateAvatar(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, java.io.File)
     */
    public NodeRef updateAvatar(final NodeRef person, final String avatarFileName,
                                final File avatarFile) {
        final BusinessStackError stack = new BusinessStackError();
        validationManager.validateNodeRef(person, stack);
        validationManager.validatePerson(person, stack);
        validationManager.validateTitle(avatarFileName, stack);
        stack.finish(logger);

        AssertUtils.canAccess(avatarFile);

        return updateAvatar(person, avatarFileName, avatarFile, null);
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.user.UserDetailsBusinessSrv#removeAvatar(org.alfresco.service.cmr.repository.NodeRef)
     */
    public void removeAvatar(final NodeRef person) {
        // remove old image child node if we already have one
        final List<ChildAssociationRef> prefAssocs = getImagePrefAssociations(person);
        removeChildAssociations(person, prefAssocs);

        // remove avatar associations
        final List<AssociationRef> avatarAssocs = getAvatarAssociations(person);
        removeAssociations(person, avatarAssocs);

    }


    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.user.UserDetailsBusinessSrv#getAvatar(org.alfresco.service.cmr.repository.NodeRef)
     */
    public NodeRef getAvatar(final NodeRef person) {
        final BusinessStackError stack = new BusinessStackError();
        validationManager.validateNodeRef(person, stack);
        stack.finish(logger);

        final List<AssociationRef> avatarAssocs = getAvatarAssociations(person);

        if (avatarAssocs.size() < 1) {
            return getDefaultAvatar();
        } else {
            return avatarAssocs.get(0).getTargetRef();
        }
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.user.UserDetailsBusinessSrv#getDefaultAvatar()
     */
    public NodeRef getDefaultAvatar() {
        if (defaultAvatar == null) {
            final NodeRef rootRef = managementService.getCircabcNodeRef();
            try {
                defaultAvatar = nodePreferencesService
                        .getDefaultConfigurationFile(rootRef, AVATAR_CONFIG_ROOT, AVATAR_CONFIG_TYPE,
                                AVATAR_CONFIG_ELEMENT);
            } catch (CustomizationException e) {
                if (logger.isErrorEnabled()) {
                    logger.warn("Error getting the default avatar: " + e.getMessage(), e);
                }
            }

            if (defaultAvatar == null && logger.isWarnEnabled()) {
                logger.warn(
                        "Impossible to found the default avatar. Check the presence of a file in the location: $circabc dictionary folder$/"
                                + AVATAR_CONFIG_ROOT + "/" + AVATAR_CONFIG_TYPE + "/" + AVATAR_CONFIG_ELEMENT);
            }
        }

        return defaultAvatar;
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.user.UserDetailsBusinessSrv#updateUserDetails(org.alfresco.service.cmr.repository.NodeRef, eu.cec.digit.circabc.business.api.user.UserDetails)
     */
    public void updateUserDetails(final NodeRef personRef, final UserDetails userDetails) {
        AssertUtils.notNull(userDetails);
        ValidationUtils.assertUserDetails(userDetails, validationManager, logger);

        if (userDetails.getNodeRef().equals(personRef) == false) {
            throw new IllegalArgumentException("User details node reference (" + userDetails.getNodeRef()
                    + ") must be equals to the person reference (" + personRef + ")");
        }

        final Map<QName, Serializable> propertiesToUpdate = userDetails.getUpdatedProperties();
        final Map<QName, Serializable> preferencesToUpdate = userDetails.getUpdatedPreferences();

        if (propertiesToUpdate != null && propertiesToUpdate.size() > 0) {
            nodeService.addProperties(userDetails.getNodeRef(), propertiesToUpdate);

            if (logger.isDebugEnabled()) {
                logger.debug(propertiesToUpdate.size() + " properties setted for user: " + userDetails
                        .getUserName());
            }
        } else if (logger.isDebugEnabled()) {
            logger.debug("No properties to update for user: " + userDetails.getUserName());
        }

        if (preferencesToUpdate != null && preferencesToUpdate.size() > 0) {
            final NodeRef prefRef = userManager.createUserPreferencesRef(userDetails.getNodeRef());

            nodeService.addProperties(prefRef, preferencesToUpdate);

            if (logger.isDebugEnabled()) {
                logger.debug(preferencesToUpdate.size() + " preferences setted for user: " + userDetails
                        .getUserName());
            }
        } else if (logger.isDebugEnabled()) {
            logger.debug("No preferences to update for user: " + userDetails.getUserName());
        }
    }

    //--------------
    //-- package helpers for UserDetails bean lazy loading

    /**
     * Return the preferenences of a person in a separate RO transaction (used for UserDEtails lazy
     * loading)
     */
    /*package*/ Map<QName, Serializable> getUserPreferences(final NodeRef person) {

        if (person == null) {
            return new HashMap<>();
        } else {
            final RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();

            return txnHelper.doInTransaction
                    (
                            new RetryingTransactionCallback<Map<QName, Serializable>>() {
                                public Map<QName, Serializable> execute() throws Throwable {
                                    return userManager.getUserPreferences(person);
                                }

                            },
                            true, true);
        }
    }

    /**
     * Return the properties of a person in a separate RO transaction (used for UserDEtails lazy
     * loading)
     */
    /*package*/ Map<QName, Serializable> getUserProperties(final NodeRef person) {
        if (person == null) {
            return new HashMap<>();
        } else {
            final RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();

            return txnHelper.doInTransaction
                    (
                            new RetryingTransactionCallback<Map<QName, Serializable>>() {
                                public Map<QName, Serializable> execute() throws Throwable {
                                    return userManager.getUserProperties(person);
                                }

                            }
                    );
        }
    }

    /**
     * Return the avatar of a person in a separate RO transaction (used for UserDEtails lazy loading)
     */
    /*package*/ NodeRef getUserAvatar(final NodeRef person) {
        final RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();

        return txnHelper.doInTransaction
                (
                        new RetryingTransactionCallback<NodeRef>() {
                            public NodeRef execute() throws Throwable {
                                if (person == null) {
                                    return getDefaultAvatar();
                                } else {
                                    return getAvatar(person);
                                }
                            }

                        }
                );
    }

    //--------------
    //-- private helpers

    private NodeRef updateAvatar(final NodeRef person, final String avatarFileName,
                                 final File avatarFile, final NodeRef imageRef) {
        // ensure cm:person has 'cm:preferences' aspect applied - as we want to add the avatar as
        // the child node of the 'cm:preferenceImage' association
        if (nodeService.hasAspect(person, ContentModel.ASPECT_PREFERENCES) == false) {
            nodeService.addAspect(person, ContentModel.ASPECT_PREFERENCES, null);
        }

        // remove old image child node if we already have one
        final List<ChildAssociationRef> prefAssocs = getImagePrefAssociations(person);
        removeChildAssociations(person, prefAssocs);

        // wire up 'cm:avatar' target association - backward compatible with JSF web-client avatar
        // and allow to set an avatar from the library or user home.
        final List<AssociationRef> avatarAssocs = getAvatarAssociations(person);
        removeAssociations(person, avatarAssocs);

        final NodeRef imageToLink;
        if (imageRef == null) {
            imageToLink = contentManager
                    .createContent(person, avatarFileName, ContentModel.ASSOC_PREFERENCE_IMAGE,
                            ContentModel.TYPE_CONTENT, avatarFile, false);
        } else {
            imageToLink = imageRef;
        }

        permissionService
                .setPermission(imageToLink, CircabcConstant.GUEST_AUTHORITY, PermissionService.READ, true);

        nodeService.createAssociation(person, imageToLink, ContentModel.ASSOC_AVATAR);

        return imageToLink;
    }

    private List<AssociationRef> getAvatarAssociations(final NodeRef person) {
        final List<AssociationRef> targetAssoc = nodeService
                .getTargetAssocs(person, ContentModel.ASSOC_AVATAR);

        if (logger.isWarnEnabled() && targetAssoc != null && targetAssoc.size() > 1) {
            logger.warn("Business model inconsistency: To many avatars found (" + targetAssoc.size()
                    + ") for user " + nodeService.getProperty(person, ContentModel.PROP_USERNAME));
        }

        return targetAssoc;
    }

    private List<ChildAssociationRef> getImagePrefAssociations(final NodeRef person) {
        final List<ChildAssociationRef> childAssocs = nodeService
                .getChildAssocs(person, ContentModel.ASSOC_PREFERENCE_IMAGE, RegexQNamePattern.MATCH_ALL);

        if (logger.isWarnEnabled() && childAssocs != null && childAssocs.size() > 1) {
            logger.warn(
                    "Business model inconsistency: To many prefered images found (" + childAssocs.size()
                            + ") for user " + nodeService.getProperty(person, ContentModel.PROP_USERNAME));
        }
        return childAssocs;
    }

    private void removeChildAssociations(final NodeRef person,
                                         final List<ChildAssociationRef> assocs) {
        if (assocs != null) {
            for (final ChildAssociationRef assoc : assocs) {
                nodeService.deleteNode(assoc.getChildRef());
            }
        }
    }

    private void removeAssociations(final NodeRef person, final List<AssociationRef> assocs) {
        if (assocs != null) {
            for (final AssociationRef assoc : assocs) {
                nodeService.removeAssociation(person, assoc.getTargetRef(), assoc.getTypeQName());
            }
        }
    }

    //--------------
    //-- IOC

    /**
     * @param validationManager the validationManager to set
     */
    public final void setValidationManager(final ValidationManager validationManager) {
        this.validationManager = validationManager;
    }

    /**
     * @param nodeService the nodeService to set
     */
    public final void setNodeService(final NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /**
     * @param contentManager the contentManager to set
     */
    public final void setContentManager(ContentManager contentManager) {
        this.contentManager = contentManager;
    }

    /**
     * @param nodePreferencesService the nodePreferencesService to set
     */
    public final void setNodePreferencesService(NodePreferencesService nodePreferencesService) {
        this.nodePreferencesService = nodePreferencesService;
    }

    /**
     * @param managementService the managementService to set
     */
    public final void setManagementService(ManagementService managementService) {
        this.managementService = managementService;
    }

    /**
     * @param userManager the userManager to set
     */
    public final void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    /**
     * @param transactionService the transactionService to set
     */
    public final void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * @param permissionService
     */
    public final void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    public final void setRemoteUserBusinessSrv(RemoteUserBusinessSrv remoteUserBusinessSrv) {
        this.remoteUserBusinessSrv = remoteUserBusinessSrv;
    }
}
