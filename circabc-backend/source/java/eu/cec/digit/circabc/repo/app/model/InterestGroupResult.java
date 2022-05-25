package eu.cec.digit.circabc.repo.app.model;

public class InterestGroupResult {

    Long memberId;
    String name;
    String title;
    Boolean isPublic;
    Boolean isRegistered;
    Boolean isApplyForMembership;
    String nodeRef;
    String titleTranslation;
    String lightDescTranslation;
    String logoRef;
    Long id;

    public InterestGroupResult() {
        super();
    }

    public InterestGroupResult(
            Long id,
            Long memberId,
            String name,
            String title,
            Boolean isPublic,
            Boolean isRegistered,
            Boolean isApplyForMembership,
            String nodeRef,
            String titleTranslation,
            String lightDescTranslation) {
        super();
        this.id = id;
        this.memberId = memberId;
        this.name = name;
        this.title = title;
        this.isPublic = isPublic;
        this.isRegistered = isRegistered;
        this.isApplyForMembership = isApplyForMembership;
        this.nodeRef = nodeRef;
        this.titleTranslation = titleTranslation;
        this.lightDescTranslation = lightDescTranslation;
    }

    public InterestGroupResult(
            Long id,
            Long memberId,
            String name,
            String title,
            Boolean isPublic,
            Boolean isRegistered,
            Boolean isApplyForMembership,
            String nodeRef,
            String titleTranslation,
            String lightDescTranslation,
            String logoRef) {
        super();
        this.id = id;
        this.memberId = memberId;
        this.name = name;
        this.title = title;
        this.isPublic = isPublic;
        this.isRegistered = isRegistered;
        this.isApplyForMembership = isApplyForMembership;
        this.nodeRef = nodeRef;
        this.titleTranslation = titleTranslation;
        this.lightDescTranslation = lightDescTranslation;
        this.logoRef = logoRef;
    }

    @Override
    public String toString() {
        return "InterestGroupResult [id="
                + id
                + ", memberId="
                + memberId
                + ", name="
                + name
                + ", title="
                + title
                + ", isPublic="
                + isPublic
                + ", isRegistered="
                + isRegistered
                + ", isApplyForMembership="
                + isApplyForMembership
                + ", nodeRef="
                + nodeRef
                + ", titleTranslation="
                + titleTranslation
                + ", lightDescTranslation="
                + lightDescTranslation
                + "]";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    public Boolean getIsRegistered() {
        return isRegistered;
    }

    public void setIsRegistered(Boolean isRegistered) {
        this.isRegistered = isRegistered;
    }

    public Boolean getIsApplyForMembership() {
        return isApplyForMembership;
    }

    public void setIsApplyForMembership(Boolean isApplyForMembership) {
        this.isApplyForMembership = isApplyForMembership;
    }

    public String getNodeRef() {
        return nodeRef;
    }

    public void setNodeRef(String nodeRef) {
        this.nodeRef = nodeRef;
    }

    public String getTitleTranslation() {
        return titleTranslation;
    }

    public void setTitleTranslation(String titleTranslation) {
        this.titleTranslation = titleTranslation;
    }

    public String getLightDescTranslation() {
        return lightDescTranslation;
    }

    public void setLightDescTranslation(String lightDescTranslation) {
        this.lightDescTranslation = lightDescTranslation;
    }

    public String getLogoRef() {
        return logoRef;
    }

    public void setLogoRef(String logoRef) {
        this.logoRef = logoRef;
    }
}
