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
 * This one will get injected with a CDI producer.
 * HK2 should not mess up with this.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@RequestScoped
@Path("producer")
public class ProducerResource {

    @Inject
    MethodProducedBean<String> m;

    @Inject
    FieldProducedBean<String> f;

    /**
     * Return field produced bean value.
     *
     * @return value from field produced bean.
     */
    @Path("f")
    @GET
    public String getFieldValue() {
        return f.getValue();
    }

    /**
     * Return method produced bean value.
     *
     * @return value from method produced bean.
     */
    @Path("m")
    @GET
    public String getMethodValue() {
        return m.getValue();
    }
}
