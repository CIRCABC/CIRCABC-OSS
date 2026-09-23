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

/**
 * Represents a single entry within a compressed archive (such as a ZIP file).
 *
 * <p>Implementations expose the metadata describing one item contained in an archive, including its
 * name, uncompressed and compressed sizes, whether it is a directory, an optional comment, its CRC
 * checksum and its last-modification time. This abstraction decouples the compression handling code
 * from any concrete archive library.
 */
public interface CompressedEntry {
  /**
   * Returns the name (path) of this entry within the archive.
   *
   * @return the entry file name, including any relative path segments
   */
  String getFileName();

  /**
   * Returns the uncompressed size of this entry.
   *
   * @return the size, in bytes, of the entry before compression
   */
  long getFileSize();

  /**
   * Indicates whether this entry represents a directory.
   *
   * @return {@code true} if the entry is a directory, {@code false} if it is a file
   */
  boolean isDirectory();

  /**
   * Returns the optional comment associated with this entry.
   *
   * @return the entry comment, or {@code null} if none was set
   */
  String getComment();

  /**
   * Returns the compressed size of this entry.
   *
   * @return the size, in bytes, of the entry as stored (compressed) in the archive
   */
  long getFileCompressedSize();

  /**
   * Returns the CRC-32 checksum of the entry's uncompressed data.
   *
   * @return the CRC-32 checksum value
   */
  long getCrc();

  /**
   * Returns the last-modification time of this entry.
   *
   * @return the modification time, in milliseconds since the epoch
   */
  long getTime();
}
