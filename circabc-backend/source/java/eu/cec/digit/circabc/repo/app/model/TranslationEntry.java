/**
 *
 */
package eu.cec.digit.circabc.repo.app.model;

/** @author beaurpi */
public class TranslationEntry {

    private Long id;
    private Long alfLocaleId;
    private String translation;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAlfLocaleId() {
        return alfLocaleId;
    }

    public void setAlfLocaleId(Long alfLocaleId) {
        this.alfLocaleId = alfLocaleId;
    }

    public String getTranslation() {
        return translation;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }
}
