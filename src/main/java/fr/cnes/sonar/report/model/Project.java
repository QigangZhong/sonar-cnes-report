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

package fr.cnes.sonar.report.model;

import java.util.*;

/**
 * Represents a project
 * @author lequal
 */
public class Project {
    /**
     * Key used by sonarqube
     */
    private String key;
    /**
     * Name of the project
     */
    private String name;
    /**
     * Version of the project
     */
    private String version;
    /**
     * Description of the project
     */
    private String description;
    /**
     * Quality profiles linked to the project
     */
    private ProfileMetaData[] qualityProfiles;
    /**
     * Languages of the project
     */
    private Map<String, Language> languages;

    /**
     * Constructor to set basics
     * @param pKey SonarQube key
     * @param pName Name of the project
     * @param pVersion Version given by the user
     * @param pDescription Project's description
     */
    public Project(String pKey, String pName, String pVersion, String pDescription) {
        this.key = pKey;
        this.name = pName;
        this.version = pVersion;
        this.description = pDescription;
        this.qualityProfiles = new ProfileMetaData[0];
        this.languages = new HashMap<>();
    }

    /**
     * Getter for key
     * @return key
     */
    public String getKey() {
        return key;
    }

    /**
     * Setter for name
     * @param pKey name
     */
    public void setKey(String pKey) {
        this.key = pKey;
    }

    /**
     * Getter for name
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Setter for name
     * @param pName value
     */
    public void setName(String pName) {
        this.name = pName;
    }

    /**
     * Getter for name
     * @return name
     */
    public String getVersion() {
        return version;
    }

    /**
     * Setter for version
     * @param pVersion value
     */
    public void setVersion(String pVersion) {
        this.version = pVersion;
    }

    /**
     * Getter for name
     * @return name
     */
    public String getDescription() {
        return description;
    }

    /**
     * Setter for description
     * @param pDescription value
     */
    public void setDescription(String pDescription) {
        this.description = pDescription;
    }

    /**
     * Get meta data about linked quality profiles
     * @return an array of quality profiles
     */
    public ProfileMetaData[] getQualityProfiles() {
        return qualityProfiles.clone();
    }

    /**
     * Set the value of quality profiles by making a copy
     * @param pQualityProfiles value to set
     */
    public void setQualityProfiles(ProfileMetaData[] pQualityProfiles) {
        this.qualityProfiles = pQualityProfiles.clone();
    }

    /**
     * Find a Language by key
     * @param pLanguageKey Key of the language to get
     * @return A Language object
     */
    public Language getLanguage(String pLanguageKey) {
        return this.languages.get(pLanguageKey);
    }

    /**
     * Return all languages as a list
     * @return list of languages
     */
    public List<Language> getLanguages() {
        return new ArrayList<>(languages.values());
    }

    /**
     * Set the languages' values
     * @param pLanguages map to set
     */
    public void setLanguages(Map<String, Language> pLanguages) {
        this.languages = new HashMap<>(pLanguages);
    }
}
