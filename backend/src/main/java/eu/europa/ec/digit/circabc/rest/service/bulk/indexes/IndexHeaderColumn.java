/**
 * ***************************************************************************** Copyright 2006
 * European Community
 *
 * <p>Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 *
 * <p>https://joinup.ec.europa.eu/software/page/eupl
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * ****************************************************************************
 */
package eu.europa.ec.digit.circabc.rest.service.bulk.indexes;

/**
 * Utility class that defines the canonical column-name constants used by the bulk import/export
 * indexing feature.
 *
 * <p>During a bulk operation, CIRCABC reads an index (e.g. a spreadsheet or CSV descriptor) whose
 * columns describe the metadata to apply to each uploaded content item. Each constant declared here
 * corresponds to the literal header string expected for a given column, so that the parsing logic
 * can reference columns by symbolic name rather than by hard-coded string. This class replaces the
 * previously used {@code Headers} interface.
 *
 * <p>The class is not meant to be instantiated; it only exposes {@code public static final String}
 * constants.
 */
public class IndexHeaderColumn {

  /**
   * Private constructor to prevent instantiation of this utility class and to hide the implicit
   * public constructor.
   */
  private IndexHeaderColumn() {
    // Utility class should not be instantiated
  }

  /** Column holding the content item's file name. */
  public static final String NAME = "NAME";

  /** Column holding the content item's title. */
  public static final String TITLE = "TITLE";

  /** Column holding the content item's description. */
  public static final String DESCRIPTION = "DESCRIPTION";

  /** Column holding the author of the content item. */
  public static final String AUTHOR = "AUTHOR";

  /** Column holding the keywords associated with the content item. */
  public static final String KEYWORDS = "KEYWORDS";

  /** Column holding the publication/workflow status of the content item. */
  public static final String STATUS = "STATUS";

  /** Column holding the issue date of the content item. */
  public static final String ISSUE_DATE = "ISSUE DATE";

  /** Column holding the reference identifier of the content item. */
  public static final String REFERENCE = "REFERENCE";

  /** Column holding the expiration date of the content item. */
  public static final String EXPIRATION_DATE = "EXPIRDATE";

  /** Column holding the security ranking of the content item. */
  public static final String SECURITY_RANKING = "SECRANK";

  /**
   * Common prefix shared by the dynamic attribute columns ({@code ATTRI1}..{@code ATTRI20}). Used
   * to detect and match attribute columns generically.
   */
  public static final String ATTRIPREFIX = "ATTRI";
  /** Dynamic attribute column 1. */
  public static final String ATTRI1 = "ATTRI1";

  /** Dynamic attribute column 2. */
  public static final String ATTRI2 = "ATTRI2";

  /** Dynamic attribute column 3. */
  public static final String ATTRI3 = "ATTRI3";

  /** Dynamic attribute column 4. */
  public static final String ATTRI4 = "ATTRI4";

  /** Dynamic attribute column 5. */
  public static final String ATTRI5 = "ATTRI5";

  /** Dynamic attribute column 6. */
  public static final String ATTRI6 = "ATTRI6";

  /** Dynamic attribute column 7. */
  public static final String ATTRI7 = "ATTRI7";

  /** Dynamic attribute column 8. */
  public static final String ATTRI8 = "ATTRI8";

  /** Dynamic attribute column 9. */
  public static final String ATTRI9 = "ATTRI9";

  /** Dynamic attribute column 10. */
  public static final String ATTRI10 = "ATTRI10";

  /** Dynamic attribute column 11. */
  public static final String ATTRI11 = "ATTRI11";

  /** Dynamic attribute column 12. */
  public static final String ATTRI12 = "ATTRI12";

  /** Dynamic attribute column 13. */
  public static final String ATTRI13 = "ATTRI13";

  /** Dynamic attribute column 14. */
  public static final String ATTRI14 = "ATTRI14";

  /** Dynamic attribute column 15. */
  public static final String ATTRI15 = "ATTRI15";

  /** Dynamic attribute column 16. */
  public static final String ATTRI16 = "ATTRI16";

  /** Dynamic attribute column 17. */
  public static final String ATTRI17 = "ATTRI17";

  /** Dynamic attribute column 18. */
  public static final String ATTRI18 = "ATTRI18";

  /** Dynamic attribute column 19. */
  public static final String ATTRI19 = "ATTRI19";

  /** Dynamic attribute column 20. */
  public static final String ATTRI20 = "ATTRI20";

  /** Column holding the document type of the content item. */
  public static final String TYPE_DOCUMENT = "TYPE";

  /** Column holding the translator of the content item. */
  public static final String TRANSLATOR = "TRANSLATOR";

  /** Column holding the language of the document. */
  public static final String DOC_LANG = "LANG";

  /** Column flagging that the row carries metadata only, without associated content. */
  public static final String NO_CONTENT = "NOCONTENT";

  /** Column holding the original language of the document (source for translations). */
  public static final String ORI_LANG = "ORILANG";

  /** Column holding the relation/link to the translated document. */
  public static final String REL_TRANS = "RELTRANS";

  /** Column flagging whether an existing content item should be overwritten. */
  public static final String OVERWRITE = "OVERWRITE";
}
