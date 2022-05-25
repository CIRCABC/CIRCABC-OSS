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
package eu.cec.digit.circabc.service.event;

import org.alfresco.error.AlfrescoRuntimeException;

public class OccurenceRate {

    public static final int INVALID_VALUE = -1;
    private static final String NULL = "null";
    private static final String separator = "|";
    private MainOccurence mainOccurence;

    private TimesOccurence timesOccurence;

    private EveryTimesOccurence everyTimesOccurence;

    private int times;

    private int every;

    public OccurenceRate() {
    }

    public OccurenceRate(String occurenceRate) {
        String[] elements = occurenceRate.split("\\" + separator);
        if (!elements[0].equalsIgnoreCase(NULL)) {
            mainOccurence = MainOccurence.valueOf(elements[0]);
        }
        if (!elements[1].equalsIgnoreCase(NULL)) {
            timesOccurence = TimesOccurence.valueOf(elements[1]);
        }
        if (!elements[2].equalsIgnoreCase(NULL)) {
            everyTimesOccurence = EveryTimesOccurence.valueOf(elements[2]);
        }
        times = Integer.valueOf(elements[3]);
        every = Integer.valueOf(elements[4]);
    }

    public OccurenceRate(MainOccurence mainOccurence) {
        init(mainOccurence, null, null, INVALID_VALUE, INVALID_VALUE);
    }

    public OccurenceRate(MainOccurence mainOccurence, TimesOccurence timesOccurence, int times) {
        init(mainOccurence, timesOccurence, null, INVALID_VALUE, times);
    }

    public OccurenceRate(
            MainOccurence mainOccurence, EveryTimesOccurence everyTimesOccurence, int every, int times) {
        init(mainOccurence, null, everyTimesOccurence, every, times);
    }

    private void init(
            MainOccurence mainOccurence,
            TimesOccurence timesOccurence,
            EveryTimesOccurence everyTimesOccurence,
            int every,
            int times) {

        if ((mainOccurence == MainOccurence.OnlyOnce)
                && ((timesOccurence) != null || (everyTimesOccurence != null))) {
            throw new AlfrescoRuntimeException("");
        }
        if ((mainOccurence == MainOccurence.Times)
                && ((timesOccurence == null) || (times == INVALID_VALUE))) {
            throw new AlfrescoRuntimeException("");
        }
        if ((mainOccurence == MainOccurence.EveryTimes)
                && ((everyTimesOccurence == null)
                || (times == INVALID_VALUE)
                || (every == INVALID_VALUE))) {
            throw new AlfrescoRuntimeException("");
        }
        this.mainOccurence = mainOccurence;
        this.timesOccurence = timesOccurence;
        this.everyTimesOccurence = everyTimesOccurence;
        this.times = times;
        this.every = every;
    }

    /**
     * @return the mainOccurence
     */
    public MainOccurence getMainOccurence() {
        return mainOccurence;
    }

    /**
     * @param mainOccurence the mainOccurence to set
     */
    public void setMainOccurence(MainOccurence mainOccurence) {
        this.mainOccurence = mainOccurence;
    }

    /**
     * @return the timesOccurence
     */
    public TimesOccurence getTimesOccurence() {
        return timesOccurence;
    }

    /**
     * @param timesOccurence the timesOccurence to set
     */
    public void setTimesOccurence(TimesOccurence timesOccurence) {
        this.timesOccurence = timesOccurence;
    }

    /**
     * @return the everyTimesOccurence
     */
    public EveryTimesOccurence getEveryTimesOccurence() {
        return everyTimesOccurence;
    }

    /**
     * @param everyTimesOccurence the everyTimesOccurence to set
     */
    public void setEveryTimesOccurence(EveryTimesOccurence everyTimesOccurence) {
        this.everyTimesOccurence = everyTimesOccurence;
    }

    /**
     * @return the times
     */
    public int getTimes() {
        return times;
    }

    /**
     * @param times the times to set
     */
    public void setTimes(int times) {
        this.times = times;
    }

    /**
     * @return the every
     */
    public int getEvery() {
        return every;
    }

    /**
     * @param every the every to set
     */
    public void setEvery(int every) {
        this.every = every;
    }

    private String objectToString(Object object) {
        String result = NULL;
        if (object != null) {
            result = object.toString();
        }
        return result;
    }

    public String toString() {

        return objectToString(this.mainOccurence)
                + separator
                + objectToString(this.timesOccurence)
                + separator
                + objectToString(this.everyTimesOccurence)
                + separator
                + this.times
                + separator
                + this.every;
    }
}
