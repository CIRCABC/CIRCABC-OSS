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
package eu.cec.digit.circabc.aspect;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static eu.cec.digit.circabc.model.CircabcModel.ASPECT_IGROOT;

/**
 * Implement behavior for IGRoot aspect
 *
 * @author Philippe Dubois
 */

/**
 * @todo add the policy BeforeDeleteNodePolicy to delete the associated groups
 * and profiles
 */
public class IGRootAspect extends AbstractAspect implements
        NodeServicePolicies.OnAddAspectPolicy,
        NodeServicePolicies.BeforeDeleteNodePolicy {

    private static final Log logger = LogFactory.getLog(IGRootAspect.class);

    @Override
    public ComparatorType getComparator() {
        return ComparatorType.ASPECT;
    }

    @Override
    public QName getComparatorQName() {
        return ASPECT_IGROOT;
    }

    public void onAddAspect(final NodeRef nodeRef, final QName aspectTypeQName) {
        if (logger.isTraceEnabled()) {
            logger.trace("Add Aspect:" + getComparatorQName() + " to node:" + nodeRef);
        }
    }

    /**
     * Spring initialise method used to register the policy behaviours
     */
    public void initialise() {
        // Register the policy behaviours
        this.policyComponent.bindClassBehaviour(QName.createQName(
                NamespaceService.ALFRESCO_URI, "onAddAspect"), getComparatorQName(),
                new JavaBehaviour(this, "onAddAspect"));
        // BeforeDeleteNodePolicy
        this.policyComponent.bindClassBehaviour(QName.createQName(
                NamespaceService.ALFRESCO_URI, "beforeDeleteNode"),
                getComparatorQName(), new JavaBehaviour(this, "beforeDeleteNode"));
    }

    public interface Roles {

        /* non prefixed role */
        String IGLEADER = "IGLEADER";
        String ROLE_DIRADMIN = "ROLE_DIRADMIN";
    }
}
