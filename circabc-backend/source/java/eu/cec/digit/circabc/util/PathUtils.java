/*******************************************************************************
 * Copyright 2006 European Community
 *
 *  Licensed under the EUPL, Version 1.1 or - as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  You may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  https://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 ******************************************************************************/
package eu.cec.digit.circabc.util;

import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.Path.Element;
import org.alfresco.util.ISO9075;

/**
 * @author Slobodan Filipovic
 */
public class PathUtils {

    private static final String HTTP_WWW_ALFRESCO_ORG_MODEL_CONTENT_1_0_CONTAINS = "{http://www.alfresco.org/model/content/1.0}contains";

    private PathUtils() {

    }

    public static String getFullPath(Path path, boolean includeFirstSlash) {
        return getPath(path, 0, includeFirstSlash);
    }

    /**
     * Get path starting from circabc node
     *
     * @param path path of node
     * @return circabc path of node staring with circabc removing /company_home
     */
    public static String getCircabcPath(Path path, boolean includeFirstSlash) {
        return getPath(path, 2, includeFirstSlash);
    }

    /**
     * Get path starting from category node
     *
     * @param path path of node
     * @return circabc path of node staring with circabc removing /company_home/circabc
     */
    public static String getCategoryPath(Path path, boolean includeFirstSlash) {
        return getPath(path, 3, includeFirstSlash);
    }

    /**
     * Get path starting from interest group node
     *
     * @param path path of node
     * @return circabc path of node staring with circabc removing /company_home/circabc/categoryxy
     */
    public static String getInterestGroupPath(Path path, boolean includeFirstSlash) {
        return getPath(path, 4, includeFirstSlash);
    }

    /**
     * Get path starting from library node
     *
     * @param path path of node
     * @return circabc path of node staring with circabc removing /company_home/circabc/categoryXY/interestGroupXY
     */
    public static String getLibraryPath(Path path, boolean includeFirstSlash) {
        return getPath(path, 5, includeFirstSlash);
    }

    public static String getPath(Path path, int start, boolean includeFirstSlash) {

        String result;
        StringBuilder buf = new StringBuilder(256);
        int i = 0;
        for (Element element : path) {

            String elementString = element.getElementString();
            if ((elementString == null) || elementString
                    .equals(HTTP_WWW_ALFRESCO_ORG_MODEL_CONTENT_1_0_CONTAINS)) {
                continue;
            }
            if (i >= start) {
                if (!elementString.equalsIgnoreCase("/")) {
                    if ((i == start) && !includeFirstSlash) {
                        // do nothing
                    } else {
                        buf.append("/");
                    }
                    int endIndex = elementString.indexOf('}');
                    if (endIndex > -1) {
                        elementString = elementString.substring(endIndex + 1);
                    }

                    buf.append(elementString);
                }
            }
            i++;

        }

        result = ISO9075.decode(buf.toString());
        return result;
    }
}
