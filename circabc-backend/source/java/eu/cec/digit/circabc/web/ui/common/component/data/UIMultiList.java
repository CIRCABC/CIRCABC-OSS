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
package eu.cec.digit.circabc.web.ui.common.component.data;

import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.data.IDataContainer;
import org.alfresco.web.ui.common.component.data.GridArrayDataModel;
import org.alfresco.web.ui.common.component.data.GridListDataModel;
import org.alfresco.web.ui.common.component.data.IGridDataModel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * @author Guillaume
 */
public class UIMultiList extends UIComponentBase implements IDataContainer {
    // ------------------------------------------------------------------------------
    // Construction

    private static final Log logger = LogFactory.getLog(IDataContainer.class);

    // ------------------------------------------------------------------------------
    // Component implementation
    // component state
    private int currentPage = 0;
    private String sortColumn = null;
    private boolean sortDescending = true;
    private Object value = null;
    private IGridDataModel dataModel = null;
    /* Number of column by row */
    private int numColumn = 2;
    private int pageSize = -1;
    private String initialSortColumn = null;
    private boolean initialSortDescending = false;

    // ------------------------------------------------------------------------------
    // IDataContainer implementation
    // Index of the row in the data list
    private int rowIndex = -1;
    // Index of the row in the final output
    private int rowFinalIndex = -1;
    /* Number of row of data */
    private int maxRowIndex = -1;
    /* Number of row in the final output */
    private int maxRowFinalIndex = -1;
    private int pageCount = 1;

    /**
     * Default constructor
     */
    public UIMultiList() {
        setRendererType("eu.cec.digit.circabc.faces.MultiListRenderer");
    }

    /**
     * @see javax.faces.component.UIComponent#getFamily()
     */
    public String getFamily() {
        return "eu.cec.digit.circabc.faces.Data";
    }

    /**
     * @see javax.faces.component.StateHolder#restoreState(javax.faces.context.FacesContext,
     * java.lang.Object)
     */
    public void restoreState(FacesContext context, Object state) {
        Object values[] = (Object[]) state;
        // standard component attributes are restored by the super class
        super.restoreState(context, values[0]);
        this.currentPage = (Integer) values[1];
        this.sortColumn = (String) values[2];
        this.sortDescending = (Boolean) values[3];
        this.value = values[4]; // not serializable!
        this.dataModel = (IGridDataModel) values[5]; // not serializable!
        this.numColumn = (Integer) values[6];
        this.pageSize = (Integer) values[7];
        this.initialSortColumn = (String) values[8];
        this.initialSortDescending = (Boolean) values[9];
    }

    /**
     * @see javax.faces.component.StateHolder#saveState(javax.faces.context.FacesContext)
     */
    public Object saveState(FacesContext context) {
        Object values[] = new Object[10];
        // standard component attributes are saved by the super class
        values[0] = super.saveState(context);
        values[1] = this.currentPage;
        values[2] = this.sortColumn;
        values[3] = (this.sortDescending ? Boolean.TRUE : Boolean.FALSE);
        values[4] = this.value;
        values[5] = this.dataModel;
        values[6] = this.numColumn;
        values[7] = this.pageSize;
        values[8] = this.initialSortColumn;
        values[9] = (this.initialSortDescending ? Boolean.TRUE : Boolean.FALSE);

        return (values);
    }

    /**
     * Get the value (for this component the value is an object used as the DataModel)
     *
     * @return the value
     */
    public Object getValue() {
        if (this.value == null) {
            ValueBinding vb = getValueBinding("value");
            if (vb != null) {
                this.value = vb.getValue(getFacesContext());
            }
        }
        return this.value;
    }

    /**
     * Set the value (for this component the value is an object used as the DataModel)
     *
     * @param value the value
     */
    public void setValue(Object value) {
        this.dataModel = null;
        this.value = value;
    }

    /**
     * Clear the current sorting settings back to the defaults
     */
    public void clearSort() {
        this.sortColumn = null;
        this.sortDescending = true;
        this.initialSortColumn = null;
        this.initialSortDescending = false;
    }

