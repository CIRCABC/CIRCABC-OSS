package io.swagger.api;

import java.io.InputStream;

/**
 * Simple data holder describing a file attachment received or produced by the
 * REST layer.
 *
 * <p>It bundles together the metadata and the binary content of an uploaded or
 * streamed file so that it can be passed around as a single object. The
 * {@link #inputStream} carries the raw bytes while the remaining fields hold
 * the associated metadata (file name, size, MIME type and character encoding).
 */
public class FileAttachmentData {

  /** The file name of the attachment. */
  String name;
  /** The size of the attachment content, in bytes. */
  long size;
  /** The stream providing access to the raw binary content of the attachment. */
  InputStream inputStream;
  /** The MIME type (content type) of the attachment. */
  String mimetype;
  /** The character encoding of the attachment content. */
  String encoding;

  /**
   * Creates a new {@code FileAttachmentData} with the given metadata and
   * content stream.
   *
   * @param name the file name of the attachment
   * @param size the size of the attachment content, in bytes
   * @param inputStream the stream providing the raw binary content
   * @param mimetype the MIME type (content type) of the attachment
   * @param encoding the character encoding of the attachment content
   */
  public FileAttachmentData(
    String name,
    long size,
    InputStream inputStream,
    String mimetype,
    String encoding
  ) {
    super();
    this.name = name;
    this.size = size;
    this.inputStream = inputStream;
    this.mimetype = mimetype;
    this.encoding = encoding;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the size
   */
  public long getSize() {
    return size;
  }

  /**
   * @param size the size to set
   */
  public void setSize(long size) {
    this.size = size;
  }

  /**
   * @return the inputStream
   */
  public InputStream getInputStream() {
    return inputStream;
  }

  /**
   * @param inputStream the inputStream to set
   */
  public void setInputStream(InputStream inputStream) {
    this.inputStream = inputStream;
  }

  /**
   * @return the mimetype
   */
  public String getMimetype() {
    return mimetype;
  }

  /**
   * @param mimetype the mimetype to set
   */
  public void setMimetype(String mimetype) {
    this.mimetype = mimetype;
  }

  /**
   * @return the encoding
   */
  public String getEncoding() {
    return encoding;
  }

  /**
   * @param encoding the encoding to set
   */
  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }
}
