/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.repo.migration;


import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.util.Pair;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;

import eu.cec.digit.circabc.service.migration.CategoryStat;
import eu.cec.digit.circabc.service.migration.InterestGroupStat;
import eu.cec.digit.circabc.service.migration.RootStat;

/**
 * Write statistics in an excell file format
 *
 * @author Yanick Pignot
 */
public class WorkbookStatisticsWriter extends StatisticWriterBase
{
    private static final int LAST_EXCELL_COLUMN_INDEX = 254;
	private static final String FORMULA_SUM = "SUM";
	private static final String FORMULA_MAX = "MAX";
	private static final String FORMULA_MIN = "MIN";
	private static final String FORMULA_AVERAGE = "AVERAGE";
	private static final String FORMULA_PURCENT = "%";
	private static final String FORMULA_DIV = "/";
	private static final String FORMULA_TRUE = "TRUE";
	private static final String FORMULA_COUNTIF = "COUNTIF";
	private static final Map<StyleConfig, HSSFCellStyle> STYLES = new HashMap<StyleConfig, HSSFCellStyle>();
    private static final Map<Short, HSSFFont> FONTS = new HashMap<Short, HSSFFont>();

    /* (non-Javadoc)
	 * @see eu.cec.digit.circabc.repo.migration.StatisticsWriter#write(eu.cec.digit.circabc.service.migration.RootStat, java.io.OutputStream)
	 */
    public synchronized void write(final RootStat rootStat, final OutputStream ... outputStreams) throws IOException
    {
    	try
    	{
	    	final HSSFWorkbook wb = new HSSFWorkbook();

	        // get all category headers and their categories
	        final Map<String, List<CategoryStat>> catByHeaders = new HashMap<String, List<CategoryStat>>();
	        for(final CategoryStat stat:  rootStat.getCategoryStats())
	        {
	        	final String categoryHeader = stat.getCategoryHeader();
				if(catByHeaders.containsKey(categoryHeader) == false)
	        	{
	        		catByHeaders.put(categoryHeader, new ArrayList<CategoryStat>());
	        	}

	        	catByHeaders.get(categoryHeader).add(stat);
	        }

	        // sort the headers
	        final List<String> sortedHeader = new ArrayList<String>(catByHeaders.size());
	        sortedHeader.addAll(catByHeaders.keySet());
	        Collections.sort(sortedHeader);

	        // fill the root stat sheet
	        final HSSFSheet sheetRoot = fillRootSheet(rootStat, wb);

	        // fill the categories sheet
	        final HSSFSheet sheetCat = fillCategorySheets(wb, catByHeaders, sortedHeader);

	        // fill the interest groups sheet
	        final HSSFSheet sheetIg = fillIgSheet(wb, catByHeaders, sortedHeader);


	        addFormula(wb, sheetRoot, sheetCat, sheetIg);

	        wb.write(outputStreams[0]);
    	}
    	finally
    	{
    		// clear cache
    		STYLES.clear();
    		FONTS.clear();
    	}

    }

	private HSSFSheet fillCategorySheets(final HSSFWorkbook wb, final Map<String, List<CategoryStat>> catByHeaders, final List<String> sortedHeader)
	{
		final HSSFSheet sheetCat = wb.createSheet("Categories");
        final List<Method> catGetters = getGetters(CategoryStat.class);

		int rowIndex = -1;
		int colIndex = -1;

		final HSSFRow catTitleRow = sheetCat.createRow(++rowIndex);
        createCell(wb, sheetCat, catTitleRow, (short) ++colIndex, "Header", false, true, HSSFColor.YELLOW.index);
        createCell(wb, sheetCat, catTitleRow, (short) ++colIndex, "Category", false, true, HSSFColor.YELLOW.index);

        for(final String header: sortedHeader)
        {
        	final List<CategoryStat> categoryStats = catByHeaders.get(header);
        	Collections.sort(categoryStats, categoryComparator);

        	final int startHeaderRow = rowIndex + 1;

        	for(final CategoryStat stat: categoryStats)
            {
        		colIndex = -1;

        		final HSSFRow row = sheetCat.createRow(++rowIndex);
            	createCell(wb, sheetCat, row, (short) ++colIndex, header, false, true, HSSFColor.GREY_40_PERCENT.index);
            	createCell(wb, sheetCat, row, (short) ++colIndex, stat.getCategoryName(), false, false, HSSFColor.GREY_25_PERCENT.index);

            	for(final Method method: catGetters)
                {
            		// insert col title
            		createCell(wb, sheetCat, catTitleRow, (short) ++colIndex, getMethodTitle(method.getName()), false, true, HSSFColor.YELLOW.index);
            		createCell(wb, sheetCat, row, (short) colIndex, safeInvoke(method, stat), false, false);
                }
            }

        	// if > no row is written
        	if(startHeaderRow <= rowIndex)
        	{
        		//sheetCat.addMergedRegion(new Region(startHeaderRow, (short) 0, rowIndex, (short) 0));
        		sheetCat.addMergedRegion(new CellRangeAddress(startHeaderRow, 0, rowIndex, 0));
        	}
        }

        return sheetCat;
	}


