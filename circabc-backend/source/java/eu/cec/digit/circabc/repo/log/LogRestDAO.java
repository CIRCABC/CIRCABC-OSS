package eu.cec.digit.circabc.repo.log;

import java.util.Date;

public class LogRestDAO {

    private long id; // internal DB-generated sequence id
    private int templateID;
    private String userName;
    private Date logDate;
    private int statusCode;
    private String url;
    private String info;
    private String pathOneName;
    private String pathOneValue;
    private String pathTwoName;
    private String pathTwoValue;
    private String pathThreeName;
    private String pathThreeValue;
    private String nodeParent;
    private String nodePath;
    private Long nodeID;

    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * @return the templateID
     */
    public int getTemplateID() {
        return templateID;
    }

    /**
     * @param templateID the templateID to set
     */
    public void setTemplateID(int templateID) {
        this.templateID = templateID;
    }

    /**
     * @return the userName
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @param userName the userName to set
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * @return the logDate
     */
    public Date getLogDate() {
        return logDate;
    }

    /**
     * @param logDate the logDate to set
     */
    public void setLogDate(Date logDate) {
        this.logDate = logDate;
    }

    /**
     * @return the statusCode
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * @param statusCode the statusCode to set
     */
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return the info
     */
    public String getInfo() {
        return info;
    }

    /**
     * @param info the info to set
     */
    public void setInfo(String info) {
        this.info = info;
    }

    public String getPathOneName() {
        return pathOneName;
    }

    public void setPathOneName(String pathOneName) {
        this.pathOneName = pathOneName;
    }

    public String getPathOneValue() {
        return pathOneValue;
    }

    public void setPathOneValue(String pathOneValue) {
        this.pathOneValue = pathOneValue;
    }

    public String getPathTwoName() {
        return pathTwoName;
    }

    public void setPathTwoName(String pathTwoName) {
        this.pathTwoName = pathTwoName;
    }

    public String getPathTwoValue() {
        return pathTwoValue;
    }

    public void setPathTwoValue(String pathTwoValue) {
        this.pathTwoValue = pathTwoValue;
    }

    public String getPathThreeName() {
        return pathThreeName;
    }

    public void setPathThreeName(String pathThreeName) {
        this.pathThreeName = pathThreeName;
    }

    public String getPathThreeValue() {
        return pathThreeValue;
    }

    public void setPathThreeValue(String pathThreeValue) {
        this.pathThreeValue = pathThreeValue;
    }

    public String getNodeParent() {
        return nodeParent;
    }

    public void setNodeParent(String nodeParent) {
        this.nodeParent = nodeParent;
    }

    public String getNodePath() {
        return nodePath;
    }

    public void setNodePath(String nodePath) {
        this.nodePath = nodePath;
    }

    public Long getNodeID() {
        return nodeID;
    }

    public void setNodeID(Long nodeID) {
        this.nodeID = nodeID;
    }

    @Override
    public String toString() {
        return "LogRestDAO{"
                + "id="
                + id
                + ", templateID="
                + templateID
                + ", userName='"
                + userName
                + '\''
                + ", logDate="
                + logDate
                + ", statusCode="
                + statusCode
                + ", url='"
                + url
                + '\''
                + ", info='"
                + info
                + '\''
                + ", pathOneName='"
                + pathOneName
                + '\''
                + ", pathOneValue='"
                + pathOneValue
                + '\''
                + ", pathTwoName='"
                + pathTwoName
                + '\''
                + ", pathTwoValue='"
                + pathTwoValue
                + '\''
                + ", pathThreeName='"
                + pathThreeName
                + '\''
                + ", pathThreeValue='"
                + pathThreeValue
                + '\''
                + ", nodeParent='"
                + nodeParent
                + '\''
                + ", nodePath='"
                + nodePath
                + '\''
                + ", nodeID="
                + nodeID
                + '}';
    }
}
