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
package eu.cec.digit.circabc.web.ui.common.renderer.data;


import eu.cec.digit.circabc.web.ui.common.component.data.UIColumn;
import eu.cec.digit.circabc.web.ui.common.component.data.UIRichList;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.renderer.BaseRenderer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Guillaume
 */
public class RichListRenderer extends BaseRenderer {
    // ------------------------------------------------------------------------------
    // Renderer implemenation

    private static final Log logger = LogFactory.getLog(RichListRenderer.class);

    /**
     * @see javax.faces.render.Renderer#encodeBegin(javax.faces.context.FacesContext,
     * javax.faces.component.UIComponent)
     */
    public void encodeBegin(FacesContext context, UIComponent component)
            throws IOException {
        // always check for this flag - as per the spec
        if (component.isRendered() == true) {
            logger.info("RichList - encodeBegin");
            ResponseWriter out = context.getResponseWriter();
            Map attrs = component.getAttributes();
            out.write("<table");
            outputAttribute(out, attrs.get("styleClass"), "class");
            out.write(">");
        }
    }

    /**
     * @see javax.faces.render.Renderer#encodeChildren(javax.faces.context.FacesContext,
     * javax.faces.component.UIComponent)
     */
    public void encodeChildren(FacesContext context, UIComponent component)
            throws IOException {
        if (component.isRendered() == true) {
            // the RichList component we are working with
            UIRichList richList = (UIRichList) component;

            // prepare the component current row against the current page settings
            richList.bind();

            // collect child column components so they can be passed to the renderer
            List<UIColumn> columnList = new ArrayList<>(8);
            for (Object o : richList.getChildren()) {
                UIComponent child = (UIComponent) o;
                if (child instanceof UIColumn) {
                    columnList.add((UIColumn) child);
                }
            }

            UIColumn[] columns = new UIColumn[columnList.size()];
            columnList.toArray(columns);

            // get the renderer instance
            IRichListRenderer renderer = (IRichListRenderer) richList.getViewRenderer();
            if (renderer == null) {
                throw new IllegalStateException("IRichListRenderer must be available in UIRichList!");
            }

            // call render-before to output headers if required
            ResponseWriter out = context.getResponseWriter();
            out.write("<thead>");
            renderer.renderListBefore(context, richList, columns);
            out.write("</thead>");
            out.write("<tbody>");
            if (richList.isDataAvailable() == true) {
                while (richList.isDataAvailable() == true) {
                    // render each row in turn
                    renderer.renderListRow(context, richList, columns, richList.nextRow());
                }
            } else {
                // if no items present, render the facet with the "no items found" message
                out.write("<tr><td");
                String rowStyleClass = (String) richList.getAttributes().get("rowStyleClass");
                if (rowStyleClass != null) {
                    out.write(" class=\"");
                    out.write(rowStyleClass);
                    out.write('"');
                }
                out.write('>');

                UIComponent emptyComponent = richList.getEmptyMessage();
                if (emptyComponent != null) {
                    emptyComponent.encodeBegin(context);
                    emptyComponent.encodeChildren(context);
                    emptyComponent.encodeEnd(context);
                }
                out.write("</td></tr>");
            }
            // call render-after to output footers if required
            renderer.renderListAfter(context, richList, columns);
            out.write("</tbody>");
        }
    }

    /**
     * @see javax.faces.render.Renderer#encodeEnd(javax.faces.context.FacesContext,
     * javax.faces.component.UIComponent)
     */
    public void encodeEnd(FacesContext context, UIComponent component)
            throws IOException {
        // always check for this flag - as per the spec
        if (component.isRendered() == true) {
            ResponseWriter out = context.getResponseWriter();
            out.write("</table>");
            logger.info("RichList - encodeEnd");
        }
    }

    // ------------------------------------------------------------------------------
    // Inner classes

    /**
     * @see javax.faces.render.Renderer#getRendersChildren()
     */
    public boolean getRendersChildren() {
        // we are responsible for rendering our child components
        // this renderer is a valid use of this mode - it can render the various
        // column descriptors as a number of different list view types e.g.
        // details, icons, list etc.
        return true;
    }

    /**
     * Class to implement a Circa view for the RichList component
     *
     * @author Guillaume
     */
    public static class CircaViewRenderer implements IRichListRenderer {

        public static final String VIEWMODEID = "circa";
        /**
         *
         */
        private static final long serialVersionUID = -8613868143625579365L;
        private int rowIndex = 0;
        /* The number of Column which are rendered */
        private int numColumnRender = 0;

        public String getViewModeID() {
            return VIEWMODEID;
        }

