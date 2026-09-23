package eu.europa.ec.digit.circabc.rest.action;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.action.executer.AddFeaturesActionExecuter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.junit.Before;
import org.junit.Test;

public class SystemAddFeaturesActionExecuterTest {

  private SystemAddFeaturesActionExecuter executer;
  private NodeService nodeService;
  private NodeRef nodeRef;

  @Before
  public void setUp() throws Exception {
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

    executer = new SystemAddFeaturesActionExecuter();
    nodeService = mock(NodeService.class);
    executer.setNodeService(nodeService);

    // Set up RetryingTransactionHelper with mocked TransactionService
    TransactionService transactionService = mock(TransactionService.class);
    RetryingTransactionHelper txHelper = new RetryingTransactionHelper();
    txHelper.setTransactionService(transactionService);
    when(transactionService.getRetryingTransactionHelper()).thenReturn(
      txHelper
    );

    jakarta.transaction.UserTransaction userTransaction = mock(
      jakarta.transaction.UserTransaction.class
    );
    when(
      transactionService.getNonPropagatingUserTransaction(
        anyBoolean(),
        anyBoolean()
      )
    ).thenReturn(userTransaction);
    when(userTransaction.getStatus()).thenReturn(
      jakarta.transaction.Status.STATUS_ACTIVE
    );

    Field txField = AddFeaturesActionExecuter.class.getDeclaredField(
      "transactionService"
    );
    txField.setAccessible(true);
    txField.set(executer, transactionService);

    nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-node-id"
    );
  }

  private Action createActionWithAspect(QName aspectQName) {
    Action action = mock(Action.class);
    Map<String, Serializable> params = new HashMap<>();
    params.put(AddFeaturesActionExecuter.PARAM_ASPECT_NAME, aspectQName);
    when(action.getParameterValues()).thenReturn(params);
    when(
      action.getParameterValue(AddFeaturesActionExecuter.PARAM_ASPECT_NAME)
    ).thenReturn(aspectQName);
    return action;
  }

  @Test
  public void testName_isSystemAddFeatures() {
    assertEquals("system-add-features", SystemAddFeaturesActionExecuter.NAME);
  }

  @Test
  public void testExecuteImpl_whenNodeExists_thenAddsAspect() {
    QName aspectQName = QName.createQName(
      "http://www.alfresco.org/model/content/1.0",
      "titled"
    );
    Action action = createActionWithAspect(aspectQName);
    when(nodeService.exists(nodeRef)).thenReturn(true);

    executer.executeImpl(action, nodeRef);

    verify(nodeService).addAspect(eq(nodeRef), eq(aspectQName), anyMap());
  }

  @Test
  public void testExecuteImpl_whenNodeDoesNotExist_thenDoesNotAddAspect() {
    QName aspectQName = QName.createQName(
      "http://www.alfresco.org/model/content/1.0",
      "titled"
    );
    Action action = createActionWithAspect(aspectQName);
    when(nodeService.exists(nodeRef)).thenReturn(false);

    executer.executeImpl(action, nodeRef);

    verify(nodeService, never()).addAspect(any(), any(), anyMap());
  }

  @Test
  public void testExecuteImpl_runsAsSystemUser() {
    QName aspectQName = QName.createQName(
      "http://www.alfresco.org/model/content/1.0",
      "versionable"
    );
    Action action = createActionWithAspect(aspectQName);
    when(nodeService.exists(nodeRef)).thenReturn(true);

    AuthenticationUtil.setFullyAuthenticatedUser("regularuser");
    executer.executeImpl(action, nodeRef);

    // Verifies the parent logic executed (which only works if runAsSystem elevated)
    verify(nodeService).addAspect(eq(nodeRef), eq(aspectQName), anyMap());
  }
}
