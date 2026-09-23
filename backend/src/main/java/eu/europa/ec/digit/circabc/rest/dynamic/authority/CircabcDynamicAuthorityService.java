package eu.europa.ec.digit.circabc.rest.dynamic.authority;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Enumerates the CIRCABC service areas that a repository node may belong to.
 *
 * <p>A CIRCABC Interest Group exposes several collaborative services, and the
 * dynamic authority resolution needs to know which service a given node lives
 * under in order to evaluate the correct permissions.
 */
enum CircabcServiceType {
  /** The document management service (folders and documents). */
  LIBRARY,
  /** The discussion service (newsgroups, topics and posts). */
  NEWSGROUP,
  /** The information / static content service. */
  INFORMATION,
  /** The node does not belong to a recognised CIRCABC service. */
  UNKNOWN,
}

/**
 * Resolves CIRCABC-specific dynamic authorities and group membership for
 * repository nodes.
 *
 * <p>Alfresco supports dynamic authorities: pseudo-groups whose membership is
 * computed at evaluation time rather than stored statically. This service
 * provides the domain logic used by such authorities to decide, for a given
 * node, which CIRCABC group governs it, which service area it belongs to, and
 * whether a particular user has membership or administrative rights over it.
 *
 * <p>Implementations are typically consulted while ACS evaluates node
 * permissions, so the methods are expected to be efficient and side-effect
 * free.
 */
public interface CircabcDynamicAuthorityService {
  /**
   * Determines whether the given node is part of the CIRCABC content
   * hierarchy.
   *
   * @param nodeRef the node to inspect
   * @return {@code true} if the node belongs to a CIRCABC structure,
   *     {@code false} otherwise
   */
  boolean isCircabcNode(NodeRef nodeRef);

  /**
   * Locates the CIRCABC group node that governs the given node.
   *
   * @param nodeRef the node whose owning group should be resolved
   * @return the {@link NodeRef} of the governing group, or {@code null} if no
   *     group can be resolved for the node
   */
  NodeRef findGroup(NodeRef nodeRef);

  /**
   * Determines which CIRCABC service area the given node belongs to.
   *
   * @param nodeRef the node to classify
   * @return the {@link CircabcServiceType} of the node, or
   *     {@link CircabcServiceType#UNKNOWN} if it cannot be determined
   */
  CircabcServiceType findServiceType(NodeRef nodeRef);

  /**
   * Checks whether the given user is a member of the specified CIRCABC group.
   *
   * @param group the CIRCABC group node
   * @param userName the user name to check
   * @return {@code true} if the user is a member of the group, {@code false}
   *     otherwise
   */
  boolean isGroupMember(NodeRef group, String userName);

  /**
   * Checks whether the given user is an administrator of the category that
   * contains the specified group.
   *
   * @param group the CIRCABC group node whose category is inspected
   * @param userName the user name to check
   * @return {@code true} if the user is a category administrator, {@code false}
   *     otherwise
   */
  boolean isCategoryAdmin(NodeRef group, String userName);

  /**
   * Checks whether the given user has administrative rights over the specified
   * group for a particular service area.
   *
   * @param group the CIRCABC group node
   * @param userName the user name to check
   * @param serviceType the service area for which administrative rights are
   *     evaluated
   * @return {@code true} if the user is an administrator for the given group
   *     and service, {@code false} otherwise
   */
  boolean isAdmin(
    NodeRef group,
    String userName,
    CircabcServiceType serviceType
  );
}
