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

import eu.cec.digit.circabc.business.api.user.UserDetails;
import eu.cec.digit.circabc.web.WebClientHelper;
import eu.cec.digit.circabc.web.WebClientHelper.ExtendedURLMode;
import org.alfresco.service.cmr.repository.NodeRef;

import java.text.MessageFormat;

/**
 * @author Yanick Pignot
 */
public class HTMLUseridConverter extends UseridConverter {

    public static final String CONVERTER_ID = "eu.cec.digit.circabc.faces.HTMLUseridConverter";
    private static final String LINK_HTML = "<a href=\"{0}\" title=\"{1}\" >{2}</a>";
    private static final String MSG_TITLE = "view_user_details_url_tooltip_wai";

    @Override
    protected String getTextContent(final UserDetails userDetails) {
        if (userDetails.isUserCreated()) {
            final NodeRef person = userDetails.getNodeRef();
            final String url = WebClientHelper
                    .getGeneratedWaiFullUrl(person, ExtendedURLMode.HTTP_USERDETAILS);
            final String name = userDetails.getFullName();
            final String title = WebClientHelper.translate(MSG_TITLE, name);
            return MessageFormat.format(LINK_HTML, url, title, name);
        } else {
            return super.getTextContent(userDetails);
        }
    }

}