	private HSSFSheet fillIgSheet(final HSSFWorkbook wb, final Map<String, List<CategoryStat>> catByHeaders, final List<String> sortedHeader)
	{
		final HSSFSheet sheetIg = wb.createSheet("Interest Groups");
	    final List<Method> igGetters = getGetters(InterestGroupStat.class);

	    int rowIndex = -1;
		int colIndex = -1;

		final HSSFRow igTitleRow = sheetIg.createRow(++rowIndex);
        createCell(wb, sheetIg, igTitleRow, (short) ++colIndex, "Header", false, true, HSSFColor.YELLOW.index);
        createCell(wb, sheetIg, igTitleRow, (short) ++colIndex, "Category", false, true, HSSFColor.YELLOW.index);
        createCell(wb, sheetIg, igTitleRow, (short) ++colIndex, "InterestGroup", false, true, HSSFColor.YELLOW.index);

        for(final String header: sortedHeader)
        {
        	final List<CategoryStat> categoryStats = catByHeaders.get(header);
        	Collections.sort(categoryStats, categoryComparator);

        	final int startHeaderRow = rowIndex + 1;

        	for(final CategoryStat catStat: categoryStats)
            {
        		final List<InterestGroupStat> igStats = catStat.getInterestGroupStats();
            	Collections.sort(igStats, igComparator);

            	final int startCategoryRow = rowIndex + 1;

            	for(final InterestGroupStat igStat: igStats)
            	{
            		colIndex = -1;

            		final HSSFRow row = sheetIg.createRow(++rowIndex);
                	createCell(wb, sheetIg, row, (short) ++colIndex, header, false, true, HSSFColor.GREY_50_PERCENT.index);
                	createCell(wb, sheetIg, row, (short) ++colIndex, catStat.getCategoryName(), false, true, HSSFColor.GREY_40_PERCENT.index);
                	createCell(wb, sheetIg, row, (short) ++colIndex, igStat.getIgName(), false, true, HSSFColor.GREY_25_PERCENT.index);

                	for(final Method method: igGetters)
                    {
                		// insert col title
                		createCell(wb, sheetIg, igTitleRow, (short) ++colIndex, getMethodTitle(method.getName()), false, true, HSSFColor.YELLOW.index);
                		// inser col value
        				createCell(wb, sheetIg, row, (short) colIndex, safeInvoke(method, igStat), false, false);
                    }
            	}
            	// if > no row is written
            	if(startCategoryRow <= rowIndex)
            	{
            		//sheetIg.addMergedRegion(new Region(startCategoryRow, (short) 1, rowIndex, (short) 1));
            		sheetIg.addMergedRegion(new CellRangeAddress(startCategoryRow, 1, rowIndex, 1));
            	}
            }
        	// if > no row is written
        	if(startHeaderRow <= rowIndex)
        	{
        		//sheetIg.addMergedRegion(new Region(startHeaderRow, (short) 0, rowIndex, (short) 0));
        		sheetIg.addMergedRegion(new CellRangeAddress(startHeaderRow, 0, rowIndex, 0));
        	}
        }

        return sheetIg;
	}
	private HSSFSheet fillRootSheet(final RootStat rootStat, final HSSFWorkbook wb)
	{
		int rowIndex = -1;

        final HSSFSheet sheetRoot = wb.createSheet("Root");
        final List<Method> rootGetters = getGetters(RootStat.class);

        for(final Method method: rootGetters)
        {
            final HSSFRow row = sheetRoot.createRow(++rowIndex);
            createCell(wb, sheetRoot, row, (short) 0, getMethodTitle(method.getName()), false, true, HSSFColor.YELLOW.index);
            createCell(wb, sheetRoot, row, (short) 1, safeInvoke(method, rootStat), false, false);			
        }

        return sheetRoot;
	}

