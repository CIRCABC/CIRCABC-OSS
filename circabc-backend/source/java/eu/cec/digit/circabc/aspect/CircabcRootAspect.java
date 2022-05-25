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

import eu.cec.digit.circabc.config.CircabcConfiguration;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.Map;

import static eu.cec.digit.circabc.model.CircabcModel.ASPECT_CIRCABC_ROOT;

/**
 * Implement behavior for CircaBCAspect aspect
 *
 * @author Clinckart Stephane
 */

/**
 * @todo add the policy BeforeDeleteNodePolicy to delete the associated groups
 * and profiles
 */
public class CircabcRootAspect extends AbstractAspect implements
        NodeServicePolicies.OnAddAspectPolicy,
        NodeServicePolicies.BeforeDeleteNodePolicy,
        NodeServicePolicies.OnUpdatePropertiesPolicy {

    /**
     * Aspect name
     */
    public static final String NAME_CIRCABC = CircabcConfiguration
            .getProperty(CircabcConfiguration.CIRCABC_ROOT_NODE_NAME_PROPERTIES);
    private static final Log logger = LogFactory.getLog(CircabcRootAspect.class);

    @Override
    public ComparatorType getComparator() {
        return ComparatorType.ASPECT;
    }

    @Override
    public QName getComparatorQName() {
        return ASPECT_CIRCABC_ROOT;
    }

    /**
     * Just trace the creation of a Circabc root node
     */
    public void onAddAspect(final NodeRef nodeRef, final QName aspectTypeQName) {
        if (logger.isTraceEnabled()) {
            logger
                    .trace("add Aspect:" + getComparatorQName() + " to Node:"
                            + nodeRef);
        }
    }

    /**
     * Don't allow the modification of the name of the circabc root node
     */
    public void onUpdateProperties(final NodeRef arg0, final Map<QName, Serializable> before,
                                   final Map<QName, Serializable> after) {
        final Serializable newName = (after != null) ? after.get(ContentModel.PROP_NAME) : null;

        if (newName != null && !newName.toString().equals(NAME_CIRCABC)) {
            throw new IllegalArgumentException("The name of this folder must be " + NAME_CIRCABC);
        }
    }

    /**
     * Spring initialise method used to register the policy behaviours
     */
    public void initialise() {
        // Register the policy behaviours

        // on add aspect policy
        this.policyComponent.bindClassBehaviour(QName.createQName(
                NamespaceService.ALFRESCO_URI, "onAddAspect"), getComparatorQName(),
                new JavaBehaviour(this, "onAddAspect"));

        // BeforeDeleteNodePolicy
        this.policyComponent.bindClassBehaviour(QName.createQName(
                NamespaceService.ALFRESCO_URI, "beforeDeleteNode"),
                getComparatorQName(), new JavaBehaviour(this, "beforeDeleteNode"));

        // on property change policy
        this.policyComponent.bindClassBehaviour(QName.createQName(
                NamespaceService.ALFRESCO_URI, "onUpdateProperties"),
                getComparatorQName(), new JavaBehaviour(this, "onUpdateProperties"));
    }

    public interface Roles {

    }
}
