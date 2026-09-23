package io.swagger.api;

import io.swagger.model.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLStreamException;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Business API for operations on CIRCABC content nodes (documents stored in the
 * Alfresco repository).
 *
 * <p>Implementations of this interface encapsulate the logic invoked by the REST
 * webscript endpoints under the {@code /content} resource. It covers the full
 * lifecycle of a content item, including:
 *
 * <ul>
 *   <li>reading version history for a document;</li>
 *   <li>updating document metadata and deleting documents;</li>
 *   <li>managing the discussion topics attached to a document;</li>
 *   <li>handling multilingual documents (translations, pivot language and
 *       machine translation requests);</li>
 *   <li>creating new content and content translations from an uploaded file;</li>
 *   <li>producing bulk ZIP archives of one or more content nodes.</li>
 * </ul>
 *
 * <p>Node identifiers are the string form of Alfresco {@link NodeRef} ids and are
 * resolved to {@code NodeRef}s by the implementation.
 *
 * @author beaurpi
 */
public interface ContentApi {
  /**
   * Returns the full version history of a versioned content node.
   *
   * @param id the identifier of the content node
   * @param language the requested language for localized properties (may be
   *     {@code null})
   * @return the list of {@link Version} entries for the node, or an empty list if
   *     the node is not versioned
   */
  List<Version> contentIdVersionsGet(String id, String language);

  /**
   * Returns the most recent versions of a versioned content node, starting from
   * the current version and walking back through its predecessors (capped to the
   * latest entries).
   *
   * @param id the identifier of the content node
   * @return the list of the most recent {@link Version} entries, or an empty list
   *     if the node is not versioned
   */
  List<Version> contentIdFirstVersionsGet(String id);

  /**
   * Returns a single version of a content node identified by its frozen state
   * node id.
   *
   * @param id the identifier of the (live) content node
   * @param versionId the frozen state node id identifying the requested version
   * @param language the requested language for localized properties (may be
   *     {@code null})
   * @return the matching {@link Version}, or {@code null} if the node is not
   *     versioned or no version matches {@code versionId}
   */
  Version contentIdVersionsVersionIdGet(
    String id,
    String versionId,
    String language
  );

  /**
   * Deletes a content node. For a multilingual pivot document, all of its
   * translations are deleted as well. The originating Interest Group root id is
   * recorded before deletion.
   *
   * @param id the identifier of the content node to delete
   */
  void contentIdDelete(String id);

  /**
   * Returns the translations of a multilingual content node together with its
   * pivot translation.
   *
   * @param id the identifier of the content node
   * @return the {@link Translations} of the node, including the pivot translation
   */
  Translations contentIdTranslationsGet(String id);

  /**
   * Updates the metadata of a content node (name, title, description, issue and
   * expiration dates, reference, status, security ranking, author, dynamic
   * attributes and, depending on the node, its URL or content encoding/mimetype).
   *
   * @param id the identifier of the content node to update
   * @param body the {@link Node} carrying the new metadata values
   */
  void contentIdPut(String id, Node body);

  /**
   * Returns the discussion topics associated with a content node.
   *
   * @param id the identifier of the content node
   * @return the list of topic {@link Node}s attached to the node's discussion, or
   *     an empty list if none exist
   */
  List<Node> contentIdTopicsGet(String id);

  /**
   * Creates a new discussion topic on a content node, creating the underlying
   * discussion forum if it does not yet exist.
   *
   * @param id the identifier of the content (or folder) node to attach the topic to
   * @param body the {@link Node} providing the topic name
   * @return the created topic {@link Node}
   */
  Node contentIdTopicsPost(String id, Node body);

  /**
   * Uploads a file as a new translation of an existing multilingual content node.
   *
   * @param id the identifier of the existing multilingual content node
   * @param lang the locale/language code of the translation
   * @param file the input stream of the translation file content
   * @param mimeType the MIME type of the uploaded file
   * @param fileName the file name to use for the created translation
   * @return {@code null} (the created translation is registered as a side effect)
   */
  Node contentIdTranslationsPost(
    String id,
    String lang,
    InputStream file,
    String mimeType,
    String fileName
  );

