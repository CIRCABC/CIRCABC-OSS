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
package eu.cec.digit.circabc.web.ui.repo.component;

import eu.cec.digit.circabc.util.PathUtils;
import eu.cec.digit.circabc.web.Beans;
import eu.cec.digit.circabc.web.WebClientHelper;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNode;
import eu.cec.digit.circabc.web.bean.override.CircabcNavigationBean;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.search.impl.lucene.LuceneQueryParser;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.repo.component.UIContentSelector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;
import java.io.IOException;

public class UICircabcContentSelector extends UIContentSelector {

    private final static Log logger = LogFactory.getLog(UICircabcContentSelector.class);
    private final static String FIELD_AVAILABLE = "_available";
    private String interestGroupNodeRef;

    public String getFamily() {
        return "eu.cec.digit.circabc.faces.CircabcContentSelector";
    }

    @SuppressWarnings("unchecked")
    public void restoreState(FacesContext context, Object state) {
        Object values[] = (Object[]) state;

        super.restoreState(context, values[0]);
        this.interestGroupNodeRef = (String) values[1];
    }

    public Object saveState(FacesContext context) {
        Object values[] = new Object[2];
        // standard component attributes are saved by the super class
        values[0] = super.saveState(context);
        values[1] = this.interestGroupNodeRef;

        return (values);
    }

    protected void getAvailableOptions(FacesContext context, String contains) {
        String igNodeRefAsString = getInterestGroupNodeRef();

        if (igNodeRefAsString == null) {
            CircabcNavigationBean navigator = Beans.getWaiNavigator();

            if (navigator != null) {
                NavigableNode currentIGRoot = navigator.getCurrentIGRoot();
                if (currentIGRoot != null) {

                    igNodeRefAsString = currentIGRoot.getNodeRef().toString();
                }
            }

        }

        if (igNodeRefAsString == null) {
            super.getAvailableOptions(context, contains);
        } else {
            customGetAvailableOptions(context, contains, new NodeRef(igNodeRefAsString));
        }

    }


    /**
     * Renders the list of available options
     *
     * @param context     FacesContext
     * @param out         Writer to write output to
     * @param nodeService The NodeService
     */
    protected void renderAvailableOptions(FacesContext context, ResponseWriter out,
                                          NodeService nodeService)
            throws IOException {
        String igNodeRefAsString = getInterestGroupNodeRef();

        if (igNodeRefAsString == null) {
            CircabcNavigationBean navigator = Beans.getWaiNavigator();

            if (navigator != null) {
                NavigableNode currentIGRoot = navigator.getCurrentIGRoot();
                if (currentIGRoot != null) {

                    igNodeRefAsString = currentIGRoot.getNodeRef().toString();
                }
            }

        }

        if (igNodeRefAsString == null) {
            super.renderAvailableOptions(context, out, nodeService);
        } else {
            customRenderAvailableOptions(context, out, nodeService);
        }


    }


    private void customRenderAvailableOptions(FacesContext context,
                                              ResponseWriter out, NodeService nodeService) throws IOException {
        boolean itemsPresent = (this.availableOptions != null && this.availableOptions.size() > 0);

        out.write("<tr><td colspan='2'><select ");
        if (itemsPresent == false) {
            // rather than having a very slim select box set the width if there are no results
            out.write("style='width:240px;' ");
        }
        out.write("name='");
        out.write(getClientId(context) + FIELD_AVAILABLE);
        out.write("' size='");
        if (getMultiSelect()) {
            out.write(getAvailableOptionsSize());
            out.write("' multiple");
        } else {
            out.write("1'");
        }
        out.write(">");

        if (itemsPresent) {
            for (NodeRef item : this.availableOptions) {
                out.write("<option value='");
                out.write(item.toString());
                out.write("'>");
                out.write(Utils.encode(PathUtils.getInterestGroupPath(nodeService.getPath(item), true)));
                out.write("/");
                out.write(Utils.encode(Repository.getNameForNode(nodeService, item)));
                out.write("</option>");
            }
        }

        out.write("</select></td></tr>");

    }

    private void customGetAvailableOptions(FacesContext context, String contains, NodeRef igNodeRef) {

        // query for all content in the current repository
        StringBuilder query = new StringBuilder("+TYPE:\"");
        query.append(ContentModel.TYPE_CONTENT);
        query.append("\"");

        if (igNodeRef != null) {
            query.append(" AND  PATH:\"").append(WebClientHelper.getPathFromSpaceRef(igNodeRef, true));
            query.append("\"");
        }

        if (contains != null && contains.length() > 0) {
            String safeContains = LuceneQueryParser.escape(contains.trim());
            query.append(" AND +@");

            String nameAttr = Repository.escapeQName(QName.createQName(
                    NamespaceService.CONTENT_MODEL_1_0_URI, "name"));
            query.append(nameAttr);

            query.append(":\"*").append(safeContains).append("*\"");
        }

        int maxResults = Application.getClientConfig(context).getSelectorsSearchMaxResults();

        if (logger.isDebugEnabled()) {
            logger.debug("Query: " + query.toString());
            logger.debug("Max results size: " + maxResults);
        }

        // setup search parameters, including limiting the results
        SearchParameters searchParams = new SearchParameters();
        searchParams.addStore(Repository.getStoreRef());
        searchParams.setLanguage(SearchService.LANGUAGE_LUCENE);
        searchParams.setQuery(query.toString());
        if (maxResults > 0) {
            searchParams.setLimit(maxResults);
            searchParams.setLimitBy(LimitBy.FINAL_SIZE);
        }

        ResultSet results = null;
        try {
            results = Repository.getServiceRegistry(context).getSearchService().query(searchParams);
            this.availableOptions = results.getNodeRefs();
        } finally {
            if (results != null) {
                results.close();
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Found " + this.availableOptions.size() + " available options");
        }

    }

    public String getInterestGroupNodeRef() {
        ValueBinding vb = getValueBinding("interestGroupNodeRef");
        if (vb != null) {
            this.interestGroupNodeRef = (String) vb.getValue(getFacesContext());
        }

        return this.interestGroupNodeRef;
    }

    public void setInterestGroupNodeRef(String value) {
        this.interestGroupNodeRef = value;
    }


}
