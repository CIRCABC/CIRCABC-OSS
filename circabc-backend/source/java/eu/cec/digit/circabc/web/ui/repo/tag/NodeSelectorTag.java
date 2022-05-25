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
package eu.cec.digit.circabc.web.ui.repo.tag;

import org.alfresco.web.ui.repo.tag.ItemSelectorTag;

import javax.faces.component.UIComponent;

/**
 * @author Slobodan Filipovic
 * <p>
 * Add new parameter rootNode
 */
public class NodeSelectorTag extends ItemSelectorTag {


    /**
     * Root node for selection
     */
    private String rootNode;
    private String showContents;
    private String pathLabel;
    private String pathErrorMessage;


    /**
     * @see javax.faces.webapp.UIComponentTag#getRendererType()
     */
    public String getRendererType() {
        return null;
    }

    /**
     * @see javax.faces.webapp.UIComponentTag#setProperties(javax.faces.component.UIComponent)
     */
    protected void setProperties(UIComponent component) {
        super.setProperties(component);
        setStringBindingProperty(component, "rootNode", this.rootNode);
        setBooleanProperty(component, "showContents", this.showContents);
        setStringBindingProperty(component, "pathLabel", this.pathLabel);
        setStringBindingProperty(component, "pathErrorMessage", this.pathErrorMessage);

    }

    /**
     * @see org.alfresco.web.ui.common.tag.HtmlComponentTag#release()
     */
    public void release() {
        super.release();

        this.rootNode = null;
        this.showContents = null;
        this.pathLabel = null;
        this.pathErrorMessage = null;
    }

    /**
     * Sets the root node of so user cqn not brozse above root node
     *
     * @param rootNode The id of the root node item
     */
    public void setRootNode(String rootNode) {
        this.rootNode = rootNode;
    }

    /**
     * Sets if the contents must be displayed or not
     */
    public void setShowContents(String showContents) {
        this.showContents = showContents;
    }

    /**
     * @see javax.faces.webapp.UIComponentTag#getComponentType()
     */
    @Override
    public String getComponentType() {
        return "eu.cec.digit.circabc.faces.NodeSelector";
    }

    /**
     * @return the pathLabel
     */
    public String getPathLabel() {
        return pathLabel;
    }

    /**
     * @param pathLabel the pathLabel to set
     */
    public void setPathLabel(String pathLabel) {
        this.pathLabel = pathLabel;
    }

    /**
     * @return the pathErrorMessage
     */
    public String getPathErrorMessage() {
        return pathErrorMessage;
    }

    /**
     * @param pathErrorMessage the pathErrorMessage to set
     */
    public void setPathErrorMessage(String pathErrorMessage) {
        this.pathErrorMessage = pathErrorMessage;
    }


}
