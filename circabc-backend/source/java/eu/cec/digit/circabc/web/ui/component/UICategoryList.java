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
package eu.cec.digit.circabc.web.ui.component;

import eu.cec.digit.circabc.web.WebClientHelper;
import eu.cec.digit.circabc.web.WebClientHelper.ExtendedURLMode;
import eu.cec.digit.circabc.web.ui.common.WebResourcesCircabc;
import eu.cec.digit.circabc.web.wai.bean.navigation.CategoryHeader;
import eu.cec.digit.circabc.web.wai.bean.navigation.CategoryItem;
import org.alfresco.web.app.Application;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.shared_impl.renderkit.RendererUtils;
import org.apache.myfaces.shared_impl.renderkit.html.HtmlRendererUtils;
import org.apache.myfaces.shared_impl.renderkit.html.util.FormInfo;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.ui.common.component.SelfRenderingComponent;

import javax.faces.application.StateManager;
import javax.faces.application.ViewHandler;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;
import java.io.IOException;
import java.util.*;

/**
 * @author Stephane Clinckart
 * <p>
 * Migration 3.1 -> 3.4.6 - 02/12/2011 SelfRenderingComponent was moved to Spring. This class seems
 * to be developed for CircaBC
 */
public class UICategoryList extends SelfRenderingComponent {

    // ------------------------------------------------------------------------------
    // Component implementation

    private static final Log logger = LogFactory.getLog(UICategoryList.class);
    /**
     * The categoryHeaders
     */
    private List<CategoryHeader> value;

    /**
     * @see javax.faces.component.UIComponent#getFamily()
     */
    public String getFamily() {
        return "eu.cec.digit.circabc.faces.CategoryList";
    }

    /**
     * @see javax.faces.component.UIComponentBase#encodeBegin(javax.faces.context.FacesContext)
     */
    @SuppressWarnings("unchecked")
    public void encodeBegin(final FacesContext context) throws IOException {
        if (isRendered() == false) {
            return;
        }
        final Locale contentLocaleLang = I18NUtil.getContentLocaleLang();
        I18NUtil.setContentLocale(new Locale("en"));

        final ValueBinding vbValue = getValueBinding("value");
        final List<CategoryHeader> categoryHeaders = (List<CategoryHeader>) vbValue
                .getValue(getFacesContext());

        final ValueBinding vbChooseHeader = getValueBinding("chooseHeader");

        final String chooseHeader = (vbChooseHeader != null ? (String) vbChooseHeader
                .getValue(getFacesContext()) : "");

        final Map<?, ?> attrs = this.getAttributes();
        final Boolean displayCategories = ((Boolean) (attrs.get("displayCategories") != null)
                ? (Boolean) attrs.get("displayCategories") : Boolean.FALSE);

        final ResponseWriter writer = context.getResponseWriter();

        writer.append("<div id=\"panelCatListHeaderGlobal\" class=\"panelCatListGlobal\">");
        writer.append("<div id=\"panelCatListHeader\" class=\"panelCatListLabel\" >");
        generateTitle(writer, chooseHeader);
        generateCategoryHeadersHasList(writer, categoryHeaders);
        writer.append("</div>");
        writer.append("</div>");

        if (displayCategories) {
            int indexCategory = 1;
            for (final CategoryHeader tmp : categoryHeaders) {
                final CategoryItem navigableNode = tmp.getCategoryHeaderItem();
                final String categoryHeaderTitle = (String) navigableNode
                        .getTitle();
                final String categoryHeaderId = (String) navigableNode.getId();

                writer.append("<div id=\"panel").append(categoryHeaderId)
                        .append("Global\" class=\"panelCatListGlobal\" >");
                generateTitle(writer, categoryHeaderTitle, categoryHeaderId);
                generateCategoriesHasList(writer, tmp);
                generateGoToTop(writer, indexCategory);
                indexCategory++;
                writer.append("</div>");
            }
        }

        I18NUtil.setContentLocale(contentLocaleLang);
    }

