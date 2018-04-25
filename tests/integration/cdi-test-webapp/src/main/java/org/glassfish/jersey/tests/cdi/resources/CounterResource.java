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

package org.glassfish.jersey.tests.cdi.resources;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * Part of JERSEY-2461 reproducer. This one will get injected with a CDI extension.
 * HK2 should not mess up with this.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@RequestScoped
@Path("counter")
public class CounterResource {

    final CustomExtension e;

    /**
     * To make CDI happy... namely to make the bean proxy-able.
     */
    public CounterResource() {
        this.e = null;
    }

    /**
     * This one will get used at runtime actually.
     *
     * @param extension current application CDI custom extension.
     */
    @Inject
    public CounterResource(CustomExtension extension) {
        this.e = extension;
    }

    /**
     * Return custom extension counter state.
     *
     * @return next count.
     */
    @GET
    public int getCount() {
        return e.getCount();
    }
}
