package eu.europa.ec.digit.circabc.rest.service.customization.logo;

import static org.junit.Assert.*;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;

public class DefaultLogoConfigurationTest {

  private DefaultLogoConfigurationImpl config;
  private NodeRef configuredOn;
  private NodeRef configurationRef;

  @Before
  public void setUp() {
    configuredOn = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "on-id"
    );
    configurationRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ref-id"
    );
    config = new DefaultLogoConfigurationImpl(configuredOn, configurationRef);
  }

  @Test
  public void testConstructor_defaultValues() {
    assertNull(config.getLogo());
    assertEquals(configuredOn, config.getConfiguredOn());
    assertEquals(configurationRef, config.getConfigurationRef());
    assertFalse(config.isLogoDisplayedOnMainPage());
    assertFalse(config.isLogoDisplayedOnAllPages());
    assertTrue(config.isMainPageLogoAtLeft());
    assertFalse(config.isMainPageSizeForced());
    assertFalse(config.isOtherPagesSizeForced());
    assertEquals(-1, config.getMainPageLogoWidth());
    assertEquals(-1, config.getMainPageLogoHeight());
    assertEquals(-1, config.getOtherPagesLogoWidth());
    assertEquals(-1, config.getOtherPagesLogoHeight());
  }

  @Test
  public void testSetLogo_withNodeRef() {
    NodeRef logoRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "logo-id"
    );
    config.setLogo(logoRef);

    assertNotNull(config.getLogo());
    assertEquals(logoRef, config.getLogo().getReference());
  }

  @Test
  public void testSetLogo_withNullNodeRef_doesNothing() {
    config.setLogo((NodeRef) null);
    assertNull(config.getLogo());
  }

  @Test
  public void testSetLogo_withString() {
    String ref = "workspace://SpacesStore/logo-str-id";
    config.setLogo(ref);

    assertNotNull(config.getLogo());
    assertEquals(new NodeRef(ref), config.getLogo().getReference());
  }

  @Test
  public void testSetLogo_withEmptyString_doesNothing() {
    config.setLogo("");
    assertNull(config.getLogo());
  }

  @Test
  public void testSetLogo_withNullString_doesNothing() {
    config.setLogo((String) null);
    assertNull(config.getLogo());
  }

  @Test
  public void testSetLogo_withFullDetails() {
    NodeRef logoRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "logo-id"
    );
    NodeRef definedOn = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "defined-id"
    );

    config.setLogo(logoRef, definedOn, "name", "title", "desc");

    LogoDefinition logo = config.getLogo();
    assertNotNull(logo);
    assertEquals(logoRef, logo.getReference());
    assertEquals(definedOn, logo.getDefinedOn());
    assertEquals("name", logo.getName());
    assertEquals("title", logo.getTitle());
    assertEquals("desc", logo.getDescription());
  }

  @Test
  public void testSetMainPageLogoConfig() {
    config.setMainPageLogoConfig(true, 200, 300, true, false);

    assertTrue(config.isLogoDisplayedOnMainPage());
    assertEquals(200, config.getMainPageLogoHeight());
    assertEquals(300, config.getMainPageLogoWidth());
    assertTrue(config.isMainPageSizeForced());
    assertFalse(config.isMainPageLogoAtLeft());
  }

  @Test
  public void testSetOtherPagesLogoConfig() {
    config.setOtherPagesLogoConfig(true, 100, 150, true);

    assertTrue(config.isLogoDisplayedOnAllPages());
    assertEquals(100, config.getOtherPagesLogoHeight());
    assertEquals(150, config.getOtherPagesLogoWidth());
    assertTrue(config.isOtherPagesSizeForced());
  }

  @Test
  public void testSetters_withStringValues() {
    config.setMainPageLogoWidth("400");
    config.setMainPageLogoHeight("250");
    config.setMainPageSizeForced("true");
    config.setMainPageLogoAtLeft("false");
    config.setLogoDisplayedOnMainPage("true");
    config.setLogoDisplayedOnAllPages("true");
    config.setOtherPagesLogoWidth("120");
    config.setOtherPagesLogoHeight("80");
    config.setOtherPagesSizeForced("true");

    assertEquals(400, config.getMainPageLogoWidth());
    assertEquals(250, config.getMainPageLogoHeight());
    assertTrue(config.isMainPageSizeForced());
    assertFalse(config.isMainPageLogoAtLeft());
    assertTrue(config.isLogoDisplayedOnMainPage());
    assertTrue(config.isLogoDisplayedOnAllPages());
    assertEquals(120, config.getOtherPagesLogoWidth());
    assertEquals(80, config.getOtherPagesLogoHeight());
    assertTrue(config.isOtherPagesSizeForced());
  }

  @Test
  public void testSetters_withEmptyStrings_noChange() {
    config.setMainPageLogoWidth("");
    config.setMainPageLogoHeight("");
    config.setLogoDisplayedOnMainPage("");
    config.setOtherPagesLogoWidth("");

    assertEquals(-1, config.getMainPageLogoWidth());
    assertEquals(-1, config.getMainPageLogoHeight());
    assertFalse(config.isLogoDisplayedOnMainPage());
    assertEquals(-1, config.getOtherPagesLogoWidth());
  }

  @Test
  public void testCopy_withLogo() {
    NodeRef logoRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "logo-id"
    );
    NodeRef definedOn = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "defined-id"
    );
    config.setLogo(logoRef, definedOn, "name", "title", "desc");
    config.setMainPageLogoConfig(true, 200, 300, true, false);
    config.setOtherPagesLogoConfig(true, 100, 150, true);

    DefaultLogoConfigurationImpl copy = config.copy();

    assertEquals(configuredOn, copy.getConfiguredOn());
    assertEquals(configurationRef, copy.getConfigurationRef());
    assertTrue(copy.isLogoDisplayedOnMainPage());
    assertEquals(200, copy.getMainPageLogoHeight());
    assertEquals(300, copy.getMainPageLogoWidth());
    assertTrue(copy.isMainPageSizeForced());
    assertFalse(copy.isMainPageLogoAtLeft());
    assertTrue(copy.isLogoDisplayedOnAllPages());
    assertEquals(100, copy.getOtherPagesLogoHeight());
    assertEquals(150, copy.getOtherPagesLogoWidth());
    assertTrue(copy.isOtherPagesSizeForced());

    assertNotNull(copy.getLogo());
    assertEquals(logoRef, copy.getLogo().getReference());
    assertEquals(definedOn, copy.getLogo().getDefinedOn());
    assertEquals("name", copy.getLogo().getName());
    assertEquals("title", copy.getLogo().getTitle());
    assertEquals("desc", copy.getLogo().getDescription());
  }

  @Test
  public void testCopy_withoutLogo() {
    DefaultLogoConfigurationImpl copy = config.copy();

    assertNull(copy.getLogo());
    assertEquals(configuredOn, copy.getConfiguredOn());
  }

  @Test
  public void testSetConfigurationRef_twoArgs() {
    NodeRef newOn = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "new-on"
    );
    NodeRef newRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "new-ref"
    );

    config.setConfigurationRef(newOn, newRef);

    assertEquals(newOn, config.getConfiguredOn());
    assertEquals(newRef, config.getConfigurationRef());
  }
}
