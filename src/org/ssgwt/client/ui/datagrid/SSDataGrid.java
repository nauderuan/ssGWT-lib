/**
 * Copyright 2012 A24Group
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.ssgwt.client.ui.datagrid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.ssgwt.client.ui.datagrid.SSPager.TextLocation;
import org.ssgwt.client.ui.datagrid.event.DataGridRowSelectionChangedEvent;
import org.ssgwt.client.ui.datagrid.event.DataGridSortEvent;
import org.ssgwt.client.ui.datagrid.event.FilterChangeEvent;
import org.ssgwt.client.ui.datagrid.event.IDataGridEventHandler;
import org.ssgwt.client.ui.datagrid.event.ISelectAllEventHandler;
import org.ssgwt.client.ui.datagrid.event.SelectAllEvent;
import org.ssgwt.client.ui.datagrid.filter.AbstractHeaderFilter;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dev.resource.Resource;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.layout.client.Layout.Layer;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ClientBundle.Source;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.DataGrid.Style;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.RangeChangeEvent;

/**
 * The SSDataGrid with a changeable action bar that dispatches events for
 * sorting, filtering, selection clicking of row and paging
 * 
 * @author Johannes Gryffenberg
 * @since 29 June 2012
 */
public class SSDataGrid<T extends AbstractMultiSelectObject> extends Composite implements RequiresResize, FilterChangeEvent.FilterChangeHandler {

    /**
     * UiBinder interface for the composite
     * 
     * @author Johannes Gryffenberg
     * @since 29 June 2012
     */
    interface Binder extends UiBinder<Widget, SSDataGrid> {
    }

    /**
     * A ClientBundle that provides images for this widget.
     */
    public interface Resources extends ClientBundle {
        
        public static final Resources INSTANCE = GWT.create(Resources.class);
        
        /**
         * The styles used in this widget.
         */
        @Source("SSDataGrid.css")
        Style dataGridStyle();
    }
    
    public interface Style extends CssResource {
        
        /**
         * Applied to every cell.
         */
        @ClassName("actionBarStyle")
        String actionBarStyle();
        
        /**
         * Applied to the "No Content" label displayed.
         */
        @ClassName("noContentLabelStyle")
        String noContentLabelStyle();
        
    }
    
    /**
     * Whether the data grid supports selecting multiple rows
     */
    private boolean multiSelect;
    
    /**
     * Whether the data grid supports click actions
     */
    private boolean clickAction;
    
    /**
     * Instance of the UiBinder
     */
    private static Binder uiBinder = GWT.create(Binder.class);

    /**
     * The DataGrid that will be displayed on the screen
     */
    @UiField(provided = true)
    protected DataGrid<T> dataGrid;
    
    /**
     * The DataGrid that will be displayed on the screen
     */
    @UiField
    protected LayoutPanel actionBar;
    
    /**
     * The no content label.
     */
    @UiField
    protected Label noContentLabel;

    /**
     * The flow panel that will contain the action bar
     */
    @UiField
    FlowPanel actionBarContainer;
    
    /**
     * Sorting details of the columns
     */
    HashMap<Column, ColumnSortInfo> columnSortDetail = new HashMap<Column, ColumnSortInfo>();
    
    /**
     * The pager that will handle the paging of the DataGrid
     */
    @UiField(provided = true)
    protected SSPager pager;

    /**
     * The layout panel that holds all the Widget for the component
     */
    @UiField
    LayoutPanel mainContainer;
    
    /**
     * Class Constructor
     * 
     * @author Michael Barnard
     * @since 02 July 2012
     * 
     */
    public SSDataGrid() {
        this(false);
    }
    
    /**
     * Class Constructor
     * 
     * @author Michael Barnard
     * @since 2 July 2012
     * 
     * @param multiSelect - Whether the data grid supports multiple selects
     */
    public SSDataGrid(boolean multiSelect) {
        this((DataGrid.Resources)GWT.create(DataGridResources.class), (SSPager.Resources)GWT.create(SSPager.Resources.class), multiSelect);
    }
    
    /**
     * Class Constructor
     * 
     * @author Michael Barnard
     * @since 02 July 2012
     * 
     * @param dataGridResource - The resource that needs to be used for the data grid
     * @param pagerResource - The resource  that needs to be used for the pager
     */
    public SSDataGrid(DataGrid.Resources dataGridResource, SSPager.Resources pagerResource) {
        this(dataGridResource, pagerResource, false);
    }
    
