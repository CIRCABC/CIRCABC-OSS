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

import java.util.List;

/**
 * Represents a single row of an import/export index sheet used during CIRCABC bulk operations.
 *
 * <p>A record aggregates a collection of {@link IndexEntry} objects, each pairing a column header
 * name with a value. On top of that generic entry storage, this interface exposes typed accessors
 * for the well-known metadata columns (name, title, description, author, language, keywords, etc.)
 * as well as for arbitrary dynamic (attribute) columns identified by an index. Each record also
 * tracks the row number it originates from in the source sheet.
 *
 * <p>Implementations back these predefined accessors with the underlying {@link IndexEntry} list,
 * creating entries on demand when a value is set.
 */
public interface IndexRecord {
  /**
   * Adds an index entry (a header/value pair) to this record.
   *
   * @param indexEntry the entry to add to the record
   */
  void addIndexEntry(final IndexEntry indexEntry);

  /**
   * Returns all entries held by this record.
   *
   * @return the list of index entries backing this record
   */
  List<IndexEntry> getIndexEntries();

  /**
   * Looks up the entry associated with the given header name.
   *
   * @param headerName the header (column) name to search for
   * @return the matching entry, or {@code null} if no entry exists for that header
   */
  IndexEntry getEntry(final String headerName);

  /* Predefined Index accessor */

  /**
   * Returns the value of the name column.
   *
   * @return the record name, or an empty string if unset
   */
  String getName();

  /**
   * Sets the value of the name column.
   *
   * @param value the name to assign
   */
  void setName(final String value);

  /**
   * Returns the value of the title column.
   *
   * @return the record title, or an empty string if unset
   */
  String getTitle();

  /**
   * Sets the value of the title column.
   *
   * @param value the title to assign
   */
  void setTitle(final String value);

  /**
   * Returns the value of the description column.
   *
   * @return the record description, or an empty string if unset
   */
  String getDescription();

  /**
   * Sets the value of the description column.
   *
   * @param value the description to assign
   */
  void setDescription(final String value);

  /**
   * Returns the value of the document language column.
   *
   * @return the document language, or an empty string if unset
   */
  String getDocLang();

  /**
   * Sets the value of the document language column.
   *
   * @param value the document language to assign
   */
  void setDocLang(final String value);

  /**
   * Returns the value of the author column.
   *
   * @return the author, or an empty string if unset
   */
  String getAuthor();

  /**
   * Sets the value of the author column.
   *
   * @param value the author to assign
   */
  void setAuthor(final String value);

  /**
   * Returns the value of the keywords column.
   *
   * @return the keywords, or an empty string if unset
   */
  String getKeywords();

  /**
   * Sets the value of the keywords column.
   *
   * @param value the keywords to assign
   */
  void setKeywords(final String value);

  /**
   * Returns the value of the status column.
   *
   * @return the status, or an empty string if unset
   */
  String getStatus();

  /**
   * Sets the value of the status column.
   *
   * @param value the status to assign
   */
  void setStatus(final String value);

  /**
   * Returns the value of the issue date column.
   *
   * @return the issue date, or an empty string if unset
   */
  String getIssueDate();

  /**
   * Sets the value of the issue date column.
   *
   * @param value the issue date to assign
   */
  void setIssueDate(final String value);

  /**
   * Returns the value of the reference column.
   *
   * @return the reference, or an empty string if unset
   */
  String getReference();

  /**
   * Sets the value of the reference column.
   *
   * @param value the reference to assign
   */
  void setReference(final String value);

  /**
   * Returns the value of the expiration date column.
   *
   * @return the expiration date, or an empty string if unset
   */
  String getExpirationDate();

  /**
   * Sets the value of the expiration date column.
   *
   * @param value the expiration date to assign
   */
  void setExpirationDate(final String value);

  /**
   * Returns the value of the security ranking column.
   *
   * @return the security ranking, or an empty string if unset
   */
  String getSecurityRanking();

  /**
   * Sets the value of the security ranking column.
   *
   * @param value the security ranking to assign
   */
  void setSecurityRanking(final String value);

  /**
   * Returns the value of the document type column.
   *
   * @return the document type, or an empty string if unset
   */
  String getTypeDocument();

  /**
   * Sets the value of the document type column.
   *
   * @param value the document type to assign
   */
  void setTypeDocument(final String value);

  /**
   * Returns the value of the translator column.
   *
   * @return the translator, or an empty string if unset
   */
  String getTranslator();

  /**
   * Sets the value of the translator column.
   *
   * @param value the translator to assign
   */
  void setTranslator(final String value);

  /**
   * Returns the value of the index-record document language column.
   *
   * @return the index-record document language, or an empty string if unset
   */
  String getIndexRecordDocLang();

  /**
   * Sets the value of the index-record document language column.
   *
   * @param value the index-record document language to assign
   */
  void setIndexRecordDocLang(final String value);

  /**
   * Returns the value of the no-content column.
   *
   * @return the no-content flag value, or an empty string if unset
   */
  String getNoContent();

  /**
   * Sets the value of the no-content column.
   *
   * @param value the no-content flag value to assign
   */
  void setNoContent(final String value);

  /**
   * Returns the value of the original language column.
   *
   * @return the original language, or an empty string if unset
   */
  String getOriLang();

  /**
   * Sets the value of the original language column.
   *
   * @param value the original language to assign
   */
  void setOriLang(final String value);

  /**
   * Returns the value of the related translation column.
   *
   * @return the related translation, or an empty string if unset
   */
  String getRelTrans();

  /**
   * Sets the value of the related translation column.
   *
   * @param value the related translation to assign
   */
  void setRelTrans(final String value);

  /**
   * Returns the value of the overwrite column.
   *
   * @return the overwrite flag value, or an empty string if unset
   */
  String getOverwrite();

  /**
   * Sets the value of the overwrite column.
   *
   * @param value the overwrite flag value to assign
   */
  void setOverwrite(final String value);

  /**
   * Returns the row number this record originates from in the source index sheet.
   *
   * @return the source row number
   */
  int getRowNumber();

  /**
   * Sets the value of a dynamic (attribute) column identified by its index.
   *
   * @param index the index of the dynamic property column
   * @param value the value to assign
   */
  void setDynamicProperty(int index, final String value);

  /**
   * Returns the value of a dynamic (attribute) column identified by its index.
   *
   * @param index the index of the dynamic property column
   * @return the dynamic property value, or an empty string if unset
   */
  String getDynamicProperty(int index);

  /**
   * Returns a human-readable representation of this record, typically listing the row number and
   * the predefined column values.
   *
   * @return a string representation of the record
   */
  String toString();
}
