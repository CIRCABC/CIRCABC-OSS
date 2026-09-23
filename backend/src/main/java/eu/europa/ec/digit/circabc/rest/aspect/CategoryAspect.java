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

import eu.europa.ec.digit.circabc.rest.service.app.CircabcService;
import io.swagger.model.alfresco.CircabcModel;
import jakarta.annotation.PostConstruct;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Alfresco policy behaviour bound to the CIRCABC {@code category} aspect
 * ({@link CircabcModel#ASPECT_CATEGORY}).
 *
 * <p>This class is not a REST endpoint; it registers a repository-level policy
 * behaviour that reacts to node lifecycle events. Specifically, it implements
 * {@link NodeServicePolicies.BeforeDeleteNodePolicy} so that, whenever a node
 * carrying the category aspect is about to be deleted, the associated category
 * master group (an Alfresco authority) and the CIRCABC-specific category data
 * are cleaned up as well. This keeps security groups and CIRCABC state
 * consistent with the repository content.
 *
 * @author Clinckart Stephane
 */

public class CategoryAspect
  implements NodeServicePolicies.BeforeDeleteNodePolicy
{

  /** Alfresco component used to bind this class' behaviours to policies. */
  @Autowired
  private PolicyComponent policyComponent;

  /** Service used to read node properties (e.g. the category master group). */
  @Autowired
  private NodeService nodeService;

  /** Service used to delete the category master group authority. */
  @Autowired
  private AuthorityService authorityService;

  /** CIRCABC service used to remove category-specific data on deletion. */
  @Autowired
  private CircabcService circabcService;

  /**
   * Spring initialisation callback that registers this class' policy
   * behaviours. It binds {@link #beforeDeleteNode(NodeRef)} to the
   * {@code beforeDeleteNode} policy for nodes carrying the category aspect.
   */
  @PostConstruct
  public void initialise() {
    // BeforeDeleteNodePolicy
    this.policyComponent.bindClassBehaviour(
      QName.createQName(NamespaceService.ALFRESCO_URI, "beforeDeleteNode"),
      CircabcModel.ASPECT_CATEGORY,
      new JavaBehaviour(this, "beforeDeleteNode")
    );
  }

  /**
   * Policy callback invoked just before a category node is deleted. It resolves
   * the category master group from the node's
   * {@link CircabcModel#PROP_CATEGORY_MASTER_GROUP} property, deletes that
   * authority (as administrator) and removes the CIRCABC category data.
   *
   * @param nodeRef the reference of the category node about to be deleted
   */
  @Override
  public void beforeDeleteNode(final NodeRef nodeRef) {
    String categoryMasterGroup =
      "GROUP_" +
      (String) nodeService.getProperty(
        nodeRef,
        CircabcModel.PROP_CATEGORY_MASTER_GROUP
      );
    deleteAuthorityAsAdmin(categoryMasterGroup);
    circabcService.deleteCategory(nodeRef);
  }

  /**
   * Deletes the given authority (and its cascaded members) while running as the
   * administrator user, so that the operation succeeds regardless of the
   * currently authenticated user's permissions.
   *
   * @param masterGroup the full authority name of the group to delete
   *                     (e.g. {@code GROUP_<name>})
   */
  public void deleteAuthorityAsAdmin(String masterGroup) {
    AuthenticationUtil.runAs(
      () -> {
        authorityService.deleteAuthority(masterGroup, true);
        return null;
      },
      AuthenticationUtil.getAdminUserName()
    );
  }
}
