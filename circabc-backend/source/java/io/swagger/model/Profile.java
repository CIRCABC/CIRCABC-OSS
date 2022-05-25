package io.swagger.model;

import java.util.*;

/**
 * the representation of an access profile definition inside an Interest Group
 */
public class Profile {

    private String id = "";

    private String name = null;

    private I18nProperty title = new I18nProperty();

    private String groupName = null;

    private Map<String, String> permissions = new HashMap<>();

    private Boolean imported;

    private String importedRef;

    private Boolean exported;

    private List<String> exportedRefs = new ArrayList<>();

    /**
     * Get name
     *
     * @return name
     */
    public String getName() {
        return name;
    }

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

    public void setPermissions(Map<String, String> permissions) {
        this.permissions = permissions;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Profile profile = (Profile) o;
        return Objects.equals(this.name, profile.name)
                && Objects.equals(this.title, profile.title)
                && Objects.equals(this.permissions, profile.permissions)
                && Objects.equals(this.imported, profile.imported)
                && Objects.equals(this.importedRef, profile.importedRef)
                && Objects.equals(this.exported, profile.exported)
                && Objects.equals(this.groupName, profile.groupName)
                && Objects.equals(this.exportedRefs, profile.exportedRefs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                name, title, permissions, groupName, imported, importedRef, exported, exportedRefs);
    }

    @Override
    public String toString() {

        return "class Profile {\n"
                + "    name: "
                + toIndentedString(name)
                + "\n"
                + "    title: "
                + toIndentedString(title)
                + "\n"
                + "    groupName: "
                + toIndentedString(groupName)
                + "\n"
                + "    permissions: "
                + toIndentedString(permissions)
                + "\n"
                + "    imported: "
                + toIndentedString(imported)
                + "\n"
                + "    importedRef: "
                + toIndentedString(importedRef)
                + "\n"
                + "    exported: "
                + toIndentedString(exported)
                + "\n"
                + "    exportedRefs: "
                + toIndentedString(exportedRefs)
                + "\n"
                + "}";
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces (except the first line).
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

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getImportedRef() {
        return importedRef;
    }

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
