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
package eu.cec.digit.circabc.business.api.user;

import org.alfresco.service.cmr.repository.NodeRef;

import java.io.File;

/**
 * Business service to manage user details (properties and preferences).
 *
 * @author Yanick Pignot
 */
public interface UserDetailsBusinessSrv {

    /**
     * Get all details of the given person.
     *
     * @param person The person to query
     * @return A person POJO
     */
    UserDetails getUserDetails(final NodeRef person);

    /**
     * Get all details of the given person identified by its user name.
     *
     * @param username The username to query
     * @return A person POJO
     */
    UserDetails getUserDetails(final String username);

    /**
     * Get all details of the current authenticated user.
     *
     * @return A person POJO
     */
    UserDetails getMyDetails();

    /**
     * Upload an image file and set it as avatar for the given person.
     *
     * @param person         The person to update
     * @param avatarFileName The name of the avatar file
     * @param avatarFile     The avatar to upload
     * @return The created avatar node reference
     */
    NodeRef updateAvatar(final NodeRef person, final String avatarFileName, final File avatarFile);

    /**
     * Update the avatar of a person with an existing repository file.
     *
     * @param person   The person to update
     * @param imageRef The existing repository file
     */
    void updateAvatar(final NodeRef person, final NodeRef imageRef);

    /**
     * Remove the user defined avatar to use the default one.
     *
     * @param person The person to update
     * @return .
     */
    void removeAvatar(final NodeRef person);

    /**
     * Return the avatar of the given person.
     *
     * @param person The person to query
     * @return The configured or default avatar node reference
     */
    NodeRef getAvatar(final NodeRef person);

    /**
     * Return the default avatar.
     *
     * @return The default avatar node reference
     */
    NodeRef getDefaultAvatar();

    /**
     * Update modified user details (prefernces and properties).
     * <b>Warning:</b>
     * <p>
     * This method doen'st support avatar changes. Use updateAvatar and removeAvatar methods of this
     * same class.	 *
     * </p>
     *
     * @param personRef   The person node reference (Only used for security check, MUST be equals to
     *                    userDetails.getNodeRef())
     * @param userDetails The details of the person to update.
     * @see eu.cec.digit.circabc.business.api.user.UserDetailsBusinessSrv#updateAvatar(NodeRef,
     * String, File)
     * @see eu.cec.digit.circabc.business.api.user.UserDetailsBusinessSrv#updateAvatar(NodeRef,
     * NodeRef)
     * @see eu.cec.digit.circabc.business.api.user.UserDetailsBusinessSrv#removeAvatar(NodeRef)
     * <p>
     * TODO remove the first argument (personRef) when a <i>AclAwareWrapper</i> aware Acl voters will
     * be implemented
     */
    void updateUserDetails(final NodeRef personRef, final UserDetails userDetails);
}
