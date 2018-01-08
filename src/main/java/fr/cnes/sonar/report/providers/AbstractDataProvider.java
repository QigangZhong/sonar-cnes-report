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

package fr.cnes.sonar.report.providers;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.cnes.sonar.report.exceptions.BadSonarQubeRequestException;
import fr.cnes.sonar.report.exceptions.UnknownParameterException;
import fr.cnes.sonar.report.input.Params;
import fr.cnes.sonar.report.input.StringManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Generic interface for resources providers
 * @author lequal
 */
public abstract class AbstractDataProvider {

    /**
     * Name for properties' file about requests
     */
    public static final String REQUESTS_PROPERTIES = "requests.properties";

    /**
     *  Field to retrieve languages list.
     */
    public static final String GET_LANGUAGES = "GET_LANGUAGES";

    /**
     *  Name of the request for getting quality profiles' linked projects
     */
    public static final String GET_QUALITY_PROFILES_PROJECTS_REQUEST =
            "GET_QUALITY_PROFILES_PROJECTS_REQUEST";
    /**
     *  Name of the request allowing to retrieve the quality gate
     */
    public static final String GET_QUALITY_GATE_REQUEST = "GET_QUALITY_GATE_REQUEST";
    /**
     *  Name of the request for getting quality gates' details
     */
    public static final String GET_QUALITY_GATES_DETAILS_REQUEST =
            "GET_QUALITY_GATES_DETAILS_REQUEST";
    /**
     *  Name of the request for getting quality profiles' linked rules
     */
    public static final String GET_QUALITY_PROFILES_RULES_REQUEST =
            "GET_QUALITY_PROFILES_RULES_REQUEST";
    /**
     *  Name of the request for getting issues
     */
    public static final String GET_ISSUES_REQUEST = "GET_ISSUES_REQUEST";
    /**
     *  Name of the request for getting facets
     */
    public static final String GET_FACETS_REQUEST = "GET_FACETS_REQUEST";
    /**
     *  Name of the property for the maximum number of results per page
     */
    public static final String MAX_PER_PAGE_SONARQUBE = "MAX_PER_PAGE_SONARQUBE";
    /**
     *  Name of the request for getting quality gates
     */
    public static final String GET_QUALITY_GATES_REQUEST = "GET_QUALITY_GATES_REQUEST";
    /**
     *  Name of the request for getting measures
     */
    public static final String GET_MEASURES_REQUEST = "GET_MEASURES_REQUEST";
    /**
     *  Name of the request for getting a specific project
     */
    public static final String GET_PROJECT_REQUEST = "GET_PROJECT_REQUEST";
    /**
     *  Name of the request for getting quality profiles
     */
    public static final String GET_QUALITY_PROFILES_REQUEST = "GET_QUALITY_PROFILES_REQUEST";
    /**
     *  Name of the request for getting quality profiles' configuration
     */
    public static final String GET_QUALITY_PROFILES_CONF_REQUEST =
            "GET_QUALITY_PROFILES_CONFIGURATION_REQUEST";
    /**
     * Field to search in json to get results' values
     */
    public static final String RESULTS = "results";
    /**
     * Field to search in json to get profiles
     */
    public static final String PROFILES = "profiles";
    /**
     * Field to search in json to get issues
     */
    public static final String ISSUES = "issues";
    /**
     * Field to search in json to get the total page's number
     */
    public static final String TOTAL = "total";
    /**
     * Field to search in json to get facets
     */
    public static final String FACETS = "facets";
    /**
     * Field to search in json to get the component
     */
    public static final String COMPONENT = "component";
    /**
     * Field to search in json to get measures
     */
    public static final String MEASURES = "measures";
    /**
     * Field to search in json to get the boolean saying if a profile is the default one
     */
    public static final String DEFAULT = "default";
    /**
     * Field to search in json to get quality gates
     */
    public static final String QUALITYGATES = "qualitygates";
    /**
     * Field to search in json to get rules
     */
    public static final String RULES = "rules";

    /**
     * Logger for the class
     */
    protected static final Logger LOGGER =
            Logger.getLogger(AbstractDataProvider.class.getCanonicalName());

    /**
     * Contain all the properties related to requests
     */
    private static Properties requests;

    /**
     * Params of the program itself
     */
    private Params params;

    /**
     * Tool for parsing json
     */
    private Gson gson;

    /**
     * Url of the sonarqube server
     */
    private String url;

    /**
     * Key of the project to report
     */
    private String projectKey;

    /**
     * Name of the used quality gate
     */
    private String qualityGateName;

