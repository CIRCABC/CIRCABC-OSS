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
package eu.cec.digit.circabc.service.newsgroup;

import org.alfresco.service.Auditable;
import org.alfresco.service.NotAuditable;
import org.alfresco.service.cmr.repository.NodeRef;

import java.util.List;

/**
 * Interface for the newsgroup moderation.
 *
 * <pre>
 * 		Ideally containers are TYPE_FORUM or TYPE_TOPIC and contents are TYPE_POST. But it is not required.
 * </pre>
 *
 * @author Yanick Pignot
 * <p>Migration 3.1 -> 3.4.6 - 02/12/2011 Commented the key parameter of the @Auditable
 * annotation. Commented the deprecated @PublicService annotation.
 */
// @PublicService
public interface ModerationService {

    /**
     * Set a content being accepted
     */
    @Auditable(/*key = Auditable.Key.ARG_0, */ parameters = {"content"})
    void accept(final NodeRef content);

    /**
     * Set a container and all its subcontainer being moderated. The contents will be marked as
     * wainting for approval IF the makeContentWaiting parameter is set as TRUE.
     */
    @Auditable(/*key = Auditable.Key.ARG_0, */ parameters = {"container"})
    void applyModeration(final NodeRef container, final boolean makeContentWaiting);

    /**
     * Get the abuses signaled on a given node.
     *
     * @return An empty list if no abuse signaled.
     */
    @NotAuditable
    List<AbuseReport> getAbuses(final NodeRef content);

    /**
     * Return if the given node is accpected
     */
    @NotAuditable
    boolean isApproved(final NodeRef content);

    /**
     * Return if the given node is part of a moderation
     */
    @NotAuditable
    boolean isContainerModerated(final NodeRef container);

    /**
     * Return if the given node is rejected
     */
    @NotAuditable
    boolean isRejected(final NodeRef content);

    /**
     * Return if the given node is waiting for approval
     */
    @NotAuditable
    boolean isWaitingForApproval(final NodeRef content);

    /**
     * Set a content being rejected
     */
    @Auditable(/*key = Auditable.Key.ARG_0, */ parameters = {"content", "message"})
    void reject(final NodeRef content, final String message);

    /**
     * Signal that an abuse is reported
     */
    @Auditable(/*key = Auditable.Key.ARG_0, */ parameters = {"content", "message"})
    AbuseReport signalAbuse(final NodeRef content, final String message);

    /**
     * Signal that an abuse has been dealed.
     */
    @Auditable(/*key = Auditable.Key.ARG_0, */ parameters = {"content"})
    void signalNotAbuse(final NodeRef content);

    /**
     * Set a content being waiting for approval
     */
    @Auditable(/*key = Auditable.Key.ARG_0, */ parameters = {"content"})
    void waitForApproval(final NodeRef content);

    void stopModeration(NodeRef container, String action);
}
