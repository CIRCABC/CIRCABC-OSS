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
package eu.cec.digit.circabc.service.customisation.logo;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * The base logo configuration wrapper
 *
 * @author yanick pignot
 */
public interface DefaultLogoConfiguration {

    /**
     * @return the the default logo reference.
     */
    LogoDefinition getLogo();

    /**
     * @return if the logo must be displayed on the main page (IG-HOME)
     */
    boolean isLogoDisplayedOnMainPage();

    /**
     * @return the Logo Width for the main page or a negative value if not set
     */
    int getMainPageLogoWidth();

    /**
     * @return the Logo Height for the main page or a negative value if not set
     */
    int getMainPageLogoHeight();

    /**
     * @return if the logo size must to be forced or not
     */
    boolean isMainPageSizeForced();

    /**
     * @return if the logo is display at the left (default) or at the right of the screen
     */
    boolean isMainPageLogoAtLeft();

    /**
     * @return if the logo must be displayed on all other navigation pages
     */
    boolean isLogoDisplayedOnAllPages();

    /**
     * @return the Logo Width for all other pages or a negative value if not set
     */
    int getOtherPagesLogoWidth();

    /**
     * @return the Logo Height for all other pages or a negative value if not set
     */
    int getOtherPagesLogoHeight();

    /**
     * @return if the logo size must to be forced or not on all other pages
     */
    boolean isOtherPagesSizeForced();

    /**
     * @return the node reference on which the configuration is setted.
     */
    NodeRef getConfiguredOn();
}