    // Static initialization block for reading .properties
    static {
        // store properties
        requests = new Properties();
        // read the file
        InputStream input = null;

        final ClassLoader classLoader = AbstractDataProvider.class.getClassLoader();

        try {
            // load properties file as a stream
            input = classLoader.getResourceAsStream(REQUESTS_PROPERTIES);
            if(input!=null) {
                // load properties from the stream in an adapted structure
                requests.load(input);
            }
        } catch (IOException e) {
            // it logs all the stack trace
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } finally {
            if(input!=null) {
                try {
                    // close the stream if necessary (not null)
                    input.close();
                } catch (IOException e) {
                    // it logs all the stack trace
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Singleton which execute concrete http requests
     */
    private RequestManager requestManager;

    /**
     * Constructor
     * @param pParams Program's parameters
     * @param pSingleton RequestManager which does http request
     * @throws UnknownParameterException when a parameter is not known in the program
     */
    public AbstractDataProvider(final Params pParams, final RequestManager pSingleton)
            throws UnknownParameterException {
        this.params = pParams;
        // json tool
        this.gson = new Gson();
        // get sonar url
        this.url = getParams().get("sonar.url");
        // get project key
        this.projectKey = getParams().get("sonar.project.id");
        // set network tool to execute request
        this.requestManager = pSingleton;
    }

    /**
     * Give the value of the property corresponding to the key passed as parameter.
     * It gives only properties related to requests.
     * @param property Key of the property you want.
     * @return The value of the property you want as a String.
     */
    public static String getRequest(final String property) {
        return requests.getProperty(property);
    }

    /**
     * Check if the server has sent an error
     * @param jsonObject The response from the server
     * @throws BadSonarQubeRequestException thrown if the server do not understand our request
     */
    private void isErrorFree(final JsonObject jsonObject) throws BadSonarQubeRequestException {
        // we retrieve the exception
        final JsonElement error = jsonObject.get("errors");
        // if there is an error we search the message and throw an exception
        if (error != null) {
            // Json object of the error
            final JsonObject errorJO = error.getAsJsonArray().get(0).getAsJsonObject();
            // get the error message
            final JsonElement errorElement = errorJO.get("msg");
            final String errorMessage = (getGson().fromJson(errorElement, String.class));
            // throw exception if there was a problem when dealing with the server
            throw new BadSonarQubeRequestException(errorMessage);
        }
    }

    /**
     * Execute a given request
     * @param request Url for the request, for example http://sonarqube:1234/api/toto/list
     * @return Server's response as a JsonObject
     * @throws IOException if there were an error contacting the server
     * @throws BadSonarQubeRequestException if SonarQube Server sent an error
     */
    public JsonObject request(final String request)
            throws IOException, BadSonarQubeRequestException {
        // do the request to the server and return a string answer
        final String raw = stringRequest(request);

        // prepare json
        final JsonElement json;

        // verify that the server response was correct
        try {
            json = getGson().fromJson(raw, JsonElement.class);
        } catch (Exception e) {
            // log exception's message
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new BadSonarQubeRequestException("Server answered: " + raw +
                    StringManager.SPACE + e.getMessage());
        }

        // get the json object version
        final JsonObject jsonObject = json.getAsJsonObject();

        // verify if an error occurred
        isErrorFree(jsonObject);

        return jsonObject;
    }

    /**
     * Get the raw string response
     * @param request the raw url of the request
     * @return the server's response as a string
     * @throws IOException when not able to contact the server
     */
    protected String stringRequest(final String request) throws IOException {
        // prepare the request by replacing some relevant special characters
        // replace spaces
        String preparedRequest = request.replaceAll(" ", "%20");
        // replace + characters
        preparedRequest = preparedRequest.replaceAll("\\+", "%2B");

        // launch the request on sonarqube server and retrieve resources into a string
        return RequestManager.getInstance().get(preparedRequest);
    }

    /**
     * Getter for input
     * @return a Params object
     */
    private Params getParams() {
        return params;
    }

    /**
     * Setter for input
     * @param pParams the value to give
     */
    private void setParams(final Params pParams) {
        this.params = pParams;
    }

    /**
     * Json parsing tool
     * @return the gson tool
     */
    public Gson getGson() {
        return gson;
    }

    /**
     * Setter of gson
     * @param pGson value
     */
    public void setGson(final Gson pGson) {
        this.gson = pGson;
    }

    /**
     * Name of the project to report
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * Setter of url
     * @param pUrl value
     */
    public void setUrl(final String pUrl) {
        this.url = pUrl;
    }

    /**
     * Key of the project to report
     * @return the project key as a String
     */
    public String getProjectKey() {
        return projectKey;
    }

    /**
     * Setter of projectKey
     * @param pProjectKey value to give
     */
    public void setProjectKey(final String pProjectKey) {
        this.projectKey = pProjectKey;
    }

    /**
     * Quality gate's name (used by the project)
     * @return the name of the quality gate as a string
     */
    public String getQualityGateName() {
        return qualityGateName;
    }

    /**
     * Setter of qualityGateName
     * @param pQualityGateName value
     */
    public void setQualityGateName(final String pQualityGateName) {
        this.qualityGateName = pQualityGateName;
    }
}
