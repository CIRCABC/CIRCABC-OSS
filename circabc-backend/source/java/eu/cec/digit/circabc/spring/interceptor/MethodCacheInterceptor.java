/*******************************************************************************
 * Copyright 2006 European Community
 *
 *  Licensed under the EUPL, Version 1.1 or - as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  You may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  https://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 ******************************************************************************/
package eu.cec.digit.circabc.spring.interceptor;

import org.alfresco.repo.cache.SimpleCache;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

public class MethodCacheInterceptor implements MethodInterceptor, InitializingBean {

    private static final Log logger = LogFactory.getLog(MethodCacheInterceptor.class);

    private SimpleCache<String, Object> cache;

    /**
     * sets cache name to be used
     */
    public void setCache(final SimpleCache<String, Object> cache) {
        this.cache = cache;
    }

    /**
     * Checks if required attributes are provided.
     */
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(cache, "A cache is required. Use setCache(Cache) to provide one.");
    }

    /**
     * main method caches method result if method is configured for caching method results must be
     * serializable
     */
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        final String targetName = invocation.getThis().getClass().getName();
        final String methodName = invocation.getMethod().getName();
        final Object[] arguments = invocation.getArguments();
        final StringBuffer sb = new StringBuffer();
        final String cacheKey = getCacheKey(targetName, methodName, arguments);
        Object result = cache.get(cacheKey);
        if (logger.isTraceEnabled()) {
            final StringBuilder sbArgs = new StringBuilder();
            for (final Object argument : arguments) {
                sbArgs.append(argument).append(" ");
            }
            sb.append("calling intercepted method with params:\n").append(sbArgs.toString().trim())
                    .append("\n");
        }
        if (result == null) {
            // call target/sub-interceptor
            if (logger.isTraceEnabled()) {
                sb.append(" access not succedded for cache:").append(cache).append("\n");
            }
            result = invocation.proceed();

            // cache method result
            cache.put(cacheKey, result);
        } else {
            if (logger.isTraceEnabled()) {
                sb.append(" access succedded for cache:").append(cache).append("\n");
            }
        }
        if (logger.isTraceEnabled()) {
            sb.append(" return:").append(result).append(" for cache key:").append(cacheKey).append("\n");
            logger.trace(sb);
        }
        return result;
    }

    /**
     * creates cache key: targetName.methodName.argument0.argument1...
     */
    private String getCacheKey(final String targetName, final String methodName,
                               final Object[] arguments) {
        final StringBuilder sb = new StringBuilder();
        sb.append(targetName).append(".").append(methodName);
        if ((arguments != null) && (arguments.length != 0)) {
            for (Object argument : arguments) {
                sb.append(".").append(argument);
            }
        }
        return sb.toString();
    }

}
