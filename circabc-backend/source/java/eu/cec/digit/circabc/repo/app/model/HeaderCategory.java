package eu.cec.digit.circabc.repo.app.model;

public class HeaderCategory {

    private String headerNodeRef;
    private String headerName;
    private String headerTitle;
    private String categoryNodeRef;
    private String categoryName;
    private String categoryTitle;

    public HeaderCategory() {
    }

    public HeaderCategory(
            String headerNodeRef,
            String headerName,
            String headerTitle,
            String categoryNodeRef,
            String categoryName,
            String categoryTitle) {
        super();
        this.headerNodeRef = headerNodeRef;
        this.headerName = headerName;
        this.headerTitle = headerTitle;
        this.categoryNodeRef = categoryNodeRef;
        this.categoryName = categoryName;
        this.categoryTitle = categoryTitle;
    }

    public String getHeaderNodeRef() {
        return headerNodeRef;
    }

    public void setHeaderNodeRef(String headerNodeRef) {
        this.headerNodeRef = headerNodeRef;
    }

    public String getHeaderName() {
        return headerName;
    }

    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }

    public String getHeaderTitle() {
        return headerTitle;
    }

    public void setHeaderTitle(String headerTitle) {
        this.headerTitle = headerTitle;
    }

    public String getCategoryNodeRef() {
        return categoryNodeRef;
    }

    public void setCategoryNodeRef(String categoryNodeRef) {
        this.categoryNodeRef = categoryNodeRef;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryTitle() {
        return categoryTitle;
    }

    public void setCategoryTitle(String categoryTitle) {
        this.categoryTitle = categoryTitle;
    }
}
