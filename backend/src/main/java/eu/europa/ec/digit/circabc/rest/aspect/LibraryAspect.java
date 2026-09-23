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

import io.swagger.model.alfresco.CircabcModel;
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
 * Alfresco policy behaviour bound to the {@code ci:circaLibraryService} aspect
 * ({@link CircabcModel#ASPECT_LIBRARY}), which marks the Library service node of a CIRCABC Interest
 * Group. Although the aspect itself is essentially a tag, this class registers repository policy
 * behaviours that react to node lifecycle events occurring within a library.
 *
 * <p>Specifically, it implements {@link NodeServicePolicies.BeforeDeleteNodePolicy} to work around a
 * multilingual (ML) issue that prevents a folder from being deleted when it still contains an empty
 * translation child node.
 *
 * @author Clinckart Stephane
 */
public class LibraryAspect
  implements NodeServicePolicies.BeforeDeleteNodePolicy
{

  /** Logger for this aspect behaviour. */
  private static final Log logger = LogFactory.getLog(LibraryAspect.class);

  /** Alfresco policy component used to bind this class's behaviours to the library aspect. */
  @Autowired
  private PolicyComponent policyComponent;

  /** Node service used to inspect node types/aspects and to delete empty translation children. */
  @Autowired
  private NodeService nodeService;

  /**
   * Spring initialisation method that registers this class's policy behaviours. It binds
   * {@link #beforeDeleteNode(NodeRef)} to the {@code beforeDeleteNode} policy for the
   * {@link CircabcModel#ASPECT_LIBRARY} aspect. Invoked automatically after dependency injection.
   */
  @PostConstruct
  public void initialise() {
    // Register the policy behaviours
    // BeforeDeleteNodePolicy
    this.policyComponent.bindClassBehaviour(
      QName.createQName(NamespaceService.ALFRESCO_URI, "beforeDeleteNode"),
      CircabcModel.ASPECT_LIBRARY,
      new JavaBehaviour(this, "beforeDeleteNode")
    );
  }

  /**
   * Callback triggered before a node carrying the library aspect is deleted. This is a workaround
   * for a multilingual bug: when the node being deleted is a folder, any child node flagged with
   * {@link ContentModel#ASPECT_MULTILINGUAL_EMPTY_TRANSLATION} is explicitly deleted first so that
   * the folder can be removed cleanly. Non-folder nodes are ignored.
   *
   * @param nodeRef reference to the node that is about to be deleted
   */
  @Override
  public void beforeDeleteNode(final NodeRef nodeRef) {
    if (nodeService.getType(nodeRef).equals(ContentModel.TYPE_FOLDER)) {
      final List<ChildAssociationRef> childs = nodeService.getChildAssocs(
        nodeRef
      );

      for (ChildAssociationRef child : childs) {
        if (
          nodeService.hasAspect(
            child.getChildRef(),
            ContentModel.ASPECT_MULTILINGUAL_EMPTY_TRANSLATION
          )
        ) {
          if (logger.isTraceEnabled()) {
            logger.trace("Work arround for deleting an Empty Translation");
          }
          nodeService.deleteNode(child.getChildRef());
        }
      }
    }
  }
}
