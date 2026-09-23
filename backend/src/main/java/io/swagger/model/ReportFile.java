package io.swagger.model;

/**
 * Copyright 2006 European Community
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
 */
/**
 *
 */

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.surf.util.URLEncoder;

/**
 * Lightweight model representing a single file referenced by a report.
 *
 * <p>It wraps an Alfresco {@link FileInfo} together with a display name, a
 * pre-computed download URL pointing to the content node, and a human-readable
 * rendering of the file size (in kilobytes). Instances are typically built from
 * repository nodes and then serialized as part of report responses.
 *
 * @author beaurpi
 */
public class ReportFile {

  /** Display name of the file as it should appear in the report. */
  private String name;

  /** Alfresco file metadata backing this entry (content, size, node reference, etc.). */
  private FileInfo fileInfo;

  /** Ready-to-use URL for downloading the underlying content node. */
  private String downloadUrl;

  /** Human-readable file size in kilobytes (e.g. "12.34kbs"). */
  private String sizeAsString;

  /**
   * Creates a report file entry from the given Alfresco file metadata.
   *
   * <p>The constructor derives the human-readable size string from the file's
   * content data and builds the download URL for the associated content node.
   *
   * @param f    the Alfresco file metadata to wrap (its content data and node
   *             reference are used to compute the size and download URL)
   * @param name the display name to associate with this file
   */
  public ReportFile(FileInfo f, String name) {
    this.fileInfo = f;
    this.name = name;
    convertFileInfoSize();
    downloadUrl = getUrl(f);
  }

  /**
   * Computes the human-readable {@link #sizeAsString} from the wrapped
   * {@link FileInfo}. The size is converted from bytes to kilobytes, rounded
   * down to two decimal places, and suffixed with "kbs".
   */
  private void convertFileInfoSize() {
    BigDecimal size = new BigDecimal(
      fileInfo.getContentData().getSize() / 1024
    );

    this.sizeAsString = size.setScale(2, RoundingMode.DOWN).toString() + "kbs";
  }

  /** @return the name */
  public String getName() {
    return name;
  }

  /** @param name the name to set */
  public void setName(String name) {
    this.name = name;
  }

  /** @return the fileInfo */
  public FileInfo getFileInfo() {
    return fileInfo;
  }

  /** @param fileInfo the fileInfo to set */
  public void setFileInfo(FileInfo fileInfo) {
    this.fileInfo = fileInfo;
    convertFileInfoSize();
  }

  /** @return the downloadUrl */
  public String getDownloadUrl() {
    return downloadUrl;
  }

  /** @param downloadUrl the downloadUrl to set */
  public void setDownloadUrl(String downloadUrl) {
    this.downloadUrl = downloadUrl;
  }

  /**
   * Builds the download URL for the given file using its node reference and name.
   *
   * @param fileInfo the file whose content node URL should be generated
   * @return the browser download URL for the file's content
   */
  private String getUrl(FileInfo fileInfo) {
    return generateBrowserURL(fileInfo.getNodeRef(), fileInfo.getName());
  }

  /** URL segment indicating a direct content download. */
  protected static final String URL_DIRECT = "d";

  /** URL pattern used to build content download links, filled via {@link MessageFormat}. */
  private static final String BROWSER_URL =
    "/d/" + URL_DIRECT + "/{0}/{1}/{2}/{3}";

  /**
   * Generates a browser download URL for the given content node.
   *
   * @param ref  node reference of the content node to link to (cannot be null)
   * @param name file name to include in the generated URL (cannot be null)
   * @return the download URL for the specified content node
   */
  public static final String generateBrowserURL(NodeRef ref, String name) {
    return generateUrl(BROWSER_URL, ref, name);
  }

  /**
   * Helper to generate a URL to a content node for downloading content from the server.
   *
   * @param pattern The pattern to use for the URL
   * @param ref     NodeRef of the content node to generate URL for (cannot be null)
   * @param name    File name to return in the URL (cannot be null)
   *
   * @return URL to download the content from the specified node
   */
  protected static final String generateUrl(
    String pattern,
    NodeRef ref,
    String name
  ) {
    return MessageFormat.format(
      pattern,
      ref.getStoreRef().getProtocol(),
      ref.getStoreRef().getIdentifier(),
      ref.getId(),
      URLEncoder.encode(name)
    );
  }

  /** @return the sizeAsString */
  public String getSizeAsString() {
    return sizeAsString;
  }

  /** @param sizeAsString the sizeAsString to set */
  public void setSizeAsString(String sizeAsString) {
    this.sizeAsString = sizeAsString;
  }
}
