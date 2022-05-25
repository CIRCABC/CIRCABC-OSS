package io.swagger.api;

import io.swagger.model.*;
import org.alfresco.service.cmr.repository.NodeRef;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author beaurpi
 */
public interface ContentApi {

    List<Version> contentIdVersionsGet(String id, String language);

    List<Version> contentIdFirstVersionsGet(String id);

    Version contentIdVersionsVersionIdGet(String id, String versionId, String language);

    void contentIdDelete(String id);

    Translations contentIdTranslationsGet(String id);

    void contentIdPut(String id, Node body);

    List<Node> contentIdTopicsGet(String id);

    Node contentIdTopicsPost(String id, Node body);

    Node contentIdTranslationsPost(
            String id, String lang, InputStream file, String mimeType, String fileName);

    Node requestMachineTranslation(String id, String lang, boolean notify);

    void contentIdMultilingualAspectPost(String id, MultilingualAspectMetadata body);

    List<OfficeEditResult> checkEditInOffice(String[] nodeIds);

    PreviewResult checkPreview(String documentId);

    void buildZip(String[] nodeIds, OutputStream outputStream) throws IOException, XMLStreamException;

    NodeRef createDownloadableZip(String[] nodeIds, String parentId)
            throws IOException, XMLStreamException;

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
            Map<String, Object> dynProps);

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
            Map<String, Object> dynProps);
}
