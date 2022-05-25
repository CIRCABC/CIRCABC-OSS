/*******************************************************************************
 * Copyright 2006 European Community
 *
 *  Licensed under the EUPL, Version 1.1 or - as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  You may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  https://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 ******************************************************************************/
package eu.cec.digit.circabc.web.wai.dialog.customization;

import eu.cec.digit.circabc.service.customisation.logo.DefaultLogoConfiguration;
import eu.cec.digit.circabc.service.customisation.logo.LogoDefinition;
import eu.cec.digit.circabc.web.repository.IGServicesNode;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.ParameterCheck;
import org.alfresco.web.app.Application;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Yanick Pignot
 */
public class ServiceConfigWrapper implements Serializable {

    private static final String VALUE_NO_LOGO = "__NO_LOGO__";
    private static final String MSG_NO_LOGO = "manage_iglogo_dialog_no_logo";

    /**
     *
     */
    private static final long serialVersionUID = 8111113509926514097L;

    private final DefaultLogoConfiguration configuration;
    private final IGServicesNode service;
    private final List<LogoDefinition> availableImages;

    private Boolean display;
    private Boolean overrided;
    private String selectedImageRef;


    /*package*/ ServiceConfigWrapper(DefaultLogoConfiguration configuration,
                                     final IGServicesNode service, final List<LogoDefinition> availableImages) {
        super();

        ParameterCheck.mandatory("A configuration", configuration);
        ParameterCheck.mandatory("The service name", service);

        this.configuration = configuration;
        this.service = service;
        this.availableImages = availableImages;

        this.display = configuration.isLogoDisplayedOnAllPages();
        this.overrided = configuration.getConfiguredOn().equals(service.getNodeRef());

        this.selectedImageRef = getDefinitionLogoRef(this.configuration);
    }


    /*package*/
    final DefaultLogoConfiguration getOriginalConfiguration() {
        return configuration;
    }

    /*package*/
    final IGServicesNode getService() {
        return service;
    }

    /**
     * @return the display
     */
    public final Boolean getDisplay() {
        return display;
    }

    /**
     * @param display the display to set
     */
    public final void setDisplay(Boolean display) {
        this.display = display;
    }

    /**
     * @return the overrided
     */
    public final Boolean getOverrided() {
        return overrided;
    }

    /**
     * @param overrided the overrided to set
     */
    public final void setOverrided(Boolean overrided) {
        this.overrided = overrided;
    }

    /**
     * @return the serviceName
     */
    public final String getServiceName() {
        return service.getName();
    }

    /**
     * @return the selectedImageRef
     */
    public final String getSelectedImageRef() {
        return selectedImageRef;
    }

    /**
     * @param selectedImageRef the selectedImageRef to set
     */
    public final void setSelectedImageRef(String selectedImageRef) {
        this.selectedImageRef = selectedImageRef;
    }

    /**
     * @return the availableImages
     */
    public final List<SelectItem> getAvailableImages() {
        final List<SelectItem> items = new ArrayList<>(availableImages.size() + 1);

        final FacesContext fc = FacesContext.getCurrentInstance();
        items.add(new SelectItem(VALUE_NO_LOGO, Application.getMessage(fc, MSG_NO_LOGO)));

        for (final LogoDefinition def : availableImages) {
            items.add(new SelectItem(def.getReference().toString(), def.getName()));
        }

        return items;
    }

    /*package*/ boolean needToBeDeleted(final NodeRef igLogo, final Boolean igDisplay) {
        if (getOverrided() == false) {
            return false;
        } else if (getSelectedLogo() == null) {
            return true;
        } else {
            return !igDisplay && !display;
        }
    }

    /*package*/ boolean needToChangeLogo() {
        return selectedImageRef.equals(getDefinitionLogoRef(this.configuration)) == false;
    }

    /*package*/ boolean needToChangeDisplay() {
        return display.equals(configuration.isLogoDisplayedOnAllPages()) == false;
    }

    /*package*/ NodeRef getSelectedLogo() {
        return this.selectedImageRef.equals(VALUE_NO_LOGO) ? null : new NodeRef(this.selectedImageRef);
    }

    /**
     * @param configuration
     */
    private String getDefinitionLogoRef(final DefaultLogoConfiguration conf) {
        if (conf.getLogo() != null && conf.getLogo().getReference() != null) {
            return conf.getLogo().getReference().toString();
        } else {
            return VALUE_NO_LOGO;
        }
    }


}
