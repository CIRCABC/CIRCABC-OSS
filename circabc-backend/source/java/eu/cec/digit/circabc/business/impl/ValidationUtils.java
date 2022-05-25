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
package eu.cec.digit.circabc.business.impl;

import eu.cec.digit.circabc.business.api.BusinessStackError;
import eu.cec.digit.circabc.business.api.user.UserDetails;
import eu.cec.digit.circabc.business.helper.ValidationManager;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;

/**
 * Util methods that ecapsulate the boring work for validation.
 * <p>
 * All method follow this logic: 1.	instanciate BusinessValidationStackError 2.	call one or more
 * validation (accordind the method specification) 3.	throw BusinessValidationStackError if at least
 * one error have been found.
 *
 * <p>
 * <b>
 * No permission check are performed, since it is the responsability of the service methods access
 * controle definition
 * </b>
 * </p>
 *
 * @author Yanick Pignot
 */
public abstract class ValidationUtils {

    private ValidationUtils() {
    }

    /**
     * Assert if the node ref is a valid nodeRef and if user has read access on it.
     */
    public static void assertNodeRef(final NodeRef nodeRef, final ValidationManager validator,
                                     final Log logger) {
        final BusinessStackError stack = new BusinessStackError();
        validator.validateNodeRef(nodeRef, stack);
        validator.validateCanRead(nodeRef, stack);
        stack.finish(logger);
    }

    /**
     * Assert if the node ref is a valid document and if user has read access on it.
     * <p>
     * if forUpdateProps == true: -	user must have 'edit props' permission -	the noderef must be ready
     * to be udpated (not locked ...)
     * </p>
     * <p>
     * if forUpdateContent == true: -	user must have 'write content' permission -	the noderef must be
     * ready to be udpated (not locked ...)
     * </p>
     */
    public static void assertDocument(final NodeRef nodeRef, final ValidationManager validator,
                                      final boolean forUpdateProps, final boolean forUpdateContent, final Log logger) {
        final BusinessStackError stack = new BusinessStackError();
        validator.validateNodeRef(nodeRef, stack);

        if (!forUpdateContent && !forUpdateProps) {
            validator.validateContent(nodeRef, stack);
        } else {
            validator.validateUpdatableContent(nodeRef, stack);
        }
        stack.finish(logger);
    }

    /**
     * Assert if the node ref is a valid space and if user has read access on it.
     */
    public static void assertSpace(final NodeRef nodeRef, final ValidationManager validator,
                                   final Log logger) {
        final BusinessStackError stack = new BusinessStackError();
        validator.validateNodeRef(nodeRef, stack);
        validator.validateSpace(nodeRef, stack);
        stack.finish(logger);
    }

    /**
     * Assert if the node ref is a valid space in the library and if user has read access on it.
     */
    public static void assertLibrarySpace(final NodeRef nodeRef, final ValidationManager validator,
                                          final Log logger) {
        final BusinessStackError stack = new BusinessStackError();
        validator.validateNodeRef(nodeRef, stack);
        validator.validateSpace(nodeRef, stack);
        validator.validateLibraryChild(nodeRef, stack);
        stack.finish(logger);
    }

    /**
     * Assert if the node ref is a valid locked document and if user has read access on it.
     */
    public static void assertLockedDocument(final NodeRef nodeRef, final ValidationManager validator,
                                            final Log logger) {
        final BusinessStackError stack = new BusinessStackError();
        validator.validateNodeRef(nodeRef, stack);
        validator.validateLockedContent(nodeRef, stack);
        stack.finish(logger);
    }

    /**
     * Assert if the node ref is a valid working copy document and if user has read access on it.
     */
    public static void assertWorkingCopyDocument(final NodeRef nodeRef,
                                                 final ValidationManager validator, final Log logger) {
        final BusinessStackError stack = new BusinessStackError();
        validator.validateNodeRef(nodeRef, stack);
        validator.validateWorkingCopyContent(nodeRef, stack);
        stack.finish(logger);
    }

    /**
     * Assert if the user details before update. Validate: username (not empty and unique), email (not
     * empty, unique and well formated), first name (not empty), last name (not empty)
     */
    public static void assertUserDetails(final UserDetails userDetails,
                                         final ValidationManager validator, final Log logger) {
        final String previousEmail = (String) userDetails.getOriginalProperties()
                .get(ContentModel.PROP_EMAIL);
        final String previousUserName = (String) userDetails.getOriginalProperties()
                .get(ContentModel.PROP_USERNAME);

        final BusinessStackError stack = new BusinessStackError();
        validator.validateUserName(previousUserName, userDetails.getUserName(), stack);
        validator.validateFirstName(userDetails.getFirstName(), stack);
        validator.validateLastName(userDetails.getLastName(), stack);
        stack.finish(logger);
    }
}
