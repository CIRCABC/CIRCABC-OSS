package eu.europa.ec.digit.circabc.rest.service.customization.logo;

import static org.junit.Assert.*;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;

public class LogoDefinitionTest {

  private LogoDefinitionImpl logoDefinition;

  @Before
  public void setUp() {
    logoDefinition = new LogoDefinitionImpl();
  }

  @Test
  public void testGetReference_whenSet_thenReturnsNodeRef() {
    NodeRef ref = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "logo-id"
    );
    logoDefinition.setReference(ref);
    assertEquals(ref, logoDefinition.getReference());
  }

  @Test
  public void testGetReference_whenNotSet_thenReturnsNull() {
    assertNull(logoDefinition.getReference());
  }

  @Test
  public void testGetDefinedOn_whenSet_thenReturnsNodeRef() {
    NodeRef ref = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "defined-on-id"
    );
    logoDefinition.setDefinedOn(ref);
    assertEquals(ref, logoDefinition.getDefinedOn());
  }

  @Test
  public void testGetName_whenSet_thenReturnsName() {
    logoDefinition.setName("test-logo.png");
    assertEquals("test-logo.png", logoDefinition.getName());
  }

  @Test
  public void testGetTitle_whenSet_thenReturnsTitle() {
    logoDefinition.setTitle("Test Logo Title");
    assertEquals("Test Logo Title", logoDefinition.getTitle());
  }

  @Test
  public void testGetDescription_whenSet_thenReturnsDescription() {
    logoDefinition.setDescription("A test logo description");
    assertEquals("A test logo description", logoDefinition.getDescription());
  }

  @Test
  public void testGetters_whenNothingSet_thenReturnNulls() {
    assertNull(logoDefinition.getReference());
    assertNull(logoDefinition.getDefinedOn());
    assertNull(logoDefinition.getName());
    assertNull(logoDefinition.getTitle());
    assertNull(logoDefinition.getDescription());
  }
}
