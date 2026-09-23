package io.swagger.model;

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

import java.io.Serializable;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Lightweight, immutable domain model representing a shared space (folder) item.
 *
 * <p>It couples the Alfresco {@link NodeRef} identifying the space with its
 * human-readable repository path. As an {@link AclAwareWrapper} it exposes its
 * underlying node reference so that access-control (ACL) information can be
 * resolved and attached when the item is returned by the REST layer.
 *
 * <p>Instances are serializable so they can be cached or transferred across
 * Alfresco service boundaries.
 *
 * @author Slobodan Filipovic
 */
public final class ShareSpaceItem implements Serializable, AclAwareWrapper {

  /**
   * Serialization version identifier used to guarantee compatibility between
   * serialized instances and this class definition.
   */
  private static final long serialVersionUID = 4555015582693128592L;

  /** Alfresco node reference uniquely identifying the shared space. */
  private NodeRef id;

  /** Repository path of the shared space. */
  private String path;

  /**
   * Creates a new shared space item.
   *
   * @param id the Alfresco node reference identifying the shared space
   * @param path the repository path of the shared space
   */
  public ShareSpaceItem(NodeRef id, String path) {
    this.id = id;
    this.path = path;
  }

  /**
   * Returns the Alfresco node reference identifying this shared space.
   *
   * @return the node reference of the shared space
   */
  public NodeRef getNodeRef() {
    return id;
  }

  /**
   * Returns the repository path of this shared space.
   *
   * @return the path of the shared space
   */
  public String getPath() {
    return path;
  }
}
