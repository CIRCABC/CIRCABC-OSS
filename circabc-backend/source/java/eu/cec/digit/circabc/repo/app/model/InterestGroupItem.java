package eu.cec.digit.circabc.repo.app.model;

import org.alfresco.service.cmr.repository.NodeRef;

public class InterestGroupItem {

    private String bestTitle;
    private String name;
    private String id;
    private boolean member;
    private boolean registered;
    private boolean isPublic;
    private String logoRef;
    private Boolean needMoreDetails;
    private Boolean canJoin;
    private String noHtmlDescription;

    public InterestGroupItem(InterestGroupResult interestGroupResult) {
        if (interestGroupResult.getTitleTranslation() == null) {
            if (interestGroupResult.getTitle() == null) {
                bestTitle = interestGroupResult.getName();
            } else {
                bestTitle = interestGroupResult.getTitle();
            }
        } else {
            bestTitle = interestGroupResult.getTitleTranslation();
        }
        name = interestGroupResult.getName();
        id = new NodeRef(interestGroupResult.getNodeRef()).getId();
        needMoreDetails = false;
        noHtmlDescription = interestGroupResult.getLightDescTranslation();
        if (noHtmlDescription != null && noHtmlDescription.length() > 162) {
            needMoreDetails = true;
        }
        canJoin = interestGroupResult.isApplyForMembership;
        member = interestGroupResult.getMemberId() != null;
        registered = interestGroupResult.getIsRegistered();
        isPublic = interestGroupResult.getIsPublic();
        logoRef = interestGroupResult.getLogoRef();
    }

    public boolean isMember() {
        return member;
    }

    public boolean isRegistered() {
        return registered;
    }

    public String getBestTitle() {
        return bestTitle;
    }

    public void setBestTitle(String bestTitle) {
        this.bestTitle = bestTitle;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Boolean getNeedMoreDetails() {
        return needMoreDetails;
    }

    public void setNeedMoreDetails(Boolean needMoreDetails) {
        this.needMoreDetails = needMoreDetails;
    }

    public Boolean getCanJoin() {
        return canJoin;
    }

    public void setCanJoin(Boolean canJoin) {
        this.canJoin = canJoin;
    }

    public String getNoHtmlDescription() {
        return noHtmlDescription;
    }

    public void setNoHtmlDescription(String noHtmlDescription) {
        this.noHtmlDescription = noHtmlDescription;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    /**
     * @return the logoRef
     */
    public String getLogoRef() {
        return logoRef;
    }

    /**
     * @param logoRef the logoRef to set
     */
    public void setLogoRef(String logoRef) {
        this.logoRef = logoRef;
    }
}
