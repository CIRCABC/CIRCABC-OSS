package eu.europa.ec.digit.circabc.rest.service.statistic.ig;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class ServiceTreeRepresentationTest {

  private ServiceTreeRepresentation representation;

  @Before
  public void setUp() {
    representation = new ServiceTreeRepresentation();
  }

  @Test
  public void testDefaultConstructor_thenFieldsAreNull() {
    assertNull(representation.getName());
    assertNull(representation.getChild());
  }

  @Test
  public void testConstructorWithName_thenNameIsSet() {
    ServiceTreeRepresentation rep = new ServiceTreeRepresentation("Library");
    assertEquals("Library", rep.getName());
    assertNull(rep.getChild());
  }

  @Test
  public void testSetName_whenValidName_thenGetNameReturnsIt() {
    representation.setName("Newsgroup");
    assertEquals("Newsgroup", representation.getName());
  }

  @Test
  public void testSetName_whenNull_thenGetNameReturnsNull() {
    representation.setName("Something");
    representation.setName(null);
    assertNull(representation.getName());
  }

  @Test
  public void testSetChild_whenValidChild_thenGetChildReturnsIt() {
    Child child = new Child();
    representation.setChild(child);
    assertSame(child, representation.getChild());
  }

  @Test
  public void testSetChild_whenNull_thenGetChildReturnsNull() {
    representation.setChild(new Child());
    representation.setChild(null);
    assertNull(representation.getChild());
  }
}
