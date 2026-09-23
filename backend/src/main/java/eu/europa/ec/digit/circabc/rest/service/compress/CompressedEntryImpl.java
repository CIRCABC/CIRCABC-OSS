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
package eu.europa.ec.digit.circabc.rest.service.compress;

import de.schlichtherle.util.zip.ZipEntry;

/**
 * Default {@link CompressedEntry} implementation that adapts a TrueZIP {@link ZipEntry} into the
 * CIRCABC domain model.
 *
 * <p>An instance captures an immutable snapshot of the metadata of a single entry (file or
 * directory) contained in a compressed archive at construction time, so callers can inspect the
 * archive contents without holding on to the underlying {@link ZipEntry}.
 */
public class CompressedEntryImpl implements CompressedEntry {

  /** Name (path) of the entry within the archive. */
  private String fileName;

  /** Uncompressed size of the entry in bytes. */
  private long fileSize;

  /** Compressed size of the entry in bytes. */
  private long fileCompressedSize;

  /** Optional comment associated with the entry, or {@code null} if none. */
  private String comment;

  /** {@code true} if the entry denotes a directory rather than a file. */
  private boolean isDirectory;

  /** CRC-32 checksum of the uncompressed entry data. */
  private long crc;

  /** Last modification time of the entry, in milliseconds since the epoch. */
  private long time;

  /**
   * Builds a compressed entry snapshot from the given ZIP entry.
   *
   * @param e the source ZIP entry whose metadata (name, sizes, comment, directory flag, CRC and
   *     modification time) is copied into this instance
   */
  public CompressedEntryImpl(final ZipEntry e) {
    fileName = e.getName();
    fileSize = e.getSize();
    fileCompressedSize = e.getCompressedSize();
    isDirectory = e.isDirectory();
    comment = e.getComment();
    crc = e.getCrc();
    time = e.getTime();
  }

  /**
   * Returns the name (path) of the entry within the archive.
   *
   * @return the entry name
   */
  public String getFileName() {
    return fileName;
  }

  /**
   * Returns the uncompressed size of the entry.
   *
   * @return the uncompressed size in bytes
   */
  public long getFileSize() {
    return fileSize;
  }

  /**
   * Indicates whether this entry represents a directory.
   *
   * @return {@code true} if the entry is a directory, {@code false} otherwise
   */
  public boolean isDirectory() {
    return isDirectory;
  }

  /**
   * Returns the optional comment associated with the entry.
   *
   * @return the entry comment, or {@code null} if none was set
   */
  public String getComment() {
    return comment;
  }

  /**
   * Returns the compressed size of the entry.
   *
   * @return the compressed size in bytes
   */
  public long getFileCompressedSize() {
    return fileCompressedSize;
  }

  /**
   * Returns the CRC-32 checksum of the uncompressed entry data.
   *
   * @return the CRC-32 checksum
   */
  public long getCrc() {
    return crc;
  }

  /**
   * Returns the last modification time of the entry.
   *
   * @return the modification time in milliseconds since the epoch
   */
  public long getTime() {
    return time;
  }

  /**
   * Returns a string representation of this entry, namely its file name.
   *
   * @return the entry file name
   */
  @Override
  public String toString() {
    return fileName;
  }
}