    /**
     * Class Constructor
     * 
     * @author Michael Barnard
     * @since 02 July 2012
     * 
     * @param dataGridResource - The resource that needs to be used for the data grid
     * @param pagerResource - The resource  that needs to be used for the pager
     * @param multiSelect - Whether the data grid supports multiple selects
     */
    public SSDataGrid(DataGrid.Resources dataGridResource, SSPager.Resources pagerResource, boolean multiSelect) {
        dataGrid = new DataGrid<T>(10, dataGridResource);
        
        dataGrid.addColumnSortHandler(new ColumnSortEvent.Handler() {
            
            /**
             * Will be called on a column sort event
             * 
             * @param event - The event that initialise the handler
             */
            @Override
            public void onColumnSort(ColumnSortEvent event) {
                ColumnSortInfo columnSortInfo;
                if ((columnSortInfo = columnSortDetail.get(event.getColumn())) != null) {
                    columnSortDetail.remove(event.getColumn());
                    columnSortDetail.put(event.getColumn(), new ColumnSortInfo(event.getColumn(), !columnSortInfo.isAscending()));
                } else {
                    columnSortDetail.put(event.getColumn(), new ColumnSortInfo(event.getColumn(), true));
                }
                SSDataGrid.this.dataGrid.getColumnSortList().push(columnSortDetail.get(event.getColumn()));
                fireEvent(new DataGridSortEvent(event.getColumn(), columnSortDetail.get(event.getColumn()).isAscending()));
            }
        });
        
        pager = new SSPager(TextLocation.CENTER, pagerResource, false, 0, true);
        pager.setDisplay(dataGrid);
        setMultiSelect(multiSelect);
        setClickAction(false);
        this.initWidget(uiBinder.createAndBindUi(this));
        Resources.INSTANCE.dataGridStyle().ensureInjected();
        actionBar.setStyleName(Resources.INSTANCE.dataGridStyle().actionBarStyle());
        noContentLabel.setStyleName(Resources.INSTANCE.dataGridStyle().noContentLabelStyle());
    }

    /**
     * Set the complete list of values to display on one page
     * 
     * @param data - The data the should be displayed on the data grid
     * 
     * @author Lodewyk Duminy
     * @since 29 June 2012
     */
    public void setRowData(List<T> data) {
        if (data != null) {
            noContentLabel.setVisible(false);
            dataGrid.setRowData(data);
            refresh();
        } else {
            noContentLabel.setVisible(true);
        }
    }

    /**
     * Set the complete list of values to display on one page
     * 
     * @param startRow - The start row the data is for
     * @param data - The data the should be displayed on the data grid
     * 
     * @author Lodewyk Duminy
     * @since 29 June 2012
     */
    public void setRowData(int startRow, List<T> data) {
        if (data != null) {
            noContentLabel.setVisible(false);
            dataGrid.setRowData(startRow, data);
            refresh();
        } else {
            noContentLabel.setVisible(true);
        }
    }

    /**
     * Getter to retrieve the data currently being displayed on the DataGrid
     * 
     * @author Lodewyk Duminy
     * @since 29 June 2012
     * 
     * @return The data being displayed on the DataGrid
     */
    public List<T> getVisibleItems() {
        return dataGrid.getVisibleItems();
    }
    
    /**
     * Ensures that the datagrid has a scrollbar when the browser is too small
     * to display all the rows.
     * 
     * @author Lodewyk Duminy
     * @since 29 June 2012
     */
    @Override
    public void onResize() {
        dataGrid.setHeight((this.getOffsetHeight() - 40) + "px");
    }
    
    /**
     * Adds a column to the end of the table.
     * 
     * @param col the column to be added
     */
    public void addColumn(Column<T, ?> col) {
        col.setSortable(true);
        dataGrid.addColumn(col);
    }

    /**
     * Adds a column to the end of the table with an associated header.
     * 
     * @param col the column to be added
     * @param header the associated {@link Header}
     */
    public void addColumn(Column<T, ?> col, Header<?> header) {
        col.setSortable(true);
        if (header instanceof FilterSortHeader) {
            ((FilterSortHeader)header).addFilterChangeHandler(this);
        }
        dataGrid.addColumn(col, header);
    }

