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
package io.swagger.model.db;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Immutable value object representing a single Interest Group entry as exposed
 * through a "link" relationship (for example, an Interest Group that a user is
 * a member of or otherwise associated with).
 *
 * <p>Each instance bundles the identity of the Interest Group node together
 * with the human-readable metadata and the caller's effective permission on it.
 * The object is immutable: all fields are set at construction time and cannot be
 * changed afterwards.
 *
 * @author Slobodan Filipovic
 */
public final class InterestGroupLinkItem {

  /** Alfresco node reference uniquely identifying the Interest Group. */
  private final NodeRef id;

  /** Machine-readable (short) name of the Interest Group. */
  private final String name;

  /** Human-readable display title of the Interest Group. */
  private final String title;

  /** Effective permission the associated user holds on the Interest Group. */
  private final String permission;

  /**
   * Creates an immutable Interest Group link item.
   *
   * @param id the Alfresco node reference identifying the Interest Group
   * @param name the machine-readable (short) name of the Interest Group
   * @param permission the effective permission held on the Interest Group
   * @param title the human-readable display title of the Interest Group
   */
  public InterestGroupLinkItem(
    final NodeRef id,
    final String name,
    final String permission,
    final String title
  ) {
    this.id = id;
    this.name = name;
    this.permission = permission;
    this.title = title;
  }

  /**
   * Returns the Alfresco node reference identifying the Interest Group.
   *
   * @return the Interest Group node reference
   */
  public NodeRef getNodeRef() {
    return id;
  }

  /**
   * Returns the machine-readable (short) name of the Interest Group.
   *
   * @return the Interest Group name
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the effective permission held on the Interest Group.
   *
   * @return the permission value
   */
  public String getPermission() {
    return permission;
  }

  /**
   * Returns the human-readable display title of the Interest Group.
   *
   * @return the Interest Group title
   */
  public final String getTitle() {
    return title;
  }
}
