package com.ftp.phatng.ftpphat.utils;

import com.ftp.phatng.ftpphat.services.ExcelSheetCallback;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.StylesTable;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;

public class ExcelReader {
//    private static final Logger LOGGER = LoggerFactory.getLogger(ExcelReader.class);

    private static final int READ_ALL = -1;

    private OPCPackage xlsxPackage;
    private XSSFSheetXMLHandler.SheetContentsHandler sheetContentsHandler;
    private ExcelSheetCallback sheetCallback;

    /**
     * Constructor: Microsoft Excel File (XSLX) Reader
     *
     * @param pkg                  a {@link OPCPackage} object - The package to process XLSX
     * @param sheetContentsHandler a {@link XSSFSheetXMLHandler.SheetContentsHandler} object - WorkSheet contents handler
     * @param sheetCallback        a {@link ExcelSheetCallback} object - WorkSheet callback for sheet
     *                             processing begin and end (can be null)
     */
    public ExcelReader(OPCPackage pkg, XSSFSheetXMLHandler.SheetContentsHandler sheetContentsHandler,
                       ExcelSheetCallback sheetCallback) {
        this.xlsxPackage = pkg;
        this.sheetContentsHandler = sheetContentsHandler;
        this.sheetCallback = sheetCallback;
    }

    /**
     * Processing all the WorkSheet from XLSX Workbook.
     *
     * <br>
     * <br>
     * <strong>Example 1:</strong><br>
     * <code>ExcelReader excelReader = new ExcelReader("src/main/resources/Sample-Person-Data.xlsx", workSheetHandler, sheetCallback);
     * <br>excelReader.process();</code> <br>
     * <br>
     * <strong>Example 2:</strong><br>
     * <code>ExcelReader excelReader = new ExcelReader(file, workSheetHandler, sheetCallback);
     * <br>excelReader.process();</code> <br>
     * <br>
     * <strong>Example 3:</strong><br>
     * <code>ExcelReader excelReader = new ExcelReader(pkg, workSheetHandler, sheetCallback);
     * <br>excelReader.process();</code>
     *
     * @throws Exception
     */
    public void process(Integer sheetNum) throws Exception {
        read(sheetNum);
    }

    private void read(int sheetNumber) throws RuntimeException {
        ReadOnlySharedStringsTable strings;
        try {
            strings = new ReadOnlySharedStringsTable(this.xlsxPackage);
            XSSFReader xssfReader = new XSSFReader(this.xlsxPackage);
            StylesTable styles = xssfReader.getStylesTable();
            XSSFReader.SheetIterator worksheets = (XSSFReader.SheetIterator) xssfReader.getSheetsData();

            for (int sheetIndex = 0; worksheets.hasNext(); sheetIndex++) {
                InputStream stream = worksheets.next();
                if (null != sheetCallback) {
                    this.sheetCallback.startSheet(sheetIndex, worksheets.getSheetName());
                }

                if ((READ_ALL == sheetNumber) || (sheetIndex == sheetNumber)) {
                    readSheet(styles, strings, stream);
                }
                IOUtils.closeQuietly(stream);

                if (null != sheetCallback) {
                    this.sheetCallback.endSheet();
                }
            }
        } catch (IOException | SAXException | OpenXML4JException | ParserConfigurationException e) {
//            LOGGER.error(e.getMessage(), e.getCause());
        }
    }

    /**
     * Parses the content of one sheet using the specified styles and shared-strings tables.
     *
     * @param styles             a {@link StylesTable} object
     * @param sharedStringsTable a {@link ReadOnlySharedStringsTable} object
     * @param sheetInputStream   a {@link InputStream} object
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    private void readSheet(StylesTable styles, ReadOnlySharedStringsTable sharedStringsTable,
                           InputStream sheetInputStream) throws IOException, ParserConfigurationException, SAXException {

        SAXParserFactory saxFactory = SAXParserFactory.newInstance();
        XMLReader sheetParser = saxFactory.newSAXParser().getXMLReader();

        ContentHandler handler =
                new XSSFSheetXMLHandler(styles, sharedStringsTable, sheetContentsHandler, false);

        sheetParser.setContentHandler(handler);
        sheetParser.parse(new InputSource(sheetInputStream));
    }
}