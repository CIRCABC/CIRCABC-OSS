/**
 * Copyright 2006 European Community
 *
 * <p>Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 *
 * <p>https://joinup.ec.europa.eu/software/page/eupl
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
/**
 *
 */
package eu.cec.digit.circabc.service.user;

import eu.cec.digit.circabc.util.CircabcUserDataBean;
import org.alfresco.service.cmr.repository.NodeRef;

/** @author beaurpi */
public class BulkImportUserData {

    public static final String STATUS_OK = "ok";
    public static final String STATUS_ERROR = "nok";
    public static final String STATUS_IGNORE = "ignore";
    public static final String STATUS_ALREADY_MEMBER = "member";
    private Boolean selected = false;
    private NodeRef igRef;
    private String igName;
    private String departmentNumber;
    private String fromFile;
    private CircabcUserDataBean user;
    private String profile;
    private String status;
    /*
     * in some case, members inside one IG are not anymore in LDAP, because it is not synchronised
     * if one username does not match any LDAP user, at least we have the wanted username
     */
    private String expectedUsername;

    /** */
    public BulkImportUserData() {
    }

    /** @return the igRef */
    public NodeRef getIgRef() {
        return igRef;
    }

    /** @param igRef the igRef to set */
    public void setIgRef(NodeRef igRef) {
        this.igRef = igRef;
    }

    /** @return the profile */
    public String getProfile() {
        return profile;
    }

    /** @param profile the profile to set */
    public void setProfile(String profile) {
        this.profile = profile;
    }

    /** @return the user */
    public CircabcUserDataBean getUser() {
        return user;
    }

    /** @param user the user to set */
    public void setUser(CircabcUserDataBean user) {
        this.user = user;
    }

    /** @return the departmentNumber */
    public String getDepartmentNumber() {
        return departmentNumber;
    }

    /** @param departmentNumber the departmentNumber to set */
    public void setDepartmentNumber(String departmentNumber) {
        this.departmentNumber = departmentNumber;
    }

    /** @return the status */
    public String getStatus() {
        return status;
    }

    /** @param status the status to set */
    public void setStatus(String status) {
        this.status = status;
    }

    /** @return the fromFile */
    public String getFromFile() {
        return fromFile;
    }

    /** @param fromFile the fromFile to set */
    public void setFromFile(String fromFile) {
        this.fromFile = fromFile;
    }

    /** @return the igName */
    public String getIgName() {
        return igName;
    }

    /** @param igName the igName to set */
    public void setIgName(String igName) {
        this.igName = igName;
    }

    /** @return the selected */
    public Boolean getSelected() {
        return selected;
    }

    /** @param selected the selected to set */
    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

    /** @return the expectedUsername */
    public String getExpectedUsername() {
        return expectedUsername;
    }

    /** @param expectedUsername the expectedUsername to set */
    public void setExpectedUsername(String expectedUsername) {
        this.expectedUsername = expectedUsername;
    }
}
