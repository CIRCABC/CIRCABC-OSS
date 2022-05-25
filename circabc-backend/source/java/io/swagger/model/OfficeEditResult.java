package io.swagger.model;

/**
 * Encapsulation to store the result of the check if the given document can be edited in Office
 *
 * @author schwerr
 */
public class OfficeEditResult {

    private String id = "";
    private boolean canEdit = false;
    private String documentLocation = "";
    private String errorDescription = "";

    /**
     * @return the canEdit
     */
    public boolean isCanEdit() {
        return canEdit;
    }

    /**
     * @param canEdit the canEdit to set
     */
    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the documentLocation
     */
    public String getDocumentLocation() {
        return documentLocation;
    }

    /**
     * @param documentLocation the documentLocation to set
     */
    public void setDocumentLocation(String documentLocation) {
        this.documentLocation = documentLocation;
    }

    /**
     * @return the errorDescription
     */
    public String getErrorDescription() {
        return errorDescription;
    }

    /**
     * @param errorDescription the errorDescription to set
     */
    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }
}