    private void generateGoToTop(final ResponseWriter writer, final int index)
            throws IOException {
        writer.append("<a href=\"#top\"");
        writer.append(" id=\"topOfPageAnchorCatListEndImg").append(String.valueOf(index)).append("\"");
        writer.append(" class=\"topOfPageAnchor\"");
        writer.append(" title=\"Back to the top of the page\" >");
        writer.append("Top of the page&nbsp;");
        generateTopAnchorImg(writer);
        writer.append("</a>");
    }

    private void generateTitle(final ResponseWriter writer, final String title)
            throws IOException {
        generateTitle(writer, title, null);
    }

    private void generateTitle(final ResponseWriter writer, final String title,
                               final String id) throws IOException {
        writer.append("<h3>");
        if (id != null) {
            writer.append("<a name=\"n").append(id).append("\" ></a>");
        }
        writer.append("<img src=\"").append(FacesContext.getCurrentInstance().getExternalContext()
                .getRequestContextPath())
                .append("/images/extension/expanded.gif\" alt=\".\" title=\".\" />");
        writer.append(title);
        writer.append("</h3>");
    }

    private void generateTopAnchorImg(final ResponseWriter writer)
            throws IOException {
        writer.append("<img src=\"").append(FacesContext.getCurrentInstance().getExternalContext()
                .getRequestContextPath()).append("/images/extension/top_ns.gif\"");
        writer.append(" alt=\"Back to the top of the page\"");
        writer.append(" title=\"Back to the top of the page\" />");
    }

    private void generateCategoryHeadersHasList(final ResponseWriter writer,
                                                final List<CategoryHeader> categoryHeaders) throws IOException {

        final int categoryHeaderCount = categoryHeaders.size();
        Collections.sort(categoryHeaders);
        if (categoryHeaderCount <= 10) {
            writer.append("<ul class=\"categoryListItem\" style=\"width:100%\">");
            int currentRow = 1;
            for (final CategoryHeader tmp : categoryHeaders) {
                final String liClass = (currentRow % 2 == 0) ? "recordSetRow"
                        : "recordSetRowAlt";
                writer.append("<li class=\"").append(liClass).append("\">");
                writer.append("<a href=\"#n").append(tmp.getCategoryHeaderItem().getId()).append("\"");
                writer.append(" >").append(tmp.getCategoryHeaderItem().getTitle()).append("</a>");
                writer.append("</li>");
                currentRow++;
            }
            writer.append("</ul>");
        } else {
            final int desiredColumnCount = 3;
            final int maxCategoryHeaderByList = (categoryHeaders.size() / desiredColumnCount) + 1;

            final Iterator<CategoryHeader> categoryHeaderIterator = categoryHeaders
                    .iterator();

            while (categoryHeaderIterator.hasNext()) {
                int currentCategoryHeaderCount = 0;
                final List<CategoryHeader> subCategoryHeaderList = new ArrayList<>(
                        10);
                while (categoryHeaderIterator.hasNext()
                        && currentCategoryHeaderCount < maxCategoryHeaderByList) {
                    subCategoryHeaderList.add(categoryHeaderIterator.next());
                    currentCategoryHeaderCount++;
                }
                writer.append("<ul class=\"categoryListItem\" style=\"width:"
                        + 100 / desiredColumnCount + "%\">");
                int currentRow = 1;

                if (subCategoryHeaderList.size() > 0) {
                    for (final CategoryHeader tmp : subCategoryHeaderList) {
                        // check if current row is even to determine style
                        final String liClass = (currentRow % 2 == 0) ? "recordSetRow"
                                : "recordSetRowAlt";
                        writer.append("<li class=\"").append(liClass).append("\">");
                        writer.append("<a href=\"#n").append(tmp.getCategoryHeaderItem().getId()).append("\"");
                        writer.append(" >").append(tmp.getCategoryHeaderItem().getTitle()).append("</a>");
                        writer.append("</li>");
                        currentRow++;
                    }
                }
                while (currentRow <= maxCategoryHeaderByList) {
                    // check if current row is even to determine style
                    final String liClass = (currentRow % 2 == 0) ? "recordSetRow"
                            : "recordSetRowAlt";
                    currentRow++;
                    writer.append("<li class=\"").append(liClass).append("\">");
                    writer.append("&nbsp;");
                    writer.append("</li>");
                }
                writer.append("</ul>");
            }
        }
    }

