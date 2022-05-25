/**
 *
 */
package eu.cec.digit.circabc.repo.log;

import java.util.Date;

/** @author beaurpi */
public class UserActionLogDAO {

    private String documentId;
    private String igId;
    private String action;
    private Date logDate;
    private String username;

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getIgId() {
        return igId;
    }

    public void setIgId(String igId) {
        this.igId = igId;
    }

    public Date getLogDate() {
        return logDate;
    }

    public void setLogDate(Date logDate) {
        this.logDate = logDate;
    }

    /** @return the action */
    public String getAction() {
        return action;
    }

    /** @param action the action to set */
    public void setAction(String action) {
        this.action = action;
    }

    /** @return the username */
    public String getUsername() {
        return username;
    }

    /** @param username the username to set */
    public void setUsername(String username) {
        this.username = username;
    }
}
