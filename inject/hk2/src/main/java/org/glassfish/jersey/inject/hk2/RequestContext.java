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

package org.glassfish.jersey.inject.hk2;

import java.lang.annotation.Annotation;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.glassfish.jersey.internal.inject.ForeignDescriptor;
import org.glassfish.jersey.process.internal.RequestScope;
import org.glassfish.jersey.process.internal.RequestScoped;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Context;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

/**
 * Class is able to communicate with {@link RequestScope} and provide request-scoped descriptors to HK2 DI provider to create or
 * destroy instances.
 */
@Singleton
public class RequestContext implements Context<RequestScoped> {

    private final RequestScope requestScope;

    @Inject
    public RequestContext(RequestScope requestScope) {
        this.requestScope = requestScope;
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return RequestScoped.class;
    }

    @Override
    public <U> U findOrCreate(ActiveDescriptor<U> activeDescriptor, ServiceHandle<?> root) {
        Hk2RequestScope.Instance instance = (Hk2RequestScope.Instance) requestScope.current();

        U retVal = instance.get(ForeignDescriptor.wrap(activeDescriptor));
        if (retVal == null) {
            retVal = activeDescriptor.create(root);
            instance.put(ForeignDescriptor.wrap(activeDescriptor, obj -> activeDescriptor.dispose((U) obj)), retVal);
        }
        return retVal;
    }

    @Override
    public boolean containsKey(ActiveDescriptor<?> descriptor) {
        Hk2RequestScope.Instance instance = (Hk2RequestScope.Instance) requestScope.current();
        return instance.contains(ForeignDescriptor.wrap(descriptor));
    }

    @Override
    public boolean supportsNullCreation() {
        return true;
    }

    @Override
    public boolean isActive() {
        return requestScope.isActive();
    }

    @Override
    public void destroyOne(ActiveDescriptor<?> descriptor) {
        Hk2RequestScope.Instance instance = (Hk2RequestScope.Instance) requestScope.current();
        instance.remove(ForeignDescriptor.wrap(descriptor));
    }

    @Override
    public void shutdown() {
        requestScope.shutdown();
    }

    /**
     * Request scope injection binder.
     */
    public static class Binder extends AbstractBinder {

        @Override
        protected void configure() {
            bindAsContract(RequestContext.class)
                    .to(new TypeLiteral<Context<RequestScoped>>() {}.getType())
                    .in(Singleton.class);
        }
    }
}
