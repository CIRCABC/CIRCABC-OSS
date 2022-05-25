/**
 *
 */
package io.swagger.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/** @author schwerr */
@XmlRootElement(name = "members")
public class BulkImportUsers {

    List<BulkImportUser> userData = null;

    /** @return the userData */
    public List<BulkImportUser> getUserData() {
        return userData;
    }

    /** @param userData the userData to set */
    @XmlElement(name = "member")
    public void setUserData(List<BulkImportUser> userData) {
        this.userData = userData;
    }
}
