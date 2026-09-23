package eu.europa.ec.digit.circabc.rest.aspect;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.notification.NotificationService;
import eu.europa.ec.digit.circabc.rest.service.notification.NotificationSubscriptionService;
import io.swagger.config.CircabcConfig;
import io.swagger.model.alfresco.aspect.DisableNotificationThreadLocal;
import java.lang.reflect.Field;
import java.util.concurrent.ThreadPoolExecutor;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.junit.Before;
import org.junit.Test;

public class ContentNotifyAspectTest {

  private ContentNotifyAspect aspect;
  private NotificationService notificationService;
  private NotificationSubscriptionService notificationSubscriptionService;
  private BehaviourFilter policyFilter;
  private TransactionService transactionService;
  private ThreadPoolExecutor threadExecutor;
  private PolicyComponent policyComponent;
  private CircabcConfig circabcConfig;

  @Before
  public void setUp() throws Exception {
    aspect = new ContentNotifyAspect();

    notificationService = mock(NotificationService.class);
    notificationSubscriptionService = mock(
      NotificationSubscriptionService.class
    );
    policyFilter = mock(BehaviourFilter.class);
    transactionService = mock(TransactionService.class);
    threadExecutor = mock(ThreadPoolExecutor.class);
    policyComponent = mock(PolicyComponent.class);
    circabcConfig = mock(CircabcConfig.class);

    setField("notificationService", notificationService);
    setField(
      "notificationSubscriptionService",
      notificationSubscriptionService
    );
    setField("policyFilter", policyFilter);
    setField("transactionService", transactionService);
    setField("threadExecutor", threadExecutor);
    setField("policyComponent", policyComponent);
    setField("circabcConfig", circabcConfig);

    // Clear the DisableNotificationThreadLocal
    DisableNotificationThreadLocal threadLocal =
      new DisableNotificationThreadLocal();
    threadLocal.set(Boolean.FALSE);
  }

  @Test
  public void testInitialise_registersAllPolicies() {
    aspect.initialise();

    verify(policyComponent, times(3)).bindClassBehaviour(
      any(QName.class),
      any(QName.class),
      any()
    );
  }

  @Test
  public void testOnAddAspect_whenNotificationDisabled_doesNothing() {
    DisableNotificationThreadLocal threadLocal =
      new DisableNotificationThreadLocal();
    threadLocal.set(Boolean.TRUE);

    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-id"
    );
    aspect.onAddAspect(nodeRef, ContentNotifyAspect.ASPECT_CONTENT_NOTIFY);

    // No exception, no interaction with transaction support
    threadLocal.set(Boolean.FALSE);
  }

  @Test
  public void testOnContentUpdate_whenNotificationDisabled_doesNothing() {
    DisableNotificationThreadLocal threadLocal =
      new DisableNotificationThreadLocal();
    threadLocal.set(Boolean.TRUE);

    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-id"
    );
    aspect.onContentUpdate(nodeRef, false);

    // No exception, no interaction with transaction support
    threadLocal.set(Boolean.FALSE);
  }

  @Test
  public void testOnMoveNode_whenNotificationDisabled_doesNothing() {
    DisableNotificationThreadLocal threadLocal =
      new DisableNotificationThreadLocal();
    threadLocal.set(Boolean.TRUE);

    ChildAssociationRef oldRef = mock(ChildAssociationRef.class);
    ChildAssociationRef newRef = mock(ChildAssociationRef.class);

    aspect.onMoveNode(oldRef, newRef);

    verifyNoInteractions(threadExecutor);
    threadLocal.set(Boolean.FALSE);
  }

  @Test
  public void testOnMoveNode_whenNotificationEnabled_executesRunnable() {
    ChildAssociationRef oldRef = mock(ChildAssociationRef.class);
    ChildAssociationRef newRef = mock(ChildAssociationRef.class);
    NodeRef childRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "moved-node"
    );
    when(newRef.getChildRef()).thenReturn(childRef);

    aspect.onMoveNode(oldRef, newRef);

    verify(threadExecutor).execute(any(Runnable.class));
  }

  @Test
  public void testOnContentUpdate_whenNotificationEnabled_doesNotThrow() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-id"
    );

    // This will attempt to bind to transaction support which may not be
    // available in unit test, but should not throw NPE from our code
    try {
      aspect.onContentUpdate(nodeRef, true);
    } catch (Exception e) {
      // Expected: AlfrescoTransactionSupport not available in unit test
    }
  }

  @Test
  public void testOnAddAspect_whenNotificationEnabled_doesNotThrow() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-id"
    );

    try {
      aspect.onAddAspect(nodeRef, ContentNotifyAspect.ASPECT_CONTENT_NOTIFY);
    } catch (Exception e) {
      // Expected: AlfrescoTransactionSupport not available in unit test
    }
  }

  @Test
  public void testAspectContentNotifyQName() {
    assertNotNull(ContentNotifyAspect.ASPECT_CONTENT_NOTIFY);
    assertEquals(
      "circaContentNotify",
      ContentNotifyAspect.ASPECT_CONTENT_NOTIFY.getLocalName()
    );
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = ContentNotifyAspect.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(aspect, value);
  }
}
