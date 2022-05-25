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
package eu.cec.digit.circabc.web.wai.dialog.properties;

import eu.cec.digit.circabc.service.dynamic.property.DynamicProperty;
import eu.cec.digit.circabc.service.dynamic.property.DynamicPropertyService;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNodeType;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.bean.repository.Node;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.context.FacesContext;
import java.util.List;
import java.util.Map;


/**
 * Bean that backs the "Manage properties" WAI page.
 *
 * @author Slobodan Filipovic
 */
public class ManagePropertiesDialog extends BaseWaiDialog {

    /**
     * Public JSF Bean name
     */
    public static final String BEAN_NAME = "ManagePropertiesDialog";
    /**
     * The constant for the 'property' parameter
     */
    public static final String PROPERTY_PARAMETER = "property";
    /**
     *
     */
    private static final long serialVersionUID = -4573272979102874036L;
    private static final Log logger = LogFactory.getLog(ManagePropertiesDialog.class);


    private transient DynamicPropertyService propertiesService;

    private List<DynamicProperty> properties;

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);

        if (parameters != null) {
            if (getActionNode() == null) {
                throw new IllegalArgumentException("Node id is a mandatory parameter");
            }

            if (!NavigableNodeType.IG_ROOT.isNodeFromType(getActionNode())) {
                throw new IllegalArgumentException("The node id must be an interest group");
            }

            this.properties = this.getPropertiesService()
                    .getDynamicProperties(getInterestGroup().getNodeRef());

            if (logger.isDebugEnabled()) {
                logger.debug(
                        properties.size() + " properties found for the IG " + getInterestGroup().getName()
                                + "\n\tIG NodeRef        :" + getInterestGroup().getNodeRef()
                                + "\n\tDynamic Properties:" + properties
                );
            }

        }
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception {
        // nothing to do
        return null;
    }

    @Override
    public void restored() {
        final NodeRef ig = getInterestGroup().getNodeRef();
        this.properties = this.getPropertiesService().getDynamicProperties(ig);
    }

    @Override
    public String getCancelButtonLabel() {
        return translate("close");
    }

    public boolean isAddNewAvailable() {
        return properties == null || properties.size() < getMaxProperty();
    }

    public int getMaxProperty() {
        return DynamicPropertyService.MAX_PROPERTY_BY_IG;
    }

    public String getBrowserTitle() {
        return translate("manage_properties_dialog_browser_title");
    }

    public String getPageIconAltText() {
        return translate("manage_properties_dialog_icon_tooltip");
    }

    public Node getInterestGroup() {
        return getActionNode();
    }

    /**
     * @return the properties
     */
    public List<DynamicProperty> getProperties() {
        return properties;
    }

    /**
     * @return the propertiesService
     */
    protected final DynamicPropertyService getPropertiesService() {
        if (propertiesService == null) {
            propertiesService = Services.getCircabcServiceRegistry(FacesContext.getCurrentInstance())
                    .getDynamicPropertieService();
        }
        return propertiesService;
    }

    /**
     * @param propertiesService the propertiesService to set
     */
    public final void setDynamicPropertyService(DynamicPropertyService dynamicPropertyService) {
        this.propertiesService = dynamicPropertyService;
    }

}
