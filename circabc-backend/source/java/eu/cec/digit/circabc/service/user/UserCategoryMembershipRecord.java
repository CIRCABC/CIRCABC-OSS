/**
 * ***************************************************************************** Copyright 2006
 * European Community
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
 * ****************************************************************************
 */
package eu.cec.digit.circabc.service.user;

import java.io.Serializable;

public class UserCategoryMembershipRecord implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 3300655181779771569L;
    /**
     *
     */
    private String category;

    private String profile;
    private String categoryNodeId;

    public UserCategoryMembershipRecord(String category, String profile, String categoryNodeId) {
        this.category = category;
        this.profile = profile;
        this.categoryNodeId = categoryNodeId;
    }

    /**
     * @return the profile
     */
    public String getProfile() {
        return profile;
    }

    /**
     * @param profile the profile to set
     */
    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategoryNodeId() {
        return categoryNodeId;
    }

    public void setCategoryNodeId(String categoryNodeId) {
        this.categoryNodeId = categoryNodeId;
    }
}
