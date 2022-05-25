/**
 *
 */
package eu.cec.digit.circabc.repo.keywords;

/** @author beaurpi used in the manage keyword dialog during the exportation & importation phase. */
public class KeywordEntry {

    private String language;
    private String value;

    /** */
    public KeywordEntry() {
    }

    /** */
    public KeywordEntry(String language, String value) {
        this.language = language;
        this.value = value;
    }

    @Override
    public String toString() {

        return language + ":" + value;
    }

    /** @return the language */
    public String getLanguage() {
        return language;
    }

    /** @param language the language to set */
    public void setLanguage(String language) {
        this.language = language;
    }

    /** @return the value */
    public String getValue() {
        return value;
    }

    /** @param value the value to set */
    public void setValue(String value) {
        this.value = value;
    }
}
