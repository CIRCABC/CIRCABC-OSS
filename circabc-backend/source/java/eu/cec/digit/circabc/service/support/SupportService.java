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
package eu.cec.digit.circabc.service.support;

import eu.cec.digit.circabc.repo.support.SupportContact;
import eu.cec.digit.circabc.repo.support.SupportTypes;
import org.alfresco.service.NotAuditable;
import org.alfresco.service.cmr.repository.NodeRef;

import java.util.List;

/**
 * Interface to manage support capabilities
 *
 * @author Stephane Clinckart
 * @author Pierre Beauregard
 */
public interface SupportService {

    /**
     * check if the user is from the support
     */
    @NotAuditable
    boolean isUserFromSupport(final String user);

    @NotAuditable
    void setSupportGroupName(final String supportGroupName);

    List<SupportContact> getAllSupportContacts();

    SupportContact getContactById(String id);

    Boolean sendSupportRequest(
            String subject,
            String description,
            SupportTypes byId,
            String currentUserName,
            SupportContact contact,
            NodeRef actionNodeRef);

    Boolean sendSupportRequestAsGuest(
            String subject,
            String description,
            SupportTypes byId,
            String mailAddress,
            SupportContact contact,
            NodeRef actionNodeRef);
}
