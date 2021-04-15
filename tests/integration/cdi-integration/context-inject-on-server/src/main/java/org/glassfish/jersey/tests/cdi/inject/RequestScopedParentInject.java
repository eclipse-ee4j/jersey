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

package org.glassfish.jersey.tests.cdi.inject;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;

public abstract class RequestScopedParentInject extends ParentInject {
    @Context
    ContainerRequestContext contextContainerRequestContext;

    @Inject
    ContainerRequestContext injectContainerRequestContext;

    //  CDI Scoped only
//    @Inject
//    protected javax.enterprise.inject.spi.BeanManager beanManager;

    @Override
    public boolean checkContexted(StringBuilder stringBuilder) {
        boolean contexted = super.checkContexted(stringBuilder);
        contexted &= InjectionChecker.checkContainerRequestContext(contextContainerRequestContext, stringBuilder);
        return contexted;
    }

    @Override
    public boolean checkInjected(StringBuilder stringBuilder) {
        boolean injected = super.checkInjected(stringBuilder);
        injected &= InjectionChecker.checkContainerRequestContext(injectContainerRequestContext, stringBuilder);
        return injected;
    }
}
