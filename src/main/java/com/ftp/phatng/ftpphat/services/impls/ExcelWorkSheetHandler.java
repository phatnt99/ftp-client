package com.ftp.phatng.ftpphat.services.impls;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ExcelWorkSheetHandler implements XSSFSheetXMLHandler.SheetContentsHandler {

    private int skipRows = 0;
    private int firstRowIndex = 0;
    private int currentRow = 0;
    private List<Map<String, String>> valueList;
    private Map<String, String> objCurrentRow = null;

    /**
     * Constructor
     *
     * <br>
     * <br>
     * <strong>For Example:</strong> Reading rows (zero based) starting from Row 11<br>
     * <code>ExcelWorkSheetHandler&lt;PersonVO> workSheetHandler = new ExcelWorkSheetHandler&lt;PersonVO>(PersonVO.class, cellMapping, 10);</code>
     *
     * @param firstRowIndex a <code>int</code> object - Number of row to start. default is 0
     */
    public ExcelWorkSheetHandler(int firstRowIndex) {
        this.valueList = new ArrayList<>();
        this.firstRowIndex = firstRowIndex;
    }

    /**
     * @return
     */
    public List<Map<String, String>> getValueList() {
        return valueList;
    }

    /**
     * @see org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler.SheetContentsHandler#startRow(int)
     */
    @Override
    public void startRow(int rowNum) {
        this.currentRow = rowNum;

        if (rowNum >= firstRowIndex && rowNum != skipRows) {
            objCurrentRow = new LinkedHashMap<>();
        }
    }

    @Override
    public void endRow() {
        if (currentRow >= firstRowIndex && currentRow != skipRows) {
            if (null != objCurrentRow && isObjectHasValue(objCurrentRow)) {
                // Current row data is populated in the object, so add it to
                // list
                objCurrentRow.put("RowIndex", String.valueOf(this.currentRow + 1));
                this.valueList.add(objCurrentRow);
            }

            // Row object is added, so reset it to null
            objCurrentRow = null;
        }
    }

    @Override
    public void cell(String cellReference, String formattedValue) {
        if (currentRow >= firstRowIndex && currentRow != skipRows) {
            if (StringUtils.isBlank(formattedValue)) {
                return;
            }

            String columnId = getCellReference(cellReference);
            objCurrentRow.put(columnId, formattedValue.trim());
        }
    }

    /**
     * Currently not considered for implementation
     *
     * @see org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler.SheetContentsHandler#headerFooter(java.lang.String,
     * boolean, java.lang.String)
     */
    @Override
    public void headerFooter(String text, boolean isHeader, String tagName) {
        // currently not consider for implementation
    }

    private String getCellReference(String cellReference) {
        if (StringUtils.isBlank(cellReference)) {
            return "";
        }

        return cellReference.split("[0-9]*$")[0];
    }

    /**
     * To check generic object of T has a minimum one value assigned or not
     */
    private boolean isObjectHasValue(Map<String, String> targetObj) {
        return targetObj.entrySet().stream().anyMatch(e -> StringUtils.isNotBlank(e.getValue()));
    }


}
