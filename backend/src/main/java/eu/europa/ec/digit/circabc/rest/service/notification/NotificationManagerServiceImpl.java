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

import io.swagger.model.alfresco.CircabcModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Default implementation of {@link NotificationManagerService}.
 *
 * <p>This service manages the "paste" notification settings of an Interest Group (IG) root node.
 * The enabled/disabled state of each notification type is represented by the presence or absence of
 * a dedicated Alfresco aspect on the node:
 *
 * <ul>
 *   <li>{@link CircabcModel#ASPECT_NOTIFY_PASTE_ALL} — notify all recipients on paste operations;
 *   <li>{@link CircabcModel#ASPECT_NOTIFY_PASTE} — notify on paste operations.
 * </ul>
 *
 * <p>All operations require the target node to be an Interest Group root, i.e. to carry the {@link
 * CircabcModel#ASPECT_IGROOT} aspect; otherwise an {@link IllegalArgumentException} is raised.
 *
 * @author filipsl
 */
public class NotificationManagerServiceImpl
  implements NotificationManagerService
{

  /** Alfresco node service used to inspect and mutate aspects on the IG root node. */
  @Autowired
  private NodeService nodeService;

  /**
   * Indicates whether "paste all" notifications are enabled for the given Interest Group root node.
   *
   * @param igNodeRef the reference to the Interest Group root node
   * @return {@code true} if the {@link CircabcModel#ASPECT_NOTIFY_PASTE_ALL} aspect is present,
   *     {@code false} otherwise
   * @throws IllegalArgumentException if {@code igNodeRef} is not an Interest Group root node
   */
  public boolean isPasteAllNotificationEnabled(NodeRef igNodeRef) {
    validateNode(igNodeRef);
    return nodeService.hasAspect(
      igNodeRef,
      CircabcModel.ASPECT_NOTIFY_PASTE_ALL
    );
  }

  /**
   * Indicates whether "paste" notifications are enabled for the given Interest Group root node.
   *
   * @param igNodeRef the reference to the Interest Group root node
   * @return {@code true} if the {@link CircabcModel#ASPECT_NOTIFY_PASTE} aspect is present, {@code
   *     false} otherwise
   * @throws IllegalArgumentException if {@code igNodeRef} is not an Interest Group root node
   */
  public boolean isPasteNotificationEnabled(NodeRef igNodeRef) {
    validateNode(igNodeRef);
    return nodeService.hasAspect(igNodeRef, CircabcModel.ASPECT_NOTIFY_PASTE);
  }

  /**
   * Enables or disables "paste all" notifications for the given Interest Group root node by adding
   * or removing the {@link CircabcModel#ASPECT_NOTIFY_PASTE_ALL} aspect.
   *
   * @param igNodeRef the reference to the Interest Group root node
   * @param value {@code true} to enable the notification (add the aspect), {@code false} to disable
   *     it (remove the aspect)
   * @throws IllegalArgumentException if {@code igNodeRef} is not an Interest Group root node
   */
  public void setPasteAllNotificationEnabled(NodeRef igNodeRef, boolean value) {
    validateNode(igNodeRef);
    checkNotificationAspect(
      igNodeRef,
      value,
      CircabcModel.ASPECT_NOTIFY_PASTE_ALL
    );
  }

  /**
   * Reconciles the presence of a notification aspect on the node with the desired enabled state.
   *
   * <p>The aspect is added when {@code value} is {@code true} and not yet present, and removed when
   * {@code value} is {@code false} and currently present. In all other cases the node is left
   * unchanged.
   *
   * @param igNodeRef the reference to the Interest Group root node
   * @param value the desired state: {@code true} to ensure the aspect is present, {@code false} to
   *     ensure it is absent
   * @param aspectName the qualified name of the notification aspect to add or remove
   */
  private void checkNotificationAspect(
    NodeRef igNodeRef,
    boolean value,
    QName aspectName
  ) {
    if (!nodeService.hasAspect(igNodeRef, aspectName)) {
      if (value) {
        nodeService.addAspect(igNodeRef, aspectName, null);
      }
    } else {
      if (!value) {
        nodeService.removeAspect(igNodeRef, aspectName);
      }
    }
  }

  /**
   * Enables or disables "paste" notifications for the given Interest Group root node by adding or
   * removing the {@link CircabcModel#ASPECT_NOTIFY_PASTE} aspect.
   *
   * @param igNodeRef the reference to the Interest Group root node
   * @param value {@code true} to enable the notification (add the aspect), {@code false} to disable
   *     it (remove the aspect)
   * @throws IllegalArgumentException if {@code igNodeRef} is not an Interest Group root node
   */
  public void setPasteNotificationEnabled(NodeRef igNodeRef, boolean value) {
    validateNode(igNodeRef);
    checkNotificationAspect(igNodeRef, value, CircabcModel.ASPECT_NOTIFY_PASTE);
  }

  /**
   * Ensures the supplied node is an Interest Group root node.
   *
   * @param igNodeRef the reference to the node to validate
   * @throws IllegalArgumentException if the node does not carry the {@link
   *     CircabcModel#ASPECT_IGROOT} aspect
   */
  private void validateNode(NodeRef igNodeRef) {
    if (!nodeService.hasAspect(igNodeRef, CircabcModel.ASPECT_IGROOT)) {
      throw new IllegalArgumentException(
        "Node " +
          igNodeRef +
          " does not have requied aspect " +
          CircabcModel.ASPECT_IGROOT
      );
    }
  }
}
