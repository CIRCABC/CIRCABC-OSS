package eu.europa.ec.digit.circabc.rest.service.iam;

import static org.junit.Assert.*;

import org.junit.Test;

public class SynchronizationServiceTest {

  @Test
  public void testDefaultEcordaRoleConstant() {
    assertEquals("THEME_MEMBER", SynchronizationService.DEFAULT_ECORDA_ROLE);
  }

  @Test
  public void testImplementationImplementsInterface() {
    SynchronizationServiceImpl impl = new SynchronizationServiceImpl();
    assertTrue(impl instanceof SynchronizationService);
  }
}
