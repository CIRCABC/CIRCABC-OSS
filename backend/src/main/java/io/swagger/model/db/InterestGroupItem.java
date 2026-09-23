package io.swagger.model.db;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Lightweight data model representing a single Interest Group (IG) as exposed to
 * REST API consumers.
 *
 * <p>An instance is typically built from an {@link InterestGroupResult} (a raw
 * query result coming from the database/search layer) and carries only the
 * fields needed by the frontend to render an Interest Group entry, together with
 * flags describing the current user's relationship to that group (membership,
 * registration, join eligibility, visibility).
 */
public class InterestGroupItem {

  /**
   * Best available human-readable title for the group. Resolved from the
   * translated title, falling back to the default title and finally to the
   * group name.
   */
  private String bestTitle;

  /** Technical (short) name of the Interest Group. */
  private String name;

  /** Alfresco node identifier (the id part of the group's {@link NodeRef}). */
  private String id;

  /** Whether the current user is a member of this group. */
  private boolean member;

  /** Whether the current user is registered for this group. */
  private boolean registered;

  /** Whether the group is publicly visible. */
  private boolean isPublic;

  /** Reference to the group's logo resource, if any. */
  private String logoRef;

  /**
   * Whether the description was truncated and additional details can be
   * requested/expanded (set when the HTML-free description exceeds 162
   * characters).
   */
  private Boolean needMoreDetails;

  /** Whether the current user is allowed to apply for membership. */
  private Boolean canJoin;

  /** Group description stripped of HTML markup. */
  private String noHtmlDescription;

  /**
   * Builds an {@code InterestGroupItem} from a raw {@link InterestGroupResult}.
   *
   * <p>Resolves the best title (translated title, then default title, then
   * name), copies over the identifier and description, and derives the
   * user-relationship flags ({@code member}, {@code registered},
   * {@code canJoin}, {@code isPublic}). The {@code needMoreDetails} flag is set
   * to {@code true} when the HTML-free description is longer than 162
   * characters.
   *
   * @param interestGroupResult the raw query result to map from
   */
  public InterestGroupItem(InterestGroupResult interestGroupResult) {
    if (interestGroupResult.getTitleTranslation() == null) {
      if (interestGroupResult.getTitle() == null) {
        bestTitle = interestGroupResult.getName();
      } else {
        bestTitle = interestGroupResult.getTitle();
      }
    } else {
      bestTitle = interestGroupResult.getTitleTranslation();
    }
    name = interestGroupResult.getName();
    id = new NodeRef(interestGroupResult.getNodeRef()).getId();
    needMoreDetails = false;
    noHtmlDescription = interestGroupResult.getLightDescTranslation();
    if (noHtmlDescription != null && noHtmlDescription.length() > 162) {
      needMoreDetails = true;
    }
    canJoin = interestGroupResult.getIsApplyForMembership();
    member = interestGroupResult.getMemberId() != null;
    registered = interestGroupResult.getIsRegistered();
    isPublic = interestGroupResult.getIsPublic();
    logoRef = interestGroupResult.getLogoRef();
  }

  /**
   * @return {@code true} if the current user is a member of this group
   */
  public boolean isMember() {
    return member;
  }

  /**
   * @return {@code true} if the current user is registered for this group
   */
  public boolean isRegistered() {
    return registered;
  }

  /**
   * @return the best available human-readable title
   */
  public String getBestTitle() {
    return bestTitle;
  }

  /**
   * @param bestTitle the best title to set
   */
  public void setBestTitle(String bestTitle) {
    this.bestTitle = bestTitle;
  }

  /**
   * @return the technical name of the group
   */
  public String getName() {
    return name;
  }

  /**
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the Alfresco node identifier of the group
   */
  public String getId() {
    return id;
  }

  /**
   * @param id the identifier to set
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * @return {@code true} if the description was truncated and more details can
   *     be requested
   */
  public Boolean getNeedMoreDetails() {
    return needMoreDetails;
  }

  /**
   * @param needMoreDetails the needMoreDetails flag to set
   */
  public void setNeedMoreDetails(Boolean needMoreDetails) {
    this.needMoreDetails = needMoreDetails;
  }

  /**
   * @return {@code true} if the current user may apply for membership
   */
  public Boolean getCanJoin() {
    return canJoin;
  }

  /**
   * @param canJoin the canJoin flag to set
   */
  public void setCanJoin(Boolean canJoin) {
    this.canJoin = canJoin;
  }

  /**
   * @return the group description with HTML markup removed
   */
  public String getNoHtmlDescription() {
    return noHtmlDescription;
  }

  /**
   * @param noHtmlDescription the HTML-free description to set
   */
  public void setNoHtmlDescription(String noHtmlDescription) {
    this.noHtmlDescription = noHtmlDescription;
  }

  /**
   * @return {@code true} if the group is publicly visible
   */
  public boolean isPublic() {
    return isPublic;
  }

  /**
   * @param isPublic the public-visibility flag to set
   */
  public void setPublic(boolean isPublic) {
    this.isPublic = isPublic;
  }

  /**
   * @return the logoRef
   */
  public String getLogoRef() {
    return logoRef;
  }

  /**
   * @param logoRef the logoRef to set
   */
  public void setLogoRef(String logoRef) {
    this.logoRef = logoRef;
  }
}
