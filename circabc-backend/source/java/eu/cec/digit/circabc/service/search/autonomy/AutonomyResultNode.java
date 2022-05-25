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

/**
 * Result record that provides an Autonomy record.
 *
 * @author schwerr
 */
public class AutonomyResultNode {

    private String name = null;
    private String title = null;
    private String link = null;
    private String description = null;

    /**
     * Gets the value of the name
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name
     *
     * @param name the name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the value of the title
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the value of the title
     *
     * @param title the title to set.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets the value of the link
     *
     * @return the link
     */
    public String getLink() {
        return link;
    }

    /**
     * Sets the value of the link
     *
     * @param link the link to set.
     */
    public void setLink(String link) {
        this.link = link;
    }

    /**
     * Gets the value of the description
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description
     *
     * @param description the description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }
}
