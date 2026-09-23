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
package eu.europa.ec.digit.circabc.rest.service;

import io.swagger.model.ShareSpaceItem;
import io.swagger.model.db.InterestGroupLinkItem;
import io.swagger.model.permissions.LibraryPermissions;
import java.util.List;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Business service contract for managing links and space sharing within the CIRCABC library.
 *
 * <p>Implementations are responsible for creating links that point to other repository nodes
 * (content or folder links), for creating shared-space links, and for managing the sharing of
 * library spaces between interest groups (applying and removing sharing with a given permission
 * level). It also exposes read operations to discover the shared spaces and the interest groups
 * that are eligible for sharing.
 *
 * @author Yanick Pignot
 */
public interface LinksBusinessSrv {
  /**
   * Add a space or content link to a given parent. The name will be unique and computed from the
   * target name.
   *
   * @param parent An existing parent
   * @param target An existing link target
   * @return the {@link NodeRef} of the newly created link node
   */
  NodeRef createLink(final NodeRef parent, final NodeRef target);

  /**
   * Add a share space link to the given parent. The name will be unique and computed from the
   * title.
   *
   * @param parent      An existing parent
   * @param target      An existing link target
   * @param title       A title (not empty) that will become the title
   * @param description An optional descption of the link
   * @return the {@link NodeRef} of the newly created shared-space link node
   */
  NodeRef createSharedSpaceLink(
    final NodeRef parent,
    final NodeRef target,
    final String title,
    final String description
  );

  /**
   * Share a space with an interest group by applying a gien permissions
   *
   * @param shareSpace        Any library space
   * @param interestGroup     Any interest group that is not shared yet with the space
   * @param libraryPermission A library permission
   */
  void applySharing(
    final NodeRef shareSpace,
    final NodeRef interestGroup,
    final LibraryPermissions libraryPermission
  );

  /**
   * Remove a sharing between a space and an interest group
   *
   * @param shareSpace    Any library space
   * @param interestGroup Any interest group that shared with the space
   */
  void removeSharing(final NodeRef shareSpace, final NodeRef interestGroup);

  /**
   * Return all available shared space for the given Interest group, Interest group child or
   * category.
   *
   * @param nodeRef Any nodeRef located at or under a category.
   * @return The list of available SharedSpace. Never null.
   */
  List<ShareSpaceItem> getAvailableSharedSpaces(final NodeRef nodeRef);

  /**
   * Return all shared space defined recursivly under any location.
   *
   * @param nodeRef Any nodeRef located at or under a category.
   * @return The list of available SharedSpace. Never null.
   */
  List<ShareSpaceItem> findSharedSpaces(final NodeRef nodeRef);

  /**
   * Return all interest group that are available for an invitation for sharing
   *
   * @param nodeRef Any library space
   * @return The list avaliable Interest Group. Never null.
   */
  List<InterestGroupLinkItem> getInterestGroupForSharing(final NodeRef nodeRef);
}
