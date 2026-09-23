package io.swagger.model;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Read-only wrapper around an attachment referenced by an Alfresco
 * {@link NodeRef}.
 *
 * <p>Instances hold the essential presentation data for an attachment - its
 * name, the node it points to and whether it is a link or an actual file - and
 * expose localized, human-readable descriptions (type label and creation
 * status) resolved through {@link I18NUtil} message bundles. This is primarily
 * used to render attachment information in content-creation dialogs.
 */
public class AttachementWrapper {

  /** Message key for the "file" attachment type label. */
  private static final String MSG_TYPE_FILE =
    "create_content_dialog_attachement_type_file";

  /** Message key for the "link" attachment type label. */
  private static final String MSG_TYPE_LINK =
    "create_content_dialog_attachement_type_link";

  /** Message key for the attachment removal information text (currently unused). */
  @SuppressWarnings("unused")
  private static final String MSG_INFO_REMOVE =
    "create_content_dialog_attachement_remove_info";

  /** Message key for the localized "yes" label. */
  private static final String MSG_YES = "create_content_dialog_attachement_yes";

  /** Message key for the localized "no" label. */
  private static final String MSG_NO = "create_content_dialog_attachement_no";

  /** {@code true} when this attachment is a link, {@code false} when it is a file. */
  public final boolean isLink;

  /** The underlying attachment, or {@code null} when none has been created yet. */
  private final Attachement attachement;

  /** Reference to the Alfresco node representing the attachment. */
  private final NodeRef attachRef;

  /** Display name of the attachment. */
  private final String name;

  /**
   * Creates a wrapper for an attachment. The underlying {@link Attachement} is
   * left uninitialized ({@code null}), so {@link #isCreated()} will report
   * {@code false} for instances built with this constructor.
   *
   * @param attachRef reference to the Alfresco node representing the attachment
   * @param name display name of the attachment
   * @param isLink {@code true} if the attachment is a link, {@code false} if it
   *     is a file
   */
  public AttachementWrapper(
    final NodeRef attachRef,
    final String name,
    boolean isLink
  ) {
    super();
    this.name = name;
    this.attachRef = attachRef;
    this.attachement = null;
    this.isLink = isLink;
  }

  /**
   * Returns the display name of the attachment.
   *
   * @return the attachment name
   */
  public String getName() {
    return this.name;
  }

  /**
   * Returns the localized label describing the attachment type.
   *
   * @return the localized "link" label when {@link #isLink} is {@code true},
   *     otherwise the localized "file" label
   */
  public String getType() {
    if (isLink) {
      return I18NUtil.getMessage(MSG_TYPE_LINK);
    } else {
      return I18NUtil.getMessage(MSG_TYPE_FILE);
    }
  }

  /**
   * Indicates whether the underlying attachment has been created.
   *
   * @return {@code true} if an {@link Attachement} is present, {@code false}
   *     otherwise
   */
  public final boolean isCreated() {
    return attachement != null;
  }

  /**
   * Returns the localized creation status as a "yes"/"no" label.
   *
   * @return the localized "yes" label if the attachment is created, otherwise
   *     the localized "no" label
   */
  public final String getCreatedStr() {
    return translateBool(isCreated());
  }

  /**
   * Translates a boolean value into its localized "yes"/"no" label.
   *
   * @param bool the value to translate
   * @return the localized "yes" label when {@code true}, otherwise the
   *     localized "no" label
   */
  private String translateBool(final boolean bool) {
    if (bool) {
      return I18NUtil.getMessage(MSG_YES);
    } else {
      return I18NUtil.getMessage(MSG_NO);
    }
  }

  /**
   * Returns the reference to the Alfresco node representing the attachment.
   *
   * @return the attachment node reference
   */
  public final NodeRef getAttachRef() {
    return attachRef;
  }

  /**
   * Returns the underlying attachment.
   *
   * @return the attachement, or {@code null} if none has been set
   */
  protected final Attachement getAttachement() {
    return attachement;
  }

  /**
   * {@inheritDoc}
   *
   * @return a hash code computed from the node reference, attachment, link flag
   *     and name
   */
  @Override
  public int hashCode() {
    final int PRIME = 31;
    int result = 1;
    result = PRIME * result + ((attachRef == null) ? 0 : attachRef.hashCode());
    result =
      PRIME * result + ((attachement == null) ? 0 : attachement.hashCode());
    result = PRIME * result + (isLink ? 1231 : 1237);
    result = PRIME * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Two wrappers are equal when their node reference, attachment, link flag
   * and name are all equal.
   *
   * @param obj the object to compare with
   * @return {@code true} if this wrapper is equal to {@code obj}
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final AttachementWrapper other = (AttachementWrapper) obj;

    if (attachRef == null) {
      if (other.attachRef != null) {
        return false;
      }
    } else if (!attachRef.equals(other.attachRef)) {
      return false;
    }
    if (attachement == null) {
      if (other.attachement != null) {
        return false;
      }
    } else if (!attachement.equals(other.attachement)) {
      return false;
    }
    if (isLink != other.isLink) {
      return false;
    }
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equals(other.name)) {
      return false;
    }
    return true;
  }
}
