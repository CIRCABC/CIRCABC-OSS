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
package eu.europa.ec.digit.circabc.rest.service.newsgroup;

import io.swagger.model.AbuseReport;
import java.util.List;
import org.alfresco.service.Auditable;
import org.alfresco.service.NotAuditable;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Service contract for the moderation of newsgroup content within CIRCABC.
 *
 * <p>Moderation controls the lifecycle of posted content: a container (typically a forum or topic)
 * can be placed under moderation, and the content it holds then transitions through the moderation
 * states of <em>waiting for approval</em>, <em>approved/accepted</em> or <em>rejected</em>. The
 * service also supports reporting content as abusive and clearing such reports.
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
   * Marks the given content as accepted (approved), making it visible to end users.
   *
   * @param content the node reference of the content to accept
   */
  @Auditable(/*key = Auditable.Key.ARG_0, */ parameters = { "content" })
  void accept(final NodeRef content);

  /**
   * Places the given container, and all of its sub-containers, under moderation. When
   * {@code makeContentWaiting} is {@code true}, the content currently held by the container is
   * flagged as waiting for approval.
   *
   * @param container the node reference of the container to place under moderation
   * @param makeContentWaiting {@code true} to mark existing content as waiting for approval,
   *     {@code false} to leave the current content state unchanged
   */
  @Auditable(/*key = Auditable.Key.ARG_0, */ parameters = { "container" })
  void applyModeration(
    final NodeRef container,
    final boolean makeContentWaiting
  );

  /**
   * Returns the abuse reports that have been signalled against the given node.
   *
   * @param content the node reference to inspect
   * @return the list of abuse reports, or an empty list if no abuse has been signalled
   */
  @NotAuditable
  List<AbuseReport> getAbuses(final NodeRef content);

  /**
   * Indicates whether the given content has been approved (accepted).
   *
   * @param content the node reference to test
   * @return {@code true} if the content is approved, {@code false} otherwise
   */
  @NotAuditable
  boolean isApproved(final NodeRef content);

  /**
   * Indicates whether the given container is currently under moderation.
   *
   * @param container the node reference of the container to test
   * @return {@code true} if the container is moderated, {@code false} otherwise
   */
  @NotAuditable
  boolean isContainerModerated(final NodeRef container);

  /**
   * Indicates whether the given content has been rejected.
   *
   * @param content the node reference to test
   * @return {@code true} if the content is rejected, {@code false} otherwise
   */
  @NotAuditable
  boolean isRejected(final NodeRef content);

  /**
   * Indicates whether the given content is waiting for approval.
   *
   * @param content the node reference to test
   * @return {@code true} if the content is waiting for approval, {@code false} otherwise
   */
  @NotAuditable
  boolean isWaitingForApproval(final NodeRef content);

  /**
   * Marks the given content as rejected, keeping it hidden from end users and recording the reason.
   *
   * @param content the node reference of the content to reject
   * @param message the message describing the reason for the rejection
   */
  @Auditable(
    /*key = Auditable.Key.ARG_0, */ parameters = { "content", "message" }
  )
  void reject(final NodeRef content, final String message);

  /**
   * Signals that the given content is being reported as an abuse.
   *
   * @param content the node reference of the reported content
   * @param message the message describing the reported abuse
   * @return the created {@link AbuseReport} representing the signalled abuse
   */
  @Auditable(
    /*key = Auditable.Key.ARG_0, */ parameters = { "content", "message" }
  )
  AbuseReport signalAbuse(final NodeRef content, final String message);

  /**
   * Signals that a previously reported abuse on the given content has been dealt with (cleared).
   *
   * @param content the node reference whose abuse report is being cleared
   */
  @Auditable(/*key = Auditable.Key.ARG_0, */ parameters = { "content" })
  void signalNotAbuse(final NodeRef content);

  /**
   * Marks the given content as waiting for approval.
   *
   * @param content the node reference of the content to place in the waiting-for-approval state
   */
  @Auditable(/*key = Auditable.Key.ARG_0, */ parameters = { "content" })
  void waitForApproval(final NodeRef content);

  /**
   * Stops moderation on the given container, applying the supplied action to the content it holds.
   *
   * @param container the node reference of the container whose moderation is being stopped
   * @param action the action to apply to the existing content when moderation is stopped
   */
  void stopModeration(NodeRef container, String action);
}
