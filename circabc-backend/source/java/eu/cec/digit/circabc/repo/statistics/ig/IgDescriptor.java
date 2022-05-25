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
package eu.cec.digit.circabc.repo.statistics.ig;

import org.alfresco.service.cmr.repository.NodeRef;

import java.util.Set;

/** @author beaurpi */
public class IgDescriptor {

    private String name;
    private String title;
    private NodeRef ref;
    private Set<String> setOfLeaders;

    private String description;
    private String lightDescription;
    private String creationDate;
    private Boolean publicVisibility;
    private Boolean publicEnabled;
    private Boolean registeredEnabled;
    private Set<String> availableServices;
    private String lastAccessDate;
    private String lastUpdateDate;
    private Integer nbDocuments;
    private Integer nbMembers;
    private String libraryDocSize;
    private String informationInfoSize;
    private Integer nbEvents;
    private Integer nbPosts;
    private Integer deepness;

    /** */
    public IgDescriptor() {
    }

    /** @return the name */
    public String getName() {
        return name;
    }

    /** @param name the name to set */
    public void setName(String name) {
        this.name = name;
    }

    /** @return the title */
    public String getTitle() {
        return title;
    }

    /** @param title the title to set */
    public void setTitle(String title) {
        this.title = title;
    }

    /** @return the ref */
    public NodeRef getRef() {
        return ref;
    }

    /** @param ref the ref to set */
    public void setRef(NodeRef ref) {
        this.ref = ref;
    }

    /** @return the setOfLeaders */
    public Set<String> getSetOfLeaders() {
        return setOfLeaders;
    }

    /** @param setOfLeaders the setOfLeaders to set */
    public void setSetOfLeaders(Set<String> setOfLeaders) {
        this.setOfLeaders = setOfLeaders;
    }

    /** @return the description */
    public String getDescription() {
        return description;
    }

    /** @param description the description to set */
    public void setDescription(String description) {
        this.description = description;
    }

    /** @return the creationDate */
    public String getCreationDate() {
        return creationDate;
    }

    /** @param string the creationDate to set */
    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    /** @return the publicVisibility */
    public Boolean getPublicVisibility() {
        return publicVisibility;
    }

    /** @param publicVisibility the publicVisibility to set */
    public void setPublicVisibility(Boolean publicVisibility) {
        this.publicVisibility = publicVisibility;
    }

    /** @return the availableServices */
    public Set<String> getAvailableServices() {
        return availableServices;
    }

    /** @param availableServices the availableServices to set */
    public void setAvailableServices(Set<String> availableServices) {
        this.availableServices = availableServices;
    }

    /** @return the lightDescription */
    public String getLightDescription() {
        return lightDescription;
    }

    /** @param lightDescription the lightDescription to set */
    public void setLightDescription(String lightDescription) {
        this.lightDescription = lightDescription;
    }

    /** @return the publicEnabled */
    public Boolean getPublicEnabled() {
        return publicEnabled;
    }

    /** @param publicEnabled the publicEnabled to set */
    public void setPublicEnabled(Boolean publicEnabled) {
        this.publicEnabled = publicEnabled;
    }

    /** @return the registeredEnabled */
    public Boolean getRegisteredEnabled() {
        return registeredEnabled;
    }

    /** @param registeredEnabled the registeredEnabled to set */
    public void setRegisteredEnabled(Boolean registeredEnabled) {
        this.registeredEnabled = registeredEnabled;
    }

    /** @return the lastAccessDate */
    public String getLastAccessDate() {
        return lastAccessDate;
    }

    /** @param lastAccessDate the lastAccessDate to set */
    public void setLastAccessDate(String lastAccessDate) {
        this.lastAccessDate = lastAccessDate;
    }

    /** @return the lastUpdatDate */
    public String getLastUpdateDate() {
        return lastUpdateDate;
    }

    /** @param lastUpdatDate the lastUpdatDate to set */
    public void setLastUpdateDate(String lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    /** @return the nbDocuments */
    public Integer getNbDocuments() {
        return nbDocuments;
    }

    /** @param nbDocuments the nbDocuments to set */
    public void setNbDocuments(Integer nbDocuments) {
        this.nbDocuments = nbDocuments;
    }

    /** @return the nbMembers */
    public Integer getNbMembers() {
        return nbMembers;
    }

    /** @param nbMembers the nbMembers to set */
    public void setNbMembers(Integer nbMembers) {
        this.nbMembers = nbMembers;
    }

    /** @return the libraryDocSize */
    public String getLibraryDocSize() {
        return libraryDocSize;
    }

    /** @param libraryDocSize the libraryDocSize to set */
    public void setLibraryDocSize(String libraryDocSize) {
        this.libraryDocSize = libraryDocSize;
    }

    /** @return the informationInfoSize */
    public String getInformationInfoSize() {
        return informationInfoSize;
    }

    /** @param informationInfoSize the informationInfoSize to set */
    public void setInformationInfoSize(String informationInfoSize) {
        this.informationInfoSize = informationInfoSize;
    }

    /** @return the nbEvents */
    public Integer getNbEvents() {
        return nbEvents;
    }

    /** @param nbEvents the nbEvents to set */
    public void setNbEvents(Integer nbEvents) {
        this.nbEvents = nbEvents;
    }

    /** @return the nbPosts */
    public Integer getNbPosts() {
        return nbPosts;
    }

    /** @param nbPosts the nbPosts to set */
    public void setNbPosts(Integer nbPosts) {
        this.nbPosts = nbPosts;
    }

    public Integer getDeepness() {
        return deepness;
    }

    public void setDeepness(Integer deepness) {
        this.deepness = deepness;
    }
}
