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
package eu.cec.digit.circabc.repo.translation;

public class SearchResultNotify {

  private String requestID;
  private String targetLangs;
  private String targetPath;
  private String username;
  private String email;
  private String documentID;
  private String propertyQName;
  private int translationCount;

  public String getRequestID() {
    return requestID;
  }

  public void setRequestID(String requestID) {
    this.requestID = requestID;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getDocumentID() {
    return documentID;
  }

  public void setDocumentID(String documentID) {
    this.documentID = documentID;
  }

  public String getPropertyQName() {
    return propertyQName;
  }

  public void setPropertyQName(String propertyQName) {
    this.propertyQName = propertyQName;
  }

  public String getTargetPath() {
    return targetPath;
  }

  public void setTargetPath(String targetPath) {
    this.targetPath = targetPath;
  }

  public String getTargetLangs() {
    return targetLangs;
  }

  public void setTargetLangs(String targetLangs) {
    this.targetLangs = targetLangs;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public int getTranslationCount() {
    return translationCount;
  }

  public void setTranslationCount(int translationCount) {
    this.translationCount = translationCount;
  }
}
