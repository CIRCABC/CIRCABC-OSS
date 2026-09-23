package eu.europa.ec.digit.circabc.rest.service.mail;

import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.extensions.surf.util.I18NUtil;

public class MailTemplateTest {

  @Test
  public void testGetTemplateDirectoryName_returnsCorrectValue() {
    assertEquals(
      "notifyDoc",
      MailTemplate.NOTIFY_DOC.getTemplateDirectoryName()
    );
    assertEquals(
      "inviteUser",
      MailTemplate.INVITE_USER.getTemplateDirectoryName()
    );
    assertEquals(
      "autoUploadSuccess",
      MailTemplate.AUTO_UPLOAD_SUCCESS.getTemplateDirectoryName()
    );
  }

  @Test
  public void testGetDefaultTemplateName_returnsDefaultFtl() {
    assertEquals(
      "default.ftl",
      MailTemplate.NOTIFY_DOC.getDefaultTemplateName()
    );
    assertEquals(
      "default.ftl",
      MailTemplate.INVITE_USER.getDefaultTemplateName()
    );
  }

  @Test
  public void testGetDefaultTemplateName_returnsCustomFtl() {
    assertEquals(
      "success.ftl",
      MailTemplate.AUTO_UPLOAD_SUCCESS.getDefaultTemplateName()
    );
    assertEquals(
      "error.ftl",
      MailTemplate.AUTO_UPLOAD_ERROR.getDefaultTemplateName()
    );
    assertEquals(
      "ftp-problem.ftl",
      MailTemplate.AUTO_UPLOAD_FTP_PROBLEM.getDefaultTemplateName()
    );
    assertEquals(
      "refusal.ftl",
      MailTemplate.CATEGORY_GROUP_REQUEST_REFUSE.getDefaultTemplateName()
    );
    assertEquals(
      "acceptation.ftl",
      MailTemplate.CATEGORY_GROUP_REQUEST_ACCEPT.getDefaultTemplateName()
    );
  }

  @Test
  public void testMultipleAllowed_returnsTrueForMarkedTemplates() {
    assertTrue(MailTemplate.INVITE_USER.multipleAllowed());
    assertTrue(MailTemplate.UPDATE_USER_PROFILE.multipleAllowed());
    assertTrue(MailTemplate.SHARE_SPACE_NOTIFICATION.multipleAllowed());
  }

  @Test
  public void testMultipleAllowed_returnsFalseForDefaultTemplates() {
    assertFalse(MailTemplate.NOTIFY_NEWS.multipleAllowed());
    assertFalse(MailTemplate.NOTIFY_DOC.multipleAllowed());
    assertFalse(MailTemplate.REJECT_POST.multipleAllowed());
  }

  @Test
  public void testGetMailTemplateForFolderName_findsExistingTemplate() {
    assertEquals(
      MailTemplate.NOTIFY_NEWS,
      MailTemplate.getMailTemplateForFolderName("notifyNews")
    );
    assertEquals(
      MailTemplate.INVITE_USER,
      MailTemplate.getMailTemplateForFolderName("inviteUser")
    );
    assertEquals(
      MailTemplate.NOTIFY_DOC,
      MailTemplate.getMailTemplateForFolderName("notifyDoc")
    );
  }

  @Test
  public void testGetMailTemplateForFolderName_returnsNullForUnknownFolder() {
    assertNull(MailTemplate.getMailTemplateForFolderName("nonExistentFolder"));
  }

  @Test
  public void testGetMailTemplateForFolderName_returnsNullForNull() {
    assertNull(MailTemplate.getMailTemplateForFolderName(null));
  }

  @Test
  public void testGetMailTemplateForFolderName_returnsFirstMatchForDuplicateDir() {
    // "categoryGroupDeleteRequest" is used by multiple enum values; first match wins
    MailTemplate result = MailTemplate.getMailTemplateForFolderName(
      "categoryGroupDeleteRequest"
    );
    assertNotNull(result);
    assertEquals(
      "categoryGroupDeleteRequest",
      result.getTemplateDirectoryName()
    );
  }

  @Test
  public void testTranslate_withNoParams_returnsNull_whenKeyNotFound() {
    // I18NUtil.getMessage returns null for unknown keys when no bundle is registered
    String result = MailTemplate.translate("nonexistent_key_xyz");
    assertNull(result);
  }

  @Test
  public void testTranslate_withEmptyParams_usesNoParamsBranch() {
    // Empty params array (length < 1) triggers the no-params branch
    String result = MailTemplate.translate(
      "nonexistent_key_xyz",
      new Object[0]
    );
    assertNull(result);
  }

  @Test
  public void testKeyConstants_areNotNull() {
    assertNotNull(MailTemplate.KEY_ME);
    assertNotNull(MailTemplate.KEY_PERSON);
    assertNotNull(MailTemplate.KEY_DATE);
    assertNotNull(MailTemplate.KEY_LOCATION);
    assertNotNull(MailTemplate.KEY_DOCUMENT);
    assertNotNull(MailTemplate.KEY_CIRCABC);
    assertNotNull(MailTemplate.KEY_INTEREST_GROUP);
    assertNotNull(MailTemplate.KEY_APPOINTMENT);
  }

  @Test
  public void testAllEnumValues_haveNonNullDirectoryName() {
    for (MailTemplate template : MailTemplate.values()) {
      assertNotNull(
        "Template " + template.name() + " has null directory name",
        template.getTemplateDirectoryName()
      );
    }
  }

  @Test
  public void testAllEnumValues_haveNonNullDefaultTemplateName() {
    for (MailTemplate template : MailTemplate.values()) {
      assertNotNull(
        "Template " + template.name() + " has null default template name",
        template.getDefaultTemplateName()
      );
    }
  }
}
