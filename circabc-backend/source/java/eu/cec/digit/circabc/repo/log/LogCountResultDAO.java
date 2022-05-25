/**
 * Copyright 2006 European Community
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
 */
/**
 *
 */
package eu.cec.digit.circabc.repo.log;

/** @author beaurpi */
public class LogCountResultDAO {

    private Integer hourPeriod;
    private Integer numberOfActions;

    public LogCountResultDAO() {
    }

    /** @return the date */
    public Integer getHourPeriod() {
        return hourPeriod;
    }

    /** @param date the date to set */
    public void setHourPeriod(Integer hourPeriod) {
        this.hourPeriod = hourPeriod;
    }

    /** @return the numberOfActions */
    public Integer getNumberOfActions() {
        return numberOfActions;
    }

    /** @param numberOfActions the numberOfActions to set */
    public void setNumberOfActions(Integer numberOfActions) {
        this.numberOfActions = numberOfActions;
    }
}
