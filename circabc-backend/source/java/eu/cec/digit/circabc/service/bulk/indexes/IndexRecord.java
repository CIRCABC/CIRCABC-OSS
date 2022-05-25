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

import java.util.List;

public interface IndexRecord {

    void addIndexEntry(final IndexEntry indexEntry);

    List<IndexEntry> getIndexEntries();

    IndexEntry getEntry(final String headerName);

    /* Predefined Index accessor */

    String getName();

    void setName(final String value);

    String getTitle();

    void setTitle(final String value);

    String getDescription();

    void setDescription(final String value);

    String getDocLang();

    void setDocLang(final String value);

    String getAuthor();

    void setAuthor(final String value);

    String getKeywords();

    void setKeywords(final String value);

    String getStatus();

    void setStatus(final String value);

    String getIssueDate();

    void setIssueDate(final String value);

    String getReference();

    void setReference(final String value);

    String getExpirationDate();

    void setExpirationDate(final String value);

    String getSecurityRanking();

    void setSecurityRanking(final String value);

    String getAttri1();

    void setAttri1(final String value);

    String getAttri2();

    void setAttri2(final String value);

    String getAttri3();

    void setAttri3(final String value);

    String getAttri4();

    void setAttri4(final String value);

    String getAttri5();

    void setAttri5(final String value);

    String getAttri6();

    void setAttri6(final String value);

    String getAttri7();

    void setAttri7(final String value);

    String getAttri8();

    void setAttri8(final String value);

    String getAttri9();

    void setAttri9(final String value);

    String getAttri10();

    void setAttri10(final String value);

    String getAttri11();

    void setAttri11(final String value);

    String getAttri12();

    void setAttri12(final String value);

    String getAttri13();

    void setAttri13(final String value);

    String getAttri14();

    void setAttri14(final String value);

    String getAttri15();

    void setAttri15(final String value);

    String getAttri16();

    void setAttri16(final String value);

    String getAttri17();

    void setAttri17(final String value);

    String getAttri18();

    void setAttri18(final String value);

    String getAttri19();

    void setAttri19(final String value);

    String getAttri20();

    void setAttri20(final String value);

    String getTypeDocument();

    void setTypeDocument(final String value);

    String getTranslator();

    void setTranslator(final String value);

    String getIndexRecordDocLang();

    void setIndexRecordDocLang(final String value);

    String getNoContent();

    void setNoContent(final String value);

    String getOriLang();

    void setOriLang(final String value);

    String getRelTrans();

    void setRelTrans(final String value);

    String getOverwrite();

    void setOverwrite(final String value);

    int getRowNumber();

    void setDynamicProperty(int index, final String value);

    String getDynamicProperty(int index);

    String toString();
}
