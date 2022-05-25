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
package eu.cec.digit.circabc.service.search.autonomy;

import java.util.List;

/**
 * Returns the Autonomy search results with number of hits.
 *
 * @author schwerr
 */
public class AutonomyResults {

    private String error = null;

    private List<AutonomyResultNode> results = null;

    private String numHits = null;
    private String totalHits = null;
    private String maxResults = null;

    public AutonomyResults(String error) {
        this.error = error;
    }

    public AutonomyResults(
            List<AutonomyResultNode> results, String numHits, String totalHits, String maxResults) {
        this.results = results;
        this.numHits = numHits;
        this.totalHits = totalHits;
        this.maxResults = maxResults;
    }

    /**
     * Gets the value of the results
     *
     * @return the results
     */
    public List<AutonomyResultNode> getResults() {
        return results;
    }

    /**
     * Gets the value of the numHits
     *
     * @return the numHits
     */
    public String getNumHits() {
        return numHits;
    }

    /**
     * Gets the value of the totalHits
     *
     * @return the totalHits
     */
    public String getTotalHits() {
        return totalHits;
    }

    /**
     * Gets the value of the maxResults
     *
     * @return the maxResults
     */
    public String getMaxResults() {
        return maxResults;
    }

    /**
     * Gets the value of the error
     *
     * @return the error
     */
    public String getError() {
        return error;
    }
}
