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

import eu.cec.digit.circabc.model.DocumentModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author patrice.coppens@trasys.lu
 * <p>
 * 24-juil.-07 - 09:26:59
 */
public class MLDocumentAspect extends AbstractAspect implements
        NodeServicePolicies.OnAddAspectPolicy,
        NodeServicePolicies.BeforeDeleteNodePolicy {

    /**
     * bean name
     */
    public static final String NAME = "mLDocumentAspect";
    private static final Log LOGGER = LogFactory.getLog(MLDocumentAspect.class);

    @Override
    public ComparatorType getComparator() {
        return ComparatorType.ASPECT;
    }

    @Override
    public QName getComparatorQName() {
        return ContentModel.ASPECT_MULTILINGUAL_DOCUMENT;
    }


    /**
     * Initialise the Multilingual Aspect.
     * <p>
     * Ensures that the {@link ContentModel#ASPECT_MULTILINGUAL_DOCUMENT ml document aspect}
     */

    @Override
    public void initialise() {

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("policy bind (onAddAspect)");
        }

        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onAddAspect"),
                ContentModel.ASPECT_MULTILINGUAL_DOCUMENT,
                new JavaBehaviour(this, "onAddAspect"));

        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "beforeDeleteNode"),
                ContentModel.ASPECT_MULTILINGUAL_EMPTY_TRANSLATION,
                new JavaBehaviour(this, "beforeDeleteNode"));


    }

    public void onAddAspect(final NodeRef nodeRef, final QName aspectTypeQName) {

        //	check if exist
        if (!this.nodeService.exists(nodeRef)) {
            return;
        }

        nodeService.removeAspect(nodeRef, DocumentModel.ASPECT_BPROPERTIES);
        //add CProperties Aspect
        nodeService.addAspect(nodeRef, DocumentModel.ASPECT_CPROPERTIES, null);

    }

    public void beforeDeleteNode(final NodeRef nodeRef) {
        // add temporary aspect to force a complete deletion of the empty translation
        nodeService.addAspect(nodeRef, ContentModel.ASPECT_TEMPORARY, null);

        super.beforeDeleteNode(nodeRef);
    }


}
