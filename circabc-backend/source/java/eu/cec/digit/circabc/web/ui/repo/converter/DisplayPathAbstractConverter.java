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
package eu.cec.digit.circabc.web.ui.repo.converter;

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.repo.struct.SimplePath;
import eu.cec.digit.circabc.service.struct.ManagementService;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.WebClientHelper;
import eu.cec.digit.circabc.web.WebClientHelper.ExtendedURLMode;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.InvalidAspectException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.web.ui.repo.converter.DisplayPathConverter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.ConverterException;
import javax.jcr.PathNotFoundException;
import java.text.MessageFormat;
import java.util.StringTokenizer;

/**
 * Base class for the display path of a file link or a folder link
 *
 * @author yanick pignot
 */
public abstract class DisplayPathAbstractConverter extends DisplayPathConverter {

    private static final String URL = "<a title=\"{0}\" href=\"{2}\">{1}</a>";

    public DisplayPathAbstractConverter() {
        super();
    }

    protected abstract FromElement getFromElement();

    protected abstract LinkType getLinkType();

    @Override
    public String getAsString(final FacesContext context, final UIComponent component,
                              final Object value) throws ConverterException {
        final SimplePath simplePath = getSimplePath(context, value);
        if (simplePath == null) {
            return super.getAsString(context, component, value);
        } else {
            String path = simplePath.toString();

            final StringTokenizer tokens = new StringTokenizer(path, "/", false);

            int tokenCount = tokens.countTokens();
            int cutCount = 0;
            switch (getFromElement()) {
                case SERVICE:
                    cutCount = 3;
                    //remove the 3 first elements that should be: /CircaBC/Category/InterestGroup/
                    break;
                case INTEREST_GROUP:
                    cutCount = 2;
                    //remove the 2 first elements that should be: /CircaBC/Category
                    break;
                case CATEGORY:
                    cutCount = 1;
                    //remove the 1 first elements that should be: /CircaBC
                    break;
                case CIRCABC_ROOT:
                    cutCount = 0;
                    //remove nothing
                    break;
                default:
                    // keep all
                    break;
            }

            if (tokenCount > cutCount) {
                final StringBuilder buff = new StringBuilder(path.length());
                String token = null;

                for (int c = 0; c < tokenCount; ++c) {
                    token = tokens.nextToken();
                    if (c >= cutCount) {
                        buff
                                .append('/')
                                .append(token);

                    }
                }

                path = buff.toString();
            }

            final LinkType linkType = getLinkType();

            if (path != null && LinkType.NONE.equals(linkType) == false) {
                final NodeService nodeService = Services.getAlfrescoServiceRegistry(context)
                        .getNodeService();
                final NodeRef nodeRef = simplePath.getNodeRef();
                final ExtendedURLMode mode;

                if (LinkType.BROWSE.equals(linkType)) {
                    mode = getBestBrowseMode(nodeService, nodeRef);
                } else {
                    if (ContentModel.TYPE_CONTENT.equals(nodeService.getType(nodeRef))) {
                        mode = ExtendedURLMode.HTTP_DOWNLOAD;
                    } else {
                        mode = getBestBrowseMode(nodeService, nodeRef);
                    }
                }

                return MessageFormat.format(
                        URL,
                        nodeService.getProperty(nodeRef, ContentModel.PROP_NAME),
                        path,
                        WebClientHelper.getGeneratedWaiFullUrl(nodeRef, mode));
            } else {
                return path;
            }
        }
    }

    private SimplePath getSimplePath(final FacesContext context, final Object value) {
        SimplePath path = null;

        if (value != null && value instanceof NodeRef) {
            final ManagementService managementService = Services.getCircabcServiceRegistry(context)
                    .getManagementService();
            try {
                path = managementService.getNodePath((NodeRef) value);
            } catch (final PathNotFoundException e) {
                path = null;
            }
        }

        return path;
    }

    /**
     * @param nodeService
     * @param nodeRef
     * @return
     * @throws InvalidNodeRefException
     * @throws InvalidAspectException
     */
    private ExtendedURLMode getBestBrowseMode(final NodeService nodeService, final NodeRef nodeRef)
            throws InvalidNodeRefException, InvalidAspectException {
        if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_CIRCABC_MANAGEMENT)) {
            return ExtendedURLMode.HTTP_WAI_BROWSE;
        } else {
            return ExtendedURLMode.HTTP_NATIVE_BROWSE;
        }
    }

    protected enum FromElement {
        CIRCABC_ROOT,
        CATEGORY,
        INTEREST_GROUP,
        SERVICE
    }

    protected enum LinkType {
        DOWNLOAD_BROWSE,
        BROWSE,
        NONE
    }
}
