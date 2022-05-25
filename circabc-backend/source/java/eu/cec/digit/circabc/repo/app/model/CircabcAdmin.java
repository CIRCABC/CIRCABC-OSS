package eu.cec.digit.circabc.repo.app.model;

import eu.cec.digit.circabc.service.profile.CircabcRootProfileManagerService;

/**
 * Created by filipsl on 12/07/2017.
 */
public class CircabcAdmin {

    long userID;
    String name;

    public CircabcAdmin(long userID) {
        super();
        this.userID = userID;
        this.name = CircabcRootProfileManagerService.Profiles.CIRCABC_ADMIN;
    }

    public long getUserID() {
        return userID;
    }

    public void setUserID(long userID) {
        this.userID = userID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
