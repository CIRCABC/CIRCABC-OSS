package io.swagger.model.db;

import java.util.Objects;

/**
 * Data access / transfer object that captures the state exchanged with the ARES bridge
 * integration for a single Alfresco node.
 *
 * <p>ARES is the European Commission's document registration system. Instances of this class
 * carry the correlation between a CIRCABC repository node (identified by {@link #nodeId} and a
 * specific {@link #versionLabel}) and the ARES-side registration details (transaction, document,
 * save number and registration number) produced or consumed while registering or transferring a
 * document.
 *
 * <p>This is a plain mutable holder with standard getters/setters and value-based
 * {@link #equals(Object)}, {@link #hashCode()} and {@link #toString()} implementations; it
 * contains no business logic.
 */
public class AresBridgeDAO {

  /** Identifier of the CIRCABC/Alfresco node this ARES bridge record refers to. */
  private String nodeId;
  /** Human-readable name of the referenced node. */
  private String nodeName;
  /** Version label of the referenced node (e.g. {@code "1.0"}). */
  private String versionLabel;
  /** Identifier of the ARES transaction associated with this node/version. */
  private String transactionId;
  /** Type of ARES request represented by this record (e.g. registration, transfer). */
  private String requestType;
  /** ARES document identifier assigned to the node. */
  private String documentId;
  /** ARES save number assigned to the document. */
  private String saveNumber;
  /** Official ARES registration number assigned to the document. */
  private String registrationNumber;

  /**
   * Returns the version label of the referenced node.
   *
   * @return the version label, or {@code null} if not set
   */
  public String getVersionLabel() {
    return versionLabel;
  }

  /**
   * Sets the version label of the referenced node.
   *
   * @param versionLabel the version label to set
   */
  public void setVersionLabel(String versionLabel) {
    this.versionLabel = versionLabel;
  }

  /**
   * Returns the identifier of the associated ARES transaction.
   *
   * @return the transaction identifier, or {@code null} if not set
   */
  public String getTransactionId() {
    return transactionId;
  }

  /**
   * Sets the identifier of the associated ARES transaction.
   *
   * @param transactionId the transaction identifier to set
   */
  public void setTransactionId(String transactionId) {
    this.transactionId = transactionId;
  }

  /**
   * Returns the ARES request type of this record.
   *
   * @return the request type, or {@code null} if not set
   */
  public String getRequestType() {
    return requestType;
  }

  /**
   * Sets the ARES request type of this record.
   *
   * @param requestType the request type to set
   */
  public void setRequestType(String requestType) {
    this.requestType = requestType;
  }

  /**
   * Returns the ARES document identifier.
   *
   * @return the document identifier, or {@code null} if not set
   */
  public String getDocumentId() {
    return documentId;
  }

  /**
   * Sets the ARES document identifier.
   *
   * @param documentId the document identifier to set
   */
  public void setDocumentId(String documentId) {
    this.documentId = documentId;
  }

  /**
   * Returns the ARES save number.
   *
   * @return the save number, or {@code null} if not set
   */
  public String getSaveNumber() {
    return saveNumber;
  }

  /**
   * Sets the ARES save number.
   *
   * @param saveNumber the save number to set
   */
  public void setSaveNumber(String saveNumber) {
    this.saveNumber = saveNumber;
  }

  /**
   * Returns the official ARES registration number.
   *
   * @return the registration number, or {@code null} if not set
   */
  public String getRegistrationNumber() {
    return registrationNumber;
  }

  /**
   * Sets the official ARES registration number.
   *
   * @param registrationNumber the registration number to set
   */
  public void setRegistrationNumber(String registrationNumber) {
    this.registrationNumber = registrationNumber;
  }

  /**
   * Returns the identifier of the referenced CIRCABC/Alfresco node.
   *
   * @return the node identifier, or {@code null} if not set
   */
  public String getNodeId() {
    return nodeId;
  }

  /**
   * Sets the identifier of the referenced CIRCABC/Alfresco node.
   *
   * @param nodeId the node identifier to set
   */
  public void setNodeId(String nodeId) {
    this.nodeId = nodeId;
  }

  /**
   * Compares this record with another for value equality across all fields.
   *
   * @param o the object to compare with
   * @return {@code true} if {@code o} is an {@code AresBridgeDAO} with equal field values,
   *     {@code false} otherwise
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AresBridgeDAO that = (AresBridgeDAO) o;
    return (
      Objects.equals(nodeId, that.nodeId) &&
      Objects.equals(nodeName, that.nodeName) &&
      Objects.equals(versionLabel, that.versionLabel) &&
      Objects.equals(transactionId, that.transactionId) &&
      Objects.equals(requestType, that.requestType) &&
      Objects.equals(documentId, that.documentId) &&
      Objects.equals(saveNumber, that.saveNumber) &&
      Objects.equals(registrationNumber, that.registrationNumber)
    );
  }

  /**
   * Returns a hash code consistent with {@link #equals(Object)}, derived from all fields.
   *
   * @return the hash code for this record
   */
  @Override
  public int hashCode() {
    return Objects.hash(
      nodeId,
      nodeName,
      versionLabel,
      transactionId,
      requestType,
      documentId,
      saveNumber,
      registrationNumber
    );
  }

  /**
   * Returns a human-readable representation of this record, listing all field values.
   *
   * @return a string representation of this {@code AresBridgeDAO}
   */
  @Override
  public String toString() {
    return (
      "AresBridgeDAO{" +
      "nodeId='" +
      nodeId +
      '\'' +
      ", nodeName='" +
      nodeName +
      '\'' +
      ", versionLabel='" +
      versionLabel +
      '\'' +
      ", transactionId='" +
      transactionId +
      '\'' +
      ", requestType='" +
      requestType +
      '\'' +
      ", documentId='" +
      documentId +
      '\'' +
      ", saveNumber='" +
      saveNumber +
      '\'' +
      ", registrationNumber='" +
      registrationNumber +
      '\'' +
      '}'
    );
  }

  /**
   * Returns the human-readable name of the referenced node.
   *
   * @return the node name, or {@code null} if not set
   */
  public String getNodeName() {
    return nodeName;
  }

  /**
   * Sets the human-readable name of the referenced node.
   *
   * @param nodeName the node name to set
   */
  public void setNodeName(String nodeName) {
    this.nodeName = nodeName;
  }
}
