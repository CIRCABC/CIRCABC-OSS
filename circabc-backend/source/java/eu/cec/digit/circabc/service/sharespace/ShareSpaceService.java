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
package eu.cec.digit.circabc.service.sharespace;

import org.alfresco.service.Auditable;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Pair;

import java.util.List;

/**
 * Interface for shared space operations. A shared space is common way for IG Leader to share
 * documents with members of other IG.
 *
 * @author Stephane Clinckart
 * @author Slobodan Filipovic
 * <p>Migration 3.1 -> 3.4.6 - 02/12/2011 Commented the key parameter of the @Auditable
 * annotation. Commented the deprecated @PublicService annotation.
 */
// @PublicService
public interface ShareSpaceService {

    /**
     * @param interestGroup
     * @return
     */
    @Auditable(/*key = Auditable.Key.ARG_0, */ parameters = {"interestGroup"})
    List<NodeRef> getAllSharedSpaceInInterestGroup(final NodeRef interestGroup);

    /**
     * Get
     */
    @Auditable(/*key = Auditable.Key.ARG_0, */ parameters = {"shareSpace"})
    List<NodeRef> getAvailableInterestGroups(final NodeRef shareSpace);

    /**
     * Get available shared spaces that can be linked from space
     *
     * @param space node ref
     * @return List of sharespace items
     */
    @Auditable(/*key = Auditable.Key.ARG_0, */ parameters = {"space"})
    List<NodeRef> getAvailableShareSpaces(final NodeRef space);

    /**
     * Get all interest gruop that are invited to shared space Interest groups belong to same category
     *
     * @param shareSpace node ref of spaces that is shared
     * @return A pair with the IG nodeRef as first the related permissions as second
     */
    @Auditable(/*key = Auditable.Key.ARG_0, */ parameters = {"shareSpace"})
    List<Pair<NodeRef, String>> getInvitedInterestGroups(final NodeRef shareSpace);

    /**
     * Invite interest group to shared space All users that have greater or equal permissin then
     * libraryPermission in invited interest group will have libraryPermission in shared space
     *
     * @param shareSpace        noderef of shared spaced
     * @param interestGroup     node ref of interest group that you want to invite to shared space
     * @param libraryPermission perrmision that qualified users from interest gruop will have in
     *                          shared space
     */
    @Auditable(
            /*key = Auditable.Key.ARG_0, */ parameters = {
            "shareSpace",
            "interestGroup",
            "libraryPermission"
    })
    void inviteInterestGroup(
            final NodeRef shareSpace, final NodeRef interestGroup, final String libraryPermission);

    /**
     * Crate link to a shared space Normally currentSpace and and sharedSpace belong to same category
     * but different interest group
     *
     * @param currentSpace node ref of space where link will be created
     * @param sharedSpace  node ref of shared space to which link is pointing
     * @param name         name of link
     */
    @Auditable(/*key = Auditable.Key.ARG_0, */ parameters = {"shareSpace", "interestGroup", "name"})
    NodeRef linkSharedSpace(final NodeRef currentSpace, final NodeRef sharedSpace, final String name);

    /**
     * Uninvite interest group from shared space
     *
     * @param shareSpace    noderef of shared spaced
     * @param interestGroup node ref of interest group that you want to uninvite
     */
    @Auditable(/*key = Auditable.Key.ARG_0, */ parameters = {"shareSpace", "interestGroup"})
    void unInviteInterestGroup(final NodeRef shareSpace, final NodeRef interestGroup);
}
