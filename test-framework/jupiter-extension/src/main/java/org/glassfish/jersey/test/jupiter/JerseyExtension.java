/*
 * Copyright (c) 2010, 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.test.jupiter;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.function.Supplier;

import static org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import static org.junit.jupiter.api.extension.ExtensionContext.Store;

public class JerseyExtension implements TestInstancePostProcessor, BeforeEachCallback, AfterEachCallback, ParameterResolver {

    private static final Namespace NAMESPACE = Namespace.create(JerseyExtension.class);

    private final Supplier<Application> applicationSupplier;

    @SuppressWarnings("WeakerAccess")
    public JerseyExtension(Supplier<Application> applicationSupplier) {
        this.applicationSupplier = applicationSupplier;
    }

    @SuppressWarnings("unused")
    public JerseyExtension() {
        applicationSupplier = null;
    }

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext extensionContext) {
        // TODO: validate predonditions
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        Object testInstance = extensionContext.getRequiredTestInstance();

        JerseyTestImpl jerseyTest = new JerseyTestImpl(findApplication(testInstance));
        jerseyTest.setUp();

        injectFields(testInstance, jerseyTest);

        Store store = store(extensionContext);
        store.put(JerseyTestImpl.class, jerseyTest);
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) throws Exception {
        store(extensionContext).remove(JerseyTestImpl.class, JerseyTestImpl.class).tearDown();
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        return supportsInjectable(parameterContext.getParameter().getType());
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        JerseyTestImpl jerseyTest = store(extensionContext).get(JerseyTestImpl.class, JerseyTestImpl.class);
        return resolveInjectable(parameterContext.getParameter().getType(), jerseyTest);
    }

    private Application findApplication(Object testInstance) throws Exception {
        if (applicationSupplier != null) {
            return applicationSupplier.get();
        }
        for (Method method : testInstance.getClass().getDeclaredMethods()) {
            if (Application.class.isAssignableFrom(method.getReturnType()) && method.getParameterCount() == 0) {
                method.setAccessible(true);
                return (Application) method.invoke(testInstance);
            }
        }
        throw new IllegalStateException("Couldn't find a way to configure Application");
    }

    private static void injectFields(Object testInstance, JerseyTestImpl jerseyTest) throws IllegalAccessException {
        for (Field field : testInstance.getClass().getDeclaredFields()) {
            if (supportsInjectable(field.getType())) {
                field.setAccessible(true);
                field.set(testInstance, resolveInjectable(field.getType(), jerseyTest));
            }
        }
    }

    private static boolean supportsInjectable(Class<?> type) {
        return type == WebTarget.class
                || type == Client.class
                || type == URI.class;
    }

    private static Object resolveInjectable(Class<?> type, JerseyTestImpl jerseyTest) {
        if (type == WebTarget.class) {
            return jerseyTest.target();
        }
        if (type == Client.class) {
            return jerseyTest.client();
        }
        if (type == URI.class) {
            return jerseyTest.baseUri();
        }
        throw new IllegalArgumentException("Unsupported injectable type " + type);
    }

    private static Store store(ExtensionContext extensionContext) {
        return extensionContext.getStore(NAMESPACE);
    }

    private static class JerseyTestImpl extends org.glassfish.jersey.test.JerseyTest {

        JerseyTestImpl(Application application) {
            super(application);
        }

        URI baseUri() {
            return getBaseUri();
        }
    }
}
