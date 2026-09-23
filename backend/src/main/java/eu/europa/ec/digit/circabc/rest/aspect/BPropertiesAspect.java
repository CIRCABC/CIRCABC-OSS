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
import java.util.List;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Alfresco repository behaviour that enforces the invariants of the CIRCABC
 * {@code bproperties} aspect ({@link DocumentModel#ASPECT_BPROPERTIES}) on
 * multilingual content.
 *
 * <p>This class is not a REST endpoint; it registers Alfresco content policies
 * (behaviours) and reacts to node lifecycle events by implementing
 * {@link NodeServicePolicies.OnAddAspectPolicy} and
 * {@link NodeServicePolicies.OnCreateNodePolicy}. Its purpose is to guarantee
 * that the {@code bproperties} aspect is carried by the correct nodes in a
 * multilingual document structure:</p>
 *
 * <ul>
 *   <li>When the aspect is added to a multilingual container, it is removed
 *       from that container's children (the aspect must live on the container,
 *       not on the individual translations).</li>
 *   <li>When a multilingual container node is created, the aspect is added to
 *       the newly created child node.</li>
 * </ul>
 */
public class BPropertiesAspect
  implements
    NodeServicePolicies.OnAddAspectPolicy,
    NodeServicePolicies.OnCreateNodePolicy
{

  /** Bean/behaviour name used to reference this aspect handler in Spring configuration. */
  public static final String NAME = "BPropertiesAspect";

  /** Logger for this behaviour. */
  private static final Log logger = LogFactory.getLog(BPropertiesAspect.class);

  /** Alfresco policy component used to bind this class's behaviours to node events. */
  @Autowired
  private PolicyComponent policyComponent;

  /** Alfresco node service used to inspect and mutate nodes and their aspects. */
  @Autowired
  private NodeService nodeService;

  /**
   * Registers this class's behaviours with the Alfresco policy component after
   * Spring has injected its dependencies.
   *
   * <p>Binds {@link #onAddAspect(NodeRef, QName)} to the {@code onAddAspect}
   * policy for the {@code bproperties} aspect, and
   * {@link #onCreateNode(ChildAssociationRef)} to the {@code onCreateNode}
   * policy for multilingual container nodes.</p>
   */
  @PostConstruct
  public void initialise() {
    if (logger.isInfoEnabled()) {
      logger.info("policy bind (onAddAspect)");
    }

    policyComponent.bindClassBehaviour(
      QName.createQName(NamespaceService.ALFRESCO_URI, "onAddAspect"),
      DocumentModel.ASPECT_BPROPERTIES,
      new JavaBehaviour(this, "onAddAspect")
    );

    policyComponent.bindClassBehaviour(
      QName.createQName(NamespaceService.ALFRESCO_URI, "onCreateNode"),
      ContentModel.TYPE_MULTILINGUAL_CONTAINER,
      new JavaBehaviour(this, "onCreateNode")
    );
  }

  /**
   * Handles the {@code onAddAspect} policy event.
   *
   * <p>When the {@code bproperties} aspect is added to a multilingual
   * container, this method removes the same aspect from the container's child
   * nodes so that the aspect is only held by the container itself. The call is
   * a no-op if the node no longer exists or is not a multilingual container
   * receiving the {@code bproperties} aspect.</p>
   *
   * @param nodeRef        reference to the node the aspect was added to
   * @param aspectTypeQName qualified name of the aspect that was added
   */
  public void onAddAspect(final NodeRef nodeRef, final QName aspectTypeQName) {
    if (!nodeService.exists(nodeRef)) {
      return;
    }

    if (!isMultilingualContainerWithBProperties(nodeRef, aspectTypeQName)) {
      return;
    }

    if (logger.isInfoEnabled()) {
      logger.info(
        "mlcontainer receive aspect bproperties. We must remove this aspect on his child"
      );
    }

    removeBPropertiesFromChildren(nodeRef);
  }

  private boolean isMultilingualContainerWithBProperties(
    NodeRef nodeRef,
    QName aspectTypeQName
  ) {
    return (
      nodeService
        .getType(nodeRef)
        .isMatch(ContentModel.TYPE_MULTILINGUAL_CONTAINER) &&
      aspectTypeQName.equals(DocumentModel.ASPECT_BPROPERTIES)
    );
  }

  private void removeBPropertiesFromChildren(NodeRef nodeRef) {
    List<ChildAssociationRef> docs = nodeService.getChildAssocs(nodeRef);
    for (ChildAssociationRef ref : docs) {
      NodeRef childRef = ref.getChildRef();
      if (nodeService.hasAspect(childRef, DocumentModel.ASPECT_BPROPERTIES)) {
        if (logger.isInfoEnabled()) {
          logger.info("remove bproperties to " + childRef.getId());
        }
        nodeService.removeAspect(childRef, DocumentModel.ASPECT_BPROPERTIES);
      }
    }
  }

  /**
   * Handles the {@code onCreateNode} policy event for multilingual containers.
   *
   * <p>Adds the {@code bproperties} aspect to the newly created child node of
   * the given parent-child association.</p>
   *
   * @param ref the child association describing the newly created node
   */
  public void onCreateNode(final ChildAssociationRef ref) {
    nodeService.addAspect(
      ref.getChildRef(),
      DocumentModel.ASPECT_BPROPERTIES,
      null
    );
  }
}
