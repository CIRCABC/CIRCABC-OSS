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
package eu.cec.digit.circabc.repo.log;

import java.util.Date;

public class ActivityCountDAO {

    private Date monthActivity;
    private String service;
    private String activity;
    private Integer actionId;
    private Integer actionNumber;

    public ActivityCountDAO() {
    }

    /**
     * @return the monthActivity
     */
    public Date getMonthActivity() {
        return monthActivity;
    }

    /**
     * @param monthActivity the monthActivity to set
     */
    public void setMonthActivity(Date monthActivity) {
        this.monthActivity = monthActivity;
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
     * @return the activity
     */
    public String getActivity() {
        return activity;
    }

    /**
     * @param activity the activity to set
     */
    public void setActivity(String activity) {
        this.activity = activity;
    }

    /**
     * @return the actionId
     */
    public Integer getActionId() {
        return actionId;
    }

    /**
     * @param actionId the actionId to set
     */
    public void setActionId(Integer actionId) {
        this.actionId = actionId;
    }

    /**
     * @return the actionNumber
     */
    public Integer getActionNumber() {
        return actionNumber;
    }

    /**
     * @param actionNumber the actionNumber to set
     */
    public void setActionNumber(Integer actionNumber) {
        this.actionNumber = actionNumber;
    }
}
