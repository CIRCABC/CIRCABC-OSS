package io.swagger.model;

import java.util.*;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;

/**
 * Domain model (DTO) representing an access profile definition inside an Interest Group.
 *
 * <p>A profile groups together a set of permissions that can be assigned to members of an
 * Interest Group. Each profile is backed by an Alfresco authority (group) and maps node
 * types or resources to permission levels. Profiles may optionally be imported from, or
 * exported to, other profiles, which is tracked via the imported/exported flags and their
 * associated references.</p>
 *
 * <p>This class is used to serialize profile data to and from the REST API and provides
 * standard {@link #equals(Object)}, {@link #hashCode()} and {@link #toString()}
 * implementations based on its properties.</p>
 */
public class Profile {

  /** Alfresco node identifier (UUID) of the profile; empty when not yet persisted. */
  private String id = "";

  /** Machine-readable name of the profile. */
  private String name = null;

  /** Localized, human-readable title of the profile. */
  private I18nProperty title = new I18nProperty();

  /** Name of the underlying Alfresco authority (group) that backs this profile. */
  private String groupName = null;

  /** Mapping of resource/node type to the permission level granted by this profile. */
  private Map<String, String> permissions = new HashMap<>();

  /** Flag indicating whether this profile was imported from another profile. */
  private Boolean imported;

  /** Reference to the source profile this profile was imported from, if any. */
  private String importedRef;

  /** Flag indicating whether this profile is exported to one or more other profiles. */
  private Boolean exported;

  /** References to the target profiles this profile is exported to. */
  private List<String> exportedRefs = new ArrayList<>();

  /**
   * Get name
   *
   * @return name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the machine-readable name of the profile.
   *
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Get title
   *
   * @return title
   */
  public I18nProperty getTitle() {
    return title;
  }

  /**
   * Sets the localized title of the profile.
   *
   * @param title the title to set
   */
  public void setTitle(I18nProperty title) {
    this.title = title;
  }

  /**
   * Get permissions
   *
   * @return permissions
   */
  public Map<String, String> getPermissions() {
    return permissions;
  }

  /**
   * Sets the mapping of resource/node type to granted permission level.
   *
   * @param permissions the permissions to set
   */
  public void setPermissions(Map<String, String> permissions) {
    this.permissions = permissions;
  }

  /**
   * Compares this profile with another object for equality. Two profiles are equal when all
   * of their properties (name, title, permissions, imported/exported flags and references,
   * and group name) are equal.
   *
   * @param o the object to compare with
   * @return {@code true} if the given object is a {@code Profile} with equal properties,
   *     {@code false} otherwise
   */
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Profile profile = (Profile) o;
    return (
      Objects.equals(this.name, profile.name) &&
      Objects.equals(this.title, profile.title) &&
      Objects.equals(this.permissions, profile.permissions) &&
      Objects.equals(this.imported, profile.imported) &&
      Objects.equals(this.importedRef, profile.importedRef) &&
      Objects.equals(this.exported, profile.exported) &&
      Objects.equals(this.groupName, profile.groupName) &&
      Objects.equals(this.exportedRefs, profile.exportedRefs)
    );
  }

  /**
   * Computes a hash code consistent with {@link #equals(Object)}, based on all profile
   * properties.
   *
   * @return the hash code for this profile
   */
  @Override
  public int hashCode() {
    return Objects.hash(
      name,
      title,
      permissions,
      groupName,
      imported,
      importedRef,
      exported,
      exportedRefs
    );
  }

  /**
   * Returns a human-readable, multi-line string representation of this profile and its
   * properties.
   *
   * @return a string representation of this profile
   */
  @Override
  public String toString() {
    return (
      "class Profile {\n" +
      "    name: " +
      toIndentedString(name) +
      "\n" +
      "    title: " +
      toIndentedString(title) +
      "\n" +
      "    groupName: " +
      toIndentedString(groupName) +
      "\n" +
      "    permissions: " +
      toIndentedString(permissions) +
      "\n" +
      "    imported: " +
      toIndentedString(imported) +
      "\n" +
      "    importedRef: " +
      toIndentedString(importedRef) +
      "\n" +
      "    exported: " +
      toIndentedString(exported) +
      "\n" +
      "    exportedRefs: " +
      toIndentedString(exportedRefs) +
      "\n" +
      "}"
    );
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first line).
   *
   * @param o the object to convert
   * @return the indented string representation of the object
   */
  private String toIndentedString(java.lang.Object o) {
    return Util.toIndentedString(o);
  }

  /**
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * Builds the Alfresco {@link NodeRef} for this profile from its id, using the workspace
   * spaces store.
   *
   * @return the node reference pointing to this profile in the workspace spaces store
   */
  public NodeRef getNodeRef() {
    return new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, id);
  }

  /**
   * @param id the id to set
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * @return the imported
   */
  public Boolean getImported() {
    return imported;
  }

  /**
   * @param imported the imported to set
   */
  public void setImported(Boolean imported) {
    this.imported = imported;
  }

  /**
   * @return the exported
   */
  public Boolean getExported() {
    return exported;
  }

  /**
   * @param exported the exported to set
   */
  public void setExported(Boolean exported) {
    this.exported = exported;
  }

  /**
   * @return the name of the backing Alfresco authority (group)
   */
  public String getGroupName() {
    return groupName;
  }

  /**
   * @param groupName the group name to set
   */
  public void setGroupName(String groupName) {
    this.groupName = groupName;
  }

  /**
   * @return the reference to the source profile this profile was imported from
   */
  public String getImportedRef() {
    return importedRef;
  }

  /**
   * @param importedRef the imported reference to set
   */
  public void setImportedRef(String importedRef) {
    this.importedRef = importedRef;
  }

  /**
   * @return the exportedRefs
   */
  public List<String> getExportedRefs() {
    return exportedRefs;
  }

  /**
   * @param exportedRefs the exportedRefs to set
   */
  public void setExportedRefs(List<String> exportedRefs) {
    this.exportedRefs = exportedRefs;
  }
}
