package eu.cec.digit.circabc.repo.app.model;

import eu.cec.digit.circabc.service.profile.CategoryProfileManagerService;

public class CategoryAdmin {

    long userID;
    long categoryID;
    String name;

    public CategoryAdmin(long userID, long categoryID) {
        super();
        this.userID = userID;
        this.categoryID = categoryID;
        this.name = CategoryProfileManagerService.Profiles.CIRCA_CATEGORY_ADMIN;
    }

    public long getUserID() {
        return userID;
    }

    public void setUserID(long userID) {
        this.userID = userID;
    }

    public long getCategoryID() {
        return categoryID;
    }

    public void setCategoryID(long categoryID) {
        this.categoryID = categoryID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "CategoryAdmin [userID="
                + userID
                + ", categoryID="
                + categoryID
                + ", name="
                + name
                + "]";
    }
}
