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
package eu.europa.ec.digit.circabc.rest.service.notification;

import org.alfresco.service.Auditable;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Service contract for managing paste-related notification settings on an Interest Group (IG).
 *
 * <p>Implementations control whether users are notified when content is pasted into an Interest
 * Group. Two independent flags are supported per IG: a "paste all" notification flag and a "paste"
 * notification flag. Each flag can be queried and toggled through the operations defined below.
 *
 * <p>Migration 3.1 -&gt; 3.4.6 - 02/12/2011: the {@code key} parameter of the {@code @Auditable}
 * annotation was commented out, and the deprecated {@code @PublicService} annotation was disabled.
 */
// @PublicService
public interface NotificationManagerService {
  /**
   * Indicates whether the "paste all" notification is enabled for the given Interest Group.
   *
   * @param igNodeRef the {@link NodeRef} of the Interest Group to inspect
   * @return {@code true} if "paste all" notifications are enabled for the Interest Group, {@code
   *     false} otherwise
   */
  @Auditable(/*key = Auditable.Key.ARG_0, */ parameters = { "igNodeRef" })
  boolean isPasteAllNotificationEnabled(NodeRef igNodeRef);

  /**
   * Indicates whether the "paste" notification is enabled for the given Interest Group.
   *
   * @param ignodeRef the {@link NodeRef} of the Interest Group to inspect
   * @return {@code true} if "paste" notifications are enabled for the Interest Group, {@code false}
   *     otherwise
   */
  @Auditable(/*key = Auditable.Key.ARG_0, */ parameters = { "igNodeRef" })
  boolean isPasteNotificationEnabled(NodeRef ignodeRef);

  /**
   * Enables or disables the "paste all" notification for the given Interest Group.
   *
   * @param igNodeRef the {@link NodeRef} of the Interest Group to update
   * @param value {@code true} to enable "paste all" notifications, {@code false} to disable them
   */
  @Auditable(
    /*key = Auditable.Key.ARG_0, */ parameters = { "igNodeRef", "value" }
  )
  void setPasteAllNotificationEnabled(NodeRef igNodeRef, boolean value);

  /**
   * Enables or disables the "paste" notification for the given Interest Group.
   *
   * @param igNodeRef the {@link NodeRef} of the Interest Group to update
   * @param value {@code true} to enable "paste" notifications, {@code false} to disable them
   */
  @Auditable(
    /*key = Auditable.Key.ARG_0, */ parameters = { "igNodeRef", "value" }
  )
  void setPasteNotificationEnabled(NodeRef igNodeRef, boolean value);
}
