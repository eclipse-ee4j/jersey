/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.model;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Singleton;

import org.glassfish.jersey.internal.Errors;
import org.glassfish.jersey.internal.inject.PerLookup;
import org.glassfish.jersey.internal.inject.Providers;
import org.glassfish.jersey.server.internal.LocalizationMessages;

/**
 * Validator ensuring that {@link Invocable invocable} and {@link HandlerConstructor constructor} is correctly defined (for
 * example correctly annotated with scope annotation). This validator is stateful and therefore new instance must be created
 * for each resource model validation.
 *
 * @author Miroslav Fuksa
 *
 */
class InvocableValidator extends AbstractResourceModelVisitor {
    private static final Set<Class<?>> SCOPE_ANNOTATIONS = getScopeAnnotations();
    /**
     * Classes that have been checked already.
     */
    protected final Set<Class<?>> checkedClasses = new HashSet<Class<?>>();

    private static Set<Class<?>> getScopeAnnotations() {
        Set<Class<?>> scopeAnnotations = new HashSet<Class<?>>();
        scopeAnnotations.add(Singleton.class);
        scopeAnnotations.add(PerLookup.class);
        return scopeAnnotations;
    }

    @Override
    public void visitInvocable(final Invocable invocable) {
        // TODO: check invocable.
        Class resClass = invocable.getHandler().getHandlerClass();
        if (resClass != null && !checkedClasses.contains(resClass)) {
            checkedClasses.add(resClass);
            final boolean provider = Providers.isProvider(resClass);
            int counter = 0;
            for (Annotation annotation : resClass.getAnnotations()) {
                if (SCOPE_ANNOTATIONS.contains(annotation.annotationType())) {
                    counter++;
                }
            }
            if (counter == 0 && provider) {
                Errors.warning(resClass, LocalizationMessages.RESOURCE_IMPLEMENTS_PROVIDER(resClass,
                        Providers.getProviderContracts(resClass)));
            } else if (counter > 1) {
                Errors.fatal(resClass, LocalizationMessages.RESOURCE_MULTIPLE_SCOPE_ANNOTATIONS(resClass));
            }
        }


    }

    /**
     * Check if the resource class is declared to be a singleton.
     *
     * @param resourceClass resource class.
     * @return {@code true} if the resource class is a singleton, {@code false} otherwise.
     */
    public static boolean isSingleton(Class<?> resourceClass) {
        return resourceClass.isAnnotationPresent(Singleton.class)
                || (Providers.isProvider(resourceClass) && !resourceClass.isAnnotationPresent(PerLookup.class));
    }


    @Override
    public void visitResourceHandlerConstructor(final HandlerConstructor constructor) {
        Class<?> resClass = constructor.getConstructor().getDeclaringClass();
        boolean isSingleton = isSingleton(resClass);
        int paramCount = 0;
        for (Parameter p : constructor.getParameters()) {
            ResourceMethodValidator.validateParameter(p, constructor.getConstructor(), constructor.getConstructor()
                    .toGenericString(),
                    Integer.toString(++paramCount), isSingleton);
        }
    }


    // TODO: validate also method handler.


}
