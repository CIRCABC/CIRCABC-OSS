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

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Wrapper contract describing an attachement associated with a repository node.
 *
 * <p>An attachement exposes metadata (name, title, type, size, mimetype, encoding) about a piece of
 * content that is either linked from, or hidden under, another Alfresco node. As an {@link
 * AclAwareWrapper} it also carries the {@link NodeRef} required to perform permission checks on the
 * underlying content.
 *
 * @author Yanick Pignot
 */
public interface Attachement extends AclAwareWrapper {
  /**
   * AclAware wrapper method contract to allow permission check on this wrapper.
   *
   * @return the {@link NodeRef} of the attachement content used for ACL evaluation
   * @see AclAwareWrapper#getNodeRef()
   */
  NodeRef getNodeRef();

  /**
   * Get the node on which the attachement is attached.
   *
   * @return the {@link NodeRef} of the node that owns this attachement
   */
  NodeRef getAttachedOn();

  /**
   * Get the name of the attachement node.
   *
   * @return the attachement name
   */
  String getName();

  /**
   * Get the title of the attachement node.
   *
   * @return the attachement title
   */
  String getTitle();

  /**
   * Get the type of the attachement.
   *
   * @return the {@link AttachementType} describing how the attachement is stored
   */
  AttachementType geType();

  /**
   * Get the size of the attachement content.
   *
   * @return the content size in bytes
   */
  long getSize();

  /**
   * Set the size of the attachement content.
   *
   * @param size the content size in bytes
   */
  void setSize(long size);

  /**
   * Get the MIME type of the attachement content.
   *
   * @return the content MIME type
   */
  String getMimetype();

  /**
   * Set the MIME type of the attachement content.
   *
   * @param mimetype the content MIME type
   */
  void setMimetype(String mimetype);

  /**
   * Get the character encoding of the attachement content.
   *
   * @return the content encoding
   */
  String getEncoding();

  /**
   * Set the character encoding of the attachement content.
   *
   * @param encoding the content encoding
   */
  void setEncoding(String encoding);

  /**
   * Represent the kind of attachement.
   *
   * <p>An attachement can be either - a link in the repository. - a content hidden under the node
   * where it is attached.
   *
   * @author Yanick Pignot
   */
  enum AttachementType {
    /** The attachement is a link to another node within the repository. */
    REPO_LINK,

    /** The attachement is content hidden under the node it is attached to. */
    HIDDEN_FILE,
  }
}
