/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.internal.inject;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import javax.ws.rs.BeanParam;
import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Configuration;

import javax.inject.Provider;

import org.glassfish.jersey.internal.BootstrapBag;
import org.glassfish.jersey.internal.BootstrapConfigurator;
import org.glassfish.jersey.internal.inject.Bindings;
import org.glassfish.jersey.internal.inject.ContextInjectionResolver;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.Injections;
import org.glassfish.jersey.internal.util.collection.LazyValue;
import org.glassfish.jersey.internal.util.collection.Value;
import org.glassfish.jersey.internal.util.collection.Values;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ServerBootstrapBag;
import org.glassfish.jersey.server.Uri;
import org.glassfish.jersey.server.AsyncContext;
import org.glassfish.jersey.server.internal.process.RequestProcessingContextReference;
import org.glassfish.jersey.server.spi.internal.ValueParamProvider;

/**
 * Configurator which initializes and register {@link ValueParamProvider} instances into {@link InjectionManager} and
 * {@link BootstrapBag}.
 *
 * @author Petr Bouda
 */
public class ValueParamProviderConfigurator implements BootstrapConfigurator {

    public void init(InjectionManager injectionManager, BootstrapBag bootstrapBag) {
        ServerBootstrapBag serverBag = (ServerBootstrapBag) bootstrapBag;

        // Provide request scoped AsyncContext without the proxy.
        Provider<AsyncContext> asyncContextProvider = () -> {
            RequestProcessingContextReference reference = injectionManager.getInstance(RequestProcessingContextReference.class);
            return reference.get().asyncContext();
        };

        // Provide ContextInjectionResolver that is implemented by Injection Provider.
        LazyValue<ContextInjectionResolver> lazyContextResolver =
                Values.lazy((Value<ContextInjectionResolver>) () -> injectionManager.getInstance(ContextInjectionResolver.class));

        Supplier<Configuration> configuration = serverBag::getConfiguration;
        Provider<MultivaluedParameterExtractorProvider> paramExtractor = serverBag::getMultivaluedParameterExtractorProvider;

        // Parameter injection value providers
        Collection<ValueParamProvider> suppliers = new ArrayList<>();

        AsyncResponseValueParamProvider asyncProvider = new AsyncResponseValueParamProvider(asyncContextProvider);
        suppliers.add(asyncProvider);

        CookieParamValueParamProvider cookieProvider = new CookieParamValueParamProvider(paramExtractor);
        suppliers.add(cookieProvider);

        EntityParamValueParamProvider entityProvider = new EntityParamValueParamProvider(paramExtractor);
        suppliers.add(entityProvider);

        FormParamValueParamProvider formProvider = new FormParamValueParamProvider(paramExtractor);
        suppliers.add(formProvider);

        HeaderParamValueParamProvider headerProvider = new HeaderParamValueParamProvider(paramExtractor);
        suppliers.add(headerProvider);

        MatrixParamValueParamProvider matrixProvider = new MatrixParamValueParamProvider(paramExtractor);
        suppliers.add(matrixProvider);

        PathParamValueParamProvider pathProvider = new PathParamValueParamProvider(paramExtractor);
        suppliers.add(pathProvider);

        QueryParamValueParamProvider queryProvider = new QueryParamValueParamProvider(paramExtractor);
        suppliers.add(queryProvider);

        BeanParamValueParamProvider beanProvider = new BeanParamValueParamProvider(paramExtractor, injectionManager);
        suppliers.add(beanProvider);

        WebTargetValueParamProvider webTargetProvider = new WebTargetValueParamProvider(configuration,
                clientConfigClass -> Injections.getOrCreate(injectionManager, clientConfigClass));
        suppliers.add(webTargetProvider);

        DelegatedInjectionValueParamProvider contextProvider =
                new DelegatedInjectionValueParamProvider(lazyContextResolver, injectionManager::createForeignDescriptor);
        suppliers.add(contextProvider);

        serverBag.setValueParamProviders(Collections.unmodifiableCollection(suppliers));

        // Needs to be in InjectionManager because of CdiComponentProvider
        injectionManager.register(Bindings.service(asyncProvider).to(ValueParamProvider.class));
        injectionManager.register(Bindings.service(cookieProvider).to(ValueParamProvider.class));
        injectionManager.register(Bindings.service(formProvider).to(ValueParamProvider.class));
        injectionManager.register(Bindings.service(headerProvider).to(ValueParamProvider.class));
        injectionManager.register(Bindings.service(matrixProvider).to(ValueParamProvider.class));
        injectionManager.register(Bindings.service(pathProvider).to(ValueParamProvider.class));
        injectionManager.register(Bindings.service(queryProvider).to(ValueParamProvider.class));
        injectionManager.register(Bindings.service(webTargetProvider).to(ValueParamProvider.class));
        injectionManager.register(Bindings.service(beanProvider).to(ValueParamProvider.class));
        injectionManager.register(Bindings.service(entityProvider).to(ValueParamProvider.class));
        injectionManager.register(Bindings.service(contextProvider).to(ValueParamProvider.class));

        // Provide request scoped ContainerRequest without the proxy.
        Provider<ContainerRequest> request = () -> {
            RequestProcessingContextReference reference = injectionManager.getInstance(RequestProcessingContextReference.class);
            return reference.get().request();
        };

        registerResolver(injectionManager, asyncProvider, Suspended.class, request);
        registerResolver(injectionManager, cookieProvider, CookieParam.class, request);
        registerResolver(injectionManager, formProvider, FormParam.class, request);
        registerResolver(injectionManager, headerProvider, HeaderParam.class, request);
        registerResolver(injectionManager, matrixProvider, MatrixParam.class, request);
        registerResolver(injectionManager, pathProvider, PathParam.class, request);
        registerResolver(injectionManager, queryProvider, QueryParam.class, request);
        registerResolver(injectionManager, webTargetProvider, Uri.class, request);
        registerResolver(injectionManager, beanProvider, BeanParam.class, request);
    }

    private void registerResolver(InjectionManager im, ValueParamProvider vfp, Class<? extends Annotation> annotation,
            Provider<ContainerRequest> request) {
        im.register(Bindings.injectionResolver(new ParamInjectionResolver<>(vfp, annotation, request)));
    }

    @Override
    public void postInit(InjectionManager injectionManager, BootstrapBag bootstrapBag) {
        // Add the ValueSupplierProviders which has been added to ResourceConfig/Feature
        List<ValueParamProvider> addedInstances = injectionManager.getAllInstances(ValueParamProvider.class);
        if (!addedInstances.isEmpty()) {
            ServerBootstrapBag serverBag = (ServerBootstrapBag) bootstrapBag;
            addedInstances.addAll(serverBag.getValueParamProviders());
            serverBag.setValueParamProviders(Collections.unmodifiableCollection(addedInstances));
        }
    }
}
