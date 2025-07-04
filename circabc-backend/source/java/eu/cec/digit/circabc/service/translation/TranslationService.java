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
package eu.cec.digit.circabc.service.translation;

import eu.cec.digit.circabc.util.CircabcUserDataBean;
import java.util.Set;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

public interface TranslationService {
  Set<String> getAvailableLanguages();

  void translateProperty(
    NodeRef nodeRef,
    QName property,
    String sourceLanguage,
    Set<String> languages,
    boolean notifyUserByEmail
  );

  NodeRef copyDocumentToBeTranslated(NodeRef nodeRef);

  void translateDocument(
    NodeRef originalDocument,
    NodeRef copyOfDocument,
    String sourceLanguage,
    Set<String> languages,
    boolean notifyUserByEmail
  );

  CircabcUserDataBean getMTUserDetails();

  void processTranslatedFiles();

  boolean canBeTranslated(String filename);

  long fileMaxSize();

  Set<String> getAvailableFileExtensions();

  void cleanTempSpace(int year, int month);
}
