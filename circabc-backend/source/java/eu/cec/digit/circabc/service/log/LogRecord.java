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

public class LogRecord implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 7838345879030553825L;

    private Long igID;
    private String igName = "";
    private Long documentID;
    private String user;
    private String activity;
    private StringBuilder info = new StringBuilder();
    private String path;
    private String service;
    private boolean isOK;
    private Date date;

    /**
     * @return the igID
     */
    public Long getIgID() {
        return igID;
    }

    /**
     * @param igID the igID to set
     */
    public void setIgID(Long igID) {
        this.igID = igID;
    }

    /**
     * @return the documentID
     */
    public Long getDocumentID() {
        return documentID;
    }

    /**
     * @param documentID the documentID to set
     */
    public void setDocumentID(Long documentID) {
        this.documentID = documentID;
    }

    /**
     * @return the user
     */
    public String getUser() {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * @return the service
     */
    public String getService() {
        return service;
    }

    /**
     * @param service the service to set
     */
    public void setService(String service) {
        this.service = service;
    }

    /**
     * @return the action
     */
    public String getActivity() {
        return activity;
    }

    /**
     * @param activity the action to set
     */
    public void setActivity(String activity) {
        this.activity = activity;
    }

    /**
     * @return the info
     */
    public String getInfo() {
        return info.toString();
    }

    /**
     * @param info the info to set
     */
    public void setInfo(String info) {
        this.info.delete(0, this.info.length());
        this.info.append(info);
    }

    /**
     * @param info the info to set
     */
    public void addInfo(String info) {
        this.info.append(" ");
        this.info.append(info);
    }

    /**
     * @return the isOK
     */
    public boolean isOK() {
        return isOK;
    }

    /**
     * @param isOK the isOK to set
     */
    public void setOK(boolean isOK) {
        this.isOK = isOK;
    }

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path the path to set
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * @return the igName
     */
    public String getIgName() {
        return igName;
    }

    /**
     * @param igName the igName to set
     */
    public void setIgName(String igName) {
        this.igName = igName;
    }

    /**
     * @return the date
     */
    public final Date getDate() {
        return date;
    }

    /**
     * @param date the date to set
     */
    public final void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "LogRecord [igID="
                + this.igID
                + ", igName="
                + this.igName
                + ", documentID="
                + this.documentID
                + ", user="
                + this.user
                + ", activity="
                + this.activity
                + ", info="
                + this.info
                + ", path="
                + this.path
                + ", service="
                + this.service
                + ", isOK="
                + this.isOK
                + ", date="
                + this.date
                + "]";
    }
}
