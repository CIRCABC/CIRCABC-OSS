/**
 *
 */
package io.swagger.model;

/** @author beaurpi */
public class ColumnOptions {

    private Boolean name = true;
    private Boolean title = true;
    private Boolean version = true;
    private Boolean modification = true;
    private Boolean creation = false;
    private Boolean size = true;
    private Boolean expiration = true;
    private Boolean status = false;
    private Boolean description = false;
    private Boolean author = false;
    private Boolean securityRanking = false;

    /** @return the name */
    public Boolean getName() {
        return name;
    }

    public Boolean getTitle() {
        return title;
    }

    public void setTitle(Boolean title) {
        this.title = title;
    }

    /** @param name the name to set */
    public void setName(Boolean name) {
        this.name = name;
    }

    /** @return the version */
    public Boolean getVersion() {
        return version;
    }

    /** @param version the version to set */
    public void setVersion(Boolean version) {
        this.version = version;
    }

    /** @return the modification */
    public Boolean getModification() {
        return modification;
    }

    /** @param modification the modification to set */
    public void setModification(Boolean modification) {
        this.modification = modification;
    }

    /** @return the creation */
    public Boolean getCreation() {
        return creation;
    }

    /** @param creation the creation to set */
    public void setCreation(Boolean creation) {
        this.creation = creation;
    }

    /** @return the size */
    public Boolean getSize() {
        return size;
    }

    /** @param size the size to set */
    public void setSize(Boolean size) {
        this.size = size;
    }

    /** @return the expiration */
    public Boolean getExpiration() {
        return expiration;
    }

    /** @param expiration the expiration to set */
    public void setExpiration(Boolean expiration) {
        this.expiration = expiration;
    }

    /** @return the status */
    public Boolean getStatus() {
        return status;
    }

    /** @param status the status to set */
    public void setStatus(Boolean status) {
        this.status = status;
    }

    /** @return the description */
    public Boolean getDescription() {
        return description;
    }

    /** @param description the description to set */
    public void setDescription(Boolean description) {
        this.description = description;
    }

    /** @return the author */
    public Boolean getAuthor() {
        return author;
    }

    /** @param author the author to set */
    public void setAuthor(Boolean author) {
        this.author = author;
    }

    public Boolean getSecurityRanking() {
        return securityRanking;
    }

    public void setSecurityRanking(Boolean securityRanking) {
        this.securityRanking = securityRanking;
    }
}
