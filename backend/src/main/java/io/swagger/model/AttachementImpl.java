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

package io.swagger.model;

import io.swagger.model.alfresco.DocumentModel;
import java.io.Serializable;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

/**
 * Default implementation of the {@link Attachement} wrapper.
 *
 * <p>An {@code AttachementImpl} captures the metadata of a piece of Alfresco content that is
 * either linked from, or hidden under, another node. On construction it reads the referred node's
 * properties (name and title) and inspects its content type to derive the {@link AttachementType}:
 * a {@link AttachementType#HIDDEN_FILE} when the node is a hidden attachement content type, or a
 * {@link AttachementType#REPO_LINK} otherwise. Content-related metadata (size, mimetype, encoding)
 * defaults to empty/zero values and can be populated afterwards through the corresponding setters.
 *
 * <p>Instances are {@link Serializable} so they can be carried through the web layer.
 *
 * @author Yanick Pignot
 */
public class AttachementImpl implements Attachement, Serializable {

  /** The node that owns (is attached to) this attachement. */
  private final NodeRef referer;

  /** The referred content node this attachement points to and evaluates permissions against. */
  private final NodeRef refered;

  /** How the attachement is stored (repository link or hidden file), derived from the node type. */
  private final AttachementType type;

  /** The {@code cm:name} of the referred node. */
  private final String name;

  /** The {@code cm:title} of the referred node; may be {@code null} or empty. */
  private final String title;

  /** The content size in bytes; defaults to {@code 0} until explicitly set. */
  private long size = 0;

  /** The content mimetype; defaults to an empty string until explicitly set. */
  private String mimetype = "";

  /** The content encoding; defaults to an empty string until explicitly set. */
  private String encoding = "";

  /**
   * Builds an attachement wrapper for the given referred node, reading its properties and type via
   * the supplied {@link NodeService}.
   *
   * @param referer the node that owns this attachement
   * @param refered the referred content node whose metadata and type are read
   * @param nodeService the Alfresco node service used to load properties and resolve the node type
   */
  public AttachementImpl(
    final NodeRef referer,
    final NodeRef refered,
    final NodeService nodeService
  ) {
    super();
    this.referer = referer;
    this.refered = refered;

    final Map<QName, Serializable> props = nodeService.getProperties(refered);
    name = (String) props.get(ContentModel.PROP_NAME);
    title = (String) props.get(ContentModel.PROP_TITLE);

    final QName nodeType = nodeService.getType(refered);

    if (DocumentModel.TYPE_HIDDEN_ATTACHEMENT_CONTENT.equals(nodeType)) {
      type = AttachementType.HIDDEN_FILE;
    } else {
      type = AttachementType.REPO_LINK;
    }
  }

  /**
   * Get the type of the attachement.
   *
   * @return the {@link AttachementType} describing how the attachement is stored
   */
  public AttachementType geType() {
    return type;
  }

  /**
   * Get the node on which the attachement is attached.
   *
   * @return the {@link NodeRef} of the node that owns this attachement
   */
  public NodeRef getAttachedOn() {
    return referer;
  }

  /**
   * Get the name of the attachement node.
   *
   * @return the attachement name
   */
  public String getName() {
    return name;
  }

  /**
   * Get the {@link NodeRef} used for ACL evaluation on this wrapper.
   *
   * @return the {@link NodeRef} of the referred attachement content
   */
  public NodeRef getNodeRef() {
    return refered;
  }

  /**
   * Get the title of the attachement, falling back to the name when no title is set.
   *
   * @return the title if present and non-empty, otherwise the attachement name
   */
  public String getTitle() {
    if (title != null && !title.isEmpty()) {
      return title;
    } else {
      return getName();
    }
  }

  /**
   * @return the size
   */
  public long getSize() {
    return size;
  }

  /**
   * @param size the size to set
   */
  public void setSize(long size) {
    this.size = size;
  }

  /**
   * @return the mimetype
   */
  public String getMimetype() {
    return mimetype;
  }

  /**
   * @param mimetype the mimetype to set
   */
  public void setMimetype(String mimetype) {
    this.mimetype = mimetype;
  }

  /**
   * @return the encoding
   */
  public String getEncoding() {
    return encoding;
  }

  /**
   * @param encoding the encoding to set
   */
  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }
}