    /**
     * Adds a column to the end of the table with an associated header and footer.
     * 
     * @param col the column to be added
     * @param header the associated {@link Header}
     * @param footer the associated footer (as a {@link Header} object)
     */
    public void addColumn(Column<T, ?> col, Header<?> header, Header<?> footer) {
        col.setSortable(true);
        if (header instanceof FilterSortHeader) {
            ((FilterSortHeader)header).addFilterChangeHandler(this);
        }
        dataGrid.addColumn(col, header, footer);
    }

    /**
     * Adds a column to the end of the table with an associated String header.
     * 
     * @param col the column to be added
     * @param headerString the associated header text, as a String
     */
    public void addColumn(Column<T, ?> col, String headerString) {
        col.setSortable(true);
        dataGrid.addColumn(col, headerString);
    }

    /**
     * Adds a column to the end of the table with an associated {@link SafeHtml}
     * header.
     * 
     * @param col the column to be added
     * @param headerHtml the associated header text, as safe HTML
     */
    public void addColumn(Column<T, ?> col, SafeHtml headerHtml) {
        col.setSortable(true);
        dataGrid.addColumn(col, headerHtml);
    }

    /**
     * Adds a column to the end of the table with an associated String header and
     * footer.
     * 
     * @param col the column to be added
     * @param headerString the associated header text, as a String
     * @param footerString the associated footer text, as a String
     */
    public void addColumn(Column<T, ?> col, String headerString, String footerString) {
        col.setSortable(true);
        dataGrid.addColumn(col, headerString, footerString);
    }

    /**
     * Adds a column to the end of the table with an associated {@link SafeHtml}
     * header and footer.
     * 
     * @param col the column to be added
     * @param headerHtml the associated header text, as safe HTML
     * @param footerHtml the associated footer text, as safe HTML
     */
    public void addColumn(Column<T, ?> col, SafeHtml headerHtml, SafeHtml footerHtml) {
        col.setSortable(true);
        dataGrid.addColumn(col, headerHtml, footerHtml);
    }
    
    /**
     * Sets the amount of rows that the data grid will have. This
     * does not specify how many rows will be displayed.
     * 
     * @param count - The amount of rows that the data grid will have
     */
    public final void setRowCount(int count) {
        dataGrid.setRowCount(count);
    }

    /**
     * Sets the amount of rows that the data grid will have. This
     * does not specify how many rows will be displayed.
     * 
     * @param size - The amount of rows that the data grid will have
     * @param isExact - Whether or not the row measurement is exact
     */
    public void setRowCount(int size, boolean isExact) {
        dataGrid.setRowCount(size, isExact);
    }
    
    /**
     * Hides or shows the action bar
     * 
     * @param visible - The visibility of the action bar
     */
    public void setActionBarVisible(boolean visible) {
        actionBar.setVisible(visible);
        int actionBarHeight = 0;
        if ( visible ) {
            actionBarHeight = 32;
        }
        mainContainer.setWidgetBottomHeight(actionBar, 0, Unit.PX, actionBarHeight, Unit.PX);
        mainContainer.setWidgetTopBottom(dataGrid, actionBarHeight, Unit.PX, 0, Unit.PX);
    }
    
    /**
     * Hides the header by setting it invisible
     * 
     * TODO: Fix this. We need to be able to change the action bar back to visible
     */
    public void hideHeader() {
        //TODO: Add functionality for reusability
    }
    
    /**
     * Adds a widget to the action bar
     * 
     * @param actionBarWidget - The widget that needs to be added to the action bar
     */
    public void setActionBarWidget(Widget actionBarWidget) {
        this.actionBarContainer.add(actionBarWidget);
    }
    
    /**
     * Sets the size of the pager.
     * 
     * @param pageSize - The size of the pager
     */
    public void setPageSize(int pageSize) {
      pager.setPageSize(pageSize);
    }
    
    /**
     * Whether or not the datagrid supports multi select
     * 
     * @return Whether the data grid supports multi select
     */
    public boolean isMultiSelect() {
        return this.multiSelect;
    }
    
    /**
     * Set whether or not the grid should support multiple row select.
     * NOTE This needs to be set before any columns or data is set on the data grid.
     * 
     * @param multiSelect - Whether the data grid should support multiple row select
     */
    public void setMultiSelect(boolean multiSelect) {
        this.multiSelect = multiSelect;
        if (!multiSelect) {
            dataGrid.removeStyleName("isMultiSelect");
        } else {
            dataGrid.addStyleName("isMultiSelect");
            addMultiSelectField();
        }
    }
    
