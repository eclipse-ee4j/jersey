/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.ext.cdi1x.internal;

import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.ws.rs.core.Context;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

/**
 * Client side runtime CDI ComponentProvider specific implementation.
 */
class CdiComponentProviderClientRuntimeSpecifics implements CdiComponentProviderRuntimeSpecifics {
    /*
     * annotation types that distinguish the classes to be added to jaxrsInjectableTypes
     */
    private static final Set<Class<? extends Annotation>> JAX_RS_INJECT_ANNOTATIONS =
            new HashSet<Class<? extends Annotation>>() {{
                add(Context.class);
            }};

    @Override
    public boolean containsJaxRsParameterizedCtor(AnnotatedType annotatedType) {
        return false;
    }

    @Override
    public Set<Class<? extends Annotation>> getJaxRsInjectAnnotations() {
        return JAX_RS_INJECT_ANNOTATIONS;
    }

    @Override
    public AnnotatedParameter<?> getAnnotatedParameter(AnnotatedParameter<?> ap) {
        return ap;
    }

    @Override
    public boolean isAcceptableResource(Class<?> resource) {
        return false;
    }

    @Override
    public boolean isJaxRsResource(Class<?> resource) {
        return false;
    }
}
