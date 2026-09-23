package io.swagger.model.db;

/**
 * Simple data holder bundling the parameters required to query "shared spaces"
 * for a given Interest Group directly against the Alfresco database.
 *
 * <p>The identifiers it carries reference internal Alfresco metadata: a type
 * QName id, a property QName id and a store id, together with the node
 * reference of the Interest Group whose shared spaces are being resolved. It
 * contains no business logic and is used to pass these values between the
 * persistence layer and its callers.
 */
public class ShareSpaceQueryParameter {

  /** Alfresco QName id identifying the content type to match. */
  private Integer typeQnameId;
  /** Alfresco QName id identifying the property to match. */
  private Integer propQnameId;
  /** Id of the Alfresco store the nodes belong to. */
  private Integer storeID;
  /** Node reference of the Interest Group whose shared spaces are queried. */
  private String igNodeRef;

  /**
   * Creates a fully populated query parameter holder.
   *
   * @param typeQnameId the Alfresco QName id of the content type to match
   * @param propQnameId the Alfresco QName id of the property to match
   * @param storeID the id of the Alfresco store the nodes belong to
   * @param igNodeRef the node reference of the target Interest Group
   */
  public ShareSpaceQueryParameter(
    Integer typeQnameId,
    Integer propQnameId,
    Integer storeID,
    String igNodeRef
  ) {
    super();
    this.typeQnameId = typeQnameId;
    this.propQnameId = propQnameId;
    this.storeID = storeID;
    this.igNodeRef = igNodeRef;
  }

  /**
   * Returns the Alfresco QName id of the content type to match.
   *
   * @return the type QName id
   */
  public Integer getTypeQnameId() {
    return typeQnameId;
  }

  /**
   * Sets the Alfresco QName id of the content type to match.
   *
   * @param typeQnameId the type QName id
   */
  public void setTypeQnameId(Integer typeQnameId) {
    this.typeQnameId = typeQnameId;
  }

  /**
   * Returns the Alfresco QName id of the property to match.
   *
   * @return the property QName id
   */
  public Integer getPropQnameId() {
    return propQnameId;
  }

  /**
   * Sets the Alfresco QName id of the property to match.
   *
   * @param propQnameId the property QName id
   */
  public void setPropQnameId(Integer propQnameId) {
    this.propQnameId = propQnameId;
  }

  /**
   * Returns the id of the Alfresco store the nodes belong to.
   *
   * @return the store id
   */
  public Integer getStoreID() {
    return storeID;
  }

  /**
   * Sets the id of the Alfresco store the nodes belong to.
   *
   * @param storeID the store id
   */
  public void setStoreID(Integer storeID) {
    this.storeID = storeID;
  }

  /**
   * Returns the node reference of the target Interest Group.
   *
   * @return the Interest Group node reference
   */
  public String getIgNodeRef() {
    return igNodeRef;
  }

  /**
   * Sets the node reference of the target Interest Group.
   *
   * @param igNodeRef the Interest Group node reference
   */
  public void setIgNodeRef(String igNodeRef) {
    this.igNodeRef = igNodeRef;
  }
}
