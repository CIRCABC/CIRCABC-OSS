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

package eu.europa.ec.digit.circabc.rest.service.translation;

import io.swagger.model.CircabcUserDataBean;
import java.util.Set;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Service contract for the CIRCABC machine translation (MT) integration.
 *
 * <p>This service orchestrates the translation of Alfresco content — both individual node
 * properties (e.g. multilingual text fields) and whole documents — by delegating the actual
 * translation work to the external DGT machine translation platform. It builds and dispatches
 * translation requests, persists them for later correlation with the asynchronous callbacks, and
 * handles the post-processing of the translated files once they are returned (including optional
 * user notification by email).
 *
 * <p>It also exposes helper operations that describe the MT capabilities available to callers, such
 * as the supported languages, the accepted file extensions and the maximum file size, as well as
 * housekeeping operations for the temporary storage used during translation.
 */
public interface TranslationService {
  /**
   * Returns the set of languages supported by the machine translation service.
   *
   * @return the set of available language codes (never {@code null})
   */
  Set<String> getAvailableLanguages();

  /**
   * Submits a single node property (a {@code String} or multilingual {@code MLText} value) to the
   * machine translation service.
   *
   * <p>A translation request is generated with a unique external reference, persisted in the
   * database (so the asynchronous MT callback can be correlated back to the originating node) and
   * dispatched to the MT platform.
   *
   * @param nodeRef the node whose property is to be translated
   * @param property the qualified name of the property holding the text to translate
   * @param sourceLanguage the language code of the source text
   * @param languages the set of target language codes to translate into
   * @param notifyUserByEmail when {@code true}, the requesting user is notified by email once the
   *     translation completes
   */
  void translateProperty(
    NodeRef nodeRef,
    QName property,
    String sourceLanguage,
    Set<String> languages,
    boolean notifyUserByEmail
  );

  /**
   * Creates a working copy of the given document within the machine translation temporary folder
   * structure, so that the original document is left untouched while the copy is submitted for
   * translation.
   *
   * @param nodeRef the original document node to be translated; must be of content type
   * @return the {@link NodeRef} of the newly created copy to be translated
   * @throws IllegalArgumentException if {@code nodeRef} is not a content node
   * @throws IllegalStateException if the machine translation root folder does not exist
   */
  NodeRef copyDocumentToBeTranslated(NodeRef nodeRef);

  /**
   * Submits a whole document for machine translation.
   *
   * <p>A translation request referencing the original document is generated with a unique external
   * reference, persisted in the database and dispatched to the MT platform. The previously created
   * copy is used as the source content that is actually sent for translation.
   *
   * @param originalDocument the original document node (used to link the request back to its source)
   * @param copyOfDocument the working copy of the document that is sent for translation
   * @param sourceLanguage the language code of the source document
   * @param languages the set of target language codes to translate into
   * @param notifyUserByEmail when {@code true}, the requesting user is notified by email once the
   *     translation completes
   */
  void translateDocument(
    NodeRef originalDocument,
    NodeRef copyOfDocument,
    String sourceLanguage,
    Set<String> languages,
    boolean notifyUserByEmail
  );

  /**
   * Builds the pseudo-user data bean representing the machine translation system account, populated
   * with the configured MT credentials and fixed display details. This account is used as the
   * identity for content produced by the translation process.
   *
   * @return a {@link CircabcUserDataBean} describing the machine translation user
   */
  CircabcUserDataBean getMTUserDetails();

  /**
   * Processes translation requests whose results have been returned by the MT platform.
   *
   * <p>Typically invoked by a scheduled job, this method imports the finished translations back into
   * the repository and triggers any pending user notifications for requests completed since the
   * previous run.
   */
  void processTranslatedFiles();

  /**
   * Indicates whether a file can be submitted for translation based on its extension.
   *
   * @param filename the name of the file, including its extension
   * @return {@code true} if the file extension is among the supported extensions, {@code false}
   *     otherwise
   */
  boolean canBeTranslated(String filename);

  /**
   * Returns the maximum size, in bytes, allowed for a file submitted for translation.
   *
   * @return the maximum file size in bytes
   */
  long fileMaxSize();

  /**
   * Returns the set of file extensions accepted by the machine translation service.
   *
   * @return the set of supported file extensions (never {@code null})
   */
  Set<String> getAvailableFileExtensions();

  /**
   * Deletes the temporary translation storage folder for the given year and month, removing the
   * transient files produced during translation.
   *
   * @param year the year of the temporary folder to clean
   * @param month the month of the temporary folder to clean
   */
  void cleanTempSpace(int year, int month);
}
