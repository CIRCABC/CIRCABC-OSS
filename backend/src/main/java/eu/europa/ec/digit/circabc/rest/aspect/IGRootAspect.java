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
 * Alfresco policy behaviour bound to the {@code circabc:igRoot} aspect
 * ({@link CircabcModel#ASPECT_IGROOT}), which marks the root node of an
 * Interest Group (IG).
 *
 * <p>This class implements {@link NodeServicePolicies.BeforeDeleteNodePolicy}
 * so that, whenever an IG root node is about to be deleted, the associated
 * Interest Group resources are cleaned up. Specifically, it removes the IG's
 * master authority group (running as the admin user) and delegates the
 * remaining Interest Group teardown to {@link CircabcService}.</p>
 *
 * <p>The behaviour is registered with the {@link PolicyComponent} at Spring
 * startup via {@link #initialise()}.</p>
 *
 * @author Philippe Dubois
 */

public class IGRootAspect
  implements NodeServicePolicies.BeforeDeleteNodePolicy
{

  /** Alfresco component used to bind this class as a policy behaviour. */
  @Autowired
  private PolicyComponent policyComponent;

  /** Provides access to node properties (e.g. the IG master group name). */
  @Autowired
  private NodeService nodeService;

  /** Used to delete the Interest Group's master authority group. */
  @Autowired
  private AuthorityService authorityService;

  /** CIRCABC service handling the higher-level Interest Group deletion logic. */
  @Autowired
  private CircabcService circabcService;

  /**
   * Spring initialise method used to register the policy behaviours.
   *
   * <p>Binds the {@link #beforeDeleteNode(NodeRef)} method to the Alfresco
   * {@code beforeDeleteNode} policy for nodes carrying the
   * {@link CircabcModel#ASPECT_IGROOT} aspect.</p>
   */
  @PostConstruct
  public void initialise() {
    // BeforeDeleteNodePolicy
    this.policyComponent.bindClassBehaviour(
      QName.createQName(NamespaceService.ALFRESCO_URI, "beforeDeleteNode"),
      CircabcModel.ASPECT_IGROOT,
      new JavaBehaviour(this, "beforeDeleteNode")
    );
  }

  /**
   * Policy callback invoked just before an IG root node is deleted.
   *
   * <p>Resolves the Interest Group's master authority group from the node's
   * {@link CircabcModel#PROP_IG_ROOT_MASTER_GROUP} property, deletes that
   * authority group, and then delegates the remaining Interest Group cleanup
   * to {@link CircabcService#deleteIntestGroup(NodeRef)}.</p>
   *
   * @param nodeRef reference to the IG root node that is about to be deleted
   */
  @Override
  public void beforeDeleteNode(final NodeRef nodeRef) {
    String interestGroupMasterGroup =
      "GROUP_" +
      (String) nodeService.getProperty(
        nodeRef,
        CircabcModel.PROP_IG_ROOT_MASTER_GROUP
      );
    deleteAuthorityAsAdmin(interestGroupMasterGroup);
    circabcService.deleteIntestGroup(nodeRef);
  }

  /**
   * Deletes the given authority group while running as the admin user.
   *
   * <p>The deletion is performed within an
   * {@link AuthenticationUtil#runAs} block so that it succeeds regardless of
   * the currently authenticated user's permissions. Any child authorities are
   * cascaded (the {@code true} flag on {@code deleteAuthority}).</p>
   *
   * @param masterGroup the fully qualified name of the authority group to
   *                     delete (e.g. {@code "GROUP_..."})
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
