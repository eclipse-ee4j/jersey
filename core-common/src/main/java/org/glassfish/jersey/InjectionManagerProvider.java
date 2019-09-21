/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey;

import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.ReaderInterceptorContext;
import javax.ws.rs.ext.WriterInterceptorContext;

import org.glassfish.jersey.internal.LocalizationMessages;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.InjectionManagerSupplier;

/**
 * Utility class with static methods that extract {@link InjectionManager injection manager}
 * from various JAX-RS components. This class can be used when no injection is possible by
 * {@link javax.ws.rs.core.Context} or {@link javax.inject.Inject} annotation due to character of
 * provider but there is a need to get any service from {@link InjectionManager}.
 * <p>
 * Injections are not possible for example when a provider is registered as an instance on the client.
 * In this case the runtime will not inject the instance as this instance might be used in other client
 * runtimes too.
 * </p>
 * <p>
 * Example. This is the class using a standard injection:
 * <pre>
 *     public static class MyWriterInterceptor implements WriterInterceptor {
 *         &#64;Inject
 *         MyInjectedService service;
 *
 *         &#64;Override
 *         public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
 *             Something something = service.getSomething();
 *             ...
 *         }
 *
 *     }
 * </pre>
 * </p>
 * <p>
 * If this injection is not possible then this construct can be used:
 * <pre>
 *     public static class MyWriterInterceptor implements WriterInterceptor {
 *
 *         &#64;Override
 *         public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
 *             InjectionManager injectionManager = InjectionManagerProvider.getInjectionManager(context);
 *             MyInjectedService service = injectionManager.getInstance(MyInjectedService.class);
 *             Something something = service.getSomething();
 *             ...
 *         }
 *     }
 * </pre>
 * </p>
 * <p>
 * Note, that this injection support is intended mostly for injection of custom user types. JAX-RS types
 * are usually available without need for injections in method parameters. However, when injection of custom
 * type is needed it is preferred to use standard injections if it is possible rather than injection support
 * provided by this class.
 * </p>
 * <p>
 * User returned {@code InjectionManager} only for purpose of getting services (do not change the state of the injection manager).
 * </p>
 *
 *
 * @author Miroslav Fuksa
 *
 * @since 2.6
 */
public class InjectionManagerProvider {

    /**
     * Extract and return injection manager from {@link javax.ws.rs.ext.WriterInterceptorContext writerInterceptorContext}.
     * The method can be used to inject custom types into a {@link javax.ws.rs.ext.WriterInterceptor}.
     *
     * @param writerInterceptorContext Writer interceptor context.
     *
     * @return injection manager.
     *
     * @throws java.lang.IllegalArgumentException when {@code writerInterceptorContext} is not a default
     * Jersey implementation provided by Jersey as argument in the
     * {@link javax.ws.rs.ext.WriterInterceptor#aroundWriteTo(javax.ws.rs.ext.WriterInterceptorContext)} method.
     */
    public static InjectionManager getInjectionManager(WriterInterceptorContext writerInterceptorContext) {
        if (!(writerInterceptorContext instanceof InjectionManagerSupplier)) {
            throw new IllegalArgumentException(
                    LocalizationMessages.ERROR_SERVICE_LOCATOR_PROVIDER_INSTANCE_FEATURE_WRITER_INTERCEPTOR_CONTEXT(
                            writerInterceptorContext.getClass().getName()));
        }
        return ((InjectionManagerSupplier) writerInterceptorContext).getInjectionManager();
    }

    /**
     * Extract and return injection manager from {@link javax.ws.rs.ext.ReaderInterceptorContext readerInterceptorContext}.
     * The method can be used to inject custom types into a {@link javax.ws.rs.ext.ReaderInterceptor}.
     *
     * @param readerInterceptorContext Reader interceptor context.
     *
     * @return injection manager.
     *
     * @throws java.lang.IllegalArgumentException when {@code readerInterceptorContext} is not a default
     * Jersey implementation provided by Jersey as argument in the
     * {@link javax.ws.rs.ext.ReaderInterceptor#aroundReadFrom(javax.ws.rs.ext.ReaderInterceptorContext)} method.

     */
    public static InjectionManager getInjectionManager(ReaderInterceptorContext readerInterceptorContext) {
        if (!(readerInterceptorContext instanceof InjectionManagerSupplier)) {
            throw new IllegalArgumentException(
                    LocalizationMessages.ERROR_SERVICE_LOCATOR_PROVIDER_INSTANCE_FEATURE_READER_INTERCEPTOR_CONTEXT(
                            readerInterceptorContext.getClass().getName()));
        }
        return ((InjectionManagerSupplier) readerInterceptorContext).getInjectionManager();
    }

    /**
     * Extract and return injection manager from {@link javax.ws.rs.core.FeatureContext featureContext}.
     * The method can be used to inject custom types into a {@link javax.ws.rs.core.Feature}.
     * <p>
     * Note that features are utilized during initialization phase when not all providers are registered yet.
     * It is undefined which injections are already available in this phase.
     * </p>
     *
     * @param featureContext Feature context.
     *
     * @return injection manager.
     *
     * @throws java.lang.IllegalArgumentException when {@code writerInterceptorContext} is not a default
     * Jersey instance provided by Jersey
     * in {@link javax.ws.rs.core.Feature#configure(javax.ws.rs.core.FeatureContext)} method.
     */
    public static InjectionManager getInjectionManager(FeatureContext featureContext) {
        if (!(featureContext instanceof InjectionManagerSupplier)) {
            throw new IllegalArgumentException(
                    LocalizationMessages.ERROR_SERVICE_LOCATOR_PROVIDER_INSTANCE_FEATURE_CONTEXT(
                            featureContext.getClass().getName()));
        }
        return ((InjectionManagerSupplier) featureContext).getInjectionManager();
    }
}
