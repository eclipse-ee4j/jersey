/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.jersey.microprofile.restclient;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.spi.InterceptionType;
import javax.enterprise.inject.spi.Interceptor;
import javax.interceptor.InvocationContext;
import javax.ws.rs.client.WebTarget;

/**
 * Invokes all interceptors bound to the target.
 *
 * This approach needs to be used due to CDI does not handle properly interceptor invocation
 * on proxy instances.
 *
 * @author David Kral
 */
class InterceptorInvocationContext implements InvocationContext {

    private final MethodModel methodModel;
    private final Method method;
    private final Map<String, Object> contextData;
    private final List<InvocationInterceptor> interceptors;
    private final WebTarget classLevelWebTarget;
    private Object[] args;
    private int currentPosition;

    /**
     * Creates new instance of InterceptorInvocationContext.
     *
     * @param classLevelWebTarget class level web target
     * @param methodModel method model
     * @param method reflection method
     * @param args actual method arguments
     */
    InterceptorInvocationContext(WebTarget classLevelWebTarget,
                                 MethodModel methodModel,
                                 Method method,
                                 Object[] args) {
        this.contextData = new HashMap<>();
        this.currentPosition = 0;
        this.methodModel = methodModel;
        this.method = method;
        this.args = args;
        this.classLevelWebTarget = classLevelWebTarget;
        this.interceptors = methodModel.getInvocationInterceptors();

    }

    @Override
    public Object getTarget() {
        return methodModel;
    }

    @Override
    public Object getTimer() {
        return null;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public Constructor<?> getConstructor() {
        return null;
    }

    @Override
    public Object[] getParameters() {
        return args;
    }

    @Override
    public void setParameters(Object[] params) {
        this.args = params;
    }

    @Override
    public Map<String, Object> getContextData() {
        return contextData;
    }

    @Override
    public Object proceed() {
        if (currentPosition < interceptors.size()) {
            return interceptors.get(currentPosition++).intercept(this);
        } else {
            return methodModel.invokeMethod(classLevelWebTarget, method, args);
        }
    }

    /**
     * Contains actual interceptor instance and interceptor itself.
     */
    static class InvocationInterceptor {

        private final Object interceptorInstance;
        private final Interceptor interceptor;

        InvocationInterceptor(Object interceptorInstance, Interceptor interceptor) {
            this.interceptorInstance = interceptorInstance;
            this.interceptor = interceptor;
        }

        /**
         * Invokes interceptor with interception type AROUND_INVOKE.
         *
         * @param ctx invocation context
         * @return interception result
         */
        @SuppressWarnings("unchecked")
        Object intercept(InvocationContext ctx) {
            try {
                return interceptor.intercept(InterceptionType.AROUND_INVOKE, interceptorInstance, ctx);
            } catch (Exception e) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                }
                throw new RuntimeException(e);
            }
        }
    }
}
