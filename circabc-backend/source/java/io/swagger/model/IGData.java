package io.swagger.model;

public class IGData {

    private String igName;
    private String igId;
    private String categoryId;

    public IGData(String igId, String igName, String categoryId) {
        super();
        this.igName = igName;
        this.igId = igId;
        this.categoryId = categoryId;
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
     * @return the categoryId
     */
    public String getCategoryId() {
        return categoryId;
    }

    /**
     * @param categoryId the categoryId to set
     */
    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }
}
