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
package eu.europa.ec.digit.circabc.rest.aspect;

import io.swagger.model.alfresco.DocumentModel;
import jakarta.annotation.PostConstruct;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Alfresco policy behaviour that reacts to lifecycle events on multilingual documents.
 *
 * <p>This is not a REST endpoint; it is a Spring-managed bean that binds Alfresco
 * {@link NodeServicePolicies node service policies} during initialisation and keeps the
 * CIRCABC-specific document metadata consistent when multilingual content is created or
 * removed. Specifically it:
 * <ul>
 *   <li>Reacts to the {@code onAddAspect} policy for
 *       {@link ContentModel#ASPECT_MULTILINGUAL_DOCUMENT}, swapping the CIRCABC
 *       "B properties" aspect for the "C properties" aspect.</li>
 *   <li>Reacts to the {@code beforeDeleteNode} policy for
 *       {@link ContentModel#ASPECT_MULTILINGUAL_EMPTY_TRANSLATION}, flagging empty
 *       translations as temporary so they are fully deleted.</li>
 * </ul>
 *
 * @author patrice.coppens@trasys.lu
 * <p>
 * 24-juil.-07 - 09:26:59
 */
public class MLDocumentAspect
  implements
    NodeServicePolicies.OnAddAspectPolicy,
    NodeServicePolicies.BeforeDeleteNodePolicy
{

  /** Logger for this policy behaviour. */
  private static final Log LOGGER = LogFactory.getLog(MLDocumentAspect.class);

  /** Alfresco policy component used to register (bind) the class behaviours. */
  @Autowired
  private PolicyComponent policyComponent;

  /** Alfresco node service used to inspect and mutate node aspects. */
  @Autowired
  private NodeService nodeService;

  /**
   * Initialises the multilingual document behaviours.
   *
   * <p>Invoked automatically after bean construction. Binds this instance's
   * {@link #onAddAspect(NodeRef, QName)} method to the {@code onAddAspect} policy for the
   * {@link ContentModel#ASPECT_MULTILINGUAL_DOCUMENT} aspect, and its
   * {@link #beforeDeleteNode(NodeRef)} method to the {@code beforeDeleteNode} policy for the
   * {@link ContentModel#ASPECT_MULTILINGUAL_EMPTY_TRANSLATION} aspect.
   */

  @PostConstruct
  public void initialise() {
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("policy bind (onAddAspect)");
    }

    this.policyComponent.bindClassBehaviour(
      QName.createQName(NamespaceService.ALFRESCO_URI, "onAddAspect"),
      ContentModel.ASPECT_MULTILINGUAL_DOCUMENT,
      new JavaBehaviour(this, "onAddAspect")
    );

    this.policyComponent.bindClassBehaviour(
      QName.createQName(NamespaceService.ALFRESCO_URI, "beforeDeleteNode"),
      ContentModel.ASPECT_MULTILINGUAL_EMPTY_TRANSLATION,
      new JavaBehaviour(this, "beforeDeleteNode")
    );
  }

  /**
   * Handles the addition of the multilingual document aspect on a node.
   *
   * <p>When the {@link ContentModel#ASPECT_MULTILINGUAL_DOCUMENT} aspect is added to an
   * existing node, this removes the CIRCABC {@link DocumentModel#ASPECT_BPROPERTIES} aspect
   * and adds the {@link DocumentModel#ASPECT_CPROPERTIES} aspect. Non-existing nodes are
   * ignored.
   *
   * @param nodeRef        reference to the node the aspect was added to
   * @param aspectTypeQName qualified name of the aspect that was added
   */
  public void onAddAspect(final NodeRef nodeRef, final QName aspectTypeQName) {
    //	check if exist
    if (!this.nodeService.exists(nodeRef)) {
      return;
    }

    nodeService.removeAspect(nodeRef, DocumentModel.ASPECT_BPROPERTIES);
    //add CProperties Aspect
    nodeService.addAspect(nodeRef, DocumentModel.ASPECT_CPROPERTIES, null);
  }

  /**
   * Handles deletion of an empty translation node.
   *
   * <p>Invoked before a node carrying the
   * {@link ContentModel#ASPECT_MULTILINGUAL_EMPTY_TRANSLATION} aspect is deleted. Adds the
   * {@link ContentModel#ASPECT_TEMPORARY} aspect so that the empty translation is completely
   * removed rather than being moved to the archive/trash store.
   *
   * @param nodeRef reference to the node that is about to be deleted
   */
  public void beforeDeleteNode(final NodeRef nodeRef) {
    // add temporary aspect to force a complete deletion of the empty translation
    nodeService.addAspect(nodeRef, ContentModel.ASPECT_TEMPORARY, null);
  }
}
