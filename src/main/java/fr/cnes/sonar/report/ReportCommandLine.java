/*
 * This file is part of cnesreport.
 *
 * cnesreport is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * cnesreport is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with cnesreport.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.cnes.sonar.report;

import fr.cnes.sonar.report.exceptions.*;
import fr.cnes.sonar.report.exporters.IExporter;
import fr.cnes.sonar.report.exporters.JsonExporter;
import fr.cnes.sonar.report.exporters.XmlExporter;
import fr.cnes.sonar.report.exporters.docx.DocXExporter;
import fr.cnes.sonar.report.exporters.xlsx.XlsXExporter;
import fr.cnes.sonar.report.factory.ReportFactory;
import fr.cnes.sonar.report.utils.Params;
import fr.cnes.sonar.report.utils.ParamsFactory;
import fr.cnes.sonar.report.utils.StringManager;
import fr.cnes.sonar.report.model.ProfileMetaData;
import fr.cnes.sonar.report.model.QualityProfile;
import fr.cnes.sonar.report.model.Report;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.xmlbeans.XmlException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Main entry point
 * @author lequal
 */
public final class ReportCommandLine {

    // Static initialization to set logging configuration.
    static {
        // Configure logging system
        try (InputStream fis = ReportCommandLine.class.getResourceAsStream("/logging.properties")) {
            LogManager.getLogManager().readConfiguration(fis);
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }

        // Configure temp files: creation of folder ~/.cnesreport/log to contain log files
        (new File(org.apache.commons.io.FileUtils.getUserDirectory().getPath().concat("/.cnesreport/log"))).mkdirs();
    }

	 /**
     * Property for the word report filename
     */
    public static final String REPORT_FILENAME = "REPORT_FILENAME";
    /**
     * Property for the excel report filename
     */
    public static final String ISSUES_FILENAME = "ISSUES_FILENAME";
    /**
     * Pattern for the name of the directory containing configuration files
     */
    public static final String CONF_FOLDER_PATTERN = "%s/conf";
    /**
     * Error message returned when the program cannot create a folder because it already exists
     */
    public static final String CNES_MKDIR_ERROR =
            "Impossible to create the following directory: %s";
    /**
     * Logger of this class
     */
    private static final Logger LOGGER = Logger.getLogger(ReportCommandLine.class.getName());
    /**
     * Placeholder for the date of reporting
     */
    private static final String DATE = "DATE";
    /**
     * Placeholder for the name of the project
     */
    private static final String NAME = "NAME";
    /**
     * Pattern to format the date
     */
    private static final String DATE_PATTERN = "yyyy-MM-dd";
    /**
     * Name of the property to find the base of report location
     */
    public static final String REPORT_PATH = "report.path";
    /**
     * Help message to display when a user misused this program
     */
    private static final String HELP_MESSAGE = "Welcome to Sonar CNES Report.\n" +
            "Here are the list of parameters you can use:\n" +
            "  > --sonar.url [mandatory]\n" +
            "  > --sonar.token\n" +
            "  > --sonar.project.id [mandatory]\n" +
            "  > --report.author\n" +
            "  > --report.date\n" +
            "  > --report.path\n" +
            "  > --report.conf [yes|no]\n" +
            "  > --report.locale [fr_FR|en_US]\n" +
            "  > --report.template\n" +
            "  > --issues.template\n" +
            "\nExample :\n" +
            "java -jar sonar-report-cnes.jar --sonar.url http://sonarqube:9000" +
            " --sonar.project.id cat";

    /**
     * Private constructor to not be able to instantiate it.
     */
    private ReportCommandLine(){}

