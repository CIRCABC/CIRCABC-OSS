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
import eu.cec.digit.circabc.web.ui.common.component.data.UIMultiList;
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
public class MultiListRenderer extends BaseRenderer {
    // ------------------------------------------------------------------------------
    // Renderer implemenation

    private static final Log logger = LogFactory.getLog(MultiListRenderer.class);
    private int rowIndex = 0;

    /**
     * @see javax.faces.render.Renderer#encodeBegin(javax.faces.context.FacesContext,
     * javax.faces.component.UIComponent)
     */
    public void encodeBegin(final FacesContext context, final UIComponent component)
            throws IOException {
        // always check for this flag - as per the spec
        if (component.isRendered() == true) {
            logger.info("MultiList - encodeBegin");
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
    public void encodeChildren(final FacesContext context, final UIComponent component)
            throws IOException {
        if (component.isRendered() == true) {
            // the RichList component we are working with
            UIMultiList multiList = (UIMultiList) component;

            // prepare the component current row against the current page settings
            multiList.bind();

            // collect child column component then test if only one present
            List<UIColumn> columnList = new ArrayList<>();
            for (Object o : multiList.getChildren()) {
                UIComponent child = (UIComponent) o;
                if (child instanceof UIColumn) {
                    UIColumn column = (UIColumn) child;
                    if (column.isRendered()) {
                        columnList.add(column);
                    }
                }
            }

            // Optimisation de la liste
            UIColumn[] columns = new UIColumn[columnList.size()];
            columnList.toArray(columns);

            if (columnList.size() != multiList.getNumColumn()) {
                throw new IllegalStateException("The number of columns specified is incorrect");
            }

            // call render-before to output headers if required
            ResponseWriter out = context.getResponseWriter();
            out.write("<thead>");
            renderListBefore(context, multiList, columns);
            out.write("</thead>");
            out.write("<tbody>");
            if (multiList.isDataAvailable() == true) {
                while (multiList.isDataAvailable() == true) {
                    // render each row in turn
                    renderListRow(context, multiList, columns);
                }
            } else {
                // if no items present, render the facet with the "no items found" message
                out.write("<tr><td");
                String rowStyleClass = (String) multiList.getAttributes().get("rowStyleClass");
                if (rowStyleClass != null) {
                    out.write(" class=\"");
                    out.write(rowStyleClass);
                    out.write('"');
                }
                out.write('>');

                UIComponent emptyComponent = multiList.getEmptyMessage();
                if (emptyComponent != null) {
                    emptyComponent.encodeBegin(context);
                    emptyComponent.encodeChildren(context);
                    emptyComponent.encodeEnd(context);
                }
                out.write("</td></tr>");
            }
            // call render-after to output footers if required
            renderListAfter(context, multiList, columns);
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
            logger.info("MultiList - encodeEnd");
        }
    }

    /**
     * @see javax.faces.render.Renderer#getRendersChildren()
     */
    public boolean getRendersChildren() {
        // we are responsible for rendering our child components
        // this renderer is a valid use of this mode.
        return true;
    }

    /**
     * Callback executed by the RichList component to render any adornments before the main list rows
     * are rendered. This is generally used to output header items.
     *
     * @param context   FacesContext
     * @param multiList The parent MultiList component
     * @param columns   Array of columns to be shown
     */
    public void renderListBefore(FacesContext context, UIMultiList multiList, UIColumn[] columns)
            throws IOException {
        ResponseWriter out = context.getResponseWriter();

        // render column headers as labels
        out.write("<tr>");
        String headerClass = (String) multiList.getAttributes().get("headerStyleClass");

        for (UIColumn column : columns) {
            // render column header tag
            out.write("<th");
            String columnClass = (String) column.getAttributes().get("styleClass");
            outputAttribute(out, columnClass != null ? columnClass : headerClass, "class");
            out.write('>');

            // output the header facet if any
            UIComponent header = column.getHeader();
            if (header != null) {
                header.encodeBegin(context);
                if (header.getRendersChildren()) {
                    header.encodeChildren(context);
                }
                header.encodeEnd(context);
            }

            // we don't render child controls for the header row
            out.write("</th>");
        }
        out.write("</tr>");

        this.rowIndex = 0;
    }

    /**
     * Callback executed by the MutliList component once per row of data to be rendered. The bean used
     * as the current row data is provided, but generally rendering of the column data will be
     * performed by recursively encoding Column child components.
     *
     * @param context   FacesContext
     * @param multiList The parent MultiList component
     * @param columns   Array of columns to be shown
     */
    public void renderListRow(FacesContext context, UIMultiList multiList, UIColumn[] columns)
            throws IOException {
        ResponseWriter out = context.getResponseWriter();

        // Get the row
        ArrayList objectList = (ArrayList) multiList.nextRow();

        // output row or alt style row if set
        out.write("<tr");
        String rowStyle = (String) multiList.getAttributes().get("rowStyleClass");
        String altStyle = (String) multiList.getAttributes().get("altRowStyleClass");

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

        // output each column in turn and render all children
        for (int i = 0; i < columns.length; i++) {
            UIColumn column = columns[i];
            out.write("<td");
            outputAttribute(out, column.getAttributes().get("styleClass"), "class");
            out.write('>');

            // Render child only if data if present for this column
            if (i < objectList.size()) {
                // Render
                if (column.getChildCount() != 0) {
                    if (column == actionsColumn) {
                        out.write("<nobr>");
                    }

                    // allow child controls inside the columns to render themselves
                    Utils.encodeRecursive(context, column);

                    if (column == actionsColumn) {
                        out.write("</nobr>");
                    }
                }
            }
            out.write("</td>");
        }
        out.write("</tr>");
    }

    /**
     * Callback executed by the MultiList component to render any adornments after the main list rows
     * are rendered. This is generally used to output footer items.
     *
     * @param context   FacesContext
     * @param multiList The parent MultiList component
     * @param columns   Array of columns to be shown
     */
    public void renderListAfter(FacesContext context, UIMultiList multiList, UIColumn[] columns)
            throws IOException {
        Integer numColumn = (Integer) multiList.getAttributes().get("numColumn");
        // Children which are not UIClomun and have to be rendered
        ArrayList<UIComponent> childToRender = new ArrayList<>();
        // check if there are components to render
        if (!multiList.getChildren().isEmpty()) {
            for (Object o : multiList.getChildren()) {
                UIComponent child = (UIComponent) o;
                if ((child instanceof UIColumn == false) && (child.isRendered())) {
                    childToRender.add(child);
                }
            }
        }
        if (!childToRender.isEmpty()) {
            //	output all remaining child components that are not UIColumn
            ResponseWriter out = context.getResponseWriter();

            String nsColumn = numColumn > 1 ? "colspan=\"" + numColumn + "\"" : "";
            out.write("<tr><td " + nsColumn + ">");

            for (UIComponent component : childToRender) {
                Utils.encodeRecursive(context, component);
            }
            out.write("</td></tr>");
        }
    }
}
