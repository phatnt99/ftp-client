package com.ftp.phatng.ftpphat.services;

public interface ExcelSheetCallback {
    /**
     * Callback for Worksheet start
     *
     * @param sheetNum
     * @param sheetName
     */
    void startSheet(int sheetNum, String sheetName);

    /**
     * Callback for Worksheet end
     */
    void endSheet();
}