    /**
     * Returns the number of column by row
     */
    public int getNumColumn() {
        ValueBinding vb = getValueBinding("numColumn");
        if (vb != null) {
            int numColumn = (Integer) vb.getValue(getFacesContext());
            if (numColumn > 2) {
                this.numColumn = numColumn;
            }
        }
        return this.numColumn;
    }

    /**
     * Sets the number of column by row.
     */
    public void setNumColumn(int val) {
        if (val > 2) {
            this.numColumn = val;
        }
    }

    // ------------------------------------------------------------------------------
    // UIRichList implementation

    /**
     * Return the UI Component to be used as the "no items available" message
     *
     * @return UIComponent
     */
    public UIComponent getEmptyMessage() {
        return getFacet("empty");
    }

    /**
     * Return the currently sorted column if any
     *
     * @return current sorted column if any
     */
    public String getCurrentSortColumn() {
        return this.sortColumn;
    }

    // ------------------------------------------------------------------------------
    // Private data

    /**
     * @see org.alfresco.web.data.IDataContainer#isCurrentSortDescending()
     */
    public boolean isCurrentSortDescending() {
        return this.sortDescending;
    }

    /**
     * @return Returns the initialSortColumn.
     */
    public String getInitialSortColumn() {
        return this.initialSortColumn;
    }

    /**
     * @param initialSortColumn The initialSortColumn to set.
     */
    public void setInitialSortColumn(String initialSortColumn) {
        this.initialSortColumn = initialSortColumn;
    }

    /**
     * @return Returns the initialSortDescending.
     */
    public boolean isInitialSortDescending() {
        return this.initialSortDescending;
    }

    /**
     * @param initialSortDescending The initialSortDescending to set.
     */
    public void setInitialSortDescending(boolean initialSortDescending) {
        this.initialSortDescending = initialSortDescending;
    }

    /**
     * Returns the current page size used for this list, or -1 for no paging.
     */
    public int getPageSize() {
        ValueBinding vb = getValueBinding("pageSize");
        if (vb != null) {
            int pageSize = (Integer) vb.getValue(getFacesContext());
            if (pageSize != this.pageSize) {
                // force a reset of the current page - else the bind may show a
                // page that isn't there
                setPageSize(pageSize);
            }
        }

        return this.pageSize;
    }

    /**
     * Sets the current page size used for the list.
     */
    public void setPageSize(int val) {
        if (val >= -1) {
            this.pageSize = val;
            setCurrentPage(0);
        }
    }

    /**
     * @see org.alfresco.web.data.IDataContainer#getPageCount()
     */
    public int getPageCount() {
        return this.pageCount;
    }

    /**
     * Return the current page the list is displaying
     *
     * @return current page zero based index
     */
    public int getCurrentPage() {
        return this.currentPage;
    }

    /**
     * @see org.alfresco.web.data.IDataContainer#setCurrentPage(int)
     */
    public void setCurrentPage(int index) {
        this.currentPage = index;
    }

    /**
     * Returns true if a row of data is available
     *
     * @return true if data is available, false otherwise
     */
    public boolean isDataAvailable() {
        return (this.rowFinalIndex) < this.maxRowFinalIndex;
    }

    /**
     * Returns the next row of data from the data model
     *
     * @return next row of data as a Bean object
     */
    @SuppressWarnings("unchecked")
    public Object nextRow() {
        // get next row and increment row count
        ArrayList objectList = new ArrayList(this.numColumn);
        for (int i = 0; i < this.numColumn; i++) {
            int index = this.rowIndex + 1 + (i * this.maxRowFinalIndex) + i;
            if (index <= maxRowIndex) {
                Object rowData = getDataModel().getRow(index);
                objectList.add(i, rowData);
            }
        }

        // Prepare the data-binding variable "var" ready for the next cycle of
        // renderering for the child components.
        String var = (String) getAttributes().get("var");
        if (var != null) {
            Map requestMap = getFacesContext().getExternalContext()
                    .getRequestMap();
            if (isDataAvailable() == true) {
                requestMap.put(var, objectList);
            } else {
                requestMap.remove(var);
            }
        }

        this.rowIndex++;
        this.rowFinalIndex++;

        return objectList;
    }

