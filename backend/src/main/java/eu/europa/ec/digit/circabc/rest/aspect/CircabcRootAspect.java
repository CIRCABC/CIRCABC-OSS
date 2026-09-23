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
import java.io.Serializable;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies.BeforeDeleteNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Alfresco policy behaviour bound to the {@code circabc:circabcRoot} aspect
 * ({@link CircabcModel#ASPECT_CIRCABC_ROOT}) that guards the single CircaBC
 * root node of the repository.
 *
 * <p>This class is not a REST endpoint; it is a Spring-managed bean that
 * registers itself against the Alfresco {@link PolicyComponent} to react to
 * repository events on nodes carrying the CircaBC root aspect. It enforces two
 * invariants:
 *
 * <ul>
 *   <li>{@link OnUpdatePropertiesPolicy}: the root node must always keep the
 *       name {@code "CircaBC"}.</li>
 *   <li>{@link BeforeDeleteNodePolicy}: deleting the root node also removes its
 *       associated master authority group and cascades a full CircaBC cleanup.</li>
 * </ul>
 *
 * @author Clinckart Stephane
 */

public class CircabcRootAspect
  implements BeforeDeleteNodePolicy, OnUpdatePropertiesPolicy
{

  /** Logger for reporting failures raised while handling policy callbacks. */
  private static final Log logger = LogFactory.getLog(CircabcRootAspect.class);

  /** Service used to remove the CircaBC master authority group on deletion. */
  @Autowired
  private AuthorityService authorityService;

  /** Repository node service used to read node properties. */
  @Autowired
  private NodeService nodeService;

  /** Alfresco policy component used to register the behaviours in {@link #init()}. */
  @Autowired
  private PolicyComponent policyComponent;

  /** CircaBC application service used to perform the global cleanup on root deletion. */
  @Autowired
  private CircabcService circabcService;

  /**
   * Enforces that the CircaBC root node keeps its mandatory name.
   *
   * <p>Invoked by the Alfresco {@code onUpdateProperties} policy whenever the
   * properties of a node bearing the CircaBC root aspect change. If the updated
   * {@code cm:name} property is present and differs from {@code "CircaBC"}, the
   * update is rejected.
   *
   * @param arg0 the node reference whose properties are being updated
   * @param before the property values before the update
   * @param after the property values after the update; may be {@code null}
   * @throws IllegalArgumentException if the new name is set to a value other
   *     than {@code "CircaBC"}
   */
  public void onUpdateProperties(
    final NodeRef arg0,
    final Map<QName, Serializable> before,
    final Map<QName, Serializable> after
  ) {
    final Serializable newName = (after != null)
      ? after.get(ContentModel.PROP_NAME)
      : null;

    if (newName != null && !newName.toString().equals("CircaBC")) {
      throw new IllegalArgumentException(
        "The name of this folder must be CircaBC"
      );
    }
  }

  /**
   * Registers this bean's policy behaviours with the Alfresco
   * {@link PolicyComponent} once Spring has finished dependency injection.
   *
   * <p>Binds both the {@code beforeDeleteNode} and {@code onUpdateProperties}
   * behaviours to the CircaBC root aspect
   * ({@link CircabcModel#ASPECT_CIRCABC_ROOT}).
   */

  @PostConstruct
  public void init() {
    // Register the policy behaviors

    // BeforeDeleteNodePolicy
    this.policyComponent.bindClassBehaviour(
      QName.createQName(NamespaceService.ALFRESCO_URI, "beforeDeleteNode"),
      CircabcModel.ASPECT_CIRCABC_ROOT,
      new JavaBehaviour(this, "beforeDeleteNode")
    );

    // on property change policy
    this.policyComponent.bindClassBehaviour(
      QName.createQName(NamespaceService.ALFRESCO_URI, "onUpdateProperties"),
      CircabcModel.ASPECT_CIRCABC_ROOT,
      new JavaBehaviour(this, "onUpdateProperties")
    );
  }

  /**
   * Cleans up CircaBC-wide resources before the root node is deleted.
   *
   * <p>Invoked by the Alfresco {@code beforeDeleteNode} policy. It resolves the
   * master authority group from the {@code circabcMasterGroup} property of the
   * node, deletes that group (cascading to its members), and triggers a full
   * CircaBC cleanup via {@link CircabcService#deleteAll()}. Any exception raised
   * during the process is caught and logged so that the node deletion is not
   * blocked.
   *
   * @param nodeRef the CircaBC root node about to be deleted
   */
  public void beforeDeleteNode(final NodeRef nodeRef) {
    try {
      String circabcMasterGroup =
        "GROUP_" +
        (String) nodeService.getProperty(
          nodeRef,
          CircabcModel.CIRCA_BC_MASTER_GROUP_PROPERTY
        );
      authorityService.deleteAuthority(circabcMasterGroup, true);
      circabcService.deleteAll();
    } catch (Exception e) {
      logger.error("Error while deleting CircaBC root", e);
    }
  }
}
