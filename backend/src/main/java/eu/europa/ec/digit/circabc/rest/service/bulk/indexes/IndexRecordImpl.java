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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Default {@link IndexRecord} implementation representing a single row of a bulk-import index
 * (typically parsed from an index/manifest file such as a spreadsheet).
 *
 * <p>A record is a collection of {@link IndexEntry} objects, each pairing a column header name (see
 * {@link IndexHeaderColumn}) with its string value. The typed getters and setters exposed by this
 * class are convenience accessors that read from and write to those entries by header name, so the
 * underlying storage remains a generic, header-keyed list. Values that are absent are returned as
 * empty strings rather than {@code null}.
 *
 * <p>Besides the fixed, well-known columns, arbitrary dynamic properties are supported through
 * {@link #getDynamicProperty(int)} / {@link #setDynamicProperty(int, String)}, which key entries
 * using an indexed attribute-prefix header.
 */
public class IndexRecordImpl implements IndexRecord {

  /**
   * Ordered list of header columns rendered by {@link #toString()}. Used only for producing a
   * human-readable dump of the record; it does not affect the entries actually stored.
   */
  private static final String[] TO_STRING_HEADERS = {
    IndexHeaderColumn.NAME,
    IndexHeaderColumn.TITLE,
    IndexHeaderColumn.DESCRIPTION,
    IndexHeaderColumn.AUTHOR,
    IndexHeaderColumn.DOC_LANG,
    IndexHeaderColumn.KEYWORDS,
    IndexHeaderColumn.STATUS,
    IndexHeaderColumn.ISSUE_DATE,
    IndexHeaderColumn.REFERENCE,
    IndexHeaderColumn.EXPIRATION_DATE,
    IndexHeaderColumn.SECURITY_RANKING,
    IndexHeaderColumn.TRANSLATOR,
    IndexHeaderColumn.DOC_LANG,
    IndexHeaderColumn.NO_CONTENT,
    IndexHeaderColumn.ORI_LANG,
    IndexHeaderColumn.REL_TRANS,
    IndexHeaderColumn.OVERWRITE,
  };

  /** Header-keyed entries that make up this record, in insertion order. */
  private final List<IndexEntry> indexEntries = new ArrayList<>();

  /** 1-based (or source-defined) position of this record within the source index. */
  private int rowNumber;

  /**
   * Creates an empty record positioned at the given row.
   *
   * @param rowNumber the position of this record within the source index
   */
  public IndexRecordImpl(final int rowNumber) {
    this.rowNumber = rowNumber;
  }

  /**
   * Appends an entry to this record without checking for duplicate header names.
   *
   * @param indexEntry the entry to add
   */
  public void addIndexEntry(final IndexEntry indexEntry) {
    indexEntries.add(indexEntry);
  }

  /**
   * Returns the live list of entries backing this record.
   *
   * @return the modifiable list of entries
   */
  public List<IndexEntry> getIndexEntries() {
    return indexEntries;
  }

  /**
   * Finds the first entry matching the given header name.
   *
   * @param headerName the header name to look up
   * @return the matching entry, or {@code null} if no entry has that header name
   */
  public IndexEntry getEntry(final String headerName) {
    for (final IndexEntry indexEntry : indexEntries) {
      if (indexEntry.getHeaderName().equals(headerName)) {
        return indexEntry;
      }
    }
    return null;
  }

  /**
   * Returns the value of the entry with the given header name, or an empty string when the entry is
   * missing or holds a {@code null} value.
   *
   * @param headerName the header name to read
   * @return the entry value, never {@code null}
   */
  private String getGeneric(final String headerName) {
    final IndexEntry indexEntry = getEntry(headerName);
    String value = "";
    if (indexEntry != null) {
      value = indexEntry.getValue();
      if (value == null) {
        value = "";
      }
    }
    return value;
  }

  /**
   * Sets the value for the given header name, updating the existing entry if present or creating a
   * new one otherwise.
   *
   * @param headerName the header name to write
   * @param value the value to store
   */
  private void setGeneric(final String headerName, final String value) {
    IndexEntry indexEntry = getEntry(headerName);
    if (indexEntry != null) {
      indexEntry.setValue(value);
    } else {
      indexEntry = new IndexEntryImpl(headerName, value);
      addIndexEntry(indexEntry);
    }
  }

  /**
   * @return the target node name, or an empty string if unset
   */
  public String getName() {
    return getGeneric(IndexHeaderColumn.NAME);
  }

  /**
   * Sets the target name, normalizing path separators to the current platform's separator so the
   * value can be used as a file-system path regardless of the slash style used in the source index.
   *
   * @param value the name value, possibly containing {@code /} or {@code \} separators
   */
  public void setName(final String value) {
    String name =
      File.separatorChar == '/'
        ? value.replace('\\', File.separatorChar)
        : value.replace('/', File.separatorChar);
    setGeneric(IndexHeaderColumn.NAME, name);
  }

  /**
   * @return the title, or an empty string if unset
   */
  public String getTitle() {
    return getGeneric(IndexHeaderColumn.TITLE);
  }

  /**
   * @param value the title to set
   */
  public void setTitle(final String value) {
    setGeneric(IndexHeaderColumn.TITLE, value);
  }

  /**
   * @return the description, or an empty string if unset
   */
  public String getDescription() {
    return getGeneric(IndexHeaderColumn.DESCRIPTION);
  }

  /**
   * @param value the description to set
   */
  public void setDescription(final String value) {
    setGeneric(IndexHeaderColumn.DESCRIPTION, value);
  }

  /**
   * @return the author, or an empty string if unset
   */
  public String getAuthor() {
    return getGeneric(IndexHeaderColumn.AUTHOR);
  }

  /**
   * @param value the author to set
   */
  public void setAuthor(final String value) {
    setGeneric(IndexHeaderColumn.AUTHOR, value);
  }

  /**
   * @return the document language, or an empty string if unset
   */
  public String getDocLang() {
    return getGeneric(IndexHeaderColumn.DOC_LANG);
  }

  /**
   * @param value the document language to set
   */
  public void setDocLang(final String value) {
    setGeneric(IndexHeaderColumn.DOC_LANG, value);
  }

  /**
   * @return the keywords, or an empty string if unset
   */
  public String getKeywords() {
    return getGeneric(IndexHeaderColumn.KEYWORDS);
  }

  /**
   * @param value the keywords to set
   */
  public void setKeywords(final String value) {
    setGeneric(IndexHeaderColumn.KEYWORDS, value);
  }

  /**
   * @return the status, or an empty string if unset
   */
  public String getStatus() {
    return getGeneric(IndexHeaderColumn.STATUS);
  }

  /**
   * @param value the status to set
   */
  public void setStatus(final String value) {
    setGeneric(IndexHeaderColumn.STATUS, value);
  }

  /**
   * @return the issue date, or an empty string if unset
   */
  public String getIssueDate() {
    return getGeneric(IndexHeaderColumn.ISSUE_DATE);
  }

  /**
   * @param value the issue date to set
   */
  public void setIssueDate(final String value) {
    setGeneric(IndexHeaderColumn.ISSUE_DATE, value);
  }

  /**
   * @return the reference, or an empty string if unset
   */
  public String getReference() {
    return getGeneric(IndexHeaderColumn.REFERENCE);
  }

  /**
   * @param value the reference to set
   */
  public void setReference(final String value) {
    setGeneric(IndexHeaderColumn.REFERENCE, value);
  }

  /**
   * @return the expiration date, or an empty string if unset
   */
  public String getExpirationDate() {
    return getGeneric(IndexHeaderColumn.EXPIRATION_DATE);
  }

  /**
   * @param value the expiration date to set
   */
  public void setExpirationDate(final String value) {
    setGeneric(IndexHeaderColumn.EXPIRATION_DATE, value);
  }

  /**
   * @return the security ranking, or an empty string if unset
   */
  public String getSecurityRanking() {
    return getGeneric(IndexHeaderColumn.SECURITY_RANKING);
  }

  /**
   * @param value the security ranking to set
   */
  public void setSecurityRanking(final String value) {
    setGeneric(IndexHeaderColumn.SECURITY_RANKING, value);
  }

  /**
   * @return the document type, or an empty string if unset
   */
  public String getTypeDocument() {
    return getGeneric(IndexHeaderColumn.TYPE_DOCUMENT);
  }

  /**
   * @param value the document type to set
   */
  public void setTypeDocument(final String value) {
    setGeneric(IndexHeaderColumn.TYPE_DOCUMENT, value);
  }

  /**
   * @return the translator, or an empty string if unset
   */
  public String getTranslator() {
    return getGeneric(IndexHeaderColumn.TRANSLATOR);
  }

  /**
   * @param value the translator to set
   */
  public void setTranslator(final String value) {
    setGeneric(IndexHeaderColumn.TRANSLATOR, value);
  }

  /**
   * @return the index-record document language, or an empty string if unset
   */
  public String getIndexRecordDocLang() {
    return getGeneric(IndexHeaderColumn.DOC_LANG);
  }

  /**
   * @param value the index-record document language to set
   */
  public void setIndexRecordDocLang(final String value) {
    setGeneric(IndexHeaderColumn.DOC_LANG, value);
  }

  /**
   * @return the no-content flag value, or an empty string if unset
   */
  public String getNoContent() {
    return getGeneric(IndexHeaderColumn.NO_CONTENT);
  }

  /**
   * @param value the no-content flag value to set
   */
  public void setNoContent(final String value) {
    setGeneric(IndexHeaderColumn.NO_CONTENT, value);
  }

  /**
   * @return the original language, or an empty string if unset
   */
  public String getOriLang() {
    return getGeneric(IndexHeaderColumn.ORI_LANG);
  }

  /**
   * @param value the original language to set
   */
  public void setOriLang(final String value) {
    setGeneric(IndexHeaderColumn.ORI_LANG, value);
  }

  /**
   * @return the related-translation reference, or an empty string if unset
   */
  public String getRelTrans() {
    return getGeneric(IndexHeaderColumn.REL_TRANS);
  }

  /**
   * @param value the related-translation reference to set
   */
  public void setRelTrans(final String value) {
    setGeneric(IndexHeaderColumn.REL_TRANS, value);
  }

  /**
   * @return the overwrite flag value, or an empty string if unset
   */
  public String getOverwrite() {
    return getGeneric(IndexHeaderColumn.OVERWRITE);
  }

  /**
   * @param value the overwrite flag value to set
   */
  public void setOverwrite(final String value) {
    setGeneric(IndexHeaderColumn.OVERWRITE, value);
  }

  /**
   * @return the position of this record within the source index
   */
  public int getRowNumber() {
    return rowNumber;
  }

  /**
   * Reads an indexed dynamic (custom) property.
   *
   * @param index the numeric suffix identifying the dynamic property
   * @return the property value, or an empty string if unset
   */
  public String getDynamicProperty(int index) {
    return getGeneric(IndexHeaderColumn.ATTRIPREFIX + Integer.toString(index));
  }

  /**
   * Writes an indexed dynamic (custom) property.
   *
   * @param index the numeric suffix identifying the dynamic property
   * @param value the value to store
   */
  public void setDynamicProperty(int index, String value) {
    setGeneric(IndexHeaderColumn.ATTRIPREFIX + Integer.toString(index), value);
  }

  /**
   * Returns a human-readable, multi-line dump of the record showing its row number and the values
   * of the {@link #TO_STRING_HEADERS well-known columns}.
   *
   * @return a debug-oriented string representation of this record
   */
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Row Number=").append(getRowNumber()).append('\n');
    for (String header : TO_STRING_HEADERS) {
      sb.append(header).append('=').append(getGeneric(header)).append('\n');
    }
    return sb.toString();
  }
}
