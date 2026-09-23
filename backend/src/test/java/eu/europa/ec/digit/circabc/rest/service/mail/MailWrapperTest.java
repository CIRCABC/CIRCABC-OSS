package eu.europa.ec.digit.circabc.rest.service.mail;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.TemplateService;
import org.junit.Before;
import org.junit.Test;

public class MailWrapperTest {

  private NodeService nodeService;
  private TemplateService templateService;
  private NodeRef templateRef;
  private MailWrapperImpl mailWrapper;

  @Before
  public void setUp() {
    nodeService = mock(NodeService.class);
    templateService = mock(TemplateService.class);
    templateRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "template-id"
    );

    when(
      nodeService.getProperty(templateRef, ContentModel.PROP_NAME)
    ).thenReturn("default.ftl");

    mailWrapper = new MailWrapperImpl(
      templateRef,
      MailTemplate.NOTIFY_DOC,
      templateService,
      nodeService
    );
  }

  @Test
  public void testGetName_returnsNodeName() {
    assertEquals("default.ftl", mailWrapper.getName());
  }

  @Test
  public void testGetMailTemplate_returnsConfiguredTemplate() {
    assertEquals(MailTemplate.NOTIFY_DOC, mailWrapper.getMailTemplate());
  }

  @Test
  public void testGetTemplateNodeRef_returnsPivotTemplate() {
    assertEquals(templateRef, mailWrapper.getTemplateNodeRef());
  }

  @Test
  public void testIsOriginalTemplate_whenNameMatchesDefault_returnsTrue() {
    assertEquals(true, mailWrapper.isOriginalTemplate());
  }

  @Test
  public void testIsOriginalTemplate_whenNameDiffers_returnsFalse() {
    NodeRef customRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "custom-id"
    );
    when(nodeService.getProperty(customRef, ContentModel.PROP_NAME)).thenReturn(
      "custom-template.ftl"
    );

    MailWrapperImpl custom = new MailWrapperImpl(
      customRef,
      MailTemplate.NOTIFY_DOC,
      templateService,
      nodeService
    );
    assertFalse(custom.isOriginalTemplate());
  }

  @Test
  public void testGetSubject_whenTitlePropertySet_processesTitle() {
    String title = "Subject: ${name}";
    when(
      nodeService.getProperty(templateRef, ContentModel.PROP_TITLE)
    ).thenReturn(title);
    when(
      templateService.processTemplateString(
        eq("freemarker"),
        eq(title),
        anyMap()
      )
    ).thenReturn("Subject: resolved");

    Map<String, Object> model = new HashMap<>();
    model.put("name", "test");

    String result = mailWrapper.getSubject(model);
    assertEquals("Subject: resolved", result);
  }

  @Test
  public void testGetSubject_whenTitlePropertyEmpty_usesDefaultSubject() {
    // Use a template with no params in its default subject to avoid I18NUtil NPE
    NodeRef ref2 = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ref2"
    );
    when(nodeService.getProperty(ref2, ContentModel.PROP_NAME)).thenReturn(
      "default.ftl"
    );
    when(nodeService.getProperty(ref2, ContentModel.PROP_TITLE)).thenReturn("");

    MailWrapperImpl wrapper = new MailWrapperImpl(
      ref2,
      MailTemplate.GROUP_REQUEST,
      templateService,
      nodeService
    );

    // getDefaultSubject() returns null (no bundle), which is passed to processTemplateString
    when(
      templateService.processTemplateString(eq("freemarker"), any(), anyMap())
    ).thenReturn("default subject");

    String result = wrapper.getSubject(null);
    assertEquals("default subject", result);
  }

  @Test
  public void testGetSubject_whenTitlePropertyNull_usesDefaultSubject() {
    NodeRef ref2 = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ref2"
    );
    when(nodeService.getProperty(ref2, ContentModel.PROP_NAME)).thenReturn(
      "default.ftl"
    );
    when(nodeService.getProperty(ref2, ContentModel.PROP_TITLE)).thenReturn(
      null
    );

    MailWrapperImpl wrapper = new MailWrapperImpl(
      ref2,
      MailTemplate.GROUP_REQUEST,
      templateService,
      nodeService
    );

    when(
      templateService.processTemplateString(eq("freemarker"), any(), anyMap())
    ).thenReturn("default subject");

    String result = wrapper.getSubject(new HashMap<>());
    assertEquals("default subject", result);
  }

  @Test
  public void testGetBody_delegatesToTemplateService() {
    String expectedBody = "<html>body</html>";
    when(
      templateService.processTemplate(
        eq("freemarker"),
        eq(templateRef.toString()),
        anyMap()
      )
    ).thenReturn(expectedBody);

    String result = mailWrapper.getBody(new HashMap<>());
    assertEquals(expectedBody, result);
  }

  @Test
  public void testGetBody_withNullModel_passesEmptyMap() {
    when(
      templateService.processTemplate(
        eq("freemarker"),
        eq(templateRef.toString()),
        eq(new HashMap<>())
      )
    ).thenReturn("body");

    String result = mailWrapper.getBody(null);
    assertEquals("body", result);
  }

  @Test
  public void testGetSubject_withLocale_usesTranslatedTemplate() {
    Locale french = Locale.FRENCH;
    NodeRef frenchRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "french-id"
    );

    Map<Locale, NodeRef> translations = new HashMap<>();
    translations.put(Locale.ENGLISH, templateRef);
    translations.put(french, frenchRef);

    when(
      nodeService.getProperty(templateRef, ContentModel.PROP_NAME)
    ).thenReturn("default.ftl");
    when(
      nodeService.getProperty(frenchRef, ContentModel.PROP_TITLE)
    ).thenReturn("Sujet FR");
    when(
      templateService.processTemplateString(
        eq("freemarker"),
        eq("Sujet FR"),
        anyMap()
      )
    ).thenReturn("Sujet FR processed");

    MailWrapperImpl multiLingual = new MailWrapperImpl(
      translations,
      MailTemplate.NOTIFY_DOC,
      templateService,
      nodeService,
      Locale.ENGLISH
    );

    String result = multiLingual.getSubject(new HashMap<>(), french);
    assertEquals("Sujet FR processed", result);
  }

  @Test
  public void testGetBody_withLocale_usesTranslatedTemplate() {
    Locale german = Locale.GERMAN;
    NodeRef germanRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "german-id"
    );

    Map<Locale, NodeRef> translations = new HashMap<>();
    translations.put(Locale.ENGLISH, templateRef);
    translations.put(german, germanRef);

    when(
      nodeService.getProperty(templateRef, ContentModel.PROP_NAME)
    ).thenReturn("default.ftl");
    when(
      templateService.processTemplate(
        eq("freemarker"),
        eq(germanRef.toString()),
        anyMap()
      )
    ).thenReturn("German body");

    MailWrapperImpl multiLingual = new MailWrapperImpl(
      translations,
      MailTemplate.NOTIFY_DOC,
      templateService,
      nodeService,
      Locale.ENGLISH
    );

    String result = multiLingual.getBody(new HashMap<>(), german);
    assertEquals("German body", result);
  }

  @Test
  public void testGetBody_withUnknownLocale_fallsToPivot() {
    when(
      templateService.processTemplate(
        eq("freemarker"),
        eq(templateRef.toString()),
        anyMap()
      )
    ).thenReturn("pivot body");

    String result = mailWrapper.getBody(new HashMap<>(), Locale.JAPANESE);
    assertEquals("pivot body", result);
  }

  @Test
  public void testEquals_sameInstance_returnsTrue() {
    assertTrue(mailWrapper.equals(mailWrapper));
  }

  @Test
  public void testEquals_null_returnsFalse() {
    assertFalse(mailWrapper.equals(null));
  }

  @Test
  public void testEquals_differentClass_returnsFalse() {
    assertFalse(mailWrapper.equals("not a MailWrapperImpl"));
  }

  @Test
  public void testEquals_equivalentInstances_returnsTrue() {
    MailWrapperImpl other = new MailWrapperImpl(
      templateRef,
      MailTemplate.NOTIFY_DOC,
      templateService,
      nodeService
    );
    assertTrue(mailWrapper.equals(other));
    assertEquals(mailWrapper.hashCode(), other.hashCode());
  }

  @Test
  public void testEquals_differentTemplate_returnsFalse() {
    NodeRef otherRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "other-id"
    );
    when(nodeService.getProperty(otherRef, ContentModel.PROP_NAME)).thenReturn(
      "default.ftl"
    );

    MailWrapperImpl other = new MailWrapperImpl(
      otherRef,
      MailTemplate.NOTIFY_POST,
      templateService,
      nodeService
    );
    assertFalse(mailWrapper.equals(other));
  }

  @Test
  public void testToString_containsNameAndTemplate() {
    String str = mailWrapper.toString();
    assertTrue(str.contains("default.ftl"));
    assertTrue(str.contains("NOTIFY_DOC"));
  }
}
