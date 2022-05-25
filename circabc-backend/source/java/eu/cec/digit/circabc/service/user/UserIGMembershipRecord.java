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

public class UserIGMembershipRecord implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -8706766125898983357L;

    private String category;
    private String categoryNodeId;
    private String interestGroup;
    private String interestGroupNodeId;
    private String interestGroupTitle;
    private String profile;
    private String profileNodeRefId;
    private String categoryTitle;
    private String profileTitle;
    private String alfrescoGroup;

    public UserIGMembershipRecord(
            String interestGroupNodeId,
            String interesGroup,
            String categoryNodeId,
            String category,
            String profile,
            String categoryTitle,
            String interesGroupTitle,
            String profileTitle) {
        this.interestGroupNodeId = interestGroupNodeId;
        this.category = category;
        this.categoryNodeId = categoryNodeId;
        this.interestGroup = interesGroup;
        this.profile = profile;
        this.categoryTitle = bestTitleOrName(categoryTitle, category);
        this.interestGroupTitle = bestTitleOrName(interesGroupTitle, interesGroup);
        this.profileTitle = bestTitleOrName(profileTitle, profile);
    }

    public UserIGMembershipRecord(
            String interestGroupNodeId,
            String interesGroup,
            String categoryNodeId,
            String category,
            String profile,
            String categoryTitle,
            String interesGroupTitle,
            String profileTitle,
            String alfrescoGroup) {
        this.interestGroupNodeId = interestGroupNodeId;
        this.category = category;
        this.categoryNodeId = categoryNodeId;
        this.interestGroup = interesGroup;
        this.profile = profile;
        this.categoryTitle = bestTitleOrName(categoryTitle, category);
        this.interestGroupTitle = bestTitleOrName(interesGroupTitle, interesGroup);
        this.profileTitle = bestTitleOrName(profileTitle, profile);
        this.alfrescoGroup = alfrescoGroup;
    }

    public UserIGMembershipRecord(
            String interestGroupNodeId,
            String interesGroup,
            String categoryNodeId,
            String category,
            String profile,
            String categoryTitle,
            String interesGroupTitle,
            String profileTitle,
            String alfrescoGroup,
            String profileNodeRefId) {
        this.interestGroupNodeId = interestGroupNodeId;
        this.category = category;
        this.categoryNodeId = categoryNodeId;
        this.interestGroup = interesGroup;
        this.profile = profile;
        this.categoryTitle = bestTitleOrName(categoryTitle, category);
        this.interestGroupTitle = bestTitleOrName(interesGroupTitle, interesGroup);
        this.profileTitle = bestTitleOrName(profileTitle, profile);
        this.alfrescoGroup = alfrescoGroup;
        this.setprofileNodeRefId(profileNodeRefId);
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

    public String getInterestGroup() {
        return interestGroup;
    }

    public void setInterestGroup(String interesGroup) {
        this.interestGroup = interesGroup;
    }

    public String getInterestGroupNodeId() {
        return interestGroupNodeId;
    }

    public void setInterestGroupNodeId(String interestGroupNodeId) {
        this.interestGroupNodeId = interestGroupNodeId;
    }

    /**
     * @return the categoryTitle
     */
    public final String getCategoryTitle() {
        return categoryTitle;
    }

    /**
     * @param categoryTitle the categoryTitle to set
     */
    public final void setCategoryTitle(String categoryTitle) {
        this.categoryTitle = categoryTitle;
    }

    /**
     * @return the interestGroupTitle
     */
    public final String getInterestGroupTitle() {
        return interestGroupTitle;
    }

    /**
     * @param interestGroupTitle the interestGroupTitle to set
     */
    public final void setInterestGroupTitle(String interestGroupTitle) {
        this.interestGroupTitle = interestGroupTitle;
    }

    /**
     * @return the profileTitle
     */
    public final String getProfileTitle() {
        return profileTitle;
    }

    /**
     * @param profileTitle the profileTitle to set
     */
    public final void setProfileTitle(String profileTitle) {
        this.profileTitle = profileTitle;
    }

    private String bestTitleOrName(final String title, final String name) {
        if (title == null || title.trim().length() < 1) {
            return name;
        } else {
            return title;
        }
    }

    public String getCategoryNodeId() {
        return categoryNodeId;
    }

    public void setCategoryNodeId(String categoryNodeId) {
        this.categoryNodeId = categoryNodeId;
    }

    public boolean isImported() {
        if (profileTitle == null) {
            return false;
        } else {
            return profileTitle.contains(":");
        }
    }

    public String getAlfrescoGroup() {
        return alfrescoGroup;
    }

    public void setAlfrescoGroup(String alfrescoGroup) {
        this.alfrescoGroup = alfrescoGroup;
    }

    public String getprofileNodeRefId() {
        return profileNodeRefId;
    }

    public void setprofileNodeRefId(String profileNodeRefId) {
        this.profileNodeRefId = profileNodeRefId;
    }
}
