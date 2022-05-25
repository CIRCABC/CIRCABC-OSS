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

import eu.cec.digit.circabc.service.keyword.Keyword;
import eu.cec.digit.circabc.web.Services;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.owasp.esapi.ESAPI;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import java.util.List;

/**
 * Converter for a keyword. The list of keywords is displayed as as list of their names.
 *
 * @author yanick pignot
 */
public class KeywordConverter implements Converter {

    /**
     * <p>The standard converter id for this converter.</p>
     */
    public static final String CONVERTER_ID = "eu.cec.digit.circabc.faces.KeywordConverter";

    public static String getDisplayString(FacesContext context, List keywords, Boolean escape) {
        if (keywords == null || keywords.size() < 1) {
            return "";
        }

        final StringBuilder buff = new StringBuilder("");

        final NodeService nodeService = Services.getAlfrescoServiceRegistry(context).getNodeService();

        boolean first = true;
        NodeRef nodeRef = null;

        for (Object keyword : keywords) {
            if (keyword != null) {
                if (keyword instanceof NodeRef) {
                    nodeRef = (NodeRef) keyword;
                } else if (keyword instanceof Keyword) {
                    nodeRef = ((Keyword) keyword).getId();
                }

                if (nodeRef == null) {
                    throw new IllegalArgumentException(
                            "The list of keywords accepted must be either a list of NodeRef either a list of Keyword");
                }

                if (first) {
                    first = false;
                } else {
                    buff.append(", ");
                }
                if (escape != null && escape) {
                    // jsf will escape it
                    buff.append((String) nodeService.getProperty(nodeRef, ContentModel.PROP_TITLE));
                } else {
                    buff.append(ESAPI.encoder()
                            .encodeForHTML((String) nodeService.getProperty(nodeRef, ContentModel.PROP_TITLE)));
                }

            }
        }

        return buff.toString();
    }

    public Object getAsObject(FacesContext context, UIComponent component, String value)
            throws ConverterException {
        return value;
    }

    @SuppressWarnings("unchecked")
    public String getAsString(FacesContext context, UIComponent component, Object value)
            throws ConverterException {

        if (value instanceof List) {
            final Boolean escape = (Boolean) component.getAttributes().get("escape");
            return getDisplayString(context, (List) value, escape);
        } else {
            return "";
        }


    }


}
