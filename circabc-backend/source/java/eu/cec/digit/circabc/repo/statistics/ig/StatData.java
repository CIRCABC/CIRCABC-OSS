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
package eu.cec.digit.circabc.repo.statistics.ig;

/** @author beaurpi */
public class StatData {

    private String dataName;
    private Object dataValue;

    /** * Simple constructor */
    public StatData() {
    }

    /** * Complete constructor */
    public StatData(String name, Object value) {
        this.dataName = name;
        this.dataValue = value;
    }

    /** @return the dataName */
    public String getDataName() {
        return dataName;
    }

    /** @param dataName the dataName to set */
    public void setDataName(String dataName) {
        this.dataName = dataName;
    }

    /** @return the dataValue */
    public Object getDataValue() {
        return dataValue;
    }

    /** @param dataValue the dataValue to set */
    public void setDataValue(Object dataValue) {
        this.dataValue = dataValue;
    }
}
