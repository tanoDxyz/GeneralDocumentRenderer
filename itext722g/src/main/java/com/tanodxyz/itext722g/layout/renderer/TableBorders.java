/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2022 iText Group NV
    Authors: iText Software.

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation with the addition of the
    following permission added to Section 15 as permitted in Section 7(a):
    FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
    ITEXT GROUP. ITEXT GROUP DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
    OF THIRD PARTY RIGHTS

    This program is distributed in the hope that it will be useful, but
    WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
    or FITNESS FOR A PARTICULAR PURPOSE.
    See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License
    along with this program; if not, see http://www.gnu.org/licenses or write to
    the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
    Boston, MA, 02110-1301 USA, or download the license from the following URL:
    http://itextpdf.com/terms-of-use/

    The interactive user interfaces in modified source and object code versions
    of this program must display Appropriate Legal Notices, as required under
    Section 5 of the GNU Affero General Public License.

    In accordance with Section 7(b) of the GNU Affero General Public License,
    a covered work must retain the producer line in every PDF that is created
    or manipulated using iText.

    You can be released from the requirements of the license by purchasing
    a commercial license. Buying such a license is mandatory as soon as you
    develop commercial activities involving the iText software without
    disclosing the source code of your own applications.
    These activities include: offering paid services to customers as an ASP,
    serving PDFs on the fly in a web application, shipping iText with a closed
    source product.

    For more information, please contact iText Software Corp. at this
    address: sales@itextpdf.com
 */
package com.tanodxyz.itext722g.layout.renderer;


import com.tanodxyz.itext722g.io.logs.IoLogMessageConstant;
import com.tanodxyz.itext722g.kernel.geom.Rectangle;
import com.tanodxyz.itext722g.kernel.pdf.canvas.PdfCanvas;
import com.tanodxyz.itext722g.layout.borders.Border;
import com.tanodxyz.itext722g.layout.properties.Property;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

abstract class TableBorders {
    /**
     * Horizontal borders of the table.
     *
     * It consists of a list, each item of which represents
     * a horizontal border of a row, each of them is a list of borders of the cells.
     * The amount of the lists is the number of rows + 1, the size of each of these lists
     * corresponds to the number of columns.
     */
    protected List<List<Border>> horizontalBorders = new ArrayList<>();

    /**
     * Vertical borders of the table.
     *
     * It consists of a list, each item of which represents
     * a vertical border of a row, each of them is a list of borders of the cells.
     * The amount of the lists is the number of columns + 1, the size of each of these lists
     * corresponds to the number of rows.
     */
    protected List<List<Border>> verticalBorders = new ArrayList<>();

    /**
     * The number of the table's columns.
     */
    protected final int numberOfColumns;

    /**
     * The outer borders of the table (as body).
     */
    protected Border[] tableBoundingBorders = new Border[4];

    /**
     * All the cells of the table.
     *
     * Each item of the list represents a row and consists of its cells.
     */
    protected List<CellRenderer[]> rows;

    /**
     * The first row, which should be processed on this area.
     *
     * The value of this field varies from area to area.
     * It's zero-based and inclusive.
     */
    protected int startRow;
    /**
     * The last row, which should be processed on this area.
     *
     * The value of this field varies from area to area.
     * It's zero-based and inclusive. The last border will have index (finishRow+1) because
     * the number of borders is greater by one than the number of rows
     */
    protected int finishRow;

    /**
     * The width of the widest left border.
     */
    protected float leftBorderMaxWidth;

    /**
     * The width of the widest right border.
     */
    protected float rightBorderMaxWidth;

    /**
     * The number of rows flushed to the table.
     *
     * Its value is zero for regular tables. The field makes sense only for large tables.
     */
    protected int largeTableIndexOffset = 0;

    public TableBorders(List<CellRenderer[]> rows, int numberOfColumns, Border[] tableBoundingBorders) {
        this.rows = rows;
        this.numberOfColumns = numberOfColumns;
        setTableBoundingBorders(tableBoundingBorders);
    }

    public TableBorders(List<CellRenderer[]> rows, int numberOfColumns, Border[] tableBoundingBorders, int largeTableIndexOffset) {
        this(rows, numberOfColumns, tableBoundingBorders);
        this.largeTableIndexOffset = largeTableIndexOffset;
    }
    // region abstract

    // region draw
    protected abstract TableBorders drawHorizontalBorder(PdfCanvas canvas, TableBorderDescriptor borderDescriptor);

    protected abstract TableBorders drawVerticalBorder(PdfCanvas canvas, TableBorderDescriptor borderDescriptor);
    // endregion

    // region area occupation
    protected abstract TableBorders applyTopTableBorder(Rectangle occupiedBox, Rectangle layoutBox, boolean isEmpty, boolean force, boolean reverse);

    protected abstract TableBorders applyTopTableBorder(Rectangle occupiedBox, Rectangle layoutBox, boolean reverse);

