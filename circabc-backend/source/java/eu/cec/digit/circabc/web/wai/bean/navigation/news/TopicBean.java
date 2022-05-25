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
package eu.cec.digit.circabc.web.wai.bean.navigation.news;

import eu.cec.digit.circabc.web.bean.navigation.NavigableNodeType;
import eu.cec.digit.circabc.web.wai.bean.navigation.NewsGroupBean;
import org.alfresco.model.ContentModel;
import org.alfresco.web.bean.repository.Node;

/**
 * Bean that backs the navigation inside the topics of the newsgroup service
 *
 * @author yanick pignot
 */
public class TopicBean extends NewsGroupBean {

    public static final String JSP_NAME = "topic.jsp";
    public static final String BEAN_NAME = "NewsTopicBean";
    public static final String MSG_PAGE_DESCRIPTION = "newsgroups_topic_title_desc";
    public static final String MSG_PAGE_ICON_ALT = "newsgroups_topic_icon_tooltip";
    /**
     *
     */
    private static final long serialVersionUID = -6967164595499987493L;

    public NavigableNodeType getManagedNodeType() {
        return NavigableNodeType.NEWSGROUP_TOPIC;
    }

    public String getRelatedJsp() {
        return NAVIGATION_JSP_FOLDER + "news/" + JSP_NAME;
    }

    public String getPageDescription() {
        return translate(MSG_PAGE_DESCRIPTION);
    }

    public String getPageTitle() {
        return getCurrentNode().getName();
    }

    public String getPageIcon() {
        return "/images/icons/" + getCurrentNode().getProperties().get("icon") + ".gif";
    }

    public String getPageIconAltText() {
        return translate(MSG_PAGE_ICON_ALT);
    }

    public String getTopicTitle() {
        final Node current = getCurrentNode();
        final String title = (String) current.getProperties().get(ContentModel.PROP_TITLE.toString());

        final String toCrop;
        if (title == null || title.length() < 1) {
            toCrop = current.getName();
        } else {
            toCrop = title;
        }

        if (toCrop.length() > 80) {
            return toCrop.substring(0, 80) + "...";
        } else {
            return toCrop;
        }
    }

}
