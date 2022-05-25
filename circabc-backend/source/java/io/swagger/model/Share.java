package io.swagger.model;

public class Share {

    String igId;
    String igName;
    String permission;

    public Share(String igId, String igName, String permission) {
        super();
        this.igId = igId;
        this.igName = igName;
        this.permission = permission;
    }

    /**
     * @return the igId
     */
    public String getIgId() {
        return igId;
    }

    /**
     * @param igId the igId to set
     */
    public void setIgId(String igId) {
        this.igId = igId;
    }

    /**
     * @return the igName
     */
    public String getIgName() {
        return igName;
    }

    /**
     * @param igName the igName to set
     */
    public void setIgName(String igName) {
        this.igName = igName;
    }

    /**
     * @return the permission
     */
    public String getPermission() {
        return permission;
    }

    /**
     * @param permission the permission to set
     */
    public void setPermission(String permission) {
        this.permission = permission;
    }
}