    /**
     * Add a field that supports multiple selection
     */
    public void addMultiSelectField() {
        Cell<Boolean> booleanCell = (Cell<Boolean>)new CheckboxCell();
        Column<T, Boolean> selectedColumn = new Column<T, Boolean>(booleanCell) {
            
            /**
             * Get the value of the multi select field
             * 
             * @return the selected state of the row
             */
            @Override
            public Boolean getValue(T object) {
                return object.isSelected();
            }
            
        };
        SelectAllHeader header = new SelectAllHeader();
        header.addEventHandler(new ISelectAllEventHandler() {
            
            /**
             * Event triggered when the select all button is clicked
             * 
             * @param event - The event that was fired
             */
            @Override
            public void onSelectAllEvent(SelectAllEvent event) {
                DataGrid dataGrid = SSDataGrid.this.dataGrid;
                
                Range rows = dataGrid.getVisibleRange();
                
                int end = rows.getStart() + rows.getLength();
                int numRecordsDisplayed = rows.getLength();
                if (end > dataGrid.getRowCount()) {
                    end = dataGrid.getRowCount();
                    numRecordsDisplayed = end % numRecordsDisplayed;
                }
                
                boolean allSelected = true;
                for (int i = 0; i < numRecordsDisplayed; i++) {
                    if (!((T)dataGrid.getVisibleItem(i)).isSelected()) {
                        allSelected = false;
                        break;
                    }
                }
                
                for (int i = 0; i < numRecordsDisplayed; i++) {
                    ((T)dataGrid.getVisibleItem(i)).setSelected(!allSelected);
                }
                DataGridRowSelectionChangedEvent<T> eventX = new DataGridRowSelectionChangedEvent<T>(
                        dataGrid.getVisibleItems());
                
                SSDataGrid.this.fireEvent(eventX);
                refresh();
            }
        });
        dataGrid.addColumn(selectedColumn, header);
        dataGrid.setColumnWidth(selectedColumn, "60px");
        selectedColumn.setFieldUpdater(new FieldUpdater<T, Boolean>() {

            /**
             * Update the selection state 
             * 
             * @param index - the index of the row to be updated
             * @param object - The object currently being referenced
             * @param value - the selected state of the current row
             */
            @Override
            public void update(int index, T object, Boolean value) {
                object.setSelected(value);
                refresh();
                List<T> selectedRowList = new ArrayList<T>();
                selectedRowList.add(object);
                DataGridRowSelectionChangedEvent<T> eventX = new DataGridRowSelectionChangedEvent<T>(selectedRowList);
                SSDataGrid.this.fireEvent(eventX);
            }
        });
    }
    
    /**
     * Refresh the data grid
     */
    private void refresh() {
        dataGrid.redraw();
        
        Range rows = dataGrid.getVisibleRange();
        
        int end = rows.getStart() + rows.getLength();
        int numRecordsDisplayed = rows.getLength();
        if (end > dataGrid.getRowCount()) {
            end = dataGrid.getRowCount();
            numRecordsDisplayed = end % numRecordsDisplayed;
        }
        for (int i = 0; i < numRecordsDisplayed; i++) {
            setRowSelectedStyle(i, dataGrid.getVisibleItem(i).isSelected());
        }
    }
    
    /**
     * Sets the style of a selected row at a certain index
     * 
     * @param rowIndex - The index of the row that needs to change
     * @param selected - Whether or not the row is selected
     */
    private void setRowSelectedStyle(int rowIndex, boolean selected) {
        TableRowElement tableRow = dataGrid.getRowElement(rowIndex);
        
        String styleNames = tableRow.getClassName();
        String newStyles = styleNames;
        boolean containsStyle = styleNames.contains("selectedRow");
        if (containsStyle && !selected) {
            newStyles = "";
            String[] styles = styleNames.split(" ");
            for (int i = 0; i < styles.length; i++) {
                if (!styles[i].equals("selectedRow")) {
                    newStyles += " " + styles[i];
                }
            }
        } else if (!containsStyle && selected) {
            if (!newStyles.equals("")) {
                newStyles += " ";
            }
            newStyles += "selectedRow ";
        }
        
        tableRow.setClassName(newStyles);
        
        Label l = new Label(" ");
        if (Window.Navigator.getAppName().equals("Microsoft Internet Explorer")) {
            tableRow.appendChild(l.getElement());
            
            tableRow.removeChild(l.getElement());
        }
    }
    