        public void renderListBefore(FacesContext context, UIRichList richList, UIColumn[] columns)
                throws IOException {
            ResponseWriter out = context.getResponseWriter();

            // render column headers as labels
            out.write("<tr>");
            String headerClass = (String) richList.getAttributes().get("headerStyleClass");
            for (UIColumn column : columns) {
                if (column.isRendered() == true || column.getPrimary()) {
                    // Increment the number of rendered columns
                    numColumnRender++;
                    // render column header tag
                    out.write("<th");
                    String columnClass = (String) column.getAttributes().get("styleClass");
                    outputAttribute(out, columnClass != null ? columnClass : headerClass, "class");

                    if (column.getActions()) {
                        outputAttribute(out, "width:15%;", "style");
                    }

                    out.write('>');

                    // output the header facet if any
                    UIComponent header = column.getHeader();
                    if (header != null) {
                        FacesHelper.setupComponentId(context, header, null);
                        header.encodeBegin(context);
                        if (header.getRendersChildren()) {
                            header.encodeChildren(context);
                        }
                        header.encodeEnd(context);
                    }

                    // we don't render child controls for the header row
                    out.write("</th>");
                }
            }
            out.write("</tr>");

            this.rowIndex = 0;
        }

        public void renderListRow(FacesContext context, UIRichList richList, UIColumn[] columns,
                                  Object row)
                throws IOException {
            ResponseWriter out = context.getResponseWriter();

            // output row or alt style row if set
            out.write("<tr");
            String rowStyle = (String) richList.getAttributes().get("rowStyleClass");
            String altStyle = (String) richList.getAttributes().get("altRowStyleClass");
            if (altStyle != null && (this.rowIndex++ & 1) == 1) {
                rowStyle = altStyle;
            }
            outputAttribute(out, rowStyle, "class");
            out.write('>');

            // find the actions column if it exists
            UIColumn actionsColumn = null;
            for (UIColumn column1 : columns) {
                if (column1.isRendered() == true && column1.getActions() == true) {
                    actionsColumn = column1;
                    break;
                }
            }

            // Boolean to tell if the icon has already been put
            boolean renderedFirst = false;
            // output each column in turn and render all children
            for (UIColumn column : columns) {
                if (column.isRendered() == true) {
                    out.write("<td");
                    outputAttribute(out, column.getAttributes().get("styleClass"), "class");
                    out.write('>');

                    // for details view, we show the small column icon for the first column
                    if (renderedFirst == false) {
                        UIComponent smallIcon = column.getSmallIcon();
                        if (smallIcon != null) {
                            smallIcon.encodeBegin(context);
                            if (smallIcon.getRendersChildren()) {
                                smallIcon.encodeChildren(context);
                            }
                            smallIcon.encodeEnd(context);
                            out.write("&nbsp;");
                        }
                        renderedFirst = true;
                    }

                    // The Standard content
                    if (column.getChildCount() != 0) {
                        if (column == actionsColumn) {
                            out.write("<nobr><span class=\"actionContainer\">");
                        }

                        // allow child controls inside the columns to render themselves

                        List<UIComponent> colChildren = column.getChildren();
                        for (UIComponent uiComp : colChildren) {
                            final Object escape = uiComp.getAttributes().get("escape");
                            if (escape == null) {
                                uiComp.getAttributes().put("escape", Boolean.TRUE);
                            }
                        }
                        Utils.encodeRecursive(context, column);

                        if (column == actionsColumn) {
                            out.write("</span></nobr>");
                        }
                    }

                    out.write("</td>");
                }
            }
            out.write("</tr>");
        }

        public void renderListAfter(FacesContext context, UIRichList richList, UIColumn[] columns)
                throws IOException {
            // Children which are not UIClomun and have to be rendered
            ArrayList<UIComponent> childToRender = new ArrayList<>();
            // check if there are components to render
            if (!richList.getChildren().isEmpty()) {
                for (Object o : richList.getChildren()) {
                    UIComponent child = (UIComponent) o;
                    if ((child instanceof UIColumn == false) && (child.isRendered())) {
                        childToRender.add(child);
                    }
                }
            }
            if (!childToRender.isEmpty()) {
                //	output all remaining child components that are not UIColumn
                ResponseWriter out = context.getResponseWriter();

                String nsColumn = numColumnRender > 1 ? "colspan=\"" + numColumnRender + "\"" : "";
                out.write("<tr><td " + nsColumn + ">");

                for (UIComponent component : childToRender) {
                    Utils.encodeRecursive(context, component);
                }
                out.write("</td></tr>");
            }
        }

        public void renderListAfter(FacesContext context,
                                    org.alfresco.web.ui.common.component.data.UIRichList richList,
                                    org.alfresco.web.ui.common.component.data.UIColumn[] columns) throws IOException {
            throw new IllegalStateException("Original Alfresco JSF client not managed");
        }

        public void renderListBefore(FacesContext context,
                                     org.alfresco.web.ui.common.component.data.UIRichList richList,
                                     org.alfresco.web.ui.common.component.data.UIColumn[] columns) throws IOException {
            throw new IllegalStateException("Original Alfresco JSF client not managed");
        }

        public void renderListRow(FacesContext context,
                                  org.alfresco.web.ui.common.component.data.UIRichList richList,
                                  org.alfresco.web.ui.common.component.data.UIColumn[] columns, Object row)
                throws IOException {
            throw new IllegalStateException("Original Alfresco JSF client not managed");
        }
    }
}