    private void generateCategoriesHasList(final ResponseWriter writer, final CategoryHeader tmp)
            throws IOException {
        // merge the items
        List<CategoryItem> allItems = new ArrayList<>();
        for (final CategoryItem category : tmp.getCategories()) {
            allItems.add(category);
        }
        for (final CategoryItem externalLink : tmp.getExternalLinks()) {
            allItems.add(externalLink);
        }

        // rendering arithmetics
        final int columnCount = (allItems.size() <= 10) ? 1 : 3;
        final int columnWidth = 100 / columnCount; // in percent
        final double columnLength = allItems.size() / (double) columnCount;
        final int columnLengthRounded = (int) Math.ceil(columnLength);
        Collections.sort(allItems);
        // write the columns
        for (int i = 0; i < columnCount; i++) {
            int offset = i * columnLengthRounded;
            writeColumn(writer, allItems, columnWidth, offset, columnLengthRounded);
        }
    }

    private void writeColumn(final ResponseWriter writer, List<CategoryItem> items, int width,
                             int offset, int count) throws IOException {
        writer.append("<ul class=\"categoryListItem\" style=\"width:").append(String.valueOf(width))
                .append("%\">");
        for (int i = offset; i < (offset + count); i++) {
            String liClass = (i % 2 == 0) ? "recordSetRow" : "recordSetRowAlt";
            try {
                CategoryItem item = items.get(i); //will cause IndexOutOfBoundsException
                if (item.getIsLink()) {
                    writeExternalLink(writer, item, liClass);
                } else {
                    writeCategoryLink(writer, item, liClass);
                }
            } catch (IndexOutOfBoundsException ioob) {
                //write empty list item to fill the list
                writeDummyLink(writer, liClass);
            }
        }
        writer.append("</ul>");
    }

    private void writeDummyLink(final ResponseWriter writer, String liClass) throws IOException {
        writer.append("<li class=\"").append(liClass).append("\">");
        writer.append("&nbsp;");
        writer.append("</li>");
    }

    private void writeCategoryLink(final ResponseWriter writer, final CategoryItem category,
                                   String liClass) throws IOException {
        String path = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
        String url =
                path + WebClientHelper.getGeneratedWaiUrl(category, ExtendedURLMode.HTTP_WAI_BROWSE, true);

        writer.append("<li class=\"").append(liClass).append("\">");
        writer.append("<a name=\"n").append(category.getTitle()).append("\"");
        writer.append(" href=\"").append(url).append("\"");
        writer.append(" onclick=\"showWaitProgress();\"");
        writer.append(" id=\"").append(category.getId()).append("\">");
        writer.append(category.getTitle());
        writer.append("</a>");
        writer.append("</li>");
    }

    private void writeExternalLink(final ResponseWriter writer, CategoryItem externalLink,
                                   String liClass) throws IOException {
        String path = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
        String url =
                path + WebClientHelper.getGeneratedWaiUrl(externalLink, ExtendedURLMode.HTTP_INLINE, true);

        writer.append("<li class=\"").append(liClass).append("\">");
        writer.append("<a name=\"n").append(externalLink.getTitle()).append("\"");
        writer.append(" href=\"").append(url).append("\"");
        writer.append(" target=\"_blank\"");
        writer.append(" id=\"").append(externalLink.getId()).append("\">");
        writer.append(externalLink.getTitle());
        writer.append("</a>");
        writer.append("</li>");
    }

