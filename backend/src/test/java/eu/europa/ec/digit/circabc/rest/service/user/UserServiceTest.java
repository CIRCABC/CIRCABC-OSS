package eu.europa.ec.digit.circabc.rest.service.user;

import static org.junit.Assert.*;

import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.junit.Test;

public class UserServiceTest {

  @Test
  public void testPrefContentFilterLanguage_isCorrectQName() {
    QName expected = QName.createQName(
      NamespaceService.APP_MODEL_1_0_URI,
      "content-filter-language"
    );
    assertEquals(expected, UserService.PREF_CONTENT_FILTER_LANGUAGE);
  }

  @Test
  public void testPrefInterfaceLanguage_isCorrectQName() {
    QName expected = QName.createQName(
      NamespaceService.APP_MODEL_1_0_URI,
      "interface-language"
    );
    assertEquals(expected, UserService.PREF_INTERFACE_LANGUAGE);
  }

  @Test
  public void testPrefSignature_isCorrectQName() {
    QName expected = QName.createQName(
      NamespaceService.APP_MODEL_1_0_URI,
      "signature"
    );
    assertEquals(expected, UserService.PREF_SIGNATURE);
  }
}