  /**
   * Requests a machine translation of a multilingual content node into the given
   * target language. A copy of the source document is created and submitted to the
   * translation service.
   *
   * @param id the identifier of the multilingual source content node
   * @param lang the target language code to translate into
   * @param notify whether to notify users when the translation completes
   * @return {@code null} (the translation is produced asynchronously as a side effect)
   */
  Node requestMachineTranslation(String id, String lang, boolean notify);

  /**
   * Makes a content node multilingual by applying the multilingual aspect and
   * setting the given pivot language and author on the translation container.
   *
   * @param id the identifier of the content node to make multilingual
   * @param body the {@link MultilingualAspectMetadata} providing the pivot language
   *     and author
   */
  void contentIdMultilingualAspectPost(
    String id,
    MultilingualAspectMetadata body
  );

  /**
   * Builds a ZIP archive containing the readable content nodes for the given ids
   * (together with a generated index file) and streams it to the supplied output
   * stream. Nodes for which the current user lacks read permission are skipped.
   *
   * @param nodeIds the identifiers of the nodes to include in the archive
   * @param outputStream the stream the ZIP archive is written to
   * @throws IOException if an I/O error occurs while building or streaming the archive
   * @throws XMLStreamException if an error occurs while generating the archive index
   */
  void buildZip(String[] nodeIds, OutputStream outputStream)
    throws IOException, XMLStreamException;

  /**
   * Builds a ZIP archive of the given nodes and stores it as a new content node in
   * the specified parent space, so it can subsequently be downloaded.
   *
   * @param nodeIds the identifiers of the nodes to include in the archive
   * @param parentId the identifier of the parent space where the ZIP is created
   * @return the {@link NodeRef} of the created ZIP content node
   * @throws IOException if an I/O error occurs while building or storing the archive
   * @throws XMLStreamException if an error occurs while generating the archive index
   */
  NodeRef createDownloadableZip(String[] nodeIds, String parentId)
    throws IOException, XMLStreamException;

  /**
   * Creates a new content node under the given parent from an uploaded file, setting
   * its metadata. Optionally makes the created node the pivot of a multilingual
   * document.
   *
   * @param parentId the identifier of the parent folder/space
   * @param name the desired file name (made unique within the parent)
   * @param title the localized title
   * @param description the localized description
   * @param author the author name
   * @param reference the document reference
   * @param securityRanking the security ranking value
   * @param status the document status value
   * @param keywords the keyword node ids to associate with the content
   * @param expiration the expiration date, or {@code null}
   * @param mimetype the MIME type of the uploaded file
   * @param file the input stream of the file content
   * @param isPivot whether the created node should become a multilingual pivot
   * @param lang the pivot language code (used when {@code isPivot} is true)
   * @param dynProps additional dynamic properties to set on the node
   * @return the {@link NodeRef} of the created content node
   */
  @SuppressWarnings("java:S107") // Parameters map to content metadata fields
  NodeRef createContent(
    String parentId,
    String name,
    I18nProperty title,
    I18nProperty description,
    String author,
    String reference,
    String securityRanking,
    String status,
    String[] keywords,
    Date expiration,
    String mimetype,
    InputStream file,
    Boolean isPivot,
    String lang,
    Map<String, Object> dynProps
  );

  /**
   * Creates a new translation of an existing multilingual content node from an
   * uploaded file, setting its metadata and registering it as a translation in the
   * given language.
   *
   * @param id the identifier of the existing multilingual content node
   * @param defaultName the desired file name (made unique within the parent)
   * @param title the localized title
   * @param description the localized description
   * @param author the author name
   * @param reference the document reference
   * @param securityRanking the security ranking value
   * @param statusProp the document status value
   * @param keywords the keyword node ids to associate with the content
   * @param expirationDate the expiration date, or {@code null}
   * @param mimeType the MIME type of the uploaded file
   * @param file the input stream of the file content
   * @param lang the language code of the translation
   * @param dynProps additional dynamic properties to set on the node
   * @return the {@link NodeRef} of the created translation content node
   */
  @SuppressWarnings("java:S107") // Parameters map to content metadata fields
  NodeRef createContentTranslation(
    String id,
    String defaultName,
    I18nProperty title,
    I18nProperty description,
    String author,
    String reference,
    String securityRanking,
    String statusProp,
    String[] keywords,
    Date expirationDate,
    String mimeType,
    InputStream file,
    String lang,
    Map<String, Object> dynProps
  );
}
