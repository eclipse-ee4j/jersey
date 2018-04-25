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

package org.glassfish.jersey.server.internal.routing;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.SecurityContext;

import org.glassfish.jersey.server.SubjectSecurityContext;
import org.glassfish.jersey.server.internal.JerseyResourceContext;
import org.glassfish.jersey.server.internal.LocalizationMessages;
import org.glassfish.jersey.server.internal.process.MappableException;
import org.glassfish.jersey.server.internal.process.RequestProcessingContext;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.spi.internal.ParamValueFactoryWithSource;
import org.glassfish.jersey.server.spi.internal.ParameterValueHelper;
import org.glassfish.jersey.server.spi.internal.ValueParamProvider;

/**
 * An methodAcceptorPair to accept sub-resource requests.
 * It first retrieves the sub-resource instance by invoking the given model method.
 * Then the {@link RuntimeLocatorModelBuilder} is used to generate corresponding methodAcceptorPair.
 * Finally the generated methodAcceptorPair is invoked to return the request methodAcceptorPair chain.
 * <p/>
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 * @author Michal Gajdos
 * @author Miroslav Fuksa
 */
final class SubResourceLocatorRouter implements Router {

    private final ResourceMethod locatorModel;
    private final List<ParamValueFactoryWithSource<?>> valueProviders;
    private final RuntimeLocatorModelBuilder runtimeLocatorBuilder;
    private final JerseyResourceContext resourceContext;
    private final Function<Class<?>, ?> createFunction;

    /**
     * Create a new sub-resource locator router.
     *
     * @param createServiceFunction function to create a new service and make other operations (injection).
     * @param valueSuppliers        all registered value suppliers.
     * @param locatorModel          resource locator method model.
     * @param resourceContext       resource context to bind sub-resource locator singleton instances.
     * @param runtimeLocatorBuilder original runtime model builder.
     */
    SubResourceLocatorRouter(final Function<Class<?>, ?> createServiceFunction,
                             final Collection<ValueParamProvider> valueSuppliers,
                             final ResourceMethod locatorModel,
                             final JerseyResourceContext resourceContext,
                             final RuntimeLocatorModelBuilder runtimeLocatorBuilder) {
        this.runtimeLocatorBuilder = runtimeLocatorBuilder;
        this.locatorModel = locatorModel;
        this.resourceContext = resourceContext;
        this.createFunction = createServiceFunction;
        this.valueProviders = ParameterValueHelper.createValueProviders(valueSuppliers, locatorModel.getInvocable());
    }

    @Override
    public Continuation apply(final RequestProcessingContext processingContext) {
        Object subResourceInstance = getResource(processingContext);

        if (subResourceInstance == null) {
            throw new NotFoundException();
        }

        final RoutingContext routingContext = processingContext.routingContext();

        final LocatorRouting routing;
        if (subResourceInstance instanceof Resource) {
            // Caching here is disabled by default. It can be enabled by setting
            // ServerProperties.SUBRESOURCE_LOCATOR_CACHE_JERSEY_RESOURCE_ENABLED to true.
            routing = runtimeLocatorBuilder.getRouting((Resource) subResourceInstance);
        } else {
            Class<?> locatorClass = subResourceInstance.getClass();

            if (locatorClass.isAssignableFrom(Class.class)) {
                // subResourceInstance is class itself
                locatorClass = (Class<?>) subResourceInstance;

                if (!runtimeLocatorBuilder.isCached(locatorClass)) {
                    // If we can't create an instance of the class, don't proceed.
                    subResourceInstance = createFunction.apply(locatorClass);
                }
            }
            routingContext.pushMatchedResource(subResourceInstance);
            resourceContext.bindResourceIfSingleton(subResourceInstance);

            routing = runtimeLocatorBuilder.getRouting(locatorClass);
        }

        routingContext.pushLocatorSubResource(routing.locator.getResources().get(0));
        processingContext.triggerEvent(RequestEvent.Type.SUBRESOURCE_LOCATED);

        return Continuation.of(processingContext, routing.router);
    }

    private Object getResource(final RequestProcessingContext context) {
        final Object resource = context.routingContext().peekMatchedResource();
        final Method handlingMethod = locatorModel.getInvocable().getHandlingMethod();
        final Object[] parameterValues = ParameterValueHelper.getParameterValues(valueProviders, context.request());

        context.triggerEvent(RequestEvent.Type.LOCATOR_MATCHED);

        final PrivilegedAction invokeMethodAction = () -> {
            try {
                return handlingMethod.invoke(resource, parameterValues);
            } catch (IllegalAccessException | IllegalArgumentException | UndeclaredThrowableException ex) {
                throw new ProcessingException(LocalizationMessages.ERROR_RESOURCE_JAVA_METHOD_INVOCATION(), ex);
            } catch (final InvocationTargetException ex) {
                final Throwable cause = ex.getCause();
                if (cause instanceof WebApplicationException) {
                    throw (WebApplicationException) cause;
                }

                // handle all exceptions as potentially mappable (incl. ProcessingException)
                throw new MappableException(cause);
            } catch (final Throwable t) {
                throw new ProcessingException(t);
            }
        };

        final SecurityContext securityContext = context.request().getSecurityContext();
        return (securityContext instanceof SubjectSecurityContext)
                ? ((SubjectSecurityContext) securityContext).doAsSubject(invokeMethodAction) : invokeMethodAction.run();

    }
}
