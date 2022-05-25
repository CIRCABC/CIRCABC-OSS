package eu.cec.digit.circabc.repo.ares;

import java.util.Objects;

public class AresBridgeDAO {

    private String nodeId;
    private String nodeName;
    private String versionLabel;
    private String transactionId;
    private String requestType;
    private String documentId;
    private String saveNumber;
    private String registrationNumber;

    public String getVersionLabel() {
        return versionLabel;
    }

    public void setVersionLabel(String versionLabel) {
        this.versionLabel = versionLabel;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getSaveNumber() {
        return saveNumber;
    }

    public void setSaveNumber(String saveNumber) {
        this.saveNumber = saveNumber;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AresBridgeDAO that = (AresBridgeDAO) o;
        return Objects.equals(nodeId, that.nodeId)
                && Objects.equals(nodeName, that.nodeName)
                && Objects.equals(versionLabel, that.versionLabel)
                && Objects.equals(transactionId, that.transactionId)
                && Objects.equals(requestType, that.requestType)
                && Objects.equals(documentId, that.documentId)
                && Objects.equals(saveNumber, that.saveNumber)
                && Objects.equals(registrationNumber, that.registrationNumber);
    }

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
                registrationNumber);
    }

    @Override
    public String toString() {
        return "AresBridgeDAO{"
                + "nodeId='"
                + nodeId
                + '\''
                + ", nodeName='"
                + nodeName
                + '\''
                + ", versionLabel='"
                + versionLabel
                + '\''
                + ", transactionId='"
                + transactionId
                + '\''
                + ", requestType='"
                + requestType
                + '\''
                + ", documentId='"
                + documentId
                + '\''
                + ", saveNumber='"
                + saveNumber
                + '\''
                + ", registrationNumber='"
                + registrationNumber
                + '\''
                + '}';
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }
}
