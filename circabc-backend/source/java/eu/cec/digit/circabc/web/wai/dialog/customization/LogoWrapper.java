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

import eu.cec.digit.circabc.repo.struct.SimplePath;
import eu.cec.digit.circabc.service.customisation.logo.LogoDefinition;
import eu.cec.digit.circabc.web.WebClientHelper;
import eu.cec.digit.circabc.web.WebClientHelper.ExtendedURLMode;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.util.ParameterCheck;
import org.alfresco.web.bean.repository.Node;

import javax.jcr.PathNotFoundException;
import java.io.Serializable;

import static eu.cec.digit.circabc.web.wai.dialog.customization.NavigationPrefWrapper.MSG_CURRENT_NODE;
import static eu.cec.digit.circabc.web.wai.dialog.customization.NavigationPrefWrapper.MSG_UNDEFINED;

/**
 * @author Yanick Pignot
 */
public class LogoWrapper implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 8468335099265140975L;

    private final LogoDefinition definition;
    private final NodeService nodeService;
    private final Node currentNode;
    private final String selectedLogid;

    /*package*/ LogoWrapper(final LogoDefinition definition, final Node currentNode,
                            final NodeService nodeService, final String selectedLogid) {
        super();

        ParameterCheck.mandatory("A logo definition", definition);
        ParameterCheck.mandatory("The current node", currentNode);
        ParameterCheck.mandatory("The node service", nodeService);

        this.definition = definition;
        this.currentNode = currentNode;
        this.nodeService = nodeService;
        this.selectedLogid = selectedLogid;
    }

    public NodeRef getDefinedOn() {
        return definition.getDefinedOn();
    }

    public String getDefinedOnPath() {
        final NodeRef customizedOn = getDefinedOn();

        if (customizedOn.equals(currentNode.getNodeRef())) {
            return translate(MSG_CURRENT_NODE);
        } else {
            SimplePath path;
            try {
                path = new SimplePath(nodeService, customizedOn);
                return path.toString();
            } catch (PathNotFoundException e) {
                return "<i>" + translate(MSG_UNDEFINED) + "</i>";
            }
        }
    }

    public String getDownloadPath() {
        return WebClientHelper.getGeneratedWaiUrl(getReference(), ExtendedURLMode.HTTP_DOWNLOAD, true);
    }

    public String getDescription() {
        return definition.getDescription();
    }

    public String getName() {
        return definition.getName();
    }

    public NodeRef getReference() {
        return definition.getReference();
    }

    public String getTitle() {
        return definition.getTitle();
    }

    public boolean isSelected() {
        return selectedLogid != null && selectedLogid.equals(getReference().getId());
    }

    public boolean isSelectable() {
        return selectedLogid == null || selectedLogid.equals(getReference().getId()) == false;
    }

    public boolean isRemovable() {
        return isSelected() == false && getDefinedOn().equals(currentNode.getNodeRef());
    }

    private String translate(final String message) {
        return WebClientHelper.translate(message);
    }
}
