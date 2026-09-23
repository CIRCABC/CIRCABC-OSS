package eu.europa.ec.digit.circabc.rest.service.iam;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.junit.Before;
import org.junit.Test;

public class IamWSClientImplTest {

  private IamWSClientImpl client;

  @Before
  public void setUp() throws Exception {
    client = new IamWSClientImpl();
    client.setServiceUrl("http://localhost:9999/iam");
    client.setUser("testUser");
    client.setPassword("testPassword");
  }

  @Test
  public void testGetSetServiceUrl() {
    client.setServiceUrl("http://example.com");
    assertEquals("http://example.com", client.getServiceUrl());
  }

  @Test
  public void testGetSetUser() {
    client.setUser("admin");
    assertEquals("admin", client.getUser());
  }

  @Test
  public void testGetSetPassword() {
    client.setPassword("secret");
    assertEquals("secret", client.getPassword());
  }

  @Test
  public void testCalculatePasswordDigest_returnsNonNull() throws Exception {
    Method method = IamWSClientImpl.class.getDeclaredMethod(
      "calculatePasswordDigest",
      String.class,
      String.class,
      String.class
    );
    method.setAccessible(true);

    String nonce = "313233343536";
    String created = "2024-01-01T00:00:00Z";
    String password = "testPassword";

    String result = (String) method.invoke(null, nonce, created, password);

    assertNotNull(result);
    assertFalse(result.isEmpty());
  }

  @Test
  public void testCalculatePasswordDigest_differentInputsProduceDifferentDigests()
    throws Exception {
    Method method = IamWSClientImpl.class.getDeclaredMethod(
      "calculatePasswordDigest",
      String.class,
      String.class,
      String.class
    );
    method.setAccessible(true);

    String digest1 = (String) method.invoke(
      null,
      "313233343536",
      "2024-01-01T00:00:00Z",
      "password1"
    );
    String digest2 = (String) method.invoke(
      null,
      "313233343536",
      "2024-01-01T00:00:00Z",
      "password2"
    );

    assertNotEquals(digest1, digest2);
  }

  @Test
  public void testHexEncode() throws Exception {
    Method method = IamWSClientImpl.class.getDeclaredMethod(
      "hexEncode",
      String.class
    );
    method.setAccessible(true);

    // "41" hex = 'A', "42" hex = 'B'
    String result = (String) method.invoke(null, "4142");
    assertEquals("AB", result);
  }

  @Test
  public void testHexEncode_emptyString() throws Exception {
    Method method = IamWSClientImpl.class.getDeclaredMethod(
      "hexEncode",
      String.class
    );
    method.setAccessible(true);

    String result = (String) method.invoke(null, "");
    assertEquals("", result);
  }

  @Test
  public void testBuildRequestData_containsUserAndThemeAndRole()
    throws Exception {
    Method method = IamWSClientImpl.class.getDeclaredMethod(
      "buildRequestData",
      String.class,
      String.class,
      String.class,
      String.class
    );
    method.setAccessible(true);

    String template =
      "<v3:UserId>%5$s</v3:UserId><v3:ThemeId>%6$s</v3:ThemeId><v3:Role>%7$s</v3:Role>" +
      "<wsse:Username>%1$s</wsse:Username>" +
      "<wsse:Password>%2$s</wsse:Password>" +
      "<wsse:Nonce>%3$s</wsse:Nonce>" +
      "<wsu:Created>%4$s</wsu:Created>";

    String result = (String) method.invoke(
      client,
      template,
      "userId123",
      "theme456",
      "roleAdmin"
    );

    assertTrue(result.contains("userId123"));
    assertTrue(result.contains("theme456"));
    assertTrue(result.contains("roleAdmin"));
    assertTrue(result.contains("testUser"));
  }

  @Test(expected = IamWSEception.class)
  public void testGrantThemeRole_whenConnectionFails_throwsIamWSEception() {
    client.setServiceUrl("http://127.0.0.1:1/nonexistent");
    client.grantThemeRole("user1", "theme1", "role1");
  }

  @Test(expected = IamWSEception.class)
  public void testRevokeThemeRole_whenConnectionFails_throwsIamWSEception() {
    client.setServiceUrl("http://127.0.0.1:1/nonexistent");
    client.revokeThemeRole("user1", "theme1", "role1");
  }

  @Test
  public void testGetNonce_returnsNonEmptyString() throws Exception {
    Method method = IamWSClientImpl.class.getDeclaredMethod("getNonce");
    method.setAccessible(true);

    String nonce = (String) method.invoke(null);

    assertNotNull(nonce);
    assertFalse(nonce.isEmpty());
  }

  @Test
  public void testGetNonce_returnsDifferentValues() throws Exception {
    Method method = IamWSClientImpl.class.getDeclaredMethod("getNonce");
    method.setAccessible(true);

    // Call multiple times — statistically should differ
    boolean foundDifferent = false;
    String first = (String) method.invoke(null);
    for (int i = 0; i < 10; i++) {
      String next = (String) method.invoke(null);
      if (!first.equals(next)) {
        foundDifferent = true;
        break;
      }
    }
    assertTrue("getNonce should produce varying values", foundDifferent);
  }
}
