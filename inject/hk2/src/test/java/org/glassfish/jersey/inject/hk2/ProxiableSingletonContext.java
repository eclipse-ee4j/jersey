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

import javax.inject.Singleton;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Context;
import org.glassfish.hk2.api.ServiceHandle;

/**
 * The context keeping the objects created in {@link ProxiableSingleton} scope.
 */
@Singleton
public class ProxiableSingletonContext implements Context<ProxiableSingleton> {

    @Override
    public Class<? extends Annotation> getScope() {
        return ProxiableSingleton.class;
    }

    @Override
    public <U> U findOrCreate(ActiveDescriptor<U> activeDescriptor, ServiceHandle<?> root) {
        U cached = activeDescriptor.getCache();
        if (cached != null) {
            return cached;
        }

        cached = activeDescriptor.create(root);
        activeDescriptor.setCache(cached);
        return cached;
    }

    @Override
    public boolean containsKey(ActiveDescriptor<?> descriptor) {
        return descriptor.isCacheSet();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void destroyOne(ActiveDescriptor<?> descriptor) {
        if (!descriptor.isCacheSet()) {
            return;
        }

        Object value = descriptor.getCache();
        descriptor.releaseCache();

        ((ActiveDescriptor<Object>) descriptor).dispose(value);

    }

    @Override
    public boolean supportsNullCreation() {
        return false;
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public void shutdown() {
    }
}