    /**
     * Main method
     * See HELP_MESSAGE for more information about using this program
     * Entry point of the program
     * @param args arguments that will be preprocessed
     */
    public static void main(String[] args)  {
        // main catches all exceptions
        try {
            // preparing args
            final Params params = new ParamsFactory().create(args);

            // extract params
            final String url = params.get("sonar.url");
            final String token = params.get("sonar.token");
            final String project = params.get("sonar.project.id");
            final String author = params.get("report.author");
            final String date = params.get("report.date");
            final String reportPath = params.get(REPORT_PATH);
            final String reportTemplate = params.get(StringManager.REPORT_TEMPLATE);
            final String issuesTemplate = params.get(StringManager.ISSUES_TEMPLATE);

            // generate report
            report(url, token, project, author, date, reportPath, reportTemplate, issuesTemplate);

        } catch (BadExportationDataTypeException | MalformedParameterException |
                BadSonarQubeRequestException | IOException | UnknownParameterException |
                MissingParameterException | UnknownQualityGateException | OpenXML4JException |
                XmlException e) {
            // it logs all the stack trace
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            // prints the help
            LOGGER.log(Level.INFO, HELP_MESSAGE);
        }
    }

    public static void report(final String url, final String token, final String project,
                              final String author, final String date, final String reportPath,
                              final String reportTemplate, final String issuesTemplate)
            throws IOException, BadSonarQubeRequestException, UnknownQualityGateException,
            XmlException, BadExportationDataTypeException, OpenXML4JException {
        // Files exporters : export the resources in the correct file type
        final DocXExporter docXExporter = new DocXExporter();
        final XmlExporter profileExporter = new XmlExporter();
        final JsonExporter gateExporter = new JsonExporter();
        final XlsXExporter issuesExporter = new XlsXExporter();

        // full path to the configuration folder
        final String confDirectory = String.format(CONF_FOLDER_PATTERN, reportPath);

        // create the configuration folder
        final File configFolder = new File(confDirectory);
        final boolean success = configFolder.mkdirs();
        if (!success && !configFolder.exists()) {
            // Directory creation failed
            LOGGER.warning(String.format(CNES_MKDIR_ERROR, confDirectory));
        }

        // Producing the report
        final Report superReport = new ReportFactory(url, token, project, author, date).create();

        // Export all
        // export each linked quality profile
        exportAllQualityProfiles(superReport, profileExporter, confDirectory);

        // quality gate information
        final String qualityGateName = superReport.getQualityGate().getName();
        final String qualityGateConf = superReport.getQualityGate().getConf();
        // export the quality gate
        gateExporter.export(qualityGateConf, confDirectory, qualityGateName);

        // prepare docx report's filename
        final String docXFilename = formatFilename(REPORT_FILENAME,
                superReport.getProjectName());
        // export the full docx report
        docXExporter.export(superReport, reportPath+"/"+docXFilename, reportTemplate);

        // construct the xlsx filename by replacing date and name
        final String xlsXFilename = formatFilename(ISSUES_FILENAME,
                superReport.getProjectName());
        // export the xlsx issues' list
        issuesExporter.export(superReport, reportPath+"/"+xlsXFilename, issuesTemplate);
    }

    /**
     * Export all quality profiles related to a given report as xml file depending on exporter.
     * @param report Modeling data containing data to export.
     * @param exporter Class given the way to export previous data.
     * @param dir Directory for output.
     * @throws XmlException Thrown on xml error.
     * @throws BadExportationDataTypeException Thrown if the data does not correspond to exporter.
     * @throws OpenXML4JException Thrown on OpenXML error.
     * @throws IOException Thrown on files error.
     */
    public static void exportAllQualityProfiles(final Report report, final IExporter exporter, final String dir) throws
            XmlException, BadExportationDataTypeException, OpenXML4JException, IOException {
        for(ProfileMetaData metaData : report.getProject().getQualityProfiles()) {
            final Iterator<QualityProfile> iterator =
                    report.getQualityProfiles().iterator();
            boolean goOn = true;
            while(iterator.hasNext() && goOn) {
                final QualityProfile qp = iterator.next();
                if(qp.getKey().equals(metaData.getKey())) {
                    exporter.export(qp.getConf(), dir, qp.getKey());
                    goOn = false;
                }
            }
        }
    }

    /**
     * Format a given filename pattern
     * Add the date and the project's name
     * @param propertyName Name of pattern's property
     * @param projectName Name of the current project
     * @return a formatted filename
     */
    public static String formatFilename(String propertyName, String projectName) {
        // construct the filename by replacing date and name
        return StringManager.getProperty(propertyName)
                .replaceAll(DATE, new SimpleDateFormat(DATE_PATTERN).format(new Date()))
                .replaceAll(NAME, projectName);
    }

}
