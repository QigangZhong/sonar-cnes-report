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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.cnes.sonar.report.exceptions.BadSonarQubeRequestException;
import fr.cnes.sonar.report.exceptions.UnknownParameterException;
import fr.cnes.sonar.report.model.Measure;
import fr.cnes.sonar.report.input.Params;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Provides issue items
 * @author lequal
 */
public class MeasureProvider extends AbstractDataProvider {

    /**
     * Complete constructor
     * @param params Program's parameters
     * @param singleton RequestManager which does http request
     * @throws UnknownParameterException The program does not recognize the parameter
     */
    public MeasureProvider(Params params, RequestManager singleton)
            throws UnknownParameterException {
        super(params, singleton);
    }

    /**
     * Get all the measures of a project
     * @return Array containing all the measures
     * @throws IOException when contacting the server
     * @throws BadSonarQubeRequestException when the server does not understand the request
     */
    public List<Measure> getMeasures() throws IOException, BadSonarQubeRequestException {
        // results list
        final List<Measure> res = new ArrayList<>();

        // send a request to sonarqube server and return th response as a json object
        // if there is an error on server side this method throws an exception
        final JsonObject jo = request(String.format(getRequest(GET_MEASURES_REQUEST),
                getUrl(), getProjectKey()));

        // json element containing measure information
        final JsonElement measuresJE = jo.get(COMPONENT).getAsJsonObject().get(MEASURES);
        // put json in a list of measures
        final Measure[] tmp = (getGson().fromJson(measuresJE, Measure[].class));
        // then add all measure to the results list
        res.addAll(Arrays.asList(tmp));

        // return the list
        return res;
    }
}
