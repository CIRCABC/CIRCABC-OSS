package eu.cec.digit.circabc.repo.report;

public class ShareSpaceQueryParameter {

    private Integer typeQnameId;
    private Integer propQnameId;
    private Integer storeID;
    private String igNodeRef;

    public ShareSpaceQueryParameter(
            Integer typeQnameId, Integer propQnameId, Integer storeID, String igNodeRef) {
        super();
        this.typeQnameId = typeQnameId;
        this.propQnameId = propQnameId;
        this.storeID = storeID;
        this.igNodeRef = igNodeRef;
    }

    public Integer getTypeQnameId() {
        return typeQnameId;
    }

    public void setTypeQnameId(Integer typeQnameId) {
        this.typeQnameId = typeQnameId;
    }

    public Integer getPropQnameId() {
        return propQnameId;
    }

    public void setPropQnameId(Integer propQnameId) {
        this.propQnameId = propQnameId;
    }

    public Integer getStoreID() {
        return storeID;
    }

    public void setStoreID(Integer storeID) {
        this.storeID = storeID;
    }

    public String getIgNodeRef() {
        return igNodeRef;
    }

    public void setIgNodeRef(String igNodeRef) {
        this.igNodeRef = igNodeRef;
    }
}