    /**
     * Gets the current click action state for the data grid
     * 
     * @return Whether the data grid has click action support
     */
    public boolean hasClickAction() {
        return this.clickAction;
    }
    
    /**
     * Gets the current click action state for the data grid
     *
     * @param clickAction - If the data grid should support click actions
     */
    public void setClickAction(boolean clickAction) {
        this.clickAction = clickAction;
        if (!clickAction) {
            dataGrid.removeStyleName("hasClickAction");
            dataGrid.addStyleName("noClickAction");
        } else {
            dataGrid.removeStyleName("noClickAction");
            dataGrid.addStyleName("hasClickAction");
        }
    }
    
    /**
     * Adds a event handler to the data grid
     * 
     * @param handler - The action handler to apply on the data grid
     * @return {@link HandlerRegistration} used to remove the handler
     */
    public HandlerRegistration addDataGridSortEvent(IDataGridEventHandler handler) {
        return this.addHandler(handler, DataGridSortEvent.TYPE);
    }
    
    /**
     * Adds a column to the data grid with a FilterSortHeader as a header
     * 
     * @param col - The column that should be added to the data grid
     * @param label - The label that should be displayed in the header
     * @param filterWidget - The filter widget that should be displayed if the user clicks on the filter icon
     */
    public void addFilterColumn(Column<T, ?> col, String label, AbstractHeaderFilter filterWidget) {
        FilterSortHeader header = new FilterSortHeader(label, filterWidget);
        header.addFilterChangeHandler(this);
        this.addColumn(col, header);
    }
    
    /**
     * Adds a event handler for the FilterChangeEvent
     * 
     * @param handler - The event handler
     * 
     * @return The handler registration object that will be used to remove the event handler
     */
    public HandlerRegistration addFilterChangeHandler(FilterChangeEvent.FilterChangeHandler handler) {
        return this.addHandler(handler, FilterChangeEvent.TYPE);
    }
    
    /**
     * Adds a event handler for the FilterChangeEvent
     * 
     * @param handler - The event handler
     * 
     * @return The handler registration object that will be used to remove the event handler
     */
    public HandlerRegistration addDataGridRowSelectionChangedHandler(DataGridRowSelectionChangedEvent.DataGridRowSelectionChangedHandler handler) {
        return this.addHandler(handler, DataGridRowSelectionChangedEvent.TYPE);
    }
    
    /**
     * Adds a range change event handler to the data grid
     * 
     * @param handler - The handler the will act on the RangeChangeEvent
     * 
     * @return The handler registration object
     */
    public HandlerRegistration addRangeChangeHandler(RangeChangeEvent.Handler handler) {
        return dataGrid.addRangeChangeHandler(handler);
    }
    
    /**
     * Adds a column to the data grid with a help icon as a header
     * 
     * @param helpWidget - The filter widget that should be displayed if the user clicks on the help icon
     * 
     * @author Ruan Naude<ruan.naude@a24group.com>
     * @since 4 July 2012
     */
    public void setHelpWidget(Widget helpWidget) {
        if (helpWidget != null) {
            HelpHeader header = new HelpHeader(helpWidget);
            // Add empty column for the help header
            TextColumn<T> helpColumn = new TextColumn<T>() {
                
                @Override
                public String getValue(T object) {
                    return "";
                }
                
            };
            dataGrid.addColumn(helpColumn, header);
            dataGrid.setColumnWidth(helpColumn, "26px");
        }
    }

    /**
     * Handles the FilterChangeEvent of the headers
     * 
     * @param event - The event that should be handled
     */
    @Override
    public void onFilterChange(FilterChangeEvent event) {
        FilterChangeEvent.fire(this);
    }
    
    /**
     * Indents the table with a specified amount of pixels this leaves the action bar on 100% width
     * 
     * @param pixels - The number of pixels the table should be indented with
     */
    public void indentTable(int pixels) {
        mainContainer.setWidgetLeftRight(dataGrid, pixels, Unit.PX, 0, Unit.PX);
    }
}
