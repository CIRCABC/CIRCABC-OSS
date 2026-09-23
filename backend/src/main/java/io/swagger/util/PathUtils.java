package io.swagger.util;

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

import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.Path.Element;
import org.alfresco.util.ISO9075;

/**
 * Utility class for converting Alfresco {@link Path} objects into human-readable, slash-separated
 * string paths as used throughout CIRCABC.
 *
 * <p>The Alfresco repository exposes node locations as {@link Path} instances whose elements are
 * qualified names (e.g. {@code {http://www.alfresco.org/model/content/1.0}contains}). This class
 * flattens those paths into plain strings, skipping the {@code cm:contains} association elements,
 * stripping the namespace prefixes from each element and ISO-9075 decoding the result so that the
 * returned value reflects the original (unescaped) node names.
 *
 * <p>CIRCABC organises content under a fixed hierarchy
 * ({@code /company_home/circabc/<category>/<interestGroup>/<library>/...}). The various
 * {@code get*Path} helpers return the path relative to a specific level of that hierarchy by
 * skipping a fixed number of leading elements.
 *
 * <p>This class is not instantiable; all members are static.
 *
 * @author Slobodan Filipovic
 */
public class PathUtils {

  /**
   * The fully qualified name of the Alfresco {@code cm:contains} child association. Path elements
   * matching this value represent structural containment links rather than named nodes and are
   * therefore skipped when building the readable path.
   */
  private static final String HTTP_WWW_ALFRESCO_ORG_MODEL_CONTENT_1_0_CONTAINS =
    "{http://www.alfresco.org/model/content/1.0}contains";

  /** Private constructor to prevent instantiation of this static utility class. */
  private PathUtils() {}

  /**
   * Returns the full readable path of a node, starting from the repository root.
   *
   * @param path the Alfresco node path to convert
   * @param includeFirstSlash whether the resulting path should be prefixed with a leading slash
   * @return the full path, with namespace prefixes stripped and ISO-9075 decoded
   */
  public static String getFullPath(Path path, boolean includeFirstSlash) {
    return getPath(path, 0, includeFirstSlash);
  }

  /**
   * Get path starting from circabc node
   *
   * @param path path of node
   * @param includeFirstSlash whether the resulting path should be prefixed with a leading slash
   * @return circabc path of node staring with circabc removing /company_home
   */
  public static String getCircabcPath(Path path, boolean includeFirstSlash) {
    return getPath(path, 2, includeFirstSlash);
  }

  /**
   * Get path starting from category node
   *
   * @param path path of node
   * @param includeFirstSlash whether the resulting path should be prefixed with a leading slash
   * @return circabc path of node staring with circabc removing /company_home/circabc
   */
  public static String getCategoryPath(Path path, boolean includeFirstSlash) {
    return getPath(path, 3, includeFirstSlash);
  }

  /**
   * Get path starting from interest group node
   *
   * @param path path of node
   * @param includeFirstSlash whether the resulting path should be prefixed with a leading slash
   * @return circabc path of node staring with circabc removing /company_home/circabc/categoryxy
   */
  public static String getInterestGroupPath(
    Path path,
    boolean includeFirstSlash
  ) {
    return getPath(path, 4, includeFirstSlash);
  }

  /**
   * Get path starting from library node
   *
   * @param path path of node
   * @param includeFirstSlash whether the resulting path should be prefixed with a leading slash
   * @return circabc path of node staring with circabc removing /company_home/circabc/categoryXY/interestGroupXY
   */
  public static String getLibraryPath(Path path, boolean includeFirstSlash) {
    return getPath(path, 5, includeFirstSlash);
  }

  /**
   * Builds a readable, slash-separated string from an Alfresco {@link Path}, starting from a given
   * element index.
   *
   * <p>Elements are processed in order; {@code null} elements and {@code cm:contains} association
   * elements are ignored and do not count towards the returned path segments (though they are
   * counted for indexing purposes). For each remaining element beyond {@code start}, the namespace
   * prefix (everything up to and including the closing {@code '}'}) is stripped and the element name
   * is appended, separated by slashes. The assembled path is finally ISO-9075 decoded.
   *
   * @param path the Alfresco node path to convert
   * @param start the index of the first path element to include; elements before this index are
   *     skipped, allowing the path to be expressed relative to a deeper node in the hierarchy
   * @param includeFirstSlash whether the first included segment should be preceded by a leading
   *     slash
   * @return the ISO-9075 decoded, slash-separated path with namespace prefixes removed
   */
  public static String getPath(
    Path path,
    int start,
    boolean includeFirstSlash
  ) {
    String result;
    StringBuilder buf = new StringBuilder(256);
    int i = 0;
    for (Element element : path) {
      String elementString = element.getElementString();
      if (
        (elementString == null) ||
        elementString.equals(HTTP_WWW_ALFRESCO_ORG_MODEL_CONTENT_1_0_CONTAINS)
      ) {
        continue;
      }
      if (i >= start && !elementString.equalsIgnoreCase("/")) {
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

      i++;
    }

    result = ISO9075.decode(buf.toString());
    return result;
  }
}
