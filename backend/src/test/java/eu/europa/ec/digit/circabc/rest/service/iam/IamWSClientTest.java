package eu.europa.ec.digit.circabc.rest.service.iam;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class IamWSClientTest {

  private IamWSClientImpl client;

  @Before
  public void setUp() {
    client = new IamWSClientImpl();
    client.setServiceUrl("http://127.0.0.1:1/nonexistent");
    client.setUser("testUser");
    client.setPassword("testPassword");
  }

  @Test
  public void testImplementsIamWSClient() {
    assertTrue(client instanceof IamWSClient);
  }

  @Test(expected = IamWSEception.class)
  public void testGrantThemeRole_throwsOnConnectionFailure() {
    client.grantThemeRole("user1", "theme1", "role1");
  }

  @Test(expected = IamWSEception.class)
  public void testRevokeThemeRole_throwsOnConnectionFailure() {
    client.revokeThemeRole("user1", "theme1", "role1");
  }
}
