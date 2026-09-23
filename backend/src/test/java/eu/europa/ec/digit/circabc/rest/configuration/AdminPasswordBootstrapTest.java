package eu.europa.ec.digit.circabc.rest.configuration;

import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.event.ContextRefreshedEvent;

public class AdminPasswordBootstrapTest {

  private AdminPasswordBootstrap bootstrap;
  private MutableAuthenticationService authenticationService;

  @Before
  public void setUp() throws Exception {
    bootstrap = new AdminPasswordBootstrap();
    authenticationService = mock(MutableAuthenticationService.class);
    setField("authenticationService", authenticationService);

    Field initialized = AuthenticationUtil.class.getDeclaredField(
      "initialized"
    );
    initialized.setAccessible(true);
    initialized.set(null, true);
    Field guest = AuthenticationUtil.class.getDeclaredField(
      "defaultGuestUserName"
    );
    guest.setAccessible(true);
    guest.set(null, "guest");
    AuthenticationUtil.setFullyAuthenticatedUser("testuser");
  }

  @Test
  public void testOnBootstrap_whenPasswordConfigured_thenSetsAuthentication()
    throws Exception {
    setField("adminPassword", "secret123");

    bootstrap.onBootstrap(mock(ContextRefreshedEvent.class));

    verify(authenticationService).setAuthentication(
      "admin",
      "secret123".toCharArray()
    );
  }

  @Test
  public void testOnBootstrap_whenPasswordNull_thenSkips() throws Exception {
    setField("adminPassword", null);

    bootstrap.onBootstrap(mock(ContextRefreshedEvent.class));

    verifyNoInteractions(authenticationService);
  }

  @Test
  public void testOnBootstrap_whenPasswordBlank_thenSkips() throws Exception {
    setField("adminPassword", "   ");

    bootstrap.onBootstrap(mock(ContextRefreshedEvent.class));

    verifyNoInteractions(authenticationService);
  }

  @Test
  public void testOnBootstrap_whenServiceThrows_thenNoExceptionPropagated()
    throws Exception {
    setField("adminPassword", "pass");
    doThrow(new RuntimeException("fail"))
      .when(authenticationService)
      .setAuthentication(anyString(), any(char[].class));

    bootstrap.onBootstrap(mock(ContextRefreshedEvent.class));
    // no exception propagated
  }

  @Test
  public void testOnShutdown_doesNothing() {
    bootstrap.onShutdown(mock(ContextRefreshedEvent.class));
    verifyNoInteractions(authenticationService);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = AdminPasswordBootstrap.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(bootstrap, value);
  }
}
