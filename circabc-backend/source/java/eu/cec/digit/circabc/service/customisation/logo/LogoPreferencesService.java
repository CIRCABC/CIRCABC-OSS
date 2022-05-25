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

import eu.cec.digit.circabc.repo.customisation.CustomizationException;
import eu.cec.digit.circabc.repo.customisation.logo.DefaultLogoConfigurationImpl;
import org.alfresco.service.Auditable;
import org.alfresco.service.cmr.repository.NodeRef;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Interface for the customisation of Interest Groups logo.
 *
 * @author Yanick Pignot
 * <p>Migration 3.1 -> 3.4.6 - 02/12/2011 Commented the key parameter of the @Auditable
 * annotation. Commented the deprecated @PublicService annotation.
 * @see eu.cec.digit.circabc.service.customisation.NodePreferencesService
 */
// @PublicService
public interface LogoPreferencesService {

    /**
     * Add a logo for the current node and all its childs. For setting it being the default one, use
     * the <code>setDefault</code> method.
     *
     * @param ref  The node on which the logo is setted
     * @param name The mandatory and unique name of the logo
     * @param file Where to found the logo stream
     * @return The created logo
     * @throws CustomizationException If the name is missing or if is already existing
     * @throws IOException            If any IO error occurs
     */
    @Auditable(/* key = Auditable.Key.ARG_0, */ parameters = {"ref", "name", "file"})
    LogoDefinition addLogo(final NodeRef ref, final String name, final File file)
            throws CustomizationException, IOException;

    /**
     * Add a logo for the current node and all its childs. For setting it being the default one, use
     * the <code>setDefault</code> method.
     *
     * @param ref         The node on which the logo is setted
     * @param name        The mandatory and unique name of the logo
     * @param inputStream Where to found the logo stream
     * @return The created logo
     * @throws CustomizationException If the name is missing or if is already existing
     * @throws IOException            If any IO error occurs
     */
    @Auditable(/* key = Auditable.Key.ARG_0, */ parameters = {"ref", "name", "inputStream"})
    LogoDefinition addLogo(final NodeRef ref, final String name, final InputStream inputStream)
            throws CustomizationException, IOException;

    /**
     * Get all defined logo for a given node on all parents
     *
     * @param ref The node on which the logo is setted
     * @return All defined logo.
     */
    @Auditable(/* key = Auditable.Key.ARG_0, */ parameters = {"ref"})
    List<LogoDefinition> getAllLogos(final NodeRef ref) throws CustomizationException;

    /**
     * Get the actual default logo. Null if no one is setted.
     *
     * @param ref The node on which the logo is setted
     * @return The default logo and its configuration
     */
    @Auditable(/* key = Auditable.Key.ARG_0, */ parameters = {"ref"})
    DefaultLogoConfiguration getDefault(final NodeRef ref) throws CustomizationException;

    /**
     * Remove the configuration of the logo on the given node to apply default settings.
     *
     * @param ref The node on which the logo is setted
     * @throws CustomizationException If the the configurqtion doesn't exists on the specified
     *                                reference.
     */
    @Auditable(/* key = Auditable.Key.ARG_0, */ parameters = {"ref"})
    void removeConfiguration(final NodeRef ref) throws CustomizationException;

    /**
     * Remove a logo on a given location.
     *
     * @param ref      The node on which the logo is setted
     * @param logoName The name of the logo
     * @throws CustomizationException If the the logo doesn't exists on the specified reference.
     */
    @Auditable(/* key = Auditable.Key.ARG_0, */ parameters = {"ref", "logoName"})
    void removeLogo(final NodeRef ref, final String logoName) throws CustomizationException;

    /**
     * Set the given logo being the default one.
     *
     * @param ref      The node on which the logo is setted
     * @param logoName The logo to set as default or null to set no logo
     * @throws CustomizationException If the the logo doesn't exists in the
     *                                <pre>
     *                                            getAllLogos
     *                                            </pre>
     *                                return method
     */
    @Auditable(/* key = Auditable.Key.ARG_0, */ parameters = {"ref", "logoName"})
    void setDefault(final NodeRef ref, final NodeRef logoReference) throws CustomizationException;

    /**
     * Define how the default logo must be displayed on the main page (interest group home)
     *
     * @param ref        The node on which the logo is setted
     * @param display    If the logo is displayed on the main page or not
     * @param height     The height of the logo
     * @param width      The width of the logo
     * @param sizeForced If the defined sizes must be forced or are maximum
     * @param logoAtLeft If the logo is displayed at the left or at the right of the screen
     */
    @Auditable(
            /* key = Auditable.Key.ARG_0, */ parameters = {
            "ref",
            "display",
            "height",
            "width",
            "sizeForced",
            "logoAtLeft"
    })
    void setMainPageLogoConfig(
            final NodeRef ref,
            boolean display,
            int height,
            int width,
            boolean sizeForced,
            boolean logoAtLeft)
            throws CustomizationException;

    /**
     * Define if and how the default logo must be displayed on all othert pages (not interest group
     * home)
     *
     * @param ref        The node on which the logo is setted
     * @param display    If the logo is displayed on all other pages or not
     * @param height     The height of the logo
     * @param width      The width of the logo
     * @param sizeForced If the defined sizes must be forced or are maximum
     */
    @Auditable(
            /* key = Auditable.Key.ARG_0, */ parameters = {
            "ref",
            "display",
            "height",
            "width",
            "sizeForced"
    })
    void setOtherPagesLogoConfig(
            final NodeRef ref, boolean display, int height, int width, boolean sizeForced)
            throws CustomizationException;

    /**
     * @param ref
     * @param createIfMissing
     * @return
     * @throws CustomizationException
     */
    @Auditable(/* key = Auditable.Key.ARG_0, */ parameters = {"ref", "createIfMissing"})
    public DefaultLogoConfigurationImpl getOrCreateConfiguraton(
            final NodeRef ref, final boolean createIfMissing) throws CustomizationException;

    /**
     *
     */
    public void forceClearCache();
}
