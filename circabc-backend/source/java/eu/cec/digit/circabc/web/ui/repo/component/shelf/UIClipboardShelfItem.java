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
package eu.cec.digit.circabc.web.ui.repo.component.shelf;

import eu.cec.digit.circabc.business.helper.LuceneQueryHelper;
import eu.cec.digit.circabc.web.Beans;
import eu.cec.digit.circabc.web.ui.common.UtilsCircabc;
import eu.cec.digit.circabc.web.ui.common.WebResourcesCircabc;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.web.scripts.FileTypeImageUtils;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.DownloadContentServlet;
import org.alfresco.web.bean.clipboard.ClipboardItem;
import org.alfresco.web.bean.clipboard.ClipboardStatus;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.repo.WebResources;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.shared_impl.renderkit.RendererUtils;
import org.apache.myfaces.shared_impl.renderkit.html.HtmlRendererUtils;
import org.apache.myfaces.shared_impl.renderkit.html.util.FormInfo;

import javax.faces.component.NamingContainer;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.FacesEvent;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * @author Guillaume
 * @author Clinckart Stephane
 */
public class UIClipboardShelfItem extends
        org.alfresco.web.ui.repo.component.shelf.UIClipboardShelfItem {
    // ------------------------------------------------------------------------------
    // Component Impl

    /**
     * Url allowing copy
     */
    public static final String urlBrowse = "/jsp/browse/browse.jsp";
    private static final String STYLE_CLASS = "styleClass";
    private static final String ID = "id";
    private static final String _CLIPFINAL = "_clipfinal";
    private static final String CLASS = "class";
    private static final String ALT = "alt";
    private static final String TITLE = "title";
    private static final String HREF = "href";
    private static final String FALSE = "false";
    private static final String COLLAPSED = "collapsed";
    private static final String DIV_A = "<div><a";
    private static final String END_DIV = "</div>";
    private static final String NBSP_NBSP = "&nbsp;|&nbsp;";
    private static final String DIV = "<div";
    private static final String END_UL = "</ul>";
    private static final String END_LI = "</li>";
    private static final String A = "</a>";
    private static final String TARGET_NEW = "' target='new'>";
    private static final String A_HREF = "<a href='";
    private static final String NBSP = "&nbsp;";
    private static final String _16_GIF = "-16.gif";
    private static final String IMAGES_ICONS = "/images/icons/";
    private static final String LI = "<li>";
    private static final long MB = 1024 * 1024;
    /**
     * I18N messages
     */
    private static final String MSG_CLIPBOARD_COPY_TOOLTIP = "clipboard_copy_tooltip";
    private static final String MSG_CLIPBOARD_CUT_TOOLTIP = "clipboard_cut_tooltip";
    private static final String MSG_CLIPBOARD_ITEM_ICON_FILE_TOOLTIP = "clipboard_item_icon_file_tooltip";
    private static final String MSG_CLIPBOARD_ITEM_ICON_FOLDER_TOOLTIP = "clipboard_item_icon_folder_tooltip";
    private static final String MSG_CLIPBOARD_PASTE_ALL = "clipboard_paste_all";
    private static final String MSG_CLIPBOARD_PASTE_ALL_TOOLTIP = "clipboard_paste_all_tooltip";

    // ------------------------------------------------------------------------------
    // Strongly typed component property accessors
    private static final String MSG_CLIPBOARD_PASTE_ITEM_TOOLTIP = "clipboard_paste_item";
    private static final String MSG_CLIPBOARD_PASTE_LINK_TOOLTIP = "clipboard_paste_link";
    private static final String MSG_CLIPBOARD_REMOVE_ALL = "clipboard_remove_all";

    // ------------------------------------------------------------------------------
    // Private helpers
    private static final String MSG_CLIPBOARD_REMOVE_ALL_TOOLTIP = "clipboard_remove_all_tooltip";
    private static final String MSG_CLIPBOARD_DOWNLOAD_ALL = "clipboard_download_all";
    private static final String MSG_CLIPBOARD_DOWNLOAD_ALL_TOOLTIP = "clipboard_download_all_tooltip";
    private static final String MSG_CLIPBOARD_DOWNLOAD_ALL_DISABLED = "clipboard_download_all_disabled";
    private static final String MSG_CLIPBOARD_REMOVE_ITEM_TOOLTIP = "clipboard_remove_item";
    private static final String MSG_CLIPBOARD_TITLE = "clipboard_title";

    // ------------------------------------------------------------------------------
    // Private data
    private static final String MSG_CLIPBOARD_TITLE_COLLAPSED = "clipboard_title_collapsed";
    private static final String MSG_CLIPBOARD_TITLE_EXPANDED = "clipboard_title_expanded";
    private static final String MSG_CLIPBOARD_DOWNLOAD_ALL_LIMIT = "clipboard_download_all_limit";
    private static final String MSG_CLIPBOARD_DOWNLOAD_ALL_CUMULATED = "clipboard_download_all_cumulated";
    private static final String MSG_CLIPBOARD_DOWNLOAD_ALL_REMAINING = "clipboard_download_all_remaining";
    /**
     * Download All action
     */
    private static final int ACTION_DOWNLOAD_ALL = 5;
    /**
     * The logger
     */
    private static final Log logger = LogFactory.getLog(UIClipboardShelfItem.class);
    private static long downloadLimitMB = 50;
    /**
     * the style class
     */
    private String styleClass;
    /**
     * If true the box is collapsed
     */
    private boolean collapsed = false;
    /**
     * String to memorize the real View Id url to permit usurpation at the clipbard action level
     */
    private String realViewId = "";
    private MethodBinding downloadAllActionListener;

    /**
     * Default constructor
     */
    public UIClipboardShelfItem() {
        setRendererType(null);
    }

    /**
     * Encode the specified values for output to a hidden field
     *
     * @param action Action identifer
     * @param index  Index of the clipboard item the action is for
     * @return encoded values
     */
    private static String encodeValues(final int action, final int index) {
        return Integer.toString(action) + NamingContainer.SEPARATOR_CHAR + Integer.toString(index);
    }

    /**
     * Sets the value of the downloadLimitMB
     *
     * @param theDownloadLimitMB the downloadLimitMB to set.
     */
    public static void setDownloadLimitMB(long theDownloadLimitMB) {
        downloadLimitMB = theDownloadLimitMB;
    }

    /**
     * @see javax.faces.component.UIComponent#getFamily()
     */
    public String getFamily() {
        return "eu.cec.digit.circabc.faces.Shelf";
    }

    /**
     * @see javax.faces.component.StateHolder#restoreState(javax.faces.context.FacesContext,
     * java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    public void restoreState(final FacesContext context, final Object state) {
        final Object values[] = (Object[]) state;
        // standard component attributes are restored by the super class
        super.restoreState(context, values[0]);
        this.styleClass = (String) values[1];
        this.collapsed = (Boolean) values[2];
        this.downloadAllActionListener = (MethodBinding) restoreAttachedState(context, values[3]);
    }

    /**
     * @see javax.faces.component.StateHolder#saveState(javax.faces.context.FacesContext)
     */
    public Object saveState(final FacesContext context) {
        final Object values[] = new Object[4];
        // standard component attributes are saved by the super class
        values[0] = super.saveState(context);
        values[1] = this.styleClass;
        values[2] = this.collapsed;
        values[3] = saveAttachedState(context, this.downloadAllActionListener);
        return values;
    }

    /**
     * @see javax.faces.render.Renderer#decode(javax.faces.context.FacesContext,
     * javax.faces.component.UIComponent)
     */
    public void decode(final FacesContext context) {

        final String clientId = getId();
        final FormInfo formInfo = RendererUtils.findNestingForm(this, context);
        final String reqValue = (String) context.getExternalContext().getRequestParameterMap()
                .get(HtmlRendererUtils.getHiddenCommandLinkFieldName(formInfo));
        if (logger.isInfoEnabled()) {
            logger.info("decodeUIClipboardShelfItem " + HtmlRendererUtils
                    .getHiddenCommandLinkFieldName(formInfo));
        }
        if (reqValue != null && reqValue.equals(clientId)) {
            // Get all the params for this actionlink, see if any values have been set
            // on the request which match our params and set them into the component

            final Map requestMap = context.getExternalContext().getRequestParameterMap();
            final String paramValueCollapse = (String) requestMap.get(COLLAPSED);
            // If null, we don't change the render value
            if (paramValueCollapse != null) {
                this.collapsed = Boolean.valueOf(paramValueCollapse);
            } else {
                // We search for action on the clipboard
                final String paramValueActionClip = (String) requestMap.get("actionClip");
                // decode the values - we are expecting an action identifier and an index
                final int sepIndex = paramValueActionClip.indexOf(NamingContainer.SEPARATOR_CHAR);
                final int action = Integer.parseInt(paramValueActionClip.substring(0, sepIndex));
                final int index = Integer.parseInt(paramValueActionClip.substring(sepIndex + 1));
                // Nous usurpons la page du viewId car alfreco dans sa classe WorkspaceClipboardItem n'autorise la copie que de deux endroits que l'on ne peut redefinir aisement. On fait donc croire au moteur que l'on est a cette endroit mais on conserve notre url pour pouvoir retomber sur nos pieds plus tard.
                realViewId = context.getViewRoot().getViewId();

                // TODO check if keep or work around !!!
                // We are doing the action only if we are on the library
                //if (realViewId.equalsIgnoreCase(urlLibrary)) {
                //	context.getViewRoot().setViewId(urlBrowse);
                //	//raise an event to process the action later in the lifecycle
                //	ClipboardEvent event = new ClipboardEvent(this, action, index);
                //	this.queueEvent(event);
                //}

                context.getViewRoot().setViewId(urlBrowse);
                final ClipboardEvent event = new ClipboardEvent(this, action, index);
                this.queueEvent(event);
            }

            RendererUtils.initPartialValidationAndModelUpdate(this, context);
        }
    }

    /**
     * @see javax.faces.component.UIComponentBase#broadcast(javax.faces.event.FacesEvent)
     */
    public void broadcast(final FacesEvent event) throws AbortProcessingException {
        if (event instanceof ClipboardEvent) {
            // found an event we should handle
            final ClipboardEvent clipEvent = (ClipboardEvent) event;

            final List<ClipboardItem> items = getCollections();
            if (items.size() > clipEvent.Index) {
                // process the action
                switch (clipEvent.Action) {
                    case ACTION_REMOVE_ALL:
                        items.clear();
                        break;

                    case ACTION_REMOVE_ITEM:
                        items.remove(clipEvent.Index);
                        break;

                    case ACTION_PASTE_ALL:
                    case ACTION_PASTE_ITEM:
                    case ACTION_PASTE_LINK:
                        Utils.processActionMethod(getFacesContext(), getPasteActionListener(), clipEvent);
                        break;

                    case ACTION_DOWNLOAD_ALL:
                        Utils.processActionMethod(getFacesContext(), getDownloadAllActionListener(), clipEvent);
                        break;
                }
            }

            final FacesContext context = FacesContext.getCurrentInstance();
            if (clipEvent.Action == ACTION_DOWNLOAD_ALL) {
                //final Node currentNode = Beans.getWaiNavigator().getCurrentNode();
                //Beans.getWaiBrowseBean().clickWai(currentNode.getNodeRef());
                //Beans.getWaiBrowseBean().refreshBrowsing();
                //context.renderResponse();
            } else {
                //On revient vers notre page suite a l'usurpation pour permettre le fonctionnement du clipboard
                context.getViewRoot().setViewId(realViewId);
                Beans.getWaiBrowseBean().refreshBrowsing();
            }
        } else {
            super.broadcast(event);
        }
    }

    /**
     * @see javax.faces.component.UIComponentBase#encodeBegin(javax.faces.context.FacesContext)
     */
    public void encodeBegin(final FacesContext context) throws IOException {
        if (isRendered() == false) {
            return;
        }

        final ResponseWriter out = context.getResponseWriter();

        // Get the value binding for the styleClass
        getStyleClass();
        final ResourceBundle bundle = Application.getBundle(context);
        final Map<String, String> attributes = new HashMap<>();
        String tempClass = null;

        final List<ClipboardItem> items = getCollections();
        // Create the global container
        out.write(DIV);
        if (this.styleClass != null) {
            outputAttribute(out, this.styleClass, CLASS);
            outputAttribute(out, this.styleClass, ID);
            tempClass = this.styleClass + "Header";
        }
        out.write('>');

        // test if the box is collapsed and then render as state
        if (this.collapsed) {
            // Create the header part
            out.write(DIV_A);
            attributes.put(COLLAPSED, FALSE);
            outputAttribute(out, UtilsCircabc.generateHrefPart(context, this, getId(), attributes), HREF);
            outputAttribute(out, bundle.getString(MSG_CLIPBOARD_TITLE_COLLAPSED), TITLE);
            outputAttribute(out, bundle.getString(MSG_CLIPBOARD_TITLE_COLLAPSED), ALT);
            outputAttribute(out, tempClass, CLASS);
            out.write(">");
            out.write(UtilsCircabc.buildImageTag(context, WebResourcesCircabc.IMAGE_CLIPBOARD_COLLAPSED,
                    bundle.getString(MSG_CLIPBOARD_TITLE_COLLAPSED), tempClass));
            out.write(A);
            out.write(bundle.getString(MSG_CLIPBOARD_TITLE));
            out.write(END_DIV);
        } else {
            long totalDownloadSize = 0;

            // Create the header part
            out.write(DIV_A);
            attributes.put(COLLAPSED, "true");
            outputAttribute(out, UtilsCircabc.generateHrefPart(context, this, getId(), attributes), HREF);
            outputAttribute(out, bundle.getString(MSG_CLIPBOARD_TITLE_EXPANDED), TITLE);
            outputAttribute(out, bundle.getString(MSG_CLIPBOARD_TITLE_EXPANDED), ALT);
            outputAttribute(out, tempClass, CLASS);
            out.write(">");
            out.write(UtilsCircabc.buildImageTag(context, WebResourcesCircabc.IMAGE_CLIPBOARD_EXPANDED,
                    bundle.getString(MSG_CLIPBOARD_TITLE_EXPANDED),
                    bundle.getString(MSG_CLIPBOARD_TITLE_EXPANDED), tempClass));
            out.write(A);
            out.write(bundle.getString(MSG_CLIPBOARD_TITLE));
            out.write(END_DIV);

            // Create the list of items
            if (items.size() != 0) {

                final DictionaryService dd = Repository.getServiceRegistry(context).getDictionaryService();
                final NodeService nodeService = Repository.getServiceRegistry(context).getNodeService();

                out.write("<br/>");
                out.write("<ul>");
                String icon;
                String image;
                ClipboardItem item;
                QName type;
                boolean isFolder;
                for (int i = 0; i < items.size(); i++) {
                    item = items.get(i);

//					 check that the item has not been deleted since added to the clipboard
                    if (nodeService.exists(item.getNodeRef()) == false) {
                        // remove from clipboard
                        items.remove(i--);
                        continue;
                    }

                    if (nodeService.getType(item.getNodeRef()).equals(ContentModel.TYPE_CONTENT)) {
                        Object contentData = nodeService.getProperty(
                                item.getNodeRef(),
                                ContentModel.PROP_CONTENT);

                        if (contentData instanceof ContentData) {
                            totalDownloadSize += ((ContentData) contentData).getSize();
                        }
                    } else if (nodeService.getType(item.getNodeRef()).equals(ContentModel.TYPE_FOLDER)) {
                        totalDownloadSize += computeContentSizeOfFolder(item.getNodeRef(), context);
                    }

                    // start row with cut/copy state icon
                    out.write("<li>");
                    if (item.getMode() == ClipboardStatus.COPY) {
                        out.write(UtilsCircabc.buildImageTag(context, WebResources.IMAGE_COPY,
                                bundle.getString(MSG_CLIPBOARD_COPY_TOOLTIP)));
                    } else {
                        out.write(UtilsCircabc.buildImageTag(context, WebResources.IMAGE_CUT,
                                bundle.getString(MSG_CLIPBOARD_CUT_TOOLTIP)));
                    }
                    out.write(NBSP);

                    // then the icon item
                    type = item.getType();
                    isFolder = (ContentModel.TYPE_FOLDER.equals(type) || dd
                            .isSubClass(type, ContentModel.TYPE_FOLDER));
                    if (isFolder) {
                        // start row with correct node icon
                        icon = (String) item.getIcon();
                        if (icon != null) {
                            icon = IMAGES_ICONS + icon + _16_GIF;
                        } else {
                            icon = WebResources.IMAGE_SPACE;
                        }
                        out.write(UtilsCircabc.buildImageTag(context, icon,
                                bundle.getString(MSG_CLIPBOARD_ITEM_ICON_FOLDER_TOOLTIP)));
                    } else {
                        image = FileTypeImageUtils.getFileTypeImage(item.getName(), true);
                        out.write(Utils.buildImageTag(context, image,
                                bundle.getString(MSG_CLIPBOARD_ITEM_ICON_FILE_TOOLTIP)));
                    }
                    out.write(NBSP);

                    // then the truncked name (with or without link)
                    if (isFolder) {
                        out.write(Utils.cropEncode(item.getName()));
                    } else {
                        // output as a content download link
                        out.write(A_HREF);
                        out.write(context.getExternalContext().getRequestContextPath());
                        out.write(DownloadContentServlet.generateBrowserURL(item.getNodeRef(), item.getName()));
                        out.write(TARGET_NEW);
                        out.write(Utils.cropEncode(item.getName()));
                        out.write(A);
                    }
                    out.write(NBSP);

                    // output actions
                    buildActionLinkImage(out, ACTION_REMOVE_ITEM, i,
                            bundle.getString(MSG_CLIPBOARD_REMOVE_ITEM_TOOLTIP), WebResources.IMAGE_REMOVE);
                    out.write(NBSP);
                    buildActionLinkImage(out, ACTION_PASTE_ITEM, i,
                            bundle.getString(MSG_CLIPBOARD_PASTE_ITEM_TOOLTIP), WebResources.IMAGE_PASTE);
                    if (item.supportsLink() && item.getMode() == ClipboardStatus.COPY &&
                            (ContentModel.TYPE_LINK.equals(item.getType()) || dd
                                    .isSubClass(item.getType(), ContentModel.TYPE_LINK)) == false) {
                        out.write(NBSP);
                        buildActionLinkImage(out, ACTION_PASTE_LINK, i,
                                bundle.getString(MSG_CLIPBOARD_PASTE_LINK_TOOLTIP), WebResources.IMAGE_PASTE_LINK);
                    }

                    // end actions cell and end row
                    out.write("</li>");
                }

                out.write("</ul>");

                // test the item number yet. If the Collection was only containing deleted files, it s now empty DIGIT-CIRCABC-952
                if (items.size() > 0) {
                    //	output general actions if any clipboard items are present
                    out.write(DIV);
                    if (this.styleClass != null) {
                        outputAttribute(out, this.styleClass + _CLIPFINAL, ID);
                    }
                    out.write(" >");

                    buildActionLink(out, ACTION_PASTE_ALL, -1, bundle.getString(MSG_CLIPBOARD_PASTE_ALL),
                            bundle.getString(MSG_CLIPBOARD_PASTE_ALL_TOOLTIP));
                    out.write(NBSP_NBSP);
                    buildActionLink(out, ACTION_REMOVE_ALL, -1, bundle.getString(MSG_CLIPBOARD_REMOVE_ALL),
                            bundle.getString(MSG_CLIPBOARD_REMOVE_ALL_TOOLTIP));
                    out.write(NBSP_NBSP);
                    // Check if the total file size is less than downloadLimitMB MB to display the Download All link or disable it
                    if (totalDownloadSize < downloadLimitMB * MB) {
                        buildActionLink(out, ACTION_DOWNLOAD_ALL, -1,
                                bundle.getString(MSG_CLIPBOARD_DOWNLOAD_ALL),
                                bundle.getString(MSG_CLIPBOARD_DOWNLOAD_ALL_TOOLTIP));
                    } else {
                        out.write(bundle.getString(MSG_CLIPBOARD_DOWNLOAD_ALL) + "&nbsp;<i>" +
                                MessageFormat
                                        .format(bundle.getString(MSG_CLIPBOARD_DOWNLOAD_ALL_DISABLED), downloadLimitMB)
                                + "</i>");
                    }
                    out.write("<br/><br/>");
                    double remaining = (double) downloadLimitMB - (double) totalDownloadSize / MB;
                    out.write(bundle.getString(MSG_CLIPBOARD_DOWNLOAD_ALL_LIMIT) + ": " +
                            downloadLimitMB + " MB");
                    out.write("<br/>");
                    out.write(bundle.getString(MSG_CLIPBOARD_DOWNLOAD_ALL_CUMULATED) + ": " +
                            String.format("%.2f", (double) totalDownloadSize / MB) + " MB");
                    out.write("<br/>");
                    out.write(
                            "<font color=\"red\">" + bundle.getString(MSG_CLIPBOARD_DOWNLOAD_ALL_REMAINING) + ": "
                                    +
                                    (remaining < 0 ? 0 : String.format("%.2f", remaining)) + " MB</font>");
                    out.write(END_DIV);
                }
            }
        }

        out.write(END_DIV);
    }

    private long computeContentSizeOfFolder(NodeRef nodeRef, FacesContext context) {

        final SearchService searchService = Repository.getServiceRegistry(context).getSearchService();
        final NodeService nodeService = Repository.getServiceRegistry(context).getNodeService();

        Long resultSize = 0L;

        String currentLucenePath = LuceneQueryHelper.getPathFromSpaceRef(nodeRef);
        String documentsBelowSpaceQuery =
                "(PATH:\"" + currentLucenePath + "/*\") AND (TYPE:\"" + ContentModel.TYPE_CONTENT + "\")";

        SearchParameters sp = new SearchParameters();
        sp.addStore(nodeRef.getStoreRef());
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);

        ResultSet results = null;

        sp.setQuery(documentsBelowSpaceQuery);

        try {
            results = searchService.query(sp);

            for (NodeRef nTmp : results.getNodeRefs()) {
                Object contentData = nodeService.getProperty(
                        nTmp, ContentModel.PROP_CONTENT);

                if (contentData instanceof ContentData) {
                    resultSize += ((ContentData) contentData).getSize();
                }
            }
        } finally {
            if (results != null) {
                results.close();
            }
        }

        return resultSize;
    }

    /**
     * Get the CSS class to use for general html composant
     *
     * @return The CSS class to use for general html composant
     */
    public String getStyleClass() {
        final ValueBinding vb = getValueBinding(STYLE_CLASS);
        if (vb != null) {
            this.styleClass = (String) vb.getValue(getFacesContext());
        }

        return this.styleClass;
    }

    /**
     * Set the CSS class to use for general html composant
     *
     * @param styleClass The CSS class to use for general html composant
     */
    public void setStyleClass(final String styleClass) {
        this.styleClass = styleClass;
    }

    /**
     * Build HTML for an link representing a clipboard action as image and write it to the writer
     *
     * @param out    The responseWriter to write
     * @param action action identifier to represent
     * @param index  index of the clipboard item this action relates too
     * @param text   text of the action to display
     * @param image  image icon to display
     */
    private void buildActionLinkImage(final ResponseWriter out, final int action, final int index,
                                      final String text, final String image) throws IOException {
        final FacesContext context = getFacesContext();
        final Map<String, String> attributes = new HashMap<>();

        out.write("<a");
        attributes.put("actionClip", encodeValues(action, index));
        outputAttribute(out, UtilsCircabc.generateHrefPart(context, this, getId(), attributes), HREF);
        outputAttribute(out, text, TITLE);
        outputAttribute(out, text, ALT);
        out.write(">");
        out.write(UtilsCircabc.buildImageTag(context, image, text));
        out.write(A);

    }

    /**
     * Build HTML for an link representing a clipboard action as text and write it to the writer
     *
     * @param out     The responseWriter to write
     * @param action  action identifier to represent
     * @param index   index of the clipboard item this action relates too
     * @param text    text of the action to display
     * @param tooltip tooltip of the text
     */
    private void buildActionLink(final ResponseWriter out, final int action, final int index,
                                 final String text, final String tooltip) throws IOException {
        final FacesContext context = getFacesContext();
        final Map<String, String> attributes = new HashMap<>();

        out.write("<a");
        attributes.put("actionClip", encodeValues(action, index));
        outputAttribute(out, UtilsCircabc.generateHrefPart(context, this, getId(), attributes), HREF);
        outputAttribute(out, tooltip, TITLE);
        outputAttribute(out, tooltip, ALT);
        out.write(">");
        out.write(text);
        out.write(A);

    }

    /**
     * @return The MethodBinding to call when DownloadAll is selected by the user
     */
    public MethodBinding getDownloadAllActionListener() {
        return this.downloadAllActionListener;
    }

    /** Url on our page we allow copy */
    // TODO check if keep or work around !!!   public static final String urlLibrary = "/jsp/extension/circabc-library-home.jsp";

    /**
     * @param binding The MethodBinding to call when DownloadAll is selected by the user
     */
    public void setDownloadAllActionListener(final MethodBinding binding) {
        this.downloadAllActionListener = binding;
    }
}