    protected abstract TableBorders applyBottomTableBorder(Rectangle occupiedBox, Rectangle layoutBox, boolean isEmpty, boolean force, boolean reverse);

    protected abstract TableBorders applyBottomTableBorder(Rectangle occupiedBox, Rectangle layoutBox, boolean reverse);

    protected abstract TableBorders applyLeftAndRightTableBorder(Rectangle layoutBox, boolean reverse);

    protected abstract TableBorders skipFooter(Border[] borders);

    protected abstract TableBorders skipHeader(Border[] borders);

    protected abstract TableBorders collapseTableWithFooter(TableBorders footerBordersHandler, boolean hasContent);

    protected abstract TableBorders collapseTableWithHeader(TableBorders headerBordersHandler, boolean updateBordersHandler);

    protected abstract TableBorders fixHeaderOccupiedArea(Rectangle occupiedBox, Rectangle layoutBox);

    protected abstract TableBorders applyCellIndents(Rectangle box, float topIndent, float rightIndent, float bottomIndent, float leftIndent, boolean reverse);
    // endregion

    // region getters
    abstract public List<Border> getVerticalBorder(int index);

    abstract public List<Border> getHorizontalBorder(int index);

    protected abstract float getCellVerticalAddition(float[] indents);
    // endregion

    protected abstract void buildBordersArrays(CellRenderer cell, int row, int col, int[] rowspansToDeduct);

    protected abstract TableBorders updateBordersOnNewPage(boolean isOriginalNonSplitRenderer, boolean isFooterOrHeader,  TableRenderer currentRenderer,  TableRenderer headerRenderer,  TableRenderer footerRenderer);
    // endregion

    protected TableBorders processAllBordersAndEmptyRows() {
        CellRenderer[] currentRow;
        int[] rowspansToDeduct = new int[numberOfColumns];
        int numOfRowsToRemove = 0;
        if (!rows.isEmpty()) {
            for (int row = startRow - largeTableIndexOffset; row <= finishRow - largeTableIndexOffset; row++) {
                currentRow = rows.get(row);
                boolean hasCells = false;
                for (int col = 0; col < numberOfColumns; col++) {
                    if (null != currentRow[col]) {
                        int colspan = (int) currentRow[col].getPropertyAsInteger(Property.COLSPAN);
                        if (rowspansToDeduct[col] > 0) {
                            int rowspan = (int) currentRow[col].getPropertyAsInteger(Property.ROWSPAN) - rowspansToDeduct[col];
                            if (rowspan < 1) {
                                Logger logger = Logger.getLogger( TableRenderer.class.getName());
                                logger.warning(IoLogMessageConstant.UNEXPECTED_BEHAVIOUR_DURING_TABLE_ROW_COLLAPSING);
                                rowspan = 1;
                            }
                            currentRow[col].setProperty(Property.ROWSPAN, rowspan);
                            if (0 != numOfRowsToRemove) {
                                removeRows(row - numOfRowsToRemove, numOfRowsToRemove);
                                row -= numOfRowsToRemove;
                                numOfRowsToRemove = 0;
                            }
                        }
                        buildBordersArrays(currentRow[col], row, col, rowspansToDeduct);
                        hasCells = true;
                        for (int i = 0; i < colspan; i++) {
                            rowspansToDeduct[col + i] = 0;
                        }
                        col += colspan - 1;
                    } else {
                        if (horizontalBorders.get(row).size() <= col) {
                            horizontalBorders.get(row).add(null);
                        }
                    }
                }
                if (!hasCells) {
                    if (row == rows.size() - 1) {
                        removeRows(row - rowspansToDeduct[0], rowspansToDeduct[0]);
                        // delete current row
                        rows.remove(row - rowspansToDeduct[0]);
                        setFinishRow(finishRow - 1);

                        Logger logger = Logger.getLogger( TableRenderer.class.getName());
                        logger.warning(IoLogMessageConstant.LAST_ROW_IS_NOT_COMPLETE);
                    } else {
                        for (int i = 0; i < numberOfColumns; i++) {
                            rowspansToDeduct[i]++;
                        }
                        numOfRowsToRemove++;
                    }
                }
            }
        }
        if (finishRow < startRow) {
            setFinishRow(startRow);
        }
        return this;
    }

    private void removeRows(int startRow, int numOfRows) {
        for (int row = startRow; row < startRow + numOfRows; row++) {
            rows.remove(startRow);
            horizontalBorders.remove(startRow + 1);
            for (int j = 0; j <= numberOfColumns; j++) {
                verticalBorders.get(j).remove(startRow + 1);
            }
        }
        setFinishRow(finishRow - numOfRows);
    }

    // region init
    protected TableBorders initializeBorders() {
        List<Border> tempBorders;
        // initialize vertical borders
        while (numberOfColumns + 1 > verticalBorders.size()) {
            tempBorders = new ArrayList<Border>();
            while ((int) Math.max(rows.size(), 1) > tempBorders.size()) {
                tempBorders.add(null);
            }
            verticalBorders.add(tempBorders);
        }
        // initialize horizontal borders
        while ((int) Math.max(rows.size(), 1) + 1 > horizontalBorders.size()) {
            tempBorders = new ArrayList<Border>();
            while (numberOfColumns > tempBorders.size()) {
                tempBorders.add(null);
            }
            horizontalBorders.add(tempBorders);
        }
        return this;
    }
    // endregion

