package io.swagger.model;

/**
 * Data transfer object describing a single transaction against an external
 * (non-Alfresco) content repository.
 *
 * <p>An instance captures the identifiers needed to correlate a piece of
 * content that lives in, or was exchanged with, an external repository: the
 * repository itself, the transaction under which the operation was performed,
 * the affected node, its version and its display name. It is a plain,
 * mutable value holder used to carry this information across the REST layer;
 * it contains no business logic.
 */
public class ExternalRepositoryTransaction {

  /** Identifier of the external repository the transaction targets. */
  private String repositoryId;

  /** Identifier of the transaction within the external repository. */
  private String transactionId;

  /** Identifier of the node (content item) affected by the transaction. */
  private String nodeId;

  /** Version label of the affected node (e.g. {@code "1.0"}). */
  private String versionLabel;

  /** Human-readable name of the affected node. */
  private String name;

  /**
   * Returns the identifier of the external repository.
   *
   * @return the repository identifier, or {@code null} if not set
   */
  public String getRepositoryId() {
    return repositoryId;
  }

  /**
   * Sets the identifier of the external repository.
   *
   * @param repositoryId the repository identifier to set
   */
  public void setRepositoryId(String repositoryId) {
    this.repositoryId = repositoryId;
  }

  /**
   * Returns the identifier of the transaction within the external repository.
   *
   * @return the transaction identifier, or {@code null} if not set
   */
  public String getTransactionId() {
    return transactionId;
  }

  /**
   * Sets the identifier of the transaction within the external repository.
   *
   * @param transactionId the transaction identifier to set
   */
  public void setTransactionId(String transactionId) {
    this.transactionId = transactionId;
  }

  /**
   * Returns the identifier of the node affected by the transaction.
   *
   * @return the node identifier, or {@code null} if not set
   */
  public String getNodeId() {
    return nodeId;
  }

  /**
   * Sets the identifier of the node affected by the transaction.
   *
   * @param nodeId the node identifier to set
   */
  public void setNodeId(String nodeId) {
    this.nodeId = nodeId;
  }

  /**
   * Returns the version label of the affected node.
   *
   * @return the version label, or {@code null} if not set
   */
  public String getVersionLabel() {
    return versionLabel;
  }

  /**
   * Sets the version label of the affected node.
   *
   * @param versionLabel the version label to set
   */
  public void setVersionLabel(String versionLabel) {
    this.versionLabel = versionLabel;
  }

  /**
   * Returns the human-readable name of the affected node.
   *
   * @return the node name, or {@code null} if not set
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the human-readable name of the affected node.
   *
   * @param name the node name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Computes a hash code based on all fields of this transaction.
   *
   * @return the hash code consistent with {@link #equals(Object)}
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((nodeId == null) ? 0 : nodeId.hashCode());
    result =
      prime * result + ((repositoryId == null) ? 0 : repositoryId.hashCode());
    result =
      prime * result + ((transactionId == null) ? 0 : transactionId.hashCode());
    result =
      prime * result + ((versionLabel == null) ? 0 : versionLabel.hashCode());
    return result;
  }

  /**
   * Compares this transaction to another object for equality. Two instances
   * are equal when all of their fields ({@code name}, {@code nodeId},
   * {@code repositoryId}, {@code transactionId} and {@code versionLabel})
   * are equal.
   *
   * @param obj the object to compare with
   * @return {@code true} if the given object is an equivalent
   *         {@code ExternalRepositoryTransaction}, {@code false} otherwise
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    ExternalRepositoryTransaction other = (ExternalRepositoryTransaction) obj;
    if (name == null) {
      if (other.name != null) return false;
    } else if (!name.equals(other.name)) return false;
    if (nodeId == null) {
      if (other.nodeId != null) return false;
    } else if (!nodeId.equals(other.nodeId)) return false;
    if (repositoryId == null) {
      if (other.repositoryId != null) return false;
    } else if (!repositoryId.equals(other.repositoryId)) return false;
    if (transactionId == null) {
      if (other.transactionId != null) return false;
    } else if (!transactionId.equals(other.transactionId)) return false;
    if (versionLabel == null) {
      if (other.versionLabel != null) return false;
    } else if (!versionLabel.equals(other.versionLabel)) return false;
    return true;
  }

  /**
   * Returns a string representation of this transaction, listing all of its
   * fields. Intended for logging and debugging.
   *
   * @return a human-readable representation of this object
   */
  @Override
  public String toString() {
    return (
      "ExternalRepositoryTransaction [name=" +
      name +
      ", nodeId=" +
      nodeId +
      ", repositoryId=" +
      repositoryId +
      ", transactionId=" +
      transactionId +
      ", versionLabel=" +
      versionLabel +
      "]"
    );
  }
}
