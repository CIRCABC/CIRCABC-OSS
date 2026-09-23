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

import io.swagger.model.alfresco.DocumentModel;
import jakarta.annotation.PostConstruct;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Alfresco content-model behaviour bean for forum post and topic types.
 *
 * <p>This class registers Alfresco policy behaviours that are triggered whenever a forum node is
 * created in the repository. It enforces two CIRCABC-specific rules:
 *
 * <ul>
 *   <li>Every {@link ForumModel#TYPE_POST post} is made versionable (with auto-versioning) so that
 *       edits to posts are tracked.
 *   <li>Every {@link ForumModel#TYPE_TOPIC topic} receives the CIRCABC business-properties aspect
 *       with sensible defaults (no expiration date, normal security ranking).
 * </ul>
 *
 * <p>The behaviours are bound to the {@code onCreateNode} policy in {@link #init()} and are invoked
 * by the Alfresco policy framework, not directly by application code.
 *
 * <p>{@link ForumModel#TYPE_POST post type}
 *
 * @author Yanick Pignot
 */
public class PostType {

  /** Logger for this behaviour bean. */
  private static final Log logger = LogFactory.getLog(PostType.class);

  /** Alfresco policy component used to bind the class behaviours to node lifecycle events. */
  @Autowired
  private PolicyComponent policyComponent;

  /** Alfresco node service used to inspect nodes and add aspects/properties. */
  @Autowired
  private NodeService nodeService;

  /**
   * Registers the node-creation behaviours with the Alfresco policy component.
   *
   * <p>Invoked automatically after dependency injection ({@link PostConstruct}). It binds:
   *
   * <ul>
   *   <li>{@link #makeVersionnable(ChildAssociationRef)} to {@code onCreateNode} for
   *       {@link ForumModel#TYPE_POST}.
   *   <li>{@link #addBProperties(ChildAssociationRef)} to {@code onCreateNode} for
   *       {@link ForumModel#TYPE_TOPIC}.
   * </ul>
   */
  @PostConstruct
  public void init() {
    this.policyComponent.bindClassBehaviour(
      QName.createQName(NamespaceService.ALFRESCO_URI, "onCreateNode"),
      ForumModel.TYPE_POST,
      new JavaBehaviour(this, "makeVersionnable")
    );

    this.policyComponent.bindClassBehaviour(
      QName.createQName(NamespaceService.ALFRESCO_URI, "onCreateNode"),
      ForumModel.TYPE_TOPIC,
      new JavaBehaviour(this, "addBProperties")
    );
  }

  /**
   * Behaviour callback that makes a newly created forum post versionable.
   *
   * <p>Adds the {@link ContentModel#ASPECT_VERSIONABLE versionable aspect} with auto-versioning
   * enabled to the created post, unless the post lives in the archive store or already has the
   * aspect.
   *
   * @param childAssocRef the child association reference of the newly created post node; its child
   *     reference is the post being processed
   */
  public void makeVersionnable(final ChildAssociationRef childAssocRef) {
    final NodeRef postRef = childAssocRef.getChildRef();
    if (
      !isArchived(postRef) &&
      !nodeService.hasAspect(postRef, ContentModel.ASPECT_VERSIONABLE)
    ) {
      // add versionable aspect (set auto-version)
      final Map<QName, Serializable> versionProps = new HashMap<>();
      versionProps.put(ContentModel.PROP_AUTO_VERSION, true);
      nodeService.addAspect(
        postRef,
        ContentModel.ASPECT_VERSIONABLE,
        versionProps
      );

      if (logger.isInfoEnabled()) {
        logger.info("Add AutoVersionning on post:" + postRef);
      }
    }
  }

  /**
   * Behaviour callback that adds CIRCABC business properties to a newly created forum topic.
   *
   * <p>Adds the {@link DocumentModel#ASPECT_BPROPERTIES business-properties aspect} with default
   * values (no expiration date and a {@link DocumentModel#SECURITY_RANKINGS_NORMAL normal} security
   * ranking) to the created topic, unless the topic lives in the archive store or already has the
   * aspect.
   *
   * @param childAssocRef the child association reference of the newly created topic node; its child
   *     reference is the topic being processed
   */
  public void addBProperties(final ChildAssociationRef childAssocRef) {
    final NodeRef topicRef = childAssocRef.getChildRef();
    if (
      !isArchived(topicRef) &&
      !nodeService.hasAspect(topicRef, DocumentModel.ASPECT_BPROPERTIES)
    ) {
      final Map<QName, Serializable> bProps = HashMap.newHashMap(2);
      bProps.put(DocumentModel.PROP_EXPIRATION_DATE, null);
      // issue 4779 the default security ranking should be normal not public
      bProps.put(
        DocumentModel.PROP_SECURITY_RANKING,
        DocumentModel.SECURITY_RANKINGS_NORMAL
      );
      nodeService.addAspect(topicRef, DocumentModel.ASPECT_BPROPERTIES, bProps);

      if (logger.isInfoEnabled()) {
        logger.info(
          "addBPropertiesToTopics with properties: " +
            bProps +
            " on " +
            topicRef
        );
      }
    }
  }

  private boolean isArchived(final NodeRef nodeRef) {
    return StoreRef.STORE_REF_ARCHIVE_SPACESSTORE.equals(nodeRef.getStoreRef());
  }
}
