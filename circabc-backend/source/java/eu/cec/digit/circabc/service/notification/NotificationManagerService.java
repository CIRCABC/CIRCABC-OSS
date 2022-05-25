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
package eu.cec.digit.circabc.service.notification;

import org.alfresco.service.Auditable;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Migration 3.1 -> 3.4.6 - 02/12/2011 Commented the key parameter of the @Auditable annotation.
 * Commented the deprecated @PublicService annotation.
 */
// @PublicService
public interface NotificationManagerService {

    @Auditable(/*key = Auditable.Key.ARG_0, */ parameters = {"igNodeRef"})
    boolean isPasteAllNotificationEnabled(NodeRef igNodeRef);

    @Auditable(/*key = Auditable.Key.ARG_0, */ parameters = {"igNodeRef"})
    boolean isPasteNotificationEnabled(NodeRef ignodeRef);

    @Auditable(/*key = Auditable.Key.ARG_0, */ parameters = {"igNodeRef", "value"})
    void setPasteAllNotificationEnabled(NodeRef igNodeRef, boolean value);

    @Auditable(/*key = Auditable.Key.ARG_0, */ parameters = {"igNodeRef", "value"})
    void setPasteNotificationEnabled(NodeRef igNodeRef, boolean value);
}
