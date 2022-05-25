package io.swagger.model;

public class Util {

    private Util() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces (except the first line).
     */
    static String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
