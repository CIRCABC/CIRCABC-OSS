package eu.cec.digit.circabc.service.rendition;

import java.io.Serializable;
import java.util.Date;

/**
 * Represents a rendition request which is a row in the table that stores the renditions that were
 * posted to be rendered by the rendering job.
 *
 * @author schwerr
 */
public class Request implements Serializable {

    public static final int DEFAULT_RENDER_RETRIES = 3;
    private static final long serialVersionUID = -7981628059814941931L;
    private String requestId = null;
    private String machineId = null;
    private String nodeRefUUID = null;
    private Date requestDate = null;
    private Date fetchDate = null;
    private Date startProcessingDate = null;
    private Date endProcessingDate = null;
    private boolean success = false;
    private String errorMessage = null;
    private int remainingRenderRetries = DEFAULT_RENDER_RETRIES;

    public Request() {
    }

    public Request(String requestId, String machineId, String nodeRefUUID, Date requestDate) {
        this.requestId = requestId;
        this.machineId = machineId;
        this.nodeRefUUID = nodeRefUUID;
        this.requestDate = requestDate;
    }

    /**
     * Gets the value of the requestId
     *
     * @return the requestId
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * Sets the value of the requestId
     *
     * @param requestId the requestId to set.
     */
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    /**
     * Gets the value of the machineId
     *
     * @return the machineId
     */
    public String getMachineId() {
        return machineId;
    }

    /**
     * Sets the value of the machineId
     *
     * @param machineId the machineId to set.
     */
    public void setMachineId(String machineId) {
        this.machineId = machineId;
    }

    /**
     * Gets the value of the nodeRefUUID
     *
     * @return the nodeRefUUID
     */
    public String getNodeRefUUID() {
        return nodeRefUUID;
    }

    /**
     * Sets the value of the nodeRefUUID
     *
     * @param nodeRefUUID the nodeRefUUID to set.
     */
    public void setNodeRefUUID(String nodeRefUUID) {
        this.nodeRefUUID = nodeRefUUID;
    }

    /**
     * Gets the value of the requestDate
     *
     * @return the requestDate
     */
    public Date getRequestDate() {
        return requestDate;
    }

    /**
     * Sets the value of the requestDate
     *
     * @param requestDate the requestDate to set.
     */
    public void setRequestDate(Date requestDate) {
        this.requestDate = requestDate;
    }

    /**
     * Gets the value of the fetchDate
     *
     * @return the fetchDate
     */
    public Date getFetchDate() {
        return fetchDate;
    }

    /**
     * Sets the value of the fetchDate
     *
     * @param fetchDate the fetchDate to set.
     */
    public void setFetchDate(Date fetchDate) {
        this.fetchDate = fetchDate;
    }

    /**
     * Gets the value of the startProcessingDate
     *
     * @return the startProcessingDate
     */
    public Date getStartProcessingDate() {
        return startProcessingDate;
    }

    /**
     * Sets the value of the startProcessingDate
     *
     * @param startProcessingDate the startProcessingDate to set.
     */
    public void setStartProcessingDate(Date startProcessingDate) {
        this.startProcessingDate = startProcessingDate;
    }

    /**
     * Gets the value of the endProcessingDate
     *
     * @return the endProcessingDate
     */
    public Date getEndProcessingDate() {
        return endProcessingDate;
    }

    /**
     * Sets the value of the endProcessingDate
     *
     * @param endProcessingDate the endProcessingDate to set.
     */
    public void setEndProcessingDate(Date endProcessingDate) {
        this.endProcessingDate = endProcessingDate;
    }

    /**
     * Gets the value of the success
     *
     * @return the success
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Sets the value of the success
     *
     * @param success the success to set.
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * Gets the value of the errorMessage
     *
     * @return the errorMessage
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Sets the value of the errorMessage
     *
     * @param errorMessage the errorMessage to set.
     */
    public void setErrorMessage(String errorMessage) {

        if (errorMessage != null && errorMessage.length() > 4000) {
            errorMessage = errorMessage.substring(0, 3999);
        }

        this.errorMessage = errorMessage;
    }

    /**
     * Gets the value of the remainingRenderRetries
     *
     * @return the remainingRenderRetries
     */
    public int getRemainingRenderRetries() {
        return remainingRenderRetries;
    }

    /**
     * Sets the value of the remainingRenderRetries
     *
     * @param remainingRenderRetries the remainingRenderRetries to set.
     */
    public void setRemainingRenderRetries(int remainingRenderRetries) {
        this.remainingRenderRetries = remainingRenderRetries;
    }

    /**
     * Decrements by 1 the value of the remainingRenderRetries
     */
    public void decRemainingRenderRetries() {
        this.remainingRenderRetries--;
    }
}
