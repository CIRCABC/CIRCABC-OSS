package eu.cec.digit.circabc.service.app.message;

import java.util.Date;

/**
 * AppMessage
 */
public class AppMessageDAO {

    private Integer id = null;

    private String messageContent = null;

    private Date dateClosure = null;

    private String messageLevel = null;

    private Boolean showMessage = false;

    private Integer displayTime = null;

    /**
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @return the messageContent
     */
    public String getMessageContent() {
        return messageContent;
    }

    /**
     * @param messageContent the messageContent to set
     */
    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    /**
     * @return the dateClosure
     */
    public Date getDateClosure() {
        return dateClosure;
    }

    /**
     * @param dateClosure the dateClosure to set
     */
    public void setDateClosure(Date dateClosure) {
        this.dateClosure = dateClosure;
    }

    /**
     * @return the messageClevel
     */
    public String getMessageLevel() {
        return messageLevel;
    }

    /**
     * @param messageClevel the messageClevel to set
     */
    public void setMessageLevel(String messageLevel) {
        this.messageLevel = messageLevel;
    }

    /**
     * @return the showMessage
     */
    public Boolean getShowMessage() {
        return showMessage;
    }

    /**
     * @param showMessage the showMessage to set
     */
    public void setShowMessage(Boolean showMessage) {
        this.showMessage = showMessage;
    }

    /**
     * @return the displayTime
     */
    public Integer getDisplayTime() {
        return displayTime;
    }

    /**
     * @param displayTime the displayTime to set
     */
    public void setDisplayTime(Integer displayTime) {
        this.displayTime = displayTime;
    }
}