    // region setters
    protected TableBorders setTableBoundingBorders(Border[] borders) {
        tableBoundingBorders = new Border[4];
        if (null != borders) {
            for (int i = 0; i < borders.length; i++) {
                tableBoundingBorders[i] = borders[i];
            }
        }
        return this;
    }

    protected TableBorders setRowRange(int startRow, int finishRow) {
        this.startRow = startRow;
        this.finishRow = finishRow;
        return this;
    }

    protected TableBorders setStartRow(int row) {
        this.startRow = row;
        return this;
    }

    protected TableBorders setFinishRow(int row) {
        this.finishRow = row;
        return this;
    }
    // endregion

    // region getters
    public float getLeftBorderMaxWidth() {
        return leftBorderMaxWidth;
    }

    public float getRightBorderMaxWidth() {
        return rightBorderMaxWidth;
    }

    public float getMaxTopWidth() {
        float width = 0;
        Border widestBorder = getWidestHorizontalBorder(startRow);
        if (null != widestBorder && widestBorder.getWidth() >= width) {
            width = widestBorder.getWidth();
        }
        return width;
    }

    public float getMaxBottomWidth() {
        float width = 0;
        Border widestBorder = getWidestHorizontalBorder(finishRow + 1);
        if (null != widestBorder && widestBorder.getWidth() >= width) {
            width = widestBorder.getWidth();
        }
        return width;
    }

    public float getMaxRightWidth() {
        float width = 0;
        Border widestBorder = getWidestVerticalBorder(verticalBorders.size() - 1);
        if (null != widestBorder && widestBorder.getWidth() >= width) {
            width = widestBorder.getWidth();
        }
        return width;
    }

    public float getMaxLeftWidth() {
        float width = 0;
        Border widestBorder = getWidestVerticalBorder(0);
        if (null != widestBorder && widestBorder.getWidth() >= width) {
            width = widestBorder.getWidth();
        }
        return width;
    }

    public Border getWidestVerticalBorder(int col) {
        return  TableBorderUtil.getWidestBorder(getVerticalBorder(col));
    }

    public Border getWidestVerticalBorder(int col, int start, int end) {
        return  TableBorderUtil.getWidestBorder(getVerticalBorder(col), start, end);
    }

    public Border getWidestHorizontalBorder(int row) {
        return  TableBorderUtil.getWidestBorder(getHorizontalBorder(row));
    }

    public Border getWidestHorizontalBorder(int row, int start, int end) {
        return  TableBorderUtil.getWidestBorder(getHorizontalBorder(row), start, end);
    }

    public List<Border> getFirstHorizontalBorder() {
        return getHorizontalBorder(startRow);
    }

    public List<Border> getLastHorizontalBorder() {
        return getHorizontalBorder(finishRow + 1);
    }

    public List<Border> getFirstVerticalBorder() {
        return getVerticalBorder(0);
    }

    public List<Border> getLastVerticalBorder() {
        return getVerticalBorder(verticalBorders.size() - 1);
    }

    public int getNumberOfColumns() {
        return numberOfColumns;
    }

    public int getStartRow() {
        return startRow;
    }

    public int getFinishRow() {
        return finishRow;
    }

    public Border[] getTableBoundingBorders() {
        return tableBoundingBorders;
    }

    public float[] getCellBorderIndents(int row, int col, int rowspan, int colspan) {
        float[] indents = new float[4];
        List<Border> borderList;
        Border border;
        // process top border
        borderList = getHorizontalBorder(startRow + row - rowspan + 1);
        for (int i = col; i < col + colspan; i++) {
            border = borderList.get(i);
            if (null != border && border.getWidth() > indents[0]) {
                indents[0] = border.getWidth();
            }
        }
        // process right border
        borderList = getVerticalBorder(col + colspan);
        for (int i = startRow + row - rowspan + 1; i < startRow + row + 1; i++) {
            border = borderList.get(i);
            if (null != border && border.getWidth() > indents[1]) {
                indents[1] = border.getWidth();
            }
        }
        // process bottom border
        borderList = getHorizontalBorder(startRow + row + 1);
        for (int i = col; i < col + colspan; i++) {
            border = borderList.get(i);
            if (null != border && border.getWidth() > indents[2]) {
                indents[2] = border.getWidth();
            }
        }
        // process left border
        borderList = getVerticalBorder(col);
        for (int i = startRow + row - rowspan + 1; i < startRow + row + 1; i++) {
            border = borderList.get(i);
            if (null != border && border.getWidth() > indents[3]) {
                indents[3] = border.getWidth();
            }
        }
        return indents;
    }
    // endregion
}