    /**
     * get the href part of the tag
     *
     * @param facesContext The FacesContext
     * @param clientId     The clientId of the component
     */
    protected String getNonJavaScriptAnchor(final FacesContext facesContext,
                                            final String clientId) throws IOException {
        if (logger.isInfoEnabled()) {
            logger.info("UICategoryList : encodeBegin - renderNonJavaScriptAnchorStart - Start");
        }
        final ViewHandler viewHandler = facesContext.getApplication()
                .getViewHandler();
        final String viewId = facesContext.getViewRoot().getViewId();
        final String path = viewHandler.getActionURL(facesContext, viewId);

        String href = "";
        final StringBuilder hrefBuf = new StringBuilder(path);
        if (logger.isInfoEnabled()) {
            logger.info("hrefBuf -> path |" + hrefBuf + "|");
        }

        // add clientId parameter for decode

        if (path.indexOf('?') == -1) {
            hrefBuf.append('?');
        } else {
            hrefBuf.append("&amp;");
        }

        final FormInfo forInfo = RendererUtils.findNestingForm(this,
                facesContext);

        final String hiddenFieldName = HtmlRendererUtils
                .getHiddenCommandLinkFieldName(forInfo);
        if (logger.isInfoEnabled()) {
            logger.info("NavigationListRenderer : hiddenFieldName |"
                    + hiddenFieldName + "|");
        }
        hrefBuf.append(hiddenFieldName);
        hrefBuf.append('=');
        if (logger.isInfoEnabled()) {
            logger.info("NavigationListRenderer : clientId |" + clientId + "|");
        }
        hrefBuf.append(clientId);

        hrefBuf.append("&amp;");
        hrefBuf.append(RendererUtils.findNestingForm(this, facesContext)
                .getFormName());
        hrefBuf.append("_SUBMIT");
        hrefBuf.append('=');
        hrefBuf.append(1);

        final StateManager stateManager = facesContext.getApplication()
                .getStateManager();

        if (stateManager.isSavingStateInClient(facesContext)) {
            hrefBuf.append("&amp;");
            logger.info("NavigationListRenderer : isSavingStateInClient |"
                    + WebResourcesCircabc.URL_STATE_MARKER + "|");
            hrefBuf.append(WebResourcesCircabc.URL_STATE_MARKER);
        }

        // Migration 3.1 -> 3.4.6 - 12/01/2012
        // Adding this line to correct the improvement "isPostBack" from the newer version of alfresco (3.4.1)
        // Between the 3.1.2 to 3.4.1 of alfresco version, a test has been changed in the class RestoreViewExecutor.java (line 93),
        // uses the isPostBack method
        hrefBuf.append("&amp;").append(WebResourcesCircabc.URL_FIX_ISPOSTBACK);

        href = hrefBuf.toString();
        if (logger.isInfoEnabled()) {
            logger.info("href |" + href + "|");
        }
        href = facesContext.getExternalContext().encodeActionURL(href);
        if (logger.isDebugEnabled()) {
            logger.info("href encode |" + href + "|");
        }
        if (logger.isInfoEnabled()) {
            logger.info("UICategoryList : encodeBegin - renderNonJavaScriptAnchorStart - Stop");
        }
        return href;
    }

    protected String translate(final String key, final Object... params) {
        return Application.getMessage(FacesContext.getCurrentInstance(), key);
        //return WebClientHelper.translate(key, params);
    }

    /**
     * @see javax.faces.component.StateHolder#restoreState(javax.faces.context.FacesContext,
     * java.lang.Object)
     */
    public void restoreState(final FacesContext context, final Object state) {
        final Object values[] = (Object[]) state;
        // standard component attributes are restored by the super class
        super.restoreState(context, values[0]);
        this.value = (List<CategoryHeader>) restoreAttachedState(context,
                values[1]);
    }

    /**
     * @see javax.faces.component.StateHolder#saveState(javax.faces.context.FacesContext)
     */
    public Object saveState(FacesContext context) {
        Object values[] = new Object[2];
        // standard component attributes are saved by the super class
        values[0] = super.saveState(context);
        values[1] = saveAttachedState(context, this.value);
        return (values);
    }

    /**
     * @see javax.faces.component.UIComponentBase#getRendersChildren()
     */
    public boolean getRendersChildren() {
        return true;
    }

    /**
     * Get the value (for this component the value is an object used as the DataModel)
     *
     * @return the value
     */
    @SuppressWarnings("unchecked")
    public List<CategoryHeader> getValue() {
        final ValueBinding vb = getValueBinding("");
        if (vb != null) {
            this.value = (List<CategoryHeader>) vb.getValue(getFacesContext());
        }
        return this.value;
    }

    // ------------------------------------------------------------------------------
    // Private data

    /**
     * Set the value
     *
     * @param value the value
     */
    public void setValue(final List<CategoryHeader> value) {
        this.value = value;
        if (logger.isDebugEnabled()) {
            logger.debug("value setted " + value);
        }
    }
}
