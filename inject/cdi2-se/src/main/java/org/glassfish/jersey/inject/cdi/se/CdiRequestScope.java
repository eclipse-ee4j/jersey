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

package org.glassfish.jersey.inject.cdi.se;

import javax.inject.Inject;

import org.glassfish.jersey.process.internal.RequestContext;
import org.glassfish.jersey.process.internal.RequestScope;

import org.jboss.weld.context.bound.BoundRequestContext;

/**
 * CDI Request scope implementation using Weld-specific {@link BoundRequestContext} which allows pass on storage for
 * request-scoped objects.
 */
public class CdiRequestScope extends RequestScope {

    @Inject
    private BoundRequestContext requestContextController;

    @Override
    public RequestContext createContext() {
        return new CdiRequestContext();
    }

    @Override
    protected void activate(RequestContext context, RequestContext oldContext) {
        super.activate(context, oldContext);

        if (oldContext != null) {
            CdiRequestContext oldRequestContext = (CdiRequestContext) oldContext;
            requestContextController.deactivate();
            requestContextController.dissociate(oldRequestContext.getStore());
        }

        CdiRequestContext cdiRequestContext = (CdiRequestContext) context;
        requestContextController.associate(cdiRequestContext.getStore());
        requestContextController.activate();
    }

    @Override
    protected void resume(RequestContext context) {
        super.resume(context);

        if (context != null) {
            CdiRequestContext cdiRequestContext = (CdiRequestContext) context;
            requestContextController.associate(cdiRequestContext.getStore());
            requestContextController.activate();
        }
    }

    @Override
    protected void release(RequestContext context) {
        super.release(context);

        CdiRequestContext cdiRequestContext = (CdiRequestContext) context;
        requestContextController.invalidate();
        requestContextController.deactivate();
        requestContextController.dissociate(cdiRequestContext.getStore());
    }

    @Override
    protected void suspend(RequestContext context) {
        if (context != null) {
            CdiRequestContext cdiRequestContext = (CdiRequestContext) context;
            requestContextController.deactivate();
            requestContextController.dissociate(cdiRequestContext.getStore());
        }
    }
}
