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
package eu.cec.digit.circabc.repo.notification;

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.service.notification.NotificationManagerService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author filipsl
 */
public class NotificationManagerServiceImpl implements NotificationManagerService {

    private static final Log logger = LogFactory.getLog(NotificationManagerServiceImpl.class);

    // the services to inject
    private NodeService nodeService;

    /**
     * @return the nodeService
     */
    private NodeService getNodeService() {
        return nodeService;
    }

    /**
     * @param nodeService the nodeService to set
     */
    public final void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public boolean isPasteAllNotificationEnabled(NodeRef igNodeRef) {
        validateNode(igNodeRef);
        return getNodeService().hasAspect(igNodeRef, CircabcModel.ASPECT_NOTIFY_PASTE_ALL);
    }

    public boolean isPasteNotificationEnabled(NodeRef igNodeRef) {
        validateNode(igNodeRef);
        return getNodeService().hasAspect(igNodeRef, CircabcModel.ASPECT_NOTIFY_PASTE);
    }

    public void setPasteAllNotificationEnabled(NodeRef igNodeRef, boolean value) {
        validateNode(igNodeRef);
        checkNotificationAspect(igNodeRef, value, CircabcModel.ASPECT_NOTIFY_PASTE_ALL);
    }

    /**
     * @param igNodeRef
     * @param value
     * @param aspectName
     */
    private void checkNotificationAspect(NodeRef igNodeRef, boolean value, QName aspectName) {
        if (!getNodeService().hasAspect(igNodeRef, aspectName)) {
            if (value) {
                getNodeService().addAspect(igNodeRef, aspectName, null);
            }
        } else {
            if (!value) {
                getNodeService().removeAspect(igNodeRef, aspectName);
            }
        }
    }

    public void setPasteNotificationEnabled(NodeRef igNodeRef, boolean value) {
        validateNode(igNodeRef);
        checkNotificationAspect(igNodeRef, value, CircabcModel.ASPECT_NOTIFY_PASTE);
    }

    /**
     * @param igNodeRef
     */
    private void validateNode(NodeRef igNodeRef) {

        if (!nodeService.hasAspect(igNodeRef, CircabcModel.ASPECT_IGROOT)) {
            throw new IllegalArgumentException(
                    "Node " + igNodeRef + " does not have requied aspect " + CircabcModel.ASPECT_IGROOT);
        }
    }
}
