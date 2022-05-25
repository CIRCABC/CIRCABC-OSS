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

import javax.faces.context.FacesContext;
import java.io.IOException;

/**
 * Contract for implementations capable of rendering the columns for a Rich List component.
 *
 * @author Guillaume
 */
public interface IRichListRenderer extends
        org.alfresco.web.ui.common.renderer.data.IRichListRenderer {

    /**
     * Callback executed by the RichList component to render any adornments before the main list rows
     * are rendered. This is generally used to output header items.
     *
     * @param context  FacesContext
     * @param richList The parent RichList component
     * @param columns  Array of columns to be shown
     */
    void renderListBefore(FacesContext context, UIRichList richList, UIColumn[] columns)
            throws IOException;

    /**
     * Callback executed by the RichList component once per row of data to be rendered. The bean used
     * as the current row data is provided, but generally rendering of the column data will be
     * performed by recursively encoding Column child components.
     *
     * @param context  FacesContext
     * @param richList The parent RichList component
     * @param columns  Array of columns to be shown
     * @param row      The data bean for the current row
     */
    void renderListRow(FacesContext context, UIRichList richList, UIColumn[] columns, Object row)
            throws IOException;

    /**
     * Callback executed by the RichList component to render any adornments after the main list rows
     * are rendered. This is generally used to output footer items.
     *
     * @param context  FacesContext
     * @param richList The parent RichList component
     * @param columns  Array of columns to be shown
     */
    void renderListAfter(FacesContext context, UIRichList richList, UIColumn[] columns)
            throws IOException;

    /**
     * Return the unique view mode identifier that this renderer is responsible for.
     *
     * @return Unique view mode identifier for this renderer e.g. "icons" or "details"
     */
    String getViewModeID();
}