    /**
     * Sort the dataset using the specified sort parameters
     *
     * @param column     Column to sort
     * @param descending True for descending sort, false for ascending
     * @param mode       Sort mode to use (see IDataContainer constants)
     */
    public void sort(final String column, final boolean descending, final String mode) {
        this.sortColumn = column;
        this.sortDescending = descending;

        // delegate to the data model to sort its contents
        // place in a User Transaction as we may need to perform a LOT of node
        // calls to complete

        final FacesContext context = FacesContext.getCurrentInstance();
        final RetryingTransactionHelper txnHelper = Repository.getRetryingTransactionHelper(context);
        final RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>() {
            public Object execute() throws Throwable {
                getDataModel().sort(column, descending, mode);
                return null;
            }
        };
        txnHelper.doInTransaction(callback, true);
    }

    /**
     * Method called to bind the RichList component state to the data model value
     */
    public void bind() {
        int rowCount = getDataModel().size();
        // if a page size is specified, then we use that
        int pageSize = getPageSize();
        int elementByPage = pageSize * getNumColumn();
        if (pageSize != -1 && pageSize != 0) {
            // TODO : Verify the pagination process
            // calc start row index based on current page index
            this.rowIndex = (this.currentPage * elementByPage) - 1;

            // calc total number of pages available
            this.pageCount = (rowCount / (elementByPage)) + 1;
            if (rowCount % pageSize == 0 && this.pageCount != 1) {
                this.pageCount--;
            }

            // calc the maximum row index that can be returned
            this.maxRowIndex = this.rowIndex + elementByPage;
            if (this.maxRowIndex >= rowCount) {
                this.maxRowIndex = rowCount - 1;
            }
        }
        // else we are not paged so show all data from start
        else {
            this.rowIndex = -1;
            this.rowFinalIndex = -1;
            this.pageCount = 1;
            this.maxRowIndex = (rowCount - 1);
            //	Check if a row to display is not full : ie reste > 0
            int reste = rowCount % this.numColumn;
            if (reste > 0) {
                // A row is not full
                this.maxRowFinalIndex = (rowCount / this.numColumn);
            } else {
                this.maxRowFinalIndex = (rowCount / this.numColumn) - 1;
            }

        }
        if (logger.isDebugEnabled()) {
            logger.debug("Bound datasource: PageSize: " + pageSize
                    + "; CurrentPage: " + this.currentPage + "; RowIndex: "
                    + this.rowIndex + "; MaxRowIndex: " + this.maxRowIndex
                    + "; RowCount: " + rowCount + "; MaxElementByPage: " + elementByPage);
        }
    }

    /**
     * Return the data model wrapper
     *
     * @return IGridDataModel
     */
    private IGridDataModel getDataModel() {
        if (this.dataModel == null) {
            // build the appropriate data-model wrapper object
            Object val = getValue();
            if (val instanceof List) {
                this.dataModel = new GridListDataModel((List) val);
            } else if ((java.lang.Object[].class).isAssignableFrom(val
                    .getClass())) {
                this.dataModel = new GridArrayDataModel((Object[]) val);
            } else {
                throw new IllegalStateException(
                        "UIMultiList 'value' attribute binding should specify data model of a supported type!");
            }

            // sort first time on initially sorted column if set
            if (this.sortColumn == null) {
                String initialSortColumn = getInitialSortColumn();
                if (initialSortColumn != null
                        && initialSortColumn.length() != 0) {
                    boolean descending = isInitialSortDescending();

                    // TODO: add support for retrieving correct column sort mode
                    // here
                    this.sortColumn = initialSortColumn;
                    this.sortDescending = descending;
                }
            }
            if (this.sortColumn != null) {
                // delegate to the data model to sort its contents
                this.dataModel.sort(this.sortColumn, this.sortDescending,
                        IDataContainer.SORT_CASEINSENSITIVE);
            }

            // reset current page
            this.currentPage = 0;
        }

        return this.dataModel;
    }
}
