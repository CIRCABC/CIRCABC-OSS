/**
 * ***************************************************************************** Copyright 2006
 * European Community
 *
 * <p>Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 *
 * <p>https://joinup.ec.europa.eu/software/page/eupl
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * ****************************************************************************
 */
package eu.cec.digit.circabc.service.log;

import java.io.Serializable;
import java.util.Date;

public class LogRestRecord implements Serializable {

    private static final long serialVersionUID = -2932718978323728183L;
    private String template;
    private String method;
    private Integer statusCode;
    private String pathOneName;
    private String pathOneValue;
    private String pathTwoName;
    private String pathTwoValue;
    private String pathThreeName;
    private String pathThreeValue;
    private String user;
    private String url;
    private Date date;
    private StringBuilder info = new StringBuilder();
    private String nodePath;
    private Long nodeID;
    private String nodeParent;

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * @return the info
     */
    public String getInfo() {
        return info.toString();
    }

    public void setInfo(String info) {
        this.info.delete(0, this.info.length());
        this.info.append(info);
    }

    public void addInfo(String info) {
        this.info.append(" ");
        this.info.append(info);
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

    public void setNodeParent(String parentNode) {
        this.nodeParent = parentNode;
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
}