    private void createCell(final HSSFWorkbook wb, final HSSFSheet sheet, final HSSFRow row, final short cellIndex, final Object cellValue, final boolean formula, final boolean isTitle)
    {
    	createCell(wb, sheet, row, cellIndex, cellValue, formula, isTitle, -1);
    }

    private void createCell(final HSSFWorkbook wb, final HSSFSheet sheet, final HSSFRow row, final int cellIndex, final Object cellValue, final boolean formula, final boolean isTitle, final int color)
    {
        if(cellValue == null)
        {
        	createCell(wb, sheet, row, cellIndex, "", formula, isTitle, color);
        }
        else
        {
        	final HSSFCell cell = row.createCell(cellIndex);
        	final StyleConfig cellStyle = new StyleConfig();
            cellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);

            sheet.autoSizeColumn(cellIndex);

            if(formula)
            {
            	cellStyle.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
            	cell.setCellFormula((String) cellValue);
            }
            else if(cellValue instanceof Integer
                    || cellValue instanceof Long
                    || cellValue instanceof Float)
            {
            	cellStyle.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
            	cell.setCellValue(Double.parseDouble(cellValue.toString()));
            }
            else if(cellValue instanceof Double)
            {
            	cellStyle.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
            	cell.setCellValue((Double)cellValue);
            }
            else if(cellValue instanceof Date)
            {
            	cellStyle.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
            	cell.setCellValue((Date) cellValue);
            }
            else if(cellValue instanceof Boolean)
            {
            	cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER_SELECTION);
            	cell.setCellValue((Boolean) cellValue);
            }
            else
            {
            	cellStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);
                cell.setCellValue(new HSSFRichTextString(cellValue.toString()));
            }

            cellStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
            cellStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
            cellStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
            cellStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);

            if(isTitle)
            {
            	cellStyle.setFillBoldweight(HSSFFont.BOLDWEIGHT_BOLD);

            }

            if(color > -1)
            {
            	cellStyle.setFillForegroundColor((short) color);
            	cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
            }

            cell.setCellStyle(getStyleFromConfig(wb, cellStyle));
        }
    }

    private HSSFCellStyle getStyleFromConfig(final HSSFWorkbook wb, final StyleConfig cellStyle)
    {
    	if(STYLES.containsKey(cellStyle) == false)
    	{
    		final HSSFCellStyle style = wb.createCellStyle();

    		style.setAlignment(cellStyle.getAlignment());
    		style.setVerticalAlignment(cellStyle.getVerticalAlignment());

    		style.setBorderBottom(cellStyle.getBorderBottom());
    		style.setBorderLeft(cellStyle.getBorderLeft());
    		style.setBorderRight(cellStyle.getBorderRight());
    		style.setBorderTop(cellStyle.getBorderTop());

    		if(cellStyle.getFillForegroundColor() != 0)
            {
    			style.setFillForegroundColor((short) cellStyle.getFillForegroundColor());
            }

    		if(cellStyle.getFillPattern() != 0)
            {
    			style.setFillPattern(cellStyle.getFillPattern());
            }

    		final short fillBoldweight = cellStyle.getFillBoldweight();
			if(fillBoldweight != 0)
            {
    			if(FONTS.containsKey(fillBoldweight) == false)
    			{
    				final HSSFFont font = wb.createFont();
                	font.setBoldweight(fillBoldweight);
                	FONTS.put(fillBoldweight, font);
    			}
    			style.setFont(FONTS.get(fillBoldweight));
            }

    		STYLES.put(cellStyle, style);
    	}

    	return STYLES.get(cellStyle);
    }

	private void addFormula(final HSSFWorkbook wb, final HSSFSheet sheetRoot, final HSSFSheet sheetCat, final HSSFSheet sheetIg)
	{
    	if(sheetIg.getRow(1) == null)
    	{
    		//no interest group
    		return;
    	}

		final String igSheetName = wb.getSheetName(wb.getSheetIndex(sheetIg));

		int firstFreeRowIndex = getFirstFreeRowIndex(sheetCat);
		final Map<String, Pair<Integer, Integer>> categoryRows = new HashMap<String, Pair<Integer,Integer>>(firstFreeRowIndex);

		final Iterator<Row> igRows = sheetIg.rowIterator();
		final HSSFRow igFirstRow = (HSSFRow)igRows.next();
		HSSFRow igSecondRow = null; // keep the second row to check the content type
		while(igRows.hasNext())
		{
			final HSSFRow row = (HSSFRow)igRows.next();
			if(igSecondRow == null)
			{
				igSecondRow = row;
			}

			final String catName = row.getCell(1).getRichStringCellValue().getString(); // get the name of the row category
			final int currentRowIdx = row.getRowNum();

			Pair<Integer, Integer> rowIndexes =  categoryRows.get(catName);

			if(rowIndexes == null)
			{
				rowIndexes = new Pair<Integer, Integer>(currentRowIdx, currentRowIdx);
			}

			if(currentRowIdx < rowIndexes.getFirst())
			{
				rowIndexes.setFirst(currentRowIdx);
			}
			if(currentRowIdx > rowIndexes.getSecond())
			{
				rowIndexes.setSecond(currentRowIdx);
			}

			categoryRows.put(catName, rowIndexes);
		}

		// the range of all defined interest groups
		final Pair<Integer, Integer> allIgsRows = new Pair<Integer, Integer>(1, sheetIg.getLastRowNum());
		// counter for row iteration
		int rootRow = getFirstFreeRowIndex(sheetRoot);

		createFormulaTitle(wb, sheetRoot, ++rootRow, "All Interst Groups Statistics", (short) 0, HSSFColor.GREY_40_PERCENT.index);
		iterateIgValuesFormula(wb, sheetRoot, igSheetName, igFirstRow, igSecondRow, (short) 0, rootRow, allIgsRows);

		short catColumn = -2;

		final List<String> categories = new ArrayList<String>(categoryRows.size());
		categories.addAll(categoryRows.keySet());
		Collections.sort(categories);

		for(final String catName: categories)
		{
			if(catColumn > LAST_EXCELL_COLUMN_INDEX)
			{
				catColumn = -2;
				firstFreeRowIndex = getFirstFreeRowIndex(sheetCat);
			}

			int catRow = firstFreeRowIndex;
			catColumn += 2;

			final Pair<Integer, Integer> catRowRange = categoryRows.get(catName);

			createFormulaTitle(wb, sheetCat, ++catRow, catName + " Statistics", catColumn, HSSFColor.GREY_40_PERCENT.index);
			iterateIgValuesFormula(wb, sheetCat, igSheetName, igFirstRow, igSecondRow, catColumn, catRow, catRowRange);

		}
	}

	private void iterateIgValuesFormula(final HSSFWorkbook wb, final HSSFSheet sheet, final String igSheetName, final HSSFRow igFirstRow, HSSFRow igSecondRow, short colmunIdx, int rowIdx, final Pair<Integer, Integer> rowRange)
	{
		final Iterator<Cell> igCells = igFirstRow.cellIterator();
		while(igCells.hasNext())
		{
			// get the name and the type of the current ig row
			final HSSFCell cell = (HSSFCell)igCells.next();
			final int igCellNum = cell.getColumnIndex();
			final String name = igFirstRow.getCell(igCellNum).getRichStringCellValue().getString();
			final int type = igSecondRow.getCell(igCellNum).getCellType();

			if(HSSFCell.CELL_TYPE_NUMERIC == type)
			{
				// create titles of the current row
				createFormulaTitle(wb, sheet, ++rowIdx, name.toUpperCase(), (short) colmunIdx, HSSFColor.GREY_25_PERCENT.index);

				// in sheet: create SUM of all cells from the IG sheet in the range rows
				createFormulaRow(wb, sheet, ++rowIdx, igCellNum, rowRange, igSheetName, name, FORMULA_SUM, colmunIdx);
				// in sheet: create MAX of all cells from the IG sheet in the range rows
				createFormulaRow(wb, sheet, ++rowIdx, igCellNum, rowRange, igSheetName, name, FORMULA_MAX, colmunIdx);
				// in sheet: create MIN of all cells from the IG sheet in the range rows
				createFormulaRow(wb, sheet, ++rowIdx, igCellNum, rowRange, igSheetName, name, FORMULA_MIN, colmunIdx);
				// in sheet: create AVERAGE of all cells from the IG sheet in the range rows
				createFormulaRow(wb, sheet, ++rowIdx, igCellNum, rowRange, igSheetName, name, FORMULA_AVERAGE, colmunIdx);

			}
			else if(HSSFCell.CELL_TYPE_BOOLEAN == type)
			{
				// create titles of the current row
				createFormulaTitle(wb, sheet, ++rowIdx, name.toUpperCase(), (short) colmunIdx, HSSFColor.GREY_25_PERCENT.index);
				// in sheet: create % of all cells as TRUE from the IG sheet in the range rows
				createPCBooleanFormulaRow(wb, sheet, ++rowIdx, igCellNum, rowRange, igSheetName, name, colmunIdx);
			}
		}
	}

    private void createFormulaTitle(final HSSFWorkbook wb, final HSSFSheet sheet, int rowIndex, final String title, final short fromCol, final short color)
	{
		final HSSFRow rootNewRow = sheet.createRow(rowIndex);
		createCell(wb, sheet, rootNewRow, fromCol, title, false, true, color);
		createCell(wb, sheet, rootNewRow, (short) (fromCol + 1), "", false, true);

		//sheet.addMergedRegion(new Region(rowIndex, fromCol, rowIndex, (short) (fromCol + 1)));
		sheet.addMergedRegion(new CellRangeAddress(rowIndex, fromCol, rowIndex, (short) (fromCol + 1)));
	}

    private void createFormulaRow(final HSSFWorkbook wb, final HSSFSheet sheet, int rowIndex, final int cellIndex, final Pair<Integer, Integer> formulaRowRange, final String igSheetName, final String igRowName, final String formulaType, final short firstCol)
	{
		final HSSFRow newRow = sheet.createRow(rowIndex);
		createCell(wb, sheet, newRow, firstCol, formulaType + " " + igRowName, false, true, HSSFColor.YELLOW.index);
		createCell(wb, sheet, newRow, (short) (firstCol + 1),
				toFormulaRange(igSheetName, formulaRowRange, cellIndex, formulaType), true, false);
	}

	private void createPCBooleanFormulaRow(final HSSFWorkbook wb, final HSSFSheet sheet, int rowIndex, final int cellIndex, final Pair<Integer, Integer> formulaRowRange, final String igSheetName, final String igRowName, final short firstCol)
	{
		final HSSFRow newRow = sheet.createRow(rowIndex);
		createCell(wb, sheet, newRow, firstCol, FORMULA_PURCENT + FORMULA_TRUE + " " + igRowName, false, true, HSSFColor.YELLOW.index);
		createCell(wb, sheet, newRow, (short) (firstCol + 1),
				"(" + toFormulaRange(igSheetName, formulaRowRange, cellIndex, FORMULA_COUNTIF, FORMULA_TRUE) + FORMULA_DIV + (formulaRowRange.getSecond() - formulaRowRange.getFirst() + 1) + ") * 100" , true, false);
	}

    private String toFormulaRange(final String sheetName, final Pair<Integer, Integer> rowRange, final int column, final String formulaOp, final String ... otherArgs)
    {
    	if(rowRange == null)
    	{
    		return "0";
    	}
    	else
    	{
    		final StringBuffer buff = new StringBuffer(formulaOp);

        	// example: SUM('My Sheet'!D36:D113) or COUNTIF('My Sheet'!AO4:AO25, TRUE)
        	final String columnStr = computeColumnFormulRef(column);

        	buff
        		.append("('")
        		.append(sheetName)
        		.append("'!")
        		.append(columnStr)
        		.append(rowRange.getFirst() + 1)
        		.append(':')
        		.append(columnStr)
        		.append(rowRange.getSecond() + 1);

        	for(String arg: otherArgs)
        	{
        		buff
        			.append(", ")
        			.append(arg);
        	}

        	buff.append(')');


        	return buff.toString();
    	}

    }

    private int getFirstFreeRowIndex(final HSSFSheet sheet)
    {
    	return sheet.getLastRowNum() + 1 ;
    }

    private String computeColumnFormulRef(final int colIndex)
	{
		if(colIndex < 26)
		{
			return String.valueOf((char) ('A' + colIndex));
		}
		else
		{
			return String.valueOf(new char[]{(char) ('A' + (colIndex / 26) -1 ), (char) ('A' + colIndex % 26)});
		}
	}



    /**
     * Wrapper that hold the state of a sytle for caching purposes and to reduce the number of celle formating
     *
     * @author Yanick Pignot
     */
    static class StyleConfig
    {
    	private short alignment = 0;
    	private short verticalAlignment = 0;

    	private short borderBottom = 0;
    	private short borderLeft = 0;
    	private short borderRight = 0;
    	private short borderTop = 0;

    	private short fillForegroundColor = 0;
    	private short fillPattern = 0;
    	private short fillBoldweight = 0;

		/**
		 * @return the alignment
		 */
		private final short getAlignment()
		{
			return alignment;
		}

		/**
		 * @param alignment the alignment to set
		 */
		private final void setAlignment(short alignment)
		{
			this.alignment = alignment;
		}

		/**
		 * @return the borderBottom
		 */
		private final short getBorderBottom()
		{
			return borderBottom;
		}

		/**
		 * @param borderBottom the borderBottom to set
		 */
		private final void setBorderBottom(short borderBottom)
		{
			this.borderBottom = borderBottom;
		}

		/**
		 * @return the borderLeft
		 */
		private final short getBorderLeft()
		{
			return borderLeft;
		}

		/**
		 * @param borderLeft the borderLeft to set
		 */
		private final void setBorderLeft(short borderLeft)
		{
			this.borderLeft = borderLeft;
		}

		/**
		 * @return the borderRight
		 */
		private final short getBorderRight()
		{
			return borderRight;
		}

		/**
		 * @param borderRight the borderRight to set
		 */
		private final void setBorderRight(short borderRight)
		{
			this.borderRight = borderRight;
		}

		/**
		 * @return the borderTop
		 */
		private final short getBorderTop()
		{
			return borderTop;
		}

		/**
		 * @param borderTop the borderTop to set
		 */
		private final void setBorderTop(short borderTop)
		{
			this.borderTop = borderTop;
		}

		/**
		 * @return the fillBoldweight
		 */
		private final short getFillBoldweight()
		{
			return fillBoldweight;
		}

		/**
		 * @param fillBoldweight the fillBoldweight to set
		 */
		private final void setFillBoldweight(short fillBoldweight)
		{
			this.fillBoldweight = fillBoldweight;
		}

		/**
		 * @return the fillForegroundColor
		 */
		private final short getFillForegroundColor()
		{
			return fillForegroundColor;
		}

		/**
		 * @param fillForegroundColor the fillForegroundColor to set
		 */
		private final void setFillForegroundColor(short fillForegroundColor)
		{
			this.fillForegroundColor = fillForegroundColor;
		}

		/**
		 * @return the fillPattern
		 */
		private final short getFillPattern()
		{
			return fillPattern;
		}

		/**
		 * @param fillPattern the fillPattern to set
		 */
		private final void setFillPattern(short fillPattern)
		{
			this.fillPattern = fillPattern;
		}

		/**
		 * @return the verticalAlignment
		 */
		private final short getVerticalAlignment()
		{
			return verticalAlignment;
		}

		/**
		 * @param verticalAlignment the verticalAlignment to set
		 */
		private final void setVerticalAlignment(short verticalAlignment)
		{
			this.verticalAlignment = verticalAlignment;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode()
		{
			final int PRIME = 31;
			int result = 1;
			result = PRIME * result + alignment;
			result = PRIME * result + borderBottom;
			result = PRIME * result + borderLeft;
			result = PRIME * result + borderRight;
			result = PRIME * result + borderTop;
			result = PRIME * result + fillBoldweight;
			result = PRIME * result + fillForegroundColor;
			result = PRIME * result + fillPattern;
			result = PRIME * result + verticalAlignment;
			return result;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final StyleConfig other = (StyleConfig) obj;
			if (alignment != other.alignment)
				return false;
			if (borderBottom != other.borderBottom)
				return false;
			if (borderLeft != other.borderLeft)
				return false;
			if (borderRight != other.borderRight)
				return false;
			if (borderTop != other.borderTop)
				return false;
			if (fillBoldweight != other.fillBoldweight)
				return false;
			if (fillForegroundColor != other.fillForegroundColor)
				return false;
			if (fillPattern != other.fillPattern)
				return false;
			if (verticalAlignment != other.verticalAlignment)
				return false;
			return true;
		}

    }
}
