/**
 * Copyright 2006 European Community
 *
 * <p>Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 *
 * <p>https://joinup.ec.europa.eu/software/page/eupl
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
/**
 *
 */
package eu.cec.digit.circabc.service.statistics.ig;

import eu.cec.digit.circabc.repo.log.ActivityCountDAO;
import eu.cec.digit.circabc.repo.statistics.ig.ServiceTreeRepresentation;
import org.alfresco.service.cmr.repository.NodeRef;

import java.io.Serializable;
import java.util.List;

/** @author beaurpi */
public interface IgStatisticsService {

    void buildStatsData(NodeRef igRoot);

    /**
     * *
     *
     * @param igRoot
     * @return the number of invited users in InterestGroup
     */
    Integer getNumberOfUsers(NodeRef igRoot);

    /**
     * *
     *
     * @return the number of document stored in the library
     */
    Integer getNumberOfLibraryDocuments();

    /** @return the number of versions of documents */
    Integer getNumberOfVersions();

    /** @return the size of versions of documents */
    Long getSizeOfVersions();

    /** @return the numberOfCustomizationAndHiddenContent */
    Integer getNumberOfCustomizationAndHiddenContent();

    /** @return the sizeOfCustomizationAndHiddenContent */
    Long getSizeOfCustomizationAndHiddenContent();

    /**
     * *
     *
     * @return the number of Spaces stored in the library
     */
    Integer getNumberOfLibrarySpaces();

    /**
     * *
     *
     * @return the number of document stored in the library
     */
    Integer getNumberOfInformationDocuments();

    /**
     * *
     *
     * @return the number of Spaces stored in the library
     */
    Integer getNumberOfInformationSpaces();

    /**
     * *
     *
     * @param igRoot
     * @return the title of the group.
     */
    String getIGTitle(NodeRef igRoot);

    /**
     * *
     *
     * @param igRoot
     * @return the created date of Interest group
     */
    Serializable getIGCreationDate(NodeRef igRoot);

    /**
     * *
     *
     * @return the number of Meeting objects in this IG
     */
    Integer getNumbetOfMeetings();

    /**
     * *
     *
     * @return the number of Event objects in this IG
     */
    Integer getNumbetOfEvents();

    /**
     * *
     *
     * @return the number of forums in this IG
     */
    Integer getNumberOfForums();

    /**
     * *
     *
     * @return the number of topics in this IG
     */
    Integer getNumberOfTopics();

    /**
     * *
     *
     * @return the number of topics in this IG
     */
    Integer getNumberOfPosts();

    /**
     * *
     *
     * @return the total size of content in Library
     */
    Long getContentSizeOfLibrary();

    /**
     * *
     *
     * @return the total size of content in Library
     */
    Long getContentSizeOfInformation();

    /**
     * *
     *
     * @param igRoot
     * @return the Tree structure of the library service
     */
    ServiceTreeRepresentation getLibraryStructure(NodeRef igRoot);

    /**
     * *
     *
     * @param igRoot
     * @return the Tree structure of the information service
     */
    ServiceTreeRepresentation getInformationStructure(NodeRef igRoot);

    /**
     * *
     *
     * @param currentNode
     * @return the Tree structure of the newsgroup service
     */
    ServiceTreeRepresentation getNewsgroupsStructure(NodeRef currentNode);

    /**
     * * get the list from audit database
     *
     * @param igRoot
     * @return
     */
    List<ActivityCountDAO> getListOfActivityCount(NodeRef igRoot);

    /** get the deepness of the library or information service */
    int getMaxLevel();
}
