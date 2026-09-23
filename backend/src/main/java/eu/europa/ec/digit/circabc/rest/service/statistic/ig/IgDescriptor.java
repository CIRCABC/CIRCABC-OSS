package eu.europa.ec.digit.circabc.rest.service.statistic.ig;

import java.util.Set;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Plain data holder (DTO) describing an Interest Group (IG) and its aggregated
 * statistics.
 *
 * <p>Instances of this class are used by the IG statistics feature to carry
 * descriptive metadata (name, title, visibility flags, available services,
 * leaders, contact information) together with computed usage figures such as
 * the number of documents, members, events and posts, as well as storage sizes
 * for the Library and Information services.
 *
 * <p>This class contains only fields with their corresponding getters and
 * setters; it holds no business logic and is intended to be populated by the
 * statistics services and serialized to the REST/JSON response.
 */
public class IgDescriptor {

  /** Technical (system) name of the Interest Group. */
  private String name;
  /** Human-readable display title of the Interest Group. */
  private String title;
  /** Alfresco node reference identifying the Interest Group node. */
  private NodeRef ref;
  /** User names (or identifiers) of the members acting as leaders of the IG. */
  private Set<String> setOfLeaders;

  /** Full textual description of the Interest Group. */
  private String description;
  /** Short/summary description of the Interest Group. */
  private String lightDescription;
  /** Creation date of the Interest Group, formatted as a string. */
  private String creationDate;
  /** Whether the Interest Group is publicly visible. */
  private Boolean publicVisibility;
  /** Whether public (anonymous) access to the Interest Group is enabled. */
  private Boolean publicEnabled;
  /** Whether access for registered users is enabled. */
  private Boolean registeredEnabled;
  /** Names of the services (e.g. Library, Newsgroup, Events) available in the IG. */
  private Set<String> availableServices;
  /** Date of the last access to the Interest Group, formatted as a string. */
  private String lastAccessDate;
  /** Date of the last update of the Interest Group, formatted as a string. */
  private String lastUpdateDate;
  /** Total number of documents contained in the Interest Group. */
  private Integer nbDocuments;
  /** Total number of members of the Interest Group. */
  private Integer nbMembers;
  /** Aggregated size of the Library documents (in bytes). */
  private Double libraryDocSize;
  /** Aggregated size of the Information service content (in bytes). */
  private Double informationInfoSize;
  /** Total number of events in the Interest Group. */
  private Integer nbEvents;
  /** Total number of forum/newsgroup posts in the Interest Group. */
  private Integer nbPosts;
  /** Nesting depth of the Interest Group within its category hierarchy. */
  private Integer deepness;
  /** Contact information associated with the Interest Group. */
  private String contactInformation;

  /** Default constructor for bean initialization */
  @SuppressWarnings("java:S1186") // Empty method is intentional
  public IgDescriptor() {
    // Default constructor required for bean/serialization frameworks
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

  /** @param creationDate the creationDate to set */
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

  /** @param lastUpdateDate the lastUpdatDate to set */
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
  public Double getLibraryDocSize() {
    return libraryDocSize;
  }

  /** @param libraryDocSize the libraryDocSize to set */
  public void setLibraryDocSize(Double libraryDocSize) {
    this.libraryDocSize = libraryDocSize;
  }

  /** @return the informationInfoSize */
  public Double getInformationInfoSize() {
    return informationInfoSize;
  }

  /** @param informationInfoSize the informationInfoSize to set */
  public void setInformationInfoSize(Double informationInfoSize) {
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

  /** @return the deepness */
  public Integer getDeepness() {
    return deepness;
  }

  /** @param deepness the deepness to set */
  public void setDeepness(Integer deepness) {
    this.deepness = deepness;
  }

  /** @return the contactInformation */
  public String getContactInformation() {
    return contactInformation;
  }

  /** @param contactInformation the contactInformation to set */
  public void setContactInformation(String contactInformation) {
    this.contactInformation = contactInformation;
  }
}
