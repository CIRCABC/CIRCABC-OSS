package eu.europa.ec.digit.circabc.rest.dynamic.authority;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class CircabcPermissionTest {

  private CircabcPermission permission;

  @Before
  public void setUp() {
    permission = new CircabcPermission();
  }

  @Test
  public void testGetLibraryPermission_whenSet_thenReturnsValue() {
    permission.setLibraryPermission("LibAdmin");
    assertEquals("LibAdmin", permission.getLibraryPermission());
  }

  @Test
  public void testGetLibraryPermission_whenNotSet_thenReturnsNull() {
    assertNull(permission.getLibraryPermission());
  }

  @Test
  public void testGetNewsGroupPermission_whenSet_thenReturnsValue() {
    permission.setNewsGroupPermission("NwsPost");
    assertEquals("NwsPost", permission.getNewsGroupPermission());
  }

  @Test
  public void testGetNewsGroupPermission_whenNotSet_thenReturnsNull() {
    assertNull(permission.getNewsGroupPermission());
  }

  @Test
  public void testGetInformationPermission_whenSet_thenReturnsValue() {
    permission.setInformationPermission("InfAccess");
    assertEquals("InfAccess", permission.getInformationPermission());
  }

  @Test
  public void testGetInformationPermission_whenNotSet_thenReturnsNull() {
    assertNull(permission.getInformationPermission());
  }
}
