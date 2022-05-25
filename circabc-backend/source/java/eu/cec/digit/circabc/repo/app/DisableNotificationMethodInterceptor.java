package eu.cec.digit.circabc.repo.app;

import eu.cec.digit.circabc.aspect.DisableNotificationThreadLocal;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

public class DisableNotificationMethodInterceptor implements MethodInterceptor {

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        final String targetName = invocation.getThis().getClass().getSimpleName();
        final String methodName = invocation.getMethod().getName();
        final Object[] arguments = invocation.getArguments();
        DisableNotificationThreadLocal disableNotificationThreadLocal =
                new DisableNotificationThreadLocal();
        disableNotificationThreadLocal.set(true);
        Object result = invocation.proceed();
        return result;
    }
}
