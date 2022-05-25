/**
 * Copyright 2006 European Community
 * <p>
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 * <p>
 * https://joinup.ec.europa.eu/software/page/eupl
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence
 * is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the Licence for the specific language governing permissions and limitations under
 * the Licence.
 */
/**
 *
 */
package eu.cec.digit.circabc.web.wai.dialog.category;

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author beaurpi
 *
 */
public class CustomiseCategoryDialog extends BaseWaiDialog {

    /**
     *
     */
    private static final long serialVersionUID = 2288344234549598018L;
    private static final String navigationListShortNameRender = "shortname";
    private static final String navigationListTitleRender = "title";
    private NodeRef currentNode;
    private String selectedRenderChoice;
    private List<SelectItem> selectRenderChoices;

    /**
     * @return the navigationlistshortnamerender
     */
    public static String getNavigationlistshortnamerender() {
        return navigationListShortNameRender;
    }

    /**
     * @return the navigationlisttitlerender
     */
    public static String getNavigationlisttitlerender() {
        return navigationListTitleRender;
    }

    public String getPageIconAltText() {

        return translate("category_customise_dialog_alt_text");
    }

    public String getBrowserTitle() {
        return translate("category_customise_dialog_title");
    }

    @Override
    public void init(Map<String, String> parameters) {

        super.init(parameters);

        currentNode = getActionNode().getNodeRef();

        this.selectRenderChoices = new ArrayList<>();
        this.selectRenderChoices.add(new SelectItem(navigationListShortNameRender,
                translate("category_customise_dialog_navigation_shortname_render")));
        this.selectRenderChoices.add(new SelectItem(navigationListTitleRender,
                translate("category_customise_dialog_navigation_title_render")));

        if (getNodeService().getProperties(currentNode)
                .containsKey(CircabcModel.PROP_NAVIGATION_LIST_RENDER_TYPE)) {
            selectedRenderChoice = getNodeService()
                    .getProperty(currentNode, CircabcModel.PROP_NAVIGATION_LIST_RENDER_TYPE).toString();
        }

    }

    @Override
    protected String finishImpl(FacesContext context, String outcome)
            throws Throwable {

        Boolean isCategory = getNodeService().hasAspect(currentNode, CircabcModel.ASPECT_CATEGORY);

        Map<QName, Serializable> rendererProperty = new HashMap<>();

        /*
         * verify if property already exist and add | update it to the current category node
         */

        if (selectedRenderChoice.equals(navigationListShortNameRender) && isCategory) {
            /*
             * To display the short name
             */
            rendererProperty
                    .put(CircabcModel.PROP_NAVIGATION_LIST_RENDER_TYPE, navigationListShortNameRender);

            if (getNodeService().getProperties(currentNode)
                    .containsKey(CircabcModel.PROP_NAVIGATION_LIST_RENDER_TYPE)) {
                getNodeService().removeProperty(currentNode, CircabcModel.PROP_NAVIGATION_LIST_RENDER_TYPE);
            }

            getNodeService().addProperties(currentNode, rendererProperty);

        } else if (selectedRenderChoice.equals(navigationListTitleRender) && isCategory) {
            /*
             * To display the title
             */
            rendererProperty
                    .put(CircabcModel.PROP_NAVIGATION_LIST_RENDER_TYPE, navigationListTitleRender);

            if (getNodeService().getProperties(currentNode)
                    .containsKey(CircabcModel.PROP_NAVIGATION_LIST_RENDER_TYPE)) {
                getNodeService().removeProperty(currentNode, CircabcModel.PROP_NAVIGATION_LIST_RENDER_TYPE);
            }

            getNodeService().addProperties(currentNode, rendererProperty);
        }

        return outcome;
    }

    /**
     * @return the currentNode
     */
    public NodeRef getCurrentNode() {
        return currentNode;
    }

    /**
     * @param currentNode the currentNode to set
     */
    public void setCurrentNode(NodeRef currentNode) {
        this.currentNode = currentNode;
    }

    /**
     * @return the selectedRenderChoice
     */
    public String getSelectedRenderChoice() {

        return selectedRenderChoice;
    }

    /**
     * @param selectedRenderChoice the selectedRenderChoice to set
     */
    public void setSelectedRenderChoice(String selectedRenderChoice) {
        this.selectedRenderChoice = selectedRenderChoice;
    }

    /**
     * @return the selectRenderChoices
     */
    public List<SelectItem> getSelectRenderChoices() {
        return selectRenderChoices;
    }

    /**
     * @param selectRenderChoices the selectRenderChoices to set
     */
    public void setSelectRenderChoices(List<SelectItem> selectRenderChoices) {
        this.selectRenderChoices = selectRenderChoices;
    }


}
