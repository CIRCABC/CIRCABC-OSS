package eu.europa.ec.digit.circabc.rest.service.helper;

import io.swagger.model.alfresco.CircabcModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Helper responsible for managing CIRCABC-specific Alfresco aspects on repository nodes.
 *
 * <p>This service centralises the logic for attaching the service-type aspects
 * (Library, Newsgroup, Event, Information) together with the shared CIRCABC management
 * aspect, and for checking whether a node already carries a given service aspect. Aspects
 * are only added when missing, making the mutating operations idempotent.
 */
@SuppressWarnings("java:S2178")
public class AspectManager {

  /** Alfresco node service used to read and mutate the aspects applied to nodes. */
  @Autowired
  private NodeService nodeService;

  /**
   * Ensures the given node carries the Library service aspects.
   *
   * <p>Adds both the CIRCABC management aspect and the Library aspect when they are not
   * already present on the node.
   *
   * @param nodeRef reference to the node to update
   * @return {@code true} if at least one aspect was added, {@code false} if the node
   *     already carried all required aspects
   */
  public boolean addLibraryAspect(final NodeRef nodeRef) {
    boolean result = false;
    result = addAspectIfMissing(
      nodeRef,
      CircabcModel.ASPECT_CIRCABC_MANAGEMENT
    );
    result = result | addAspectIfMissing(nodeRef, CircabcModel.ASPECT_LIBRARY);
    return result;
  }

  /**
   * Ensures the given node carries the Newsgroup service aspects.
   *
   * <p>Adds both the CIRCABC management aspect and the Newsgroup aspect when they are not
   * already present on the node.
   *
   * @param nodeRef reference to the node to update
   * @return {@code true} if at least one aspect was added, {@code false} if the node
   *     already carried all required aspects
   */
  public boolean addNewsgroupAspect(final NodeRef nodeRef) {
    boolean result = false;
    result = addAspectIfMissing(
      nodeRef,
      CircabcModel.ASPECT_CIRCABC_MANAGEMENT
    );
    result =
      result | addAspectIfMissing(nodeRef, CircabcModel.ASPECT_NEWSGROUP);
    return result;
  }

  /**
   * Ensures the given node carries the Event service aspects.
   *
   * <p>Adds both the CIRCABC management aspect and the Event aspect when they are not
   * already present on the node.
   *
   * @param nodeRef reference to the node to update
   * @return {@code true} if at least one aspect was added, {@code false} if the node
   *     already carried all required aspects
   */
  public boolean addEventAspect(final NodeRef nodeRef) {
    boolean result = false;
    result = addAspectIfMissing(
      nodeRef,
      CircabcModel.ASPECT_CIRCABC_MANAGEMENT
    );
    result = result | addAspectIfMissing(nodeRef, CircabcModel.ASPECT_EVENT);
    return result;
  }

  /**
   * Ensures the given node carries the Information service aspects.
   *
   * <p>Adds both the CIRCABC management aspect and the Information aspect when they are not
   * already present on the node.
   *
   * @param nodeRef reference to the node to update
   * @return {@code true} if at least one aspect was added, {@code false} if the node
   *     already carried all required aspects
   */
  public boolean addInformationAspect(final NodeRef nodeRef) {
    boolean result = false;
    result = addAspectIfMissing(
      nodeRef,
      CircabcModel.ASPECT_CIRCABC_MANAGEMENT
    );
    result =
      result | addAspectIfMissing(nodeRef, CircabcModel.ASPECT_INFORMATION);
    return result;
  }

  /**
   * Adds the given aspect to the node only if it is not already present.
   *
   * @param nodeRef reference to the node to update
   * @param aspectQname qualified name of the aspect to add
   * @return {@code true} if the aspect was added, {@code false} if it was already present
   */
  private boolean addAspectIfMissing(final NodeRef nodeRef, QName aspectQname) {
    boolean result = false;

    if (!nodeService.hasAspect(nodeRef, aspectQname)) {
      nodeService.addAspect(nodeRef, aspectQname, null);
      result = true;
    }
    return result;
  }

  /**
   * Checks whether the given node carries the Library aspect.
   *
   * @param nodeRef reference to the node to inspect
   * @return {@code true} if the node has the Library aspect, {@code false} otherwise
   */
  public boolean isLibraryNode(NodeRef nodeRef) {
    return nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_LIBRARY);
  }

  /**
   * Checks whether the given node carries the Newsgroup aspect.
   *
   * @param nodeRef reference to the node to inspect
   * @return {@code true} if the node has the Newsgroup aspect, {@code false} otherwise
   */
  public boolean isNewsgroupNode(NodeRef nodeRef) {
    return nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_NEWSGROUP);
  }
}
