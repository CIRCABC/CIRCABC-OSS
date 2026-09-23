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
package eu.europa.ec.digit.circabc.rest.model;

import io.swagger.exception.CircabcRuntimeException;
import io.swagger.model.alfresco.DossierModel;
import jakarta.annotation.PostConstruct;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Alfresco policy behaviour that enforces the containment rules of a dossier space.
 *
 * <p>A dossier is not meant to hold real content. This behaviour therefore prevents users from
 * adding arbitrary nodes to a dossier and only permits file links, folder links and forums to be
 * created inside it. The rule is applied both when a node is created directly under a dossier
 * (via {@link NodeServicePolicies.BeforeCreateNodePolicy}) and when an existing node is moved into
 * a dossier (via {@link NodeServicePolicies.OnMoveNodePolicy}).
 *
 * <p>Both policies are bound to {@link ContentModel#TYPE_CMOBJECT} at start-up in {@link #init()}.
 *
 * @author Slobodan Filipovic
 */
public class DossierModelType
  implements
    NodeServicePolicies.BeforeCreateNodePolicy,
    NodeServicePolicies.OnMoveNodePolicy
{

  /** Message key raised when a disallowed node type is placed in a dossier. */
  private static final String ERR_MSG_ONLY_LINKS = "only_links";

  /** Alfresco policy component used to register (bind) the behaviours in {@link #init()}. */
  @Autowired
  private PolicyComponent policyComponent;

  /** Node service used to resolve the type of the parent and of the affected nodes. */
  @Autowired
  private NodeService nodeService;

  /**
   * Registers the behaviours with the policy component after Spring has injected the dependencies.
   *
   * <p>Binds both the {@code beforeCreateNode} and {@code onMoveNode} behaviours to
   * {@link ContentModel#TYPE_CMOBJECT} so that the dossier containment rules are enforced on node
   * creation and node moves.
   */
  @PostConstruct
  public void init() {
    // Register the policy behaviours
    policyComponent.bindClassBehaviour(
      NodeServicePolicies.BeforeCreateNodePolicy.QNAME,
      ContentModel.TYPE_CMOBJECT,
      new JavaBehaviour(this, "beforeCreateNode")
    );

    policyComponent.bindClassBehaviour(
      NodeServicePolicies.OnMoveNodePolicy.QNAME,
      ContentModel.TYPE_CMOBJECT,
      new JavaBehaviour(this, "onMoveNode")
    );
  }

  /**
   * Callback invoked before a node is created; rejects disallowed types under a dossier.
   *
   * @param parentRef the reference of the parent node under which the new node is being created
   * @param assocTypeQName the qualified name of the association type linking parent and child
   * @param assocQName the qualified name of the child association instance
   * @param nodeTypeQName the qualified name of the type of the node being created
   * @throws CircabcRuntimeException if the parent is a dossier and the node type is not a file
   *     link, folder link or forum
   */
  public void beforeCreateNode(
    final NodeRef parentRef,
    final QName assocTypeQName,
    final QName assocQName,
    final QName nodeTypeQName
  ) {
    final QName parentType = nodeService.getType(parentRef);
    checkTypes(nodeTypeQName, parentType);
  }

  /**
   * Enforces the dossier containment rule: only file links, folder links or forums may live in a
   * dossier space.
   *
   * <p>If the parent is not a dossier space the check is a no-op.
   *
   * @param nodeTypeQName the qualified name of the type of the affected node
   * @param parentType the qualified name of the type of the parent node
   * @throws CircabcRuntimeException if the parent is a dossier and the node type is not one of the
   *     allowed link/forum types
   */
  private void checkTypes(final QName nodeTypeQName, final QName parentType) {
    if (
      parentType.equals(DossierModel.TYPE_DOSSIER_SPACE) &&
      !(nodeTypeQName.equals(ApplicationModel.TYPE_FILELINK) ||
        nodeTypeQName.equals(ApplicationModel.TYPE_FOLDERLINK) ||
        nodeTypeQName.equals(ForumModel.TYPE_FORUM))
    ) {
      throw new CircabcRuntimeException(ERR_MSG_ONLY_LINKS);
    }
  }

  /**
   * Callback invoked after a node has been moved; rejects the move if the new parent is a dossier
   * and the moved node is not an allowed type.
   *
   * @param oldChildAssocRef the child association reference before the move
   * @param newChildAssocRef the child association reference after the move, used to resolve the new
   *     parent and the moved node
   * @throws CircabcRuntimeException if the new parent is a dossier and the moved node is not a file
   *     link, folder link or forum
   */
  @Override
  public void onMoveNode(
    ChildAssociationRef oldChildAssocRef,
    ChildAssociationRef newChildAssocRef
  ) {
    final NodeRef parentRef = newChildAssocRef.getParentRef();
    final QName parentType = nodeService.getType(parentRef);
    final NodeRef nodeRef = newChildAssocRef.getChildRef();
    final QName nodeTypeQName = nodeService.getType(nodeRef);
    checkTypes(nodeTypeQName, parentType);
  }
}
