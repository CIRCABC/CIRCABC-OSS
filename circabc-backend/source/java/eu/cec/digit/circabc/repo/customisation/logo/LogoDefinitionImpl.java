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
package eu.cec.digit.circabc.repo.customisation.logo;

import eu.cec.digit.circabc.service.customisation.logo.LogoDefinition;
import org.alfresco.service.cmr.repository.NodeRef;

import java.io.Serializable;

/**
 * The base single logo wrapper
 *
 * @author yanick pignot
 */
public class LogoDefinitionImpl implements Serializable, LogoDefinition {

    /**
     *
     */
    private static final long serialVersionUID = -4254888522583804060L;

    private NodeRef logo = null;
    private NodeRef definedOn = null;
    private String title;
    private String description;
    private String name;

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.repo.customisation.logo.LogoDefinition#getLogo()
     */
    public final NodeRef getReference() {
        return logo;
    }

    /**
     * @param logo the logo to set
     */
    /*package*/
    final void setReference(NodeRef logo) {
        this.logo = logo;
    }

    public final NodeRef getDefinedOn() {
        return definedOn;
    }

    /**
     * @param definedOn the definedOn to set
     */
    /*package*/ void setDefinedOn(NodeRef definedOn) {
        this.definedOn = definedOn;
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.repo.customisation.logo.LogoDefinition#getLogoDescription()
     */
    public final String getDescription() {
        return description;
    }

    /**
     * @param logoDescription the logoDescription to set
     */
    /*package*/
    final void setDescription(String logoDescription) {
        this.description = logoDescription;
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.repo.customisation.logo.LogoDefinition#getLogoName()
     */
    public final String getName() {
        return name;
    }

    /**
     * @param logoName the logoName to set
     */
    /*package*/
    final void setName(String logoName) {
        this.name = logoName;
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.repo.customisation.logo.LogoDefinition#getLogoTitle()
     */
    public final String getTitle() {
        return title;
    }

    /**
     * @param logoTitle the logoTitle to set
     */
    /*package*/
    final void setTitle(String logoTitle) {
        this.title = logoTitle;
    }
}
