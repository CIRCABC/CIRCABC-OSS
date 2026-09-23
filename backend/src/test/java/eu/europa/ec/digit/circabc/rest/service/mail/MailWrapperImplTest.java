package eu.europa.ec.digit.circabc.rest.service.mail;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
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

public class MailWrapperImplTest {

  private NodeService nodeService;
  private TemplateService templateService;
  private MailTemplate mailTemplate;
  private NodeRef templateRef;
  private MailWrapperImpl mailWrapper;

  @Before
  public void setUp() {
    nodeService = mock(NodeService.class);
    templateService = mock(TemplateService.class);
    mailTemplate = MailTemplate.NOTIFY_DOC;
    templateRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "template-id"
    );

    when(
      nodeService.getProperty(templateRef, ContentModel.PROP_NAME)
    ).thenReturn("default.ftl");

    mailWrapper = new MailWrapperImpl(
      templateRef,
      mailTemplate,
      templateService,
      nodeService
    );
  }

  @Test
  public void testGetName_returnsNodeName() {
    assertEquals("default.ftl", mailWrapper.getName());
  }

  @Test
  public void testGetMailTemplate_returnsTemplate() {
    assertEquals(mailTemplate, mailWrapper.getMailTemplate());
  }

  @Test
  public void testGetBody_withNullModel_callsTemplateService() {
    when(
      templateService.processTemplate(
        eq("freemarker"),
        eq(templateRef.toString()),
        anyMap()
      )
    ).thenReturn("body content");

    String body = mailWrapper.getBody(null);

    assertEquals("body content", body);
    verify(templateService).processTemplate(
      eq("freemarker"),
      eq(templateRef.toString()),
      anyMap()
    );
  }

  @Test
  public void testGetBody_withModel_passesModelToTemplateService() {
    Map<String, Object> model = new HashMap<>();
    model.put("key", "value");

    when(
      templateService.processTemplate(
        "freemarker",
        templateRef.toString(),
        model
      )
    ).thenReturn("rendered body");

    String body = mailWrapper.getBody(model);

    assertEquals("rendered body", body);
  }

  @Test
  public void testGetSubject_whenTitlePropertyIsString_usesIt() {
    when(
      nodeService.getProperty(templateRef, ContentModel.PROP_TITLE)
    ).thenReturn("Subject: ${name}");
    when(
      templateService.processTemplateString(
        eq("freemarker"),
        eq("Subject: ${name}"),
        anyMap()
      )
    ).thenReturn("Subject: Doc1");

    String subject = mailWrapper.getSubject(null);

    assertEquals("Subject: Doc1", subject);
  }

  @Test
  public void testGetSubject_whenTitlePropertyIsEmpty_usesDefaultSubject() {
    // Use GROUP_REQUEST which has no params in its default subject supplier
    // to avoid NPE from MessageFormat.format(null, params)
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

    when(
      templateService.processTemplateString(eq("freemarker"), any(), anyMap())
    ).thenReturn("default subject rendered");

    String subject = wrapper.getSubject(null);

    assertEquals("default subject rendered", subject);
  }

  @Test
  public void testGetSubject_whenTitlePropertyIsNull_usesDefaultSubject() {
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
    ).thenReturn("fallback subject");

    String subject = wrapper.getSubject(null);

    assertEquals("fallback subject", subject);
  }

  @Test
  public void testGetTemplateNodeRef_returnsPivotTemplate() {
    assertEquals(templateRef, mailWrapper.getTemplateNodeRef());
  }

  @Test
  public void testIsOriginalTemplate_whenNameMatchesDefault_returnsTrue() {
    assertTrue(mailWrapper.isOriginalTemplate());
  }

  @Test
  public void testIsOriginalTemplate_whenNameDiffers_returnsFalse() {
    NodeRef customRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "custom-id"
    );
    when(nodeService.getProperty(customRef, ContentModel.PROP_NAME)).thenReturn(
      "custom.ftl"
    );

    MailWrapperImpl custom = new MailWrapperImpl(
      customRef,
      mailTemplate,
      templateService,
      nodeService
    );

    assertFalse(custom.isOriginalTemplate());
  }

  @Test
  public void testGetBody_withLocale_usesCorrectTranslation() {
    NodeRef frRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "fr-id"
    );
    Map<Locale, NodeRef> translations = new HashMap<>();
    translations.put(Locale.ENGLISH, templateRef);
    translations.put(Locale.FRENCH, frRef);

    when(
      nodeService.getProperty(templateRef, ContentModel.PROP_NAME)
    ).thenReturn("default.ftl");

    MailWrapperImpl multiWrapper = new MailWrapperImpl(
      translations,
      mailTemplate,
      templateService,
      nodeService,
      Locale.ENGLISH
    );

    when(
      templateService.processTemplate(
        eq("freemarker"),
        eq(frRef.toString()),
        anyMap()
      )
    ).thenReturn("corps français");

    String body = multiWrapper.getBody(null, Locale.FRENCH);

    assertEquals("corps français", body);
  }

  @Test
  public void testGetBody_withUnknownLocale_fallsToPivot() {
    Map<Locale, NodeRef> translations = Collections.singletonMap(
      Locale.ENGLISH,
      templateRef
    );

    when(
      nodeService.getProperty(templateRef, ContentModel.PROP_NAME)
    ).thenReturn("default.ftl");

    MailWrapperImpl multiWrapper = new MailWrapperImpl(
      translations,
      mailTemplate,
      templateService,
      nodeService,
      Locale.ENGLISH
    );

    when(
      templateService.processTemplate(
        eq("freemarker"),
        eq(templateRef.toString()),
        anyMap()
      )
    ).thenReturn("english body");

    String body = multiWrapper.getBody(null, Locale.GERMAN);

    assertEquals("english body", body);
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
  public void testToString_containsNameAndTemplate() {
    String str = mailWrapper.toString();
    assertTrue(str.contains("default.ftl"));
    assertTrue(str.contains(mailTemplate.toString()));
  }
}
