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

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

import static eu.cec.digit.circabc.model.CircabcModel.ASPECT_LIBRARY;

/**
 * This class contains the behaviour behind the 'ci:circaLibraryService' aspect. So far this aspect
 * is just used as a tag.
 *
 * @author Clinckart Stephane
 */
public class LibraryAspect extends AbstractAspect implements
        NodeServicePolicies.OnAddAspectPolicy,
        NodeServicePolicies.BeforeDeleteNodePolicy {

    private static final Log logger = LogFactory.getLog(LibraryAspect.class);

    /**
     * Spring initialise method used to register the policy behaviours
     */
    public void initialise() {
        // Register the policy behaviours
        this.policyComponent.bindClassBehaviour(QName.createQName(
                NamespaceService.ALFRESCO_URI, "onAddAspect"),
                getComparatorQName(), new JavaBehaviour(this, "onAddAspect"));
        // BeforeDeleteNodePolicy
        this.policyComponent.bindClassBehaviour(QName.createQName(
                NamespaceService.ALFRESCO_URI, "beforeDeleteNode"),
                getComparatorQName(), new JavaBehaviour(this,
                        "beforeDeleteNode"));
    }

    @Override
    public ComparatorType getComparator() {
        return ComparatorType.ASPECT;
    }

    @Override
    public QName getComparatorQName() {
        return ASPECT_LIBRARY;
    }

    /**
     * onAddAspect policy behaviour. No special java code for behavior
     *
     * @param nodeRef         the node reference
     * @param aspectTypeQName the qname of the aspect being applied
     */
    public void onAddAspect(final NodeRef nodeRef, final QName aspectTypeQName) {

        if (aspectTypeQName.equals(getComparatorQName()) == true) {
        }
    }

    /**
     * WorkArround for ML bug when deleting a folder who contains a empty_translation
     */
    @Override
    public void beforeDeleteNode(final NodeRef nodeRef) {
        if (nodeService.getType(nodeRef).equals(ContentModel.TYPE_FOLDER)) {
            final List<ChildAssociationRef> childs = nodeService.getChildAssocs(nodeRef);

            for (ChildAssociationRef child : childs) {
                if (nodeService
                        .hasAspect(child.getChildRef(), ContentModel.ASPECT_MULTILINGUAL_EMPTY_TRANSLATION)) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Work arround for deleting an Empty Translation");
                    }
                    nodeService.deleteNode(child.getChildRef());
                }
            }
        }
    }
}
