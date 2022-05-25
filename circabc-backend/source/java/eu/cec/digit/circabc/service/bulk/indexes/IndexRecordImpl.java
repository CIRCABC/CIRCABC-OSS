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
package eu.cec.digit.circabc.service.bulk.indexes;

import eu.cec.digit.circabc.service.bulk.indexes.IndexService.Headers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class IndexRecordImpl implements IndexRecord {

    private static final String EQUALS_SIGN = "=";
    private final List<IndexEntry> indexEntries = new ArrayList<>();
    private int rowNumber;

    public IndexRecordImpl(final int rowNumber) {
        this.rowNumber = rowNumber;
    }

    public void addIndexEntry(final IndexEntry indexEntry) {
        indexEntries.add(indexEntry);
    }

    public List<IndexEntry> getIndexEntries() {
        return indexEntries;
    }

    public IndexEntry getEntry(final String headerName) {
        for (final IndexEntry indexEntry : indexEntries) {
            if (indexEntry.getHeaderName().equals(headerName)) {
                return indexEntry;
            }
        }
        return null;
    }

    /* Predefined Index accessor */

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

    private void setGeneric(final String headerName, final String value) {
        IndexEntry indexEntry = getEntry(headerName);
        if (indexEntry != null) {
            indexEntry.setValue(value);
        } else {
            indexEntry = new IndexEntryImpl(headerName, value);
            addIndexEntry(indexEntry);
        }
    }

    public String getName() {
        return getGeneric(Headers.NAME);
    }

    public void setName(final String value) {
        String name;
        if (File.separatorChar == '/') {
            name = value.replace('\\', File.separatorChar);
        } else {
            name = value.replace('/', File.separatorChar);
        }
        setGeneric(Headers.NAME, name);
    }

    public String getTitle() {
        return getGeneric(Headers.TITLE);
    }

    public void setTitle(final String value) {
        setGeneric(Headers.TITLE, value);
    }

    public String getDescription() {
        return getGeneric(Headers.DESCRIPTION);
    }

    public void setDescription(final String value) {
        setGeneric(Headers.DESCRIPTION, value);
    }

    public String getAuthor() {
        return getGeneric(Headers.AUTHOR);
    }

    public void setAuthor(final String value) {
        setGeneric(Headers.AUTHOR, value);
    }

    public String getDocLang() {
        return getGeneric(Headers.DOC_LANG);
    }

    public void setDocLang(final String value) {
        setGeneric(Headers.DOC_LANG, value);
    }

    public String getKeywords() {
        return getGeneric(Headers.KEYWORDS);
    }

    public void setKeywords(final String value) {
        setGeneric(Headers.KEYWORDS, value);
    }

    public String getStatus() {
        return getGeneric(Headers.STATUS);
    }

    public void setStatus(final String value) {
        setGeneric(Headers.STATUS, value);
    }

    public String getIssueDate() {
        return getGeneric(Headers.ISSUE_DATE);
    }

    public void setIssueDate(final String value) {
        setGeneric(Headers.ISSUE_DATE, value);
    }

    public String getReference() {
        return getGeneric(Headers.REFERENCE);
    }

    public void setReference(final String value) {
        setGeneric(Headers.REFERENCE, value);
    }

    public String getExpirationDate() {
        return getGeneric(Headers.EXPIRATION_DATE);
    }

    public void setExpirationDate(final String value) {
        setGeneric(Headers.EXPIRATION_DATE, value);
    }

    public String getSecurityRanking() {
        return getGeneric(Headers.SECURITY_RANKING);
    }

    public void setSecurityRanking(final String value) {
        setGeneric(Headers.SECURITY_RANKING, value);
    }

  /*
  public String getAttri1() {
  	return getGeneric(Headers.ATTRI1);
  }

  public void setAttri1(final String value) {
  	setGeneric(Headers.ATTRI1, value);
  }

  public String getAttri2() {
  	return getGeneric(Headers.ATTRI2);
  }

  public void setAttri2(final String value) {
  	setGeneric(Headers.ATTRI2, value);
  }

  public String getAttri3() {
  	return getGeneric(Headers.ATTRI3);
  }

  public void setAttri3(final String value) {
  	setGeneric(Headers.ATTRI3, value);
  }

  public String getAttri4() {
  	return getGeneric(Headers.ATTRI4);
  }

  public void setAttri4(final String value) {
  	setGeneric(Headers.ATTRI4, value);
  }

  public String getAttri5() {
  	return getGeneric(Headers.ATTRI5);
  }

  public void setAttri5(final String value) {
  	setGeneric(Headers.ATTRI5, value);
  }


  public String getAttri6() {
  	return getGeneric(Headers.ATTRI6);
  }

  public void setAttri6(final String value) {
  	setGeneric(Headers.ATTRI6, value);
  }

  public String getAttri7() {
  	return getGeneric(Headers.ATTRI7);
  }

  public void setAttri7(final String value) {
  	setGeneric(Headers.ATTRI7, value);
  }

  public String getAttri8() {
  	return getGeneric(Headers.ATTRI8);
  }

  public void setAttri8(final String value) {
  	setGeneric(Headers.ATTRI8, value);
  }

  public String getAttri9() {
  	return getGeneric(Headers.ATTRI9);
  }

  public void setAttri9(final String value) {
  	setGeneric(Headers.ATTRI9, value);
  }

  public String getAttri10() {
  	return getGeneric(Headers.ATTRI10);
  }

  public void setAttri10(final String value) {
  	setGeneric(Headers.ATTRI10, value);
  }

  public String getAttri11() {
  	return getGeneric(Headers.ATTRI11);
  }

  public void setAttri11(final String value) {
  	setGeneric(Headers.ATTRI11, value);
  }

  public String getAttri12() {
  	return getGeneric(Headers.ATTRI12);
  }

  public void setAttri12(final String value) {
  	setGeneric(Headers.ATTRI12, value);
  }

  public String getAttri13() {
  	return getGeneric(Headers.ATTRI13);
  }

  public void setAttri13(final String value) {
  	setGeneric(Headers.ATTRI13, value);
  }

  public String getAttri14() {
  	return getGeneric(Headers.ATTRI14);
  }

  public void setAttri14(final String value) {
  	setGeneric(Headers.ATTRI14, value);
  }

  public String getAttri15() {
  	return getGeneric(Headers.ATTRI15);
  }

  public void setAttri15(final String value) {
  	setGeneric(Headers.ATTRI15, value);
  }


  public String getAttri16() {
  	return getGeneric(Headers.ATTRI16);
  }

  public void setAttri16(final String value) {
  	setGeneric(Headers.ATTRI16, value);
  }

  public String getAttri17() {
  	return getGeneric(Headers.ATTRI17);
  }

  public void setAttri17(final String value) {
  	setGeneric(Headers.ATTRI17, value);
  }

  public String getAttri18() {
  	return getGeneric(Headers.ATTRI18);
  }

  public void setAttri18(final String value) {
  	setGeneric(Headers.ATTRI18, value);
  }

  public String getAttri19() {
  	return getGeneric(Headers.ATTRI19);
  }

  public void setAttri19(final String value) {
  	setGeneric(Headers.ATTRI19, value);
  }

  public String getAttri20() {
  	return getGeneric(Headers.ATTRI20);
  }

  public void setAttri20(final String value) {
  	setGeneric(Headers.ATTRI20, value);
  }
  */

    public String getTypeDocument() {
        return getGeneric(Headers.TYPE_DOCUMENT);
    }

    public void setTypeDocument(final String value) {
        setGeneric(Headers.TYPE_DOCUMENT, value);
    }

    public String getTranslator() {
        return getGeneric(Headers.TRANSLATOR);
    }

    public void setTranslator(final String value) {
        setGeneric(Headers.TRANSLATOR, value);
    }

    public String getIndexRecordDocLang() {
        return getGeneric(Headers.DOC_LANG);
    }

    public void setIndexRecordDocLang(final String value) {
        setGeneric(Headers.DOC_LANG, value);
    }

    public String getNoContent() {
        return getGeneric(Headers.NO_CONTENT);
    }

    public void setNoContent(final String value) {
        setGeneric(Headers.NO_CONTENT, value);
    }

    public String getOriLang() {
        return getGeneric(Headers.ORI_LANG);
    }

    public void setOriLang(final String value) {
        setGeneric(Headers.ORI_LANG, value);
    }

    public String getRelTrans() {
        return getGeneric(Headers.REL_TRANS);
    }

    public void setRelTrans(final String value) {
        setGeneric(Headers.REL_TRANS, value);
    }

    public String getOverwrite() {
        return getGeneric(Headers.OVERWRITE);
    }

    public void setOverwrite(final String value) {
        setGeneric(Headers.OVERWRITE, value);
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public String toString() {
        String sb =
                "Row Number"
                        + EQUALS_SIGN
                        + getRowNumber()
                        + "\n"
                        + Headers.NAME
                        + EQUALS_SIGN
                        + getGeneric(Headers.NAME)
                        + "\n"
                        + Headers.TITLE
                        + EQUALS_SIGN
                        + getGeneric(Headers.TITLE)
                        + "\n"
                        + Headers.DESCRIPTION
                        + EQUALS_SIGN
                        + getGeneric(Headers.DESCRIPTION)
                        + "\n"
                        + Headers.AUTHOR
                        + EQUALS_SIGN
                        + getGeneric(Headers.AUTHOR)
                        + "\n"
                        + Headers.DOC_LANG
                        + EQUALS_SIGN
                        + getGeneric(Headers.DOC_LANG)
                        + "\n"
                        + Headers.KEYWORDS
                        + EQUALS_SIGN
                        + getGeneric(Headers.KEYWORDS)
                        + "\n"
                        + Headers.KEYWORDS
                        + EQUALS_SIGN
                        + getGeneric(Headers.KEYWORDS)
                        + "\n"
                        + Headers.STATUS
                        + EQUALS_SIGN
                        + getGeneric(Headers.STATUS)
                        + "\n"
                        + Headers.ISSUE_DATE
                        + EQUALS_SIGN
                        + getGeneric(Headers.ISSUE_DATE)
                        + "\n"
                        + Headers.REFERENCE
                        + EQUALS_SIGN
                        + getGeneric(Headers.REFERENCE)
                        + "\n"
                        + Headers.EXPIRATION_DATE
                        + EQUALS_SIGN
                        + getGeneric(Headers.EXPIRATION_DATE)
                        + "\n"
                        + Headers.SECURITY_RANKING
                        + EQUALS_SIGN
                        + getGeneric(Headers.SECURITY_RANKING)
                        + "\n"
                        + Headers.TRANSLATOR
                        + EQUALS_SIGN
                        + getGeneric(Headers.TRANSLATOR)
                        + "\n"
                        + Headers.DOC_LANG
                        + EQUALS_SIGN
                        + getGeneric(Headers.DOC_LANG)
                        + "\n"
                        + Headers.NO_CONTENT
                        + EQUALS_SIGN
                        + getGeneric(Headers.NO_CONTENT)
                        + "\n"
                        + Headers.ORI_LANG
                        + EQUALS_SIGN
                        + getGeneric(Headers.ORI_LANG)
                        + "\n"
                        + Headers.REL_TRANS
                        + EQUALS_SIGN
                        + getGeneric(Headers.REL_TRANS)
                        + "\n"
                        + Headers.OVERWRITE
                        + EQUALS_SIGN
                        + getGeneric(Headers.OVERWRITE)
                        + "\n";
        // Row Number
        // Name
        // Title
        // Description
        // Author
        // DocLang
        // Keywords
        // Keywords
        // Status
        // Issue Date
        // Reference
        // Expiration Date
        // Security Ranking
    /*
    //Attr1
    sb.append(Headers.ATTRI1);
    sb.append(EQUALS_SIGN);
    sb.append(getGeneric(Headers.ATTRI1));
    sb.append("\n");
    //Attr2
    sb.append(Headers.ATTRI2);
    sb.append(EQUALS_SIGN);
    sb.append(getGeneric(Headers.ATTRI2));
    sb.append("\n");
    //Attr3
    sb.append(Headers.ATTRI3);
    sb.append(EQUALS_SIGN);
    sb.append(getGeneric(Headers.ATTRI3));
    sb.append("\n");
    //Attr4
    sb.append(Headers.ATTRI4);
    sb.append(EQUALS_SIGN);
    sb.append(getGeneric(Headers.ATTRI4));
    sb.append("\n");
    //Attr5
    sb.append(Headers.ATTRI5);
    sb.append(EQUALS_SIGN);
    sb.append(getGeneric(Headers.ATTRI5));
    sb.append("\n");
    */
        // Translator
        // Doc Lang
        // No Content
        // Ori Lang
        // Rel Trans
        // Overwrite
        return sb;
    }

    public String getDynamicProperty(int index) {
        return getGeneric(Headers.ATTRIPREFIX + Integer.toString(index));
    }

    public String getAttri1() {
        return getDynamicProperty(1);
    }

    public void setAttri1(String value) {
        setDynamicProperty(1, value);
    }

    public String getAttri2() {
        return getDynamicProperty(2);
    }

    public void setAttri2(String value) {
        setDynamicProperty(2, value);
    }

    public String getAttri3() {
        return getDynamicProperty(3);
    }

    public void setAttri3(String value) {
        setDynamicProperty(3, value);
    }

    public String getAttri4() {
        return getDynamicProperty(4);
    }

    public void setAttri4(String value) {
        setDynamicProperty(4, value);
    }

    public String getAttri5() {
        return getDynamicProperty(5);
    }

    public void setAttri5(String value) {
        setDynamicProperty(5, value);
    }

    public String getAttri6() {
        return getDynamicProperty(6);
    }

    public void setAttri6(String value) {
        setDynamicProperty(6, value);
    }

    public String getAttri7() {
        return getDynamicProperty(7);
    }

    public void setAttri7(String value) {
        setDynamicProperty(7, value);
    }

    public String getAttri8() {
        return getDynamicProperty(8);
    }

    public void setAttri8(String value) {
        setDynamicProperty(8, value);
    }

    public String getAttri9() {
        return getDynamicProperty(9);
    }

    public void setAttri9(String value) {
        setDynamicProperty(9, value);
    }

    public String getAttri10() {
        return getDynamicProperty(10);
    }

    public void setAttri10(String value) {
        setDynamicProperty(10, value);
    }

    public String getAttri11() {
        return getDynamicProperty(11);
    }

    public void setAttri11(String value) {
        setDynamicProperty(11, value);
    }

    public String getAttri12() {
        return getDynamicProperty(12);
    }

    public void setAttri12(String value) {
        setDynamicProperty(12, value);
    }

    public String getAttri13() {
        return getDynamicProperty(13);
    }

    public void setAttri13(String value) {
        setDynamicProperty(13, value);
    }

    public String getAttri14() {
        return getDynamicProperty(14);
    }

    public void setAttri14(String value) {
        setDynamicProperty(14, value);
    }

    public String getAttri15() {
        return getDynamicProperty(15);
    }

    public void setAttri15(String value) {
        setDynamicProperty(15, value);
    }

    public String getAttri16() {
        return getDynamicProperty(16);
    }

    public void setAttri16(String value) {
        setDynamicProperty(16, value);
    }

    public String getAttri17() {
        return getDynamicProperty(17);
    }

    public void setAttri17(String value) {
        setDynamicProperty(17, value);
    }

    public String getAttri18() {
        return getDynamicProperty(18);
    }

    public void setAttri18(String value) {
        setDynamicProperty(18, value);
    }

    public String getAttri19() {
        return getDynamicProperty(19);
    }

    public void setAttri19(String value) {
        setDynamicProperty(19, value);
    }

    public String getAttri20() {
        return getDynamicProperty(20);
    }

    public void setAttri20(String value) {
        setDynamicProperty(20, value);
    }

    public void setDynamicProperty(int index, String value) {
        setGeneric(Headers.ATTRIPREFIX + Integer.toString(index), value);
    }
}
