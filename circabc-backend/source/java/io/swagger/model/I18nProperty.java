package io.swagger.model;

import java.util.HashMap;
import java.util.Objects;

/**
 * The object that is used to compile all the translations of a node into a JSON object. It is
 * basically composed of a map with a key languaguage code and its value
 */
public class I18nProperty extends HashMap<String, String> {

    /**
     *
     */
    private static final long serialVersionUID = -3027818284745604594L;

    public I18nProperty() {
        super();
    }

    public I18nProperty(String language, String value) {
        super();
        super.put(language, value);
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return Objects.equals(this, o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode());
    }

    @Override
    public String toString() {
        return "class I18nProperty {\n" + "    " + toIndentedString(super.toString()) + "\n" + "}";
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        return Util.toIndentedString(o);
    }

    public String getDefaultValue() {
        String res = "";

        if (!this.keySet().isEmpty() && !"".equals(this.get("en"))) {
            res = this.get("en");
        } else if (!this.keySet().isEmpty() && "".equals(this.get("en"))) {
            for (String value : this.values()) {
                if (!"".equals(value)) {
                    res = value;
                    break;
                }
            }
        }

        return res;
    }
}
