package eu.europa.ec.digit.circabc.rest.service.customization.logo;

import static org.junit.Assert.*;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;

public class LogoDefinitionImplTest {

  private LogoDefinitionImpl logoDefinition;

  @Before
  public void setUp() {
    logoDefinition = new LogoDefinitionImpl();
  }

  @Test
  public void testGetReference_whenNotSet_thenReturnsNull() {
    assertNull(logoDefinition.getReference());
  }

  @Test
  public void testSetReference_whenSet_thenGetReturnsValue() {
    NodeRef ref = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "logo-id"
    );
    logoDefinition.setReference(ref);
    assertEquals(ref, logoDefinition.getReference());
  }

  @Test
  public void testSetReference_whenSetToNull_thenGetReturnsNull() {
    logoDefinition.setReference(
      new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "id")
    );
    logoDefinition.setReference(null);
    assertNull(logoDefinition.getReference());
  }

  @Test
  public void testGetDefinedOn_whenNotSet_thenReturnsNull() {
    assertNull(logoDefinition.getDefinedOn());
  }

  @Test
  public void testSetDefinedOn_whenSet_thenGetReturnsValue() {
    NodeRef ref = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "defined-on-id"
    );
    logoDefinition.setDefinedOn(ref);
    assertEquals(ref, logoDefinition.getDefinedOn());
  }

  @Test
  public void testGetName_whenNotSet_thenReturnsNull() {
    assertNull(logoDefinition.getName());
  }

  @Test
  public void testSetName_whenSet_thenGetReturnsValue() {
    logoDefinition.setName("logo.png");
    assertEquals("logo.png", logoDefinition.getName());
  }

  @Test
  public void testGetTitle_whenNotSet_thenReturnsNull() {
    assertNull(logoDefinition.getTitle());
  }

  @Test
  public void testSetTitle_whenSet_thenGetReturnsValue() {
    logoDefinition.setTitle("My Logo");
    assertEquals("My Logo", logoDefinition.getTitle());
  }

  @Test
  public void testGetDescription_whenNotSet_thenReturnsNull() {
    assertNull(logoDefinition.getDescription());
  }

  @Test
  public void testSetDescription_whenSet_thenGetReturnsValue() {
    logoDefinition.setDescription("A description");
    assertEquals("A description", logoDefinition.getDescription());
  }

  @Test
  public void testIsSerializable() {
    assertTrue(logoDefinition instanceof java.io.Serializable);
  }

  @Test
  public void testImplementsLogoDefinition() {
    assertTrue(logoDefinition instanceof LogoDefinition);
  }
}
