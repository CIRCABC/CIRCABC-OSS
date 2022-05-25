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
package eu.cec.digit.circabc.service.dynamic.authority;

import org.alfresco.service.namespace.QName;

import java.util.List;
import java.util.Map;

public class TransgressCutHeritance extends AbstractTransgressCutHeritance {

    public TransgressCutHeritance() {
        super();
    }

    /**
     * @param mandatoryAspect the node that has been evaluated need to have specified aspect
     */
    @Override
    public void setMandatoryAspects(final Map<String, String> mandatoryAspects) {

        for (Map.Entry<String, String> elem : mandatoryAspects.entrySet()) {
            this.mandatoryAspects.put(
                    QName.createQName(elem.getKey()), QName.createQName(elem.getValue()));
        }
    }

    /**
     * List of permissions where the dynamic authority has competence for
     *
     * @param permissions list of permissions
     */
    @Override
    public void setRequiredFor(final List<String> permissions) {
        this.permissions.addAll(permissions);
        this.permissions.remove("NotificationStatus");
    }

    @Override
    public final void setCircabcService(final String circabcService) {
        this.circabcService = circabcService;
    }
}
