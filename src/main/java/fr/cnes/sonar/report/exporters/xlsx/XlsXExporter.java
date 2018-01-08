package fr.cnes.sonar.report.exporters.xlsx;

import fr.cnes.sonar.report.exceptions.BadExportationDataTypeException;
import fr.cnes.sonar.report.exceptions.UnknownParameterException;
import fr.cnes.sonar.report.exporters.IExporter;
import fr.cnes.sonar.report.input.Params;
import fr.cnes.sonar.report.model.Report;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;

/**
 * Exports the report in .docx format
 * @author lequal
 */
public class XlsXExporter implements IExporter {

    /**
     * Name of the tab containing formatted issues
     */
    private static final String ISSUES_SHEET_NAME = "Issues";
    /**
     * Name of the tab containing unconfirmed issues
     */
    private static final String UNCONFIRMED_SHEET_NAME = "Unconfirmed";
    /**
     *  Name of the tab containing all detailed issues
     */
    private static final String ALL_DETAILS_SHEET_NAME = "All";
    /**
     *  Name of the property for the path of the issues template
     */
    private static final String ISSUES_TEMPLATE = "issues.template";
    /**
     *  Name of the property for the path of the report output folder
     */
    private static final String REPORT_PATH = "report.path";
    /**
     * Name for the table containing selected resources
     */
    private static final String SELECTED_TABLE_NAME = "selected";
    /**
     * Name for the table containing unconfirmed resources
     */
    private static final String UNCONFIRMED_TABLE_NAME = "unconfirmed";
    /**
     * Name for the table containing all raw resources
     */
    private static final String ALL_TABLE_NAME = "all";

    /**
     * Overridden export for XlsX
     * @param data Data to export as Report
     * @param params Program's parameters
     * @param path Path where to export the file
     * @param filename Name of the file to export
     * @throws BadExportationDataTypeException ...
     * @throws UnknownParameterException report.path is not set
     * @throws IOException when reading a file
     */
    @Override
    public void export(Object data, Params params, String path, String filename)
            throws BadExportationDataTypeException, UnknownParameterException, IOException {
        // check resources type
        if(!(data instanceof Report)) {
            throw new BadExportationDataTypeException();
        }
        // resources casting
        final Report report = (Report) data;

        // set output filename
        final String outputFilePath = params.get(REPORT_PATH)+"/"+filename;

        // open excel file from the path given in the parameters
        final File file = new File(params.get(ISSUES_TEMPLATE));

        // open the template
        try(
                FileInputStream excelFile = new FileInputStream(file);
                Workbook workbook = new XSSFWorkbook(excelFile);
                FileOutputStream fileOut = new FileOutputStream(outputFilePath)) {

            // retrieve the sheet aiming to contain selected resources
            final XSSFSheet selectedSheet = (XSSFSheet) workbook.getSheet(ISSUES_SHEET_NAME);

            // retrieve the sheet aiming to contain selected resources
            final XSSFSheet unconfirmedSheet = (XSSFSheet) workbook.getSheet(UNCONFIRMED_SHEET_NAME);

            // retrieve the sheet aiming to contain selected resources
            final XSSFSheet allDataSheet = (XSSFSheet) workbook.getSheet(ALL_DETAILS_SHEET_NAME);

            // write selected resources in the file
            XlsXTools.addSelectedData(report.getIssues(), selectedSheet, SELECTED_TABLE_NAME);

            // write selected resources in the file
            XlsXTools.addSelectedData(report.getUnconfirmed(), unconfirmedSheet, UNCONFIRMED_TABLE_NAME);

            // write all raw resources in the third sheet
            XlsXTools.addListOfMap(allDataSheet, report.getRawIssues(), ALL_TABLE_NAME);

            // write output as file
            workbook.write(fileOut);
        }
    }

}
